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
                telefon TEXT NOT NULL,
                age INTEGER NOT NULL,
                email TEXT
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
            """};

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

    public void rezervasyonEkle(@NotNull Rezervasyon rezervasyon) throws SQLException{
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
        }
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

            System.out.println("Gönderilen Müşteri/Rezervasyon ID: " + musteriId);
            System.out.println("Gönderilen Oda ID: " + rezervasyonId);
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
}
