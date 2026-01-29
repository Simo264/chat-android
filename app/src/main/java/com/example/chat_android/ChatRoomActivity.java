package com.example.chat_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatRoomActivity extends AppCompatActivity
{
    private String m_current_username;
    private RoomParcel m_room;
    private MessageAdapter m_adapter;
    private RecyclerView m_recycler_view;
    private EditText m_input_text;
    private View m_container_preview_wrapper;
    private ImageView m_img_preview;
    private String m_selected_media_uri = "";
    private String m_selected_media_type = "";
    private Button m_btn_send;
    private LinearProgressIndicator m_upload_bar;
    private ActivityResultLauncher<PickVisualMediaRequest> m_photo_picker_launcher;
    private ActivityResultLauncher<Intent> m_camera_launcher;
    private ActivityResultLauncher<String> m_camera_permission_launcher;
    private Uri m_camera_uri = null;
    private boolean m_is_camera_video_media = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        m_room = getIntent().getParcelableExtra("ROOM_OBJECT", RoomParcel.class);
        if(m_room == null)
        {
            Toast.makeText(this,  getString(R.string.error_retrieve_room), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        m_current_username = AuthRepository.getInstance().getUsername();
        m_adapter = new MessageAdapter(m_current_username, message ->
        {
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_message_title)
                .setMessage(R.string.delete_message_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                {
                    var repo = MessageRepository.getInstance();
                    repo.removeChatMessageFromRoom(m_room.name, message)
                        .thenRun(() ->
                        {
                            Snackbar.make(m_recycler_view, "Messaggio eliminato", Snackbar.LENGTH_SHORT).show();
                        })
                        .exceptionally(e ->
                        {
                            Log.e("CHAT_ERROR", "Errore eliminazione", e);
                            return null;
                        });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });
        m_recycler_view = findViewById(R.id.rv_messages);
        m_recycler_view.setAdapter(m_adapter);
        m_input_text = findViewById(R.id.input_text);
        m_container_preview_wrapper = findViewById(R.id.container_preview_wrapper);
        m_container_preview_wrapper.setVisibility(View.GONE);
        m_img_preview = findViewById(R.id.img_preview);
        m_upload_bar = findViewById(R.id.upload_progress_bar);
        m_upload_bar.setVisibility(View.GONE);
        m_btn_send = findViewById(R.id.btn_send);
        m_btn_send.setOnClickListener(v -> send());
        m_photo_picker_launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->
        {
            if (uri != null)
            {
                m_selected_media_uri = uri.toString();
                var mime_type = getContentResolver().getType(uri);
                if (mime_type != null && mime_type.startsWith("video/"))
                {
                    m_selected_media_type = MessageEntity.MEDIA_VIDEO;
                    showVideoThumbnail(uri);
                }
                else
                {
                    m_selected_media_type = MessageEntity.MEDIA_IMAGE;
                    m_img_preview.setImageURI(uri);
                }
                m_container_preview_wrapper.setVisibility(View.VISIBLE);
            }
        });
        m_camera_permission_launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), is_granted ->
        {
            if (is_granted)
            {
                openCameraDialog();
            }
            else
            {
                Toast.makeText(this, R.string.error_camera_permission, Toast.LENGTH_SHORT).show();
            }
        });
        m_camera_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
        {
            if (result.getResultCode() != RESULT_OK)
                return;

            if (m_is_camera_video_media)
            {
                if (result.getData() != null)
                    m_camera_uri = result.getData().getData();

                if (m_camera_uri == null)
                    return;

                m_selected_media_type = MessageEntity.MEDIA_VIDEO;
                m_selected_media_uri = m_camera_uri.toString();
                showVideoThumbnail(m_camera_uri);
            }
            else
            {
                if (m_camera_uri == null)
                    return;

                m_selected_media_type = MessageEntity.MEDIA_IMAGE;
                m_selected_media_uri = m_camera_uri.toString();
                m_img_preview.setImageURI(m_camera_uri);
            }
            m_container_preview_wrapper.setVisibility(View.VISIBLE);
        });

        initTopAppBar();

        MaterialButton btn_remove_image = findViewById(R.id.btn_remove_image);
        btn_remove_image.setOnClickListener(v ->
        {
            m_selected_media_uri = "";
            m_container_preview_wrapper.setVisibility(View.GONE);
        });

        ImageButton btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(v ->
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            {
                openCameraDialog();
            }
            else
            {
                m_camera_permission_launcher.launch(Manifest.permission.CAMERA);
            }
        });
        ImageButton btn_gallery = findViewById(R.id.btn_gallery);
        btn_gallery.setOnClickListener(v -> openPhotoPicker());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        var message_repo = MessageRepository.getInstance();
        message_repo.observeRoomMessages(m_room.name, new MessagesListener()
        {
            @Override
            public void onMessagesUpdated(ArrayList<MessageEntity> messages)
            {
                runOnUiThread(() ->
                {
                    m_adapter.updateMessages(messages);
                    if (!messages.isEmpty())
                        m_recycler_view.smoothScrollToPosition(messages.size() - 1);
                });
            }

            @Override
            public void onError(Exception e)
            {
                runOnUiThread(() ->
                {
                    Log.e("MESSAGE_REPO", "Errore caricamento messaggi: " + e.toString());
                });
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // Rimuovi il listener quando l'activity non è più visibile
        MessageRepository.getInstance().removeMessagesListener();
    }

    private void showVideoThumbnail(Uri video_uri)
    {
        Glide.with(m_img_preview)
            .asBitmap()
            .load(video_uri)
            .frame(1_000_000) // 1 secondo
            .centerCrop()
            .error(new ColorDrawable(MaterialColors.getColor(m_img_preview, com.google.android.material.R.attr.colorSurfaceVariant)))
            .into(m_img_preview);
    }

    private void initTopAppBar()
    {
        MaterialToolbar top_app_bar = findViewById(R.id.top_app_bar);
        top_app_bar.setTitle(getString(R.string.chat_room_title, m_room.name));
        top_app_bar.inflateMenu(R.menu.chat_menu);
        top_app_bar.setNavigationIcon(R.drawable.chevron_left_24);
        top_app_bar.setNavigationOnClickListener(v -> finish());
        top_app_bar.setOnMenuItemClickListener(item ->
        {
            if(item.getItemId() == R.id.action_info)
            {
                var intent = new Intent(ChatRoomActivity.this, RoomInfoActivity.class);
                intent.putExtra("ROOM_OBJECT", m_room);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void send()
    {
        var message_text = m_input_text.getText().toString().trim();
        if (message_text.isEmpty() && m_selected_media_uri.isEmpty())
            return;

        var has_image = !m_selected_media_uri.isEmpty();
        if (has_image)
        {
            // Mostra progress bar o disabilita il pulsante
            m_btn_send.setEnabled(false);
            m_upload_bar.setVisibility(View.VISIBLE);
            m_upload_bar.setProgress(0);
        }

        var message_entity = new MessageEntity(
            message_text,
            m_selected_media_uri,  // URI locale o stringa vuota
            m_selected_media_type,
            m_current_username
        );
        MessageRepository.getInstance().sendMessage(m_room.name, message_entity, new SendMessageCallback()
        {
            @Override
            public void onProgress(int progress)
            {
                runOnUiThread(() ->
                {
                    if (m_upload_bar.getVisibility() != View.VISIBLE)
                        m_upload_bar.setVisibility(View.VISIBLE);

                    m_upload_bar.setProgress(progress, true);
                });
            }

            @Override
            public void onSuccess()
            {
                runOnUiThread(() ->
                {
                    m_btn_send.setEnabled(true);
                    m_upload_bar.setVisibility(View.GONE);
                    m_upload_bar.setProgress(0);
                });
            }

            @Override
            public void onError(Exception e)
            {
                runOnUiThread(() ->
                {
                    m_btn_send.setEnabled(true);
                    m_upload_bar.setVisibility(View.GONE);
                    Log.d("UPLOAD", "Errore invio: " + e.toString());
                });
            }
        });

        m_input_text.setText("");
        m_selected_media_uri = "";
        m_selected_media_type = "";
        m_container_preview_wrapper.setVisibility(View.GONE);
    }

    private void openCameraDialog()
    {
        final var options = new String[]{
            getString(R.string.photo),
            getString(R.string.video),
        };

        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_camera_title)
            .setItems(options, (dialog, which) ->
            {
                if (which == 0)
                    openCameraImage();
                else
                    openCameraVideo();
            })
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void openCameraImage()
    {
        m_is_camera_video_media = false;

        var intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null)
            return;

        try
        {
            File photo_file = createImageFile();
            m_camera_uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                photo_file);


            intent.putExtra(MediaStore.EXTRA_OUTPUT, m_camera_uri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            m_camera_launcher.launch(intent);
        }
        catch (IOException e)
        {
            Log.e("openCameraImage", e.toString());
        }
    }

    private void openCameraVideo()
    {
        m_is_camera_video_media = true;
        m_camera_uri = null;
        var intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        m_camera_launcher.launch(intent);
    }

    @NonNull
    private File createImageFile() throws IOException
    {
        var timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        var filename = "IMG_" + timestamp;
        var dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(filename, ".jpg", dir);
    }

    private void openPhotoPicker()
    {
        m_photo_picker_launcher.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE) // immagini + video
            .build()
        );
    }
}
