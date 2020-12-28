package com.example.beaconbattery.data;

public class EndTimeData {
    private Long firstTime;
    private String name;
    private Long endTime;
    private Long lastTime;

    public EndTimeData(Long firstTime, Long lastTime, String name, Long endTime) {
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.name = name;
        this.endTime = endTime;
    }

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Long getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(Long firstTime) {
        this.firstTime = firstTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
