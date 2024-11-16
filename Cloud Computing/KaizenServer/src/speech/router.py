from fastapi import File, UploadFile, HTTPException, APIRouter
import whisper
import os
import ssl
import urllib.request
import json
from starlette.responses import StreamingResponse
import time

router = APIRouter()
model = None

@router.on_event("startup")
async def startup_event():
    global model

    ssl_context = ssl._create_unverified_context()
    opener = urllib.request.build_opener(urllib.request.HTTPSHandler(context=ssl_context))
    urllib.request.install_opener(opener)

    model = whisper.load_model("tiny")


def json_word_streamer(text: str):
    for word in text.split():
        json_data = json.dumps({"word": word})
        yield json_data + "\n"
        time.sleep(0.05)


@router.post("/speech-to-text/")
async def speech_to_text(file: UploadFile = File(...)):
    if file.content_type not in ["audio/wav", "audio/mpeg", "audio/mp3"]:
        raise HTTPException(status_code=400, detail="Unsupported file format. Please upload a WAV or MP3 file.")

    file_location = "temp_audio.wav"
    with open(file_location, "wb") as f:
        while contents := file.file.read(1024 * 1024):
            f.write(contents)
    file.file.close()

    try:
        result = model.transcribe(file_location)
        transcription = result["text"]
        os.remove(file_location)

        return StreamingResponse(json_word_streamer(transcription), media_type="application/x-ndjson")
    except Exception as e:
        os.remove(file_location)
        raise HTTPException(status_code=500, detail=f"Transcription failed: {e}")
