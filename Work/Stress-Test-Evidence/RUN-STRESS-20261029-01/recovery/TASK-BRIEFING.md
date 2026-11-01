# BRIEFING PENUGASAN SUB-AGENT C: PERSISTENSI & RECOVERY

**ID Agen:** `AGENT-SUB-RECOVERY`  
**Run ID:** `RUN-STRESS-20261029-01`  
**Direktori Kerja Eksklusif:** `Work/Stress-Test-Evidence/RUN-STRESS-20261029-01/recovery/`  
**Cakupan Tanggung Jawab:**
1. Fault injection terkendali pada titik transaksi (short write, file terpotong, korupsi JSON, disk error).
2. Matriks pemulihan: Primary [valid, invalid, missing] × Backup [valid, invalid, missing].
3. Verifikasi penawaran dialog pemulihan cadangan (`pendingBackupRecoveryOffer` dengan timestamp & risiko riil).
4. Pengujian 1.000 siklus save–reload pada profil kecil, menengah, dan besar.
5. Verifikasi penghentian auto-renewal: akses Pro/Whale tetap aktif hingga hari ke-30 dan turun Basic hari ke-31 tanpa debit lanjutan.
6. Verifikasi proteksi last-good backup (file primer yang rusak tidak menimpa cadangan yang valid).

**Larangan:** Dilarang menyentuh save pemain asli `save_game_v2.json`. Seluruh uji wajib di direktori QA terisolasi.
