package es.um.sisdist.models;

public class MR_DTO {
	
	private String map;
	private String reduce;
	private String out_db;
	
	public String getMap() {
		return map;
	}
	public void setMap(String map) {
		this.map = map;
	}
	public String getReduce() {
		return reduce;
	}
	public void setReduce(String reduce) {
		this.reduce = reduce;
	}
	public String getOut_db() {
		return out_db;
	}
	public void setOut_db(String out_db) {
		this.out_db = out_db;
	}
	public MR_DTO() {
		
	}
	
	public MR_DTO(String map, String reduce, String out_db) {
		
		this.map = map;
		this.reduce = reduce;
		this.out_db = out_db;
	}
	
	
}
