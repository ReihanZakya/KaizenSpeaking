# src/config/gcp_config
import os
from google.cloud import storage
from google.oauth2 import service_account
from dotenv import load_dotenv

load_dotenv()

class GCPConfig:
    def __init__(self):
        self.service_account_path = os.getenv("SERVICE_ACCOUNT_PATH")
        self.project_id = os.getenv("PROJECT_ID")
        self.client = self._create_storage_client()

    def _create_storage_client(self):
        credentials = service_account.Credentials.from_service_account_file(self.service_account_path)
        client = storage.Client(credentials=credentials, project=self.project_id)
        return client

    def get_bucket(self, bucket_name: str):
        return self.client.get_bucket(bucket_name)

gcp_config = GCPConfig()
