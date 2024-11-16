# src/speech/schemas.py
from pydantic import BaseModel
from uuid import UUID

class UsersSpeechHistoryCreate(BaseModel):
    audio_file_url: str
    topic: str
    transcribe: str
    analize: str = None

class UsersSpeechHistoryResponse(BaseModel):
    id: UUID
    audio_file_url: str
    topic: str
    transcribe: str
    analize: str = None

    class Config:
        from_attributes = True
        json_encoders = {UUID: str}
