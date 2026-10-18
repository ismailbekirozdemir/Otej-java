package com.example.otelotomasyon;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Rezervasyon {
    private int musteriId;
    private int odaId;
    private LocalDate girisTarihi;
    private LocalDate cikisTarihi;
    private double toplamUcret;
    private boolean odendiMi;
    private String durum;
    private int rezId;

    public Rezervasyon(int musteriId, int odaId, LocalDate girisTarihi,
                       LocalDate cikisTarihi, double gecelikUcret, boolean odendiMi){
        this.musteriId = musteriId;
        this.odaId = odaId;
        this.girisTarihi = girisTarihi;
        this.cikisTarihi = cikisTarihi;
        this.odendiMi = odendiMi;
        this.durum = "Onaylandı";
        long gunSayisi = ChronoUnit.DAYS.between(girisTarihi, cikisTarihi);
        this.toplamUcret = gunSayisi * gecelikUcret;
    }

    public int getOdaId() {
        return odaId;
    }

    public int getMusteriId() {
        return musteriId;
    }

    public LocalDate getCikisTarihi() {
        return cikisTarihi;
    }

    public LocalDate getGirisTarihi() {
        return girisTarihi;
    }

    public double getToplamUcret() {
        return toplamUcret;
    }

    public boolean isOdendiMi() {
        return odendiMi;
    }

    public String getDurum() {
        return durum;
    }

    public void setMusteriId(int musteriId) {
        this.musteriId = musteriId;
    }

    public void setDurum(String durum) {
        this.durum = durum;
    }

    public void setOdendiMi(boolean odendiMi) {
        this.odendiMi = odendiMi;
    }

    public void setOdaId(int odaId) {
        this.odaId = odaId;
    }

    public void setGirisTarihi(LocalDate girisTarihi) {
        this.girisTarihi = girisTarihi;
    }

    public void setToplamUcret(double toplamUcret) {
        this.toplamUcret = toplamUcret;
    }

    public void setCikisTarihi(LocalDate cikisTarihi) {
        this.cikisTarihi = cikisTarihi;
    }

    public int getRezId() {
        return rezId;
    }

    public void setRezId(int rezId) {
        this.rezId = rezId;
    }
}
