package com.example.chat_android;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder>
{
    private final ArrayList<RoomParcel> m_room_list;
    private final String m_current_username;

    public RoomAdapter(@NonNull ArrayList<RoomParcel> room_list, @NonNull String current_username)
    {
        m_room_list = room_list;

        var auth_repo = AuthRepository.getInstance();
        m_current_username = current_username;
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
        var context = holder.itemView.getContext();

        // setup colonna 1
        holder.text_room_name.setText(room.name);
        holder.text_user_count.setText(context.getString(R.string.partecipants, room.users.size()));

        // setup colonna 2: icona Bookmark Check
        if (room.users.contains(m_current_username))
            holder.icon_chat.setVisibility(View.VISIBLE);
        else
            holder.icon_chat.setVisibility(View.GONE);

        // setup colonna 3:icona Supervisor Account
        if (room.creator_name.equals(m_current_username))
            holder.icon_is_owner.setVisibility(View.VISIBLE);
        else
            holder.icon_is_owner.setVisibility(View.GONE);

        // click sull'icona chat
        holder.icon_chat.setOnClickListener(v ->
        {
            var intent = new Intent(context, ChatRoomActivity.class);
            intent.putExtra("ROOM_OBJECT", room);
            context.startActivity(intent);
        });

        // click sulla card
        holder.itemView.setOnClickListener(v ->
        {
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

    public void updateRooms(ArrayList<RoomParcel> new_rooms)
    {
        m_room_list.clear();
        m_room_list.addAll(new_rooms);
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder
    {
        public TextView text_room_name;
        public TextView text_user_count;
        public ImageView icon_chat;
        public ImageView icon_is_owner;
        public RoomViewHolder(@NonNull View itemView)
        {
            super(itemView);
            text_room_name = itemView.findViewById(R.id.text_room_name);
            text_user_count = itemView.findViewById(R.id.text_user_count_item);
            icon_chat = itemView.findViewById(R.id.icon_chat);
            icon_is_owner = itemView.findViewById(R.id.icon_is_owner);
        }
    }
}