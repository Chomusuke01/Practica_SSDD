package es.um.sisdist.models;

import java.util.ArrayList;

import es.um.sisdist.backend.dao.models.KeyValue;
import es.um.sisdist.backend.dao.models.Userdb;

public class BD_DTOUtils {

	public static ArrayList<KeyValue> fromDTO(BD_DTO bd) {
		ArrayList<KeyValueDTO> lista = bd.getD();
		ArrayList<KeyValue> returnlist = new ArrayList<>();
		for (KeyValueDTO keyvalue : lista) {
            KeyValue kv = new KeyValue();
            kv.setK(keyvalue.getK());
            kv.setV(keyvalue.getV());

            returnlist.add(kv);
        }
		return returnlist;
	}
	
	public static ArrayList<KeyValueDTO> toDTO(ArrayList<KeyValue> kv){
		ArrayList<KeyValueDTO> lista = new ArrayList<>();
		for (KeyValue keyvalue : kv) {
			KeyValueDTO dto = new KeyValueDTO();
			dto.setK(keyvalue.getK());
			dto.setV(keyvalue.getV());
			lista.add(dto);
		}
		return lista;
	}
	
	
	public static BD_DTO toBD_DTO(Userdb userdb) {
		BD_DTO bddto = new BD_DTO();
		bddto.setD(toDTO(userdb.getD()));
		bddto.setDbname(userdb.getId());
		return bddto;
	}
}
