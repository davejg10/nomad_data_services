#!/bin/bash
ENV=$1
# --- Configuration ---
API_BASE_URL="http://localhost:7071/api" # Replace with your actual API base URL
if [[ "$http_status" -eq 409 ]]; then
    API_BASE_URL="https://fa-$ENV-uks-nomad-02-admin-api/api"
fi

echo "Environment is $ENV, therefore API_BASE_URL is: $API_BASE_URL"

COUNTRIES_FILE="./data/countries.json"
CITIES_FILE="./data/cities.json"
# Optional: Add headers if needed, e.g., for authentication
# CURL_HEADERS=(-H "Authorization: Bearer YOUR_TOKEN")
# CURL_HEADERS=(-H "Content-Type: application/json")

# --- Helper Functions ---

# Function to check if jq and curl are installed
check_deps() {
  if ! command -v jq &> /dev/null; then
    echo "Error: 'jq' command not found. Please install jq."
    exit 1
  fi
  if ! command -v curl &> /dev/null; then
    echo "Error: 'curl' command not found. Please install curl."
    exit 1
  fi
}

# Function to make API call and handle response
# Usage: make_api_call <METHOD> <ENDPOINT> <JSON_PAYLOAD>
# Returns: HTTP status code
# Outputs: Response body to stdout if successful or non-409 error
#          entityId to stdout if 409 conflict
make_api_call() {
    local method="$1"
    local endpoint="$2"
    local payload="$3"
    local full_url="${API_BASE_URL}${endpoint}" # Adjust if endpoint already has /api

    # Use process substitution to avoid issues with multi-line response body/status separation
    local response_output
    local http_status

    # -s: silent, -w: write-out format, ${CURL_HEADERS[@]}: expand array of headers
    response_output=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
                      -X "$method" \
                      -d "$payload" \
                      "$full_url")

    # Extract status code (last line)
    http_status=$(echo "$response_output" | tail -n1 | cut -d: -f2)

    # Extract body (everything except last line)
    local response_body
    response_body=$(echo "$response_output" | sed '$d')

    # --- Response Handling ---
    if [[ "$http_status" -eq 409 ]]; then
        local entity_id
        # Use jq to safely extract entityId, handle null/missing
        entity_id=$(echo "$response_body" | jq -r '.entityId // empty')

        if [[ -z "$entity_id" || "$entity_id" == "null" ]]; then
            echo "Error: Received 409 Conflict but could not extract entityId from response." >&2
            echo "Response Body: $response_body" >&2
            # Decide whether to return an error status or just the 409
            # Returning 409 indicates conflict, but caller needs to know ID extraction failed
            # Let's return a custom code or just log and return 409
            return 409 # Indicates conflict, caller should check logs
        else
            # Output entityId for the caller (update function)
            echo "$entity_id"
            return 409 # Still return 409 to signal conflict occurred
        fi
    elif [[ "$http_status" -ge 200 && "$http_status" -lt 300 ]]; then
        # Success (2xx)
        echo "$response_body" # Output success response body (might be useful)
        return 0 # Using 0 for success seems more standard for shell functions
    else
        # Other error
        echo "Error: API call failed ($method $endpoint)." >&2
        echo "Status: $http_status" >&2
        echo "Response: $response_body" >&2
        return 1 # General error
    fi
}

# --- Main Script ---

check_deps

echo "--- Processing Countries (${COUNTRIES_FILE}) ---"

# Read the countries file, process each object
jq -c '.[]' "$COUNTRIES_FILE" | while IFS= read -r country_json; do
    country_name=$(echo "$country_json" | jq -r '.name')
    echo "Processing Country: $country_name"

    # Attempt to create
    entity_id_or_body=$(make_api_call "POST" "/createCountry" "$country_json")
    
    create_status=$?

    echo "and then create status IS: $create_status"
    echo "and the entityid is: $entity_id_or_body"

    if [[ "$create_status" -eq 0 ]]; then
        echo "  -> Country '$country_name' created successfully."
    elif [[ "$create_status" -eq 153 ]]; then # Note 153 not 409 BECAUSE../ Shell function exit statuses (what $? reads) are limited to values between 0 and 255. When you use return N where N is greater than 255, the shell typically takes the value modulo 256.
        # Conflict detected, entity_id_or_body contains the ID
        entity_id="$entity_id_or_body"
        if [[ -n "$entity_id" ]]; then
             echo "  -> Conflict (409) detected for '$country_name'. Entity ID: $entity_id. Attempting update..."
             # Attempt to update
             update_response=$(make_api_call "PUT" "/updateCountry/${entity_id}" "$country_json")
             update_status=$?

             if [[ "$update_status" -eq 0 ]]; then
                 echo "  -> Country '$country_name' (ID: $entity_id) updated successfully."
             else
                 echo "  -> Error updating country '$country_name' (ID: $entity_id). Status: $update_status"
             fi
        else
             echo "  -> Conflict (409) detected for '$country_name', but failed to get entityId. Skipping update."
        fi
    else
        echo "  -> Error creating country '$country_name'. Status: $create_status"
        # Decide if you want to stop the script on error: exit 1
    fi
    echo # Add a newline for readability
done

echo # Add a newline for readability
echo "--- Processing Cities (${CITIES_FILE}) ---"

# Read the cities file, process each object
jq -c '.[]' "$CITIES_FILE" | while IFS= read -r city_json; do
    city_name=$(echo "$city_json" | jq -r '.name')
    country_name=$(echo "$city_json" | jq -r '.countryName')
    echo "Processing City: $city_name (Country: $country_name)"

    # Attempt to create
    entity_id_or_body=$(make_api_call "POST" "/createCity" "$city_json")
    create_status=$?

    if [[ "$create_status" -eq 0 ]]; then
        echo "  -> City '$city_name' created successfully."
    elif [[ "$create_status" -eq 153 ]]; then # Note 153 not 409 BECAUSE../ Shell function exit statuses (what $? reads) are limited to values between 0 and 255. When you use return N where N is greater than 255, the shell typically takes the value modulo 256.
        # Conflict detected, entity_id_or_body contains the ID
        entity_id="$entity_id_or_body"
         if [[ -n "$entity_id" ]]; then
            echo "  -> Conflict (409) detected for '$city_name'. Entity ID: $entity_id. Attempting update..."
            # Attempt to update
            update_response=$(make_api_call "PUT" "/updateCity/${entity_id}" "$city_json")
            update_status=$?

            if [[ "$update_status" -eq 0 ]]; then
                echo "  -> City '$city_name' (ID: $entity_id) updated successfully."
            else
                echo "  -> Error updating city '$city_name' (ID: $entity_id). Status: $update_status"
            fi
        else
             echo "  -> Conflict (409) detected for '$city_name', but failed to get entityId. Skipping update."
        fi
    else
        echo "  -> Error creating city '$city_name'. Status: $create_status"
        # Decide if you want to stop the script on error: exit 1
    fi
    echo # Add a newline for readability
done

echo "--- Script Finished ---"