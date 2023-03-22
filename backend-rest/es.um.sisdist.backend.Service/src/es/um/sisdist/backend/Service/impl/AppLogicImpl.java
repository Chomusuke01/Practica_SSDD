/**
 *
 */
package es.um.sisdist.backend.Service.impl;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.models.BD_DTO;
import es.um.sisdist.models.BD_DTOUtils;
import es.um.sisdist.models.KeyValueDTO;
import es.um.sisdist.models.Query_DTO;
import es.um.sisdist.models.Query_DTOUtils;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.KeyValue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.Userdb;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl
{
    IDAOFactory daoFactory;
    IUserDAO dao;

    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    //private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl()
    {
        daoFactory = new DAOFactoryImpl();
        Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));
        
        if (backend.isPresent() && backend.get().equals("mongo"))
            dao = daoFactory.createMongoUserDAO();
        else
            dao = daoFactory.createSQLUserDAO();

        var grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
        var grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

        channel = ManagedChannelBuilder
                .forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid needing certificates.
                .usePlaintext().build();
        blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
        //asyncStub = GrpcServiceGrpc.newStub(channel);
    }

    public static AppLogicImpl getInstance()
    {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId)
    {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId)
    {
        return dao.getUserById(userId);
    }

    public Optional<User> newUser(UserDTO uo){
    	
    	return dao.newUser(uo.getName(), uo.getId(), uo.getEmail(), uo.getPassword());
    }
    
    public Optional<String> newBD(BD_DTO bdDto, String userID) {
    	return dao.newBBDD(userID, bdDto.getDbname(), BD_DTOUtils.fromDTO(bdDto));
    }
    
    public Optional<BD_DTO> getDatabases(String userID, String bdID){
    	Optional<Userdb> userdb = dao.getDatabases(userID, bdID);
    	BD_DTO bddto = BD_DTOUtils.toBD_DTO(userdb.get());
    	return Optional.of(bddto);
    }
    
    public boolean addKeyValue(String userID, String key, String value, String dbID) {
    	
    	return dao.addKeyValue(userID, key, value, dbID);
    }
    
    public Optional<KeyValueDTO> getValue(String userID, String key, String dbID){
    	
    	Optional<KeyValue> kv = dao.getValue(userID, key, dbID);
    	
    	if (kv.isPresent()) {
    		return Optional.of(BD_DTOUtils.keyValueToDTO(kv.get()));
    	}
    	return Optional.empty();
    	
    }
    
    public boolean deletePair(String userID, String key, String dbID) {
    	
    	return dao.deletePair(userID, key, dbID);
    }
    
    public boolean ping(int v)
    {
    	logger.info("Issuing ping, value: " + v);
    	
        // Test de grpc, puede hacerse con la BD
    	var msg = PingRequest.newBuilder().setV(v).build();
        var response = blockingStub.ping(msg);
        
        return response.getV() == v;
    }

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String pass)
    {
        Optional<User> u = dao.getUserByEmail(email);

        if (u.isPresent())
        {
            String hashed_pass = UserUtils.md5pass(pass);
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash())) {
            	dao.updateVisits(u.get());
            	return u;
            }
        }

        return Optional.empty();
    }
    public Optional<Query_DTO> makeQuery(String userID, String pattern, String dbID, int page, int perpage){
    	Optional<ArrayList<KeyValue>> values = dao.makeQuery(userID, pattern, dbID, page, perpage);
    	if (values.isPresent()) return Optional.of(Query_DTOUtils.toDTO(dbID, pattern, page, perpage, values.get()));
    	return Optional.empty();
    }
}
