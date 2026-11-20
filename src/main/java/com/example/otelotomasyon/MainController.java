package com.example.otelotomasyon;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Otel Otomasyon - Ana Arayüz Kontrolcüsü (UI Mimari Katmanı)
 *
 * Ekranlar arası geçişler, tema yönetimi (Light / Dark Mode) ve ana ekrandaki
 * dinamik oda kartı temsili oluşturucu metodlar hazırdır.
 * İşlem butonlarının (Oda Ekle, Müşteri Ekle, Rezervasyon, Fatura vb.) @FXML
 * metod gövdeleri sırayla veritabanı (Sql.java) işlemleri bağlanmak üzere boş bırakılmıştır.
 */
public class MainController {
    private int seciliMisafirRezID = -1;
    private int seciliMisafirId = -1;
    // =========================================================================
    // KÖK PANEL VE ÜST BAR (SHELL & TOPBAR)
    // =========================================================================
    @FXML private BorderPane rootPane;
    @FXML private StackPane contentStack;
    @FXML private Label lblTopbarTitle;
    @FXML private Label lblTopbarSubtitle;
    @FXML private Label lblDbBaglantiDurumu;
    @FXML private Label lblGununTarihi;
    @FXML private Button btnThemeToggle;
    @FXML private Button btnUygulamaCikis;

    // =========================================================================
    // SOL NAVİGASYON BUTONLARI (SIDEBAR BUTTON BAR)
    // =========================================================================
    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavMusteriGiris;
    @FXML private Button btnNavMusteriCikis;
    @FXML private Button btnNavRezervasyonEkle;
    @FXML private Button btnNavRezervasyonIptal;
    @FXML private Button btnNavMusteriEkle;
    @FXML private Button btnNavMisafirEkle;
    @FXML private Button btnNavOdaEkle;
    @FXML private Button btnNavOdaDuzenle;
    @FXML private Button btnNavOdaTemizlendi;
    @FXML private Button btnNavFaturaGoster;
    @FXML private Button btnNavBorcIslemleri;

    // =========================================================================
    // EKRAN GÖRÜNÜMLERİ (12 AYRI EKRAN PANE'İ)
    // =========================================================================
    @FXML private ScrollPane viewDashboard;
    @FXML private ScrollPane viewOdaEkle;
    @FXML private ScrollPane viewMusteriEkle;
    @FXML private ScrollPane viewMisafirEkle;
    @FXML private ScrollPane viewRezervasyonEkle;
    @FXML private ScrollPane viewFaturaGoster;
    @FXML private ScrollPane viewOdaDuzenle;
    @FXML private ScrollPane viewMusteriGiris;
    @FXML private ScrollPane viewMusteriCikis;
    @FXML private ScrollPane viewOdaTemizlendi;
    @FXML private ScrollPane viewRezervasyonIptal;
    @FXML private ScrollPane viewBorcIslemleri;

    // =========================================================================
    // 1. ANA EKRAN (DASHBOARD) DİNAMİK BİLEŞENLERİ (DB'DEN ÇEKİLMEK ÜZERE)
    // =========================================================================
    @FXML private Label lblDolulukYuzdeBadge;
    @FXML private Label lblDoluOdaSayisi;
    @FXML private Label lblBosOdaSayisi;
    @FXML private ProgressBar prgDolulukOrani;
    @FXML private Label lblToplamOdaMeta;

    @FXML private Label lblGunlukGirisInsanSayisi;
    @FXML private Label lblGunlukGirisOdaSayisi;
    @FXML private Label lblGunlukGirisDetay;

    @FXML private Label lblGunlukCikisInsanSayisi;
    @FXML private Label lblGunlukCikisOdaSayisi;
    @FXML private Label lblGunlukCikisDetay;

    @FXML private Label lblTemizlenecekOdaSayisi;
    @FXML private Label lblTemizlenecekDetay;

    @FXML private Label lblBakimYapilacakSayisi;
    @FXML private Label lblBakimDetay;

    @FXML private Label lblGunlukCiro;
    @FXML private Label lblBekleyenBorcToplam;

    @FXML private TextField txtDashboardOdaAra;
    @FXML private Button btnDashboardVeriYenile;
    @FXML private Button btnFiltreTumu;
    @FXML private Button btnFiltreMusait;
    @FXML private Button btnFiltreDolu;
    @FXML private Button btnFiltreTemizlenecek;
    @FXML private Button btnFiltreBakimda;
    @FXML private FlowPane flowOdaTemsilleri;

    // =========================================================================
    // 2. ODA EKLEME EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtOdaEkleNo;
    @FXML private TextField txtOdaEkleKapasite;
    @FXML private Label lblOdaEkleBilgi;
    @FXML private Label lblKayitliOdaSayisiBadge;
    @FXML private ListView<String> listKayitliOdalar;
    @FXML private Button btnOdaEkleTemizle;
    @FXML private Button btnOdaEkleKaydet;

    // =========================================================================
    // 3. MÜŞTERİ EKLEME EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtMusteriEkleTc;
    @FXML private TextField txtMusteriEkleAd;
    @FXML private TextField txtMusteriEkleSoyad;
    @FXML private TextField txtMusteriEkleTelefon;
    @FXML private DatePicker dpMusteriEkleDogumTarihi;
    @FXML private TextField txtMusteriEkleEmail;
    @FXML private Label lblMusteriEkleBilgi;
    @FXML private TextField txtMusteriRehberAra;
    @FXML private ListView<String> listKayitliMusteriler;
    @FXML private Button btnMusteriEkleTemizle;
    @FXML private Button btnMusteriEkleKaydet;

    // =========================================================================
    // 4. ODA ARKADAŞI (MİSAFİR) EKLEME EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtMisafirRezSahibiTc;
    @FXML private Button btnMisafirAktifRezBul;
    @FXML private Label lblMisafirRezDurumBadge;
    @FXML private Label lblMisafirBulunanRezId;
    @FXML private Label lblMisafirBulunanRezSahibi;
    @FXML private Label lblMisafirBulunanOdaBilgi;
    @FXML private Label lblMisafirBulunanTarihler;
    @FXML private ListView<String> listRezervasyonMisafirleri;

    @FXML private TextField txtMisafirTc;
    @FXML private Button btnMisafirTcKontrol;
    @FXML private Label lblMisafirKayitDurumu;
    @FXML private TextField txtMisafirAd;
    @FXML private TextField txtMisafirSoyad;
    @FXML private TextField txtMisafirTelefon;
    @FXML private DatePicker dpMisafirDogumTarihi;
    @FXML private TextField txtMisafirEmail;
    @FXML private Button btnMisafirFormTemizle;
    @FXML private Button btnMisafirOdayaEkle;

    // =========================================================================
    // 5. REZERVASYON EKLEME EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtRezMusteriTc;
    @FXML private Button btnRezMusteriSorgula;
    @FXML private Label lblRezMusteriSorguSonuc;
    @FXML private VBox boxRezYeniMusteriAlanlari;
    @FXML private TextField txtRezYeniAd;
    @FXML private TextField txtRezYeniSoyad;
    @FXML private TextField txtRezYeniTelefon;
    @FXML private DatePicker dpRezYeniDogumTarihi;
    @FXML private TextField txtRezYeniEmail;
    @FXML private TextField txtRezOdaNo;
    @FXML private TextField txtRezGecelikUcret;
    @FXML private DatePicker dpRezGirisTarihi;
    @FXML private DatePicker dpRezCikisTarihi;
    @FXML private Button btnRezMusaitlikKontrol;
    @FXML private Label lblRezMusaitlikDurumu;
    @FXML private Label lblRezHesaplananGun;
    @FXML private Label lblRezOzetGecelik;
    @FXML private Label lblRezHesaplananToplam;
    @FXML private CheckBox chkRezGiristeFaturaKes;
    @FXML private VBox boxRezFaturaDetay;
    @FXML private TextField txtRezFaturaBaslik;
    @FXML private TextField txtRezVergiNo;
    @FXML private ComboBox<String> cmbRezOdemeYontemi;
    @FXML private TextArea txtRezFaturaAdresi;
    @FXML private Button btnRezFormTemizle;
    @FXML private Button btnRezervasyonOlustur;

    // =========================================================================
    // 6. FATURA GÖSTERME EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtFaturaSorguTelefon;
    @FXML private Button btnFaturaSorgula;
    @FXML private Label lblFaturaSorguDurum;
    @FXML private Label lblFaturaId;
    @FXML private Label lblFaturaRezId;
    @FXML private Label lblFaturaTarihi;
    @FXML private Label lblFaturaBasligi;
    @FXML private Label lblFaturaVergiNo;
    @FXML private Label lblFaturaAdresi;
    @FXML private Label lblFaturaOdemeYontemi;
    @FXML private Label lblFaturaToplamTutar;

    // =========================================================================
    // 7. ODA DÜZENLEME EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtOdaDuzenleTcKimlik;
    @FXML private TextField txtOdaDuzenleOdaNo;
    @FXML private ToggleGroup grpOdaDurumSecim;
    @FXML private RadioButton rbOdaDurumDolu;
    @FXML private RadioButton rbOdaDurumTemizlenecek;
    @FXML private RadioButton rbOdaDurumMusait;
    @FXML private RadioButton rbOdaDurumBakimda;
    @FXML private Button btnOdaDuzenleTemizle;
    @FXML private Button btnOdaDurumGuncelle;
    @FXML private ListView<String> listOdaDurumGecmisi;

    // =========================================================================
    // 8. MÜŞTERİ GİRİŞ YAPTI (CHECK-IN) EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtMusteriGirisTelefon;
    @FXML private Button btnMusteriGirisSorgula;
    @FXML private Label lblGirisRezDurumBadge;
    @FXML private Label lblGirisMusteriAdSoyad;
    @FXML private Label lblGirisAtananOdaNo;
    @FXML private Label lblGirisTarihAraligi;
    @FXML private Button btnMusteriGirisOnayla;
    @FXML private ListView<String> listBugunGirisBeklenenler;

    // =========================================================================
    // 9. MÜŞTERİ ÇIKIŞ YAPTI (CHECK-OUT) EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtMusteriCikisTelefon;
    @FXML private Button btnMusteriCikisSorgula;
    @FXML private Label lblCikisMusteriVeOda;
    @FXML private Label lblCikisToplamUcret;
    @FXML private Label lblCikisFaturaDurumu;
    @FXML private Label lblCikisOdemeDurumu;
    @FXML private Button btnMusteriCikisOnayla;
    @FXML private TextField txtCikisFaturaBaslik;
    @FXML private TextField txtCikisVergiNo;
    @FXML private ComboBox<String> cmbCikisOdemeYontemi;
    @FXML private TextArea txtCikisFaturaAdresi;

    // =========================================================================
    // 10. ODA TEMİZLENDİ EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtTemizlikBitenOdaNo;
    @FXML private TextArea txtTemizlikNotu;
    @FXML private Button btnOdaTemizlendiOnayla;
    @FXML private Label lblTemizlikBekleyenSayacBadge;
    @FXML private ListView<String> listTemizlikBekleyenOdalar;

    // =========================================================================
    // 11. REZERVASYON İPTAL EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtRezIptalTelefon;
    @FXML private Button btnRezIptalSorgula;
    @FXML private Label lblIptalEdilecekRezId;
    @FXML private Label lblIptalEdilecekMusteriOda;
    @FXML private Label lblIptalEdilecekTarihler;
    @FXML private Button btnRezervasyonIptalOnayla;
    @FXML private ListView<String> listSonIptalEdilenler;

    // =========================================================================
    // 12. BORÇ GÖRÜNTÜLE VE BORÇ ÖDE EKRANI BİLEŞENLERİ
    // =========================================================================
    @FXML private TextField txtBorcSorguTelefon;
    @FXML private Button btnBorcGoruntule;
    @FXML private ListView<String> listMusteriBorclari;
    @FXML private Label lblBorcToplamTutar;
    @FXML private Label lblBorcKayitAdedi;
    @FXML private TextField txtBorcOdemeTelefon;
    @FXML private ComboBox<String> cmbBorcOdemeYontemi;
    @FXML private Button btnBorcOdeOnayla;

    // =========================================================================
    // ARAYÜZ DURUM DEĞİŞKENLERİ (TEMA & ODA TEMSİL LİSTESİ)
    // =========================================================================
    private boolean darkModeAktif = false;
    private String aktifOdaFiltresi = "Tümü";
    private final List<OdaTemsilModeli> odaTemsilListesi = new ArrayList<>();

    /**
     * Ana ekrandaki dinamik oda kartlarını oluşturmak için kullanılan görsel veri modeli.
     * Veritabanındaki 'odalar' tablosundan çekilen veriler bu listeye aktarıldığında
     * ana ekran otomatik olarak güncellenir.
     */
    public record OdaTemsilModeli(
            String odaNo,
            int kapasite,
            String odaDurum,
            String detayBilgi
    ) {}

    private Sql db;

    @FXML
    public void initialize() {
        try {
            db = new Sql();
            db.gecikmisCikislariKapat();
            lblDbBaglantiDurumu.setText("● Sistem Hazır");
        } catch (SQLException e) {
            lblDbBaglantiDurumu.setText("✕ Bağlantı Sorunu!");
            e.printStackTrace();
        }
        // Günün tarihini üst bara yazdır
        lblGununTarihi.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        List<String> odemeYontemleri = List.of("Nakit", "Kredi Kartı", "Havale/EFT");
        cmbRezOdemeYontemi.getItems().setAll(odemeYontemleri);
        cmbRezOdemeYontemi.getSelectionModel().selectFirst();

        cmbCikisOdemeYontemi.getItems().setAll(odemeYontemleri);
        cmbCikisOdemeYontemi.getSelectionModel().selectFirst();

        cmbBorcOdemeYontemi.getItems().setAll(odemeYontemleri);
        cmbBorcOdemeYontemi.getSelectionModel().selectFirst();

        // Rezervasyon ekranında tarih veya gecelik ücret değiştiğinde tutar özetini dinamik güncelle
        dpRezGirisTarihi.setValue(LocalDate.now());
        dpRezCikisTarihi.setValue(LocalDate.now().plusDays(2));
        dpRezGirisTarihi.valueProperty().addListener((obs, oldV, newV) -> rezervasyonOzetiniHesapla());
        dpRezCikisTarihi.valueProperty().addListener((obs, oldV, newV) -> rezervasyonOzetiniHesapla());
        txtRezGecelikUcret.textProperty().addListener((obs, oldV, newV) -> rezervasyonOzetiniHesapla());

        // Ana ekranda oda arama kutusu dinleyicisi
        txtDashboardOdaAra.textProperty().addListener((obs, oldV, newV) -> odaHaritasiniCiz());

        // Temizlik bekleyen odalar listesinde bir odaya tıklandığında oda numarasını inputa aktar
        listTemizlikBekleyenOdalar.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains("Oda ")) {
                String[] parcalar = newVal.split("•")[0].replace("Oda", "").trim().split(" ");
                txtTemizlikBitenOdaNo.setText(parcalar[0].trim());
            }
        });

        // Veritabanından (Otel.db) gerçek oda ve istatistik verilerini yükle
        veritabanindanGuncelle();
    }

    // =========================================================================
    // EKRANLAR ARASI GEÇİŞ METODLARI (SOL NAVİGASYON BAR)
    // =========================================================================

    private void ekranDegistir(Node hedefEkran, Button aktifButon, String baslik, String altBaslik) {
        for (Node child : contentStack.getChildren()) {
            child.setVisible(false);
            child.setManaged(false);
        }
        hedefEkran.setVisible(true);
        hedefEkran.setManaged(true);

        List<Button> navButonlari = List.of(
                btnNavDashboard, btnNavMusteriGiris, btnNavMusteriCikis,
                btnNavRezervasyonEkle, btnNavRezervasyonIptal,
                btnNavMusteriEkle, btnNavMisafirEkle,
                btnNavOdaEkle, btnNavOdaDuzenle, btnNavOdaTemizlendi,
                btnNavFaturaGoster, btnNavBorcIslemleri
        );
        for (Button btn : navButonlari) {
            btn.getStyleClass().remove("nav-btn-active");
        }
        if (aktifButon != null && !aktifButon.getStyleClass().contains("nav-btn-active")) {
            aktifButon.getStyleClass().add("nav-btn-active");
        }

        lblTopbarTitle.setText(baslik);
        lblTopbarSubtitle.setText(altBaslik);
    }

    @FXML
    private void showDashboard() {
        ekranDegistir(
                viewDashboard,
                btnNavDashboard,
                "Ana Ekran • Otel Genel Bakış",
                "Canlı oda doluluk durumu, günlük giriş/çıkış hareketleri ve finansal özet"
        );
    }

    @FXML
    private void showOdaEkle() {
        ekranDegistir(
                viewOdaEkle,
                btnNavOdaEkle,
                "Oda Ekleme Ekranı",
                "Veritabanına yeni oda numarası, tipi, kapasitesi ve gecelik ücret tanımlayın"
        );
    }

    @FXML
    private void showMusteriEkle() {
        ekranDegistir(
                viewMusteriEkle,
                btnNavMusteriEkle,
                "Müşteri Ekleme Ekranı",
                "Yeni müşteri kimlik ve iletişim bilgilerini sisteme kaydedin"
        );
    }

    @FXML
    private void showMisafirEkle() {
        ekranDegistir(
                viewMisafirEkle,
                btnNavMisafirEkle,
                "Oda Arkadaşı (Misafir) Ekleme Ekranı",
                "Aktif rezervasyon sahibinin odasına birlikte konaklayacak misafirleri bağlayın"
        );
    }

    @FXML
    private void showRezervasyonEkle() {
        ekranDegistir(
                viewRezervasyonEkle,
                btnNavRezervasyonEkle,
                "Rezervasyon Ekleme Ekranı",
                "Müsaitlik kontrolü yaparak yeni konaklama rezervasyonu ve opsiyonel giriş faturası oluşturun"
        );
    }

    @FXML
    private void showFaturaGoster() {
        ekranDegistir(
                viewFaturaGoster,
                btnNavFaturaGoster,
                "Fatura Görüntüleme Ekranı",
                "Müşteri telefon numarası ile kesilmiş resmi konaklama faturasını görüntüleyin"
        );
    }

    @FXML
    private void showOdaDuzenle() {
        ekranDegistir(
                viewOdaDuzenle,
                btnNavOdaDuzenle,
                "Oda Düzenleme & Durum Değiştirme Ekranı",
                "Oda durumunu (Dolu, Temizlenecek, Müsait, Bakımda) güncelleyin"
        );
    }

    @FXML
    private void showMusteriGiris() {
        ekranDegistir(
                viewMusteriGiris,
                btnNavMusteriGiris,
                "Müşteri Giriş Yaptı (Check-In) Ekranı",
                "Onaylı rezervasyon sahibinin otele girişini onaylayın ve odayı 'Dolu' durumuna geçirin"
        );
    }

    @FXML
    private void showMusteriCikis() {
        ekranDegistir(
                viewMusteriCikis,
                btnNavMusteriCikis,
                "Müşteri Çıkış Yaptı (Check-Out) Ekranı",
                "Konaklaması biten müşterinin fatura/borç kontrolünü yaparak odayı temizliğe yönlendirin"
        );
    }

    @FXML
    private void showOdaTemizlendi() {
        ekranDegistir(
                viewOdaTemizlendi,
                btnNavOdaTemizlendi,
                "Oda Temizlendi Bildirim Ekranı",
                "Kat hizmetleri tamamlanan odayı tekrar 'Müsait' durumuna getirin"
        );
    }

    @FXML
    private void showRezervasyonIptal() {
        ekranDegistir(
                viewRezervasyonIptal,
                btnNavRezervasyonIptal,
                "Rezervasyon İptal Ekranı",
                "Müşteri telefon numarası ile aktif veya onaylı rezervasyonu iptal edin"
        );
    }

    @FXML
    private void showBorcIslemleri() {
        ekranDegistir(
                viewBorcIslemleri,
                btnNavBorcIslemleri,
                "Borç Görüntüle ve Borç Öde Ekranı",
                "Müşterinin ödenmemiş rezervasyon bakiyelerini sorgulayın ve tahsilat yapın"
        );
    }

    // =========================================================================
    // TEMA DEĞİŞTİRİCİ (LIGHT / DARK MODE MİMARİSİ) VE UYGULAMA ÇIKIŞ
    // =========================================================================

    @FXML
    private void onToggleThemeClick() {
        darkModeAktif = !darkModeAktif;
        if (darkModeAktif) {
            if (!rootPane.getStyleClass().contains("theme-dark")) {
                rootPane.getStyleClass().add("theme-dark");
            }
            btnThemeToggle.setText("☀   Açık Tema (Light Mode)");
        } else {
            rootPane.getStyleClass().remove("theme-dark");
            btnThemeToggle.setText("☾   Koyu Tema (Dark Mode)");
        }
    }

    @FXML
    private void onUygulamaCikisClick() {
        Platform.exit();
    }

    // =========================================================================
    // ANA EKRAN DİNAMİK ODA TEMSİLİ VE İSTATİSTİK YARDIMCI METODLARI
    // =========================================================================

    /**
     * Veritabanından çekilen istatistikleri ana ekrandaki KPI kartlarına tek seferde basar.
     */
    public void dashboardIstatistikleriGuncelle(
            int doluOda,
            int musaitOda,
            int temizlenecekOda,
            int bakimdakiOda,
            int gunlukGirisInsan,
            int gunlukGirisRezervasyon,
            int gunlukCikisInsan,
            int gunlukCikisOda,
            double gunlukCiro,
            double bekleyenBorc
    ) {
        int toplamOda = doluOda + musaitOda + temizlenecekOda + bakimdakiOda;
        double dolulukOrani = toplamOda > 0 ? (double) doluOda / toplamOda : 0.0;
        int dolulukYuzde = (int) Math.round(dolulukOrani * 100);

        lblDoluOdaSayisi.setText(doluOda + " Dolu");
        lblBosOdaSayisi.setText(musaitOda + " Müsait");
        lblDolulukYuzdeBadge.setText("%" + dolulukYuzde + " Dolu");
        prgDolulukOrani.setProgress(dolulukOrani);
        lblToplamOdaMeta.setText("Toplam kayıtlı oda: " + toplamOda);

        lblGunlukGirisInsanSayisi.setText(gunlukGirisInsan + " Misafir");
        lblGunlukGirisOdaSayisi.setText("(" + gunlukGirisRezervasyon + " Rezervasyon)");

        lblGunlukCikisInsanSayisi.setText(gunlukCikisInsan + " Misafir");
        lblGunlukCikisOdaSayisi.setText("(" + gunlukCikisOda + " Oda)");

        lblTemizlenecekOdaSayisi.setText(temizlenecekOda + " Oda");
        lblBakimYapilacakSayisi.setText(bakimdakiOda + " Oda");

        lblGunlukCiro.setText(String.format(Locale.forLanguageTag("tr-TR"), "₺%,.2f", gunlukCiro));
        lblBekleyenBorcToplam.setText(String.format(Locale.forLanguageTag("tr-TR"),
                "Tahsilat bekleyen açık bakiye: ₺%,.2f", bekleyenBorc));
    }

    /**
     * odaTemsilListesi içindeki odaları seçili durum filtresi ve arama metnine göre
     * dinamik olarak FlowPane içerisine çizer.
     */
    public void odaHaritasiniCiz() {
        flowOdaTemsilleri.getChildren().clear();
        if (odaTemsilListesi.isEmpty()) {
            Label bosBilgi = new Label("Sistemde henüz kayıtlı oda bulunmuyor. Sol menüden 'Oda Ekle' ekranına giderek oda ekleyebilirsiniz.");
            bosBilgi.getStyleClass().add("card-subtitle");
            flowOdaTemsilleri.getChildren().add(bosBilgi);
            return;
        }

        String arama = txtDashboardOdaAra.getText() == null
                ? ""
                : txtDashboardOdaAra.getText().trim().toLowerCase(Locale.forLanguageTag("tr-TR"));

        for (OdaTemsilModeli oda : odaTemsilListesi) {
            boolean filtreUygun = aktifOdaFiltresi.equals("Tümü")
                    || oda.odaDurum().equalsIgnoreCase(aktifOdaFiltresi);

            boolean aramaUygun = arama.isEmpty()
                    || oda.odaNo().toLowerCase(Locale.ROOT).contains(arama)
                    || String.valueOf(oda.kapasite()).contains(arama)
                    || oda.detayBilgi().toLowerCase(Locale.forLanguageTag("tr-TR")).contains(arama);

            if (filtreUygun && aramaUygun) {
                flowOdaTemsilleri.getChildren().add(odaKartiOlustur(oda));
            }
        }
    }

    /**
     * Veritabanındaki bir odayı temsil eden şık görsel kart bileşenini (VBox) üretir.
     */
    private VBox odaKartiOlustur(OdaTemsilModeli oda) {
        VBox kart = new VBox(8.0);
        kart.getStyleClass().add("room-card");

        String rozetStili;
        switch (oda.odaDurum()) {
            case "Müsait" -> {
                kart.getStyleClass().add("room-card-available");
                rozetStili = "badge-available";
            }
            case "Dolu" -> {
                kart.getStyleClass().add("room-card-occupied");
                rozetStili = "badge-occupied";
            }
            case "Temizlenecek" -> {
                kart.getStyleClass().add("room-card-cleaning");
                rozetStili = "badge-cleaning";
            }
            default -> {
                kart.getStyleClass().add("room-card-maintenance");
                rozetStili = "badge-danger";
            }
        }

        // Üst Satır: Oda Numarası + Durum Rozeti
        HBox ustSatir = new HBox(8.0);
        ustSatir.setAlignment(Pos.CENTER_LEFT);
        Label lblOdaNo = new Label("Oda " + oda.odaNo());
        lblOdaNo.getStyleClass().add("room-no-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblDurum = new Label(oda.odaDurum());
        lblDurum.getStyleClass().addAll("badge", rozetStili);
        ustSatir.getChildren().addAll(lblOdaNo, spacer, lblDurum);

        // Orta Satır: Oda Kapasitesi
        Label lblKapasite = new Label("Kapasite: " + oda.kapasite() + " Kişilik");
        lblKapasite.getStyleClass().add("room-price-label");

        Label lblDetay = new Label(oda.detayBilgi());
        lblDetay.getStyleClass().add("room-guest-label");

        kart.getChildren().addAll(ustSatir, lblKapasite, lblDetay);

        // Oda kartına tıklandığında durumuna göre ilgili ekrana hızlı yönlendirme
        kart.setOnMouseClicked(e -> {
            if ("Temizlenecek".equalsIgnoreCase(oda.odaDurum())) {
                txtTemizlikBitenOdaNo.setText(oda.odaNo());
                showOdaTemizlendi();
            } else if ("Müsait".equalsIgnoreCase(oda.odaDurum())) {
                txtRezOdaNo.setText(oda.odaNo());
                showRezervasyonEkle();
            } else {
                txtOdaDuzenleOdaNo.setText(oda.odaNo());
                showOdaDuzenle();
            }
        });

        return kart;
    }

    private void aktifFiltreButonunuAyarla(String filtreAdi, Button seciliButon) {
        this.aktifOdaFiltresi = filtreAdi;
        List<Button> filtreButonlari = List.of(
                btnFiltreTumu, btnFiltreMusait, btnFiltreDolu, btnFiltreTemizlenecek, btnFiltreBakimda
        );
        for (Button b : filtreButonlari) {
            b.getStyleClass().remove("filter-chip-active");
        }
        if (!seciliButon.getStyleClass().contains("filter-chip-active")) {
            seciliButon.getStyleClass().add("filter-chip-active");
        }
        odaHaritasiniCiz();
    }

    @FXML private void onFiltreTumuClick() { aktifFiltreButonunuAyarla("Tümü", btnFiltreTumu); }
    @FXML private void onFiltreMusaitClick() { aktifFiltreButonunuAyarla("Müsait", btnFiltreMusait); }
    @FXML private void onFiltreDoluClick() { aktifFiltreButonunuAyarla("Dolu", btnFiltreDolu); }
    @FXML private void onFiltreTemizlenecekClick() { aktifFiltreButonunuAyarla("Temizlenecek", btnFiltreTemizlenecek); }
    @FXML private void onFiltreBakimdaClick() { aktifFiltreButonunuAyarla("Bakımda", btnFiltreBakimda); }

    /**
     * Rezervasyon ekranında giriş/çıkış tarihi veya gecelik ücret değiştiğinde
     * görsel tutar özetini hesaplar (Salt UI hesaplaması).
     */
    private void rezervasyonOzetiniHesapla() {
        LocalDate giris = dpRezGirisTarihi.getValue();
        LocalDate cikis = dpRezCikisTarihi.getValue();
        long gun = 0;
        if (giris != null && cikis != null && cikis.isAfter(giris)) {
            gun = ChronoUnit.DAYS.between(giris, cikis);
        }
        double gecelik = 0.0;
        try {
            if (txtRezGecelikUcret.getText() != null && !txtRezGecelikUcret.getText().isBlank()) {
                gecelik = Double.parseDouble(txtRezGecelikUcret.getText().trim().replace(",", "."));
            }
        } catch (NumberFormatException ignored) {
        }
        double toplam = gun * gecelik;
        lblRezHesaplananGun.setText(gun + " Gece");
        lblRezOzetGecelik.setText(String.format(Locale.forLanguageTag("tr-TR"), "₺%,.2f", gecelik));
        lblRezHesaplananToplam.setText(String.format(Locale.forLanguageTag("tr-TR"), "₺%,.2f", toplam));
    }

    @FXML
    private void onRezFaturaTercihDegisti() {
        boxRezFaturaDetay.setDisable(!chkRezGiristeFaturaKes.isSelected());
    }

    // =========================================================================
    // İŞLEV BUTONLARI (BACKEND / SQL BAĞLANTISI SIRASIYLA YAPILACAK STUBLAR)
    // =========================================================================

    @FXML
    private void onDashboardYenileAction() {
        veritabanindanGuncelle();
    }

    // --- 2. Oda Ekleme Ekranı Butonları ---
    @FXML
    private void onOdaEkleKaydetAction() {
        String odaNo = txtOdaEkleNo.getText() != null ? txtOdaEkleNo.getText().trim() : "";
        String txtOdaKapasite = txtOdaEkleKapasite.getText() != null ? txtOdaEkleKapasite.getText().trim() : "";

        if (odaNo.isEmpty() || txtOdaKapasite.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen oda numarası ve kapasite alanlarını doldurunuz.");
            return;
        }

        int kapasite;
        try {
            kapasite = Integer.parseInt(txtOdaKapasite);
        } catch (NumberFormatException e) {
            uyariGoster("Geçersiz Giriş", "Oda kapasitesi tam sayı olmalıdır.");
            return;
        }

        if (kapasite <= 0) {
            uyariGoster("Geçersiz Değer", "Oda kapasitesi 0'dan büyük olmalıdır.");
            return;
        }

        Odalar oda = new Odalar(odaNo, kapasite);
        try {
            db.odaEkle(oda);
            veritabanindanGuncelle();
            onOdaEkleFormTemizleAction();
            bilgiGoster("Oda Eklendi", "Oda " + odaNo + " başarıyla sisteme eklendi.");
        } catch (SQLException e) {
            uyariGoster("Kayıt Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onOdaEkleFormTemizleAction() {
        txtOdaEkleNo.clear();
        txtOdaEkleKapasite.clear();
    }

    // --- 3. Müşteri Ekleme Ekranı Butonları ---
    @FXML
    private void onMusteriEkleKaydetAction() {
        String tcKimlik = txtMusteriEkleTc.getText() != null ? txtMusteriEkleTc.getText().trim() : "";
        String ad = txtMusteriEkleAd.getText() != null ? txtMusteriEkleAd.getText().trim() : "";
        String soyad = txtMusteriEkleSoyad.getText() != null ? txtMusteriEkleSoyad.getText().trim() : "";
        String telefon = txtMusteriEkleTelefon.getText() != null ? txtMusteriEkleTelefon.getText().trim() : "";
        LocalDate dogumTarihi = dpMusteriEkleDogumTarihi.getValue();
        String eMailGirdi = txtMusteriEkleEmail.getText() != null ? txtMusteriEkleEmail.getText().trim() : "";
        String eMail = eMailGirdi.isEmpty() ? null : eMailGirdi;

        if (tcKimlik.isEmpty() || ad.isEmpty() || soyad.isEmpty() || telefon.isEmpty() || dogumTarihi == null) {
            uyariGoster("Eksik Bilgi", "Lütfen zorunlu (*) alanların tamamını doldurunuz.");
            return;
        }

        if (!dogumTarihi.isBefore(LocalDate.now())) {
            uyariGoster("Geçersiz Tarih", "Doğum tarihi bugünden önceki bir tarih olmalıdır.");
            return;
        }

        Musteri musteri = new Musteri(ad, soyad, tcKimlik, telefon, dogumTarihi, eMail);
        try {
            db.musteriEkle(musteri);
            listKayitliMusteriler.getItems().add(0,
                    ad + " " + soyad + "  •  Tel: " + telefon + "  •  T.C.: " + tcKimlik);
            onMusteriEkleFormTemizleAction();
            bilgiGoster("Müşteri Kaydedildi", ad + " " + soyad + " başarıyla sisteme kaydedildi.");
        } catch (SQLException e) {
            uyariGoster("Kayıt Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onMusteriEkleFormTemizleAction() {
        txtMusteriEkleTc.clear();
        txtMusteriEkleAd.clear();
        txtMusteriEkleSoyad.clear();
        txtMusteriEkleTelefon.clear();
        dpMusteriEkleDogumTarihi.setValue(null);
        txtMusteriEkleEmail.clear();
    }

    // --- 4. Oda Arkadaşı (Misafir) Ekleme Ekranı Butonları ---
    @FXML
    private void onMisafirAktifRezBulAction() {
        String tcKimlik = txtMisafirRezSahibiTc.getText() != null ? txtMisafirRezSahibiTc.getText().trim() : "";
        if (tcKimlik.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen rezervasyon sahibinin T.C. Kimlik numarasını giriniz.");
            return;
        }
        try {
            int rezId = db.aktifRezervasyonIdGetirTcIle(tcKimlik);
            if (rezId != -1) {
                lblMisafirRezDurumBadge.setText("Aktif Rezervasyon Bulundu");
                lblMisafirBulunanRezId.setText("#" + rezId);
                seciliMisafirRezID = rezId;
            } else {
                lblMisafirRezDurumBadge.setText("Rezervasyon Bulunamadı");
                lblMisafirBulunanRezId.setText("-");
                seciliMisafirRezID = -1;
                uyariGoster("Kayıt Bulunamadı", "Bu T.C. Kimlik numarasına ait aktif bir rezervasyon bulunamadı.");
            }
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onMisafirTcKontrolAction() {
        String misafirTc = txtMisafirTc.getText() != null ? txtMisafirTc.getText().trim() : "";
        if (misafirTc.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen oda arkadaşının T.C. Kimlik numarasını giriniz.");
            return;
        }
        try {
            int musteriId = db.musteriIdGetirTcIle(misafirTc);
            if (musteriId != -1) {
                lblMisafirKayitDurumu.setText("Müşteri sistemde kayıtlı, doğrudan odaya ekleyebilirsiniz.");
            } else {
                lblMisafirKayitDurumu.setText("Müşteri kaydı bulunamadı. Lütfen aşağıdaki bilgileri doldurunuz.");
            }
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onMisafirOdayaEkleAction() {
        String rezSahibiTc = txtMisafirRezSahibiTc.getText() != null ? txtMisafirRezSahibiTc.getText().trim() : "";
        String misafirTc = txtMisafirTc.getText() != null ? txtMisafirTc.getText().trim() : "";

        if (rezSahibiTc.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen önce rezervasyon sahibinin T.C. Kimlik numarasını giriniz.");
            return;
        }
        if (misafirTc.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen eklenecek oda arkadaşının T.C. Kimlik numarasını giriniz.");
            return;
        }

        try {
            int rezID = db.aktifRezervasyonIdGetirTcIle(rezSahibiTc);
            if (rezID == -1) {
                uyariGoster("Rezervasyon Bulunamadı", "Rezervasyon sahibine ait aktif bir rezervasyon bulunamadı.");
                return;
            }

            int musteriID = db.musteriIdGetirTcIle(misafirTc);
            if (musteriID == -1) {
                String ad = txtMisafirAd.getText() != null ? txtMisafirAd.getText().trim() : "";
                String soyad = txtMisafirSoyad.getText() != null ? txtMisafirSoyad.getText().trim() : "";
                String telefon = txtMisafirTelefon.getText() != null ? txtMisafirTelefon.getText().trim() : "";
                LocalDate dogumTarihi = dpMisafirDogumTarihi.getValue();
                String eMailGirdi = txtMisafirEmail.getText() != null ? txtMisafirEmail.getText().trim() : "";
                String eMail = eMailGirdi.isEmpty() ? null : eMailGirdi;

                if (ad.isEmpty() || soyad.isEmpty() || telefon.isEmpty() || dogumTarihi == null) {
                    uyariGoster("Eksik Bilgi", "Kayıtsız misafir için ad, soyad, telefon ve doğum tarihi alanlarını doldurunuz.");
                    return;
                }
                if (!dogumTarihi.isBefore(LocalDate.now())) {
                    uyariGoster("Geçersiz Tarih", "Doğum tarihi bugünden önceki bir tarih olmalıdır.");
                    return;
                }

                Musteri musteri = new Musteri(ad, soyad, misafirTc, telefon, dogumTarihi, eMail);
                db.musteriEkle(musteri);
                musteriID = musteri.getMusteriId();
            }

            db.musteriMisafirEkle(musteriID, rezID);
            listRezervasyonMisafirleri.getItems().add(0, "Rezervasyon #" + rezID + " • Misafir T.C.: " + misafirTc);
            onMisafirFormTemizleAction();
            veritabanindanGuncelle();
            bilgiGoster("Oda Arkadaşı Eklendi", "Misafir ilgili rezervasyona başarıyla eklendi.");
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onMisafirFormTemizleAction() {
        txtMisafirTc.clear();
        txtMisafirAd.clear();
        txtMisafirSoyad.clear();
        txtMisafirTelefon.clear();
        dpMisafirDogumTarihi.setValue(null);
        txtMisafirEmail.clear();
    }

    // --- 5. Rezervasyon Ekleme Ekranı Butonları ---
    @FXML
    private void onRezMusteriSorgulaAction() {
        String tcKimlik = txtRezMusteriTc.getText() != null ? txtRezMusteriTc.getText().trim() : "";
        if (tcKimlik.isEmpty()) {
            lblRezMusteriSorguSonuc.setText("Lütfen bir T.C. Kimlik numarası giriniz.");
            return;
        }
        try {
            int musteriId = db.musteriIdGetirTcIle(tcKimlik);
            if (musteriId == -1) {
                lblRezMusteriSorguSonuc.setText("Müşteri kayıtlı değil • Aşağıdaki hızlı kayıt alanlarını doldurunuz.");
                return;
            }
            lblRezMusteriSorguSonuc.setText("✓ Kayıtlı Müşteri: " + db.musteriAdSoyadGetir(musteriId));
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onRezMusaitlikKontrolAction() {
        String odaNo = txtRezOdaNo.getText() != null ? txtRezOdaNo.getText().trim() : "";
        LocalDate giris = dpRezGirisTarihi.getValue();
        LocalDate cikis = dpRezCikisTarihi.getValue();
        if (odaNo.isEmpty() || giris == null || cikis == null) {
            uyariGoster("Eksik Bilgi", "Lütfen oda numarası ile giriş ve çıkış tarihlerini eksiksiz seçiniz.");
            return;
        }
        if (!cikis.isAfter(giris)) {
            uyariGoster("Geçersiz Tarih", "Çıkış tarihi giriş tarihinden en az 1 gün sonra olmalıdır.");
            return;
        }

        try {
            int odaId = db.odaIdGetir(odaNo);
            if (odaId == -1) {
                lblRezMusaitlikDurumu.setText("✕ Oda bulunamadı");
                uyariGoster("Oda Bulunamadı", odaNo + " numaralı bir oda sistemde bulunamadı.");
                return;
            }
            boolean musait = db.odaMusaitMi(odaId, giris, cikis);
            lblRezMusaitlikDurumu.setText(musait ? "✓ Oda Müsait" : "✕ Oda Bu Tarihlerde Dolu!");
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onRezervasyonOlusturAction() {
        String tcKimlik = txtRezMusteriTc.getText() != null ? txtRezMusteriTc.getText().trim() : "";
        if (tcKimlik.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen müşterinin T.C. Kimlik numarasını giriniz.");
            return;
        }
        LocalDate giris = dpRezGirisTarihi.getValue();
        LocalDate cikis = dpRezCikisTarihi.getValue();
        if (giris == null || cikis == null || !cikis.isAfter(giris)) {
            uyariGoster("Geçersiz Tarih", "Çıkış tarihi giriş tarihinden en az 1 gün sonra olmalıdır.");
            return;
        }

        String odaNo = txtRezOdaNo.getText() != null ? txtRezOdaNo.getText().trim() : "";
        String ucretStr = txtRezGecelikUcret.getText() != null ? txtRezGecelikUcret.getText().trim().replace(",", ".") : "";
        if (odaNo.isEmpty() || ucretStr.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen kalınacak oda numarasını ve gecelik ücreti giriniz.");
            return;
        }

        double gecelikUcret;
        try {
            gecelikUcret = Double.parseDouble(ucretStr);
        } catch (NumberFormatException e) {
            uyariGoster("Geçersiz Ücret", "Lütfen gecelik ücret alanına geçerli bir sayı giriniz.");
            return;
        }
        if (gecelikUcret <= 0) {
            uyariGoster("Geçersiz Ücret", "Gecelik ücret 0'dan büyük olmalıdır.");
            return;
        }

        try {
            int musteriId = db.musteriIdGetirTcIle(tcKimlik);
            if (musteriId == -1) {
                String ad = txtRezYeniAd.getText() != null ? txtRezYeniAd.getText().trim() : "";
                String soyad = txtRezYeniSoyad.getText() != null ? txtRezYeniSoyad.getText().trim() : "";
                String telefon = txtRezYeniTelefon.getText() != null ? txtRezYeniTelefon.getText().trim() : "";
                LocalDate dogumTarihi = dpRezYeniDogumTarihi.getValue();
                String eMailGirdi = txtRezYeniEmail.getText() != null ? txtRezYeniEmail.getText().trim() : "";
                String eMail = eMailGirdi.isEmpty() ? null : eMailGirdi;

                if (ad.isEmpty() || soyad.isEmpty() || telefon.isEmpty() || dogumTarihi == null) {
                    uyariGoster("Eksik Müşteri Bilgisi", "Kayıtsız müşteri için ad, soyad, telefon ve doğum tarihi alanlarını doldurunuz.");
                    return;
                }
                if (!dogumTarihi.isBefore(LocalDate.now())) {
                    uyariGoster("Geçersiz Tarih", "Doğum tarihi bugünden önceki bir tarih olmalıdır.");
                    return;
                }

                Musteri musteri = new Musteri(ad, soyad, tcKimlik, telefon, dogumTarihi, eMail);
                db.musteriEkle(musteri);
                musteriId = musteri.getMusteriId();
            }

            int odaId = db.odaIdGetir(odaNo);
            if (odaId == -1) {
                uyariGoster("Oda Bulunamadı", odaNo + " numaralı bir oda sistemde bulunamadı. Lütfen oda numarasını kontrol ediniz.");
                return;
            }

            if (!db.odaMusaitMi(odaId, giris, cikis)) {
                uyariGoster("Oda Müsait Değil", "Seçilen tarihler arasında Oda " + odaNo + " için başka bir rezervasyon bulunmaktadır.");
                return;
            }

            boolean giristeFaturaKesilsin = chkRezGiristeFaturaKes.isSelected();
            Rezervasyon rez = new Rezervasyon(musteriId, odaId, giris, cikis, gecelikUcret, giristeFaturaKesilsin);
            int rezID = db.rezervasyonEkle(rez);

            if (giristeFaturaKesilsin) {
                String baslik = txtRezFaturaBaslik.getText() != null ? txtRezFaturaBaslik.getText().trim() : "";
                if (baslik.isEmpty()) {
                    baslik = db.musteriAdSoyadGetir(musteriId);
                }
                String vergiNo = txtRezVergiNo.getText() != null ? txtRezVergiNo.getText().trim() : "";
                if (vergiNo.isEmpty()) {
                    vergiNo = tcKimlik;
                }
                String adres = txtRezFaturaAdresi.getText() != null ? txtRezFaturaAdresi.getText().trim() : "";
                if (adres.isEmpty()) {
                    adres = "Belirtilmedi";
                }
                String odemeYontemi = cmbRezOdemeYontemi.getValue();

                Fatura fatura = new Fatura(rezID, baslik, vergiNo, adres, rez.getToplamUcret(), odemeYontemi, LocalDate.now());
                db.faturaKes(fatura);
            }

            veritabanindanGuncelle();
            DateTimeFormatter trFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String bilgiMesaji = "Rezervasyon No: #" + rezID +
                    "\nOda No: " + odaNo +
                    "\nKonaklama Tarihleri: " + giris.format(trFormat) + " - " + cikis.format(trFormat) +
                    "\nToplam Tutar: " + String.format(Locale.forLanguageTag("tr-TR"), "₺%,.2f", rez.getToplamUcret());

            if (giristeFaturaKesilsin) {
                bilgiMesaji += "\n\nÖdeme alındı ve fatura girişte düzenlendi.";
            }

            bilgiGoster("Rezervasyon Oluşturuldu", bilgiMesaji);
            onRezFormTemizleAction();
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onRezFormTemizleAction() {
        txtRezMusteriTc.clear();
        txtRezYeniAd.clear();
        txtRezYeniSoyad.clear();
        txtRezYeniTelefon.clear();
        dpRezYeniDogumTarihi.setValue(null);
        txtRezYeniEmail.clear();
        txtRezOdaNo.clear();
        txtRezGecelikUcret.clear();
        txtRezFaturaBaslik.clear();
        txtRezVergiNo.clear();
        txtRezFaturaAdresi.clear();
        chkRezGiristeFaturaKes.setSelected(false);
        boxRezFaturaDetay.setDisable(true);
        lblRezMusteriSorguSonuc.setText("Müşteri kaydı yoksa aşağıdaki hızlı kayıt alanlarını doldurabilirsiniz.");
        lblRezMusaitlikDurumu.setText("Müsaitlik henüz sorgulanmadı");
    }

    // --- 6. Fatura Gösterme Ekranı Butonları ---
    @FXML
    private void onFaturaSorgulaAction() {
        String telefon = txtFaturaSorguTelefon.getText() != null ? txtFaturaSorguTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen müşterinin telefon numarasını giriniz.");
            return;
        }
        try {
            Fatura fatura = db.faturaGoruntule(telefon);
            if (fatura == null) {
                lblFaturaSorguDurum.setText("Fatura Bulunamadı");
                uyariGoster("Kayıt Bulunamadı", "Bu telefon numarasına ait kesilmiş bir fatura bulunamadı.");
                return;
            }
            DateTimeFormatter trFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            lblFaturaId.setText("#" + fatura.getFaturaId());
            lblFaturaRezId.setText("#" + fatura.getRezervasyonId());
            lblFaturaTarihi.setText(fatura.getFaturaTarihi().format(trFormat));
            lblFaturaBasligi.setText(fatura.getFaturaBasligi());
            lblFaturaVergiNo.setText(fatura.getVergiNo());
            lblFaturaAdresi.setText(fatura.getFaturaAdresi());
            lblFaturaOdemeYontemi.setText(fatura.getOdemeYontemi());
            lblFaturaToplamTutar.setText(String.format(Locale.forLanguageTag("tr-TR"), "₺%,.2f", fatura.getToplamTutar()));
            lblFaturaSorguDurum.setText("Fatura Bulundu");
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    // --- 7. Oda Düzenleme Ekranı Butonları ---
    @FXML
    private void onOdaDurumGuncelleAction() {
        String tcKimlik = txtOdaDuzenleTcKimlik.getText() != null ? txtOdaDuzenleTcKimlik.getText().trim() : "";
        String odaNo = txtOdaDuzenleOdaNo.getText() != null ? txtOdaDuzenleOdaNo.getText().trim() : "";

        if (tcKimlik.isEmpty() && odaNo.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen müşterinin T.C. Kimlik numarasını veya Oda Numarasını giriniz.");
            return;
        }

        String odaDurum = "";
        if (rbOdaDurumDolu.isSelected()) {
            odaDurum = "Dolu";
        } else if (rbOdaDurumMusait.isSelected()) {
            odaDurum = "Müsait";
        } else if (rbOdaDurumTemizlenecek.isSelected()) {
            odaDurum = "Temizlenecek";
        } else if (rbOdaDurumBakimda.isSelected()) {
            odaDurum = "Bakımda";
        }

        try {
            if (!tcKimlik.isEmpty()) {
                boolean guncellendi = db.odaDurumDuzenle(odaDurum, tcKimlik);
                if (!guncellendi) {
                    uyariGoster("Kayıt Bulunamadı", "Bu T.C. Kimlik numarasına ait aktif bir rezervasyon bulunamadı.");
                    return;
                }
                listOdaDurumGecmisi.getItems().add(0, "T.C.: " + tcKimlik + " → Yeni durum: " + odaDurum);
            } else {
                boolean guncellendi = db.odaDurumDuzenleOdaNoIle(odaDurum, odaNo);
                if (!guncellendi) {
                    uyariGoster("Oda Bulunamadı", "Oda " + odaNo + " sistemde bulunamadı.");
                    return;
                }
                listOdaDurumGecmisi.getItems().add(0, "Oda " + odaNo + " → Yeni durum: " + odaDurum);
            }

            veritabanindanGuncelle();
            onOdaDuzenleTemizleAction();
            bilgiGoster("Oda Durumu Güncellendi", "Oda durumu '" + odaDurum + "' olarak güncellendi.");
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onOdaDuzenleTemizleAction() {
        txtOdaDuzenleTcKimlik.clear();
        txtOdaDuzenleOdaNo.clear();
        rbOdaDurumDolu.setSelected(true);
    }

    // --- 8. Müşteri Giriş Yaptı (Check-In) Ekranı Butonları ---
    @FXML
    private void onMusteriGirisSorgulaAction() {
        String telefon = txtMusteriGirisTelefon.getText() != null ? txtMusteriGirisTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen giriş yapan müşterinin telefon numarasını giriniz.");
            return;
        }
        try {
            Rezervasyon rez = db.rezervasyonGetirTelefonIle(telefon);
            if (rez == null) {
                lblGirisMusteriAdSoyad.setText("-");
                lblGirisAtananOdaNo.setText("-");
                lblGirisTarihAraligi.setText("-");
                lblGirisRezDurumBadge.setText("Bulunamadı");
                uyariGoster("Rezervasyon Bulunamadı", "Bu telefon numarasına ait onaylı bir rezervasyon bulunamadı.");
                return;
            }
            DateTimeFormatter trFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            lblGirisMusteriAdSoyad.setText(db.musteriAdSoyadGetir(rez.getMusteriId()));
            lblGirisAtananOdaNo.setText("Oda " + db.odaNoGetirIdIle(rez.getOdaId()));
            lblGirisTarihAraligi.setText(rez.getGirisTarihi().format(trFormat) + " - " + rez.getCikisTarihi().format(trFormat));
            lblGirisRezDurumBadge.setText("Onaylı • Girişe Hazır");
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onMusteriGirisOnaylaAction() {
        String telefon = txtMusteriGirisTelefon.getText() != null ? txtMusteriGirisTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen giriş yapan müşterinin telefon numarasını giriniz.");
            return;
        }
        try {
            boolean basarili = db.musteriGirisYapti(telefon);
            if (basarili) {
                veritabanindanGuncelle();
                txtMusteriGirisTelefon.clear();
                lblGirisMusteriAdSoyad.setText("-");
                lblGirisAtananOdaNo.setText("-");
                lblGirisTarihAraligi.setText("-");
                lblGirisRezDurumBadge.setText("Bekleniyor");
                bilgiGoster("Giriş Onaylandı", "Müşteri girişi onaylandı ve oda 'Dolu' durumuna alındı.");
            } else {
                uyariGoster("Giriş Yapılamadı", "Bu telefon numarasına ait giriş bekleyen onaylı bir rezervasyon bulunamadı.");
            }
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    // --- 9. Müşteri Çıkış Yaptı (Check-Out) Ekranı Butonları ---
    @FXML
    private void onMusteriCikisSorgulaAction() {
        String telefon = txtMusteriCikisTelefon.getText() != null ? txtMusteriCikisTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen çıkış yapan müşterinin telefon numarasını giriniz.");
            return;
        }
        try {
            Rezervasyon rez = db.rezervasyonGetirTelefonIle(telefon);
            if (rez == null) {
                uyariGoster("Kayıt Bulunamadı", "Bu telefon numarasına ait aktif bir konaklama bulunamadı.");
                return;
            }
            Fatura fatura = db.faturaGoruntule(telefon);
            int odendiMi = db.odendiMi(telefon);

            lblCikisMusteriVeOda.setText(db.musteriAdSoyadGetir(rez.getMusteriId()) + " • Oda " + db.odaNoGetirIdIle(rez.getOdaId()));
            lblCikisToplamUcret.setText(String.format(Locale.forLanguageTag("tr-TR"), "₺%,.2f", rez.getToplamUcret()));
            lblCikisFaturaDurumu.setText(fatura != null ? "Fatura Kesilmiş" : "Fatura Kesilmemiş");
            lblCikisOdemeDurumu.setText(odendiMi == 1 ? "Ödendi" : "Ödenmedi (Borç Var)");
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onMusteriCikisOnaylaAction() {
        String telefon = txtMusteriCikisTelefon.getText() != null ? txtMusteriCikisTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen çıkış yapan müşterinin telefon numarasını giriniz.");
            return;
        }
        try {
            boolean cikisBasarili = db.musteriCikisYapti(telefon);
            if (!cikisBasarili) {
                uyariGoster("Konaklama Bulunamadı", "Bu telefon numarasına ait aktif bir konaklama kaydı bulunamadı.");
                return;
            }

            Fatura fatura = db.faturaGoruntule(telefon);
            if (fatura == null) {
                Rezervasyon rez = db.rezervasyonGetirTelefonIle(telefon);
                if (rez != null) {
                    String baslik = txtCikisFaturaBaslik.getText() != null ? txtCikisFaturaBaslik.getText().trim() : "";
                    if (baslik.isEmpty()) {
                        baslik = db.musteriAdSoyadGetir(rez.getMusteriId());
                    }
                    String vergiNo = txtCikisVergiNo.getText() != null ? txtCikisVergiNo.getText().trim() : "";
                    if (vergiNo.isEmpty()) {
                        String temp = db.musteriTcGetirMusteriIdIle(rez.getMusteriId());
                        if (temp == null) {
                            uyariGoster("Bilgi Eksik", "Müşterinin T.C. Kimlik bilgisine ulaşılamadığı için fatura düzenlenemedi.");
                            return;
                        }
                        vergiNo = temp;
                    }
                    String adres = txtCikisFaturaAdresi.getText() != null ? txtCikisFaturaAdresi.getText().trim() : "";
                    if (adres.isEmpty()) {
                        adres = "Belirtilmedi";
                    }
                    String odemeYontemi = cmbCikisOdemeYontemi.getValue();

                    Fatura yeniFatura = new Fatura(rez.getRezId(), baslik, vergiNo, adres, rez.getToplamUcret(), odemeYontemi, LocalDate.now());
                    db.faturaKes(yeniFatura);
                }
            }
            if (db.odendiMi(telefon) == 0) {
                db.borcOde(telefon);
            }
            veritabanindanGuncelle();
            txtMusteriCikisTelefon.clear();
            txtCikisFaturaBaslik.clear();
            txtCikisVergiNo.clear();
            txtCikisFaturaAdresi.clear();
            bilgiGoster("Çıkış İşlemi Tamamlandı", "Müşteri çıkışı yapıldı, fatura ve tahsilat kapatıldı, oda temizliğe yönlendirildi.");

        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    // --- 10. Oda Temizlendi Ekranı Butonları ---
    @FXML
    private void onOdaTemizlendiOnaylaAction() {
        String odaNo = txtTemizlikBitenOdaNo.getText() != null ? txtTemizlikBitenOdaNo.getText().trim() : "";

        if (odaNo.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen temizliği biten oda numarasını giriniz.");
            return;
        }

        try {
            boolean basarili = db.temizlikTamamlandi(odaNo);

            if (basarili) {
                veritabanindanGuncelle();
                txtTemizlikBitenOdaNo.clear();
                txtTemizlikNotu.clear();
                bilgiGoster("Oda Hazır", "Oda " + odaNo + " temizlendi ve 'Müsait' durumuna alındı.");
            } else {
                uyariGoster("Oda Bulunamadı", odaNo + " numaralı bir oda sistemde bulunamadı.");
            }

        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    // --- 11. Rezervasyon İptal Ekranı Butonları ---
    @FXML
    private void onRezIptalSorgulaAction() {
        String telefon = txtRezIptalTelefon.getText() != null ? txtRezIptalTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen rezervasyon sahibinin telefon numarasını giriniz.");
            return;
        }

        try {
            Rezervasyon rez = db.rezervasyonGetirTelefonIle(telefon);
            if (rez == null) {
                lblIptalEdilecekRezId.setText("-");
                lblIptalEdilecekMusteriOda.setText("-");
                lblIptalEdilecekTarihler.setText("-");
                uyariGoster("Rezervasyon Bulunamadı", "Bu telefon numarasına ait aktif bir rezervasyon bulunamadı.");
                return;
            }
            DateTimeFormatter trFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            lblIptalEdilecekRezId.setText("#" + rez.getRezId());
            lblIptalEdilecekMusteriOda.setText(db.musteriAdSoyadGetir(rez.getMusteriId()) + " • Oda " + db.odaNoGetirIdIle(rez.getOdaId()));
            lblIptalEdilecekTarihler.setText(rez.getGirisTarihi().format(trFormat) + " - " + rez.getCikisTarihi().format(trFormat));
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onRezervasyonIptalOnaylaAction() {
        String telefon = txtRezIptalTelefon.getText() != null ? txtRezIptalTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen iptal edilecek rezervasyon sahibinin telefon numarasını giriniz.");
            return;
        }

        try {
            boolean iptalEdildi = db.rezIptal(telefon);
            if (iptalEdildi) {
                listSonIptalEdilenler.getItems().add(0, "İptal Edildi • Tel: " + telefon);
                txtRezIptalTelefon.clear();
                lblIptalEdilecekRezId.setText("-");
                lblIptalEdilecekMusteriOda.setText("-");
                lblIptalEdilecekTarihler.setText("-");
                veritabanindanGuncelle();
                bilgiGoster("Rezervasyon İptal Edildi", "Rezervasyon iptal edildi ve oda tekrar 'Müsait' durumuna alındı.");
            } else {
                uyariGoster("Rezervasyon Bulunamadı", "Bu telefon numarasına ait iptal edilebilecek aktif bir rezervasyon bulunamadı.");
            }
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    // --- 12. Borç Görüntüle ve Borç Öde Ekranı Butonları ---
    @FXML
    private void onBorcGoruntuleAction() {
        String telefon = txtBorcSorguTelefon.getText() != null ? txtBorcSorguTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen borcu sorgulanacak müşterinin telefon numarasını giriniz.");
            return;
        }

        try {
            List<String> borclar = db.borcGoruntule(telefon);

            listMusteriBorclari.getItems().setAll(borclar);
            lblBorcKayitAdedi.setText("Ödenmemiş " + borclar.size() + " adet kayıt");
            txtBorcOdemeTelefon.setText(telefon);

            if (borclar.isEmpty()) {
                lblBorcToplamTutar.setText("₺0,00");
                bilgiGoster("Borç Durumu", "Bu müşteriye ait ödenmemiş borç bulunmamaktadır.");
            }
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    @FXML
    private void onBorcOdeOnaylaAction() {
        String telefon = txtBorcOdemeTelefon.getText() != null ? txtBorcOdemeTelefon.getText().trim() : "";
        if (telefon.isEmpty()) {
            uyariGoster("Eksik Bilgi", "Lütfen tahsilat yapılacak müşterinin telefon numarasını giriniz.");
            return;
        }

        try {
            int odenenSayisi = db.borcOde(telefon);
            if (odenenSayisi > 0) {
                listMusteriBorclari.getItems().clear();
                lblBorcToplamTutar.setText("₺0,00");
                lblBorcKayitAdedi.setText("Ödenmemiş 0 adet rezervasyon kaydı");
                txtBorcOdemeTelefon.clear();
                veritabanindanGuncelle();
                bilgiGoster("Tahsilat Tamamlandı", odenenSayisi + " adet rezervasyon borcu ödendi olarak işlendi.");
            } else {
                uyariGoster("Borç Bulunamadı", "Bu telefon numarasına ait ödenmemiş bir borç kaydı bulunamadı.");
            }
        } catch (SQLException e) {
            uyariGoster("İşlem Başarısız", kullaniciDostuHata(e));
        }
    }

    // =========================================================================
    // VERİTABANINDAN (Otel.db) CANLI VERİ VE ODA TEMSİLİ YÜKLEME METODU
    // =========================================================================
    public void veritabanindanGuncelle() {
        if (db == null) return;

        try {
            List<Odalar> dbOdalar = db.tumOdalariGetir();

            odaTemsilListesi.clear();
            listKayitliOdalar.getItems().clear();
            listTemizlikBekleyenOdalar.getItems().clear();

            int doluSayisi = 0;
            int musaitSayisi = 0;
            int temizlenecekSayisi = 0;
            int bakimdaSayisi = 0;

            for (Odalar o : dbOdalar) {
                String durum = o.getOda_durum() != null ? o.getOda_durum() : "Müsait";
                String altDetay;

                switch (durum) {
                    case "Dolu" -> {
                        doluSayisi++;
                        altDetay = "Odada aktif konaklama mevcut";
                    }
                    case "Temizlenecek" -> {
                        temizlenecekSayisi++;
                        altDetay = "Çıkış yapıldı • Kat hizmeti bekliyor";
                        listTemizlikBekleyenOdalar.getItems().add(
                                "Oda " + o.getOda_no() + "  •  Kapasite: " + o.getOda_kapasite() + " Kişilik  •  Durum: Temizlenecek"
                        );
                    }
                    case "Bakımda" -> {
                        bakimdaSayisi++;
                        altDetay = "Teknik servis / bakımda";
                    }
                    default -> {
                        musaitSayisi++;
                        altDetay = "Yeni giriş ve rezervasyona hazır";
                    }
                }

                odaTemsilListesi.add(new OdaTemsilModeli(
                        o.getOda_no(),
                        o.getOda_kapasite(),
                        durum,
                        altDetay
                ));

                listKayitliOdalar.getItems().add(String.format(
                        Locale.forLanguageTag("tr-TR"),
                        "Oda %s  •  Kapasite: %d Kişilik  •  Durum: [%s]",
                        o.getOda_no(), o.getOda_kapasite(), durum
                ));
            }

            lblKayitliOdaSayisiBadge.setText(dbOdalar.size() + " Oda Kayıtlı");
            lblTemizlikBekleyenSayacBadge.setText(temizlenecekSayisi + " Oda Bekliyor");

            odaHaritasiniCiz();

            int gunlukGirisRez = db.bugunGirisYapacakRezSayisi();
            int gunlukGirisInsan = db.bugunGirisYapacakInsanSayisi();
            int gunlukCikisOda = db.bugunCikisYapacakOdaSayisi();
            int gunlukCikisInsan = db.bugunCikisYapacakInsanSayisi();
            double ciro = db.toplamOdenenCiroGetir();
            double bekleyenBorc = db.bekleyenToplamBorcGetir();

            dashboardIstatistikleriGuncelle(
                    doluSayisi,
                    musaitSayisi,
                    temizlenecekSayisi,
                    bakimdaSayisi,
                    gunlukGirisInsan,
                    gunlukGirisRez,
                    gunlukCikisInsan,
                    gunlukCikisOda,
                    ciro,
                    bekleyenBorc
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String kullaniciDostuHata(SQLException e) {
        e.printStackTrace();
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains("tc_kimlik")) {
            return "Bu T.C. Kimlik numarası sistemde zaten kayıtlı.";
        }
        if (msg.contains("telefon")) {
            return "Bu telefon numarası başka bir müşteride kayıtlı.";
        }
        if (msg.contains("email")) {
            return "Bu e-posta adresi başka bir müşteride kayıtlı.";
        }
        if (msg.contains("fatura.rezervasyon_id")) {
            return "Bu rezervasyon için fatura zaten kesilmiş.";
        }
        if (msg.contains("rezervasyon_misafir")) {
            return "Bu misafir zaten ilgili rezervasyona eklenmiş.";
        }
        if (msg.contains("FOREIGN KEY")) {
            return "İlişkili oda veya müşteri kaydı bulunamadığı için işlem tamamlanamadı.";
        }
        return "İşlem sırasında bir sorun oluştu. Lütfen girdiğiniz bilgileri kontrol edip tekrar deneyin.";
    }

    private void bilgiGoster(String baslik, String mesaj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
        }
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }

    private void uyariGoster(String baslik, String mesaj) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        if (rootPane != null && rootPane.getScene() != null && rootPane.getScene().getWindow() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
        }
        alert.setTitle(baslik);
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }
}
