"""
Generates verified company profile data for all 100 instruments + in-game IPO (IDX:NCLD).
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

PROFILES = {
    # --- IDX Keuangan ---
    'IDX:BBCA': {
        'name': 'PT Bank Central Asia Tbk',
        'ticker': 'BBCA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Central Asia adalah bank swasta terbesar di Indonesia yang menyediakan solusi perbankan komprehensif untuk nasabah korporasi, komersial, UKM, dan konsumer. Dikenal luas dengan keunggulan transaksi digital dan basis pendanaan murah (CASA) yang kokoh.',
        'website': 'https://www.bca.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations BCA', 'verifiedDate': '2026-10-22'
    },
    'IDX:BMRI': {
        'name': 'PT Bank Mandiri (Persero) Tbk',
        'ticker': 'BMRI', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Mandiri adalah salah satu bank milik negara (BUMN) terbesar di Indonesia dengan portofolio pinjaman korporasi dan komersial yang luas. Mengoperasikan ekosistem perbankan digital terintegrasi melalui super-app Livin dan Kopra.',
        'website': 'https://www.bankmandiri.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan Bank Mandiri', 'verifiedDate': '2026-10-22'
    },
    'IDX:BBRI': {
        'name': 'PT Bank Rakyat Indonesia (Persero) Tbk',
        'ticker': 'BBRI', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Rakyat Indonesia merupakan bank BUMN tertua dan pemimpin perbankan mikro di Indonesia. Memiliki jaringan unit kerja dan agen laku pandai (AgenBRILink) paling merata hingga pelosok nusantara.',
        'website': 'https://www.bri.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan BRI', 'verifiedDate': '2026-10-22'
    },
    'IDX:BBNI': {
        'name': 'PT Bank Negara Indonesia (Persero) Tbk',
        'ticker': 'BBNI', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Negara Indonesia berfokus pada pembiayaan korporasi serta diaspora dan transaksi perdagangan internasional. Memiliki jaringan kantor cabang luar negeri di berbagai pusat keuangan global.',
        'website': 'https://www.bni.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations BNI', 'verifiedDate': '2026-10-22'
    },
    'IDX:ARTO': {
        'name': 'PT Bank Jago Tbk',
        'ticker': 'ARTO', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Jago adalah bank berbasis teknologi (digital bank) yang mengintegrasikan layanan keuangan langsung ke ekosistem digital dan gaya hidup harian nasabah.',
        'website': 'https://www.jago.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Bank Jago', 'verifiedDate': '2026-10-22'
    },
    'IDX:BANK': {
        'name': 'PT Bank Aladin Syariah Tbk',
        'ticker': 'BANK', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Aladin Syariah merupakan bank digital syariah murni yang melayani segmen retail dan UMKM dengan prinsip-prinsip syariah terpadu dan kemitraan omnichannel.',
        'website': 'https://aladinbank.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Resmi Aladin', 'verifiedDate': '2026-10-22'
    },
    'IDX:BBHI': {
        'name': 'PT Allo Bank Indonesia Tbk',
        'ticker': 'BBHI', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Allo Bank Indonesia bergerak dalam penyediaan layanan perbankan digital all-in-one yang terhubung dengan ekosistem ritel, media, dan gaya hidup CT Corp.',
        'website': 'https://www.allobank.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Allo Bank', 'verifiedDate': '2026-10-22'
    },
    'IDX:BBTN': {
        'name': 'PT Bank Tabungan Negara (Persero) Tbk',
        'ticker': 'BBTN', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Tabungan Negara adalah bank BUMN dengan keunggulan utama pada pembiayaan perumahan dan Kredit Pemilikan Rumah (KPR) subsidi maupun nonsubsidi di Indonesia.',
        'website': 'https://www.btn.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan BTN', 'verifiedDate': '2026-10-22'
    },
    'IDX:BFIN': {
        'name': 'PT BFI Finance Indonesia Tbk',
        'ticker': 'BFIN', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'BFI Finance adalah salah satu perusahaan pembiayaan konsumen independen terbesar di Indonesia yang melayani pembiayaan kendaraan bermotor dan alat berat.',
        'website': 'https://www.bfi.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations BFI', 'verifiedDate': '2026-10-22'
    },
    'IDX:BRIS': {
        'name': 'PT Bank Syariah Indonesia Tbk',
        'ticker': 'BRIS', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'Bank Syariah Indonesia merupakan bank syariah terbesar di Indonesia hasil penggabungan tiga bank syariah BUMN, melayani transaksi syariah komersial dan ritel berskala global.',
        'website': 'https://www.bankbsi.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan BSI', 'verifiedDate': '2026-10-22'
    },
    'IDX:BTPS': {
        'name': 'PT Bank BTPN Syariah Tbk',
        'ticker': 'BTPS', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Keuangan', 'currency': 'IDR',
        'description': 'BTPN Syariah berfokus melayani pemberdayaan nasabah inklusi prasejahtera produktif, khususnya kelompok perempuan pengusaha ultra mikro di pelosok pedesaan.',
        'website': 'https://www.btpnsyariah.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations BTPN Syariah', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Infrastruktur & Telekomunikasi ---
    'IDX:TLKM': {
        'name': 'PT Telkom Indonesia (Persero) Tbk',
        'ticker': 'TLKM', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Infrastruktur & Telekomunikasi', 'currency': 'IDR',
        'description': 'Telkom Indonesia adalah konglomerasi telekomunikasi dan jaringan digital BUMN terbesar di Indonesia dengan anak usaha seluler Telkomsel serta jaringan fiber optik nasional.',
        'website': 'https://www.telkom.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Telkom', 'verifiedDate': '2026-10-22'
    },
    'IDX:EXCL': {
        'name': 'PT XL Axiata Tbk',
        'ticker': 'EXCL', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Infrastruktur & Telekomunikasi', 'currency': 'IDR',
        'description': 'XL Axiata adalah penyedia jasa telekomunikasi dan jaringan internet seluler serta data broadband terkemuka yang melayani jutaan pelanggan di seluruh wilayah Indonesia.',
        'website': 'https://www.xl.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan XL Axiata', 'verifiedDate': '2026-10-22'
    },
    'IDX:JSMR': {
        'name': 'PT Jasa Marga (Persero) Tbk',
        'ticker': 'JSMR', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Infrastruktur & Telekomunikasi', 'currency': 'IDR',
        'description': 'Jasa Marga adalah pengembang dan operator jalan tol pertama dan terbesar di Indonesia yang mengelola mayoritas konsesi jaringan jalan tol Trans-Jawa dan sekitarnya.',
        'website': 'https://www.jasamarga.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan Jasa Marga', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Industri ---
    'IDX:ASII': {
        'name': 'PT Astra International Tbk',
        'ticker': 'ASII', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Industri', 'currency': 'IDR',
        'description': 'Astra International adalah grup konglomerasi diversifikasi terbesar di Indonesia dengan lini bisnis utama otomotif, jasa keuangan, alat berat, agribisnis, infrastruktur, dan teknologi informasi.',
        'website': 'https://www.astra.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Astra', 'verifiedDate': '2026-10-22'
    },
    'IDX:UNTR': {
        'name': 'PT United Tractors Tbk',
        'ticker': 'UNTR', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Industri', 'currency': 'IDR',
        'description': 'United Tractors adalah distributor alat berat terbesar di Indonesia (merek Komatsu) serta kontraktor penambangan batu bara dan mineral terkemuka melalui PT Pamapersada Nusantara.',
        'website': 'https://www.unitedtractors.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan UNTR', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Konsumen Primer ---
    'IDX:UNVR': {
        'name': 'PT Unilever Indonesia Tbk',
        'ticker': 'UNVR', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Unilever Indonesia memproduksi dan mendistribusikan barang kebutuhan konsumen harian (FMCG) terkemuka, mencakup produk perawatan rumah, perawatan tubuh, serta makanan dan minuman.',
        'website': 'https://www.unilever.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Unilever', 'verifiedDate': '2026-10-22'
    },
    'IDX:ICBP': {
        'name': 'PT Indofood CBP Sukses Makmur Tbk',
        'ticker': 'ICBP', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Indofood CBP adalah produsen makanan kemasan bermerek terkemuka dengan merek mi instan global Indomie, produk susu, makanan ringan, penyedap rasa, dan nutrisi khusus.',
        'website': 'https://www.indofoodcbp.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations ICBP', 'verifiedDate': '2026-10-22'
    },
    'IDX:INDF': {
        'name': 'PT Indofood Sukses Makmur Tbk',
        'ticker': 'INDF', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Indofood Sukses Makmur adalah perusahaan induk solusi pangan total terintegrasi dari pengolahan gandum (Bogasari), agribisnis perkebunan, hingga distribusi produk konsumen.',
        'website': 'https://www.indofood.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Indofood', 'verifiedDate': '2026-10-22'
    },
    'IDX:AMRT': {
        'name': 'PT Sumber Alfaria Trijaya Tbk',
        'ticker': 'AMRT', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Sumber Alfaria Trijaya mengelola jaringan minimarket ritel Alfamart dengan puluhan ribu gerai modern yang menyediakan kebutuhan harian konsumen di berbagai pelosok Indonesia.',
        'website': 'https://alfamart.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan Alfamart', 'verifiedDate': '2026-10-22'
    },
    'IDX:MYOR': {
        'name': 'PT Mayora Indah Tbk',
        'ticker': 'MYOR', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Mayora Indah adalah produsen makanan olahan dan minuman terkemuka yang memproduksi biskuit, permen, wafer, cokelat, dan kopi olahan yang diekspor ke puluhan negara.',
        'website': 'https://www.mayoraindah.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Mayora', 'verifiedDate': '2026-10-22'
    },
    'IDX:CPIN': {
        'name': 'PT Charoen Pokphand Indonesia Tbk',
        'ticker': 'CPIN', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Charoen Pokphand Indonesia merupakan produsen pakan ternak, pengembangbiakan anak ayam ras (DOC), dan pengolahan daging ayam terpadu (merek Fiesta).',
        'website': 'https://cp.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations CPIN', 'verifiedDate': '2026-10-22'
    },
    'IDX:JPFA': {
        'name': 'PT Japfa Comfeed Indonesia Tbk',
        'ticker': 'JPFA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Japfa Comfeed bergerak dalam bidang agrifood terintegrasi yang memproduksi pakan ternak, pembibitan ayam, budidaya perairan, dan produk protein hewani bermerek.',
        'website': 'https://www.japfacomfeed.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Japfa', 'verifiedDate': '2026-10-22'
    },
    'IDX:AALI': {
        'name': 'PT Astra Agro Lestari Tbk',
        'ticker': 'AALI', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'Astra Agro Lestari adalah anak usaha Astra yang bergerak di bidang perkebunan kelapa sawit dan pengolahan minyak kelapa sawit mentah (CPO) berkelanjutan.',
        'website': 'https://www.astra-agro.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations AALI', 'verifiedDate': '2026-10-22'
    },
    'IDX:HMSP': {
        'name': 'PT Hanjaya Mandala Sampoerna Tbk',
        'ticker': 'HMSP', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Primer', 'currency': 'IDR',
        'description': 'HM Sampoerna adalah salah satu produsen rokok kretek terkemuka di Indonesia, terafiliasi dengan Philip Morris International, memproduksi Dji Sam Soe dan Sampoerna A.',
        'website': 'https://www.sampoerna.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan Sampoerna', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Konsumen Nonprimer ---
    'IDX:MAPI': {
        'name': 'PT Mitra Adiperkasa Tbk',
        'ticker': 'MAPI', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Nonprimer', 'currency': 'IDR',
        'description': 'Mitra Adiperkasa adalah peritel gaya hidup terdepan di Indonesia yang mengelola hak waralaba merek-merek ritel olahraga, busana, anak, dan gerai gaya hidup global ternama.',
        'website': 'https://www.map.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations MAP', 'verifiedDate': '2026-10-22'
    },
    'IDX:ERAA': {
        'name': 'PT Erajaya Swasembada Tbk',
        'ticker': 'ERAA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Konsumen Nonprimer', 'currency': 'IDR',
        'description': 'Erajaya Swasembada adalah importir, distributor, dan pengecer perangkat telekomunikasi seluler, gawai pintar, dan produk elektronik konsumen terbesar di Indonesia.',
        'website': 'https://www.erajaya.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Erajaya', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Teknologi ---
    'IDX:GOTO': {
        'name': 'PT GoTo Gojek Tokopedia Tbk',
        'ticker': 'GOTO', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Teknologi', 'currency': 'IDR',
        'description': 'GoTo adalah ekosistem digital terintegrasi terkemuka di Indonesia yang menggabungkan layanan on-demand transportasi/pengantaran (Gojek) dan teknologi finansial (GoTo Financial).',
        'website': 'https://www.gotocompany.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations GoTo', 'verifiedDate': '2026-10-22'
    },
    'IDX:EMTK': {
        'name': 'PT Elang Mahkota Teknologi Tbk',
        'ticker': 'EMTK', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Teknologi', 'currency': 'IDR',
        'description': 'Elang Mahkota Teknologi (Emtek) adalah grup teknologi dan media terintegrasi yang memiliki stasiun televisi nasional (SCTV, Indosiar), platform OTT Vidio, dan ekosistem digital.',
        'website': 'https://www.emtek.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Emtek', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Bahan Baku ---
    'IDX:ANTM': {
        'name': 'PT Aneka Tambang Tbk',
        'ticker': 'ANTM', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Aneka Tambang (Antam) adalah anggota holding BUMN MIND ID yang bergerak di bidang pertambangan dan pengolahan nikel, emas, perak, dan bauksit terintegrasi secara vertikal.',
        'website': 'https://www.antam.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Resmi Antam', 'verifiedDate': '2026-10-22'
    },
    'IDX:BRPT': {
        'name': 'PT Barito Pacific Tbk',
        'ticker': 'BRPT', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Barito Pacific adalah perusahaan energi terintegrasi dan petrokimia terkemuka di Indonesia, mengendalikan produsen petrokimia Chandra Asri dan aset panas bumi Barito Renewables.',
        'website': 'https://www.barito-pacific.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Barito', 'verifiedDate': '2026-10-22'
    },
    'IDX:INCO': {
        'name': 'PT Vale Indonesia Tbk',
        'ticker': 'INCO', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Vale Indonesia adalah produsen nikel dalam matte terkemuka yang mengoperasikan tambang nikel laterit dan pabrik peleburan ramah lingkungan di Sorowako, Sulawesi.',
        'website': 'https://vale.com/indonesia', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Vale', 'verifiedDate': '2026-10-22'
    },
    'IDX:INKP': {
        'name': 'PT Indah Kiat Pulp and Paper Tbk',
        'ticker': 'INKP', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Indah Kiat Pulp & Paper adalah salah satu produsen pulp, kertas budaya, dan kertas kemasan industri terbesar di dunia di bawah naungan grup Asia Pulp & Paper (APP).',
        'website': 'https://www.asiapulppaper.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan Indah Kiat', 'verifiedDate': '2026-10-22'
    },
    'IDX:INTP': {
        'name': 'PT Indocement Tunggal Prakarsa Tbk',
        'ticker': 'INTP', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Indocement Tunggal Prakarsa adalah produsen semen berkualitas terkemuka dengan merek Semen Tiga Roda serta beton siap pakai dan agregat bahan bangunan terpercaya.',
        'website': 'https://www.indocement.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Indocement', 'verifiedDate': '2026-10-22'
    },
    'IDX:MDKA': {
        'name': 'PT Merdeka Copper Gold Tbk',
        'ticker': 'MDKA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Merdeka Copper Gold adalah perusahaan pertambangan logam terkemuka di Indonesia yang mengelola tambang emas Tujuh Bukit, tambang tembaga Wetar, dan proyek nikel AIM.',
        'website': 'https://merdekacoppergold.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Merdeka', 'verifiedDate': '2026-10-22'
    },
    'IDX:SMGR': {
        'name': 'PT Semen Indonesia (Persero) Tbk',
        'ticker': 'SMGR', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Bahan Baku', 'currency': 'IDR',
        'description': 'Semen Indonesia (SIG) adalah holding BUMN produsen bahan bangunan dan semen terbesar di Asia Tenggara dengan fasilitas produksi dan distribusi di seluruh Indonesia dan Vietnam.',
        'website': 'https://www.sig.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations SIG', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Energi ---
    'IDX:ADRO': {
        'name': 'PT Alamtri Resources Indonesia Tbk',
        'ticker': 'ADRO', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'Alamtri Resources (sebelumnya Adaro Energy) adalah produsen batu bara termal dan metalurgi terintegrasi terkemuka serta pengembang pembangkit listrik dan energi bersih di Indonesia.',
        'website': 'https://www.adaro.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Adaro', 'verifiedDate': '2026-10-22'
    },
    'IDX:PGAS': {
        'name': 'PT Perusahaan Gas Negara Tbk',
        'ticker': 'PGAS', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'Perusahaan Gas Negara (PGN) adalah subholding gas Pertamina yang memimpin infrastruktur transmisi dan distribusi gas bumi nasional untuk sektor industri, komersial, dan rumah tangga.',
        'website': 'https://pgn.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan PGN', 'verifiedDate': '2026-10-22'
    },
    'IDX:AKRA': {
        'name': 'PT AKR Corporindo Tbk',
        'ticker': 'AKRA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'AKR Corporindo adalah penyedia infrastruktur logistik dan rantai pasok terintegrasi untuk perdagangan bahan bakar minyak dan bahan kimia dasar, serta pengembang kawasan industri terpadu JIIPE Gresik.',
        'website': 'https://www.akr.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations AKR', 'verifiedDate': '2026-10-22'
    },
    'IDX:ITMG': {
        'name': 'PT Indo Tambangraya Megah Tbk',
        'ticker': 'ITMG', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'Indo Tambangraya Megah adalah produsen batu bara berkualitas tinggi dengan kandungan kalori ramah lingkungan, mengoperasikan sejumlah tambang di Kalimantan Timur dan Selatan.',
        'website': 'https://www.itmg.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan ITMG', 'verifiedDate': '2026-10-22'
    },
    'IDX:MEDC': {
        'name': 'PT Medco Energi Internasional Tbk',
        'ticker': 'MEDC', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'Medco Energi Internasional adalah perusahaan energi dan sumber daya alam independen terkemuka dengan portofolio eksplorasi minyak dan gas bumi serta pembangkitan listrik panas bumi dan surya.',
        'website': 'https://www.medcoenergi.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Medco', 'verifiedDate': '2026-10-22'
    },
    'IDX:PGEO': {
        'name': 'PT Pertamina Geothermal Energy Tbk',
        'ticker': 'PGEO', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'Pertamina Geothermal Energy mengelola wilayah kerja panas bumi terbesar di Indonesia dengan kapasitas terpasang uap dan listrik yang menjadi tulang punggung transisi energi hijau nasional.',
        'website': 'https://pge.pertamina.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan PGE', 'verifiedDate': '2026-10-22'
    },
    'IDX:PTBA': {
        'name': 'PT Bukit Asam (Persero) Tbk',
        'ticker': 'PTBA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Energi', 'currency': 'IDR',
        'description': 'Bukit Asam adalah produsen batu bara BUMN anggota grup MIND ID yang mengelola tambang Tanjung Enim dan fasilitas logistik kereta api pelabuhan di Sumatera Selatan.',
        'website': 'https://www.ptba.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Bukit Asam', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Kesehatan ---
    'IDX:KLBF': {
        'name': 'PT Kalbe Farma Tbk',
        'ticker': 'KLBF', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Kesehatan', 'currency': 'IDR',
        'description': 'Kalbe Farma adalah perusahaan farmasi dan produk kesehatan terbesar di Asia Tenggara yang memproduksi obat resep, produk kesehatan konsumen, nutrisi, dan layanan rantai pasok distribusi kesehatan.',
        'website': 'https://www.kalbe.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Kalbe', 'verifiedDate': '2026-10-22'
    },
    'IDX:HEAL': {
        'name': 'PT Medikaloka Hermina Tbk',
        'ticker': 'HEAL', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Kesehatan', 'currency': 'IDR',
        'description': 'Medikaloka Hermina mengelola jaringan Rumah Sakit Hermina yang berfokus pada perawatan ibu dan anak serta pelayanan kesehatan umum komprehensif di puluhan kota di Indonesia.',
        'website': 'https://www.herminahospitals.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Hermina', 'verifiedDate': '2026-10-22'
    },
    'IDX:MIKA': {
        'name': 'PT Mitra Keluarga Karyasehat Tbk',
        'ticker': 'MIKA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Kesehatan', 'currency': 'IDR',
        'description': 'Mitra Keluarga Karyasehat mengoperasikan jaringan rumah sakit swasta modern terkemuka di wilayah Jabodetabek dan Jawa Timur dengan layanan spesialisasi medis berstandar tinggi.',
        'website': 'https://mitrakeluarga.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations MIKA', 'verifiedDate': '2026-10-22'
    },
    'IDX:SIDO': {
        'name': 'PT Industri Jamu dan Farmasi Sido Muncul Tbk',
        'ticker': 'SIDO', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Kesehatan', 'currency': 'IDR',
        'description': 'Sido Muncul adalah produsen jamu herbal dan suplemen farmasi terkemuka di Indonesia, terkenal dengan produk unggulan Tolak Angin dan Kuku Bima Energi.',
        'website': 'https://sidomuncul.co.id', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Keuangan Sido Muncul', 'verifiedDate': '2026-10-22'
    },

    # --- IDX Properti & Real Estat ---
    'IDX:BSDE': {
        'name': 'PT Bumi Serpong Damai Tbk',
        'ticker': 'BSDE', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Properti & Real Estat', 'currency': 'IDR',
        'description': 'Bumi Serpong Damai adalah pengembang kota mandiri terkemuka di bawah Sinar Mas Land, mengelola kawasan residensial, komersial, dan pusat bisnis terpadu BSD City di Tangerang.',
        'website': 'https://bsdcity.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations BSDE', 'verifiedDate': '2026-10-22'
    },
    'IDX:CTRA': {
        'name': 'PT Ciputra Development Tbk',
        'ticker': 'CTRA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Properti & Real Estat', 'currency': 'IDR',
        'description': 'Ciputra Development adalah pelopor pengembang perumahan skala kota mandiri dan proyek properti komersial bernilai tinggi yang tersebar di puluhan kota di seluruh Indonesia.',
        'website': 'https://www.ciputradevelopment.com', 'source': 'Bursa Efek Indonesia (IDX) / Laporan Tahunan Ciputra', 'verifiedDate': '2026-10-22'
    },
    'IDX:SMRA': {
        'name': 'PT Summarecon Agung Tbk',
        'ticker': 'SMRA', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'Properti & Real Estat', 'currency': 'IDR',
        'description': 'Summarecon Agung mengkhususkan diri dalam pengembangan kawasan kota terpadu mandiri seperti Kelapa Gading, Serpong, Bekasi, dan Bandung lengkap dengan pusat perbelanjaan dan fasilitas publik.',
        'website': 'https://summarecon.com', 'source': 'Bursa Efek Indonesia (IDX) / Investor Relations Summarecon', 'verifiedDate': '2026-10-22'
    },

    # --- US Teknologi ---
    'US:AAPL': {
        'name': 'Apple Inc.',
        'ticker': 'AAPL', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'Apple Inc. mendesain, memproduksi, dan memasarkan gawai komputasi pribadi iPhone, Mac, iPad, aksesoris terhubung, serta ekosistem layanan digital App Store, iCloud, dan Apple Music.',
        'website': 'https://www.apple.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Apple', 'verifiedDate': '2026-10-22'
    },
    'US:MSFT': {
        'name': 'Microsoft Corporation',
        'ticker': 'MSFT', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'Microsoft Corporation adalah raksasa perangkat lunak dan komputasi awan global yang memproduksi sistem operasi Windows, rangkaian produktivitas Microsoft 365, serta platform cloud Azure.',
        'website': 'https://www.microsoft.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Microsoft', 'verifiedDate': '2026-10-22'
    },
    'US:NVDA': {
        'name': 'NVIDIA Corporation',
        'ticker': 'NVDA', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'NVIDIA memelopori unit pengolah grafis (GPU) performa tinggi dan ekosistem komputasi kecerdasan buatan terakselerasi untuk pusat data, riset AI generatif, dan visualisasi interaktif.',
        'website': 'https://www.nvidia.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations NVIDIA', 'verifiedDate': '2026-10-22'
    },
    'US:ORCL': {
        'name': 'Oracle Corporation',
        'ticker': 'ORCL', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'Oracle Corporation menyediakan solusi basis data perusahaan, perangkat lunak perencanaan sumber daya korporat (ERP), dan layanan infrastruktur komputasi awan generasi lanjut.',
        'website': 'https://www.oracle.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Oracle', 'verifiedDate': '2026-10-22'
    },
    'US:IBM': {
        'name': 'International Business Machines Corporation',
        'ticker': 'IBM', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'IBM berfokus pada teknologi cloud hibrida (Red Hat), konsultasi transformasi TI perusahaan, sistem komputasi kuantum, dan solusi kecerdasan buatan watsonx.',
        'website': 'https://www.ibm.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations IBM', 'verifiedDate': '2026-10-22'
    },
    'US:AMD': {
        'name': 'Advanced Micro Devices, Inc.',
        'ticker': 'AMD', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'AMD mengembangkan prosesor semikonduktor berkinerja tinggi Ryzen, EPYC, serta chip akselerator kecerdasan buatan Instinct untuk server dan pusat data.',
        'website': 'https://www.amd.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations AMD', 'verifiedDate': '2026-10-22'
    },
    'US:INTC': {
        'name': 'Intel Corporation',
        'ticker': 'INTC', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'Intel Corporation mendesain dan memproduksi prosesor arsitektur x86 untuk komputer pribadi, server korporat, serta fasilitas fabrikasi manufaktur semikonduktor mandiri.',
        'website': 'https://www.intel.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Intel', 'verifiedDate': '2026-10-22'
    },
    'US:CSCO': {
        'name': 'Cisco Systems, Inc.',
        'ticker': 'CSCO', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Teknologi', 'currency': 'USD',
        'description': 'Cisco Systems memimpin industri jaringan internet global melalui perangkat switch, router telekomunikasi, sistem keamanan siber terpadu, dan perangkat lunak kolaborasi.',
        'website': 'https://www.cisco.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Cisco', 'verifiedDate': '2026-10-22'
    },

    # --- US Komunikasi & Media ---
    'US:GOOGL': {
        'name': 'Alphabet Inc.',
        'ticker': 'GOOGL', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Komunikasi & Media', 'currency': 'USD',
        'description': 'Alphabet adalah perusahaan induk Google yang memimpin mesin pencarian web, platform periklanan digital, sistem operasi Android, YouTube, dan layanan Google Cloud.',
        'website': 'https://abc.xyz', 'source': 'SEC EDGAR Form 10-K / Investor Relations Alphabet', 'verifiedDate': '2026-10-22'
    },
    'US:META': {
        'name': 'Meta Platforms Inc.',
        'ticker': 'META', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Komunikasi & Media', 'currency': 'USD',
        'description': 'Meta Platforms membangun teknologi koneksi sosial global yang mengoperasikan jejaring sosial Facebook, Instagram, WhatsApp, Messenger, serta perangkat realitas virtual Quest.',
        'website': 'https://investor.atmeta.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Meta', 'verifiedDate': '2026-10-22'
    },
    'US:DIS': {
        'name': 'The Walt Disney Company',
        'ticker': 'DIS', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Komunikasi & Media', 'currency': 'USD',
        'description': 'The Walt Disney Company adalah konglomerasi hiburan keluarga terbesar di dunia yang mengelola studio film, jaringan media televisi (ABC, ESPN), taman rekreasi tema global, dan Disney+.',
        'website': 'https://thewaltdisneycompany.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Disney', 'verifiedDate': '2026-10-22'
    },
    'US:NFLX': {
        'name': 'Netflix Inc.',
        'ticker': 'NFLX', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Komunikasi & Media', 'currency': 'USD',
        'description': 'Netflix adalah pelopor layanan hiburan streaming berlangganan global yang menyajikan ribuan film, serial televisi orisinal berkualitas tinggi, dan permainan digital.',
        'website': 'https://ir.netflix.net', 'source': 'SEC EDGAR Form 10-K / Investor Relations Netflix', 'verifiedDate': '2026-10-22'
    },

    # --- US Konsumen Nonprimer ---
    'US:AMZN': {
        'name': 'Amazon.com Inc.',
        'ticker': 'AMZN', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Nonprimer', 'currency': 'USD',
        'description': 'Amazon berfokus pada e-commerce perdagangan ritel daring terbesar, platform komputasi awan AWS yang memimpin pasar, logistik pergudangan global, dan layanan streaming digital.',
        'website': 'https://www.aboutamazon.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Amazon', 'verifiedDate': '2026-10-22'
    },
    'US:TSLA': {
        'name': 'Tesla Inc.',
        'ticker': 'TSLA', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Nonprimer', 'currency': 'USD',
        'description': 'Tesla mendesain dan memproduksi kendaraan listrik bertenaga baterai performa tinggi, sistem penyimpanan energi surya skala jaringan, serta perangkat lunak swakemudi Full Self-Driving.',
        'website': 'https://ir.tesla.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Tesla', 'verifiedDate': '2026-10-22'
    },
    'US:HD': {
        'name': 'The Home Depot, Inc.',
        'ticker': 'HD', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Nonprimer', 'currency': 'USD',
        'description': 'The Home Depot adalah peritel perlengkapan dan perbaikan rumah terbesar di dunia yang menyediakan bahan bangunan, perkakas, dan produk renovasi hunian.',
        'website': 'https://corporate.homedepot.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Home Depot', 'verifiedDate': '2026-10-22'
    },
    'US:MCD': {
        'name': 'McDonald\'s Corporation',
        'ticker': 'MCD', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Nonprimer', 'currency': 'USD',
        'description': 'McDonald\'s Corporation mengoperasikan dan mewaralabakan jaringan restoran cepat saji terbesar di dunia dengan puluhan ribu gerai yang menyajikan makanan ikonik di lebih dari 100 negara.',
        'website': 'https://corporate.mcdonalds.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations McDonald\'s', 'verifiedDate': '2026-10-22'
    },
    'US:NKE': {
        'name': 'Nike, Inc.',
        'ticker': 'NKE', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Nonprimer', 'currency': 'USD',
        'description': 'Nike adalah pemimpin global dalam desain, pengembangan, dan pemasaran alas kaki, pakaian, peralatan, dan aksesoris olahraga atletik bermutu tinggi.',
        'website': 'https://investors.nike.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Nike', 'verifiedDate': '2026-10-22'
    },
    'US:SBUX': {
        'name': 'Starbucks Corporation',
        'ticker': 'SBUX', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Nonprimer', 'currency': 'USD',
        'description': 'Starbucks adalah peritel dan pemanggang biji kopi spesialitas terbesar di dunia dengan jaringan kedai kopi modern yang menyajikan pengalaman ketiga di luar rumah dan kantor.',
        'website': 'https://investor.starbucks.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Starbucks', 'verifiedDate': '2026-10-22'
    },

    # --- US Konsumen Primer ---
    'US:WMT': {
        'name': 'Walmart Inc.',
        'ticker': 'WMT', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Primer', 'currency': 'USD',
        'description': 'Walmart Inc. mengoperasikan jaringan hypermarket, department store diskon, dan toko kelontong ritel terbesar di dunia dengan fokus harga terjangkau setiap hari.',
        'website': 'https://corporate.walmart.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Walmart', 'verifiedDate': '2026-10-22'
    },
    'US:KO': {
        'name': 'The Coca-Cola Company',
        'ticker': 'KO', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Primer', 'currency': 'USD',
        'description': 'The Coca-Cola Company memproduksi konsentrat dan sirup minuman nonalkohol terbesar di dunia, membawahi merek ikonik Coca-Cola, Sprite, Fanta, dan Minute Maid.',
        'website': 'https://www.coca-colacompany.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Coca-Cola', 'verifiedDate': '2026-10-22'
    },
    'US:PEP': {
        'name': 'PepsiCo, Inc.',
        'ticker': 'PEP', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Primer', 'currency': 'USD',
        'description': 'PepsiCo adalah produsen makanan ringan kemasan dan minuman global terkemuka dengan merek terpercaya seperti Lay\'s, Doritos, Quaker Oats, dan Pepsi-Cola.',
        'website': 'https://www.pepsico.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations PepsiCo', 'verifiedDate': '2026-10-22'
    },
    'US:PG': {
        'name': 'The Procter & Gamble Company',
        'ticker': 'PG', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Primer', 'currency': 'USD',
        'description': 'Procter & Gamble memproduksi barang konsumen terkemuka dunia di bidang perawatan pribadi, perawatan bayi, dan kebersihan rumah tangga seperti Pampers, Tide, dan Gillette.',
        'website': 'https://www.pginvestor.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations P&G', 'verifiedDate': '2026-10-22'
    },
    'US:COST': {
        'name': 'Costco Wholesale Corporation',
        'ticker': 'COST', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Konsumen Primer', 'currency': 'USD',
        'description': 'Costco Wholesale mengoperasikan klub gudang keanggotaan berbayar internasional yang menyediakan produk bermutu tinggi dalam jumlah besar dengan marjin hemat efisien.',
        'website': 'https://investor.costco.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Costco', 'verifiedDate': '2026-10-22'
    },

    # --- US Kesehatan ---
    'US:JNJ': {
        'name': 'Johnson & Johnson',
        'ticker': 'JNJ', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'Johnson & Johnson adalah perusahaan perawatan kesehatan terdiversifikasi global terkemuka yang meneliti dan memproduksi obat-obatan resep inovatif dan perangkat medis bedah canggih.',
        'website': 'https://www.jnj.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations J&J', 'verifiedDate': '2026-10-22'
    },
    'US:UNH': {
        'name': 'UnitedHealth Group Incorporated',
        'ticker': 'UNH', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'UnitedHealth Group adalah perusahaan asuransi dan layanan kesehatan terbesar di Amerika Serikat yang menyediakan perlindungan kesehatan dan layanan data farmasi Optum.',
        'website': 'https://www.unitedhealthgroup.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations UnitedHealth', 'verifiedDate': '2026-10-22'
    },
    'US:PFE': {
        'name': 'Pfizer Inc.',
        'ticker': 'PFE', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'Pfizer Inc. adalah perusahaan biofarmasi berbasis riset yang mengembangkan dan memproduksi obat-obatan inovatif, vaksin terapeutik, dan terapi kesehatan modern.',
        'website': 'https://investors.pfizer.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Pfizer', 'verifiedDate': '2026-10-22'
    },
    'US:MRK': {
        'name': 'Merck & Co., Inc.',
        'ticker': 'MRK', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'Merck & Co. adalah raksasa farmasi global yang memimpin riset imunoonkologi (Keytruda), vaksin infeksi, dan obat perawatan penyakit kritis hewan dan manusia.',
        'website': 'https://www.merck.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Merck', 'verifiedDate': '2026-10-22'
    },
    'US:ABBV': {
        'name': 'AbbVie Inc.',
        'ticker': 'ABBV', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'AbbVie adalah perusahaan biofarmasi yang berfokus pada riset terapi imunologi (Humira, Skyrizi), onkologi darah, ilmu saraf, dan estetika medis Allergan.',
        'website': 'https://investors.abbvie.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations AbbVie', 'verifiedDate': '2026-10-22'
    },
    'US:LLY': {
        'name': 'Eli Lilly and Company',
        'ticker': 'LLY', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'Eli Lilly and Company memimpin riset dan pengembangan terapi endokrinologi diabetes, penanganan obesitas berbasis hormon GLP-1, dan obat penyakit neurologi.',
        'website': 'https://investor.lilly.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Eli Lilly', 'verifiedDate': '2026-10-22'
    },
    'US:ABT': {
        'name': 'Abbott Laboratories',
        'ticker': 'ABT', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Kesehatan', 'currency': 'USD',
        'description': 'Abbott Laboratories memproduksi perangkat diagnostik medis laboratorium, alat pemantau glukosa FreeStyle Libre, nutrisi pediatrik, dan peralatan vaskular jantung.',
        'website': 'https://www.abbottinvestor.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Abbott', 'verifiedDate': '2026-10-22'
    },

    # --- US Keuangan ---
    'US:JPM': {
        'name': 'JPMorgan Chase & Co.',
        'ticker': 'JPM', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'JPMorgan Chase adalah bank investasi dan holding institusi keuangan terbesar di Amerika Serikat yang melayani jutaan nasabah perbankan ritel, korporat, dan pengelola aset global.',
        'website': 'https://www.jpmorganchase.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations JPMorgan', 'verifiedDate': '2026-10-22'
    },
    'US:BRK.B': {
        'name': 'Berkshire Hathaway Inc.',
        'ticker': 'BRK.B', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Berkshire Hathaway adalah konglomerasi investasi terkemuka pimpinan Warren Buffett yang memiliki perusahaan asuransi GEICO, jaringan rel kereta BNSF, dan portofolio saham publik masif.',
        'website': 'https://www.berkshirehathaway.com', 'source': 'SEC EDGAR Form 10-K / Laporan Tahunan Berkshire', 'verifiedDate': '2026-10-22'
    },
    'US:V': {
        'name': 'Visa Inc.',
        'ticker': 'V', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Visa Inc. memfasilitasi transaksi pembayaran digital di seluruh dunia melalui jaringan pemrosesan pembayaran VisaNet yang menghubungkan konsumen, merchant, dan lembaga perbankan.',
        'website': 'https://investor.visa.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Visa', 'verifiedDate': '2026-10-22'
    },
    'US:MA': {
        'name': 'Mastercard Incorporated',
        'ticker': 'MA', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Mastercard adalah perusahaan teknologi pembayaran global yang menyediakan solusi otorisasi, kliring, dan settlement untuk kartu kredit, debit, dan transaksi komersial digital.',
        'website': 'https://investor.mastercard.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Mastercard', 'verifiedDate': '2026-10-22'
    },
    'US:BAC': {
        'name': 'Bank of America Corporation',
        'ticker': 'BAC', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Bank of America adalah salah satu lembaga keuangan terbesar di dunia yang melayani nasabah individu, usaha kecil menengah, dan korporasi besar melalui Merrill Lynch.',
        'website': 'https://investor.bankofamerica.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations BofA', 'verifiedDate': '2026-10-22'
    },
    'US:WFC': {
        'name': 'Wells Fargo & Company',
        'ticker': 'WFC', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Wells Fargo adalah perusahaan jasa keuangan berbasis perbankan komunitas terbesar di Amerika Serikat dengan fokus pembiayaan perumahan, komersial, dan tabungan ritel.',
        'website': 'https://www.wellsfargo.com/about/investor-relations', 'source': 'SEC EDGAR Form 10-K / Investor Relations Wells Fargo', 'verifiedDate': '2026-10-22'
    },
    'US:GS': {
        'name': 'The Goldman Sachs Group, Inc.',
        'ticker': 'GS', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Goldman Sachs adalah bank investasi dan penasihat keuangan global terkemuka yang menyediakan jasa penjaminan emisi, perdagangan sekuritas, dan manajemen aset institusional.',
        'website': 'https://www.goldmansachs.com/investor-relations', 'source': 'SEC EDGAR Form 10-K / Investor Relations Goldman Sachs', 'verifiedDate': '2026-10-22'
    },
    'US:MS': {
        'name': 'Morgan Stanley',
        'ticker': 'MS', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Morgan Stanley menyediakan layanan perbankan investasi terkemuka, perdagangan pasar modal, dan manajemen kekayaan (wealth management) untuk korporasi dan individu berpenghasilan tinggi.',
        'website': 'https://www.morganstanley.com/about-us-ir', 'source': 'SEC EDGAR Form 10-K / Investor Relations Morgan Stanley', 'verifiedDate': '2026-10-22'
    },
    'US:AXP': {
        'name': 'American Express Company',
        'ticker': 'AXP', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'American Express adalah penyedia layanan pembayaran dan kartu kredit premium global yang terkenal dengan jaringan transaksi tertutup dan program loyalitas pelanggan bisnis.',
        'website': 'https://ir.americanexpress.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations American Express', 'verifiedDate': '2026-10-22'
    },
    'US:BLK': {
        'name': 'BlackRock, Inc.',
        'ticker': 'BLK', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'BlackRock adalah manajer investasi terbesar di dunia yang mengelola dana institusi dan ritel melalui produk ETF iShares dan sistem manajemen risiko teknologi Aladdin.',
        'website': 'https://ir.blackrock.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations BlackRock', 'verifiedDate': '2026-10-22'
    },
    'US:SCHW': {
        'name': 'The Charles Schwab Corporation',
        'ticker': 'SCHW', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Keuangan', 'currency': 'USD',
        'description': 'Charles Schwab menyediakan layanan pialang efek ritel, perbankan komersial, kustodian reksa dana, dan manajemen kekayaan melalui platform investasi mandiri.',
        'website': 'https://www.aboutschwab.com/investor-relations', 'source': 'SEC EDGAR Form 10-K / Investor Relations Charles Schwab', 'verifiedDate': '2026-10-22'
    },

    # --- US Energi ---
    'US:XOM': {
        'name': 'ExxonMobil Corporation',
        'ticker': 'XOM', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Energi', 'currency': 'USD',
        'description': 'ExxonMobil adalah perusahaan energi dan petrokimia multinasional terbesar di Amerika Serikat yang mengeksplorasi, memproduksi, dan menyuling minyak bumi dan gas alam dunia.',
        'website': 'https://investor.exxonmobil.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations ExxonMobil', 'verifiedDate': '2026-10-22'
    },
    'US:CVX': {
        'name': 'Chevron Corporation',
        'ticker': 'CVX', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Energi', 'currency': 'USD',
        'description': 'Chevron Corporation mengelola portofolio eksplorasi hulu minyak dan gas terpadu serta pengolahan hilir bahan bakar dan bahan pelumas berkualitas di berbagai belahan dunia.',
        'website': 'https://www.chevron.com/investors', 'source': 'SEC EDGAR Form 10-K / Investor Relations Chevron', 'verifiedDate': '2026-10-22'
    },
    'US:COP': {
        'name': 'ConocoPhillips',
        'ticker': 'COP', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Energi', 'currency': 'USD',
        'description': 'ConocoPhillips adalah perusahaan eksplorasi dan produksi (E&P) minyak dan gas bumi murni independen terbesar di dunia dengan aset di Amerika Utara, Eropa, dan Asia Pasifik.',
        'website': 'https://www.conocophillips.com/investor-relations', 'source': 'SEC EDGAR Form 10-K / Investor Relations ConocoPhillips', 'verifiedDate': '2026-10-22'
    },

    # --- US Industri ---
    'US:CAT': {
        'name': 'Caterpillar Inc.',
        'ticker': 'CAT', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Industri', 'currency': 'USD',
        'description': 'Caterpillar adalah produsen peralatan konstruksi, pertambangan, mesin diesel, dan turbin gas industri terkemuka di dunia dengan warna kuning ikonik.',
        'website': 'https://investors.caterpillar.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Caterpillar', 'verifiedDate': '2026-10-22'
    },
    'US:DE': {
        'name': 'Deere & Company',
        'ticker': 'DE', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Industri', 'currency': 'USD',
        'description': 'Deere & Company (John Deere) adalah produsen mesin pertanian, traktor canggih terpandu GPS, dan alat berat kehutanan terkemuka dunia dengan fokus pertanian presisi.',
        'website': 'https://investor.deere.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations John Deere', 'verifiedDate': '2026-10-22'
    },
    'US:HON': {
        'name': 'Honeywell International Inc.',
        'ticker': 'HON', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Industri', 'currency': 'USD',
        'description': 'Honeywell International memproduksi sistem avionik kedirgantaraan, otomasi gedung komersial, bahan kimia khusus industri, dan teknologi keselamatan kerja modern.',
        'website': 'https://investor.honeywell.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations Honeywell', 'verifiedDate': '2026-10-22'
    },
    'US:GE': {
        'name': 'GE Aerospace',
        'ticker': 'GE', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Industri', 'currency': 'USD',
        'description': 'GE Aerospace adalah penyedia mesin jet propulsi komersial dan militer serta sistem kedirgantaraan terdepan di dunia yang melayani armada penerbangan global.',
        'website': 'https://www.geaerospace.com/investor-relations', 'source': 'SEC EDGAR Form 10-K / Investor Relations GE', 'verifiedDate': '2026-10-22'
    },
    'US:UPS': {
        'name': 'United Parcel Service, Inc.',
        'ticker': 'UPS', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Industri', 'currency': 'USD',
        'description': 'United Parcel Service (UPS) mengoperasikan armada transportasi kargo udara dan logistik paket darat global terbesar dengan jaringan pengiriman terhubung ke lebih dari 200 negara.',
        'website': 'https://investors.ups.com', 'source': 'SEC EDGAR Form 10-K / Investor Relations UPS', 'verifiedDate': '2026-10-22'
    },

    # --- US Utilitas ---
    'US:NEE': {
        'name': 'NextEra Energy, Inc.',
        'ticker': 'NEE', 'exchange': 'US', 'country': 'Amerika Serikat', 'sector': 'Utilitas', 'currency': 'USD',
        'description': 'NextEra Energy adalah perusahaan utilitas tenaga listrik terbesar di Amerika Serikat dan produsen energi terbarukan tenaga angin dan surya terbesar di dunia.',
        'website': 'https://www.nexteraenergy.com/investors', 'source': 'SEC EDGAR Form 10-K / Investor Relations NextEra', 'verifiedDate': '2026-10-22'
    },

    # --- IPO Simulasi Game (Instrumen ke-101) ---
    'IDX:NCLD': {
        'name': 'PT Nusantara Cloud Tech Tbk',
        'ticker': 'NCLD', 'exchange': 'IDX', 'country': 'Indonesia', 'sector': 'IPO', 'currency': 'IDR',
        'description': 'Perusahaan penyedia infrastruktur komputasi awan dan pusat data lokal di Indonesia yang terdaftar melalui penawaran umum perdana (IPO) simulasi game. Terhubung dengan penawaran aksi korporasi game.',
        'website': 'https://game-saham.internal/ipo-cloud', 'source': 'Pusat Penawaran IPO Simulasi Game', 'verifiedDate': '2026-10-22'
    }
}

def generate_profiles():
    target_file = ROOT / 'android/app/src/main/assets/data/company_profiles.json'
    target_file.parent.mkdir(parents=True, exist_ok=True)
    with open(target_file, 'w', encoding='utf-8') as f:
        json.dump(PROFILES, f, indent=2, ensure_ascii=False)
    print(f'Successfully generated company profiles for {len(PROFILES)} companies at {target_file}')

if __name__ == '__main__':
    generate_profiles()
