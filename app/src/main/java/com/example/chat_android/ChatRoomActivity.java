package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity
{
    private String m_current_username;
    private RoomParcel m_room;
    private MessageAdapter m_adapter;
    private RecyclerView m_recycler_view;
    private EditText m_input_text;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        m_room = getIntent().getParcelableExtra("ROOM_OBJECT", RoomParcel.class);
        if(m_room == null)
        {
            Toast.makeText(this,  getString(R.string.error_retrieve_room), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        m_current_username = AuthRepository.getInstance().getUsername();

        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(getString(R.string.chat_room_title, m_room.name));
        top_app_bar.inflateMenu(R.menu.chat_menu);
        top_app_bar.setNavigationIcon(R.drawable.chevron_left_24);
        top_app_bar.setNavigationOnClickListener(v -> finish());
        top_app_bar.setOnMenuItemClickListener(item ->
        {
            if(item.getItemId() == R.id.action_info)
            {
                var intent = new Intent(ChatRoomActivity.this, RoomInfoActivity.class);
                intent.putExtra("ROOM_OBJECT", m_room);
                startActivity(intent);
                return true;
            }
            return false;
        });

        m_adapter = new MessageAdapter(m_current_username);
        m_recycler_view = findViewById(R.id.rv_messages);
        m_recycler_view.setAdapter(m_adapter);

        m_input_text = findViewById(R.id.input_text);

        Button btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Inizia ad ascoltare i messaggi quando l'activity diventa visibile
        var message_repo = MessageRepository.getInstance();
        message_repo.observeRoomMessages(m_room.name, new MessagesListener()
        {
            @Override
            public void onMessagesUpdated(ArrayList<MessageEntity> messages)
            {
                runOnUiThread(() ->
                {
                    m_adapter.updateMessages(messages);
                    if (messages.size() > 0)
                    {
                        m_recycler_view.smoothScrollToPosition(messages.size() - 1);
                    }
                });
            }

            @Override
            public void onError(Exception e)
            {
                runOnUiThread(() ->
                {
                    Toast.makeText(ChatRoomActivity.this, "Errore caricamento messaggi", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // Rimuovi il listener quando l'activity non è più visibile
        MessageRepository.getInstance().removeMessagesListener();
    }

    private void sendMessage()
    {
        var message_text = m_input_text.getText().toString().trim();
        if (message_text.isEmpty())
            return;

        var message_entity = new MessageEntity(message_text, m_current_username);
        MessageRepository.getInstance().sendMessage(m_room.name, m_current_username, message_entity);
        m_input_text.setText("");
    }
}
