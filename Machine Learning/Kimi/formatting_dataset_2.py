import json

# Load the original dataset with utf-8 encoding
with open('dataset_kaizen_v2.json', 'r', encoding='utf-8') as file:
    data = json.load(file)

# Iterate through each entry in the dataset and apply the desired transformations
for entry in data:
    # Construct the explanation array with scores and text analysis
    response_scores = entry.get("response", [{}])[0]  # Extract the first response object
    explanation_text = entry["explanation"]  # Extract the original textual explanation

    # Create the new explanation format
    explanation_array = [
        {
            "Kejelasan Berbicara": response_scores.get("Kejelasan Berbicara", 0),
            "Penggunaan Diksi": response_scores.get("Penggunaan Diksi", 0),
            "Kelancaran dan Intonasi": response_scores.get("Kelancaran dan Intonasi", 0),
            "Emosional dan Keterlibatan Audiens": response_scores.get("Emosional dan Keterlibatan Audiens", 0)
        },
        {"text": explanation_text}
    ]

    # Update the entry with the new explanation and remove the response key
    entry["explanation"] = explanation_array
    if "response" in entry:
        del entry["response"]

    # Update the instruction to reflect the new explanation format
    entry["instruction"] = (
    """
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
[{"Kejelasan Berbicara": 82, "Penggunaan Diksi": 78, "Kelancaran dan Intonasi": 72, "Emosional dan Keterlibatan Audiens": 80}, {"text": "**Analisis**\n\nPidato ini disampaikan dengan struktur yang jelas dan logis, membuat audiens dapat dengan mudah mengikuti alur pemikiran pembicara. Namun, ada beberapa kelemahan yang perlu diperhatikan. Kejelasan berbicara (82) cukup baik, tetapi bisa ditingkatkan dengan latihan yang lebih intensif. Penggunaan diksi (78) cukup beragam, tetapi ada ruang untuk perbaikan dalam variasi kata. Kelancaran dan intonasi (72) masih dapat ditingkatkan untuk menghindari kesan monoton. Emosional dan keterlibatan audiens (80) cukup efektif, tetapi dapat diperkuat dengan ekspresi lebih mendalam.\n\n**Saran untuk Peningkatan**\n- Latih kelancaran berbicara untuk menghindari jeda yang tidak perlu.\n- Gunakan variasi kata yang lebih kaya untuk membuat pidato lebih menarik.\n- Latih ekspresi emosional untuk lebih melibatkan audiens.\n- Rekam diri sendiri dan tinjau untuk meningkatkan kesadaran akan intonasi dan emosi.\n\n**Kesimpulan**\n\nPidato ini memiliki dasar yang kuat, tetapi masih ada ruang untuk perbaikan. Dengan memperhatikan saran-saran di atas, pembicara dapat lebih efektif dan menarik."}]
    """
)

# Save the modified dataset to a new file
with open('final_dataset_kaizen.json', 'w', encoding='utf-8') as file:
    json.dump(data, file, ensure_ascii=False, indent=4)

print("Dataset successfully updated and saved in 'updated_merged_dataset_kaizen.json'")
