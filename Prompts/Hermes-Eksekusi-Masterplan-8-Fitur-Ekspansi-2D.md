# Prompt Eksekusi Lengkap: Masterplan 8 Fitur Ekspansi (2D Portrait Android)
## Proyek: Game Saham Android (Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`)

Kamu adalah AI Principal Engineer yang ditugaskan untuk mengeksekusi secara penuh seluruh rancangan dalam **Masterplan 8 Fitur Ekspansi Game Saham 2D Portrait**. Tugasmu adalah membangun, mengintegrasikan, dan menguji artefak kode nyata sampai selesai, bukan sekadar menulis ringkasan, membuat rencana ulang, atau menghasilkan kode pura-pura (*stubs/mocking*).

---

### ATURAN UTAMA & LARANGAN KERAS:
1. **DILARANG MERINGKAS ATAU MENGGABUNGKAN TASK SECARA ILEGAL:**
   Jangan memangkas, meringkas, atau melompati subtask apa pun. Setiap item dari ke-8 fitur ekspansi wajib diimplementasikan secara utuh (*unbundled*), memiliki model domain C# nyata, tabel SQLite nyata, tampilan UI vertikal 2D Portrait nyata, dan pemeriksaan yang lulus verifikasi.
2. **DILARANG MENGAKHIRI TURN DENGAN JANJI TINDAKAN MASA DEPAN:**
   Jika suatu fitur atau langkah dapat dikerjakan, eksekusi sekarang menggunakan alatmu. Selesaikan tahap demi tahap secara mandiri hingga seluruh acceptance terpenuhi.
3. **KEDAULATAN LOGIKA TUNGGAL (C# DOMAIN):**
   Seluruh aturan bursa, formula pembagian dividen, evaluasi kemenangan duel bos, verifikasi kepatuhan pajak, dan bonus booster riset dikelola 100% di C# Domain. Kode JavaScript di UI murni sebagai penampil data dan penangkap gestur sentuh.
4. **BATAS REPO & PERTAHANKAN PERUBAHAN LOKAL:**
   Gunakan root absolut: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM` sebagai satu-satunya repo kerja. Jangan menyentuh repo lain. Jangan menghapus atau mengubah konfigurasi lokal yang sudah ada tanpa alasan terverifikasi.

---

### DOKUMEN RUJUKAN WAJIB (BACA SEBELUM CODING):
1. `Docs/MASTERPLAN_8_FITUR_EKSPANSI_2D.md` — Rujukan arsitektur, 8 fitur ekspansi, dan skema SQLite tambahan.
2. `Docs/MASTERPLAN_15_FITUR_2D_PORTRAIT.md` — Rujukan masterplan fondasi 15 fitur inti.
3. `Docs/Product/01-Konsep-Game-2D-Portrait.md` & `Docs/Product/02-PRD-Game-Saham-2D-Portrait.md` — Konsep produk dan PRD 2D Portrait.
4. `Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md` — Panduan visual 2D Portrait, palet warna, dan aturan aksesibilitas.

---

### CAKUPAN 8 FITUR EKSPANSI YANG WAJIB SELESAI SECARA UTUH:

#### 1. Duel Lawan Bos Taipan Pasar (*Market Boss Showdown*)
- [ ] Integrasikan 4 bos taipan (Easy, Medium, Hard, Final Boss) yang terhubung ke aset visual (`kolay_patron.jpg`, `orta_patron.jpg`, `zor_patron.png`, `final_boss.png`).
- [ ] Mekanisme pertarungan return cuan portofolio pemain vs Bos selama rentang sesi tertentu.
- [ ] Dialog tantangan, papan perbandingan return *real-time*, dan hadiah kemenangan.

#### 2. Audit Pajak & Kepatuhan Risiko (*Tax Audit & Risk Compliance*)
- [ ] Formula pemicu audit mendadak berdasarkan rasio utang/leverage margin yang melebihi batas aman (> 40% aset).
- [ ] Konsekuensi audit: denda kas dan pembekuan plafon pinjaman jika gagal, atau diskon pajak dividen 10% jika patuh.

#### 3. Pembagian Dividen Tunai Emiten Saham (*Dividend Payouts & Yield*)
- [ ] Kalender dividen emiten (Cum-Date, Ex-Date, dan Payment Date).
- [ ] Eksekusi transfer otomatis dividen tunai ke saldo rekening pemain berdasarkan jumlah lembar saham yang dimiliki saat Cum-Date.

#### 4. Pangkat Level Investor & Buka Kunci Sektor (*Tiered Level & Sector Unlocks*)
- [ ] Sistem 10 tingkat pangkat investor (terhubung ke folder aset `level_system/` dan `rank_system/`).
- [ ] Pembatasan gembok sektor: Level 1–3 (Perbankan & Konsumer), Level 4–6 (Energi & Tambang), Level 7–10 (Teknologi, Global AS, Komoditas).

#### 5. Buku Rekor Medali Prestasi Permanen (*Trophy Achievements*)
- [ ] Sistem rekor prestasi (minimal 15 medali pencapaian unik, terhubung ke aset `achievements.png`).
- [ ] Pengecekan otomatis di akhir sesi dan klaim hadiah kas serta bonus status (*perk*) permanen.

#### 6. Kustomisasi Avatar & Bingkai Profil Prestise (*Avatar & Frames*)
- [ ] Pilihan 12 foto profil karakter investor (folder `avatars_players/`) dan 15 bingkai profil eksklusif (folder `frame_players/`).
- [ ] Modal ganti avatar/bingkai di tab Beranda/Profil dengan status *Locked/Unlocked* berbasis pencapaian liga dan bos.

#### 7. Kartu Riset R&D & Booster Efisiensi Sementara (*R&D Boosters*)
- [ ] 3 tipe kartu booster (Bocoran Akurasi Berita, Bebas Komisi Broker, Suntikan Plafon Margin).
- [ ] Dompet kartu pada tab Riset dengan durasi aktif berbasis jumlah sesi atau pemakaian.

#### 8. Manajemen Moral Staf & Fasilitas Kantor (*Staff Morale & Office Perks*)
- [ ] Upgrade fasilitas kantor (Pantry Kopi, Ruang Rekreasi, Server Komputasi) terhubung ke 35 gambar ruangan di `rooms/`.
- [ ] Skor kebahagiaan staf (*Happiness Score*) yang secara dinamis mempengaruhi akurasi riset analis dan laju pertumbuhan dana kelolaan nasabah (AUM).

---

### TAHAPAN PELAKSANAAN (ROADMAP EKSEKUSI):

#### TAHAP E1: DIVIDEN & TIERING SEKTOR (Tab PASAR)
1. Terapkan tabel `StockDividends` pada SQLite lokal.
2. Bangun mesin jadwal dividen di C# Domain (jadwal Cum-Date, kalkulasi hak per lembar, dan kredit otomatis ke saldo kas saat Payment Date).
3. Bangun validasi gembok sektor pada saat pemain memilih saham di tab Pasar berdasarkan level profil pemain.
4. Uji runnable: simulasi pembayaran dividen dan pencegahan order pada sektor yang masih terkunci.

#### TAHAP E2: DUEL BOS TAIPAN & KEPATUHAN PAJAK (Tab BERANDA & ASET)
1. Terapkan tabel `MarketBossShowdown` pada SQLite lokal.
2. Bangun alur tantangan duel: pemantauan return harian pemain vs Bos Taipan (Easy s/d Final Boss).
3. Bangun sistem audit kepatuhan risiko: pemicu audit jika rasio utang tinggi dan eksekusi denda pajak.
4. Hubungkan UI dialog duel bos dan indikator status kepatuhan pajak di UI 2D Portrait.

#### TAHAP E3: MEDALI PRESTASI, AVATAR & BINGKAI PROFIL (Tab BERANDA)
1. Terapkan tabel `PlayerAchievements` dan `ProfileCosmetics` pada SQLite lokal.
2. Bangun modul pelacak prestasi otomatis (menghitung total dividen, kemenangan duel bos, dan total transaksi).
3. Bangun modal kustomisasi profil di tab Beranda: pemilihan 12 avatar dan 15 bingkai unik.
4. Uji runnable: unlock prestasi dan pergantian avatar/bingkai yang tersimpan persisten di SQLite.

#### TAHAP E4: FASILITAS KANTOR & KARTU BOOSTER RISET (Tab FIRMA & RISET)
1. Terapkan tabel `ResearchBoosters` pada SQLite lokal.
2. Bangun sistem fasilitas kantor pada tab Firma: renovasi ruangan, kalkulasi kebahagiaan staf, dan peningkatan AUM.
3. Bangun dompet kartu booster riset pada tab Riset: aktivasi kartu, pengurangan sisa durasi sesi, dan pengali buff.
4. Lakukan pengujian end-to-end menyeluruh pada 2D Portrait Android: verifikasi fluiditas sentuhan ($\ge 48\text{ dp}$), performa database, dan build APK Android.

---

### FORMAT LAPORAN KEMAJUAN (SETIAP SELESAI SUBTASK/TAHAP):
Laporankan secara ringkas dan faktual:
- **Progres:** X dari 4 Tahap Ekspansi Selesai (Fitur Aktif: ...)
- **Keluaran Nyata:** Berkas yang dibuat/dimodifikasi (sebutkan path absolutnya).
- **Pemeriksaan & Bukti:** Perintah uji yang dijalankan beserta hasil output riil (exit code 0 / PASS).
- **Hambatan:** Catat kendala teknis nyata atau nyatakan "Tidak ada".
- **Langkah Berikutnya:** Tugas terdekat yang akan langsung dikerjakan.

---

MULAI SEKARANG DARI TAHAP E1: Terapkan tabel dividen, bangun mesin pembagian dividen tunai, dan pasang validasi gembok sektor saham. Jalankan eksekusi secara mandiri sampai tuntas!
