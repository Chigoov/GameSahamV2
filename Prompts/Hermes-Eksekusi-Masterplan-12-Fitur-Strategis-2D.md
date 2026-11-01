# Prompt Eksekusi Lengkap: Masterplan 12 Fitur Strategis & Konglomerasi (2D Portrait Android)
## Proyek: Game Saham Android (Repo: `C:/Users/HYPE AMD/Downloads/VIBE CODING/GAME SAHAM`)

Kamu adalah AI Principal Engineer yang ditugaskan untuk mengeksekusi secara penuh seluruh rancangan dalam **Masterplan 12 Fitur Strategis & Konglomerasi Game Saham 2D Portrait (Fase 3)**. Tugasmu adalah membangun, mengintegrasikan, dan menguji artefak kode nyata sampai selesai, bukan sekadar menulis ringkasan, membuat rencana ulang, atau menghasilkan kode pura-pura (*stubs/mocking*).

---

### ATURAN UTAMA & LARANGAN KERAS:
1. **DILARANG MERINGKAS ATAU MENGGABUNGKAN TASK SECARA ILEGAL:**
   Jangan memangkas, meringkas, atau melompati subtask apa pun. Setiap item dari ke-12 fitur strategis ini wajib diimplementasikan secara utuh (*unbundled*), memiliki model domain C# nyata, tabel SQLite nyata, tampilan UI vertikal 2D Portrait nyata, dan pemeriksaan yang lulus verifikasi.
2. **DILARANG MENGAKHIRI TURN DENGAN JANJI TINDAKAN MASA DEPAN:**
   Jika suatu fitur atau langkah dapat dikerjakan, eksekusi sekarang menggunakan alatmu. Selesaikan tahap demi tahap secara mandiri hingga seluruh acceptance terpenuhi.
3. **KEDAULATAN LOGIKA TUNGGAL (C# DOMAIN):**
   Seluruh aturan bursa, formula deposito berjangka, penalti penarikan dini, klaim asuransi saat shock pasar, dan transfer modal internal holding dikelola 100% di C# Domain. Kode JavaScript di UI murni sebagai penampil data dan penangkap gestur sentuh.
4. **BATAS REPO & PERTAHANKAN PERUBAHAN LOKAL:**
   Gunakan root absolut: `C:\Users\HYPE AMD\Downloads\VIBE CODING\GAME SAHAM` sebagai satu-satunya repo kerja. Jangan menyentuh repo lain. Jangan menghapus atau mengubah konfigurasi lokal yang sudah ada tanpa alasan terverifikasi.

---

### DOKUMEN RUJUKAN WAJIB (BACA SEBELUM CODING):
1. `Docs/MASTERPLAN_12_FITUR_STRATEGIS_2D.md` — Rujukan arsitektur, 12 fitur strategis, dan skema SQLite tambahan Fase 3.
2. `Docs/MASTERPLAN_15_FITUR_2D_PORTRAIT.md` & `Docs/MASTERPLAN_8_FITUR_EKSPANSI_2D.md` — Rujukan masterplan fondasi dan ekspansi sebelumnya.
3. `Docs/Product/01-Konsep-Game-2D-Portrait.md` & `Docs/Product/02-PRD-Game-Saham-2D-Portrait.md` — Konsep produk dan PRD 2D Portrait.
4. `Docs/Design/03-Design-Brief-Game-Saham-2D-Portrait.md` — Panduan visual 2D Portrait, palet warna, dan aturan aksesibilitas.

---

### CAKUPAN 12 FITUR STRATEGIS YANG WAJIB SELESAI SECARA UTUH:

#### 1. Deposito Bank Berjangka & Penalti Penarikan Dini (*Time Deposits*)
- [ ] Pilihan tenor 10, 25, dan 50 sesi dengan bonus bunga berjenjang (+10%, +25%, +50%).
- [ ] Aturan penalti pembatalan dini: seluruh akumulasi bunga hangus (*Lost Interest Penalty*) jika dicairkan sebelum jatuh tempo.

#### 2. Asuransi Portofolio Lindung Nilai (*Portfolio Insurance / Hedging*)
- [ ] Pembelian polis asuransi sebelum sesi bursa dibuka dengan premi berbasis total nilai portofolio.
- [ ] Penggantian otomatis 80% dari nilai kerugian jika terjadi penurunan pasar ekstrem (*market crash* > 5%).

#### 3. Penyelamatan Kebangkrutan & Restrukturisasi (*Insolvency Bailout*)
- [ ] Fase darurat kebangkrutan: lelang kilat aset koleksi mewah untuk melunasi utang bank.
- [ ] Negosiasi penjadwalan utang dengan bantuan konsultan penyehat keuangan (*Turnaround Specialist*).

#### 4. Perusahaan Induk Konglomerasi (*Holding Company & Subsidiaries*)
- [ ] Pendirian holding company yang membawahi anak-anak usaha di sektor berbeda (Finansial, Energi, Konsumer, Teknologi).
- [ ] Fitur suntik modal internal (*capital injection*) dan penarikan dividen antar anak usaha tanpa pajak bursa.

#### 5. Guncangan Pasar Mendadak (*Market Shock & Black Swan Events*)
- [ ] Skenario bencana ekonomi global tak terduga yang merontokkan indeks pasar 10–20% dalam 1 sesi.
- [ ] Uji ketahanan likuiditas: keunggulan bagi pemain yang memegang cadangan kas tunai atau safe-haven.

#### 6. Krisis Rantai Pasok & Lonjakan Biaya Sektor (*Supply Chain Crisis*)
- [ ] Krisis kelangkaan bahan baku atau logistik yang memotong margin laba emiten manufaktur/retail secara tajam.
- [ ] Efek divergensi: sektor manufaktur anjlok, sementara sektor komoditas energi melonjak.

#### 7. Pohon Riset Bisnis & Teknologi Finansial (*Income Research Tech Tree*)
- [ ] 3 jalur riset: Jalur Algoritma (deteksi sinyal bursa), Jalur Efisiensi (potong biaya komisi/sewa), dan Jalur Sindikasi (perbesar batas AUM).
- [ ] Alokasi anggaran kas firma untuk upgrade tier riset secara berjenjang.

#### 8. Jalur Bonus Disiplin Harian (*Streak Day Strip & Broken Streak Alarm*)
- [ ] Strip progres harian (Day 1 s/d Day 7+) dengan pelipatgandaan bonus kas modal harian.
- [ ] Peringatan *Broken Streak* jika pemain absen 1 hari bursa.

#### 9. Hak Suara & Dominasi Sektor Bursa (*Sector Dominance & Influence*)
- [ ] Status Penguasa Sektor jika kepemilikan saham pemain melebihi 25% kapitalisasi pasar satu sektor industri.
- [ ] Hak veto kebijakan sektor dan diskon biaya transaksi permanen di sektor terkait.

#### 10. Perang Bakat & Pembajakan Karyawan Elit (*Headhunting & Talent Poaching*)
- [ ] Risiko staf bintang dibajak firma kompetitor jika kepuasan (*morale*) atau gajinya rendah.
- [ ] Fitur Headhunting untuk membajak analis riset legendaris dari firma saingan.

#### 11. Meracik Reksa Dana Kustom (*Custom Mutual Funds & Asset Allocation*)
- [ ] Antarmuka diagram lingkaran (*Pie Chart*) untuk mengatur bobot alokasi aset (Saham, Komoditas, Kripto, Kas).
- [ ] Penerbitan unit reksa dana untuk investor nasabah guna memperoleh komisi pengelolaan (*management fee*).

#### 12. Perdagangan Aset Kripto Alternatif (*Crypto Asset Trading*)
- [ ] Pasar aset digital alternatif 24 jam dengan volatilitas tinggi tanpa batasan suspensi (*Auto Rejection*).

---

### TAHAPAN PELAKSANAAN (ROADMAP EKSEKUSI):

#### TAHAP S1: LIKUIDITAS, DEPOSITO & ASURANSI (Tab PASAR & ASET)
1. Terapkan tabel `TimeDeposits` dan `PortfolioInsurancePolicy` pada SQLite lokal.
2. Bangun mesin perhitungan bunga deposito harian, kalkulasi bonus tenor, dan penalti pembatalan dini di C# Domain.
3. Bangun logika klaim asuransi portofolio saat nilai portofolio anjlok drastis.
4. Bangun alur restrukturisasi darurat kebangkrutan modal.

#### TAHAP S2: KONGSI KONGLEMERASI & REKSA DANA (Tab FIRMA & ASET)
1. Terapkan tabel `HoldingSubsidiaries` dan `CustomMutualFunds` pada SQLite lokal.
2. Bangun logika transfer kas internal holding company dan kalkulasi valuasi anak usaha.
3. Bangun antarmuka racikan diagram alokasi Reksa Dana Kustom di UI 2D Portrait.
4. Hubungkan modul perdagangan Aset Kripto alternatif dengan mesin harga volatilitas tinggi.

#### TAHAP S3: POHON RISET & HEADHUNTING STAF (Tab RISET & FIRMA)
1. Terapkan tabel `StaffHeadhuntingMarket` dan `ResearchTechTreeProgress` pada SQLite lokal.
2. Bangun pohon riset teknologi finansial (3 jalur tier R&D) dengan efek pasif nyata.
3. Bangun bursa pembajakan staf analis (Headhunting) dan sistem pemantauan loyalitas staf.

#### TAHAP S4: DISIPLIN HARIAN, SHOCK PASAR & FINALISASI (Tab BERANDA & PASAR)
1. Terapkan tabel `DailyStreakTracker` dan `SectorDominanceStatus` pada SQLite lokal.
2. Bangun jalur Streak harian dengan notifikasi *Broken Streak* saat absen.
3. Bangun pemicu event Black Swan Market Shock dan krisis pasokan sektor industri.
4. Bangun algoritma evaluasi dominasi sektor jika kepemilikan pemain > 25%.
5. Lakukan pengujian end-to-end menyeluruh pada 2D Portrait Android: verifikasi fluiditas sentuhan ($\ge 48\text{ dp}$), performa database lokal, dan build APK Android.

---

### FORMAT LAPORAN KEMAJUAN (SETIAP SELESAI SUBTASK/TAHAP):
Laporankan secara ringkas dan faktual:
- **Progres:** X dari 4 Tahap Strategis Selesai (Fitur Aktif: ...)
- **Keluaran Nyata:** Berkas yang dibuat/dimodifikasi (sebutkan path absolutnya).
- **Pemeriksaan & Bukti:** Perintah uji yang dijalankan beserta hasil output riil (exit code 0 / PASS).
- **Hambatan:** Catat kendala teknis nyata atau nyatakan "Tidak ada".
- **Langkah Berikutnya:** Tugas terdekat yang akan langsung dikerjakan.

---

MULAI SEKARANG DARI TAHAP S1: Terapkan tabel deposito dan asuransi, bangun mesin perhitungan bunga deposito berjangka, dan pasang penalti penarikan dini. Jalankan eksekusi secara mandiri sampai tuntas!
