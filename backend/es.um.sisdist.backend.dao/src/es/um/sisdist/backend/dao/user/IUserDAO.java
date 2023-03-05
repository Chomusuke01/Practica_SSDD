package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);
    
    public Optional<User> newUser(String name, String id, String email, String password, String token);
    
    public void updateVisits(User u);
}
