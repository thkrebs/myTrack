package com.tmv.ingest.teltonika.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class AvlDataCollection {

    @Getter private  int codecId;

    @Getter private  int dataCount;

    @Getter @Setter private  String imei;

    @Getter private  List<AvlData> data;

    public AvlDataCollection() {};

    private AvlDataCollection(int codecId, int dataCount, List<AvlData> data) {
        this.codecId = codecId;
        this.dataCount = dataCount;
        this.data = data;
    }

    public static AvlDataCollection create(int codecId, int dataCount, List<AvlData> data) {
        return new AvlDataCollection(codecId, dataCount, data);
    }
}