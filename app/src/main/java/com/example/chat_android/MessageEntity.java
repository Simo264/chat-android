package com.example.chat_android;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.PropertyName;

import org.jetbrains.annotations.NotNull;

public class MessageEntity
{
    @PropertyName("from")
    public String from;
    @PropertyName("text")
    public String text;
    @PropertyName("media_url")
    public String media_url; // URL pubblico da Firebase Storage
    @PropertyName("timestamp")
    public Object timestamp;

    public MessageEntity()
    {
        this.from = "None";
        this.text = "";
        this.media_url = "";
        this.timestamp = new Object();
    }

    public MessageEntity(@NotNull String text, @NotNull String media_url, @NotNull String from)
    {
        this.text = text;
        this.from = from;
        this.media_url = media_url;
        this.timestamp = FieldValue.serverTimestamp();
    }
}