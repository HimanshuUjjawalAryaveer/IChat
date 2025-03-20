package com.example.ichat.Model;

public class Chats {
    private String receiver;
    private String sender;
    private String message;
    private int feeling;
    private String chatId;

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    private String messageType;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private long timestamp;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    public Chats(String receiver, String sender, String message, String time, boolean seen) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.seen = seen;
    }

    public Chats(String receiver, String sender, String message, String time, String date, boolean seen) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
        this.time = time;
        this.date = date;
        this.seen = seen;
    }
    private String time;

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    private boolean seen;




    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Chats(String receiver, String sender, String message, String time) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
        this.time = time;
    }

    public Chats() {

    }
    public Chats(String receiver, String sender, String message) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
    }
}
