package com.example.chat_android;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.PropertyName;

public class MessageEntity
{
    @PropertyName("text")
    public String text;
    @PropertyName("from")
    public String from;
    @PropertyName("timestamp")
    public Object timestamp; // Usa Object per ServerTimestamp

    public MessageEntity()
    {
        from = "None";
        text = "";
        timestamp = new Object();
    }

    public MessageEntity(String text, String from)
    {
        this.text = text;
        this.from = from;
        this.timestamp = FieldValue.serverTimestamp();
    }
}