package com.example.otelotomasyon;

import org.jetbrains.annotations.NotNull;
import java.sql.*;
import java.time.LocalDate;

public class Sql {
    public Sql() throws SQLException {
        try (Connection conn = baglantiSagla()) {
            veritabaniOlustur(conn);
        }
    }

    private Connection baglantiSagla() throws SQLException {
        final String url = "jdbc:sqlite:Otel.db";
        Connection conn = DriverManager.getConnection(url);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conn.close();   // PRAGMA patlarsa bağlantı sızmasın
            throw e;
        }
        return conn;
    }

    private void veritabaniOlustur(Connection conn) throws SQLException {
        String[] tablolar = {"""
            CREATE TABLE IF NOT EXISTS odalar (
                oda_id INTEGER PRIMARY KEY AUTOINCREMENT,
                oda_no TEXT NOT NULL,
                oda_tip TEXT NOT NULL,
                oda_kapasite INTEGER NOT NULL,
                gunluk_ucret REAL NOT NULL,
                oda_durum TEXT DEFAULT 'Müsait'
            )
            """, """
            CREATE TABLE IF NOT EXISTS musteri (
                musteri_id INTEGER PRIMARY KEY AUTOINCREMENT,
                ad TEXT NOT NULL,
                soyad TEXT NOT NULL,
                tc_kimlik TEXT NOT NULL UNIQUE,
                telefon TEXT NOT NULL UNIQUE,
                age INTEGER NOT NULL,
                email TEXT UNIQUE
            )
            """, """
            CREATE TABLE IF NOT EXISTS rezervasyon (
                rezervasyon_id INTEGER PRIMARY KEY AUTOINCREMENT,
                musteri_id INTEGER NOT NULL,
                oda_id INTEGER NOT NULL,
                islem_tarihi DATETIME DEFAULT CURRENT_TIMESTAMP,
                giris_tarihi DATE NOT NULL,
                cikis_tarihi DATE NOT NULL,
                toplam_ucret REAL NOT NULL,
                odendi_mi INTEGER DEFAULT 0,
                durum TEXT DEFAULT 'Onaylandi',
                FOREIGN KEY (musteri_id) REFERENCES musteri (musteri_id) ON DELETE RESTRICT,
                FOREIGN KEY (oda_id) REFERENCES odalar (oda_id) ON DELETE RESTRICT
            )
            """, """
            CREATE TABLE IF NOT EXISTS rezervasyon_misafir (
                rezervasyon_id INTEGER NOT NULL,
                musteri_id INTEGER NOT NULL,
                PRIMARY KEY (rezervasyon_id, musteri_id),
                FOREIGN KEY (rezervasyon_id) REFERENCES rezervasyon (rezervasyon_id) ON DELETE CASCADE,
                FOREIGN KEY (musteri_id) REFERENCES musteri (musteri_id) ON DELETE RESTRICT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS fatura(
                fatura_id INTEGER PRIMARY KEY AUTOINCREMENT,
                rezervasyon_id INTEGER NOT NULL UNIQUE,
                fatura_basligi TEXT NOT NULL,
                vergi_no TEXT,
                fatura_adresi TEXT NOT NULL,
                toplam_tutar REAL NOT NULL,
                fatura_tarihi DATETIME DEFAULT CURRENT_TIMESTAMP,
                odeme_yontemi TEXT DEFAULT 'Nakit',
                FOREIGN KEY (rezervasyon_id) REFERENCES rezervasyon (rezervasyon_id) ON DELETE RESTRICT
            )
            """
        };

        try (Statement st = conn.createStatement()) {
            for (String sql : tablolar) {
                st.execute(sql);
            }
        }
    }

    public void musteriEkle(@NotNull Musteri musteri) throws SQLException {
        String sql = """
                INSERT INTO musteri (ad, soyad, tc_kimlik, telefon, age, email)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = baglantiSagla(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, musteri.getAd());
            pstmt.setString(2, musteri.getSoyad());
            pstmt.setString(3, musteri.getTcKimlik());
            pstmt.setString(4, musteri.getTelefon());
            pstmt.setInt(5, musteri.getYas());
            pstmt.setString(6, musteri.getEmail());

            pstmt.executeUpdate();

            try(ResultSet rs = pstmt.getGeneratedKeys()){
                if(rs.next()){
                    int olusanId = rs.getInt(1);
                    musteri.setMusteriId(olusanId);
                }
            }
        }
    }

    public void odaEkle(@NotNull Odalar odalar) throws SQLException {
        String sql = """
                INSERT INTO odalar(oda_no, oda_tip, oda_kapasite, gunluk_ucret)
                VALUES(?, ?, ?, ?)
                """;
        try (Connection conn = baglantiSagla(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, odalar.getOda_no());
            pstmt.setString(2, odalar.getOda_tip());
            pstmt.setInt(3, odalar.getOda_kapasite());
            pstmt.setDouble(4, odalar.getGunluk_ucret());

            pstmt.executeUpdate();

            try(ResultSet rs = pstmt.getGeneratedKeys()){
                if(rs.next()){
                    odalar.setOdaId(rs.getInt(1));
                }
            }
        }
    }

    public int rezervasyonEkle(@NotNull Rezervasyon rezervasyon) throws SQLException{
        String sql = """
                INSERT INTO rezervasyon(musteri_id, oda_id,
                    giris_tarihi, cikis_tarihi, toplam_ucret, odendi_mi)
                VALUES(?, ?, ?, ?, ?, ?)
                """;

        try(Connection conn = baglantiSagla();
            PreparedStatement pstmtm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){

            pstmtm.setInt(1, rezervasyon.getMusteriId());
            pstmtm.setInt(2, rezervasyon.getOdaId());
            pstmtm.setString(3, rezervasyon.getGirisTarihi().toString());
            pstmtm.setString(4, rezervasyon.getCikisTarihi().toString());
            pstmtm.setDouble(5, rezervasyon.getToplamUcret());
            pstmtm.setBoolean(6, rezervasyon.isOdendiMi());

            pstmtm.executeUpdate();

            try(ResultSet rs = pstmtm.getGeneratedKeys()){
                if(rs.next()){
                    int olusanRezId = rs.getInt(1);
                    rezervasyon.setRezId(olusanRezId);
                    return olusanRezId;
                }
            }
        }
        return -1;
    }

    public boolean odaMusaitMi(int odaId, LocalDate yeniGiris, LocalDate yeniCikis) throws SQLException {
        return odaMusaitMi(odaId, yeniGiris, yeniCikis, -1);
    }

    public boolean odaMusaitMi(int odaId, LocalDate yeniGiris, LocalDate yeniCikis, int haricTutulacakRezId) throws SQLException{
        String sql = """
                SELECT COUNT(*) FROM rezervasyon
                WHERE oda_id = ?
                    AND durum != 'Iptal'
                    AND rezervasyon_id != ?
                    AND giris_tarihi < ?
                    AND cikis_tarihi > ?
                """;

        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setInt(1, odaId);
            pstmt.setInt(2, haricTutulacakRezId);
            pstmt.setString(3, yeniCikis.toString());
            pstmt.setString(4, yeniGiris.toString());

            try(ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int cakisanKayitSayisi = rs.getInt(1);
                    return cakisanKayitSayisi == 0;
                }
            }
        }

        return false;
    }

    public void musteriMisafirEkle(int musteriId, int rezervasyonId) throws SQLException{
        String sql = """
                INSERT INTO rezervasyon_misafir(rezervasyon_id, musteri_id)
                VALUES(?, ?)
                """;

        try(Connection conn = baglantiSagla();
            PreparedStatement pstmtm = conn.prepareStatement(sql)){

            pstmtm.setInt(1, rezervasyonId);
            pstmtm.setInt(2, musteriId);
            pstmtm.executeUpdate();
        }
    }

    public int musteriIdGetirTcIle(String tcKimlik) throws SQLException{
        String sql = "SELECT musteri_id FROM musteri WHERE tc_kimlik = ?";
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, tcKimlik);
            try (ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getInt("musteri_id");
                }
            }
        }
        return -1;
    }

    public int aktifRezervasyonIdGetirTcIle(String tcKimlik) throws SQLException {
        String sql = """
            SELECT r.rezervasyon_id
            FROM rezervasyon r
            JOIN musteri m ON r.musteri_id = m.musteri_id
            WHERE m.tc_kimlik = ?
              AND r.durum != 'Iptal'
            ORDER BY r.giris_tarihi DESC
            LIMIT 1
            """;

        try (Connection conn = baglantiSagla();
             PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, tcKimlik);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rezervasyon_id");
                }
            }
        }
        return -1;
    }

    public Rezervasyon rezervasyonGetirTelefonIle(String telefon)throws SQLException{
        String sql = """
                SELECT r.*
                FROM rezervasyon r
                JOIN musteri m ON r.musteri_id = m.musteri_id
                WHERE m.telefon = ?
                ORDER BY r.rezervasyon_id DESC
                LIMIT 1
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, telefon);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    LocalDate giris = rs.getObject("giris_tarihi", LocalDate.class);
                    LocalDate cikis = rs.getObject("cikis_tarihi", LocalDate.class);

                    return new Rezervasyon(
                            rs.getInt("musteri_id"),
                            rs.getInt("oda_id"),
                            giris,
                            cikis,
                            rs.getDouble("gecelik_ucret"),
                            rs.getBoolean("odendi_mi")
                            );
                }
            }
        }
        return null;
    }

    public int odaIdGetir(String odaNo)throws SQLException{
        String sql = "SELECT oda_id FROM odalar WHERE oda_no = ?";
        try (Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){

            pstmt.setString(1, odaNo);
            try (ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public int faturaKes(Fatura fatura)throws SQLException{
        String sql = """
                INSERT INTO fatura(
                    rezervasyon_id,
                    fatura_basligi,
                    vergi_no,
                    fatura_adresi,
                    toplam_tutar,
                    odeme_yontemi
                )
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, fatura.getRezervasyonId());
            pstmt.setString(2, fatura.getFaturaBasligi());
            pstmt.setString(3, fatura.getVergiNo());
            pstmt.setString(4, fatura.getFaturaAdresi());
            pstmt.setDouble(5, fatura.getToplamTutar());
            pstmt.setString(6, fatura.getOdemeYontemi());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int olusanFaturaId = rs.getInt(1);
                    fatura.setFaturaId(olusanFaturaId);
                    return olusanFaturaId;
                }
            }
        }
        return -1;
    }

    public double rezToplamUcretGetir(int rezId)throws SQLException{
        String sql = "SELECT toplam_ucret FROM rezervasyon WHERE rezervasyon_id = ?";
        try (Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, rezId);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getDouble("toplam_ucret");
                }
            }
        }
        return 0.0;
    }

    public String musteriAdSoyadGetir(int musteriId)throws SQLException{
        String sql = "SELECT ad, soyad FROM musteri WHERE musteri_id = ?";
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, musteriId);

            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return rs.getString("ad") + " " + rs.getString("soyad");
                }
            }
        }
        return "";
    }

    public Fatura faturaGoruntule(String telefon)throws SQLException{
        String sql = """
            SELECT f.*
            FROM fatura f
            JOIN rezervasyon r ON f.rezervasyon_id = r.rezervasyon_id
            JOIN musteri m ON r.musteri_id = m.musteri_id
            WHERE m.telefon = ?
            ORDER BY f.fatura_id DESC
            LIMIT 1
            """;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, telefon);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    return new Fatura(
                            rs.getInt("fatura_id"),
                            rs.getInt("rezervasyon_id"),
                            rs.getString("fatura_basligi"),
                            rs.getString("vergi_no"),
                            rs.getString("fatura_adresi"),
                            rs.getDouble("toplam_tutar"),
                            rs.getString("fatura_tarihi"),
                            rs.getString("odeme_yontemi")
                            );
                }
            }
        }
        return null;
    }

    public void odaDurumDuzenle(String durum, String tcKimlik)throws SQLException{
        String sql = """
                UPDATE odalar
                SET oda_durum = ?
                FROM rezervasyon r
                JOIN musteri m ON r.musteri_id = m.musteri_id
                WHERE odalar.oda_id = r.oda_id
                    AND m.tc_kimlik = ?
                    AND r.durum != 'Iptal'
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, durum);
            pstmt.setString(2, tcKimlik);

            int etkilenenSatir = pstmt.executeUpdate();

            if(etkilenenSatir > 0){
                System.out.println("Oda durumu güncellendi.");
            }
            else {
                System.out.println("Müşteriye ait rezervasyon bulunamadı.");
            }
        }
     }

    public void odaDurumDuzenle(String durum, int rezId)throws SQLException{
        String sql = """
                UPDATE odalar
                SET oda_durum = ?
                WHERE oda_id = (
                    SELECT oda_id
                    FROM rezervasyon
                    WHERE rezervasyon_id = ?
                    )
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, durum);
            pstmt.setInt(2, rezId);

            int etkilenenSatir = pstmt.executeUpdate();

            if(etkilenenSatir > 0){
                System.out.println("Oda durumu güncellendi.");
            }
            else {
                System.out.println("Müşteriye ait rezervasyon bulunamadı.");
            }
        }
    }

    public boolean musteriGirisYapti(String telefon)throws SQLException {
        String sql = """
                UPDATE rezervasyon
                SET durum = 'Aktif'
                WHERE rezervasyon_id = (
                    SELECT r.rezervasyon_id
                    FROM rezervasyon r
                    JOIN musteri m ON r.musteri_id = m.musteri_id
                    WHERE m.telefon = ? AND r.durum = 'Onaylandi'
                    ORDER BY r.rezervasyon_id DESC
                    LIMIT 1
                )
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, telefon);
            int etkilenen = pstmt.executeUpdate();

            if (etkilenen > 0) {
                String odaSql = """
                        UPDATE odalar
                        SET oda_durum = 'Dolu'
                        WHERE oda_id = (
                            SELECT r.oda_id
                            FROM rezervasyon r
                            JOIN musteri m ON r.musteri_id = m.musteri_id
                            WHERE m.telefon = ? AND r.durum = 'Aktif'
                            ORDER BY r.rezervasyon_id DESC
                            LIMIT 1
                        )
                        """;
                try (PreparedStatement pstmtOda = conn.prepareStatement(odaSql)) {
                    pstmtOda.setString(1, telefon);
                    pstmtOda.executeUpdate();
                }
                return true;
            }
            return false;
        }
    }

    public boolean musteriCikisYapti(String telefon)throws SQLException{
        String sqlRezervasyon = """
                UPDATE rezervasyon
                SET durum = 'Tamamlandı'
                FROM musteri m
                WHERE rezervasyon.musteri_id = m.musteri_id
                    AND m.telefon = ?
                    AND rezervasyon.durum = 'Aktif'
                """;
        String sqlOda = """
                UPDATE odalar
                SET oda_durum = 'Temizlenecek'
                FROM rezervasyon r
                JOIN musteri m ON r.musteri_id = m.musteri_id
                WHERE odalar.oda_id = r.oda_id
                    AND m.telefon = ?
                    AND r.durum = 'Tamamlandı'
                """;

        try(Connection conn = baglantiSagla()){
            conn.setAutoCommit(false);
            try {
                int etkilenenRez;
                try(PreparedStatement pstmt = conn.prepareStatement(sqlRezervasyon)){
                    pstmt.setString(1, telefon);
                    etkilenenRez = pstmt.executeUpdate();
                }

                if(etkilenenRez > 0){
                    try(PreparedStatement pstmt = conn.prepareStatement(sqlOda)){
                        pstmt.setString(1, telefon);
                        pstmt.executeUpdate();
                    }
                    conn.commit();
                    return true;
                }
                else{
                    conn.rollback();
                    return false;
                }
            }catch (SQLException e){
                conn.rollback();
                throw e;
            }
            finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void temizlikTamamlandi(String odaNo)throws SQLException{
        String sql = """
                UPDATE odalar
                SET oda_durum = 'Müsait'
                WHERE oda_no = ?
                """;

        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, odaNo);
            int etkilenen = pstmt.executeUpdate();
            if(etkilenen == 0){
                System.out.println("Girdiğiniz oda numarasına sahip oda yok");
            }
        }
    }

    public void gecikmisCikislariKapat() throws SQLException {
        String sqlRez = """
            UPDATE rezervasyon
            SET durum = 'Tamamlandı'
            WHERE durum = 'Aktif' AND cikis_tarihi < date('now')
            """;

        String sqlOda = """
            UPDATE odalar
            SET oda_durum = 'Temizlenecek'
            WHERE oda_id IN (
                SELECT oda_id FROM rezervasyon
                WHERE durum = 'Tamamlandı' AND cikis_tarihi < date('now')
            ) AND oda_durum = 'Dolu'
            """;

        try (Connection conn = baglantiSagla();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sqlRez);
            st.executeUpdate(sqlOda);
        }
    }

    //Faturaya giriş çıkış her ikisi içinde seçenek ayarla ve çıkarken ödendimi kontrolü yap
}
