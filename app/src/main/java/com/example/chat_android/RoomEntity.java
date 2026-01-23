package com.example.chat_android;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;

public class RoomEntity
{
    @PropertyName("name")
    public String name;
    @PropertyName("creator_name")
    public String creator_name;
    @PropertyName("users")
    public ArrayList<String> users;
    @PropertyName("is_delete")
    public boolean is_delete;

    public RoomEntity()
    {
        name = "";
        creator_name = "";
        users = new ArrayList<>();
        is_delete = false;
    }

    public RoomEntity(String room_name, String creator_name)
    {
        this.name = room_name;
        this.creator_name = creator_name;
        this.users = new ArrayList<>();
        this.is_delete = false;
    }
}

