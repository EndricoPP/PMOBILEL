package com.example.praktikum;

public class Item {
    private String kode, nama, satuan;
    private int harga;

    public Item() {} // Diperlukan Firebase saat membaca data

    public Item(String kode, String nama, String satuan, int harga) {
        this.kode = kode;
        this.nama = nama;
        this.satuan = satuan;
        this.harga = harga;
    }

    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public int getHarga() { return harga; }
    public void setHarga(int harga) { this.harga = harga; }
}
