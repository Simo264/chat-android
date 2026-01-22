package com.example.chat_android;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RoomRepository
{
    private static volatile RoomRepository instance;

    public static final String FIRESTORE_TAG = "FIRESTORE_DEBUG";
    private static final String COLLECTION_NAME = "rooms";

    private final FirebaseFirestore m_firestore_db;
    private ListenerRegistration m_rooms_listener = null;
    private ListenerRegistration m_single_room_listener = null;

    private RoomRepository()
    {
        m_firestore_db = FirebaseFirestore.getInstance();
    }

    public static RoomRepository getInstance()
    {
        if (instance == null)
        {
            synchronized (RoomRepository.class)
            {
                if (instance == null)
                    instance = new RoomRepository();
            }
        }
        return instance;
    }

    /**
     * Crea una nuova stanza in Firestore utilizzando il nome come ID documento.*
     *
     * ESEMPIO DI UTILIZZO:
     * repository.createRoomAsync("NomeStanza", uid)
     *      .thenAccept(room -> { ... })
     *      .exceptionally(ex -> { Log.e(TAG, "Errore: " + ex.getMessage()); return null; });
     *
     * @param room_name Il nome univoco della stanza (diventa l'ID del documento).
     * @param creator_uid L'UID dell'utente che crea la stanza.
     * @return Un CompletableFuture che contiene l'oggetto Room creato.
     */
    public CompletableFuture<Room> createRoomAsync(String room_name, String creator_uid, String creator_name)
    {
        var future = new CompletableFuture<Room>();
        var document = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        m_firestore_db.runTransaction(transaction ->
        {
            var snapshot = transaction.get(document);
            if (snapshot.exists())
                throw new RuntimeException("La stanza '" + room_name + "' esiste già!");

            var new_room = new Room(room_name, creator_uid, creator_name, new ArrayList<String>());
            new_room.users.add(creator_uid);

            transaction.set(document, new_room);
            return new_room;
        })
        .addOnSuccessListener(result -> future.complete(result))
        .addOnFailureListener(e -> future.completeExceptionally(e));

        return future;
    }

    /**
     * Effettua una soft delete di una stanza.

     * ESEMPIO:
     * repository.deleteRoomAsync("NomeStanza", currentUid)
     *      .thenRun(() -> { ... })
     *      .exceptionally(ex -> { Log.e(TAG, "Errore: " + ex.getMessage()); return null; });
     *
     * @param room_name ID della stanza da eliminare.
     * @param uid UID dell'utente che richiede l'eliminazione.
     * @return Un CompletableFuture<Void> che si completa in caso di successo.
     */
    public CompletableFuture<Void> deleteRoomAsync(String room_name, String uid)
    {
        var future = new CompletableFuture<Void>();
        var document = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        m_firestore_db.runTransaction(transaction ->
        {
            var snapshot = transaction.get(document);
            if (!snapshot.exists())
                throw new RuntimeException("Impossibile eliminare: la stanza '" + room_name + "' non esiste.");

            var room = snapshot.toObject(Room.class);
            if(room == null)
                throw new RuntimeException("La stanza non è valida.");

            // Solo il creatore può eliminare la stanza
            if (!room.creator_uid.equals(uid))
                throw new RuntimeException("Non hai il permesso di eliminare la stanza.");

            if (!room.users.isEmpty())
                throw new RuntimeException("Impossibile eliminare: ci sono ancora utenti all'interno.");

            transaction.update(document, "is_delete", true);
            return null;
        })
        .addOnSuccessListener(result -> future.complete(null))
        .addOnFailureListener(e -> future.completeExceptionally(e));
        return future;
    }

    public CompletableFuture<Void> leaveRoomAsync(String room_name, String username)
    {
        var future = new CompletableFuture<Void>();
        var document = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        document.update("users", FieldValue.arrayRemove(username))
            .addOnSuccessListener(aVoid -> future.complete(null))
            .addOnFailureListener(future::completeExceptionally);

        return future;
    }

    public CompletableFuture<Void> joinRoomAsync(String room_name, String username)
    {
        var future = new CompletableFuture<Void>();
        var document = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        document.update("users", FieldValue.arrayUnion(username))
            .addOnSuccessListener(aVoid -> future.complete(null))
            .addOnFailureListener(future::completeExceptionally);

        return future;
    }


    public void observeAllRooms(RoomsListener listener)
    {
        removeAllRoomsListener();

        m_rooms_listener = m_firestore_db
            .collection(COLLECTION_NAME)
            .whereEqualTo("is_delete", false)
            .addSnapshotListener((snapshots, error) -> {

                if (error != null)
                {
                    listener.onError(error);
                    return;
                }
                if (snapshots != null)
                {
                    var rooms = new ArrayList<Room>(snapshots.toObjects(Room.class));
                    listener.onRoomsUpdated(rooms);
                }
            });
    }

    public void observeRoom(String roomName, SingleRoomListener listener)
    {
        removeSingleRoomListener();

        m_single_room_listener = m_firestore_db
            .collection(COLLECTION_NAME)
            .document(roomName)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null)
                {
                    listener.onError(error);
                    return;
                }
                if (snapshot != null && snapshot.exists())
                {
                    var room = snapshot.toObject(Room.class);
                    listener.onRoomUpdated(room);
                }
            });
    }

    public void removeAllRoomsListener()
    {
        if (m_rooms_listener != null)
        {
            m_rooms_listener.remove();
            m_rooms_listener = null;
        }
    }

    public void removeSingleRoomListener()
    {
        if (m_single_room_listener != null)
        {
            m_single_room_listener.remove();
            m_single_room_listener = null;
        }
    }
}
