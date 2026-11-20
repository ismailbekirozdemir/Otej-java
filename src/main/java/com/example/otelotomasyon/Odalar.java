package com.example.otelotomasyon;

public class Odalar {
    private String oda_no;
    private int oda_kapasite;
    private int oda_id;
    private String oda_durum = "Müsait";

    public Odalar(String oda_no,int oda_kapasite){
        this.oda_no = oda_no;
        this.oda_kapasite = oda_kapasite;
    }

    public Odalar(int oda_id, String oda_no, int oda_kapasite, String oda_durum){
        this.oda_id = oda_id;
        this.oda_no = oda_no;
        this.oda_kapasite = oda_kapasite;
        this.oda_durum = oda_durum != null ? oda_durum : "Müsait";
    }

    public String getOda_durum() { return oda_durum; }

    public void setOdaId(int odaId) { this.oda_id = odaId; }

    public String getOda_no() {return oda_no;}

    public int getOda_kapasite() {return oda_kapasite;}
}
