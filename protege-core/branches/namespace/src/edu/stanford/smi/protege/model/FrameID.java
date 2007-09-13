package edu.stanford.smi.protege.model;

import java.io.Serializable;


/**
 
 */
public class FrameID implements Serializable {
  private String name;
  
  public FrameID(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof FrameID)) {
      return false;
    }
    FrameID other = (FrameID) o;
    return name.equals(other.name);
  }
  
  public final int hashCode() {
    return name.hashCode() + 42;
  }
  
  public String toString() {
    return "FrameID(" + name + ")";
  }
}