from flask_wtf import FlaskForm
from wtforms import (StringField, PasswordField, BooleanField)
from wtforms.validators import InputRequired, Email, DataRequired, EqualTo

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
    newDatabaseName = StringField('newDatabaseName', validators=[DataRequired()])
    databaseContent = StringField('databaseContent', validators=[DataRequired()])

class MRForm(FlaskForm):
    map = StringField('map', validators=[DataRequired()])
    reduce = StringField('reduce', validators=[DataRequired()])
    in_db = StringField('in_db', validators=[DataRequired()])
    out_db = StringField('out_db', validators=[DataRequired()])

class ShowDatabaseForm(FlaskForm):
    DatabaseName = StringField('DatabaseName', validators=[DataRequired()])

class MRStatusForm(FlaskForm):
    dbName = StringField('dbName',validators=[DataRequired()])
    mrID = StringField('mrID',validators=[DataRequired()])

class NewKeyForm(FlaskForm):
    dbName = StringField('dbName', validators=[DataRequired()])
    key = StringField('key', validators=[DataRequired()])
    value =StringField('value', validators=[DataRequired()])

class GetValueForm(FlaskForm):
    dbName = StringField('dbName', validators=[DataRequired()])
    key = StringField('key', validators=[DataRequired()])

class DeleteKeyForm(FlaskForm):
    dbName = StringField('dbName', validators=[DataRequired()])
    key = StringField('key', validators=[DataRequired()])

class QueryForm(FlaskForm):
    db = StringField('db', validators=[DataRequired()])
    pattern = StringField('pattern', validators=[DataRequired()])
    page = StringField('page', validators=[DataRequired()])
    perpage = StringField('perpage', validators=[DataRequired()])