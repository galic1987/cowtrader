package otradotra.models;

public class MarketTraderKey {
 private String key;
 private String secret;
 private String name;
 private long current_nonce;
 
 
public MarketTraderKey(String key, String secret, String name, long nonce) {
	super();
	this.key = key;
	this.secret = secret;
	this.name = name;
	this.current_nonce = nonce;
}



public String getKey() {
	return key;
}
public void setKey(String key) {
	this.key = key;
}
public String getSecret() {
	return secret;
}
public void setSecret(String secret) {
	this.secret = secret;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}



public long getCurrent_nonce() {
	return current_nonce;
}



public void setCurrent_nonce(long current_nonce) {
	this.current_nonce = current_nonce;
}
}
