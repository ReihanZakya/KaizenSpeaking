from fastapi.responses import StreamingResponse
from fastapi import APIRouter, Depends, UploadFile, File, Form, HTTPException
from sqlalchemy.orm import Session
from src.handler import success_get
from src.middleware import JWTBearer
from src.dependencies import get_db
from src.exceptions import DataNotFoundError
import os
import uuid
import json
import time
import logging
from uuid import UUID
from src.speech.service import save_transcription_data, get_speech_histories_by_user_id, generate_random_score

router = APIRouter()
logger = logging.getLogger(__name__)

MAX_FILE_SIZE_MB = 20


def json_text_streamer(text: str, speech_id: int):
    try:
        # Generate scores using speech_id as seed
        score = {
            "kejelasan": str(generate_random_score(speech_id + 1)),
            "diksi": str(generate_random_score(speech_id + 2)),
            "kelancaran": str(generate_random_score(speech_id + 3)),
            "emosi": str(generate_random_score(speech_id + 4)),
        }

        # Stream each word
        for word in text.split():
            json_data = json.dumps({"word": word, "score": score})
            yield json_data + "\n"
            time.sleep(0.1)

    except Exception as e:
        logger.error(f"Error in streamer: {e}")
        raise StopIteration


@router.post("/speech-to-text", dependencies=[Depends(JWTBearer())])
async def speech_to_text_save(
        file: UploadFile = File(...),
        topic: str = Form(..., description="Topic of the audio"),
        user_id: str = Form(...),
        db: Session = Depends(get_db)
):
    # Validate file size
    logger.debug("Validating file size...")
    file.file.seek(0, os.SEEK_END)
    file_size = file.file.tell() / (1024 * 1024)
    if file_size > MAX_FILE_SIZE_MB:
        raise HTTPException(status_code=400, detail=f"File size exceeds {MAX_FILE_SIZE_MB} MB limit.")
    file.file.seek(0)

    temp_file_location = f"temp_audio_{uuid.uuid4()}.wav"
    try:
        logger.debug(f"Saving file temporarily at {temp_file_location}")
        with open(temp_file_location, "wb") as temp_file:
            while contents := file.file.read(1024 * 1024):
                temp_file.write(contents)
        file.file.seek(0)

        transcription = ""
        logger.debug(f"Calling save_transcription_data with topic={topic} and user_id={user_id}")
        history = save_transcription_data(
            db=db,
            topic=topic,
            user_id=user_id,
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

        analysis_message = "analize belum tersedia, masih dalam pengembangan\n\n" \
                           "Silakan coba lagi nanti."
        logger.debug("Returning StreamingResponse")

        # Create a combined response with score and streamer
        def combined_stream():
            # First send the static score part
            yield json.dumps({"score": score}) + "\n"

            # Then stream the word-by-word response
            for word in analysis_message.split():
                yield json.dumps({"word": word}) + "\n"
                time.sleep(0.1)

        return StreamingResponse(combined_stream(), media_type="text/event-stream")

    except DataNotFoundError:
        logger.exception("User not found")
        raise HTTPException(status_code=404, detail="User not found")
    except ValueError as e:
        logger.exception("Value error occurred")
        raise HTTPException(status_code=400, detail=str(e))
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