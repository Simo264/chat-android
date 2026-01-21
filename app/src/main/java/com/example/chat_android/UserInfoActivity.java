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
            throw new RuntimeException(getString(R.string.error_user_not_authenticated));

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

        tv_username.setText(getString(R.string.info_username_format, username));
        tv_email.setText(getString(R.string.info_email_format, email));
        tv_uid.setText(getString(R.string.info_uid_format, uid));
        tv_timestamp.setText(getString(R.string.info_timestamp_format, date_string));

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

    private void showDeleteConfirmation()
    {
        new MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_account))
            .setMessage(getString(R.string.confirm_delete_account))
            .setPositiveButton(getString(R.string.delete), (d, w) -> {
                // todo
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }
}
