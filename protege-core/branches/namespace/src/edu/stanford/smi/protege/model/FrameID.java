package edu.stanford.smi.protege.model;


/**
 
 */
public class FrameID {
  private String name;
  private boolean isSystem = false;
  
  public FrameID(String name) {
    this.name = name;
  }
  
  public FrameID(String name, boolean isSystem) {
    this(name);
    this.isSystem = isSystem;
  }
  
  public String getName() {
    return name;
  }
  
  public boolean isSystem() {
    return isSystem;
  }
  
  
  public boolean equals(Object o) {
    if (!(o instanceof FrameID)) {
      return false;
    }
    FrameID other = (FrameID) o;
    return name.equals(other.name) && isSystem == other.isSystem;
  }
  
  public final int hashCode() {
    return name.hashCode() + (isSystem ? 8 : 42);
  }
  
  public boolean isUser() {
    return !isSystem();
  }
  
  public String toString() {
    return (isSystem ? "SystemFrameID(" : "FrameID(") + name + ")";
  }
}