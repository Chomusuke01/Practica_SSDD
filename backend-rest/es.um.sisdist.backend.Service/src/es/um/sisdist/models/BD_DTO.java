package es.um.sisdist.models;

import java.util.ArrayList;


public class BD_DTO {

	private String dbname;
	
	private ArrayList<KeyValueDTO> d;
	
	public String getDbname() {
		return dbname;
	}
	
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public ArrayList<KeyValueDTO> getD() {
		return new ArrayList<>(d);
	}

	public void setD(ArrayList<KeyValueDTO> d) {
		this.d = new ArrayList<>(d);
	}
	
	
}
