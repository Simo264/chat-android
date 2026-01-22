package com.example.chat_android;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>
{
    private final ArrayList<Room> m_room_list;

    public RoomAdapter(@NonNull ArrayList<Room> room_list)
    {
        m_room_list = room_list;
    }

    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position)
    {
        var room = m_room_list.get(position);
        holder.text_room_name.setText(room.name);

        var context = holder.itemView.getContext();
        holder.text_user_count.setText(context.getString(R.string.partecipants, room.getUserCount()));

        // Click sulla card
        holder.itemView.setOnClickListener(v -> {
            var intent = new Intent(context, RoomInfoActivity.class);
            intent.putExtra("ROOM_OBJECT", room);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount()
    {
        return m_room_list.size();
    }

    public void updateRooms(ArrayList<Room> newRooms)
    {
        m_room_list.clear();
        m_room_list.addAll(newRooms);
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder
    {
        public TextView text_room_name;
        public TextView text_user_count;
        public RoomViewHolder(@NonNull View itemView)
        {
            super(itemView);
            text_room_name = itemView.findViewById(R.id.text_room_name);
            text_user_count = itemView.findViewById(R.id.text_user_count_item);
        }
    }
}