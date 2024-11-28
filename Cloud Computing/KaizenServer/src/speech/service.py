# src/speech/service.py
from src.speech.models import UsersSpeechHistory
from src.user.models import User
from src.speech.utils import upload_audio_to_gcs
from sqlalchemy.orm import Session
from fastapi import UploadFile, HTTPException
from uuid import UUID
from mutagen.wave import WAVE
from mutagen import File
from tempfile import NamedTemporaryFile
import requests
import json

def format_duration(duration_in_seconds):
    duration_in_seconds = int(duration_in_seconds)
    hours = duration_in_seconds // 3600
    minutes = (duration_in_seconds % 3600) // 60
    seconds = duration_in_seconds % 60

    if hours > 0:
        return f"{hours}:{minutes}:{seconds}"
    elif minutes > 0:
        return f"{minutes}:{seconds}"
    else:
        return f" 00:{seconds}"


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
    audio_file: UploadFile,
    analyze: dict
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
        analize=json.dumps(analyze)
    )
    db.add(history)
    db.commit()
    db.refresh(history)
    return history


def get_audio_duration_from_url(audio_url: str) -> float:
    """
    Download the audio file from the given URL and calculate its duration.
    Supports multiple audio formats.
    """
    try:
        response = requests.get(audio_url, stream=True)
        response.raise_for_status()
        with NamedTemporaryFile(delete=True) as temp_audio:
            for chunk in response.iter_content(chunk_size=8192):
                temp_audio.write(chunk)
            temp_audio.flush()

            # Automatically detect file type with mutagen
            audio = File(temp_audio.name)
            if not audio:
                raise HTTPException(status_code=400, detail="Unsupported audio format")

            return audio.info.length
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to calculate audio duration: {str(e)}")


def get_speech_histories_by_user_id(db: Session, user_id: UUID):
    speech_histories = db.query(UsersSpeechHistory).filter(
        UsersSpeechHistory.user_id == user_id
    ).order_by(
        UsersSpeechHistory.created_at.desc()
    ).all()

    if not speech_histories:
        raise HTTPException(status_code=404, detail="No speech histories found for this user.")

    response_data = []
    for speech in speech_histories:
        duration = get_audio_duration_from_url(speech.audio_file_url)

        try:
            analyze_data = json.loads(speech.analize) if speech.analize else {
                "score": {
                    "kejelasan": "0",
                    "diksi": "0",
                    "kelancaran": "0",
                    "emosi": "0"
                },
                "analysis_message": ""
            }
        except (json.JSONDecodeError, TypeError):
            analyze_data = {
                "score": {
                    "kejelasan": "0",
                    "diksi": "0",
                    "kelancaran": "0",
                    "emosi": "0"
                },
                "analysis_message": ""
            }

        response_data.append({
            "id": str(speech.id),
            "audio_file_url": speech.audio_file_url,
            "user_id": str(speech.user_id),
            "topic": speech.topic,
            "transcribe": speech.transcribe,
            "score": analyze_data.get("score", {
                "kejelasan": "0",
                "diksi": "0",
                "kelancaran": "0",
                "emosi": "0"
            }),
            "analysis_message": analyze_data.get("analysis_message", ""),
            "duration": format_duration(duration),
            "created_at": speech.created_at.isoformat() if speech.created_at else None,
            "updated_at": speech.updated_at.isoformat() if speech.updated_at else None,
        })

    return response_data
