import json

# Membaca dataset JSON
with open("final_dataset_kaizen.json", "r", encoding="utf-8") as file:
    data = json.load(file)

# Menulis ke file JSON Lines
with open("final_dataset_kaizen.jsonl", "w", encoding="utf-8") as file:
    for entry in data:
        json.dump(entry, file, ensure_ascii=False)
        file.write("\n")
