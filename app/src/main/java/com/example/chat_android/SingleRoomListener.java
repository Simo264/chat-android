package com.example.chat_android;

public interface SingleRoomListener
{
    void onRoomUpdated(RoomParcel room);
    void onError(Exception e);
}
