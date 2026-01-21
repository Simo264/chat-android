package com.example.chat_android;

import com.google.firebase.firestore.PropertyName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Room
{
    @PropertyName("name")
    public String name;
    @PropertyName("creator_uid")
    public String creator_uid;
    @PropertyName("users")
    public ArrayList<String> users; // array di user id
    @PropertyName("is_delete")
    public boolean is_delete;

    public Room()
    {
        name = "None";
        creator_uid = "None";
        users = new ArrayList<>();
        is_delete = false;
    }

    public Room(@NotNull String room_name, @NotNull String creator_uid, @NotNull ArrayList<String> users)
    {
        this.name = room_name;
        this.creator_uid = creator_uid;
        this.users = users;
        this.is_delete = false;
    }

    @Override
    public String toString()
    {
        return String.format("Room: name=%s creator_uid=%s user_count=%d", name, creator_uid, users.size());
    }

    @PropertyName("users")
    public ArrayList<String> getUserList() { return this.users; }

    public int getUserCount()
    {
        return users.size();
    }
}
