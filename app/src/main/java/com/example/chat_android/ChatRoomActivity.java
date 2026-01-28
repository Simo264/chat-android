package com.example.chat_android;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

public class ChatRoomActivity extends AppCompatActivity
{
    private String m_current_username;
    private RoomParcel m_room;
    private MessageAdapter m_adapter;
    private RecyclerView m_recycler_view;
    private EditText m_input_text;
    private View m_container_preview_wrapper;
    private ImageView m_img_preview;
    private String m_selected_image_uri;
    private Button m_btn_send;
    private LinearProgressIndicator m_upload_bar;

    private ActivityResultLauncher<PickVisualMediaRequest> m_photo_picker_launcher;

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
        m_adapter = new MessageAdapter(m_current_username);
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
        m_selected_image_uri = "";
        m_photo_picker_launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->
        {
            if (uri != null)
            {
                m_selected_image_uri = uri.toString();
                m_img_preview.setImageURI(uri);
                m_container_preview_wrapper.setVisibility(View.VISIBLE);
                Log.d("GALLERY_DEBUG", "URI selezionato: " + m_selected_image_uri);
            }
        });

        initTopAppBar();

        MaterialButton btn_remove_image = findViewById(R.id.btn_remove_image);
        btn_remove_image.setOnClickListener(v ->
        {
            m_selected_image_uri = "";
            m_container_preview_wrapper.setVisibility(View.GONE);
        });

        ImageButton btn_camera = findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(v -> {
            // todo
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
                    Toast.makeText(ChatRoomActivity.this, "Errore caricamento messaggi", Toast.LENGTH_SHORT).show();
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
        if (message_text.isEmpty() && m_selected_image_uri.isEmpty())
            return;

        var has_image = !m_selected_image_uri.isEmpty();
        if (has_image)
        {
            // Mostra progress bar o disabilita il pulsante
            m_btn_send.setEnabled(false);
            m_upload_bar.setVisibility(View.VISIBLE);
            m_upload_bar.setProgress(0);
        }

        var message_entity = new MessageEntity(
            message_text,
            m_selected_image_uri,  // URI locale o stringa vuota
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
        m_selected_image_uri = "";
        m_container_preview_wrapper.setVisibility(View.GONE);
    }

    private void openCamera()
    {
        // todo
    }

    private void openPhotoPicker()
    {
        m_photo_picker_launcher.launch(new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE) // immagini + video
            .build()
        );
    }
}
