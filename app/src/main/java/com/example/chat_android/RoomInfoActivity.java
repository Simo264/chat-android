package com.example.chat_android;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class RoomInfoActivity extends AppCompatActivity
{
    private String m_room_name;
    private TextView m_text_partecipants;
    private ChipGroup m_chip_group;
    private MaterialButton m_btn_action;
    private boolean m_user_is_joined;
    private String m_current_username;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);

        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(R.string.room_info);
        top_app_bar.setNavigationIcon(R.drawable.chevron_left_24);
        top_app_bar.setNavigationOnClickListener(v -> finish());

        var room = getIntent().getParcelableExtra("ROOM_OBJECT", Room.class);
        if(room == null)
            throw new RuntimeException("ROOM_OBJECT non valido.");

        TextView text_room_name = findViewById(R.id.text_room_name);
        text_room_name.setText(room.name);
        TextView text_creator_name = findViewById(R.id.text_creator_name);
        text_creator_name.setText(getString(R.string.creator_name, room.creator_name));
        TextView text_creator_uid = findViewById(R.id.text_creator_uid);
        text_creator_uid.setText(getString(R.string.creator_uid, room.creator_uid));

        m_room_name = room.name;
        m_user_is_joined = false;
        m_current_username = AuthRepository.getInstance().getUsername();
        m_text_partecipants = findViewById(R.id.text_partecipants);
        m_chip_group = findViewById(R.id.chip_group_users);
        m_btn_action = findViewById(R.id.btn_action);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        var room_repo = RoomRepository.getInstance();
        room_repo.observeRoom(m_room_name, new SingleRoomListener()
        {
            @Override
            public void onRoomUpdated(Room room)
            {
                runOnUiThread(() ->
                {
                    m_text_partecipants.setText(getString(R.string.partecipants, room.getUserCount()));
                    m_chip_group.removeAllViews();
                    m_user_is_joined = false;
                    for (var username : room.users)
                    {
                        var chip = new Chip(RoomInfoActivity.this);
                        chip.setText(username);
                        chip.setClickable(false);
                        chip.setCheckable(false);
                        m_chip_group.addView(chip);
                        if(m_current_username.equals(username))
                            m_user_is_joined = true;
                    }

                    m_btn_action.setText(getString(R.string.join_and_start_discussion));
                    if(m_user_is_joined)
                        m_btn_action.setText(getString(R.string.leave_room));

                    m_btn_action.setOnClickListener(view ->
                    {
                        m_btn_action.setEnabled(false);
                        CompletableFuture<Void> action;
                        if (!m_user_is_joined)
                            action = room_repo.joinRoomAsync(m_room_name, m_current_username);
                        else
                            action = room_repo.leaveRoomAsync(m_room_name, m_current_username);

                        action.thenRun(() ->
                        {
                            m_btn_action.setEnabled(true);
                        })
                        .exceptionally(ex ->
                        {
                            m_btn_action.setEnabled(true);
                            Toast.makeText(RoomInfoActivity.this, "Errore: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                            return null;
                        });
                    });
                });
            }

            @Override
            public void onError(Exception e)
            {
                Log.e(RoomRepository.FIRESTORE_TAG, "Errore ascolto stanza: " + e);
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        RoomRepository.getInstance().removeSingleRoomListener();
    }
}
