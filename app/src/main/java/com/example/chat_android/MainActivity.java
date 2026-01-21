package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private AuthRepository m_auth_repo;
    private RoomRepository m_room_repo;
    private TextView m_avail_rooms_counter;
    private RecyclerView m_recycler_all_rooms;
    private RoomAdapter m_room_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_auth_repo = new AuthRepository();
        var current_user = m_auth_repo.getCurrentUser();
        if (current_user == null)
            throw new RuntimeException(getString(R.string.error_user_not_authenticated));

        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_profile)
            {
                var intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        m_room_repo = new RoomRepository();
        m_recycler_all_rooms = findViewById(R.id.recycler_all_rooms);
        m_recycler_all_rooms.setNestedScrollingEnabled(true);
        m_avail_rooms_counter = findViewById(R.id.avail_rooms_counter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        m_room_repo.observeAllRooms(new RoomsListener() {
            @Override
            public void onRoomsUpdated(ArrayList<Room> rooms)
            {
                runOnUiThread(() -> {
                    if (m_room_adapter == null)
                    {
                        m_room_adapter = new RoomAdapter(rooms, m_auth_repo.getCurrentUser().getUid());
                        m_recycler_all_rooms.setAdapter(m_room_adapter);
                    }
                    else
                    {
                        m_room_adapter.updateRooms(rooms);
                    }
                    m_avail_rooms_counter.setText("(" + rooms.size() + ")");
                });
            }

            @Override
            public void onError(Exception e)
            {
                Log.e(RoomRepository.FIRESTORE_TAG, "Errore listener:" + e);
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        // smetti di ascoltare quando l'activity non è visibile
        m_room_repo.removeListener();
    }
}