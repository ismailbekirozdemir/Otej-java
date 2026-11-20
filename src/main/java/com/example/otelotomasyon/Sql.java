package com.example.otelotomasyon;

import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Sql {
    private final Path dbPath;
    private final String dbUrl;

    public Sql() throws SQLException {
        this.dbPath = dbYolunuHazirla();
        this.dbUrl = "jdbc:sqlite:" + this.dbPath.toAbsolutePath();
        try (Connection conn = baglantiSagla()) {
            veritabaniOlustur(conn);
        }
    }

    public Path getDbPath() {
        return dbPath;
    }

    private static Path dbYolunuHazirla() throws SQLException {
        String envPath = System.getenv("OTEL_DB_PATH");
        Path hedef = (envPath != null && !envPath.isBlank())
                ? Path.of(envPath.trim())
                : Path.of(System.getProperty("user.home"), "OtelOtomasyon", "Otel.db");
        try {
            if (hedef.getParent() != null) {
                Files.createDirectories(hedef.getParent());
            }
            Path eskiDb = Path.of("Otel.db");
            if (!Files.exists(hedef) && Files.exists(eskiDb)) {
                Files.copy(eskiDb, hedef);
            }
        } catch (IOException e) {
            throw new SQLException("Veritabanı klasörü hazırlanamadı: " + e.getMessage(), e);
        }
        return hedef;
    }

    private Connection baglantiSagla() throws SQLException {
        Connection conn = DriverManager.getConnection(this.dbUrl);
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
                oda_kapasite INTEGER NOT NULL,
                oda_durum TEXT DEFAULT 'Müsait'
            )
            """, """
            CREATE TABLE IF NOT EXISTS musteri (
                musteri_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
                ad TEXT NOT NULL,
                soyad TEXT NOT NULL,
                tc_kimlik TEXT NOT NULL UNIQUE,
                telefon TEXT NOT NULL UNIQUE,
                dogum_tarihi DATE NOT NULL,
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

    private int sonEklenenIdGetir(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public void musteriEkle(@NotNull Musteri musteri) throws SQLException {
        String sql = """
                INSERT INTO musteri (ad, soyad, tc_kimlik, telefon, dogum_tarihi, email)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = baglantiSagla(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, musteri.getAd());
            pstmt.setString(2, musteri.getSoyad());
            pstmt.setString(3, musteri.getTcKimlik());
            pstmt.setString(4, musteri.getTelefon());
            pstmt.setString(5, musteri.getDogumTarihi().toString());
            pstmt.setString(6, musteri.getEmail());

            pstmt.executeUpdate();
            int olusanId = sonEklenenIdGetir(conn);
            if (olusanId > 0) {
                musteri.setMusteriId(olusanId);
            }
        }
    }

    public void odaEkle(@NotNull Odalar odalar) throws SQLException {
        String sql = """
                INSERT INTO odalar(oda_no, oda_kapasite)
                VALUES(?, ?)
                """;
        try (Connection conn = baglantiSagla(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, odalar.getOda_no());
            pstmt.setInt(2, odalar.getOda_kapasite());

            pstmt.executeUpdate();
            int olusanId = sonEklenenIdGetir(conn);
            if (olusanId > 0) {
                odalar.setOdaId(olusanId);
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
            PreparedStatement pstmtm = conn.prepareStatement(sql)){

            pstmtm.setInt(1, rezervasyon.getMusteriId());
            pstmtm.setInt(2, rezervasyon.getOdaId());
            pstmtm.setString(3, rezervasyon.getGirisTarihi().toString());
            pstmtm.setString(4, rezervasyon.getCikisTarihi().toString());
            pstmtm.setDouble(5, rezervasyon.getToplamUcret());
            pstmtm.setBoolean(6, rezervasyon.isOdendiMi());

            pstmtm.executeUpdate();
            int olusanRezId = sonEklenenIdGetir(conn);
            if (olusanRezId > 0) {
                rezervasyon.setRezId(olusanRezId);
            }
            return olusanRezId;
        }
    }

    public boolean odaMusaitMi(int odaId, LocalDate yeniGiris, LocalDate yeniCikis) throws SQLException {
        return odaMusaitMi(odaId, yeniGiris, yeniCikis, -1);
    }

    public boolean odaMusaitMi(int odaId, LocalDate yeniGiris, LocalDate yeniCikis, int haricTutulacakRezId) throws SQLException{
        String sql = """
                SELECT COUNT(*) FROM rezervasyon
                WHERE oda_id = ?
                    AND durum NOT IN ('İptal', 'Iptal')
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
              AND r.durum NOT IN ('İptal', 'Iptal')
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
                  AND r.durum NOT IN ('İptal', 'Iptal')
                ORDER BY r.rezervasyon_id DESC
                LIMIT 1
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, telefon);
            try(ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    LocalDate giris = LocalDate.parse(rs.getString("giris_tarihi").substring(0, 10));
                    LocalDate cikis = LocalDate.parse(rs.getString("cikis_tarihi").substring(0, 10));

                    long gunSayisi = Math.max(1, ChronoUnit.DAYS.between(giris, cikis));
                    double toplamUcret = rs.getDouble("toplam_ucret");
                    double gecelikUcret = toplamUcret / gunSayisi;

                    Rezervasyon rez = new Rezervasyon(
                            rs.getInt("musteri_id"),
                            rs.getInt("oda_id"),
                            giris,
                            cikis,
                            gecelikUcret,
                            rs.getBoolean("odendi_mi")
                            );
                    rez.setRezId(rs.getInt("rezervasyon_id"));
                    rez.setDurum(rs.getString("durum"));
                    return rez;
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fatura.getRezervasyonId());
            pstmt.setString(2, fatura.getFaturaBasligi());
            pstmt.setString(3, fatura.getVergiNo());
            pstmt.setString(4, fatura.getFaturaAdresi());
            pstmt.setDouble(5, fatura.getToplamTutar());
            pstmt.setString(6, fatura.getOdemeYontemi());

            pstmt.executeUpdate();

            int olusanFaturaId = sonEklenenIdGetir(conn);
            if (olusanFaturaId > 0) {
                fatura.setFaturaId(olusanFaturaId);
            }
            return olusanFaturaId;
        }
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
                    String tarihStr = rs.getString("fatura_tarihi");
                    LocalDate faturaTarihi = (tarihStr != null && tarihStr.length() >= 10)
                            ? LocalDate.parse(tarihStr.substring(0, 10))
                            : LocalDate.now();
                    return new Fatura(
                            rs.getInt("fatura_id"),
                            rs.getInt("rezervasyon_id"),
                            rs.getString("fatura_basligi"),
                            rs.getString("vergi_no"),
                            rs.getString("fatura_adresi"),
                            rs.getDouble("toplam_tutar"),
                            rs.getString("odeme_yontemi"),
                            faturaTarihi
                            );
                }
            }
        }
        return null;
    }

    public boolean odaDurumDuzenle(String durum, String tcKimlik)throws SQLException{
        String sql = """
                UPDATE odalar
                SET oda_durum = ?
                FROM rezervasyon r
                JOIN musteri m ON r.musteri_id = m.musteri_id
                WHERE odalar.oda_id = r.oda_id
                    AND m.tc_kimlik = ?
                    AND r.durum NOT IN ('İptal', 'Iptal')
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, durum);
            pstmt.setString(2, tcKimlik);

            int etkilenenSatir = pstmt.executeUpdate();
            return etkilenenSatir > 0;
        }
     }

    public boolean odaDurumDuzenleOdaNoIle(String durum, String odaNo) throws SQLException {
        String sql = "UPDATE odalar SET oda_durum = ? WHERE oda_no = ?";
        try (Connection conn = baglantiSagla();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, durum);
            pstmt.setString(2, odaNo);
            return pstmt.executeUpdate() > 0;
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

    public boolean temizlikTamamlandi(String odaNo)throws SQLException{
        String sql = """
                UPDATE odalar
                SET oda_durum = 'Müsait'
                WHERE oda_no = ?
                """;
        int etkilenen = 0;
        try(Connection conn = baglantiSagla();
            PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, odaNo);
            etkilenen = pstmt.executeUpdate();
            if(etkilenen == 0){
                System.out.println("Girdiğiniz oda numarasına sahip oda yok");
            }
        }
        return etkilenen > 0;
    }

    public void gecikmisCikislariKapat() throws SQLException {
        String sqlRez = """
            UPDATE rezervasyon
            SET durum = 'Tamamlandı'
            WHERE durum = 'Aktif' AND cikis_tarihi < date('now', 'localtime')
            """;

        String sqlOda = """
            UPDATE odalar
            SET oda_durum = 'Temizlenecek'
            WHERE oda_id IN (
                SELECT oda_id FROM rezervasyon
                WHERE durum = 'Tamamlandı' AND cikis_tarihi < date('now', 'localtime')
            ) AND oda_durum = 'Dolu'
            """;

        try (Connection conn = baglantiSagla();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sqlRez);
            st.executeUpdate(sqlOda);
        }
    }

    public boolean rezIptal(String telefon) throws SQLException {
        String sqlRezBul = """
                SELECT r.rezervasyon_id, r.oda_id
                FROM rezervasyon r
                JOIN musteri m ON r.musteri_id = m.musteri_id
                WHERE m.telefon = ?
                  AND r.durum NOT IN ('İptal', 'Iptal', 'Tamamlandı')
                ORDER BY r.rezervasyon_id DESC
                LIMIT 1
                """;
        String sqlIptal = """
                UPDATE rezervasyon
                SET durum = 'İptal'
                WHERE rezervasyon_id = ?
                """;
        String sqlOdaBosalt = """
                UPDATE odalar
                SET oda_durum = 'Müsait'
                WHERE oda_id = ?
                  AND oda_durum = 'Dolu'
                  AND NOT EXISTS (
                      SELECT 1 FROM rezervasyon
                      WHERE oda_id = ? AND durum = 'Aktif'
                  )
                """;
        try (Connection conn = baglantiSagla()) {
            conn.setAutoCommit(false);
            try {
                int rezId = -1;
                int odaId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(sqlRezBul)) {
                    pstmt.setString(1, telefon);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            rezId = rs.getInt("rezervasyon_id");
                            odaId = rs.getInt("oda_id");
                        }
                    }
                }

                if (rezId == -1) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement pstmtIptal = conn.prepareStatement(sqlIptal)) {
                    pstmtIptal.setInt(1, rezId);
                    pstmtIptal.executeUpdate();
                }

                try (PreparedStatement pstmtOda = conn.prepareStatement(sqlOdaBosalt)) {
                    pstmtOda.setInt(1, odaId);
                    pstmtOda.setInt(2, odaId);
                    pstmtOda.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public int odendiMi(String telefon)throws SQLException{
        String sql = """
                SELECT r.odendi_mi
                FROM rezervasyon r
                JOIN musteri m ON r.musteri_id = m.musteri_id
                WHERE m.telefon = ?
                  AND r.durum NOT IN ('İptal', 'Iptal')
                ORDER BY r.rezervasyon_id DESC
                LIMIT 1
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, telefon);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return rs.getInt("odendi_mi");
                }
            }
        }
        return 0;
    }

    public int borcOde(String telefon)throws SQLException{
        String sql = """
                UPDATE rezervasyon
                SET odendi_mi = 1
                WHERE musteri_id = (
                    SELECT musteri_id
                    FROM musteri
                    WHERE telefon = ?
                )
                AND durum NOT IN ('İptal', 'Iptal')
                AND odendi_mi = 0
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, telefon);
            return ps.executeUpdate();
        }
    }

    public List<String> borcGoruntule(String telefon)throws SQLException{
        String sql = """
                SELECT r.toplam_ucret, r.rezervasyon_id,
                       m.ad, m.soyad
                FROM rezervasyon r
                JOIN musteri m ON m.musteri_id = r.musteri_id
                WHERE m.telefon = ?
                    AND r.durum NOT IN ('İptal', 'Iptal')
                    AND r.odendi_mi = 0
                """;

        List<String> borcList = new ArrayList<>();

        try(Connection conn = baglantiSagla();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, telefon);
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()) {
                    int rezId = rs.getInt("rezervasyon_id");
                    String adSoyad = rs.getString("ad") + " " + rs.getString("soyad");
                    double ucret = rs.getDouble("toplam_ucret");

                    String mesaj = rezId + " Numaralı rezervasyonun sahibi " + adSoyad +
                            " adlı müşterinin " + ucret + " Tl kadar borcu vardır";
                    borcList.add(mesaj);
                }
            }
        }
        return borcList;
    }

    // =========================================================================
    // ANA EKRAN (DASHBOARD) VE LİSTELER İÇİN VERİTABANI OKUMA METODLARI
    // =========================================================================

    public List<Odalar> tumOdalariGetir() throws SQLException {
        String sql = "SELECT oda_id, oda_no, oda_kapasite, oda_durum FROM odalar ORDER BY oda_no ASC";
        List<Odalar> liste = new ArrayList<>();
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                liste.add(new Odalar(
                        rs.getInt("oda_id"),
                        rs.getString("oda_no"),
                        rs.getInt("oda_kapasite"),
                        rs.getString("oda_durum")
                ));
            }
        }
        return liste;
    }

    public int bugunGirisYapacakRezSayisi() throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM rezervasyon
                WHERE giris_tarihi = date('now', 'localtime')
                  AND durum = 'Onaylandi'
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int bugunGirisYapacakInsanSayisi() throws SQLException {
        String sql = """
                SELECT COUNT(r.rezervasyon_id) + COUNT(rm.musteri_id)
                FROM rezervasyon r
                LEFT JOIN rezervasyon_misafir rm ON r.rezervasyon_id = rm.rezervasyon_id
                WHERE r.giris_tarihi = date('now', 'localtime')
                  AND r.durum = 'Onaylandi'
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int bugunCikisYapacakOdaSayisi() throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM rezervasyon
                WHERE cikis_tarihi = date('now', 'localtime')
                  AND durum = 'Aktif'
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int bugunCikisYapacakInsanSayisi() throws SQLException {
        String sql = """
                SELECT COUNT(r.rezervasyon_id) + COUNT(rm.musteri_id)
                FROM rezervasyon r
                LEFT JOIN rezervasyon_misafir rm ON r.rezervasyon_id = rm.rezervasyon_id
                WHERE r.cikis_tarihi = date('now', 'localtime')
                  AND r.durum = 'Aktif'
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double toplamOdenenCiroGetir() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(toplam_ucret), 0)
                FROM rezervasyon
                WHERE odendi_mi = 1
                  AND durum != 'İptal' AND durum != 'Iptal'
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double bekleyenToplamBorcGetir() throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(toplam_ucret), 0)
                FROM rezervasyon
                WHERE odendi_mi = 0
                  AND durum != 'İptal' AND durum != 'Iptal'
                """;
        try (Connection conn = baglantiSagla();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public String musteriTcGetirMusteriIdIle(int musteriId)throws SQLException{
        String sql = """
                SELECT tc_kimlik
                FROM musteri
                WHERE musteri_id = ?
                """;

        try(Connection conn = baglantiSagla();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, musteriId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tc_kimlik");
                }
            }
        }
        return null;
    }

    public int odaNoGetirIdIle(int odaId)throws SQLException{
        String sql = """
                SELECT oda_no
                FROM odalar
                WHERE oda_id = ?
                """;
        try(Connection conn = baglantiSagla();
            PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, odaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("oda_no");
                }
            }
        }
        return -1;
    }
}