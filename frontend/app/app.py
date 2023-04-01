from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
import requests, json

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm, NewDatabaseForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'
lista = ["a","b"]

backendURL = "backend-rest:8080"
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
                user = User(responseData["id"], responseData["name"], form.email.data.encode('utf-8'), ## Preguntar campos user
                            form.password.data.encode('utf-8'))
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
                "password": form.password.data,
                "token": "testToken"
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
    if request.method == "POST":
        
        dbData = request.form['databaseContent']
        dbName = request.form['newDatabaseName']

        data = {
            "dbname": dbName,
            "d": json.loads(dbData)
        }

        response = requests.post("http://{}/Service/u/{}/db".format(backendURL, current_user.id), json=data)

        if response.status_code == 201:
            return redirect(url_for('bbdd'))

    return render_template('inputDB.html', form=form)

@app.route('/showDatabase', methods=['POST', 'GET'])
@login_required
def showDatabase():

    if request.method == "POST":

        response = requests.get("http://{}/Service/u/{}/db/{}".format(backendURL, current_user.id, request.form['DatabaseName']))

        if response.status_code == 200:

            content = response.json()

            return render_template('showDatabase.html', dbName=content['dbname'], databaseContent=content['d'])
    
    return render_template('showDatabase.html', dbName=None)


@app.route('/addKey', methods=['POST', 'GET'])
@login_required
def addKey():

    if request.method == "POST":

        response = requests.put("http://{}/Service/u/{}/db/{}/d/{}?v={}".format(backendURL, 
        current_user.id, request.form['dbName'], request.form['key'], request.form['value']))

        if response.status_code == 200:

            return render_template('addkeyValue.html', result="Clave añadida con éxito")
        
        return render_template('addkeyValue.html', result="Fallo al añadir la clave")
    
    return render_template('addkeyValue.html', result=None)

@app.route('/getValue', methods=['POST', 'GET'])
@login_required
def getValue():

    if request.method == "POST":

        response = requests.get("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL, 
        current_user.id, request.form['dbName'], request.form['key']))

        if response.status_code == 200:

            responseData = response.json()
            return render_template('showKeyValue.html', result=responseData)

        return render_template('showKeyValue.html', result="Fallo al encontrar la clave")

    return render_template('showKeyValue.html', result=None)


@app.route('/deleteKey', methods=['POST', 'GET'])
@login_required
def deleteKey():

    if request.method == "POST":
        
        response = requests.delete("http://{}/Service/u/{}/db/{}/d/{}".format(backendURL, 
        current_user.id, request.form['dbName'], request.form['key']))

        if response.status_code == 200:
            
            return render_template('deleteKey.html', result="Clave eliminada con éxito")

        return render_template('deleteKey.html', result="Fallo al borrar la clave")

    return render_template('deleteKey.html', result=None)


@app.route('/makeQuery', methods=['POST', 'GET'])
@login_required
def makeQuery():
    
    if request.method == "POST":

        response = requests.get("http://{}/Service/u/{}/db/{}/q?pattern={}&page={}&perpage={}".format(backendURL,
        current_user.id, request.form['db'], request.form['pattern'], request.form['page'], request.form['perpage']))

        if response.status_code == 200:

            return render_template('query.html', result=json.dumps(response.json()['d'], indent=2))

        return render_template('query.html', result="Error al procesar la petición")

    return render_template('query.html', result=None)


@app.route('/postbd', methods=['POST'])
@login_required 
def postdb():
    #añadir la bbdd en el backend
    database = request.form['newDatabase']
    
    lista.append(database)
    return redirect(url_for('bbdd'))

@app.route('/deletedb', methods=['POST'])
@login_required
def deletedb():

    database = request.form['deleteDatabase']
    lista.remove(database)
    return redirect(url_for('bbdd'))


@app.route('/profile')
@login_required
def profile():
    return render_template('profile.html')

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
