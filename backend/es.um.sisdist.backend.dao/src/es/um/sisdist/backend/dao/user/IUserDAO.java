package es.um.sisdist.backend.dao.user;

import java.util.ArrayList;
import java.util.Optional;

import es.um.sisdist.backend.dao.models.KeyValue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.Userdb;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);
    
    public Optional<Userdb> getUserdbById(String id);

    public Optional<User> getUserByEmail(String id);
    
    public Optional<User> newUser(String name, String id, String email, String password);
    
    public void updateVisits(User u);
    
    public Optional<String> newBBDD(String userID, String bdID, ArrayList<KeyValue> kv);
    
    public Optional <Userdb>getDatabases  (String userID, String bdID);
    
    public boolean addKeyValue(String userID, String key, String value, String dbID);
    
    public Optional<KeyValue> getValue(String userID, String key, String dbID);
    
    public boolean deletePair(String userID, String key, String dbID);
}
