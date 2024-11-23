# src/speech/service.py
from src.speech.models import UsersSpeechHistory
from src.user.models import User
from src.speech.utils import upload_audio_to_gcs
from sqlalchemy.orm import Session
from fastapi import UploadFile, HTTPException
from uuid import UUID

def generate_random_score(seed: int):
    """
    Generate a pseudo-random number based on a seed without using any external libraries.
    The range of the output is between 50 and 100.
    """
    seed = (seed * 9301 + 49297) % 233280
    return 50 + seed % 51

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

def get_speech_histories_by_user_id(db: Session, user_id: UUID):
    speech_histories = db.query(UsersSpeechHistory).filter(
        UsersSpeechHistory.user_id == user_id
    ).order_by(
        UsersSpeechHistory.created_at.desc()
    ).all()

    if not speech_histories:
        raise HTTPException(status_code=404, detail="No speech histories found for this user.")
    response_data = [
        {
            "id": str(speech.id),
            "audio_file_url": speech.audio_file_url,
            "user_id": str(speech.user_id),
            "topic": speech.topic,
            "transcribe": speech.transcribe,
            "analize": speech.analize,
            "score" : {
                "kejelasan": str(generate_random_score(id(speech) + 1)),
                "diksi": str(generate_random_score(id(speech) + 2)),
                "kelancaran": str(generate_random_score(id(speech) + 3)),
                "emosi": str(generate_random_score(id(speech) + 4)),
            },
            "created_at": speech.created_at.isoformat() if speech.created_at else None,
            "updated_at": speech.updated_at.isoformat() if speech.updated_at else None,
        }
        for speech in speech_histories
    ]

    return response_data