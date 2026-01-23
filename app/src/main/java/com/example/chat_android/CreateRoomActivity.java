package com.example.chat_android;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class CreateRoomActivity extends AppCompatActivity
{
    private String m_current_username;
    private MaterialButton m_btn_create;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        m_current_username = AuthRepository.getInstance().getUsername();

        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(R.string.create_room_title);
        top_app_bar.setNavigationIcon(R.drawable.chevron_left_24);
        top_app_bar.setNavigationOnClickListener(v -> finish());

        TextInputEditText edit_name = findViewById(R.id.edit_room_name);
        m_btn_create = findViewById(R.id.btn_create_room);
        m_btn_create.setOnClickListener(v ->
        {
            var room_name = edit_name.getText().toString().trim();
            if(!room_name.isEmpty())
                createRoom(room_name);
            else
                showErrorDialog(getString(R.string.error_empty_room_name));
        });
    }

    private void createRoom(String room_name)
    {
        m_btn_create.setEnabled(false);

        RoomRepository.getInstance().createRoomAsync(room_name, m_current_username)
            .thenAccept(room ->
            {
                runOnUiThread(() ->
                {
                    Toast.makeText(this, getString(R.string.success_create_new_room), Toast.LENGTH_SHORT).show();
                    finish(); // Torna alla MainActivity
                });
            })
            .exceptionally(ex ->
            {
                runOnUiThread(() ->
                {
                    m_btn_create.setEnabled(true);
                    showErrorDialog(ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                });
                return null;
            });
    }

    private void showErrorDialog(String message) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.error))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
            .show();
    }
}
