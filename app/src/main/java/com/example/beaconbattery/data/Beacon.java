package com.example.beaconbattery.data;

public class Beacon {
    private String address;
    private long now;
    private String name;

    public Beacon(String address, String name,long now) {
        this.address = address;
        this.name = name;
        this.now = now;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }
}
