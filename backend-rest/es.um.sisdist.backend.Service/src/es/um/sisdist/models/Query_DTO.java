package es.um.sisdist.models;

import java.util.ArrayList;

public class Query_DTO {

	private String dbname;
	private String pattern;
	private int page;
	private int perpage;
	private ArrayList<KeyValueDTO> d;
	
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getPerpage() {
		return perpage;
	}
	public void setPerpage(int perpage) {
		this.perpage = perpage;
	}
	public ArrayList<KeyValueDTO> getD() {
		return new ArrayList<> (d);
	}
	public void setD(ArrayList<KeyValueDTO> d) {
		this.d = new ArrayList<> (d);
	}
	
}
