from fastapi.responses import StreamingResponse
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException
from sqlalchemy.orm import Session
from src.handler import success_get
from src.middleware import JWTBearer
from src.dependencies import get_db
import os
import uuid
import json
import time
import logging
from uuid import UUID
from src.speech.service import save_transcription_data, get_speech_histories_by_user_id, generate_random_score
from src.user.models import User
from src.user.schemas import UserCreate
from src.user.service import create_user

router = APIRouter()
logger = logging.getLogger(__name__)

MAX_FILE_SIZE_MB = 20

def is_valid_uuid(value: str) -> bool:
    try:
        uuid.UUID(value)
        return True
    except ValueError:
        return False


def json_text_streamer(text: str, speech_id: int):
    try:
        score = {
            "kejelasan": str(generate_random_score(speech_id + 1)),
            "diksi": str(generate_random_score(speech_id + 2)),
            "kelancaran": str(generate_random_score(speech_id + 3)),
            "emosi": str(generate_random_score(speech_id + 4)),
        }
        for word in text.split():
            json_data = json.dumps({"word": word, "score": score})
            yield json_data + "\n"
            time.sleep(0.1)

    except Exception as e:
        logger.error(f"Error in streamer: {e}")
        raise StopIteration


@router.post("/upload-speech")
async def speech_to_text_save(
        file: UploadFile = File(...),
        topic: str = Form(..., description="Topic of the audio"),
        user_id: str = Form(...),
        db: Session = Depends(get_db)
):
    logger.debug("Validating file size...")
    file.file.seek(0, os.SEEK_END)
    file_size = file.file.tell() / (1024 * 1024)
    if file_size > MAX_FILE_SIZE_MB:
        raise HTTPException(status_code=400, detail=f"File size exceeds {MAX_FILE_SIZE_MB} MB limit.")
    file.file.seek(0)

    temp_file_location = f"temp_audio_{uuid.uuid4()}.wav"
    try:
        user = None

        if is_valid_uuid(user_id):
            logger.debug(f"user_id {user_id} is a valid UUID. Checking in database...")
            user = db.query(User).filter(User.id == user_id).first()
        else:
            logger.debug(f"user_id {user_id} is treated as a Device ID. Checking for matching user...")
            user = db.query(User).filter(User.device_id == user_id).first()

        if not user:
            logger.info(f"No user found for user_id {user_id}. Creating a new guest user.")


            guest_user_data = UserCreate(
                email=None,
                password="123456",
                full_name="Guest",
                nickname=None,
                phone_number=None,
                role="user",
                device_id=user_id if not is_valid_uuid(user_id) else None
            )

            user = create_user(db=db, user=guest_user_data)

        logger.debug(f"Using user_id {user.id} for speech upload (device_id: {user.device_id}).")

        logger.debug(f"Saving file temporarily at {temp_file_location}")
        with open(temp_file_location, "wb") as temp_file:
            while contents := file.file.read(1024 * 1024):
                temp_file.write(contents)
        file.file.seek(0)

        transcription = ""
        logger.debug(f"Calling save_transcription_data with topic={topic} and user_id={user.id}")
        history = save_transcription_data(
            db=db,
            topic=topic,
            user_id=user.id,
            transcription=transcription,
            audio_file=file
        )

        # Generate random scores
        score = {
            "kejelasan": str(generate_random_score(id(history) + 1)),
            "diksi": str(generate_random_score(id(history) + 2)),
            "kelancaran": str(generate_random_score(id(history) + 3)),
            "emosi": str(generate_random_score(id(history) + 4)),
        }

        analysis_message = "Analize belum tersedia, masih dalam pengembangan.\n\nSilakan coba lagi nanti."

        def combined_stream():
            yield json.dumps({"score": score}) + "\n"
            for word in analysis_message.split():
                yield json.dumps({"word": word}) + "\n"
                time.sleep(0.1)

        return StreamingResponse(combined_stream(), media_type="text/event-stream")

    except Exception as e:
        logger.exception("An unexpected error occurred")
        raise HTTPException(status_code=500, detail=f"Processing failed: {str(e)}")
    finally:
        if os.path.exists(temp_file_location):
            os.remove(temp_file_location)
            logger.debug(f"Temporary file {temp_file_location} deleted.")



@router.get("/speech-history/{user_id}", dependencies=[Depends(JWTBearer())])
def get_speech_by_user_id(user_id: UUID, db: Session = Depends(get_db)):
    """
    Endpoint to retrieve speech histories by user ID.
    """
    try:
        response_data = get_speech_histories_by_user_id(db, user_id)
        return success_get(data=response_data, message="Speech histories retrieved successfully")
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An unexpected error occurred: {str(e)}")