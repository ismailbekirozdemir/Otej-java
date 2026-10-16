package com.example.otelotomasyon;

public class Musteri {
    private int musteriId;
    private String ad;
    private String soyad;
    private String tcKimlik;
    private String telefon;
    private int yas;
    private String email;

    public Musteri(int musteriId, String ad, String soyad, String tcKimlik, String telefon, int yas, String email){
        this.musteriId = musteriId;
        this.ad = ad;
        this.soyad = soyad;
        this.tcKimlik = tcKimlik;
        this.telefon = telefon;
        this.yas = yas;
        this.email = email;
    }

    public Musteri(String ad, String soyad, String tcKimlik, String telefon, int yas, String email){
        this.ad = ad;
        this.soyad = soyad;
        this.tcKimlik = tcKimlik;
        this.telefon = telefon;
        this.yas = yas;
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
    public int getYas() { return yas; }
    public String getEmail() { return email; }

    public void setAd(String ad){this.ad = ad;}
    public void setSoyad(String soyad){this.soyad = soyad;}
    public void setTcKimlik(String tcKimlik){this.tcKimlik = tcKimlik;}
    public void setTelefon(String telefon){this.telefon = telefon;}
    public void setYas(int yas){this.yas = yas;}
    public void setEmail(String email){this.email = email;}

}
