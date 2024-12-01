from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import subprocess

app = FastAPI()

MODEL_NAME = "KimiDandy/kaizenv9"

class TextInput(BaseModel):
    input_text: str

@app.post("/predict/")
async def predict(input_data: TextInput):
    """
    Endpoint untuk melakukan prediksi menggunakan CLI Ollama.
    """
    try:
        result = subprocess.run(
            ["ollama", "run", MODEL_NAME],
            input=input_data.input_text,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        if result.returncode != 0:
            raise HTTPException(
                status_code=500,
                detail=f"Ollama CLI Error: {result.stderr}"
            )
        
        raw_output = result.stdout.strip()
        return {raw_output}

    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Error during prediction: {str(e)}"
        )

@app.get("/")
async def root():
    """
    Endpoint default untuk memeriksa apakah API berjalan.
    """
    return {"message": "Kaizen Speaking API berjalan!"}
