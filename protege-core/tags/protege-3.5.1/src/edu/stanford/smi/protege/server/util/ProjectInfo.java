package edu.stanford.smi.protege.server.util;

import java.io.Serializable;

public class ProjectInfo implements Serializable {
	
	private static final long serialVersionUID = -1889764365957203408L;
	
	private String name;
	private String description;
	private String owner;
		
	public ProjectInfo(String name, String description, String owner) {
		super();
		this.name = name;
		this.description = description;
		this.owner = owner;
	}
	
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public String getOwner() {
		return owner;
	}
	
	
}
