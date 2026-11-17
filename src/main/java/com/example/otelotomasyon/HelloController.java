package com.example.otelotomasyon;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

public class HelloController {

    // ==================== SIDEBAR NAVIGATION BUTTONS ====================
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavRooms;
    @FXML private Button btnNavBookings;
    @FXML private Button btnNavCheckInOut;
    @FXML private Button btnNavGuests;
    @FXML private Button btnNavHousekeeping;
    @FXML private Button btnNavBilling;

    // ==================== TOP HEADER LABELS ====================
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;

    // ==================== PAGES (STACKPANE CHILDREN) ====================
    @FXML private VBox pageOverview;
    @FXML private VBox pageRooms;
    @FXML private VBox pageBookings;
    @FXML private VBox pageCheckInOut;
    @FXML private VBox pageGuests;
    @FXML private VBox pageHousekeeping;
    @FXML private VBox pageBilling;

    // ==================== OVERVIEW (DASHBOARD) KPI LABELS ====================
    @FXML private Label lblOccupiedRooms;
    @FXML private Label lblAvailableRooms;
    @FXML private Label lblOccupancyRate;
    @FXML private Label lblTodayCheckIns;
    @FXML private Label lblTodayCheckOuts;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblActionCount;

    // ==================== AVAILABILITY CALENDAR ====================
    @FXML private HBox calendarDaysBox;
    @FXML private Button btn14Days;
    @FXML private Button btn30Days;

    // ==================== ROOMS PAGE CONTROLS ====================
    @FXML private TextField txtOdaNo;
    @FXML private ComboBox<String> cmbOdaTip;
    @FXML private TextField txtOdaKapasite;
    @FXML private TextField txtGunlukUcret;
    @FXML private Label lblOdaKayitSonuc;

    @FXML private TextField txtOdaDurumTc;
    @FXML private ComboBox<String> cmbYeniOdaDurum;
    @FXML private Label lblOdaDurumSonuc;
    @FXML private FlowPane roomsFlowPane;

    // ==================== BOOKINGS PAGE CONTROLS ====================
    @FXML private TextField txtRezTc;
    @FXML private TextField txtRezOdaNo;
    @FXML private DatePicker dpGirisTarihi;
    @FXML private DatePicker dpCikisTarihi;
    @FXML private TextField txtRezGecelikUcret;
    @FXML private CheckBox chkFaturaGiriste;
    @FXML private Label lblRezSonuc;

    @FXML private TextField txtRezIptalTel;
    @FXML private Label lblRezIptalSonuc;

    // ==================== CHECK-IN / CHECK-OUT CONTROLS ====================
    @FXML private TextField txtCheckInTel;
    @FXML private Label lblCheckInSonuc;
    @FXML private TextField txtCheckOutTel;
    @FXML private Label lblCheckOutSonuc;

    // ==================== GUESTS PAGE CONTROLS ====================
    @FXML private TextField txtMusteriAd;
    @FXML private TextField txtMusteriSoyad;
    @FXML private TextField txtMusteriTc;
    @FXML private TextField txtMusteriTel;
    @FXML private TextField txtMusteriYas;
    @FXML private TextField txtMusteriEmail;
    @FXML private Label lblMusteriSonuc;

    @FXML private TextField txtRezSahibiTc;
    @FXML private TextField txtMisafirTc;
    @FXML private Label lblMisafirSonuc;

    // ==================== HOUSEKEEPING PAGE CONTROLS ====================
    @FXML private TextField txtTemizlikOdaNo;
    @FXML private Label lblTemizlikSonuc;

    // ==================== BILLING & DEBT PAGE CONTROLS ====================
    @FXML private TextField txtFaturaRezId;
    @FXML private TextField txtFaturaBaslik;
    @FXML private TextField txtFaturaVergiNo;
    @FXML private ComboBox<String> cmbOdemeYontemi;
    @FXML private TextField txtFaturaTutar;
    @FXML private TextField txtFaturaAdres;
    @FXML private Label lblFaturaKesSonuc;

    @FXML private TextField txtSorguTelefon;
    @FXML private TextArea txtFaturaVeBorcDetay;

    @FXML
    public void initialize() {
        // ComboBox başlangıç seçenekleri
        cmbOdaTip.setItems(FXCollections.observableArrayList("Standart", "Deluxe", "Suit", "Aile Odası", "Kral Dairesi"));
        cmbYeniOdaDurum.setItems(FXCollections.observableArrayList("Müsait", "Dolu", "Temizlenecek"));
        cmbOdemeYontemi.setItems(FXCollections.observableArrayList("Nakit", "Kredi Kartı", "Havale/EFT"));

        // Varsayılan tarih seçimleri
        dpGirisTarihi.setValue(LocalDate.now());
        dpCikisTarihi.setValue(LocalDate.now().plusDays(2));

        // InnGO Availability Calendar (14 Günlük Görünüm) oluştur
        populateAvailabilityCalendar(14);

        // Odalar sayfasındaki örnek oda kartlarını oluştur
        populateSampleRoomCards();
    }

    // =========================================================================
    // AVAILABILITY CALENDAR GENERATOR (Exact dash.png style)
    // =========================================================================
    private void populateAvailabilityCalendar(int daysCount) {
        calendarDaysBox.getChildren().clear();

        String[] dayNames = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        String[] dates = {
            "28 Sep", "29 Sep", "30 Sep", "1 Oct", "2 Oct", "3 Oct", "4 Oct",
            "5 Oct", "6 Oct", "7 Oct", "8 Oct", "9 Oct", "10 Oct", "11 Oct"
        };
        int[] availCounts = {26, 25, 24, 21, 18, 16, 20, 24, 27, 23, 19, 17, 22, 25};
        int totalRooms = 80;

        for (int i = 0; i < daysCount; i++) {
            String dayName = dayNames[i % 7];
            String dateText = (i < dates.length) ? dates[i] : (i - 2) + " Oct";
            int avail = availCounts[i % availCounts.length];
            int occ = totalRooms - avail;
            double ratio = (double) avail / totalRooms;

            VBox dayCard = createDayCard(dayName, dateText, avail, occ, ratio, i == 0);
            calendarDaysBox.getChildren().add(dayCard);
        }
    }

    private VBox createDayCard(String dayName, String dateText, int avail, int occ, double ratio, boolean isSelected) {
        VBox card = new VBox(5);
        card.getStyleClass().add(isSelected ? "day-card-selected" : "day-card");

        Label lblDay = new Label(dayName);
        lblDay.getStyleClass().add(isSelected ? "day-name-selected" : "day-name");

        Label lblDate = new Label(dateText);
        lblDate.getStyleClass().add("day-date");

        // Mini yeşil doluluk çubuğu (dash.png'deki gibi)
        HBox barTrack = new HBox();
        barTrack.setMinHeight(3);
        barTrack.setMaxHeight(3);
        barTrack.setPrefWidth(54);
        barTrack.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 2;");

        Region barFill = new Region();
        barFill.setMinHeight(3);
        barFill.setMaxHeight(3);
        barFill.setPrefWidth(Math.max(8, 54 * ratio));
        barFill.setStyle("-fx-background-color: #22C55E; -fx-background-radius: 2;");
        barTrack.getChildren().add(barFill);

        Label lblAvail = new Label(avail + " avail");
        lblAvail.getStyleClass().add("day-avail");

        Label lblOcc = new Label(occ + " occ");
        lblOcc.getStyleClass().add("day-occ");

        card.getChildren().addAll(lblDay, lblDate, barTrack, lblAvail, lblOcc);

        // Tıklandığında mavi seçili çerçeveyi o güne taşı
        card.setOnMouseClicked(event -> {
            for (Node node : calendarDaysBox.getChildren()) {
                if (node instanceof VBox vbox) {
                    vbox.getStyleClass().removeAll("day-card-selected", "day-card");
                    vbox.getStyleClass().add("day-card");
                    if (!vbox.getChildren().isEmpty() && vbox.getChildren().get(0) instanceof Label firstLbl) {
                        firstLbl.getStyleClass().removeAll("day-name-selected", "day-name");
                        firstLbl.getStyleClass().add("day-name");
                    }
                }
            }
            card.getStyleClass().remove("day-card");
            card.getStyleClass().add("day-card-selected");
            lblDay.getStyleClass().remove("day-name");
            lblDay.getStyleClass().add("day-name-selected");
        });

        return card;
    }

    @FXML
    private void onSelect14Days() {
        btn14Days.getStyleClass().setAll("pill-toggle-active");
        btn30Days.getStyleClass().setAll("pill-toggle-inactive");
        populateAvailabilityCalendar(14);
    }

    @FXML
    private void onSelect30Days() {
        btn30Days.getStyleClass().setAll("pill-toggle-active");
        btn14Days.getStyleClass().setAll("pill-toggle-inactive");
        populateAvailabilityCalendar(30);
    }

    // =========================================================================
    // SAMPLE ROOM CARDS PREVIEW (Odalar Sayfası İçin Görsel Kartlar)
    // =========================================================================
    private void populateSampleRoomCards() {
        roomsFlowPane.getChildren().clear();
        addVisualRoomCard("101", "Standart", 2, 2200, "Müsait");
        addVisualRoomCard("102", "Deluxe", 2, 3400, "Temizlenecek");
        addVisualRoomCard("103", "Suit", 4, 5200, "Dolu");
        addVisualRoomCard("201", "Standart", 2, 2200, "Müsait");
        addVisualRoomCard("204", "Aile Odası", 4, 4500, "Dolu");
        addVisualRoomCard("301", "Kral Dairesi", 4, 8500, "Temizlenecek");
    }

    private void addVisualRoomCard(String odaNo, String odaTip, int kapasite, double ucret, String durum) {
        VBox card = new VBox(6);
        card.setPrefWidth(195);

        String badgeColor;
        if ("Müsait".equalsIgnoreCase(durum)) {
            card.getStyleClass().add("room-card-avail");
            badgeColor = "#16A34A";
        } else if ("Dolu".equalsIgnoreCase(durum)) {
            card.getStyleClass().add("room-card-occupied");
            badgeColor = "#DC2626";
        } else {
            card.getStyleClass().add("room-card-cleaning");
            badgeColor = "#D97706";
        }

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label lblNo = new Label("Oda " + odaNo);
        lblNo.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0F172A;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lblDurum = new Label(durum);
        lblDurum.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: " + badgeColor + ";");
        topRow.getChildren().addAll(lblNo, spacer, lblDurum);

        Label lblTip = new Label(odaTip + " • " + kapasite + " Kişilik");
        lblTip.setStyle("-fx-font-size: 12.5px; -fx-text-fill: #475569;");

        Label lblFiyat = new Label("₺" + ucret + " / gece");
        lblFiyat.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #1E293B;");

        card.getChildren().addAll(topRow, lblTip, lblFiyat);
        roomsFlowPane.getChildren().add(card);
    }

    // =========================================================================
    // SIDEBAR PAGE SWITCHING LOGIC
    // =========================================================================
    private void switchPage(VBox activePage, Button activeButton, String title, String subtitle) {
        List<VBox> pages = List.of(
            pageOverview, pageRooms, pageBookings, pageCheckInOut,
            pageGuests, pageHousekeeping, pageBilling
        );
        for (VBox page : pages) {
            boolean isTarget = (page == activePage);
            page.setVisible(isTarget);
            page.setManaged(isTarget);
        }

        List<Button> navButtons = List.of(
            btnNavOverview, btnNavRooms, btnNavBookings, btnNavCheckInOut,
            btnNavGuests, btnNavHousekeeping, btnNavBilling
        );
        for (Button btn : navButtons) {
            btn.getStyleClass().removeAll("nav-btn-active", "nav-btn");
            btn.getStyleClass().add(btn == activeButton ? "nav-btn-active" : "nav-btn");
        }

        lblPageTitle.setText(title);
        lblPageSubtitle.setText(subtitle);
    }

    @FXML
    private void showOverviewPage() {
        switchPage(pageOverview, btnNavOverview, "Overview", "Today's live hotel performance");
    }

    @FXML
    private void showRoomsPage() {
        switchPage(pageRooms, btnNavRooms, "Rooms Management", "Yeni oda ekleme ve oda durumlarını yönetme paneli");
    }

    @FXML
    private void showBookingsPage() {
        switchPage(pageBookings, btnNavBookings, "Bookings & Reservations", "Yeni rezervasyon oluşturma, müsaitlik kontrolü ve iptal işlemleri");
    }

    @FXML
    private void showCheckInOutPage() {
        switchPage(pageCheckInOut, btnNavCheckInOut, "Check-In & Check-Out", "Müşteri otele giriş ve çıkış işlemleri");
    }

    @FXML
    private void showGuestsPage() {
        switchPage(pageGuests, btnNavGuests, "Guests & Companions", "Müşteri kaydı oluşturma ve aktif odaya misafir ekleme");
    }

    @FXML
    private void showHousekeepingPage() {
        switchPage(pageHousekeeping, btnNavHousekeeping, "Housekeeping", "Temizliği tamamlanan odaları müsait duruma getirme");
    }

    @FXML
    private void showBillingPage() {
        switchPage(pageBilling, btnNavBilling, "Billing & Invoices", "Fatura kesme, görüntüleme ve müşteri borç ödeme ekranı");
    }

    // =========================================================================
    // UI ACTION HANDLERS (Veritabanı bağlantılarını ekleyeceğin metotlar)
    // =========================================================================

    @FXML
    private void onOdaEkleClick() {
        String odaNo = txtOdaNo.getText().trim();
        String odaTip = cmbOdaTip.getValue() != null ? cmbOdaTip.getValue() : "Standart";
        String kapasiteStr = txtOdaKapasite.getText().trim();
        String ucretStr = txtGunlukUcret.getText().trim();

        if (odaNo.isEmpty() || kapasiteStr.isEmpty() || ucretStr.isEmpty()) {
            lblOdaKayitSonuc.setText("⚠ Lütfen tüm oda bilgilerini doldurun.");
            return;
        }

        // TODO: Sql.odaEkle(new Odalar(...)) bağlantısını buraya ekleyebilirsin.
        addVisualRoomCard(odaNo, odaTip, Integer.parseInt(kapasiteStr), Double.parseDouble(ucretStr), "Müsait");
        lblOdaKayitSonuc.setText("✓ Oda #" + odaNo + " arayüze eklendi.");
    }

    @FXML
    private void onOdaDurumGuncelleClick() {
        String tc = txtOdaDurumTc.getText().trim();
        String yeniDurum = cmbYeniOdaDurum.getValue();

        // TODO: Sql.odaDurumDuzenle(yeniDurum, tc) bağlantısını buraya ekleyebilirsin.
        lblOdaDurumSonuc.setText("✓ TC: " + tc + " için oda durumu '" + yeniDurum + "' olarak seçildi.");
    }

    @FXML
    private void onMusteriMusaitlikKontrolClick() {
        // TODO: Sql.odaMusaitMi(...) bağlantısını buraya ekleyebilirsin.
        lblRezSonuc.setText("✓ Seçilen tarihler için müsaitlik kontrolü tetiklendi.");
    }

    @FXML
    private void onRezervasyonOlusturClick() {
        // TODO: Sql.rezervasyonEkle(...) bağlantısını buraya ekleyebilirsin.
        lblRezSonuc.setText("✓ Rezervasyon oluşturma işlemi tetiklendi.");
    }

    @FXML
    private void onRezervasyonIptalClick() {
        String tel = txtRezIptalTel.getText().trim();
        // TODO: Sql.rezIptal(tel) bağlantısını buraya ekleyebilirsin.
        lblRezIptalSonuc.setText("✓ " + tel + " numaralı müşterinin rezervasyon iptal isteği alındı.");
    }

    @FXML
    private void onCheckInClick() {
        String tel = txtCheckInTel.getText().trim();
        // TODO: Sql.musteriGirisYapti(tel) bağlantısını buraya ekleyebilirsin.
        lblCheckInSonuc.setText("✓ " + tel + " için Check-In (Giriş) işlemi tetiklendi.");
    }

    @FXML
    private void onCheckOutClick() {
        String tel = txtCheckOutTel.getText().trim();
        // TODO: Sql.musteriCikisYapti(tel) bağlantısını buraya ekleyebilirsin.
        lblCheckOutSonuc.setText("✓ " + tel + " için Check-Out (Çıkış) işlemi tetiklendi.");
    }

    @FXML
    private void onMusteriEkleClick() {
        // TODO: Sql.musteriEkle(...) bağlantısını buraya ekleyebilirsin.
        lblMusteriSonuc.setText("✓ Müşteri kaydı: " + txtMusteriAd.getText() + " " + txtMusteriSoyad.getText());
    }

    @FXML
    private void onMisafirEkleClick() {
        // TODO: Sql.musteriMisafirEkle(...) bağlantısını buraya ekleyebilirsin.
        lblMisafirSonuc.setText("✓ Misafir (" + txtMisafirTc.getText() + ") odaya bağlandı.");
    }

    @FXML
    private void onTemizlikTamamlandiClick() {
        String odaNo = txtTemizlikOdaNo.getText().trim();
        // TODO: Sql.temizlikTamamlandi(odaNo) bağlantısını buraya ekleyebilirsin.
        lblTemizlikSonuc.setText("✓ Oda " + odaNo + " temizlendi olarak işaretlendi.");
    }

    @FXML
    private void onFaturaKesClick() {
        // TODO: Sql.faturaKes(...) bağlantısını buraya ekleyebilirsin.
        lblFaturaKesSonuc.setText("✓ Fatura oluşturma işlemi tetiklendi.");
    }

    @FXML
    private void onFaturaGoruntuleClick() {
        String tel = txtSorguTelefon.getText().trim();
        // TODO: Sql.faturaGoruntule(tel) bağlantısını buraya ekleyebilirsin.
        txtFaturaVeBorcDetay.setText("📄 Telefon: " + tel + " için fatura sorgulandı.\n(Veritabanı bağlantısını buraya ekleyebilirsiniz)");
    }

    @FXML
    private void onBorcGoruntuleClick() {
        String tel = txtSorguTelefon.getText().trim();
        // TODO: Sql.borcGoruntule(tel) bağlantısını buraya ekleyebilirsin.
        txtFaturaVeBorcDetay.setText("🔍 Telefon: " + tel + " için borç listesi sorgulandı.\n(Veritabanı bağlantısını buraya ekleyebilirsiniz)");
    }

    @FXML
    private void onBorcOdeClick() {
        String tel = txtSorguTelefon.getText().trim();
        // TODO: Sql.borcOde(tel) bağlantısını buraya ekleyebilirsin.
        txtFaturaVeBorcDetay.setText("💳 Telefon: " + tel + " için borç ödeme işlemi tetiklendi.");
    }
}
