package com.example.codasuaka.ui.screen.login

const val PRIVACY_POLICY_TEXT = """
Tanggal Efektif: 26 September 2026
Terakhir Diperbarui: 26 September 2026

CodaSuaka ("Penyedia", "Kami") berkomitmen untuk melindungi dan menghormati privasi serta data pribadi Pengguna ("Anda"). Kebijakan Privasi ini menjelaskan bagaimana Kami mengumpulkan, menggunakan, menyimpan, memproses, dan melindungi informasi pribadi yang Anda berikan saat mengakses atau menggunakan aplikasi mobile CodaSuaka, layanan backend, API, serta seluruh fitur pendukungnya ("Aplikasi").

Dengan mengunduh, mendaftar, mengakses, atau menggunakan Aplikasi, Anda dianggap telah membaca, memahami, dan menyetujui seluruh ketentuan dalam Kebijakan Privasi ini.

1. Informasi dan Data yang Kami Kumpulkan
Kami mengumpulkan beberapa jenis informasi untuk memberikan, memelihara, dan meningkatkan layanan Aplikasi kepada Anda dan Instansi Anda:

A. Data Identitas dan Akun
• Kredensial Pengguna: Nama lengkap, alamat email, nomor telepon, dan kata sandi yang digunakan untuk pembuatan dan otentikasi Akun.
• Peran Akses (Role & Permission): Informasi peran Anda dalam Instansi (Super Admin, Owner, Manajemen, Keuangan, atau Staff).

B. Data Operasional Instansi (Data Multi-Tenant)
• Data Kepegawaian & Karyawan: Nama karyawan, riwayat pekerjaan, struktur organisasi, serta dokumen pendukung terkait pengajuan cuti, izin, atau sakit.
• Data Presensi & Lokasi: Catatan jam masuk/keluar kerja, data lokasi (GPS) saat melakukan presensi (jika fitur presensi berbasis lokasi diaktifkan), serta swafoto/foto bukti presensi (jika ada).
• Data Keuangan: Catatan transaksi buku kas, pengeluaran, pemasukan, dan laporan keuangan internal Instansi.
• Komunikasi Internal: Data percakapan/pesan (chat) internal antar pengguna dalam satu Instansi.

C. Informasi Teknis dan Perangkat
• Identifikasi Perangkat: Alamat IP, jenis dan versi sistem operasi (misalnya Android), identifier unik perangkat, token otentikasi (Bearer Token), serta data diagnostik/crash log untuk keperluan teknis dan keamanan.

2. Cara Kami Menggunakan Informasi Anda
Kami menggunakan data yang dikumpulkan untuk tujuan-tujuan berikut:
• Layanan Operasional: Menyediakan, mengoperasikan, dan memelihara fitur Aplikasi, termasuk manajemen presensi, pengajuan izin/cuti, pencatatan keuangan, dan komunikasi tim.
• Otentikasi dan Keamanan: Memverifikasi identitas Pengguna, menjaga isolasi data antar-Instansi (multi-tenant isolation), dan mencegah akses tidak sah.
• Pengembangan & Pemeliharaan: Menganalisis kinerja Aplikasi, memperbaiki bug, serta mengembangkan fitur-fitur baru.
• Komunikasi Layanan: Mengirimkan notifikasi penting terkait aktivitas sistem, pembaruan Aplikasi, pemberitahuan billing/paket berlangganan, atau perubahan Kebijakan Privasi/EULA.
• Kepatuhan Hukum: Memenuhi kewajiban hukum dan peraturan perundang-undangan yang berlaku di Republik Indonesia.

3. Keamanan dan Isolasi Data (Tenant Scope)
• Isolasi Multi-Tenant: Data setiap Instansi diisolasi secara ketat dan otomatis dari Instansi lain menggunakan mekanisme Tenant Scope pada sistem backend Kami.
• Perlindungan Akses: Kami mengimplementasikan enkripsi data, pengamanan token otentikasi (Bearer Token), serta batasan akses berbasis peran untuk mencegah kebocoran atau penyalahgunaan data.
• Tanggung Jawab Akun: Pengguna wajib menjaga kerahasiaan kata sandi dan kredensial Akun masing-masing.

4. Pembagian dan Pengungkapan Data
Kami tidak akan menjual, menyewakan, atau memperdagangkan Data Instansi maupun data pribadi Pengguna kepada pihak ketiga. Kami hanya dapat mengungkapkan data dalam kondisi berikut:
• Penyedia Layanan Pihak Ketiga (Infrastructure & Service Providers): Kami dapat memanfaatkan layanan pihak ketiga yang terpercaya (seperti penyedia cloud server, penyedia database, atau payment gateway) semata-mata untuk menunjang operasional Aplikasi.
• Kewajiban Hukum: Apabila diwajibkan oleh hukum, perintah pengadilan, atau permintaan resmi dari lembaga pemerintah/penegak hukum yang berwenang di Indonesia.
• Persetujuan Pengguna: Atas persetujuan tegas dari Pengguna atau Pemilik Instansi (Owner).

5. Tanggung Jawab Pengguna atas Data Karyawan
Sesuai dengan Pasal 7 EULA CodaSuaka, Instansi dan Pengguna (khususnya Owner dan Manajemen) menyatakan bahwa mereka memiliki hak dan dasar hukum yang sah untuk memasukkan data pribadi karyawan ke dalam Aplikasi. Instansi bertanggung jawab penuh untuk memperoleh persetujuan (consent) yang diperlukan dari masing-masing karyawan sebelum memasukkan data mereka ke CodaSuaka.

6. Penyimpanan dan Penghapusan Data
• Masa Penyimpanan: Data Anda akan disimpan selama Akun/Paket Berlangganan Instansi Anda aktif, atau sejauh yang dibutuhkan untuk memenuhi tujuan operasional dan hukum.
• Penghapusan Data: Jika Paket Berlangganan berakhir atau Akun dihapus, Kami berhak membatasi akses dan/atau menghapus Data Instansi secara permanen sesuai dengan ketentuan penutupan akun yang berlaku. Pengguna dapat mengajukan permohonan penghapusan akun atau data pribadi tertentu melalui saluran dukungan resmi CodaSuaka.

7. Hak-Hak Pengguna / Subjek Data
Sesuai dengan Undang-Undang Nomor 27 Tahun 2022 tentang Pelindungan Data Pribadi (UU PDP), Anda berhak untuk:
• Mengakses dan memperbarui informasi profil/akun Anda dalam Aplikasi.
• Mengajukan keberatan atau meminta pembatasan pemrosesan data tertentu (melalui Pemilik Instansi Anda).
• Meminta penghapusan data pribadi Anda, sepanjang tidak bertentangan dengan kewajiban hukum atau hak pengelolaan data internal Instansi.

8. Perubahan Kebijakan Privasi
Penyedia berhak untuk mengubah atau memperbarui Kebijakan Privasi ini dari waktu ke waktu. Perubahan akan diberitahukan melalui notifikasi Aplikasi, email, atau saluran komunikasi resmi CodaSuaka. Penggunaan Aplikasi secara berkelanjutan setelah adanya pembaruan menandakan persetujuan Anda terhadap Kebijakan Privasi yang telah direvisi.

9. Hubungi Kami
Jika Anda memiliki pertanyaan, kendala, atau permintaan terkait Kebijakan Privasi ini atau pengelolaan data pribadi Anda pada Aplikasi CodaSuaka, silakan hubungi Kami melalui:
• Aplikasi: Menu Bantuan / Dukungan dalam Aplikasi CodaSuaka
• Email Dukungan: codasuaka@gmail.com
• Telepon/WhatsApp: 085850549645
"""
