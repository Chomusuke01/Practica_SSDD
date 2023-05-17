
Practica_SSDD

Este es el repositorio usado para la práctica de la asignatura Sistemas Distribuidos de 4º en la Facultad de Informática de la Universidad de Murcia.

Requisitos previos

Para lanzar este proyecto será necesario tener instalado tanto Docker como Docker-Compose y permisos para ejecutarlos

Instalación

Para instalar el proyecto basta con clonarlo, no es necesario ninguna configuración adicional

Estructura de Directorios

backend-grpc: Contiene los archivos relacionados con el backend gRPC.
backend-rest-extern: Contiene los archivos relacionados con el backend REST externo.
backend-rest: Contiene los archivos relacionados con el backend REST.
backend: Contiene los archivos relacionados con el backend en general.
db-mongo: Contiene los archivos relacionados con la base de datos MongoDB.
frontend: Contiene los archivos relacionados con el frontend.
.project: Archivo de configuración del proyecto específico de la plataforma o IDE.
ClientePrueba.py: Archivo de ejemplo para realizar pruebas en el cliente.
Makefile: Archivo de configuración para automatizar la construcción y ejecución.
docker-compose-devel-mongo.yml: Archivo de configuración de Docker Compose para entorno de desarrollo con MongoDB.
docker-compose-mongo.yml: Archivo de configuración de Docker Compose para entorno con MongoDB.
mongo.env: Archivo de configuración de variables de entorno para la base de datos MongoDB.
restart.sh: Script para reiniciar el proyecto.
start.sh: Script para iniciar el proyecto.
stop.sh: Script para detener el proyecto.

Uso

Para lanzar la aplicación es necesario ejecutar el script start.sh, el cual ejecutará los comandos adecuados para lanzar el escenario. Al terminar de usar la aplicación, lanzando el script stop.sh el escenario se apagará dejando de consumir sus recursos
