# src/speech/models.py
from sqlalchemy import Column, String, Text, TIMESTAMP, ForeignKey
from sqlalchemy.dialects.postgresql import UUID as PGUUID
from sqlalchemy.orm import relationship
import uuid
from src.database import Base

class UsersSpeechHistory(Base):
    __tablename__ = "users_speech_history"

    id = Column(PGUUID(as_uuid=True), primary_key=True, default=uuid.uuid4, unique=True, index=True)
    audio_file_url = Column(Text, nullable=False)
    user_id = Column(PGUUID(as_uuid=True), ForeignKey("users.id"), nullable=False)
    topic = Column(Text, nullable=False)
    transcribe = Column(Text, nullable=False)
    analize = Column(Text, nullable=True)
    created_at = Column(TIMESTAMP, default="NOW()")
    updated_at = Column(TIMESTAMP, default="NOW()")

    user = relationship("User", back_populates="speech_histories")

