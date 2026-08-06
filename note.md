Perbarui berkas changelog.md yang sudah ada dengan mengompilasi log komit terbaru dengan ketentuan sebagai berikut:

1. **Cakupan & Pemeriksaan Komit Lengkap:**
   - Periksa seluruh riwayat komit git (termasuk semua branch, tag, dan reflog) dari periode yang ditentukan (misalnya dari Juli hingga hari ini/Agustus).
   - Pastikan TIDAK ADA fitur, perbaikan bug, atau penyesuaian yang terlewat, sekecil apa pun komit tersebut.

2. **Pencegahan Duplikasi & Pengelolaan Versi:**
   - JANGAN membuat poin duplikat atau menuliskan ulang perubahan yang sudah tercatat sebelumnya di changelog.
   - Jika membuat nomor/judul versi baru di bagian paling atas, pastikan entri baru tersebut tidak menyalin entri dari versi di bawahnya.
   - Sesuaikan rentang bulan/tahun pada judul versi (contoh: `## [1.4] - July – August 2026` atau buat tag versi baru seperti `## [1.5] - August 2026`) agar mencerminkan rentang waktu komit aktual secara akurat.
   - Pertahankan seluruh riwayat versi lama yang sudah ada tanpa merusak strukturnya.

3. **Fokus pada Perubahan Perilaku (Behavioral Changes Only):**
   - Terjemahkan pesan komit menjadi deskripsi perubahan perilaku nyata yang dirasakan oleh pengguna atau sistem.
   - JANGAN mencantumkan detail teknis internal source code (dilarang menuliskan nama kelas, nama fungsi, refactoring internal, atau nama variabel).
   - JANGAN mencantumkan aktivitas build teknis atau CI/CD (contoh yang dilarang: "Rebuilt the jar file", "Updated workflow script").

4. **Kategori Perubahan:**
   Kelompokkan setiap perubahan ke dalam sub-judul yang sesuai:
   - `### Added` (untuk fitur baru / komit bertipe `feat:`, `Add...`)
   - `### Fixed` (untuk perbaikan bug / komit bertipe `fix:`, `Fixing...`)
   - `### Changed / Improved` (untuk peningkatan performa, pembaruan kompatibilitas, atau penyesuaian fungsi)

5. **Format Penulisan Poin:**
   - Gunakan awalan bullet point standar (`-`).
   - JANGAN menggunakan awalan nama fitur tebal di awal baris (contoh yang dilarang: `- **Feature Name:** Fixed...`).
   - Tulis deskripsi perubahan secara langsung, ringkas, profesional, dan jelas.

   

Buatkan berkas changelog.md untuk pembaruan proyek ini berdasarkan seluruh riwayat komit (git commits) yang ada, dengan ketentuan sebagai berikut:

1. **Cakupan & Integritas Riwayat Komit:**
   - Periksa seluruh riwayat komit git (termasuk semua branch, tag, dan reflog) pada rentang waktu/versi yang ditentukan agar tidak ada perubahan yang terlewat.
   - Lakukan deduplikasi: jika ada beberapa komit yang membahas perbaikan atau fitur yang sama, gabungkan menjadi satu poin yang jelas dan utuh tanpa mengulang entri.

2. **Fokus pada Perubahan Perilaku (Behavioral Changes Only):**
   - Terjemahkan setiap pesan komit menjadi perubahan perilaku nyata yang dirasakan oleh pengguna atau sistem.
   - JANGAN menuliskan detail teknis internal source code (dilarang menuliskan nama kelas, nama fungsi, nama variabel, atau deskripsi refactoring internal).
   - JANGAN mencantumkan aktivitas build teknis atau CI/CD (contoh yang dilarang: "Rebuilt the jar file", "Updated workflow configuration").

3. **Struktur Header & Kategori Perubahan:**
   - Sertakan judul utama proyek (contoh: `# Changelog - [Nama Proyek]`) dan nomor/versi tanggal yang akurat (contoh: `## [1.4] - July – August 2026`).
   - Kelompokkan komit ke bawah sub-judul berikut:
     - `### Added` (untuk fitur baru / komit bertipe `feat:`, `Add...`)
     - `### Fixed` (untuk perbaikan bug / komit bertipe `fix:`, `Fixing...`)
     - `### Changed / Improved` (untuk peningkatan performa, pembaruan kompatibilitas, atau komit bertipe `Optimize...`, `Normalize...`, `Update...`)

4. **Format Penulisan Poin:**
   - Gunakan bullet points standar (`-`).
   - JANGAN menggunakan awalan nama fitur tebal di awal baris (contoh yang dilarang: `- **Amethyst Tools:** Fixed case-sensitivity...` atau `- **Combat:** Fixed logout handling...`).
   - Tulis deskripsi perubahan secara langsung, ringkas, dan jelas (contoh yang benar: `- Fixed a case-sensitivity issue in the Amethyst tools countdown timer lore check...`).