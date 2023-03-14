package es.um.sisdist.backend.dao.models;

import java.io.Serializable;

public class KeyValue implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Object k;
	
	private Object v;
	
	public Object getK() {
		return k;
	}
	public void setKint(int k) {
		this.k = k;
	}
	public void setKfloat(float k) {
		this.k = k;
	}
	public void setKstring(String k) {
		this.k = k;
	}
	public Object getV() {
		return v;
	}
	public void setVint(int v) {
		this.v = v;
	}
	public void setVfloat(float v) {
		this.v = v;
	}
	public void setVstring(String v) {
		this.v = v;
	}
}
