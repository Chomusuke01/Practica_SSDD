from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'
lista = ["a","b"]
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
            if form.email.data != 'admin@um.es' or form.password.data != 'admin':
                error = 'Invalid Credentials. Please try again.'
            else:
                user = User(1, 'admin', form.email.data.encode('utf-8'),
                            form.password.data.encode('utf-8'))
                users.append(user)
                login_user(user, remember=form.remember_me.data)
                return redirect(url_for('index'))

        return render_template('login.html', form=form,  error=error)


@app.route('/register', methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('index'))
    else:
        error = None
        form = RegisterForm(None if request.method != 'POST' else request.form)
        if request.method == "POST" and form.validate():
            print (form.username.data)
            print (form.password.data)
            print (form.email.data)
            
            return redirect(url_for('login'))
        return render_template('register.html', form=form,  error=error)

@app.route('/bbdd')
@login_required
def bbdd():
    
    return render_template('bbdd.html', bdList=lista, len=len(lista))

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
        if user.id == int(user_id):
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
