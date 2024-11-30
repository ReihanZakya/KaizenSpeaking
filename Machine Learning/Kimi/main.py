from groq import Groq 
import json
import os
import time 
import logging

groq_client = Groq(api_key="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")
llama_70B = "llama-3.1-70b-versatile"


def generate_topic():
    response = groq_client.chat.completions.create(
        messages=[{"role": "user", "content": "Buatkan satu judul tema atau topik untuk pidato atau presentasi yang relevan dan mencerminkan isu, tren, atau diskusi yang sedang populer atau penting di Indonesia. Pastikan topik ini variatif dan dapat mencakup berbagai bidang, seperti pendidikan, teknologi, budaya, lingkungan, kesehatan, ekonomi, atau politik. Judul harus pendek, jelas, dan langsung ke inti, tanpa penjelasan tambahan atau kata-kata yang tidak perlu. Hanya berikan topik dalam satu kalimat tanpa karakter atau simbol tambahan."}],
        model=llama_70B
    )
    return response.choices[0].message.content.strip()

def generate_script(topic):
    prompt = (
        f"Buatlah naskah pidato atau presentasi yang komprehensif dan terstruktur berdasarkan topik berikut: \"{topic}\". "
        "Naskah harus memenuhi standar tinggi untuk presentasi publik yang profesional, dengan elemen-elemen berikut:\n\n"
        "1. **Pengantar yang Menarik**: Mulailah dengan pengantar yang kuat untuk menarik perhatian audiens. "
        "Gunakan pernyataan, fakta mengejutkan, atau pertanyaan retoris yang relevan dengan topik untuk memancing rasa ingin tahu audiens.\n\n"
        "2. **Isi yang Informatif dan Terstruktur**: Susun argumen atau poin utama secara logis, dengan setiap bagian mengalir dengan lancar ke bagian berikutnya. "
        "Gunakan bahasa yang profesional namun tetap mudah dipahami, dan pastikan setiap poin didukung oleh data, contoh, atau fakta yang relevan. "
        "Pisahkan isi naskah menjadi paragraf-paragraf yang jelas, masing-masing dengan satu ide utama yang dikembangkan secara mendalam.\n\n"
        "3. **Penggunaan Bahasa dan Gaya yang Sesuai**: Gunakan variasi gaya bahasa untuk menjaga minat audiens, termasuk kalimat yang bervariasi panjangnya dan pilihan kata yang tepat dan kaya makna. "
        "Hindari penggunaan jargon yang terlalu teknis, kecuali sangat diperlukan, dan pastikan istilah-istilah tersebut dijelaskan dengan baik.\n\n"
        "4. **Kesimpulan yang Kuat**: Akhiri dengan kesimpulan yang ringkas namun berdampak, menggarisbawahi poin-poin utama dan meninggalkan kesan yang mendalam pada audiens. "
        "Hindari kata-kata penutup seperti 'Terima kasih' atau 'Salam'. Kesimpulan harus mendorong audiens untuk merenungkan atau mengambil tindakan berdasarkan isi pidato.\n\n"
        "Pastikan naskah ini tidak menyertakan kata-kata pembuka atau penutup tambahan, dan hanya fokus pada penyampaian ide yang terstruktur, menarik, dan relevan."
    )
    response = groq_client.chat.completions.create(
        messages=[{"role": "user", "content": prompt}],
        model=llama_70B
    )
    return response.choices[0].message.content.strip()

def generate_transcript(script_text):
    prompt = (
        f"Ubah naskah pidato atau presentasi berikut menjadi transkrip yang realistis, seolah-olah berasal dari rekaman suara seseorang yang sedang membacakan pidato ini:\n\n{script_text}\n\n"
        "Transkrip harus mencerminkan nuansa berbicara yang alami, seolah-olah pembicara berbicara langsung kepada audiens, dengan variasi gaya berbicara yang menunjukkan perbedaan kemampuan berbicara berdasarkan skala 0-100. Anda harus memilih skala secara acak di belakang layar tanpa menyebutkan angka atau keputusan kualitas tersebut dalam output.\n\n"
        
        "### Instruksi Terperinci:\n\n"
        
        "1. **Pilih Skala Kualitas Berbicara (0-100)**:\n"
        "   Pilih tingkat kualitas berbicara pada skala 0-100 secara acak, dan hasilkan transkrip sesuai tingkat yang dipilih:\n"
        "   - **0-20**: Gaya berbicara sangat tidak terorganisir, penuh dengan pengulangan, kalimat yang terputus, jeda panjang, dan keragu-raguan ekstrim.\n"
        "   - **21-40**: Transkrip tetap menunjukkan banyak ketidaksempurnaan, tetapi lebih terorganisir dibandingkan tingkat 0-20.\n"
        "   - **41-60**: Transkrip lebih terorganisir, dengan beberapa ketidaksempurnaan seperti jeda yang tidak perlu dan pilihan kata yang kurang efektif.\n"
        "   - **61-80**: Gaya berbicara lancar dan cukup profesional, dengan sedikit jeda atau pengulangan.\n"
        "   - **81-100**: Transkrip sangat lancar, percaya diri, dan profesional, tanpa kesalahan atau jeda yang tidak perlu.\n\n"
        
        "2. **Penggunaan Jeda ('...') dan Pilihan Kata**:\n"
        "   - Pada tingkat 0-40, gunakan jeda yang sering dan panjang, serta pilihan kata yang berulang.\n"
        "   - Pada tingkat 41-60, gunakan jeda yang lebih sedikit, tetapi tetap ada untuk menciptakan kesan berbicara yang cukup lancar.\n"
        "   - Pada tingkat 61-100, gunakan jeda yang sangat jarang atau tidak ada sama sekali, dengan pilihan kata yang bervariasi dan terstruktur.\n\n"
        
        "3. **Format Output**:\n"
        "   Hasilkan hanya isi transkrip tanpa menyebutkan skala, kualitas, atau angka yang dipilih. Pastikan transkrip konsisten dengan tingkat kualitas yang Anda pilih di belakang layar.\n\n"
        
        "### Contoh Output Transkrip yang Diharapkan:\n"
        "- \"Baik, mari kita mulai... dengan membahas tentang pentingnya pendidikan di era modern ini... hmm, kita tahu... bahwa... ehm... pendidikan memainkan peran besar dalam kehidupan kita... ya, sangat penting, bukan?\"\n\n"
        "- \"Pendidikan di era digital... sangat penting... kita harus mempersiapkan generasi muda... dengan keterampilan yang relevan... agar mereka dapat menghadapi tantangan di masa depan.\"\n\n"
        "- \"Pendidikan sangat penting di era digital ini. Kita harus mempersiapkan generasi muda dengan keterampilan yang sesuai agar mereka mampu menghadapi tantangan masa depan dengan percaya diri.\"\n\n"
        
        "### Tujuan:\n"
        "Transkrip ini akan digunakan untuk melatih model speech-to-text yang mampu mengenali berbagai kualitas berbicara. Oleh karena itu, penting untuk menghasilkan transkrip yang bervariasi, konsisten, dan realistis sesuai dengan skala kualitas yang Anda pilih di belakang layar."
    )
    
    response = groq_client.chat.completions.create(
        messages=[{"role": "user", "content": prompt}],
        model=llama_70B
    )
    return response.choices[0].message.content.strip()

def generate_score(transcript):
    prompt = (
        f"Anda bertugas untuk mengevaluasi transkrip pidato berikut dan memberikan skor untuk setiap aspek kualitas berbicara. "
        "Skor harus dalam bentuk angka bulat antara 0 hingga 100. Ikuti format yang diberikan di bawah ini tanpa menambahkan kata-kata tambahan:\n\n"
        
        "### Format Output:\n"
        "[{\n"
        "  \"Kejelasan Berbicara\": nilai,\n"
        "  \"Penggunaan Diksi\": nilai,\n"
        "  \"Kelancaran dan Intonasi\": nilai,\n"
        "  \"Emosional dan Keterlibatan Audiens\": nilai\n"
        "}]\n\n"
        
        "### Instruksi Penilaian:\n"
        "1. **Kejelasan Berbicara**: Nilai seberapa jelas pesan yang disampaikan oleh pembicara. Apakah ide-ide utama mudah dipahami?\n"
        "2. **Penggunaan Diksi**: Evaluasi apakah kata-kata yang digunakan tepat, bervariasi, dan sesuai dengan konteks audiens.\n"
        "3. **Kelancaran dan Intonasi**: Analisis apakah pidato mengalir dengan baik dan apakah intonasi digunakan dengan tepat untuk menyoroti poin penting.\n"
        "4. **Emosional dan Keterlibatan Audiens**: Nilai bagaimana pidato melibatkan audiens secara emosional.\n\n"
        
        "### Contoh Output yang Benar:\n"
        "[{\n"
        "  \"Kejelasan Berbicara\": 90,\n"
        "  \"Penggunaan Diksi\": 85,\n"
        "  \"Kelancaran dan Intonasi\": 88,\n"
        "  \"Emosional dan Keterlibatan Audiens\": 87\n"
        "}]\n\n"
        
        f"### Transkrip:\n{transcript}\n\n"
        "Berikan output hanya dalam format yang diminta, seperti contoh di atas."
    )
    
    response = groq_client.chat.completions.create(
        messages=[{"role": "user", "content": prompt}],
        model=llama_70B
    )
    return response.choices[0].message.content.strip()

def generate_analysis(transcript, score):
    prompt = (
        f"Anda bertugas untuk menganalisis transkrip pidato berikut dengan mempertimbangkan skor yang telah diberikan untuk setiap aspek kualitas berbicara. "
        "Berikan analisis yang terperinci dan komprehensif, termasuk saran untuk peningkatan dalam bentuk bullet poin, serta kesimpulan penutup yang memotivasi. "
        "Pastikan hasil analisis Anda terhubung erat dengan skor yang diberikan dan isi transkrip.\n\n"
        
        "### Transkrip:\n"
        f"{transcript}\n\n"
        
        "### Skor Penilaian:\n"
        f"{score}\n\n"
        
        "### Format Output:\n"
        "1. **Analisis**: Tulis analisis rinci dalam bentuk paragraf yang menjelaskan kekuatan dan kelemahan pidato. Hindari menggunakan format key-value.\n"
        "2. **Saran untuk Peningkatan**: Tulis saran dalam paragraf dengan poin-poin bullet untuk mempermudah pemahaman.\n"
        "3. **Kesimpulan**: Tulis kesimpulan dalam paragraf yang memotivasi pengguna untuk terus berlatih dan meningkatkan kemampuan berbicara mereka.\n\n"
        
        "### Contoh Output yang Benar:\n"
        "\"Pidato ini disampaikan dengan kejelasan yang sangat baik, dengan struktur kalimat yang terorganisir dan diksi yang cukup bervariasi. "
        "Pembicara menggunakan intonasi yang efektif untuk menyoroti poin-poin penting, yang membantu mempertahankan perhatian audiens. "
        "Namun, ada beberapa bagian yang dapat diperkuat dengan penggunaan diksi yang lebih kuat dan ekspresi emosional yang lebih menonjol untuk meningkatkan keterlibatan audiens.\"\n\n"
        "Saran untuk peningkatan:\n"
        "- Gunakan variasi kata yang lebih kaya untuk memperkaya diksi.\n"
        "- Latih kelancaran berbicara dengan merekam diri sendiri dan mengevaluasi bagian yang terdengar tersendat.\n"
        "- Perkuat ekspresi emosional untuk menggugah audiens secara lebih efektif.\n\n"
        "\"Kesimpulannya, pidato ini kuat dalam penyampaian pesan, tetapi dapat ditingkatkan lebih lanjut dengan memperhatikan aspek-aspek yang disebutkan. "
        "Dengan latihan terus-menerus, pembicara dapat mencapai tingkat yang lebih profesional dan efektif.\"\n\n"
        
        "Berikan output Anda dalam format yang sama seperti contoh di atas."
    )
    
    response = groq_client.chat.completions.create(
        messages=[{"role": "user", "content": prompt}],
        model=llama_70B
    )
    return response.choices[0].message.content.strip()

logging.basicConfig(filename='kaizen.log', level=logging.INFO)
file_path = "dataset_kaizen_v1.json"

if os.path.exists(file_path):
    with open(file_path, "r", encoding="utf-8") as file:
        data_entries = json.load(file)
else:
    data_entries = []

try:
    while True:
        logging.info("Membuat topik random...")
        topic = generate_topic()
        logging.info(f"Topik: {topic}\n")
        time.sleep(2)

        logging.info("Membuat naskah pidato...")
        script = generate_script(topic)
        logging.info("Naskah pidato dibuat.")
        time.sleep(2)

        logging.info("Membuat transkrip pidato...")
        transcript = generate_transcript(script)
        logging.info("Transkrip pidato dibuat.")
        time.sleep(2) 

        logging.info("Memberikan skor penilaian...")
        score = generate_score(transcript)
        logging.info(f"Skor: {score}\n")
        time.sleep(2) 

        logging.info("Menganalisis dan menilai transkrip...")
        analysis = generate_analysis(transcript, score)
        logging.info("Analisis pidato selesai.")
        time.sleep(2) 

        data_entry = {
            "input": transcript,
            "score": json.loads(score), 
            "analyze": analysis
        }
        data_entries.append(data_entry)

        with open(file_path, "w", encoding="utf-8") as file:
            json.dump(data_entries, file, ensure_ascii=False, indent=4)
        logging.info("Data entry baru telah disimpan ke 'new_dataset_kaizen.json'.")

        time.sleep(5)

except Exception as e:
    logging.error(f"Terjadi error: {e}")
    with open(file_path, "w", encoding="utf-8") as file:
        json.dump(data_entries, file, ensure_ascii=False, indent=4)
    logging.info("Data yang terkumpul telah disimpan ke 'dataset_kaizenV1.json'.")
