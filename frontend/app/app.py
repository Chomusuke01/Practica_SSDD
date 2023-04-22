from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
from hashlib import md5
import requests, json
import urllib.parse as encoder
from datetime import datetime

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm, NewDatabaseForm, MRForm, ShowDatabaseForm, MRStatusForm, NewKeyForm, GetValueForm, DeleteKeyForm, QueryForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

backendURL = "backend-rest:8080"
backendURLExt = "backend-rest-extern:8180"
@app.route('/static/<path:path>')
def serve_static(path):
    return send_from_directory('static', path)

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = LoginForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            
            data ={
                "email": form.email.data,
                "password": form.password.data
            }
            response = requests.post("http://" + backendURL + "/Service/checkLogin", json=data)
            if response.status_code == 200:

                responseData = response.json() 
                user = User(responseData["id"], responseData["name"], responseData["email"], ## Preguntar campos user
                            form.password.data.encode('utf-8'), responseData["token"])
                users.append(user)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))
            else:
                error = 'Invalid credentials. Please try again'

        return render_template('login.html', form=form,  error=error)


@app.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = RegisterForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            
            data ={
                "email": form.email.data,
                "id": form.username.data,
                "name": form.name.data,
                "password": form.password.data
            }

            response = requests.post("http://" + backendURL + "/Service/u", json=data) 

            if response.status_code == 201:
                return redirect(url_for('login'))
            
            error = "User already registered"
            
        return render_template('register.html', form=form,  error=error)

@app.route('/bbdd')
@login_required
def bbdd():
    response = requests.get("http://{}/Service/u/{}/db".format(backendURL,current_user.id))
    
    if response.status_code == 200:
        listaBBDD = eval(response.content.decode('utf-8'))
        return render_template('bbdd.html', bdList=listaBBDD, len=len(listaBBDD))
    

@app.route('/newDatabase', methods=['POST', 'GET'])
@login_required
def newDatabase():

    form = NewDatabaseForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():
        
        dbData = request.form['databaseContent']
        dbName = request.form['newDatabaseName']

        dbContent = None

        try: 
            dbContent = json.loads(dbData)
        except:
            return render_template('inputDB.html', form=form, error="El contenido debe estar en formato JSON")

        data = {
            "dbname": dbName,
            "d": dbContent
        }

        response = requests.post("http://{}/Service/u/{}/db".format(backendURL, current_user.id), json=data)

        if response.status_code == 201:
            return redirect(url_for('bbdd'))

    return render_template('inputDB.html', form=form)

@app.route('/showDatabase', methods=['POST', 'GET'])
@login_required
def showDatabase():

    form = ShowDatabaseForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():

        response = requests.get("http://{}/Service/u/{}/db/{}".format(backendURL, current_user.id, request.form['DatabaseName']))

        if response.status_code == 200:

            content = response.json()
            #result=json.dumps(response.json()['d'], indent=2)
            return render_template('showDatabase.html', dbName=content['dbname'], databaseContent=json.dumps(content['d'], indent=2))
    
    return render_template('showDatabase.html', dbName=None, form=form)

@app.route('/mrRequest', methods=['POST', 'GET'])
@login_required
def mrRequest():

    form = MRForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():
        data = {
            "map": form.map.data,
            "reduce": form.reduce.data,
            "out_db": form.out_db.data
        }
        date = datetime.now().isoformat()
        url = "http://{}/Service-extern/u/{}/db/{}/mr".format(backendURLExt,current_user.id, form.in_db.data)
        authToken = md5((url + date + current_user.token).encode()).hexdigest()
        headers = {
            "Date": date,
            "User": current_user.id,
            "Auth-Token": authToken
        }

        response = requests.post(url, json=data, headers=headers)

        if response.status_code == 202:
            
            return render_template('mapreduce.html', result="Petición lanzanda con exito en {}".format(response.headers["Location"]))

        if response.status_code == 401:

            return render_template('mapreduce.html', result="Fallo de autenticacion")
        
    return render_template('mapreduce.html', result=None, form=form)


@app.route('/mrStatus', methods=['POST', 'GET'])
@login_required
def mrStatus():

    form = MRStatusForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():

        date = datetime.now().isoformat()
        url = "http://{}/Service-extern/u/{}/db/{}/mr/{}".format(backendURLExt,current_user.id, request.form['dbName'], request.form['mrID'])
        authToken = md5((url + date + current_user.token).encode()).hexdigest()

        headers = {
            "Date": date,
            "User": current_user.id,
            "Auth-Token": authToken
        }

        response = requests.get(url, headers=headers)

        if response.status_code == 200:

            responseData = response.json()

            if responseData["status"] == 1:

                return render_template('mrStatus.html', result="La petición se ha completado")
            
            return render_template('mrStatus.html', result="La petición se está procesando")
        
        if response.status_code == 401:

            return render_template('mrStatus.html', result="Fallo de autenticación")
    
    return render_template('mrStatus.html', result=None, form=form)

@app.route('/addKey', methods=['POST', 'GET'])
@login_required
def addKey():

    form = NewKeyForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():

        response = requests.put("http://{}/Service/u/{}/db/{}/d/{}?v={}".format(backendURL, 
        current_user.id, request.form['dbName'], request.form['key'], request.form['value']))

        if response.status_code == 200:

            return render_template('addkeyValue.html', result="Clave añadida con éxito")
        
        return render_template('addkeyValue.html', result="Fallo al añadir la clave")
    
    return render_template('addkeyValue.html', result=None, form=form)

@app.route('/getValue', methods=['POST', 'GET'])
@login_required
def getValue():

    form = GetValueForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():

        response = requests.get("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL, 
        current_user.id, request.form['dbName'], request.form['key']))

        if response.status_code == 200:

            responseData = response.json()
            return render_template('showKeyValue.html', result=responseData)

        return render_template('showKeyValue.html', result="Fallo al encontrar la clave")

    return render_template('showKeyValue.html', result=None, form=form)


@app.route('/deleteKey', methods=['POST', 'GET'])
@login_required
def deleteKey():

    form = DeleteKeyForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():
        
        response = requests.delete("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL, 
        current_user.id, request.form['dbName'], request.form['key']))

        if response.status_code == 200:
            
            return render_template('deleteKey.html', result="Clave eliminada con éxito")

        return render_template('deleteKey.html', result="Fallo al borrar la clave")

    return render_template('deleteKey.html', result=None, form=form)


@app.route('/makeQuery', methods=['POST', 'GET'])
@login_required
def makeQuery():
    
    form = QueryForm(None if request.method != 'POST' else request.form)

    if request.method == "POST" and form.validate():

        response = requests.get("http://{}/Service/u/{}/db/{}/q?pattern={}&page={}&perpage={}".format(backendURL,
        current_user.id, request.form['db'], encoder.quote(request.form['pattern']), request.form['page'], request.form['perpage']))

        if response.status_code == 200:

            return render_template('query.html', result=json.dumps(response.json()['d'], indent=2))

        return render_template('query.html', result="Error al procesar la petición")

    return render_template('query.html', result=None, form=form)

@app.route('/profile')
@login_required
def profile():

    response = requests.get("http://{}/Service/u/{}".format(backendURL, current_user.email))
    respondeData = response.json()

    return render_template('profile.html', visitas=respondeData["visits"], nBD=len(respondeData["bbdd"]), nMR=respondeData["mrRequest"])

@app.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == user_id:
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
