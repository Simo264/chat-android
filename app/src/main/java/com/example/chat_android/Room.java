package com.example.chat_android;

public class Room
{
    public String name = "";
    public String creator_uid = "";
    public Integer user_count = 0;

    public Room() {}

    public Room(String room_name, String creator_uid, Integer user_count)
    {
        this.name = room_name;
        this.creator_uid = creator_uid;
        this.user_count = user_count;
    }

    public String toString()
    {
        return String.format("Room: name=%s creator_uid=%s user_count=%d", name, creator_uid, user_count);
    }
}
