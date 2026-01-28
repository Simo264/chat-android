package com.example.chat_android;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private final ArrayList<MessageEntity> m_message_list;
    private final String m_current_username;

    public MessageAdapter(String current_username)
    {
        this.m_message_list = new ArrayList<MessageEntity>();
        this.m_current_username = current_username;
    }

    public void updateMessages(ArrayList<MessageEntity> new_messages)
    {
        m_message_list.clear();
        m_message_list.addAll(new_messages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        var view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position)
    {
        var message_entity = m_message_list.get(position);
        holder.bind(message_entity, m_current_username);
    }

    @Override
    public int getItemCount()
    {
        return m_message_list.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView m_tv_sender_name;
        private final TextView m_tv_message_text;
        private final TextView m_tv_timestamp;
        private final LinearLayout m_message_container;
        private final ImageView m_message_image;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            m_tv_sender_name = itemView.findViewById(R.id.tv_sender_name);
            m_tv_message_text = itemView.findViewById(R.id.tv_message_text);
            m_tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
            m_message_container = itemView.findViewById(R.id.message_container);
            m_message_image = itemView.findViewById(R.id.iv_message_image);
        }

        public void bind(@NonNull MessageEntity message, String current_username)
        {
            var is_own_message = message.from.equals(current_username);

            // Stampa lo username
            if (is_own_message)
                m_tv_sender_name.setText(m_tv_sender_name.getContext().getString(R.string.you));
            else
                m_tv_sender_name.setText(message.from);

            // messaggio a destra se il messaggio l'ho inviato io, a sinistra altrimenti
            var params = (FrameLayout.LayoutParams) m_message_container.getLayoutParams();
            var gravity = Gravity.START;
            if(is_own_message)
                gravity = Gravity.END;

            params.gravity = gravity;
            m_tv_sender_name.setGravity(gravity);
            m_tv_message_text.setGravity(gravity);
            m_tv_timestamp.setGravity(gravity);
            m_message_container.setLayoutParams(params);

            // mostra l'immagine (se presente)
            if (!message.media_url.isEmpty())
            {
                m_message_image.setVisibility(View.VISIBLE);
                Glide.with(m_message_image.getContext())
                    .load(message.media_url)
                    .centerCrop()
                    .into(m_message_image);
            }
            else
            {
                m_message_image.setVisibility(View.GONE);
            }

            // mostra il testo del messaggio (se presente)
            if (!message.text.isEmpty())
            {
                m_tv_message_text.setVisibility(View.VISIBLE);
                m_tv_message_text.setText(message.text);
            }
            else
            {
                m_tv_message_text.setVisibility(View.GONE);
            }

            // mostra il timestamp
            m_tv_timestamp.setText(formatTimestamp(message.timestamp));
        }

        @NonNull
        private String formatTimestamp(Object timestamp)
        {
            try
            {
                if (timestamp instanceof com.google.firebase.Timestamp)
                {
                    var firebaseTimestamp = (com.google.firebase.Timestamp) timestamp;
                    long milliseconds = firebaseTimestamp.getSeconds() * 1000;

                    var sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return sdf.format(new Date(milliseconds));
                }
            }
            catch (Exception e)
            {
                Log.e(RoomRepository.FIRESTORE_TAG, e.getMessage());
            }
            return "";
        }
    }
}
