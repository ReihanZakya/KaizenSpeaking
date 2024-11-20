# src/speech/service.py
from src.speech.models import UsersSpeechHistory
from src.user.models import User
from src.speech.utils import upload_audio_to_gcs
from sqlalchemy.orm import Session
from fastapi import UploadFile, HTTPException

def save_transcription_data(
    db: Session,
    topic: str,
    user_id: str,
    transcription: str,
    audio_file: UploadFile
):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    audio_file_url = upload_audio_to_gcs(audio_file)
    if not audio_file_url:
        raise ValueError("Failed to upload the audio file to GCP.")

    history = UsersSpeechHistory(
        audio_file_url=audio_file_url,
        user_id=user_id,
        topic=topic,
        transcribe=transcription,
        analize=None
    )
    db.add(history)
    db.commit()
    db.refresh(history)
    return history
