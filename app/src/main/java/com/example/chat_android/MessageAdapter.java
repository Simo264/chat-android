package com.example.chat_android;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
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
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private final ArrayList<MessageEntity> m_message_list;
    private final String m_current_username;
    private final OnMessageLongClickListener m_long_click_listener;

    public MessageAdapter(String current_username, OnMessageLongClickListener listener)
    {
        this.m_message_list = new ArrayList<MessageEntity>();
        this.m_current_username = current_username;
        this.m_long_click_listener = listener;
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
        holder.bind(message_entity, m_current_username, m_long_click_listener);
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
        private final FrameLayout m_media_container;
        private final ImageView m_message_image;
        private final ImageView m_play_button;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            m_tv_sender_name = itemView.findViewById(R.id.tv_sender_name);
            m_tv_message_text = itemView.findViewById(R.id.tv_message_text);
            m_tv_timestamp = itemView.findViewById(R.id.tv_timestamp);
            m_message_container = itemView.findViewById(R.id.message_container);
            m_media_container = itemView.findViewById(R.id.media_container);
            m_message_image = itemView.findViewById(R.id.iv_message_image);
            m_play_button = itemView.findViewById(R.id.iv_play_button);
        }

        public void bind(@NonNull MessageEntity message, String current_username, OnMessageLongClickListener click_listener)
        {
            var is_own_message = message.from.equals(current_username);
            if (is_own_message)
            {
                m_message_container.setOnLongClickListener(v ->
                {
                    if (click_listener != null)
                    {
                        click_listener.onMessageLongClick(message);
                        return true;
                    }
                    return false;
                });
            }
            else
            {
                m_message_container.setOnLongClickListener(null);
            }

            var attr_background = is_own_message
                ? com.google.android.material.R.attr.colorPrimaryContainer
                : com.google.android.material.R.attr.colorSecondaryContainer;
            var attr_on_background = is_own_message
                ? com.google.android.material.R.attr.colorOnPrimaryContainer
                : com.google.android.material.R.attr.colorOnSecondaryContainer;
            var background_color = MaterialColors.getColor(m_message_container, attr_background);
            var text_color = MaterialColors.getColor(m_message_container, attr_on_background);
            var shape_drawable = new MaterialShapeDrawable(ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, 16f * m_message_container.getResources().getDisplayMetrics().density)
                .build()
            );
            shape_drawable.setFillColor(ColorStateList.valueOf(background_color));
            m_message_container.setBackground(shape_drawable);
            m_tv_sender_name.setText(is_own_message ? m_tv_sender_name.getContext().getString(R.string.you) : message.from);
            m_tv_sender_name.setTextColor(text_color);
            m_tv_message_text.setTextColor(text_color);

            var gravity = is_own_message ? Gravity.END : Gravity.START;
            var params = (FrameLayout.LayoutParams) m_message_container.getLayoutParams();
            params.gravity = gravity;
            m_tv_sender_name.setGravity(gravity);
            m_tv_message_text.setGravity(gravity);
            m_tv_timestamp.setGravity(gravity);
            m_message_container.setLayoutParams(params);

            if (!message.media_url.isEmpty())
            {
                m_media_container.setVisibility(View.VISIBLE);
                if (message.media_type.equals(MessageEntity.MEDIA_VIDEO))
                {
                    m_play_button.setVisibility(View.VISIBLE);

                    // Carica la thumbnail del video
                    Glide.with(m_message_image.getContext())
                        .load(message.media_url)
                        .centerCrop()
                        .into(m_message_image);

                    // Click listener per aprire il video
                    m_media_container.setOnClickListener(v ->
                    {
                        var intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(message.media_url), "video/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (intent.resolveActivity(v.getContext().getPackageManager()) != null)
                            v.getContext().startActivity(intent);
                    });
                }
                else
                {
                    m_play_button.setVisibility(View.GONE);
                    Glide.with(m_message_image.getContext())
                        .load(message.media_url)
                        .centerCrop()
                        .into(m_message_image);

                    // Click listener per aprire l'immagine a schermo intero
                    m_media_container.setOnClickListener(v ->
                    {
                        var intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(message.media_url), "image/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (intent.resolveActivity(v.getContext().getPackageManager()) != null)
                            v.getContext().startActivity(intent);
                    });
                }
            }
            else
            {
                m_media_container.setVisibility(View.GONE);
                m_media_container.setOnClickListener(null);
            }

            // visualizzo il testo
            if (!message.text.isEmpty())
            {
                m_tv_message_text.setVisibility(View.VISIBLE);
                m_tv_message_text.setText(message.text);
            }
            else
            {
                m_tv_message_text.setVisibility(View.GONE);
            }

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
                Log.e(RoomRepository.FIRESTORE_TAG, Objects.requireNonNull(e.getMessage()));
            }
            return "";
        }
    }
}
