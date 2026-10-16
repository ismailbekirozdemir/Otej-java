package com.example.otelotomasyon;

public class Odalar {
    private String oda_no;
    private String oda_tip;
    private int oda_kapasite;
    private double gunluk_ucret;
    private int oda_id;

    public Odalar(String oda_no, String oda_tip, int oda_kapasite, double gunluk_ucret){
        this.oda_no = oda_no;
        this.oda_tip = oda_tip;
        this.oda_kapasite = oda_kapasite;
        this.gunluk_ucret = gunluk_ucret;
    }

    public int getOdaId() { return oda_id; }

    public void setOdaId(int odaId) { this.oda_id = odaId; }

    public void setOda_no(String oda_no) {this.oda_no = oda_no;}

    public void setGunluk_ucret(double gunluk_ucret) {this.gunluk_ucret = gunluk_ucret;}

    public void setOda_kapasite(int oda_kapasite) {this.oda_kapasite = oda_kapasite;}

    public void setOda_tip(String oda_tip) {
        this.oda_tip = oda_tip;
    }

    public String getOda_no() {return oda_no;}

    public String getOda_tip() {return oda_tip;}

    public int getOda_kapasite() {return oda_kapasite;}

    public double getGunluk_ucret() {return gunluk_ucret;}
}
