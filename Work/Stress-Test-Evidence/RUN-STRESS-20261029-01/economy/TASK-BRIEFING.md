# BRIEFING PENUGASAN SUB-AGENT A: EKONOMI & TRANSAKSI

**ID Agen:** `AGENT-SUB-EKONOMI`  
**Run ID:** `RUN-STRESS-20261029-01`  
**Direktori Kerja Eksklusif:** `Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/economy/`  
**Cakupan Tanggung Jawab:**
1. Pengujian batas saldo kas IDR & USD (tidak ada saldo negatif tanpa fasilitas pinjaman sah).
2. Mekanisme All-Or-None (AON) pesanan pemain: 0 partial fill pada kombinasi multi-lawan.
3. Alokasi kuota IPO 60:40 metode pecahan terbesar (Hamilton) dan refund penuh saat undersubscribed/oversubscribed.
4. Operasional dividen, aksi korporasi stock split/reverse split, dan pemisahan kas anak usaha dari kas pribadi.
5. Eksekusi rangkaian Level L1 (100 seed × 1.000 aksi) dan L2 (1.000 seed × 1.000 aksi) transaksi ekonomi.
6. Rekonsiliasi buku besar (`EconomyLedger.reconcile(acc, cur) == true`) pada setiap akhir siklus.

**Larangan:** Dilarang menyentuh file save pemain asli atau mengubah source produk di luar direktori kerja.
