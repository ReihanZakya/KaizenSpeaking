from pydantic import BaseModel, ConfigDict
from uuid import  UUID
from typing import Optional

class UserCreate(BaseModel):
    username: str
    email: Optional[str] = None
    password: str
    full_name: str
    nickname: Optional[str] = None
    phone_number: Optional[str] = None
    role: str = "user"
    device_id: Optional[str] = None

class UserLogin(BaseModel):
    email: str
    password: str

class UserDetailResponse(BaseModel):
    id: UUID
    email: str
    username: str
    full_name: str
    nickname: str = None
    phone_number: str = None

    class Config:
        model_config = ConfigDict(
            from_attributes=True,
            json_encoders={UUID: str}
        )

class UserResponse(BaseModel):
    id: UUID
    username: str
    email: str
    full_name: str
    nickname: str = None
    phone_number: str = None

    class Config:
        model_config = ConfigDict(
            from_attributes=True,
            json_encoders={UUID: str}
        )
