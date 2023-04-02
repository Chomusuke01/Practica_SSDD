/**
 *
 */
package es.um.sisdist.backend.Service.impl;

import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.MapReduceRequest;
import es.um.sisdist.backend.grpc.MapReduceResponse;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

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
    //private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

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
        //blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
        asyncStub = GrpcServiceGrpc.newStub(channel);
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

    public boolean mapReduce(String userID, String out_db, String map, String reduce, String in_db) {
    	
    	if (!dao.hasUserDBAccess(userID, in_db)) 
    		return false;
    	
    	dao.addMrQueue(out_db, userID);
    	
    	logger.info("Se llama al procedimiento desde REST");
    	
    	var mapReduceRequest = MapReduceRequest.newBuilder().setInDb(in_db).setMap(map).setReduce(reduce).setOutDb(out_db).setUserID(userID).build();
    	 
    	asyncStub.mapReduce(mapReduceRequest, new StreamObserver<MapReduceResponse>() {
			
			@Override
			public void onNext(MapReduceResponse value) {
				logger.info("Se ha recibido la respuesta");
				dao.updateMrQueue(value.getMrID(), 1);
			}
			
			@Override
			public void onError(Throwable t) {
				logger.warning("Fallo al llamar al método remoto");
				
			}
			
			@Override
			public void onCompleted() {
			
			}
		});
    	
    	return true;
    }
    
    public int getMrStatus(String mrID, String userID) {
    	
    	return dao.getMrStatus(mrID, userID);
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
            if (0 == hashed_pass.compareTo(u.get().getPassword_hash()))
                return u;
        }

        return Optional.empty();
    }
}
