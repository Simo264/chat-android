package com.example.chat_android;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class MessageRepository
{
    public static final String COLLECTION_NAME = "messages";
    private static final String FIRESTORE_TAG = "FIRESTORE_DEBUG";
    private static MessageRepository m_instance;
    private FirebaseFirestore m_firestore_db;
    private ListenerRegistration m_messages_listener = null;

    private MessageRepository()
    {
        m_firestore_db = FirebaseFirestore.getInstance();
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

    public void sendMessage(String room_name, String sender, MessageEntity message)
    {
        m_firestore_db.collection(COLLECTION_NAME)
            .document(room_name)
            .collection("room_messages")  // Subcollection per i messaggi
            .add(message)
            .addOnSuccessListener(documentReference ->
            {
                Log.d(FIRESTORE_TAG, "Messaggio inviato nella stanza " + room_name + ": " + documentReference.getId());
            })
            .addOnFailureListener(e ->
            {
                Log.e(FIRESTORE_TAG, "Errore invio messaggio", e);
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

                    Log.d(FIRESTORE_TAG, "Ricevuti " + messages.size() + " messaggi dalla stanza " + room_name);
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
}
