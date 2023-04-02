package es.um.sisdist.backend.grpc.impl;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.models.KeyValue;
import es.um.sisdist.backend.dao.models.Userdb;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.MapReduceRequest;
import es.um.sisdist.backend.grpc.MapReduceResponse;
import es.um.sisdist.backend.grpc.impl.jscheme.JSchemeProvider;
import es.um.sisdist.backend.grpc.impl.jscheme.MapReduceApply;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
	private Logger logger;
	IUserDAO dao;
	IDAOFactory daoFactory;
	
    public GrpcServiceImpl(Logger logger, IUserDAO dao) 
    {
		super();
		this.logger = logger;
		this.dao = dao;
	}

    @Override
	public void mapReduce(MapReduceRequest request, StreamObserver<MapReduceResponse> responseObserver)
    {
    	logger.info("Se ha recibido una nueva petición map-reduce");

		MapReduceApply mapReduceApply = new MapReduceApply(JSchemeProvider.js(), request.getMap(), request.getReduce());
		
		Optional<Userdb> userdb = dao.getUserdbById(request.getInDb());
		
		for (KeyValue kv : userdb.get().getD()) {
			mapReduceApply.apply(kv.getK(), kv.getV());
		}
		
		Map<Object,Object> result = mapReduceApply.map_reduce();
		
		ArrayList<KeyValue> db_out = new ArrayList<>();
		
		for (Object k: result.keySet()) {
			
			db_out.add(new KeyValue(k, result.get(k)));
		}

		dao.newBBDD(request.getUserID(), request.getOutDb(), db_out);
		logger.info("Se ha completado la peticion map reduce. Se envia respuesta");
		responseObserver.onNext(MapReduceResponse.newBuilder().setMrID(request.getOutDb()).build());
		responseObserver.onCompleted();
	}
}