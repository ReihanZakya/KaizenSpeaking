from sqlalchemy import Column, String, CheckConstraint
from sqlalchemy.dialects.postgresql import UUID as PGUUID
from src.database import Base
import uuid

class User(Base):
    __tablename__ = "users"

    id = Column(PGUUID(as_uuid=True), primary_key=True, default=uuid.uuid4, unique=True, index=True)
    email = Column(String, unique=True, nullable=True)
    username = Column(String, unique=True, index=True, nullable=False)
    device_id = Column(String, unique=True, nullable=True, index=True)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String, nullable=False)
    nickname = Column(String, nullable=True)
    phone_number = Column(String, nullable=True)
    role = Column(String, nullable=False, default="user")

    __table_args__ = (
        CheckConstraint("role IN ('admin', 'user')", name="check_user_role"),
    )
