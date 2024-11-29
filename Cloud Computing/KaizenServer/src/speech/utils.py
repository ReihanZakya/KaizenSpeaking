# src/speech/utils.py

from fastapi import UploadFile
from src.config import gcp_config
import uuid

def upload_audio_to_gcs(file: UploadFile, bucket_name: str = "kaizen_audio"):
    if not file:
        return None

    bucket = gcp_config.gcp_config.get_bucket(bucket_name)
    file_id = str(uuid.uuid4())
    blob = bucket.blob(f"audio/{file_id}_{file.filename}")
    blob.upload_from_file(file.file)
    return blob.public_url

