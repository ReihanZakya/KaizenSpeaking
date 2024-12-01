import requests

BASE_URL = "http://localhost:8000"

def test_root():
    try:
        response = requests.get(f"{BASE_URL}/")
        if response.status_code == 200:
            print("Endpoint Root berhasil")
        else:
            print("Response:", response.text)
    except Exception as e:
        print("Endpoint Root gagal, ", str(e))

def test_predict_complex():
    input_text = """
### Instruction:
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

### Input:
Assalamualaikum warahmatullahi wabarakatuh. Salam sejahtera untuk kita semua. Om Swastiastu nama budaya, Salam Kebajikan. Yang terhormat Bupati Kendal Bapak Diko Ganindutah Bachelor of Science, yang saya hormati, kepala dinas pendidikan dan kebudayaan ke Bupatan Kendal, Bapak Wahyu Yusuf Ahmadieh SSTP MSI, serta teman-teman satu bangsa, satu bahasa, dan satu tanah air. Pada tanggal 1 Juni 1945, seorang pendiri bangsa Bung Karnau menyampaikan pandangannya tentang fondasi dasar Indonesia merdeka yang disebut dengan istilah pancasila. Limasila pancasila adalah pedoman kita untuk hidup bernagara, membangun persatuan bangsa dalam makna yang sesungguhnya. Kita dilahirkan dari latar belakang suku, ras, agama dan adat-adat istri-adat yang berbeda. Pancazila lahir sebagai jawapan dari semua pertanyaan yang ada, setelah banyak kekhawatiran bahwa bangsa kita akan mudah terpecah pelah. Apakah kita sudah pancazilais ketika masih banyak teman-teman kita yang melakukan bullying, ketika masih banyak terjadi tauran antar pelajar dan ketika masih banyak remaja yang menggunakan media sosial dengan tidak bijak sebagai pelajar, hendaknya kita benar-benar menyadari, memahami dan mengaplikasikan nilai-nilai pancasilah dalam kehidupan kita sehari-hari sekecil apapun hal baik yang bisa kita lakukan, maka lakukanlah Jadikan pancasilah sebagai pedoman kita menuju Indonesia Mars 2045. Dimikian, Bidato yang dapat saya sampaikan. Wassalamualaikum warahmatullahi wabarakatuh.
    """
    try:
        response = requests.post(
            f"{BASE_URL}/predict/",
            json={"input_text": input_text}
        )
        if response.status_code == 200:
            print(response.json())
        else:
            print("Endpoint Predict gagal.")
            print("Response:", response.text)
    except Exception as e:
        print("Endpoint Predict gagal", str(e))

if __name__ == "__main__":
    print("Testing Endpoint Root")
    test_root()

    print("\nTesting Endpoint Predict")
    test_predict_complex()
