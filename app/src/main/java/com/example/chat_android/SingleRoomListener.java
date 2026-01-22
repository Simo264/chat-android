package com.example.chat_android;

public interface SingleRoomListener
{
    void onRoomUpdated(Room room);
    void onError(Exception e);
}
