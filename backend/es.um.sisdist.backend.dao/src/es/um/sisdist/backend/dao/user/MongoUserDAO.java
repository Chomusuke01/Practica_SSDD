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


import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
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
    
    private Supplier<MongoCollection<Userdb>> dbUserCollection;
    
    public MongoUserDAO()
    {
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().conventions(asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://root:root@" 
        		+ Optional.ofNullable(System.getenv("MONGO_SERVER")).orElse("localhost")
                + ":27017/ssdd?authSource=admin";

        collection = Lazy.lazily(() -> 
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
        		.withCodecRegistry(pojoCodecRegistry);
        	return database.getCollection("users", User.class);
        });
        //collection.get().dropIndexes(); /// PREGUNTAR, IMPORTANTE.
        dbUserCollection = Lazy.lazily(() -> 
        {
        	MongoClient mongoClient = MongoClients.create(uri);
        	MongoDatabase database = mongoClient
        		.getDatabase(Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd"))
        		.withCodecRegistry(pojoCodecRegistry);
        	return database.getCollection("keyvaluedb", Userdb.class);
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
		
		Optional<User> user = getUserById(userID);

		
		/*if (user.isEmpty()) {
			return Optional.empty();
		}
		
		Optional<Userdb> dbuser = getUserdbById(bdID);
		if (! dbuser.isEmpty()) {
			return Optional.empty();
		}*/		
		
		collection.get().updateOne(eq("id", userID), push("bbdd", bdID));
		
		Userdb db = new Userdb(); 
		db.setId(bdID);
		db.setD(kv);
		dbUserCollection.get().insertOne(db);
		
		return Optional.of(bdID);
	}

	@Override
	public Optional<Userdb> getUserdbById(String id) {
		
		return Optional.ofNullable(dbUserCollection.get().find(eq("id", id)).first());
	}

}
