package com.example.ichat.Model;

public class StatusData {
    private String statusImageUrl;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private long timestamp;

    public StatusData(String statusImageUrl, String caption, long timestamp) {
        this.statusImageUrl = statusImageUrl;
        this.timestamp = timestamp;
        this.caption = caption;
    }

    public StatusData(String statusImageUrl, String caption) {
        this.statusImageUrl = statusImageUrl;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    private String caption;

    public String getStatusImageUrl() {
        return statusImageUrl;
    }

    public StatusData() {
    }

    public void setStatusImageUrl(String statusImageUrl) {
        this.statusImageUrl = statusImageUrl;
    }

    public StatusData(String statusImageUrl) {
        this.statusImageUrl = statusImageUrl;
    }
}
