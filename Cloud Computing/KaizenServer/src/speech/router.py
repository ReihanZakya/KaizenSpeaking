from fastapi.responses import StreamingResponse
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException
from sqlalchemy.orm import Session
from src.middleware import JWTBearer
from src.dependencies import get_db
from src.speech.service import save_transcription_data
from src.exceptions import DataNotFoundError
import whisper
import os
import uuid
import json
import time

router = APIRouter()

model = whisper.load_model("tiny")

def json_word_streamer(text: str):
    """
    Stream transcription word by word in JSON format with a slight delay for streaming effect.
    """
    for word in text.split():
        json_data = json.dumps({"word": word})
        yield json_data + "\n"
        time.sleep(0.05)

@router.post("/speech-to-text-save/", dependencies=[Depends(JWTBearer(required_role="user"))])
async def speech_to_text_save(
    file: UploadFile = File(...),
    topic: str = Form(..., description="Topic of the audio"),
    user_id: str = Form(...),
    db: Session = Depends(get_db)
):
    if file.content_type not in ["audio/wav", "audio/mpeg", "audio/mp3"]:
        raise HTTPException(status_code=400, detail="Unsupported file format. Please upload a WAV or MP3 file.")

    temp_file_location = f"temp_audio_{uuid.uuid4()}.wav"
    with open(temp_file_location, "wb") as temp_file:
        while contents := file.file.read(1024 * 1024):
            temp_file.write(contents)

    try:
        result = model.transcribe(temp_file_location)
        transcription = result["text"]
        file.file.seek(0)

        save_transcription_data(db, topic=topic, user_id=user_id, transcription=transcription, audio_file=file)

        os.remove(temp_file_location)
        return StreamingResponse(
            json_word_streamer(transcription),
            media_type="application/x-ndjson"
        )

    except DataNotFoundError:
        raise HTTPException(status_code=404, detail="User not found")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        if os.path.exists(temp_file_location):
            os.remove(temp_file_location)
        raise HTTPException(status_code=500, detail=f"Transcription failed: {str(e)}")
