# FocusBuddy

FocusBuddy adalah aplikasi produktivitas yang dirancang untuk membantu siswa dan mahasiswa fokus dalam belajar dan mengelola tugas-tugas mereka secara efektif.

## Fitur Utama

- 📊 **Dashboard** - Tampilan ringkas aktivitas dan progres
- ✅ **Manajemen Tugas** - Kelola tugas dengan prioritas dan tenggat waktu
- 🍅 **Timer Pomodoro** - Teknik fokus dengan interval kerja dan istirahat
- 📝 **Catatan** - Buat dan kelola catatan pelajaran
- 📚 **Mata Pelajaran** - Organisasi tugas dan catatan berdasarkan mata pelajaran
- 👤 **Profil & Pengaturan** - Kustomisasi pengalaman pengguna

## Teknologi

- Java 17
- JavaFX untuk antarmuka pengguna
- SQLite untuk penyimpanan data
- Maven untuk manajemen proyek
- SLF4J & Logback untuk logging
- JUnit 5 untuk testing

## Persyaratan Sistem

- Java Development Kit (JDK) 17 atau lebih baru
- Maven 3.6.0 atau lebih baru
- Ruang disk minimal 100MB
- RAM minimal 2GB

## Instalasi

1. Clone repositori ini:
```bash
git clone https://github.com/yourusername/focus-buddy.git
cd focus-buddy
```

2. Build proyek dengan Maven:
```bash
mvn clean install
```

3. Jalankan aplikasi:
```bash
mvn javafx:run
```

## Penggunaan

1. **Login/Register**
   - Buat akun baru atau login dengan akun yang sudah ada
   - Pilih "Ingat Saya" untuk login otomatis

2. **Dashboard**
   - Lihat ringkasan tugas hari ini
   - Monitor waktu fokus
   - Akses cepat ke fitur utama

3. **Manajemen Tugas**
   - Tambah, edit, dan hapus tugas
   - Atur prioritas dan tenggat waktu
   - Filter dan urutkan tugas

4. **Timer Pomodoro**
   - Set durasi fokus (default: 25 menit)
   - Set durasi istirahat (default: 5 menit)
   - Notifikasi otomatis saat sesi berakhir

5. **Catatan**
   - Buat catatan dengan format rich text
   - Organisasi catatan per mata pelajaran
   - Fitur pencarian dan filter

6. **Pengaturan**
   - Pilih tema (Terang/Gelap)
   - Atur preferensi notifikasi
   - Kustomisasi timer Pomodoro

## Pengembangan

### Struktur Proyek

```
focus-buddy/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── focusbuddy/
│   │   │           ├── controllers/
│   │   │           ├── models/
│   │   │           ├── services/
│   │   │           └── utils/
│   │   └── resources/
│   │       ├── css/
│   │       ├── fxml/
│   │       └── images/
│   └── test/
│       └── java/
│           └── com/
│               └── focusbuddy/
└── pom.xml
```

### Panduan Kontribusi

1. Fork repositori
2. Buat branch fitur (`git checkout -b feature/AmazingFeature`)
3. Commit perubahan (`git commit -m 'Add some AmazingFeature'`)
4. Push ke branch (`git push origin feature/AmazingFeature`)
5. Buat Pull Request

## Lisensi

Proyek ini dilisensikan di bawah [MIT License](LICENSE).

## Kontak

- Email: your.email@example.com
- Project Link: https://github.com/yourusername/focus-buddy

## Ucapan Terima Kasih

- [JavaFX](https://openjfx.io/)
- [SQLite](https://www.sqlite.org/)
- [Font Awesome](https://fontawesome.com/)
- [Google Fonts](https://fonts.google.com/)
