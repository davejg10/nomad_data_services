package com.nomad.common_utils.domain;

import java.util.Arrays;
import java.util.List;

public enum TransportType {
    BUS, FLIGHT, TAXI, TRAIN, VAN;


    public static List<String> to12GoAsiaList() {
        return Arrays.stream(TransportType.values()).map(enumValue -> to12GoAsiaString(enumValue.name())).toList();
    }

    public static String to12GoAsiaString(String enumValue) {
        return enumValue.substring(0, 1) + enumValue.substring(1).toLowerCase();
    }
}
