package com.nomad.producer.exceptions;

public class InvalidJobConfig extends Exception {
    
  public InvalidJobConfig(String reason) {
    super("Job config error; " + reason);
  }

}