package com.example.chat_android;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class RoomParcel implements Parcelable
{
    public String name;
    public String creator_name;
    public ArrayList<String> users;
    public boolean is_delete;

    public RoomParcel(RoomEntity e)
    {
        this.name = e.name;
        this.creator_name = e.creator_name;
        this.users = e.users;
        this.is_delete = e.is_delete;
    }

    protected RoomParcel(Parcel in)
    {
        name = in.readString();
        creator_name = in.readString();
        users = in.createStringArrayList();
        is_delete = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(creator_name);
        dest.writeStringList(users);
        dest.writeByte((byte) (is_delete ? 1 : 0));
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<RoomParcel> CREATOR =
        new Creator<>()
        {
            @Override
            public RoomParcel createFromParcel(Parcel in)
            {
                return new RoomParcel(in);
            }
            @Override
            public RoomParcel[] newArray(int size)
            {
                return new RoomParcel[size];
            }
        };
}

