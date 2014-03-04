package otradotra.models;

public class MarketTraderKey {
 private String key;
 private String secret;
 private String name;
 
 
public MarketTraderKey(String key, String secret, String name) {
	super();
	this.key = key;
	this.secret = secret;
	this.name = name;
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
}
