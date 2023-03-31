backend-rest-extern:
	(cd es.um.sisdist.backend.Service-extern \
		&& mvn -DskipTests clean package)

containers:
	(cd es.um.sisdist.backend.Service-extern \
		&& docker build -t dsevilla/ssdd-backend-rest-extern:1.0 .)

clean:
	docker rmi -f dsevilla/ssdd-backend-rest-extern:1.0
