package com.nomad.producer.messages;


import com.nomad.producer.nomad.City;

public record DataCollectionJob(String jobId, JobType type, City sourceCity, City destinationCity, String dateToCollectResults) {}