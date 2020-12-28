package com.ef.mediaroutingengine.exceptions;

public class NotFoundException extends RuntimeException {
  public NotFoundException(String message){
      super(message);
  }
}
