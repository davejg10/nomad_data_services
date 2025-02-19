package com.nomad.consumer.messages;

import com.nomad.consumer.nomad.City;

public record DataCollectionJob(String jobId, JobType type, City sourceCity, City destinationCity, String dateToCollectResults) {}