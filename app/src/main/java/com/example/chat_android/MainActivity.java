package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity
{
    private TextView m_text_user_data;
    private MaterialButton m_button_logout;

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

        m_text_user_data = findViewById(R.id.textUserMeta);
        m_button_logout = findViewById(R.id.buttonLogout);
        m_button_logout.setOnClickListener(v -> {
            m_auth_repo.signOut();
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        displayUserData(current_user);

        m_room_repo = new RoomRepository();
        try
        {
            m_room_repo.createRoomAsync("Generale", current_user.getUid());
            m_room_repo.createRoomAsync("Just chatting", current_user.getUid());
            m_room_repo.createRoomAsync("CDL-Info", current_user.getUid());

            m_room_repo.deleteRoomAsync("Generale");
            m_room_repo.deleteRoomAsync("Gaming");

            var room_info = m_room_repo.getRoomInfoAsync("Just chatting");
            System.out.println(room_info.toString());

            room_info = m_room_repo.getRoomInfoAsync("Generale");
            System.out.println(room_info.toString());
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException(e);
        }

    }

    private void displayUserData(@NonNull FirebaseUser user)
    {
        var email = user.getEmail();
        var uid = user.getUid();
        var username = email.split("@")[0];
        var creationTimestamp = user.getMetadata().getCreationTimestamp();

        var data_format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        var dateString = data_format.format(new java.util.Date(creationTimestamp));

        var info = new StringBuilder();
        info.append("Username: ").append(username).append("\n");
        info.append("Email: ").append(email).append("\n");
        info.append("UUID: ").append(uid).append("\n");
        info.append("Creato il: ").append(dateString);
        m_text_user_data.setText(info.toString());
    }
}
