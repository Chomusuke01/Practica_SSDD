package es.um.sisdist.backend.dao.models;

public class KeyValue {

	private Object k;
	private Object v;

    public Object getK() {
		return k;
	}

	public void setK(Object k) {
		this.k = k;
	}

	public Object getV() {
		return v;
	}

	public void setV(Object v) {
		this.v = v;
	}
	
	
	public KeyValue(Object k, Object v) {
		this.k = k;
		this.v = v;
	}

	public KeyValue() {
		
	}
}
