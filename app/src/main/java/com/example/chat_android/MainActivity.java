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
    private TextView m_avail_rooms_counter;
    private RecyclerView m_recycler_all_rooms;
    private RoomAdapter m_room_adapter;

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
        top_app_bar.inflateMenu(R.menu.main_menu); // Carica il menu specifico
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

        m_recycler_all_rooms = findViewById(R.id.recycler_all_rooms);
        m_recycler_all_rooms.setNestedScrollingEnabled(true);
        m_avail_rooms_counter = findViewById(R.id.avail_rooms_counter);

        FloatingActionButton fab = findViewById(R.id.fab_explore);
        fab.setOnClickListener(view -> {
            var popup = new PopupMenu(MainActivity.this, view);
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item ->
            {
                return false;
            });
            popup.show();
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        RoomRepository.getInstance().observeAllRooms(new RoomsListener()
        {
            @Override
            public void onRoomsUpdated(ArrayList<Room> rooms)
            {
                runOnUiThread(() ->
                {
                    if (m_room_adapter == null)
                    {
                        m_room_adapter = new RoomAdapter(rooms);
                        m_recycler_all_rooms.setAdapter(m_room_adapter);
                    }
                    else
                    {
                        m_room_adapter.updateRooms(rooms);
                    }
                    m_avail_rooms_counter.setText(getString(R.string.rooms_counter, rooms.size()));
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
        RoomRepository.getInstance().removeAllRoomsListener();
    }
}