package com.example.chat_android;

import java.util.ArrayList;

public interface RoomsListener
{
    void onRoomsUpdated(ArrayList<Room> rooms);
    void onError(Exception e);
}
