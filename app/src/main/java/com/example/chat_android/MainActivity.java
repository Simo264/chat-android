package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity
{
    private MaterialToolbar m_top_app_bar;
    private AuthRepository m_auth_repo;
    private RoomRepository m_room_repo;
    private TextView m_all_rooms_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_auth_repo = new AuthRepository();
        var current_user = m_auth_repo.getCurrentUser();
        if (current_user == null)
            throw new RuntimeException(getString(R.string.error_user_not_authenticated));

        m_top_app_bar = findViewById(R.id.top_app_bar);
        m_top_app_bar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_profile)
            {
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        m_room_repo = new RoomRepository();
        RecyclerView recycler_all_rooms = findViewById(R.id.recycler_all_rooms);
        m_all_rooms_counter = findViewById(R.id.all_rooms_counter);
        m_room_repo.getAllRoomsAsync().thenAccept(rooms -> {
            runOnUiThread(() -> {

                var adapter = new RoomAdapter(rooms, m_auth_repo.getCurrentUser().getUid());
                recycler_all_rooms.setAdapter(adapter);

                m_all_rooms_counter.setText(getString(R.string.all_rooms_format, adapter.getItemCount()));
            });
        });


        // RecyclerView recycler_joined_rooms = findViewById(R.id.recycler_joined_rooms);
    }
}