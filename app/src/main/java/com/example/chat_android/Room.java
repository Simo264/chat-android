package com.example.chat_android;

import java.util.ArrayList;

public class Room
{
    public String name = "";
    public String creator_uid = "";
    public ArrayList<String> user_uids = null;
    public boolean is_delete = false;

    public Room() {}

    public Room(String room_name, String creator_uid, ArrayList<String> users)
    {
        this.name = room_name;
        this.creator_uid = creator_uid;
        this.user_uids = users;
    }

    @Override
    public String toString()
    {
        return String.format("Room: name=%s creator_uid=%s user_count=%d", name, creator_uid, user_uids.size());
    }

    public int getUserCount()
    {
        return user_uids.size();
    }
}
