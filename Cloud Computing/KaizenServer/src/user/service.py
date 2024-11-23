from src.user.jwt_handler import create_access_token
from sqlalchemy.orm import Session
from src.user import models, schemas, utils
from src.exceptions import AuthenticationError, DataNotFoundError, ConflictError
from uuid import UUID
from sqlalchemy.exc import DataError

def create_user(db: Session, user: schemas.UserCreate):
    existing_user = db.query(models.User).filter(models.User.username == user.username).first()
    if existing_user:
        raise ConflictError("A user with this username already exists")

    hashed_password = utils.hash_password(user.password)
    db_user = models.User(
        username=user.username,
        email=user.email,
        hashed_password=hashed_password,
        full_name=user.full_name,
        nickname=user.nickname,
        phone_number=user.phone_number,
        role=user.role
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def authenticate_user(db: Session, email: str, password: str):
    user = db.query(models.User).filter(models.User.email == email).first()
    if not user:
        raise DataNotFoundError("User not found")

    if not utils.verify_password(password, user.hashed_password):
        raise AuthenticationError("Invalid credentials provided")

    token_data = {
        "sub": user.username,
        "user_id": str(user.id),
        "role": user.role
    }
    token = create_access_token(data=token_data)
    return {"access_token": token, "token_type": "Bearer"}

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

