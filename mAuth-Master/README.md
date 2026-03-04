# mAuth (Edited LimboAuth)

**mAuth** is a highly optimized and edited version of the original LimboAuth, customized by **rushcik**. This version focuses on performance, clean code, and improved database management.

---

## 🇺🇸 English

### 🔄 What's New? (Changes from LimboAuth)
- **Project Renaming**: Completely rebranded from LimboAuth to **mAuth**.
- **Package Update**: All packages moved to `dev.rushcik.mauth` for a unique identifier.
- **Database (MySQL) Overhaul**:
  - **MySQL is now the default** storage type.
  - Added **Connection Pooling** (`MYSQL_POOL`) for better resource management.
  - Optimized JDBC parameters: UTF-8 encoding, UTC timezone, and batching support.
  - Improved logging: Clear indicators of connection status and pool settings on startup.
- **Code Optimization**:
  - Removed all license headers and single-line comments for a cleaner codebase.
  - Trimmed unnecessary leading/trailing blank lines in source files.
- **Maintenance**: Updated update checking to point to the new repository.

### 🚀 Features
- **Virtual Server (Limbo)**: Auth system works without a backend server during login.
- **BCrypt Hashing**: industry-standard security for passwords.
- **Hybrid Support**: Works with Online, Offline, and Floodgate (Bedrock) players.
- **2FA Support**: Built-in TOTP support for extra security.

### 🛠 Commands & Permissions
- `/mauth` - Main command (`mauth.admin.help`)
- `/mauth reload` - Reload configuration (`mauth.admin.reload`)
- `/login <password>` - Standard login
- `/register <password> <repeat>` - Standard registration

---

## 🇹🇷 Türkçe

### 🔄 Yenilikler neler? (LimboAuth'dan Farklar)
- **Proje Yeniden Adlandırma**: Tamamen LimboAuth'dan **mAuth**'a dönüştürüldü.
- **Paket Güncellemesi**: Tüm paket yapısı `dev.rushcik.mauth` olarak güncellendi.
- **Veritabanı (MySQL) İyileştirmeleri**:
  - **MySQL artık varsayılan** depolama türüdür.
  - Daha iyi kaynak yönetimi için **Bağlantı Havuzu** (`MYSQL_POOL`) eklendi.
  - Optimize edilmiş JDBC parametreleri: UTF-8 kodlama, UTC zaman dilimi ve toplu işlem desteği.
  - Gelişmiş Loglama: Başlangıçta bağlantı durumu ve havuz ayarları net bir şekilde belirtilir.
- **Kod Optimizasyonu**:
  - Daha temiz bir kod yapısı için tüm lisans başlıkları ve tek satırlık yorumlar kaldırıldı.
  - Gereksiz boş satırlar temizlendi.
- **Güncelleme Kontrolü**: Güncelleme kontrol sistemi yeni depoya yönlendirildi.

### 🚀 Özellikler
- **Sanal Sunucu (Limbo)**: Giriş sırasında backend sunucusuna ihtiyaç duymadan çalışır.
- **BCrypt Hashing**: Parolalar için endüstri standardı güvenlik.
- **Hibrit Destek**: Online, Offline ve Floodgate (Bedrock) oyuncularıyla uyumlu.
- **2FA Desteği**: Ekstra güvenlik için yerleşik TOTP desteği.

### 🛠 Komutlar ve Yetkiler
- `/mauth` - Ana komut (`mauth.admin.help`)
- `/mauth reload` - Yapılandırmayı yeniler (`mauth.admin.reload`)
- `/login <şifre>` - Giriş yapma
- `/register <şifre> <şifre>` - Kayıt olma

---

### 📝 Credits
Original project by Elytrium. Edited and Improved by **rushcik**.
