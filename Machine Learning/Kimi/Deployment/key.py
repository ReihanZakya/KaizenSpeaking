import os

os.environ['PUB_KEY_PATH'] = os.path.expanduser("~/.ollama/id_ed25519.pub")

pub_key_path = os.getenv('PUB_KEY_PATH')
print(pub_key_path)

with open(pub_key_path, 'r') as f:
    public_key = f.read()
    
print(public_key)