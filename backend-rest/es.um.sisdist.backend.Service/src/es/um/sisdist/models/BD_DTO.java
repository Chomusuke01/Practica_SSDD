package es.um.sisdist.models;

import java.util.HashMap;


public class BD_DTO {

	private String dbname;
	private HashMap<Object, Object> d;
	
	public String getDbname() {
		return dbname;
	}
	
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	
	public HashMap<Object, Object> getD() {
		return new HashMap<>(d);
	}
	
	public void setD(HashMap<Object, Object> d) {
		this.d = new HashMap<>(d);
	}
	
}
