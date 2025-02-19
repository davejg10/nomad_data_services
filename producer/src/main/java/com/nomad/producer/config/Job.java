package com.nomad.producer.config;

import com.nomad.producer.messages.JobType;

public record Job(String id, JobType type, String countryName, boolean isActive, String dateToCollectResults) {}
