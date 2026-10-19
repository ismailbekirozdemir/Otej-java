package com.example.otelotomasyon;

import java.sql.SQLException;
import java.time.DateTimeException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // true: Dosyanın üzerine yazmak yerine sonuna ekler (append)
            FileHandler fileHandler = new FileHandler("otel.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.warning("Log dosyası oluşturulamadı!");
        }


        try(Scanner scan = new Scanner(System.in)){
            Sql db = new Sql();
            System.out.println("Veritabanı bağlantısı başarılı ve tablolar hazır.");

            dongu:
            while (true){
                System.out.println("""
                        \n--- Otel Yönetim Paneli---
                        1: Çıkış
                        2: Oda Ekle
                        3: Müşteri Ekle
                        4: Müşteri Misafir Ekle
                        5: Rezervasyon Ekle
                        6: Fatura Göster
                        7: Oda Durum Değiştir
                        Seçiminiz:""");
                try {
                    String secimGirdi = scan.nextLine().trim();
                    if(secimGirdi.isEmpty()){
                        continue ;
                    }
                    int secim = Integer.parseInt(secimGirdi);

                    switch (secim){
                        case 1:
                            break dongu;
                        case 2:
                            odaEklemeEkrani(scan, db);
                            break;
                        case 3:
                            musteriEklemeEkrani(scan, db);
                            break;
                        case 4:
                            int rezId = rezSahibiIdBulma(scan, db);
                            if(rezId == -1) break;
                            System.out.println("Kaç tane misafir eklenecek");
                            int sayac = Integer.parseInt(scan.nextLine());
                            for (int i = 0; i < sayac; i++) {
                                int musteriId = musteriMisafirEklemeEkrani(scan, db);
                                db.musteriMisafirEkle(musteriId, rezId);
                            }
                            break;
                        case 5:
                            rezervasyonEklemeEkrani(scan, db);
                            break;
                        case 6:
                            faturaGoruntulemeEkrani(scan, db);
                        case 7:
                            odaDuzenlemeEkrani(scan, db);
                        default:
                            System.out.println("Geçersiz seçim!");
                    }
                }
                catch (NumberFormatException e){
                System.out.println("Hata: Lütfen geçerli bir sayı girin.");}
                catch (SQLException e) {
                System.out.println("Veritabanı işlemi sırasında hata oluştu: " + e.getMessage());
                logger.log(Level.SEVERE, "SQL Hatası: " + e.getMessage(), e);
                }
            }
        }
        catch (SQLException e){
            System.out.println("Veritabanı başlatılamadı!");
            logger.log(Level.SEVERE, "Veritabanı başlatma hatası: " + e.getMessage(), e);
        }
    }

    public static void musteriEklemeEkrani(Scanner scan, Sql db) throws SQLException{
        System.out.println("Veritabanı hazir.");

        System.out.println("Tc Kimlik numaranız: ");
        String tckimlik = scan.nextLine();
        musteriEklemeEkrani(scan, db, tckimlik);
    }

    public static int musteriEklemeEkrani(Scanner scan, Sql db, String tcKimlik) throws SQLException{
        System.out.println("Adınız: ");
        String ad = scan.nextLine();

        System.out.println("Soyadınız: ");
        String soyad = scan.nextLine();


        System.out.println("Telefon numaranız: ");
        String telefon = scan.nextLine();

        System.out.println("Yaşınız: ");
        int yas = Integer.parseInt(scan.nextLine());

        System.out.println("E-Posta (İsteğe bağlı, geçmek için Enter): ");
        String emailGirdi = scan.nextLine().trim();
        String email =  emailGirdi.isEmpty() ? null : emailGirdi;

        Musteri musteri = new Musteri(ad, soyad, tcKimlik, telefon, yas, email);

        db.musteriEkle(musteri);
        return musteri.getMusteriId();
    }

    public static void odaEklemeEkrani(Scanner scan, Sql db) throws SQLException{
        String odaNo; String odaTip; int odaKapasite; double gunlukUcret;

        System.out.println("Oda Kapasitesi: ");
        odaKapasite = Integer.parseInt(scan.nextLine());

        System.out.println("Oda Numarası: ");
        odaNo = scan.nextLine();

        System.out.println("Oda Tipi: ");
        odaTip = scan.nextLine();

        System.out.println("Odanın günlük ücreti: ");
        gunlukUcret = Double.parseDouble(scan.nextLine());
        Odalar odalar = new Odalar(odaNo, odaTip, odaKapasite, gunlukUcret);

        db.odaEkle(odalar);
    }

    public static int musteriMisafirEklemeEkrani(Scanner scan, Sql db)throws SQLException{
        System.out.println("Tc-Kimlik numaranızı girin: ");
        String tcKimlik = scan.nextLine();

        int musteriId = db.musteriIdGetirTcIle(tcKimlik);
        if(musteriId == -1){
            System.out.println("Müşteri kaydı bulunamamıştır. Müşteri kaydı oluşturuluyor. ");
            musteriId = musteriEklemeEkrani(scan, db, tcKimlik);
        } else {
            System.out.println("Müşteri bulunmuştur: " + musteriId);
        }
        return musteriId;
    }

    public static int rezSahibiIdBulma(Scanner scan, Sql db)throws SQLException{
        System.out.println("Misafir eklenecek odanın rezervasyon sahibi TC'sini girin: ");
        String tcKimlik = scan.nextLine();
        int rezId = db.aktifRezervasyonIdGetirTcIle(tcKimlik);
        if (rezId == -1){
            System.out.println("Bu müşterinin aktif rezervasyonu bulunamadı");
            System.out.println("Tc'nizi veya bilgilerinizi kontrol edin");
        }
        return rezId;
    }

    public static void rezervasyonEklemeEkrani(Scanner scan, Sql db)throws SQLException{
        System.out.println("Tc Kimlik numaranızı girin");
        String tcKimlik = scan.nextLine();
        int musteriId = db.musteriIdGetirTcIle(tcKimlik);

        if(musteriId == -1){
            System.out.println("Müşteri kaydı bulunamadı. Önce müşteri kaydı oluşturuluyor");
            musteriId = musteriEklemeEkrani(scan, db, tcKimlik);
        }

        System.out.println("Kalmak istediğiniz oda numarasını girin");
        int odaId = db.odaIdGetir(scan.nextLine());
        if(odaId == -1){
            System.out.println("Hata: Böyle bir oda bulunamadı!");
            return;
        }

        LocalDate giris = tarihAl(scan, "Giriş Tarihi");
        LocalDate cikis = tarihAl(scan, "Çıkış Tarihi");

        if (!cikis.isAfter(giris)) {
            System.out.println("Hata: Çıkış tarihi, giriş tarihinden sonra olmalıdır!");
            return;
        }

        if(db.odaMusaitMi(odaId, giris, cikis)){
            System.out.println("Oda müsait! Rezervasyon oluşturuluyor");

            double gecelikUcret;
            System.out.println("Gecelik ucreti girin");
            gecelikUcret = Double.parseDouble(scan.nextLine());

            Rezervasyon rezervasyon = new Rezervasyon(musteriId, odaId, giris, cikis, gecelikUcret, false);
            rezervasyon.setRezId(db.rezervasyonEkle(rezervasyon));
            if(rezervasyon.getRezId() != -1) {
                System.out.println("Rezervasyon Eklenemedi!");
                return;
            }
            faturaKesmeEkrani(scan, db, rezervasyon);

        }else{
            System.out.println("Seçilen tarihlerde bu oda dolu!");
        }
    }

    public static LocalDate tarihAl(Scanner scan, String mesaj){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        while (true){
            System.out.println(mesaj + " (Gün.Ay.Yıl, Örn: 19.12.2026)");
            String tarih = scan.nextLine().trim().replace("-", ".").replace("/", ".").replace(" ", ".");
            try {
                return LocalDate.parse(tarih, format);
            }catch (DateTimeException e){
                System.out.println("Hatalı tarih formatı! Gün.Ay.Yıl formatında giriniz");
            }
        }
    }

    public static void faturaKesmeEkrani(Scanner scan, Sql db, Rezervasyon rez)throws SQLException{
        System.out.println("Fatura Sahibi Ünvanını girin(Müşteri için Enter'a basın): ");
        String faturaBasligi = scan.nextLine().trim();
        if(faturaBasligi.isEmpty()){
            faturaBasligi = db.musteriAdSoyadGetir(rez.getMusteriId());
        }

        System.out.println("Kişiler için TcKimlik Şifketler için Vergi Kimlik Numarasını girin: ");
        String vergiNo = scan.nextLine();

        System.out.println("Fatura adresini girin: ");
        String faturaAdresi = scan.nextLine();


        double toplamTutar = rez.getToplamUcret();

        System.out.println("Ödeme Yöntemini girin(Metin Olarak):");
        System.out.println("Nakit\nKredi Kartı\nHavale/EFT");
        String odemeYontemi = scan.nextLine();

        Fatura fatura = new Fatura(rez.getRezId(), faturaBasligi, vergiNo, faturaAdresi, toplamTutar, odemeYontemi);
        int faturaId = db.faturaKes(fatura);

        if (faturaId != -1) {
            System.out.println("Fatura başarıyla kesildi! Fatura No: #" + faturaId);
        } else {
            System.out.println("Hata: Fatura kesilemedi!");
        }
    }

    public static void faturaGoruntulemeEkrani(Scanner scan, Sql db)throws SQLException{
        System.out.println("Fatura sahibinin Tc Kimlik numarasını girin");
        String tcKimlik = scan.nextLine();
        Fatura fatura = db.faturaGoruntule(tcKimlik);
        if(fatura == null){
            System.out.println("Bu Tc Kimlik numarasına ait Fatura yok");
            return;
        }
        System.out.println(fatura.getFaturaId());
        System.out.println(fatura.getRezervasyonId());
        System.out.println(fatura.getFaturaBasligi());
        System.out.println(fatura.getVergiNo());
        System.out.println(fatura.getFaturaAdresi());
        System.out.println(fatura.getToplamTutar());
        System.out.println(fatura.getFaturaTarihi());
        System.out.println(fatura.getOdemeYontemi());
    }

    public static void odaDuzenlemeEkrani(Scanner scan, Sql db)throws SQLException{
        System.out.println("Odanın durumunu seçiniz");
        System.out.println("1:Müşteri giriş yaptı\n2:Temizlik Bekliyor\n3:Müşteri çıkış yaptı\n(Sayı numarasını girin)");
        int secim = Integer.parseInt(scan.nextLine());

        System.out.println("Müşterinin adını girin");
        String ad = scan.nextLine();
        System.out.println("Müşteri soyadını girin");
        String soyad = scan.nextLine();

        String durum;
        switch (secim){
            case 1:
                durum = "Dolu";
                break;
            case 2:
                durum = "Temizlenecek";
                break;
            case 3:
                durum = "Müsait";
                break;
            default:
                System.out.println("Hatalı giriş yaptınız");
                return;

        }

        db.odaDurumDuzenle(durum, ad, soyad);
    }
}