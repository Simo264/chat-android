package com.example.chat_android;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
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
    @PropertyName("media_type")
    public String media_type;  // "IMAGE" | "VIDEO" | ""
    @PropertyName("timestamp")
    public Timestamp timestamp;
    @Exclude
    public String message_id;

    public static final String MEDIA_IMAGE = "IMAGE";
    public static final String MEDIA_VIDEO = "VIDEO";

    public MessageEntity()
    {
        this.from = "";
        this.text = "";
        this.media_url = "";
        this.media_type = "";
        this.timestamp = Timestamp.now();
        this.message_id = "";
    }

    public MessageEntity(@NotNull String text,
                         @NotNull String media_url,
                         @NotNull String media_type,
                         @NotNull String from)
    {
        this.text = text;
        this.from = from;
        this.media_url = media_url;
        this.media_type = media_type;
        this.timestamp = Timestamp.now();
        this.message_id = "";
    }
}