package es.um.sisdist.backend.dao.models;

import java.util.ArrayList;

public class Userdb {

	private String id;
	
	private ArrayList<KeyValue> d;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<KeyValue> getD() {
		return d;
	}

	public void setD(ArrayList<KeyValue> d) {
		this.d = d;
	}
	
}
