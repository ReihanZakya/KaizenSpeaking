# src/user/router.py
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from src.middleware import JWTBearer
from src.user import schemas, service
from src.dependencies import get_db
from src.handler import success_post, success_get
from src.exceptions import ConflictError, AuthenticationError, DataNotFoundError
from src.user.service import validate_unique_email_service

router = APIRouter()

@router.post("/validate-unique-email")
def validate_unique_email(email: str, db: Session = Depends(get_db)):
    return validate_unique_email_service(email, db)


@router.post("/register", response_model=schemas.UserResponse)
def register_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    try:
        new_user = service.create_user(db, user)
        user_response = schemas.UserResponse.model_validate(new_user, from_attributes=True)
        user_response_dict = user_response.model_dump()
        user_response_dict["id"] = str(user_response.id)

        return success_post(data=user_response_dict, message="User created successfully")
    except ConflictError as e:
        raise e

@router.post("/login")
def login_user(user: schemas.UserLogin, db: Session = Depends(get_db)):
    try:
        token_data = service.authenticate_user(db, user.email, user.password)
        return success_get(data=token_data, message="Login successful")
    except (AuthenticationError, DataNotFoundError) as e:
        raise e

@router.get("/{user_id}", response_model=schemas.UserDetailResponse, dependencies=[Depends(JWTBearer(required_role="user"))])
def get_user(user_id: str, db: Session = Depends(get_db)):
    try:
        user = service.get_user_by_id(db, user_id)
        user_response = schemas.UserResponse.model_validate(user, from_attributes=True)
        user_response_dict = user_response.model_dump()
        user_response_dict["id"] = str(user_response.id)
        return success_get(data=user_response_dict)
    except DataNotFoundError as e:
        raise e