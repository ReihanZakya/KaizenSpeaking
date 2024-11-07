import os
import uvicorn
from fastapi import FastAPI

app = FastAPI()

port = int(os.environ.get("PORT", 8080))

@app.get("/")
def read_root():
    return {"Hello": "World"}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=port)
