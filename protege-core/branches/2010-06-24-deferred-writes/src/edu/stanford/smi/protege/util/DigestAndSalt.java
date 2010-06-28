package edu.stanford.smi.protege.util;

public class DigestAndSalt {
	private String digest;
	private String salt;
	
	public DigestAndSalt(String digest, String salt) {
		super();
		this.digest = digest;
		this.salt = salt;
	}
	
	public String getDigest() {
		return digest;
	}
	
	public String getSalt() {
		return salt;
	}
}
