/**
 *
 */
package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.push;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.Conventions;
import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import es.um.sisdist.backend.dao.models.KeyValue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.Userdb;
import es.um.sisdist.backend.dao.models.utils.UserUtils;

import es.um.sisdist.backend.dao.utils.Lazy;


/**
 * @author dsevilla
 *
 */
public class MongoUserDAO implements IUserDAO
{
    private Supplier<MongoCollection<User>> collection;
    private String uri = "mongodb://root:root@" 
    		+ Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
            + ":27017/ssdd?authSource=admin";
    
    public MongoUserDAO()
    {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().conventions(asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();
        
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
                 
        // Replace the uri string with your MongoDB deployment's connection string

        collection = Lazy.lazily(() -> 
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
        		.withCodecRegistry(pojoCodecRegistry);
        	return database.getCollection("users", User.class);
        });
       
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("id", id)).first());
        return user;
    }

    @Override
    public Optional<User> getUserByEmail(String id)
    {
        Optional<User> user = Optional.ofNullable(collection.get().find(eq("email", id)).first());
        return user;
    }

	@Override
	public Optional<User> newUser(String name, String id, String email, String password) {
		
		Optional<User> user = getUserByEmail(email);
		if (user.isPresent()) {
			return Optional.empty();
		}
		
		User u = new User(id, email, UserUtils.md5pass(password), name, UserUtils.md5pass(id), 0);
		
		collection.get().insertOne(u);
		
		return Optional.of(u);
	}

	@Override
	public void updateVisits(User u) {
		
		collection.get().updateOne(eq("email", u.getEmail()), set("visits", u.getVisits() + 1));
	}
	

	@Override
	public Optional<String> newBBDD(String userID, String bdID, ArrayList<KeyValue> kv) {
		
		collection.get().updateOne(eq("id", userID), push("bbdd", bdID));
		
		Supplier<MongoCollection<Document>> dbUserCollection = Lazy.lazily(() -> 
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"));
        	return database.getCollection(bdID);
        });
		
		for (KeyValue keyValue: kv) {
			Document doc = new Document();
			
			doc.append("k", String.valueOf(keyValue.getK()));
			doc.append("v", String.valueOf(keyValue.getV()));
			dbUserCollection.get().insertOne(doc);
		}
		
		return Optional.of(bdID);
	}

	
	@Override
	public Optional<Userdb> getUserdbById(String id) {
		
		Supplier<MongoCollection<Document>> dbUserCollection = Lazy.lazily(() -> 
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"));
        	return database.getCollection(id);
        });

		ArrayList<KeyValue> kv = new ArrayList<>();
		FindIterable<Document> docs = dbUserCollection.get().find();
		
		MongoCursor<Document> cursor = docs.iterator();
		
		while (cursor.hasNext()) {
			
			Document doc = cursor.next();
			Object k = doc.get("k");
			Object v = doc.get("v");
			KeyValue keyValue = new KeyValue();
			
			try {
				int key = Integer.parseInt(String.valueOf(k));
				keyValue.setK(key);
			}catch (NumberFormatException e) {
				
				try {
					float key = Float.parseFloat(String.valueOf(k));
					keyValue.setK(key);
				}catch (NumberFormatException e1) {
					String key = String.valueOf(k);
					keyValue.setK(key);
				}
			}
			
			try {
				int value = Integer.parseInt(String.valueOf(v));
				keyValue.setV(value);
			}catch (NumberFormatException e) {
				
				try {
					float value = Float.parseFloat(String.valueOf(v));
					keyValue.setV(value);
				}catch (NumberFormatException e1) {
					String value = String.valueOf(v);
					keyValue.setV(value);
				}
			}
			
			kv.add(keyValue);
		}
		Userdb DB = new Userdb();
		DB.setD(kv);
		DB.setId(id);
		
		return Optional.of(DB);
	}

	@Override
	public Optional<Userdb> getDatabases(String userID, String bdID) {
		
		Optional<Userdb> userdb = getUserdbById(bdID);
		
		return userdb;
	}

	@Override
	public boolean addKeyValue(String userID, String key, String value, String dbID) {
		
		Optional<User> u = getUserById(userID);
		
		if (u.isPresent()) {
			
			if (u.get().getBbdd().contains(dbID)) {
				
				Supplier<MongoCollection<Document>> dbUserCollection = Lazy.lazily(() -> 
		        {
		        	MongoClient mongoClient = MongoClients.create(uri);
		        	MongoDatabase database = mongoClient
		        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"));
		        	return database.getCollection(dbID);
		        });
				
				Document doc = dbUserCollection.get().find(eq("k", key)).first();
				
				if (doc == null) {
					
					Document docInsert = new Document();
					docInsert.append("k", key);
					docInsert.append("v", value);
					dbUserCollection.get().insertOne(docInsert);
				}else {
					
					dbUserCollection.get().updateOne(eq("k", key), set("v", value));
				}
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Optional<KeyValue> getValue(String userID, String key, String dbID) {
		
		Optional<User> u = getUserById(userID);
		
		if (u.isPresent() && u.get().getBbdd().contains(dbID)) {
			
			Supplier<MongoCollection<Document>> dbUserCollection = Lazy.lazily(() -> 
	        {
	        	MongoClient mongoClient = MongoClients.create(uri);
	        	MongoDatabase database = mongoClient
	        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"));
	        	return database.getCollection(dbID);
	        });
			
			Document doc = dbUserCollection.get().find(eq("k", key)).first();
			
			if (doc != null) {
				
				Object v = doc.get("v");
				KeyValue keyValue = new KeyValue();
				try {
					int k = Integer.parseInt(String.valueOf(key));
					keyValue.setK(k);
				}catch (NumberFormatException e) {
					
					try {
						float k = Float.parseFloat(String.valueOf(key));
						keyValue.setK(k);
					}catch (NumberFormatException e1) {
						String k = String.valueOf(key);
						keyValue.setK(k);
					}
				}
				
				try {
					int value = Integer.parseInt(String.valueOf(v));
					keyValue.setV(value);
				}catch (NumberFormatException e) {
					
					try {
						float value = Float.parseFloat(String.valueOf(v));
						keyValue.setV(value);
					}catch (NumberFormatException e1) {
						String value = String.valueOf(v);
						keyValue.setV(value);
					}
				}
				
				return Optional.of(keyValue);
			}
		}
		
		return Optional.empty();
	}

	@Override
	public boolean deletePair(String userID, String key, String dbID) {
		
		Optional<User> u = getUserById(userID);
		if (u.isPresent() && u.get().getBbdd().contains(dbID)) {
			
			Supplier<MongoCollection<Document>> dbUserCollection = Lazy.lazily(() -> 
	        {
	        	MongoClient mongoClient = MongoClients.create(uri);
	        	MongoDatabase database = mongoClient
	        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"));
	        	return database.getCollection(dbID);
	        });
			
			dbUserCollection.get().deleteOne(eq("k", key));
			return true;
		}
		return false;
	}

	@Override
	public Optional<ArrayList<KeyValue>> makeQuery(String userID, String pattern, String dbID, int page, int perpage) {
	
		Optional<User> u = getUserById(userID);
		if (u.isPresent() && u.get().getBbdd().contains(dbID)) {
			Supplier<MongoCollection<Document>> dbUserCollection = Lazy.lazily(() -> 
	        {
	        	MongoClient mongoClient = MongoClients.create(uri);
	        	MongoDatabase database = mongoClient
	        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"));
	        	return database.getCollection(dbID);
	        });
					
			ArrayList<Document> sampleDataList = dbUserCollection.get().find(regex("k", pattern))
		            .skip( page > 0 ? ( ( page - 1 ) * perpage ) : 0 )
		            .limit(perpage)
		            .into(new ArrayList<>());
			
			ArrayList<KeyValue> returnList = new ArrayList<KeyValue>();
			
			for (Document doc : sampleDataList) {
				
				Object k = doc.get("k");
				Object v = doc.get("v");
				KeyValue keyValue = new KeyValue();
				
				try {
					int key = Integer.parseInt(String.valueOf(k));
					keyValue.setK(key);
				}catch (NumberFormatException e) {
					
					try {
						float key = Float.parseFloat(String.valueOf(k));
						keyValue.setK(key);
					}catch (NumberFormatException e1) {
						String key = String.valueOf(k);
						keyValue.setK(key);
					}
				}
				
				try {
					int value = Integer.parseInt(String.valueOf(v));
					keyValue.setV(value);
				}catch (NumberFormatException e) {
					
					try {
						float value = Float.parseFloat(String.valueOf(v));
						keyValue.setV(value);
					}catch (NumberFormatException e1) {
						String value = String.valueOf(v);
						keyValue.setV(value);
					}
				}
				returnList.add(keyValue);
			}
			return Optional.of(returnList);
		}
		return Optional.empty();
	}

	@Override
	public Optional<ArrayList<String>> getUserDatabases(String userID) {
		
		Optional<User> user = getUserById(userID);
		
		if (user.isPresent())
			return Optional.of(user.get().getBbdd());
		
		return Optional.empty();
	}
}
