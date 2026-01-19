package com.example.chat_android;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RoomRepository
{
    private static final String TAG = "FIRESTORE_DEBUG";
    private static final String COLLECTION_NAME = "rooms";
    private FirebaseFirestore m_firestore_db;

    public RoomRepository()
    {
        m_firestore_db = FirebaseFirestore.getInstance();
    }

    public CompletableFuture<Room> createRoomAsync(String room_name, String creator_uid)
    {
        var future = new CompletableFuture<Room>();
        var roomRef = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        m_firestore_db.runTransaction(transaction -> {
            if (transaction.get(roomRef).exists())
                throw new RuntimeException("La stanza '" + room_name + "' esiste già!");

            var room = new Room(room_name, creator_uid, 1);
            transaction.set(roomRef, room);
            return room;
        }).addOnSuccessListener(result -> {
            future.complete(result);
        }).addOnFailureListener(e -> {
            future.completeExceptionally(e);
        });

        return future;
    }

    public CompletableFuture<Void> deleteRoomAsync(String room_name, String uid)
    {
        var future = new CompletableFuture<Void>();
        var roomRef = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        m_firestore_db.runTransaction(transaction -> {
            var snapshot = transaction.get(roomRef);

            if (!snapshot.exists())
                throw new RuntimeException("Impossibile eliminare: la stanza '" + room_name + "' non esiste.");

            var room = snapshot.toObject(Room.class);
            // Solo il creatore può eliminare la stanza
            if (room != null && !room.creator_uid.equals(uid))
                throw new RuntimeException("Non hai il permesso di eliminare la stanza.");

            if (room != null && room.user_count >= 1)
                throw new RuntimeException("Impossibile eliminare: ci sono ancora utenti all'interno.");

            transaction.delete(roomRef);
            return null;
        }).addOnSuccessListener(result -> {
            future.complete(null);
        }).addOnFailureListener(e -> {
            future.completeExceptionally(e);
        });

        return future;
    }

    public CompletableFuture<ArrayList<Room>> getAllRoomsAsync()
    {
        var future = new CompletableFuture<ArrayList<Room>>();
        m_firestore_db.collection(COLLECTION_NAME)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                var rooms = new ArrayList<Room>(queryDocumentSnapshots.toObjects(Room.class));
                future.complete(rooms);
            })
            .addOnFailureListener(e -> {
                future.completeExceptionally(e);
            });

        return future;
    }

    public CompletableFuture<Room> getRoomInfoAsync(String roomName)
    {
        var future = new CompletableFuture<Room>();
        m_firestore_db.collection(COLLECTION_NAME)
            .document(roomName)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists())
                {
                    var room = documentSnapshot.toObject(Room.class);
                    future.complete(room);
                }
                else
                {
                    future.completeExceptionally(new RuntimeException("Stanza '" + roomName + "' non trovata."));
                }
            })
            .addOnFailureListener(e -> {
                future.completeExceptionally(e);
            });

        return future;
    }

}
