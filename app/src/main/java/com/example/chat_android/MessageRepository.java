package com.example.chat_android;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MessageRepository
{
    public static final String COLLECTION_NAME = "messages";
    private static final String FIRESTORE_TAG = "FIRESTORE_DEBUG";
    private static MessageRepository m_instance;
    private final FirebaseFirestore m_firestore_db;
    private final FirebaseStorage m_storage;
    private ListenerRegistration m_messages_listener = null;

    private MessageRepository()
    {
        m_firestore_db = FirebaseFirestore.getInstance();
        m_storage = FirebaseStorage.getInstance();
    }

    public static MessageRepository getInstance()
    {
        if (m_instance != null)
            return m_instance;

        synchronized (MessageRepository.class)
        {
            if (m_instance == null)
                m_instance = new MessageRepository();

        }
        return m_instance;
    }

    public CompletableFuture<Void> removeChatDocument(String room_name)
    {
        var future = new CompletableFuture<Void>();
        var room_messages = m_firestore_db
            .collection(COLLECTION_NAME)
            .document(room_name)
            .collection("room_messages");

        room_messages.get()
            .addOnSuccessListener(querySnapshot ->
            {
                var batch = m_firestore_db.batch();
                for (var document : querySnapshot.getDocuments())
                    batch.delete(document.getReference());

                var main_chat_doc = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
                batch.delete(main_chat_doc);
                batch.commit()
                    .addOnSuccessListener(aVoid -> future.complete(null))
                    .addOnFailureListener(future::completeExceptionally);

            })
            .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public void sendMessage(String room_name, MessageEntity message)
    {
        // Se non c'è media, salva direttamente
        if (message.media_url.isEmpty())
        {
            saveToFirestore(room_name, message);
            return;
        }


        var local_uri = Uri.parse(message.media_url);
        var extension = getFileExtension(local_uri);
        var file_name = "chat_images/" + UUID.randomUUID().toString() + extension;
        var storage_ref = m_storage.getReference().child(file_name);
        storage_ref.putFile(local_uri)
            .continueWithTask(task ->
            {
                if (!task.isSuccessful())
                    throw task.getException();

                return storage_ref.getDownloadUrl();
            })
            .addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    var public_url = task.getResult().toString();
                    var message_to_save = new MessageEntity(message.text, public_url, message.from);
                    saveToFirestore(room_name, message_to_save);
                }
                else
                {
                    Log.e("STORAGE_ERROR", "Errore upload", task.getException());
                }
            });
    }


    // Ascolta i messaggi di una stanza specifica in tempo reale
    public ListenerRegistration observeRoomMessages(String room_name, MessagesListener listener)
    {
        removeMessagesListener();

        m_messages_listener = m_firestore_db.collection(COLLECTION_NAME)
            .document(room_name)
            .collection("room_messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null)
                {
                    Log.e(FIRESTORE_TAG, "Errore ascolto messaggi", error);
                    listener.onError(error);
                    return;
                }

                if (snapshots != null)
                {
                    var messages = new ArrayList<MessageEntity>();
                    for (var document : snapshots.getDocuments())
                    {
                        var message_entity = document.toObject(MessageEntity.class);
                        if (message_entity != null)
                        {
                            messages.add(message_entity);
                        }
                    }
                    listener.onMessagesUpdated(messages);
                }
            });

        return m_messages_listener;
    }

    public void removeMessagesListener()
    {
        if (m_messages_listener != null)
        {
            m_messages_listener.remove();
            m_messages_listener = null;
        }
    }



    private String getFileExtension(Uri uri)
    {
        var extension = ".jpg";
        try
        {
            var content_resolver = m_firestore_db.getApp().getApplicationContext().getContentResolver();
            var mime_type = content_resolver.getType(uri);
            if (mime_type != null)
            {
                if (mime_type.equals("image/png"))
                    extension = ".png";
                else if (mime_type.equals("image/jpeg") || mime_type.equals("image/jpg"))
                    extension = ".jpg";
                else if (mime_type.equals("image/gif"))
                    extension = ".gif";
                else if (mime_type.equals("image/webp"))
                    extension = ".webp";
                else if (mime_type.startsWith("video/"))
                    extension = ".mp4";
            }
        }
        catch (Exception e)
        {
            Log.e("FILE_EXTENSION", "Errore ottenimento estensione", e);
        }
        return extension;
    }

    private void saveToFirestore(String room_name, MessageEntity message)
    {
        m_firestore_db.collection(COLLECTION_NAME)
            .document(room_name)
            .collection("room_messages")
            .add(message)
            .addOnSuccessListener(documentReference -> Log.d(FIRESTORE_TAG, "Messaggio salvato: " + documentReference.getId()))
            .addOnFailureListener(e -> Log.e(FIRESTORE_TAG, "Errore salvataggio messaggio", e));
    }
}
