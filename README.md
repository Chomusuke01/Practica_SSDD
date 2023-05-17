# Practica_SSDD

Este repositorio se utiliza para la práctica de la asignatura de Sistemas Distribuidos de 4º en la Facultad de Informática de la Universidad de Murcia.

## Requisitos previos

Antes de lanzar este proyecto, asegúrate de tener instalados Docker y Docker-Compose, así como los permisos necesarios para ejecutarlos.

## Instalación

No se requiere ninguna configuración adicional. Simplemente clona el repositorio y estarás listo para comenzar.

## Estructura de directorios

El repositorio tiene la siguiente estructura de directorios:

- `backend-grpc`: Contiene los archivos relacionados con el backend gRPC.
- `backend-rest-extern`: Contiene los archivos relacionados con el backend REST externo.
- `backend-rest`: Contiene los archivos relacionados con el backend REST.
- `backend`: Contiene los archivos relacionados con el backend en general.
- `db-mongo`: Contiene los archivos relacionados con la base de datos MongoDB.
- `frontend`: Contiene los archivos relacionados con el frontend.
- `.project`: Archivo de configuración específico del proyecto para la plataforma o IDE utilizado.
- `ClientePrueba.py`: Archivo de ejemplo para realizar pruebas en el cliente.
- `Makefile`: Archivo de configuración para automatizar la construcción y ejecución.
- `docker-compose-devel-mongo.yml`: Archivo de configuración de Docker Compose para el entorno de desarrollo con MongoDB.
- `docker-compose-mongo.yml`: Archivo de configuración de Docker Compose para el entorno con MongoDB.
- `mongo.env`: Archivo de configuración de variables de entorno para la base de datos MongoDB.
- `restart.sh`: Script para reiniciar el proyecto.
- `start.sh`: Script para iniciar el proyecto.
- `stop.sh`: Script para detener el proyecto.

## Uso

Para ejecutar la aplicación, simplemente ejecuta el script `start.sh`, el cual se encargará de lanzar el escenario necesario. Una vez que hayas terminado de usar la aplicación, puedes detenerla ejecutando el script `stop.sh`, lo cual liberará los recursos.
