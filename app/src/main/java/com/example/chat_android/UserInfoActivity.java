package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UserInfoActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        var auth_repo = new AuthRepository();
        var current_user = auth_repo.getCurrentUser();
        if (current_user == null)
            throw new RuntimeException("L'utente deve essere autenticato!");

        MaterialToolbar toolbar = findViewById(R.id.topAppBarUserInfo);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        var email = current_user.getEmail();
        var uid = current_user.getUid();
        var username = email.split("@")[0];
        var creation_timestamp = current_user.getMetadata().getCreationTimestamp();
        var data_format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        var date_string = data_format.format(new java.util.Date(creation_timestamp));

        TextView tv_username = findViewById(R.id.info_username);
        TextView tv_email = findViewById(R.id.info_email);
        TextView tv_uid = findViewById(R.id.info_uid);
        TextView tv_timestamp = findViewById(R.id.info_timestamp);

        tv_username.setText("Username: " + username);
        tv_email.setText("Email: " + email);
        tv_uid.setText("UID: " + uid);
        tv_timestamp.setText("Account creato il: " + date_string);

        Button btn_logout = findViewById(R.id.btn_signout);
        btn_logout.setOnClickListener(v -> {
            auth_repo.signOut();
            var intent = new Intent(UserInfoActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        Button btn_delete = findViewById(R.id.btn_delete_account);
        btn_delete.setOnClickListener(v -> {
            showDeleteConfirmation();
        });
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Elimina Account")
            .setMessage("Sei sicuro? Questa azione è irreversibile e cancellerà tutti i tuoi dati.")
            .setPositiveButton("Elimina", (d, w) -> {
                // todo
            })
            .setNegativeButton("Annulla", null)
            .show();
    }
}
