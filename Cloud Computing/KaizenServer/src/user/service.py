import re
from sqlalchemy.orm import Session
from src.user import models, schemas, utils
from src.exceptions import AuthenticationError, DataNotFoundError, ConflictError
from uuid import UUID
from sqlalchemy.exc import DataError
from src.user.jwt_handler import create_access_token

def validate_email_format(email: str):
    email_regex = r'^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$'
    if not re.match(email_regex, email):
        raise ValueError("Invalid email format")


def create_user(db: Session, user: schemas.UserCreate):
    if user.device_id:
        existing_device_user = db.query(models.User).filter(models.User.device_id == user.device_id).first()
        if existing_device_user:
            return existing_device_user
    if user.email:
        try:
            validate_email_format(user.email)
        except ValueError as e:
            raise ConflictError(str(e))
        existing_user_email = db.query(models.User).filter(models.User.email == user.email).first()
        if existing_user_email:
            raise ConflictError("A user with this email already exists")

    existing_user_username = db.query(models.User).filter(models.User.username == user.username).first()
    if existing_user_username:
        raise ConflictError("A user with this username already exists")

    hashed_password = utils.hash_password(user.password)
    db_user = models.User(
        username=user.username,
        email=user.email,
        hashed_password=hashed_password,
        full_name=user.full_name,
        nickname=user.nickname,
        phone_number=user.phone_number,
        role=user.role,
        device_id=user.device_id
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


def authenticate_user(db: Session, email: str, password: str):
    try:
        validate_email_format(email)
    except ValueError as e:
        raise AuthenticationError(str(e))

    user = db.query(models.User).filter(models.User.email == email).first()
    if not user:
        raise DataNotFoundError("User with this email not found")

    if not utils.verify_password(password, user.hashed_password):
        raise AuthenticationError("Invalid credentials provided")
    token_data = {
        "sub": user.username,
        "user_id": str(user.id),
        "role": user.role
    }
    token = create_access_token(data=token_data)
    return {"access_token": token, "token_type": "Bearer", "userId": user.id}


def get_user_by_id(db: Session, user_id: str):
    try:
        user = db.query(models.User).filter(models.User.id == UUID(user_id)).first()
    except ValueError:
        raise DataNotFoundError("User ID is not valid. Please provide a valid UUID.")
    except DataError:
        raise DataNotFoundError("User not found or invalid input format.")

    if not user:
        raise DataNotFoundError("User not found")

    return user
