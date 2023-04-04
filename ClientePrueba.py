import requests
from datetime import datetime
from hashlib import md5
from time import sleep
import json

class User:

    def __init__(self, name, id, password, email) -> None:
        
        self.name = name
        self.id = id
        self.password = password
        self.email = email

map = "(import \"java.lang.String\")(define (ssdd_map k v) (display k) (display \": \") (display v) (display \"\\n\")(for-each (lambda (w) (emit (list w 1)))   (vector->list (.split v \" \"))))"
reduce = "(define (ssdd_reduce k l)(apply + l))"

backendURL = "localhost:8080"
backendURLExt = "localhost:8180"
dataBaseName = "testDB"
outDBName = "salidaTestMR"
dataBase = {

        "dbname": dataBaseName,
        "d":[{"k": 1, "v": "abc def ghi"}, 
            {"k": 2, "v": "abc def thi"},
            {"k": 3, "v": "abc def xhi"},
            {"k": 4, "v": "abc def rhi"},
            {"k": 5, "v": "abc def jhi"}]
    }

key = 9.8
value = "valor"

user = User("Test","testUser","test123","test@test.es")

def registrarUsusario():
    
    data = {

    "email": user.email,
    "id": user.id,
    "name": user.name,
    "password": user.password

    }

    response = requests.post("http://{}/Service/u".format(backendURL), json=data) 

    return response.status_code == 201

def registrarUsuarioExistente():

    data = {

    "email": user.email,
    "id": "testUser2",
    "name": "Test2",
    "password": "test123"

    }

    response = requests.post("http://{}/Service/u".format(backendURL), json=data)

    return response.status_code == 403


def loginCorrecto():

    data = {
        "email": user.email,
        "password": user.password
    }

    response = requests.post("http://{}/Service/checkLogin".format(backendURL), json=data)
    
    user.token = response.json()['token']
    
    return response.status_code == 200

def loginIncorrecto():

    data = {
        "email": user.email,
        "password": "test124"
    }

    response = requests.post("http://{}/Service/checkLogin".format(backendURL), json=data)

    return response.status_code == 403


def crearBBDD():

    response = requests.post("http://{}/Service/u/{}/db".format(backendURL, user.id), json=dataBase)

    return response.status_code == 201

def obtenerBBDD():

    response = requests.get("http://{}/Service/u/{}/db/{}".format(backendURL, user.id, dataBaseName))

    return response.json() == dataBase

def insertarClave():

    requests.put("http://{}/Service/u/{}/db/{}/d/{}?v={}".format(backendURL,user.id, dataBaseName, key, value))

    response = requests.get("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL,user.id, dataBaseName, key))

    return response.status_code == 200

def eliminarClave():

    requests.delete("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL,user.id, dataBaseName, key))

    response = requests.get("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL,user.id, dataBaseName, key))

    return response.status_code == 404

def lanzarMapReduce():
    
    date = datetime.now().isoformat()
    url = "http://{}/Service-extern/u/{}/db/{}/mr".format(backendURLExt, user.id, dataBaseName)
    authToken = md5((url + date + user.token).encode()).hexdigest()
    
    headers = {
        "User": user.id,
        "Date": date,
        "Auth-Token": authToken
    }

    data = {
        "map": map,
        "reduce": reduce,
        "out_db": outDBName
    }

    response = requests.post(url, json=data, headers=headers)

    return response.status_code == 202

def comprobarEstadoMR():

    response = requests.get("http://{}/Service-extern/u/{}/db/{}/mr/{}".format(backendURLExt, user.id, dataBaseName, outDBName))

    return response.status_code == 200

def obtenerResultadoMapReduce ():

    response = requests.get("http://{}/Service-extern/u/{}/db/{}/mr/{}".format(backendURLExt, user.id, dataBaseName, outDBName))

    while response.json()["status"] == 0:
        print("Procedo a esperar")
        sleep(3)
        response = requests.get("http://{}/Service-extern/u/{}/db/{}/mr/{}".format(backendURLExt, user.id, dataBaseName, outDBName))

    print ("Resultado Map-Reduce")

    response = requests.get("http://{}/Service/u/{}/db/{}".format(backendURL, user.id, outDBName))

    print(json.dumps(response.json(), indent=2))


if __name__ == '__main__':
    
    print("Registrar usuario: " + "Éxito" if registrarUsusario() else "Fallo")
    print("Registrar usuario existente: " + "Éxito" if registrarUsuarioExistente() else "Fallo")
    print("Login correcto: " + "Éxito" if loginCorrecto() else "Fallo")
    print("Login incorrecto: " + "Éxito" if loginIncorrecto() else "Fallo")
    print("Crear base de datos: " + "Éxito" if crearBBDD() else "Fallo")
    print("Obtener base de datos: " + "Éxito" if obtenerBBDD() else "Fallo")
    print("Insertar clave: " + "Éxito" if insertarClave() else "Fallo")
    print("Eliminar clave: " + "Éxito" if eliminarClave() else "Fallo")
    print("Lanzar MR: " + "Éxito" if lanzarMapReduce() else "Fallo")
    print("Comprobar estado MR: " + "Éxito" if comprobarEstadoMR() else "Fallo")
    obtenerResultadoMapReduce()