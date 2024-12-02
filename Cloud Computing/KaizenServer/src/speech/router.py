import aiohttp
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
from src.speech.service import save_transcription_data, get_speech_histories_by_user_id
from src.speech.utils import upload_audio_to_gcs
from src.user.models import User
from src.user.schemas import UserCreate
from src.user.service import create_user
import asyncio

router = APIRouter()
logger = logging.getLogger(__name__)

MAX_FILE_SIZE_MB = 20

def is_valid_uuid(value: str) -> bool:
    try:
        uuid.UUID(value)
        return True
    except ValueError:
        return False


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

        logger.debug(f"Uploading file to GCS.")
        file.file.seek(0)
        audio_url = upload_audio_to_gcs(file)
        if not audio_url:
            raise HTTPException(status_code=500, detail="Failed to upload audio file to GCS.")

        logger.debug("Requesting transcription from speech-to-text API.")
        transcription_payload = {"audio_url": audio_url}
        async with aiohttp.ClientSession() as session:
            async with session.post(
                    "https://speech-to-text-whisper-955631459397.asia-southeast2.run.app/transcribe",
                    json=transcription_payload,
                    ssl=False
            ) as response:
                if response.status != 200:
                    raise HTTPException(
                        status_code=500,
                        detail=f"Failed to get transcription. Response status: {response.status}."
                    )
                transcription_data = await response.json()

        transcription_text = transcription_data.get("transcription", "")

        if not transcription_text:
            raise HTTPException(status_code=500, detail="Transcription response is empty.")

        logger.debug("Formatting transcription with Instruction and Input.")

        # Format transcription with Instruction and Input
        transcription = f"""
### Instruction:
Anda bertugas untuk mengevaluasi transkrip dari seseorang yang sedang berbicara dalam bentuk pidato atau presentasi. Evaluasi harus dilakukan dengan memperhatikan konteks dari input yang berupa transkrip untuk memberikan penilaian yang akurat terhadap kualitas berbicara. Ada empat aspek utama yang wajib Anda nilai: Kejelasan Berbicara, Penggunaan Diksi, Kelancaran dan Intonasi, serta Emosional dan Keterlibatan Audiens. Output Anda harus dalam bentuk JSON yang berisi array dengan dua elemen seperti contoh berikut:

[{{"Kejelasan Berbicara": <skor>, "Penggunaan Diksi": <skor>, "Kelancaran dan Intonasi": <skor>, "Emosional dan Keterlibatan Audiens": <skor>}}, {{"text": "<teks analisis yang mendalam dan terstruktur>"}}]

Penjelasan mengenai elemen dalam array JSON:

1. Elemen pertama adalah objek JSON yang memuat skor penilaian untuk keempat aspek berikut:
   {{"Kejelasan Berbicara": 82, "Penggunaan Diksi": 78, "Kelancaran dan Intonasi": 72, "Emosional dan Keterlibatan Audiens": 80}}

   Setiap aspek harus memiliki nilai skor dalam rentang 0-100, dengan 100 menunjukkan performa maksimal.

2. Elemen kedua adalah objek JSON yang memiliki properti "text" berisi analisis mendalam dalam format paragraf yang terstruktur. Teks analisis harus mencakup:
   - **Ulasan Umum**: Berikan ulasan yang mencakup kekuatan dan kelemahan transkrip secara keseluruhan, serta kaitkan dengan skor yang diberikan.
   - **Saran dan Solusi**: Sediakan saran praktis untuk memperbaiki kelemahan yang ditemukan, ditulis dalam format bulet poin untuk memudahkan pemahaman.
   - **Kesimpulan**: Ringkasan singkat tentang bagaimana pembicara dapat meningkatkan kemampuan berbicara mereka di masa depan.

Anda hanya perlu mengeluarkan jawaban output sesuai dengan format output yang saya minta saja, anda dilarang mengeluarkan kata kata tambahan di luar format output(json) karena dapat mengganggu pemrosesan data di backend nantinya. Anda hanya perlu mengeluarkan jawaban saja tanpa ada tambahan kata kata di awal.

Contoh format output yang benar untuk seluruh JSON:
[{{"Kejelasan Berbicara": 82, "Penggunaan Diksi": 78, "Kelancaran dan Intonasi": 72, "Emosional dan Keterlibatan Audiens": 80}}, {{"text": "**Analisis**\\n\\nPidato ini disampaikan dengan struktur yang jelas dan logis, membuat audiens dapat dengan mudah mengikuti alur pemikiran pembicara. Namun, ada beberapa kelemahan yang perlu diperhatikan. Kejelasan berbicara (82) cukup baik, tetapi bisa ditingkatkan dengan latihan yang lebih intensif. Penggunaan diksi (78) cukup beragam, tetapi ada ruang untuk perbaikan dalam variasi kata. Kelancaran dan intonasi (72) masih dapat ditingkatkan untuk menghindari kesan monoton. Emosional dan keterlibatan audiens (80) cukup efektif, tetapi dapat diperkuat dengan ekspresi lebih mendalam.\\n\\n**Saran untuk Peningkatan**\\n- Latih kelancaran berbicara untuk menghindari jeda yang tidak perlu.\\n- Gunakan variasi kata yang lebih kaya untuk membuat pidato lebih menarik.\\n- Latih ekspresi emosional untuk lebih melibatkan audiens.\\n- Rekam diri sendiri dan tinjau untuk meningkatkan kesadaran akan intonasi dan emosi.\\n\\n**Kesimpulan**\\n\\nPidato ini memiliki dasar yang kuat, tetapi masih ada ruang untuk perbaikan. Dengan memperhatikan saran-saran di atas, pembicara dapat lebih efektif dan menarik."}}]

### Input:
{transcription_text}
"""

        logger.debug("Sending transcription to analysis API.")
        analysis_payload = {"input_text": transcription}
        analysis_response = await hit_analysis_api(analysis_payload)

        try:
            analysis_result = json.loads(analysis_response[0])
            if not isinstance(analysis_result, list) or len(analysis_result) != 2:
                raise ValueError("Invalid analysis response format.")
        except (json.JSONDecodeError, ValueError) as e:
            raise HTTPException(
                status_code=500,
                detail=f"Analysis API response is not valid: {str(e)}"
            )

        logger.debug("Saving transcription and analysis to database.")
        file.file.seek(0)
        score = analysis_result[0]
        analysis_message = analysis_result[1]["text"]
        history = save_transcription_data(
            db=db,
            topic=topic,
            user_id=user.id,
            transcription=transcription,
            audio_file=file,
            analyze={"score": score, "analysis_message": analysis_message}
        )

        def combined_stream():
            yield json.dumps({"score": score}) + "\n"
            for word in analysis_message.split():
                yield json.dumps({"word": word}) + "\n"
                time.sleep(0.1)

        return StreamingResponse(combined_stream(), media_type="text/event-stream")

    finally:
        if os.path.exists(temp_file_location):
            os.remove(temp_file_location)
            logger.debug(f"Temporary file {temp_file_location} deleted.")


async def hit_analysis_api(payload: dict, max_retries: int = 3, retry_delay: float = 2.0):
    url = "http://34.42.79.133:8000/predict/"
    headers = {"accept": "application/json", "Content-Type": "application/json"}

    for attempt in range(1, max_retries + 1):
        try:
            async with aiohttp.ClientSession() as session:
                async with session.post(url, json=payload, headers=headers) as response:
                    if response.status == 200:
                        return await response.json()
                    else:
                        logger.error(f"Attempt {attempt}: API returned status {response.status}.")
        except Exception as e:
            logger.error(f"Attempt {attempt}: Error during API call: {str(e)}")
        if attempt < max_retries:
            await asyncio.sleep(retry_delay)

    raise HTTPException(status_code=500, detail="Failed to get analysis from external API after multiple retries.")


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