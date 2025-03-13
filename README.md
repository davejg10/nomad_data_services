# Data Services

This is a multi-module Maven Java project containing modules for the various data utilities used with the Nomad app. These are primarily concerned with fetching/scraping route information. The current module list is as follows;

1) admin_api -> A rest API deployed to Azure Function Apps, used to create the countries and cities in the Postgres & Neo4j databases. It initially creates the entity in Postgres and then later syncs this to Neo4j - replicating the entity ids.
2) common -> Library module containing re-usable code
3) job_orchestrator -> Another component deployed to Azure Functions Apps. Contains Functions with various triggers i.e cron, api, Service Bus. This component handles composing the jobs for the scrapers.
4) one2goasia -> The first web scraper built for Nomad. This scraper is deployed to Azure Container Apps Jobs and is used to scrape the 12goAsia website. It spins up & down on demand based on the jobs created by the job_orchestrator module above.

## Local development/Testing

Pretty difficult to do as there are alot of dependencies; Neo4j DB, Postgres DB, Azure Functions, Azure Service Bus. All of these can be mocked, hosted locally except for Azure Service Bus.

### Local development

As mentioned there are four dependencies we have to overcome;

1) Neo4j: We run a docker container for Neo4j.;

```
docker run --publish=7474:7474 --publish=7687:7687 -e 'NEO4J_AUTH=neo4j/mypassword' neo4j:5
```
> The default active Spring profile connects to this DB automatically

2) Postgres: DB is run in-memory using Springs H2 module. 

> The default active Spring profile connects to this DB automatically

3) Azure Functions: We can run the Azure Functions locally by first building the project (from the project root):

```
mvn clean package -pl <module-name> -am
```

and then running the Azure Functions (this time from within the module directory)

```
mvn azure-functions:run
```

4) Azure Service Bus: Technically you can mock any Azure Functions trigger using a rest endpoint, however we also use Service Bus SDK within our code. Currently we just use the `dev` Service Bus instance directly.


### Testing 
At the moment the Unit tests use an in memory neo4j database & in memory postgres DB. The Postgres schema is created on startup using [psql_schema.sql](./common/src/main/resources/local/psql_schema.sql). Within the Unit Tests we dont actually test the Azure Functions but test the classes/functions that these Azure Functions call. The code within the Azure Function block is kept as simple as possible for this reason. In this way we can also mock an incoming Service Bus message by just passing a Json message as a string to the method as param.

Azure Functions are currently tested manually by following the approach shown in `Local Development` above.

## Log tracing

In order to trace a log you can use the following query. (Where x is the value of the correlationId)
```
union 
    AppTraces
| extend logData = parse_json(Message)
| where logData.contextMap.correlationId == "x"
   or Properties.correlationId == "x"

```

## Spring profiles

## admin_api
Command to build 
```
[from root]
mvn clean package -pl admin_api -am -DskipTests
cd admin_api
mvn azure-functions:run
```
## common

## job_orchestrator
Command to build 
```
[from root]
mvn clean package -pl job_orchestrator -am -DskipTests
cd job_orchestrator
mvn azure-functions:run
```
## one2goasia

Command to build 
```
[from root]
mvn clean install -pl one2goasia -am -DskipTests
cd one2goasia
mvn spring-boot:run
```