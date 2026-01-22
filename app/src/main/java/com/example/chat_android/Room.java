package com.example.chat_android;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.PropertyName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Room implements Parcelable
{
    public String name;
    public String creator_uid;
    public String creator_name;
    public ArrayList<String> users;
    public boolean is_delete;

    public Room()
    {
        name = "None";
        creator_uid = "None";
        creator_name = "None";
        users = new ArrayList<>();
        is_delete = false;
    }

    public Room(@NotNull String room_name,
                @NotNull String creator_uid,
                @NotNull String creator_name,
                @NotNull ArrayList<String> users)
    {
        this.name = room_name;
        this.creator_uid = creator_uid;
        this.creator_name = creator_name;
        this.users = users;
        this.is_delete = false;
    }

    protected Room(Parcel in)
    {
        name = in.readString();
        creator_uid = in.readString();
        creator_name = in.readString();
        users = in.createStringArrayList();
        is_delete = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(creator_uid);
        dest.writeString(creator_name);
        dest.writeStringList(users);
        dest.writeByte((byte) (is_delete ? 1 : 0));
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<Room> CREATOR = new Creator<Room>()
    {
        @Override
        public Room createFromParcel(Parcel in)
        {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size)
        {
            return new Room[size];
        }
    };


    @Override
    public String toString()
    {
        return String.format("Room: name=%s creator_uid=%s creator_name=%s user_count=%d",
                name, creator_uid, creator_name, users.size());
    }

    public ArrayList<String> getUserList() { return this.users; }

    public int getUserCount()
    {
        return users.size();
    }
}
