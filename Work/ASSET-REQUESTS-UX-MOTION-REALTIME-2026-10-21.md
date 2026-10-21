# Inventaris Permintaan Aset Ilustrasi untuk Codex (Imagegen)
**Tanggal Permintaan:** 21 Oktober 2026, WIB  
**Status Keseluruhan:** **`ASSETS_PENDING_CODEX`**  
**Penyedia Aset Terpilih:** Codex (melalui skill `imagegen`)  
**Agen Integrator & UI Runtime:** Bengkel (Hermes Agent)  
**Format Teknis Target:** File PNG lokal terkompresi, transparansi alpha bersih (cutout/transparent background), resolusi target 512×512 piksel (skala tampil 64–120 dp), rasio aspek 1:1, palet gelap Neo-Cyber Glassmorphism dengan aksen Solana Neon (#9945FF, #14F195, #FFB800).

---

## 1. Panduan Gaya Bersama (Shared Style Guidelines)
- **Tema Visual:** *Dark Neo-Cyber Luxury & Tycoon Realism*. Memadukan objek fisik nyata berkualitas tinggi (mobil sport eksotis, jam tangan haute horlogerie, properti arsitektur kontemporer) dengan pencahayaan neon studio aksen cyan-teal, amber, dan ungu-violet halus.
- **Latar Belakang:** Transparan murni (*transparent cutout / alpha channel*) agar menyatu langsung dengan kartu glassmorphism aplikasi game.
- **Karakter Non-Teks:** Gambar **tidak boleh** memuat teks harga, angka saldo, label mata uang, atau watermark, karena seluruh data finansial dirender secara dinamis melalui HTML/CSS.

---

## 2. Daftar Kebutuhan Aset Koleksi Mewah & Properti (12 Item)

| ID Aset | Objek / Nama Item | Slot & Penempatan Layar | Dimensi Tampil | Prioritas | Status Integrasi | Prompt Visual Siap Pakai Codex |
|---|---|---|---|---|---|---|
| `ASSET-COL-CAR-00` | Vespa Klasik 1965 Restorasi Mint | Tab Bisnis & Galeri Koleksi (`#firma-view`, Modal Agunan Pinjaman) | 1:1 (72×72 dp) | P1 | `ASSETS_PENDING_CODEX` | *Isometric 3D render of a vintage 1965 Italian Vespa scooter, mint seafoam green color, polished chrome accents, retro leather saddle, isolated on pure transparent background, subtle neon teal studio rim light, high-end mobile game asset, clean cutout, 8k octane render.* |
| `ASSET-COL-CAR-01` | Porsche 911 GT3 RS | Tab Bisnis & Galeri Koleksi (`#firma-view`, Modal Agunan Pinjaman) | 1:1 (72×72 dp) | P1 | `ASSETS_PENDING_CODEX` | *Isometric 3D render of a Porsche 911 GT3 RS sports car, sleek metallic silver with carbon fiber aero wing and cyan brake calipers, studio rim lighting, isolated on transparent background, sharp edges, modern luxury vehicle asset, clean alpha.* |
| `ASSET-COL-CAR-02` | Ferrari 296 GTB | Tab Bisnis & Galeri Koleksi (`#firma-view`, Modal Agunan Pinjaman) | 1:1 (72×72 dp) | P1 | `ASSETS_PENDING_CODEX` | *Isometric 3D render of a modern Ferrari 296 GTB hybrid supercar, signature Rosso Corsa metallic red, aerodynamic sculpted curves, dark titanium wheels, isolated on transparent background, dramatic luxury automotive studio lighting, clean cutout.* |
| `ASSET-COL-PROP-00` | Ruko Komersial BSD City | Tab Bisnis & Galeri Koleksi (`#firma-view`, Detail Valuasi) | 1:1 (80×80 dp) | P2 | `ASSETS_PENDING_CODEX` | *Isometric 3D architectural miniature of a modern 3-story urban commercial shophouse (ruko), clean glass facade, warm interior lighting, minimalist tropical modern design, isolated on transparent background, sharp game building icon.* |
| `ASSET-COL-PROP-01` | Penthouse SCBD Mega Kuningan | Tab Bisnis & Galeri Koleksi (`#firma-view`, Detail Valuasi) | 1:1 (80×80 dp) | P2 | `ASSETS_PENDING_CODEX` | *Isometric 3D render of a luxurious high-rise skyscraper penthouse level, floor-to-ceiling illuminated panoramic glass windows, rooftop infinity edge garden, sleek dark obsidian and gold metal trims, isolated on transparent background.* |
| `ASSET-COL-PROP-02` | Villa Tebing Uluwatu Bali | Tab Bisnis & Galeri Koleksi (`#firma-view`, Detail Valuasi) | 1:1 (80×80 dp) | P2 | `ASSETS_PENDING_CODEX` | *Isometric 3D architectural render of a cliffside ultra-luxury resort villa, teak wood deck, infinity pool overlooking sheer limestone cliff, ambient sunset warm amber and purple glow, isolated on transparent background.* |
| `ASSET-COL-JEW-01` | Cincin Safir Biru Ceylon | Tab Bisnis & Galeri Koleksi (`#firma-view`) | 1:1 (64×64 dp) | P2 | `ASSETS_PENDING_CODEX` | *Detailed 3D macro close-up of a royal Ceylon blue sapphire gemstone ring, cushion-cut deep blue sapphire encircled by sparkling micropavé diamonds on platinum band, isolated on transparent background, brilliant gemstone caustics, clean cutout.* |
| `ASSET-COL-JEW-02` | Kalung Berlian Solitaire Tiffany | Tab Bisnis & Galeri Koleksi (`#firma-view`) | 1:1 (64×64 dp) | P2 | `ASSETS_PENDING_CODEX` | *3D render of an exquisite solitaire brilliant-cut diamond pendant necklace, delicate white gold chain, hyper-realistic diamond facets reflecting rainbow spectrum light, isolated on transparent background, luxury jewelry game asset.* |
| `ASSET-COL-JEW-03` | Tiara Berlian Kerajaan Langka | Tab Bisnis & Galeri Koleksi (`#firma-view`) | 1:1 (64×64 dp) | P2 | `ASSETS_PENDING_CODEX` | *3D render of an ornate royal heirloom diamond tiara, intricate filigree platinum crown studded with teardrop diamonds, soft ethereal purple and gold rim lighting, isolated on transparent background, royal prestigious jewelry item.* |
| `ASSET-COL-ART-00` | Rolex Submariner Vintage 1970 | Tab Bisnis & Galeri Koleksi (`#firma-view`) | 1:1 (64×64 dp) | P1 | `ASSETS_PENDING_CODEX` | *Front-facing 3D render of a luxury vintage 1970 stainless steel diving watch, ceramic black rotating bezel, luminous patina dial markers, steel oyster bracelet, isolated on transparent background, luxury watchmaking asset, clean edges.* |
| `ASSET-COL-ART-01` | Patek Philippe Grandmaster Chime | Tab Bisnis & Galeri Koleksi (`#firma-view`) | 1:1 (64×64 dp) | P1 | `ASSETS_PENDING_CODEX` | *3D render of an ultra-complicated haute horlogerie wristwatch with hand-engraved white gold reversible case, complex dial with multiple astronomical sub-dials, alligator leather strap, isolated on transparent background.* |
| `ASSET-COL-ART-02` | Lukisan Bersejarah Raden Saleh | Tab Bisnis & Galeri Koleksi (`#firma-view`) | 1:1 (64×64 dp) | P2 | `ASSETS_PENDING_CODEX` | *3D render of a museum-grade framed historical oil painting in ornate vintage gilded baroque gold frame, dramatic romanticism masterpiece canvas, isolated on transparent background, priceless art collection asset.* |

---

## 3. Daftar Kebutuhan Aset Sektor Bisnis & Organisasi (7 Item)

| ID Aset | Objek / Sektor | Slot & Penempatan Layar | Dimensi Tampil | Prioritas | Status Integrasi | Prompt Visual Siap Pakai Codex |
|---|---|---|---|---|---|---|
| `ASSET-BIZ-MANUFAKTUR` | Kompleks Pabrik Manufaktur | Tab Bisnis (`#view-firma`, Kartu Perusahaan Sektor Manufaktur) | 16:9 / 1:1 (100×100 dp) | P1 | `ASSETS_PENDING_CODEX` | *Isometric 3D smart manufacturing facility and robotic assembly plant, clean automated warehouse, conveyor tracks, solar panel roof, glowing cyan indicator lights, clean industrial cyberpunk aesthetic, isolated on transparent background.* |
| `ASSET-BIZ-RITEL` | Toko Flagship Ritel Modern | Tab Bisnis (`#view-firma`, Kartu Perusahaan Sektor Ritel) | 16:9 / 1:1 (100×100 dp) | P1 | `ASSETS_PENDING_CODEX` | *Isometric 3D modern retail flagship store, large luminous glass storefront, digital interactive displays, customer promenade, sleek neon accent canopy, isolated on transparent background, commercial tycoon asset.* |
| `ASSET-BIZ-JASA` | Kantor Agensi Teknologi & Finansial | Tab Bisnis (`#view-firma`, Kartu Perusahaan Sektor Jasa) | 16:9 / 1:1 (100×100 dp) | P1 | `ASSETS_PENDING_CODEX` | *Isometric 3D modern tech hub office and fintech laboratory, multi-tiered glass workspace, holographic server racks, interior greenery, neon purple edge lighting, isolated on transparent background, modern consulting firm icon.* |
| `ASSET-STAFF-COO` | Avatar Direktur Operasional (COO) | Tab Bisnis (Modal Pegawai & Organisasi) | 1:1 (48×48 dp) | P2 | `ASSETS_PENDING_CODEX` | *Stylized 3D portrait avatar of a confident professional female corporate executive in sharp navy blazer, sleek hair, subtle holographic earpiece, modern fintech game character portrait, circular composition, isolated on transparent background.* |
| `ASSET-STAFF-PROD` | Avatar Manajer Produksi | Tab Bisnis (Modal Pegawai & Organisasi) | 1:1 (48×48 dp) | P2 | `ASSETS_PENDING_CODEX` | *Stylized 3D portrait avatar of a focused industrial production manager in high-tech smart work attire, holding digital tablet stylus, clean lighting, character game icon on transparent background.* |
| `ASSET-STAFF-SALES` | Avatar Kepala Penjualan (Head of Sales) | Tab Bisnis (Modal Pegawai & Organisasi) | 1:1 (48×48 dp) | P2 | `ASSETS_PENDING_CODEX` | *Stylized 3D portrait avatar of an energetic male sales lead in stylish tailored suit, warm welcoming smile, confident posture, modern fintech tycoon character icon on transparent background.* |
| `ASSET-STAFF-FIN` | Avatar Analis Keuangan Senior | Tab Bisnis (Modal Pegawai & Organisasi) | 1:1 (48×48 dp) | P2 | `ASSETS_PENDING_CODEX` | *Stylized 3D portrait avatar of an analytical financial quantitative specialist wearing smart glasses, dark turtleneck, thoughtful expression, clean studio portrait icon on transparent background.* |

---

## 4. Kriteria Keberterimaan (Acceptance Criteria)
1. **Transparansi Alpha:** Semua aset PNG harus memiliki latar belakang transparan murni tanpa halo putih (*white fringe*) atau artefak hitam pekat.
2. **Kesesuaian Skala:** Detail objek harus tetap tajam dan terbaca jelas ketika di-downscale ke 48–80 dp pada layar ponsel 320–430 piksel.
3. **Pemisahan Logika:** Seluruh label, nama, harga, dan level di-render oleh DOM HTML (`app.js`), sehingga tidak ada teks statis tertanam dalam gambar.
4. **Penyimpanan Lokal:** Setelah Codex menghasilkan gambar, aset akan ditempatkan pada direktori lokal `android/app/src/main/assets/www/assets/images/` tanpa dependensi jaringan eksternal.
