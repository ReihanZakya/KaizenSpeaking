FROM python:3.10-slim

# Install ffmpeg dependencies
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy requirements and install dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of the application
COPY . .

# Set the build argument and environment variable
ARG ENVIRONMENT=development
ENV ENVIRONMENT=$ENVIRONMENT

# Copy the correct .env file based on the environment
RUN if [ "$ENVIRONMENT" = "development" ]; then \
        cp .env.development .env; \
    else \
        cp .env.local .env; \
    fi

# Expose the port
EXPOSE 8080

# Run database migrations
RUN alembic upgrade head || echo "Migration failed or already applied"

# Command to run the application
CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8080"]
