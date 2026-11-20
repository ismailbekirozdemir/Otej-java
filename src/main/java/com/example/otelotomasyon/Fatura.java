package com.example.otelotomasyon;

import java.time.LocalDate;

public class Fatura {
    private int faturaId;
    private int rezervasyonId;
    private String faturaBasligi;
    private String vergiNo;
    private String faturaAdresi;
    private double toplamTutar;
    private LocalDate faturaTarihi;
    private String odemeYontemi;

    public Fatura(int rezervasyonId, String faturaBasligi, String vergiNo,
                  String faturaAdresi, double toplamTutar, String odemeYontemi, LocalDate faturaTarihi) {
        this.rezervasyonId = rezervasyonId;
        this.faturaBasligi = faturaBasligi;
        this.vergiNo = vergiNo;
        this.faturaAdresi = faturaAdresi;
        this.toplamTutar = toplamTutar;
        this.odemeYontemi = odemeYontemi;
        this.faturaTarihi = faturaTarihi;
    }

    public Fatura(int faturaId, int rezervasyonId, String faturaBasligi, String vergiNo,
                  String faturaAdresi, double toplamTutar, String odemeYontemi, LocalDate faturaTarihi) {
        this(rezervasyonId, faturaBasligi, vergiNo, faturaAdresi, toplamTutar, odemeYontemi, faturaTarihi);
        this.faturaId = faturaId;
    }

    public int getFaturaId() { return faturaId; }
    public void setFaturaId(int faturaId) { this.faturaId = faturaId; }

    public int getRezervasyonId() { return rezervasyonId; }
    public void setRezervasyonId(int rezervasyonId) { this.rezervasyonId = rezervasyonId; }

    public String getFaturaBasligi() { return faturaBasligi; }
    public void setFaturaBasligi(String faturaBasligi) { this.faturaBasligi = faturaBasligi; }

    public String getVergiNo() { return vergiNo; }
    public void setVergiNo(String vergiNo) { this.vergiNo = vergiNo; }

    public String getFaturaAdresi() { return faturaAdresi; }
    public void setFaturaAdresi(String faturaAdresi) { this.faturaAdresi = faturaAdresi; }

    public double getToplamTutar() { return toplamTutar; }
    public void setToplamTutar(double toplamTutar) { this.toplamTutar = toplamTutar; }

    public LocalDate getFaturaTarihi() { return faturaTarihi; }
    public void setFaturaTarihi(LocalDate faturaTarihi) { this.faturaTarihi = faturaTarihi; }

    public String getOdemeYontemi() { return odemeYontemi; }
    public void setOdemeYontemi(String odemeYontemi) { this.odemeYontemi = odemeYontemi; }
}