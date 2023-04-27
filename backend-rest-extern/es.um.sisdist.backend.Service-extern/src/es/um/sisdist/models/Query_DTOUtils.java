package es.um.sisdist.models;

import java.util.ArrayList;

import es.um.sisdist.backend.dao.models.KeyValue;

public class Query_DTOUtils {

	public static Query_DTO toDTO(String dbname, String pattern, int page, int perpage, ArrayList<KeyValue> dto) {
		Query_DTO q = new Query_DTO();
		q.setD(BD_DTOUtils.toDTO(dto));
		q.setDbname(dbname);
		q.setPage(page);
		q.setPattern(pattern);
		q.setPerpage(perpage);
		return q;
	}
}
