# BRIEFING PENUGASAN SUB-AGENT B: WAKTU & SIMULASI

**ID Agen:** `AGENT-SUB-SIMULASI`  
**Run ID:** `RUN-STRESS-20261029-01`  
**Direktori Kerja Eksklusif:** `Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/simulation/`  
**Cakupan Tanggung Jawab:**
1. Pengujian simulasi waktu bursa 24× (09:00–16:00 WIB buka, sore/malam tutup).
2. Penegakan batas harian 25% (ceiling/floor) seragam di seluruh saham IDX tanpa pengecualian.
3. Penjadwalan 2 berita Pro per hari bursa (2–5%), Whale 1–2 per 7 hari (>20%), dan rumor lanjutan 14 hari (2x persentase awal).
4. Penanganan antrean konflik berita bertentangan pada emiten yang sama (status `WAITING_PREVIOUS_TARGET` sampai target pertama selesai).
5. Simulasi jangka panjang Level L4 (20 seed × 365 hari bursa) dan L5 (5 seed × 3.650 hari bursa).
6. Beban 1.000 NPC: pembaruan portofolio riil, evaluasi return mingguan vs kekayaan, dan sort leaderboard.

**Larangan:** Dilarang menyentuh save pemain asli atau merusak clock sistem operasi Windows.
