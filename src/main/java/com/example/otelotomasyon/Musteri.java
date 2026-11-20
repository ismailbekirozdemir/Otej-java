package com.example.otelotomasyon;

import java.time.LocalDate;

public class Musteri {
    private int musteriId;
    private String ad;
    private String soyad;
    private String tcKimlik;
    private String telefon;
    private LocalDate dogumTarihi;
    private String email;

    public Musteri(int musteriId, String ad, String soyad, String tcKimlik, String telefon, LocalDate dogumTarihi, String email){
        this.musteriId = musteriId;
        this.ad = ad;
        this.soyad = soyad;
        this.tcKimlik = tcKimlik;
        this.telefon = telefon;
        this.dogumTarihi = dogumTarihi;
        this.email = email;
    }

    public Musteri(String ad, String soyad, String tcKimlik, String telefon, LocalDate dogumTarihi, String email){
        this.ad = ad;
        this.soyad = soyad;
        this.tcKimlik = tcKimlik;
        this.telefon = telefon;
        this.dogumTarihi = dogumTarihi;
        this.email = email;
    }

    public void setMusteriId(int musteriId) {
        this.musteriId = musteriId;
    }

    public int getMusteriId() {
        return musteriId;
    }

    public String getAd() { return ad; }
    public String getSoyad() { return soyad; }
    public String getTcKimlik() { return tcKimlik; }
    public String getTelefon() { return telefon; }
    public LocalDate getDogumTarihi() { return dogumTarihi; }
    public String getEmail() { return email; }

    public void setAd(String ad){this.ad = ad;}
    public void setSoyad(String soyad){this.soyad = soyad;}
    public void setTcKimlik(String tcKimlik){this.tcKimlik = tcKimlik;}
    public void setTelefon(String telefon){this.telefon = telefon;}
    public void setDogumTarihi(LocalDate dogumTarihi){this.dogumTarihi = dogumTarihi;}
    public void setEmail(String email){this.email = email;}

}
