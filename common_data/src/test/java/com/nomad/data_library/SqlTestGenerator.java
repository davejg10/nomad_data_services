package com.nomad.data_library;

import com.nomad.data_library.domain.sql.SqlCity;
import com.nomad.data_library.domain.sql.SqlCountry;

public class SqlTestGenerator {

    public static SqlCountry sqlCountry(String countryName) {
        return SqlCountry.of(countryName, "some description");
    }

    public static SqlCity sqlCity(String cityName, SqlCountry country) {
        return SqlCity.of(cityName, "some description", GenericTestGenerator.cityMetrics(), country.getId());
    }
}
