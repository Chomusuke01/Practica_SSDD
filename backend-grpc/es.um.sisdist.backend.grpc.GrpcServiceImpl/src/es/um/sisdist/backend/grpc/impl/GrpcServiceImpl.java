package es.um.sisdist.backend.grpc.impl;

import java.util.Map;
import java.util.logging.Logger;
import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.KeyValue;
import es.um.sisdist.backend.grpc.KeyValueDB;
import es.um.sisdist.backend.grpc.KeyValueDB.Builder;
import es.um.sisdist.backend.grpc.MapReduceRequest;
import es.um.sisdist.backend.grpc.impl.jscheme.JSchemeProvider;
import es.um.sisdist.backend.grpc.impl.jscheme.MapReduceApply;
import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
	private Logger logger;
	
    public GrpcServiceImpl(Logger logger) 
    {
		super();
		this.logger = logger;
	}

    @Override
	public void mapReduce(MapReduceRequest request, StreamObserver<KeyValueDB> responseObserver)
    {
		MapReduceApply mapReduceApply = new MapReduceApply(JSchemeProvider.js(), request.getMap(), request.getReduce());
		for (KeyValue kv : request.getKvList()) {
			Object k;
			Object v;
			try {
				k = Integer.parseInt(kv.getK());				
			}catch (NumberFormatException e) {				
				try {
					k = Float.parseFloat(kv.getK());
				}catch (NumberFormatException e1) {
					k = kv.getK();
				}
			}
			try {
				v = Integer.parseInt(kv.getV());				
			}catch (NumberFormatException e) {				
				try {
					v = Float.parseFloat(kv.getV());
				}catch (NumberFormatException e1) {
					v = kv.getV();
				}
			}
			mapReduceApply.apply(k, v);
		}
		Map<Object,Object> map = mapReduceApply.map_reduce();
		Builder builder = KeyValueDB.newBuilder();
		for (Object k : map.keySet()) {
			builder.addKv(KeyValue.newBuilder().setK(String.valueOf(k)).setV(String.valueOf(map.get(k))).build());			
		}
		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}
}