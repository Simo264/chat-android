package com.example.chat_android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>
{
    private ArrayList<Room> m_room_list;
    private String m_current_user_uid;

    public RoomAdapter(ArrayList<Room> room_list, String user_uid)
    {
        m_room_list = room_list;
        m_current_user_uid = user_uid;
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
        holder.text_index.setText(String.valueOf(position + 1));
        holder.text_room_name.setText(room.name);

        var context = holder.itemView.getContext();
        holder.text_user_count.setText(context.getString(R.string.user_count, room.getUserCount()));

        if (room.creator_uid.equals(m_current_user_uid))
            holder.chip_owner.setVisibility(View.VISIBLE);
        else
            holder.chip_owner.setVisibility(View.GONE);

        // Click sulla card
        holder.itemView.setOnClickListener(v -> {
            // listener.onRoomClick(room);
        });
    }

    @Override
    public int getItemCount()
    {
        return m_room_list.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder
    {
        public TextView text_index;
        public TextView text_room_name;
        public TextView text_user_count;
        public View chip_owner;
        public RoomViewHolder(@NonNull View itemView)
        {
            super(itemView);

            text_index = itemView.findViewById(R.id.text_index);
            text_room_name = itemView.findViewById(R.id.text_room_name);
            text_user_count = itemView.findViewById(R.id.text_user_count_item);
            chip_owner = itemView.findViewById(R.id.chip_owner);
        }
    }
}