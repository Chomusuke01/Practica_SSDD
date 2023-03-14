package es.um.sisdist.backend.dao.models;

import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

public class KeyValue implements Bson{

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

	@Override
    public <TDocument> BsonDocument toBsonDocument(Class<TDocument> documentClass, CodecRegistry codecRegistry) {
        BsonDocument doc = new BsonDocument();
        
        try {
        	int k = Integer.parseInt(this.getK().toString());
        	doc.put("k", new BsonInt32(k));
		} catch (NumberFormatException e) {
			// TODO: handle exception
			try {
					float k = Float.parseFloat(this.getK().toString());
					doc.put("k", new BsonDouble(k));
					
			} catch (NumberFormatException e2) {
				// TODO: handle exception
				String k = String.valueOf(this.getK());
				doc.put("k", new BsonString(k));
			}
		
		}
        
        try {
        	int v = Integer.parseInt(this.getV().toString());
        	doc.put("v", new BsonInt32(v));
		} catch (NumberFormatException e) {
			// TODO: handle exception
			try {
					float v = Float.parseFloat(this.getV().toString());
					doc.put("v", new BsonDouble(v));
					
			} catch (NumberFormatException e2) {
				// TODO: handle exception
				String v = String.valueOf(this.getV());
				doc.put("v", new BsonString(v));
			}
		
		}
        
        return doc;
	}
	
}
