import json

# Muat dataset asli
with open("dataset_kaizen_v1.json", "r", encoding="utf-8") as file:
    data = json.load(file)

# Tambahkan instruction dan ubah key sesuai permintaan
instruction_text = (
    "Anda bertugas untuk mengevaluasi transkrip dari seseorang yang sedang berbicara dalam bentuk pidato atau presentasi. "
    "Anda harus memperhatikan konteks dari input yang berupa transkrip untuk memberikan penilaian yang akurat terhadap kualitas berbicara. "
    "Anda wajib menilai empat aspek utama, yaitu: Kejelasan Berbicara, Penggunaan Diksi, Kelancaran dan Intonasi, serta Emosional dan Keterlibatan Audiens. "
    "Penilaian untuk setiap aspek harus disajikan dalam format JSON yang terstruktur dan konsisten, seperti contoh berikut:\n\n"
    "response: [\n"
    "    {\n"
    "        \"Kejelasan Berbicara\": 82,\n"
    "        \"Penggunaan Diksi\": 78,\n"
    "        \"Kelancaran dan Intonasi\": 72,\n"
    "        \"Emosional dan Keterlibatan Audiens\": 80\n"
    "    }\n"
    "]\n\n"
    "Selain memberikan penilaian skor, anda harus menyediakan analisis mendalam dalam sektor explanation. "
    "Analisis ini harus ditulis dalam bentuk paragraf terstruktur yang menjelaskan kualitas pidato secara keseluruhan, mendukung skor yang diberikan. "
    "Explanation juga harus mencakup poin-poin berikut:\n"
    "1. **Ulasan Umum**: Jelaskan kekuatan dan kelemahan transkrip secara keseluruhan, menghubungkan dengan skor yang diberikan.\n"
    "2. **Saran dan Solusi**: Berikan saran praktis untuk memperbaiki kelemahan yang ditemukan. Saran ini harus ditulis dalam bentuk bulet poin untuk memudahkan pembaca.\n"
    "3. **Kesimpulan**: Akhiri dengan rangkuman singkat tentang bagaimana pembicara dapat meningkatkan kemampuan berbicara mereka di masa depan.\n\n"
    "Pastikan anda tetap konsisten dan tidak menyimpang dari format yang diharapkan. Hasil akhir harus mudah dipahami dan dapat diproses oleh sistem backend."
)


# Perbarui dataset
for entry in data:
    entry["instruction"] = instruction_text
    entry["input"] = entry.pop("input")
    entry["response"] = entry.pop("score")
    entry["explanation"] = entry.pop("analyze")

# Simpan dataset baru
with open("dataset_kaizen_v2.json", "w", encoding="utf-8") as file:
    json.dump(data, file, ensure_ascii=False, indent=4)
