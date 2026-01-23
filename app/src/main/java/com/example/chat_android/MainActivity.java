package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{
    private TextView m_text_rooms;
    private RecyclerView m_recycler_rooms;
    private RoomAdapter m_room_adapter;
    private RoomFilterMode m_current_filter;
    private String m_current_username;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        var auth_repo = AuthRepository.getInstance();
        if (!auth_repo.isUserLoggedIn())
            throw new RuntimeException(getString(R.string.error_user_not_authenticated));

        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(R.string.rooms);
        top_app_bar.inflateMenu(R.menu.main_menu);
        top_app_bar.setOnMenuItemClickListener(item ->
        {
            if(item.getItemId() == R.id.action_profile)
            {
                var intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        m_current_username = AuthRepository.getInstance().getUsername();
        m_current_filter = RoomFilterMode.ALL;
        m_recycler_rooms = findViewById(R.id.recycler_rooms);
        m_recycler_rooms.setNestedScrollingEnabled(true);
        m_text_rooms = findViewById(R.id.text_rooms);

        FloatingActionButton fab = findViewById(R.id.fab_explore);
        fab.setOnClickListener(view -> {
            var popup = new PopupMenu(MainActivity.this, view);
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item ->
            {
                int id = item.getItemId();
                if (id == R.id.fab_create_new_room)
                {
                    var intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                    startActivity(intent);
                    return true;
                }

                if (id == R.id.fab_view_all_rooms)
                    m_current_filter = RoomFilterMode.ALL;
                else if (id == R.id.fab_view_created_rooms)
                    m_current_filter = RoomFilterMode.OWNED;
                else if (id == R.id.fab_view_joined_rooms)
                    m_current_filter = RoomFilterMode.JOINED;

                restartRoomObservation();
                return false;
            });
            popup.show();
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        startRoomObservation();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // smetti di ascoltare quando l'activity non è visibile
        RoomRepository.getInstance().removeAllRoomsListener();
    }


    private void startRoomObservation()
    {
        RoomRepository.getInstance().observeAllRooms(new RoomsListener()
        {
            @Override
            public void onRoomsUpdated(ArrayList<Room> all_rooms)
            {
                var filtered_rooms = new ArrayList<Room>();
                switch (m_current_filter)
                {
                    case ALL:
                        filtered_rooms = all_rooms;
                        m_text_rooms.setText(getString(R.string.text_all_rooms, filtered_rooms.size()));
                        break;
                    case JOINED:
                        for (Room r : all_rooms)
                            if (r.users.contains(m_current_username))
                                filtered_rooms.add(r);
                        m_text_rooms.setText(getString(R.string.text_joined_rooms, filtered_rooms.size()));
                        break;
                    case OWNED:
                        for (var r : all_rooms)
                            if (r.creator_name.equals(m_current_username))
                                filtered_rooms.add(r);
                        m_text_rooms.setText(getString(R.string.text_created_rooms, filtered_rooms.size()));
                        break;
                }

                final ArrayList<Room> final_rooms = filtered_rooms;
                runOnUiThread(() ->
                {
                    if (m_room_adapter == null)
                    {
                        m_room_adapter = new RoomAdapter(final_rooms);
                        m_recycler_rooms.setAdapter(m_room_adapter);
                    }
                    else
                    {
                        m_room_adapter.updateRooms(final_rooms);
                    }
                });
            }

            @Override
            public void onError(Exception e)
            {
                Log.e(RoomRepository.FIRESTORE_TAG, "Errore listener: " + e);
            }
        });
    }

    private void restartRoomObservation()
    {
        RoomRepository.getInstance().removeAllRoomsListener();
        startRoomObservation();
    }
}