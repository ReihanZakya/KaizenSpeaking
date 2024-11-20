import uuid
from sqlalchemy import Column, String, CheckConstraint
from sqlalchemy.dialects.postgresql import UUID as PGUUID
from sqlalchemy.orm import relationship
from src.database import Base

class User(Base):
    __tablename__ = "users"

    id = Column(PGUUID(as_uuid=True), primary_key=True, default=uuid.uuid4, unique=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String, nullable=False)
    nickname = Column(String, nullable=True)
    phone_number = Column(String, nullable=True)
    role = Column(String, nullable=False, default="user")

    # Add a CHECK constraint to ensure only "admin" or "user" values are allowed
    __table_args__ = (
        CheckConstraint("role IN ('admin', 'user')", name="check_user_role"),
    )

    speech_histories = relationship("UsersSpeechHistory", back_populates="user", cascade="all, delete-orphan")
