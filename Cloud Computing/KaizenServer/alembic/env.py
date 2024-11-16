import sys
import os
from logging.config import fileConfig
from dotenv import load_dotenv

load_dotenv()

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from sqlalchemy import engine_from_config, pool
from alembic import context
from src.database import Base
from src.user.models import User
from src.speech.models import UsersSpeechHistory

config = context.config

database_url = os.getenv("DATABASE_URL")
if not database_url:
    raise ValueError("DATABASE_URL tidak ditemukan di .env.development")

config.set_main_option("sqlalchemy.url", database_url)

fileConfig(config.config_file_name)
target_metadata = Base.metadata

print("Detected tables:", Base.metadata.tables.keys())

def run_migrations_online():
    connectable = engine_from_config(
        config.get_section(config.config_ini_section),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    with connectable.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=target_metadata,
        )

        with context.begin_transaction():
            context.run_migrations()

run_migrations_online()
