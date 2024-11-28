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

        transcription = """
### Instruction:
Anda bertugas untuk mengevaluasi transkrip dari seseorang yang sedang berbicara dalam bentuk pidato atau presentasi. Evaluasi harus dilakukan dengan memperhatikan konteks dari input yang berupa transkrip untuk memberikan penilaian yang akurat terhadap kualitas berbicara. Ada empat aspek utama yang wajib Anda nilai: Kejelasan Berbicara, Penggunaan Diksi, Kelancaran dan Intonasi, serta Emosional dan Keterlibatan Audiens. Output Anda harus dalam bentuk JSON yang berisi array dengan dua elemen seperti contoh berikut:

[{"Kejelasan Berbicara": <skor>, "Penggunaan Diksi": <skor>, "Kelancaran dan Intonasi": <skor>, "Emosional dan Keterlibatan Audiens": <skor>}, {"text": "<teks analisis yang mendalam dan terstruktur>"}]

Penjelasan mengenai elemen dalam array JSON:

1. Elemen pertama adalah objek JSON yang memuat skor penilaian untuk keempat aspek berikut:
   {"Kejelasan Berbicara": 82, "Penggunaan Diksi": 78, "Kelancaran dan Intonasi": 72, "Emosional dan Keterlibatan Audiens": 80}

   Setiap aspek harus memiliki nilai skor dalam rentang 0-100, dengan 100 menunjukkan performa maksimal.

2. Elemen kedua adalah objek JSON yang memiliki properti "text" berisi analisis mendalam dalam format paragraf yang terstruktur. Teks analisis harus mencakup:
   - **Ulasan Umum**: Berikan ulasan yang mencakup kekuatan dan kelemahan transkrip secara keseluruhan, serta kaitkan dengan skor yang diberikan.
   - **Saran dan Solusi**: Sediakan saran praktis untuk memperbaiki kelemahan yang ditemukan, ditulis dalam format bulet poin untuk memudahkan pemahaman.
   - **Kesimpulan**: Ringkasan singkat tentang bagaimana pembicara dapat meningkatkan kemampuan berbicara mereka di masa depan.

Anda hanya perlu mengeluarkan jawaban output sesuai dengan format output yang saya minta saja, anda dilarang mengeluarkan kata kata tambahan di luar format output(json) karena dapat mengganggu pemrosesan data di backend nantinya. Anda hanya perlu mengeluarkan jawaban saja tanpa ada tambahan kata kata di awal.

Contoh format output yang benar untuk seluruh JSON:
[{"Kejelasan Berbicara": 82, "Penggunaan Diksi": 78, "Kelancaran dan Intonasi": 72, "Emosional dan Keterlibatan Audiens": 80}, {"text": "**Analisis**\\n\\nPidato ini disampaikan dengan struktur yang jelas dan logis, membuat audiens dapat dengan mudah mengikuti alur pemikiran pembicara. Namun, ada beberapa kelemahan yang perlu diperhatikan. Kejelasan berbicara (82) cukup baik, tetapi bisa ditingkatkan dengan latihan yang lebih intensif. Penggunaan diksi (78) cukup beragam, tetapi ada ruang untuk perbaikan dalam variasi kata. Kelancaran dan intonasi (72) masih dapat ditingkatkan untuk menghindari kesan monoton. Emosional dan keterlibatan audiens (80) cukup efektif, tetapi dapat diperkuat dengan ekspresi lebih mendalam.\\n\\n**Saran untuk Peningkatan**\\n- Latih kelancaran berbicara untuk menghindari jeda yang tidak perlu.\\n- Gunakan variasi kata yang lebih kaya untuk membuat pidato lebih menarik.\\n- Latih ekspresi emosional untuk lebih melibatkan audiens.\\n- Rekam diri sendiri dan tinjau untuk meningkatkan kesadaran akan intonasi dan emosi.\\n\\n**Kesimpulan**\\n\\nPidato ini memiliki dasar yang kuat, tetapi masih ada ruang untuk perbaikan. Dengan memperhatikan saran-saran di atas, pembicara dapat lebih efektif dan menarik."}]

### Input:
Assalamualaikum warahmatullahi wabarakatuh. Salam sejahtera untuk kita semua. Om Swastiastu nama budaya, Salam Kebajikan. Yang terhormat Bupati Kendal Bapak Diko Ganindutah Bachelor of Science, yang saya hormati, kepala dinas pendidikan dan kebudayaan ke Bupatan Kendal, Bapak Wahyu Yusuf Ahmadieh SSTP MSI, serta teman-teman satu bangsa, satu bahasa, dan satu tanah air. Pada tanggal 1 Juni 1945, seorang pendiri bangsa Bung Karnau menyampaikan pandangannya tentang fondasi dasar Indonesia merdeka yang disebut dengan istilah pancasila. Limasila pancasila adalah pedoman kita untuk hidup bernagara, membangun persatuan bangsa dalam makna yang sesungguhnya. Kita dilahirkan dari latar belakang suku, ras, agama dan adat-adat istri-adat yang berbeda. Pancazila lahir sebagai jawapan dari semua pertanyaan yang ada, setelah banyak kekhawatiran bahwa bangsa kita akan mudah terpecah pelah. Apakah kita sudah pancazilais ketika masih banyak teman-teman kita yang melakukan bullying, ketika masih banyak terjadi tauran antar pelajar dan ketika masih banyak remaja yang menggunakan media sosial dengan tidak bijak sebagai pelajar, hendaknya kita benar-benar menyadari, memahami dan mengaplikasikan nilai-nilai pancasilah dalam kehidupan kita sehari-hari sekecil apapun hal baik yang bisa kita lakukan, maka lakukanlah Jadikan pancasilah sebagai pedoman kita menuju Indonesia Mars 2045. Dimikian, Bidato yang dapat saya sampaikan. Wassalamualaikum warahmatullahi wabarakatuh.
"""

        logger.debug(f"Calling save_transcription_data with topic={topic} and user_id={user.id}")

        analysis_payload = {
            "input_text": transcription
        }
        analysis_response = await hit_analysis_api(analysis_payload)
        analysis_result = json.loads(analysis_response[0])

        score = {
            "kejelasan": str(analysis_result[0]["Kejelasan Berbicara"]),
            "diksi": str(analysis_result[0]["Penggunaan Diksi"]),
            "kelancaran": str(analysis_result[0]["Kelancaran dan Intonasi"]),
            "emosi": str(analysis_result[0]["Emosional dan Keterlibatan Audiens"]),
        }

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

    except Exception as e:
        logger.exception("An unexpected error occurred")
        raise HTTPException(status_code=500, detail=f"Processing failed: {str(e)}")
    finally:
        if os.path.exists(temp_file_location):
            os.remove(temp_file_location)
            logger.debug(f"Temporary file {temp_file_location} deleted.")


async def hit_analysis_api(payload: dict):
    url = "http://34.31.109.8:8000/predict/"
    headers = {
        "accept": "application/json",
        "Content-Type": "application/json"
    }
    async with aiohttp.ClientSession() as session:
        async with session.post(url, json=payload, headers=headers) as response:
            if response.status != 200:
                logger.error(f"Failed to get analysis. Status: {response.status}, Response: {await response.text()}")
                raise HTTPException(status_code=500, detail="Failed to get analysis from external API.")
            return await response.json()




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