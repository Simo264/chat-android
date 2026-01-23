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

        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(R.string.user_profile);
        top_app_bar.setNavigationIcon(R.drawable.chevron_left_24);
        top_app_bar.setNavigationOnClickListener(v -> finish());

        var auth_repo = AuthRepository.getInstance();

        TextView tv_username = findViewById(R.id.info_username);
        TextView tv_email = findViewById(R.id.info_email);
        TextView tv_uid = findViewById(R.id.info_uid);
        TextView tv_timestamp = findViewById(R.id.info_timestamp);
        tv_username.setText(getString(R.string.info_username_format, auth_repo.getUsername()));
        tv_email.setText(getString(R.string.info_email_format, auth_repo.getUserEmail()));
        tv_uid.setText(getString(R.string.info_uid_format, auth_repo.getUserUid()));
        tv_timestamp.setText(getString(R.string.info_timestamp_format, auth_repo.getUserCreationTime()));

        Button btn_logout = findViewById(R.id.btn_signout);
        btn_logout.setOnClickListener(v ->
        {
            auth_repo.signOut();
            var intent = new Intent(UserInfoActivity.this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        Button btn_delete = findViewById(R.id.btn_delete_account);
        btn_delete.setOnClickListener(v -> { showDeleteConfirmation(); });
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
