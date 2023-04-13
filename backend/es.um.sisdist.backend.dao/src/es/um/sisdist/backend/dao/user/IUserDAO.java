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
    
    public Optional<ArrayList<KeyValue>> makeQuery(String userID, String pattern, String dbID, int page, int perpage);
    
    public Optional<ArrayList<String>> getUserDatabases(String userID);
    
    public Optional<Userdb> getUserDBByIDRAW(String userID, String dbID);
    
    public void addMrQueue(String dbID, String userID);
    
    public void updateMrQueue(String dbID, int status);
    
    public int getMrStatus(String dbID, String userID);
    
    public boolean hasUserDBAccess(String userID, String bdID);
    
    public void updateMrRequest (String userID);
}
