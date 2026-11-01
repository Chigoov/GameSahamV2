# Brief gambar 150 koleksi — sesuai rarity

Permintaan pemilik22Oktober2026: buat chat khusus untuk menghasilkan gambar/design dari seluruh150item katalog. Gambar mengikuti kelangkaan; semakin langka semakin bagus dan istimewa. Tugas ini khusus aset visual, bukan implementasi gameplay atau revisi ekonomi.

## Sumber yang mengikat

Baca `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM/Work/Owner-Revision-14-2026-10-22/KATALOG-KOLEKSI-150.csv`. Gunakan item_id, nama, kategori dan rarity persis. Total150item,12kategori,5rarity:25Umum,29TidakUmum,35Langka,29Epik,32Legendaris. Jangan menciptakan daftar baru atau mengubah rarity supaya mudah digambar.

Semua perubahan hanya untuk manifest/brief/bukti/aset pada `Work/Collection-Art-150-2026-10-22/` dan `Artifacts/Collection-Art-150-2026-10-22/`. Jangan ubah source game, katalog ekonomi, save pribadi, file agent lain, signing atau pengaturan global. Pertahankan file existing dan jangan overwrite tanpa izin; gunakan varian bersufiks jika revisi.

## Gaya bersama

- Ilustrasi digital premium semi-realistis untuk game2Dportrait; objek mudah dikenali dan tetap jelas pada ukuran kartu kecil. Bukan pixel art atau screenshot aplikasi.
- Satu item per gambar, komposisi persegi1:1. TargetPNG1024x1024; jika tool memberi ukuran native berbeda/lebih besar, catat ukuran asli secara jujur. Jangan mengklaim hasil upscaling sebagai output native.
- Latar transparan dengan alpha asli agar gambar bisa ditempel pada UI navy/purple existing. Siluet utuh, objek terpusat, ruang tepi sekitar10%, tidak terpotong.
- Perspektif tiga perempat untuk kendaraan/jam/perhiasan; properti berbentuk diorama/isometrik rapi; lukisan/peta berupa objek berbingkai; trofi/memorabilia memiliki pedestal bila diperlukan. Pertahankan skala visual/kamera yang konsisten dalam kategori.
- Tanpa nama item, angka harga, tulisan rarity, UI, watermark atau logo tambahan dalam gambar. Rarity ditunjukkan lewat penyajian objek. Jangan memasukkan frame kartu/badge yang nanti bisa dibuat UI.
- Semua tier harus tetap tajam dan profesional. Umum bukan kualitas buruk. Perbedaan kelangkaan adalah kemewahan material, kerumitan detail, finishing, pencahayaan, dan daya tarik komposisi.
- Jangan mengubah identitas: Vespa tetap Vespa; mobil tetap model yang diminta; properti/jam/lukisan tidak diganti ikon generik. Untuk item rekaan, desain boleh lebih kreatif sesuai nama/kategori.

## Hierarki visual lima rarity

| Rarity katalog | Arahan visual |
|---|---|
| UMUM | Bentuk bersih, material masuk akal, detail terkendali, studio lighting lembut/netral. Mudah dibaca dan tetap rapi. |
| TIDAK_UMUM | Finishing lebih halus, aksen material premium kecil, tekstur lebih kaya, rim light lembut dan komposisi lebih elegan. |
| LANGKA | Detail kolektor yang nyata, bahan berkualitas tinggi, relief/refleksi presisi, pencahayaan produk lebih dramatis namun tidak menyilaukan. |
| EPIK | Craftsmanship mencolok, komponen/material kompleks, detail artistik eksklusif, cahaya berlapis dengan aksen ungu yang terkendali; siluet tetap terbaca. |
| LEGENDARIS | Hero asset paling istimewa: material dan detail terbaik yang sesuai objek, finishing museum/atelier, komposisi ikonik, pencahayaan sinematik serta aksen emas elegan bila cocok. Jangan sekadar menambahkan glow/emas pada salinan tier lain. |

Aksen rarity membantu hierarki, bukan alasan mengubah warna asli/model objek secara tidak masuk akal. Jangan memberi permata/gold plating pada semua mobil/rumah demi terlihat langka. Untuk warisan seni, kemewahan berada pada karya, bingkai, detail dan penyajian.

## Proses dan bukti

1. Verifikasi root/Git dan catalog hash. Baca skillimagegen yang tersedia; gunakan built-in image_gen. Satu call per item dengan prompt spesifik, bukan satu lembar150thumbnail. Jangan memakai APIberbayar/CLI/key fallback tanpa instruksi pemilik.
2. Buat5aset pertama yang mewakili seluruh rarity dari satu kategori jika memungkinkan. Kelimanya adalah item katalog sesungguhnya dan dihitung ke150. Pakai hasil yang sudah diperiksa sebagai referensi konsistensi berikutnya. Tidak perlu meminta approval rutin untuk tiap gambar.
3. Teruskan per kelompok kategori/tier; simpan hasil final langsung diworkspace. Output built-in biasanya berada di CODEX_HOME/generated_images: salin file hasil yang benar-benar dikembalikan ke folder proyek, bukan menebak path atau mengarang hasil. Jangan meninggalkan aset final hanya di folder default alat.
4. Simpan `Artifacts/Collection-Art-150-2026-10-22/images/<item_id>.png`. Varian ditaruh di subfoldervariants dan hanya satu hasil terpilih dicatat sebagai final setiapID. Jangan overwrite hasil yang bukan buatan tugas ini.
5. Periksa hasil visual: identitas subjek, kategori/rarity, siluet, crop, konsistensi, alpha, background pada navy dan terang, ketajaman ukuran kartu. Gunakan imagegen untuk koreksi visual; jangan mengganti gambar denganSVG/emoji/foto stok atau menggambar raster lewat kode sebagai pengganti.
6. Update manifest per item dengan status, path final, ukuran, SHA256, actual generation provenance, prompt dan hasil pemeriksaan. Hanya tandai GENERATED_VERIFIED jika file nyata ada dan sudah dilihat. Placeholder/prompt saja tidak dihitung gambar selesai.
7. Buat indeks HTML lokal atau contact sheets untuk review jika membantu, tetapi itu tambahan;150PNGterpisah tetap hasil wajib. Jangan menyebut contact sheet sebagai pengganti150gambar.
8. Laporkan progres x/150, kategori/tier yang selesai, temuan/revisi, lokasi output dan next action. Tidak mengklaim150selesai saat baru5/sekadar manifest. Jika tool unavailable/limit, simpan checkpoint item tepat dan sebut hambatan; jangan mengarang gambar atau otomatis beralihAPI/layanan lain.

## Acceptance akhir

150ID unik mempunyai150file final berbeda,12kategori dan5rarity cocok sumber, alpha asli untuk outputcutout, ukuran aktual tercatat, source/prompt/finalhash tersedia, seluruh hasil diperiksa, dan hierarki kemewahan antar rarity terlihat. Katalog/ekonomi/sourcegame/save tidak berubah. Final handoff memberi folderaset, manifest, preview representatif kelima rarity dan daftar yang masih kurang bila ada.
