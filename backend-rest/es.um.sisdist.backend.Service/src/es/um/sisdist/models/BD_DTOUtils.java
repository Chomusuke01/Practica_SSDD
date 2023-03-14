package es.um.sisdist.models;

import java.util.ArrayList;

import es.um.sisdist.backend.dao.models.KeyValue;

public class BD_DTOUtils {

	public static ArrayList<KeyValue> fromDTO(BD_DTO bd) {
		ArrayList<KeyValueDTO> lista = bd.getD();
		ArrayList<KeyValue> returnlist = new ArrayList<>();
		for (KeyValueDTO keyvalue : lista) {
            KeyValue kv = new KeyValue();
            try {
                kv.setKint(Integer.parseInt(keyvalue.getK().toString()));
            } catch (NumberFormatException e) {
                try {
                    kv.setKfloat(Float.parseFloat(keyvalue.getK().toString()));
                } catch (NumberFormatException e1) {
                    kv.setKstring(String.valueOf(kv.getK()));
                }
            }

            try {
                kv.setVint(Integer.parseInt(keyvalue.getV().toString()));
            } catch (NumberFormatException e) {
                try {
                    kv.setVfloat(Float.parseFloat(keyvalue.getV().toString()));
                } catch (NumberFormatException e1) {
                    kv.setVstring(String.valueOf(kv.getV()));
                }
            }

            returnlist.add(kv);
        }
		return returnlist;
	}
}
