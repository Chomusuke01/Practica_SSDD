from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField)
from wtforms.validators import InputRequired, Length, Email, DataRequired, EqualTo

class LoginForm(FlaskForm):
    email = StringField('email', validators=[Email()])
    password = PasswordField('password', validators=[DataRequired()])
    remember_me = BooleanField('remember_me')

class RegisterForm(FlaskForm):
    email = StringField('email', validators=[Email()])
    username = StringField('username', validators=[InputRequired()])
    password = PasswordField('password', validators=[DataRequired()])
    name = StringField('name', validators=[DataRequired()])
    confirm_password = PasswordField('confirm_password', validators=[InputRequired(), EqualTo('password', message='Passwords must match')])
    
class NewDatabaseForm(FlaskForm):
    databaseName = StringField('newDatabaseName', validators=[InputRequired()])
    databaseContent = StringField('databaseContent', validators=[InputRequired()])