package com.nomad.job_orchestrator.exceptions;

public class InvalidJobConfig extends Exception {
    
  public InvalidJobConfig(String reason) {
    super("Job config error; " + reason);
  }

}