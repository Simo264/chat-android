package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class ChatRoomActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        var room = getIntent().getParcelableExtra("ROOM_OBJECT", RoomParcel.class);
        if(room == null)
        {
            Toast.makeText(this,  getString(R.string.error_retrieve_room), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(getString(R.string.chat_room_title, room.name));
        top_app_bar.inflateMenu(R.menu.chat_menu);
        top_app_bar.setNavigationIcon(R.drawable.chevron_left_24);
        top_app_bar.setNavigationOnClickListener(v -> finish());
        top_app_bar.setOnMenuItemClickListener(item ->
        {
            if(item.getItemId() == R.id.action_info)
            {
                var intent = new Intent(ChatRoomActivity.this, RoomInfoActivity.class);
                intent.putExtra("ROOM_OBJECT", room);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}
