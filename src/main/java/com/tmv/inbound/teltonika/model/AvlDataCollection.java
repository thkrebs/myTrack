package com.tmv.inbound.teltonika.model;

import java.util.List;

public class AvlDataCollection {
    private  int codecId;
    private  int dataCount;

 private  String imei;
    private  List<AvlData> data;

    public AvlDataCollection() {};

    private AvlDataCollection(int codecId, int dataCount, List<AvlData> data) {
        this.codecId = codecId;
        this.dataCount = dataCount;
        this.data = data;
    }

    public int getCodecId() {
        return codecId;
    }

    public int getDataCount() {
        return dataCount;
    }

    public List<AvlData> getData() {
        return data;
    }

    public String getImei() { return imei ;}

    public void setImei(String imei) { this.imei = imei; }



    public static AvlDataCollection create(int codecId, int dataCount, List<AvlData> data) {
        return new AvlDataCollection(codecId, dataCount, data);
    }
}