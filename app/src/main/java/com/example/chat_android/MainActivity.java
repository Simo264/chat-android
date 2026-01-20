package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity
{
    private MaterialToolbar m_top_app_bar;
    private AuthRepository m_auth_repo;
    private RoomRepository m_room_repo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_auth_repo = new AuthRepository();
        var current_user = m_auth_repo.getCurrentUser();
        if (current_user == null)
            throw new RuntimeException("L'utente deve essere autenticato!");

        m_top_app_bar = findViewById(R.id.topAppBar);
        m_top_app_bar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_profile)
            {
                Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                startActivity(intent);
                return true;
            }
            else if(id == R.id.action_search)
            {
                return true;
            }
            return false;
        });
    }
}