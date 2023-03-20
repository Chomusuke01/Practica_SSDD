/**
 *
 */
package es.um.sisdist.backend.dao.user;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.push;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.pojo.Conventions;
import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

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
			
			try {
				int k = Integer.parseInt(String.valueOf(keyValue.getK()));
				doc.append("k", k);
			}catch (NumberFormatException e) {
				
				try {
					float k = Float.parseFloat(String.valueOf(keyValue.getK()));
					doc.append("k", k);
				}catch (NumberFormatException e1) {
					
					String k = String.valueOf(keyValue.getK());
					doc.append("k", k);
				}
			}
			
			try {
				int v = Integer.parseInt(String.valueOf(keyValue.getV()));
				doc.append("v", v);
			}catch (NumberFormatException e) {
				
				try {
					float v = Float.parseFloat(String.valueOf(keyValue.getV()));
					doc.append("v", v);
				}catch (NumberFormatException e1) {
					
					String v = String.valueOf(keyValue.getV());
					doc.append("v", v);
				}
			}
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
			kv.add(new KeyValue(k,v));
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

}
