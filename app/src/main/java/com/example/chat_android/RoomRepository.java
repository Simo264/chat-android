package com.example.chat_android;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RoomRepository
{
    private static volatile RoomRepository instance;

    public static final String FIRESTORE_TAG = "FIRESTORE_DEBUG";
    public static final String COLLECTION_ROOMS = "rooms";
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
     * repository.createRoomAsync("NomeStanza", username)
     *      .thenAccept(room -> { ... })
     *      .exceptionally(ex -> { Log.e(TAG, "Errore: " + ex.getMessage()); return null; });
     *
     * @param room_name Il nome univoco della stanza (diventa l'ID del documento).
     * @param creator_name Il nome dell'utente che crea la stanza.
     * @return Un CompletableFuture che contiene l'oggetto Room creato.
     */
    public CompletableFuture<RoomEntity> createRoomAsync(String room_name, String creator_name)
    {
        var future = new CompletableFuture<RoomEntity>();
        var document = m_firestore_db.collection(COLLECTION_ROOMS).document(room_name);
        m_firestore_db.runTransaction(transaction ->
        {
            var snapshot = transaction.get(document);
            if (snapshot.exists())
            {
                var is_deleted = snapshot.getBoolean("is_delete");
                if (Boolean.FALSE.equals(is_deleted))
                    throw new RuntimeException("La stanza '" + room_name + "' esiste già ed è attiva!");
            }

            var new_room = new RoomEntity(room_name, creator_name);
            new_room.users.add(creator_name);

            transaction.set(document, new_room);
            return new_room;
        })
        .addOnSuccessListener(future::complete)
        .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    /**
     * Effettua una soft delete di una stanza.
     * ESEMPIO:
     * repository.deleteRoomAsync("NomeStanza", username)
     *      .thenRun(() -> { ... })
     *      .exceptionally(ex -> { Log.e(TAG, "Errore: " + ex.getMessage()); return null; });
     *
     * @param room_name ID della stanza da eliminare.
     * @param username Nome dell'utente che richiede l'eliminazione.
     * @return Un CompletableFuture<Void> che si completa in caso di successo.
     */
    public CompletableFuture<Void> deleteRoomAsync(String room_name, String username)
    {
        var future = new CompletableFuture<Void>();
        var document = m_firestore_db.collection(COLLECTION_ROOMS).document(room_name);
        m_firestore_db.runTransaction(transaction ->
        {
            var snapshot = transaction.get(document);
            if (!snapshot.exists())
                throw new RuntimeException("Il documento " + room_name + " non esiste.");

            if(Boolean.TRUE.equals(snapshot.getBoolean("is_delete")))
                throw new RuntimeException("La stanza " + room_name + " è già stata eliminata.");

            var room = snapshot.toObject(RoomEntity.class);
            if(room == null)
                throw new RuntimeException("Errore nel caricamento dei dati della stanza.");

            // Solo il creatore può eliminare la stanza
            if (!room.creator_name.equals(username))
                throw new RuntimeException("Non hai il permesso di eliminare la stanza.");

            if (!room.users.isEmpty())
                throw new RuntimeException("Impossibile eliminare: ci sono ancora utenti all'interno.");

            transaction.update(document, "is_delete", true);
            return null;
        })
        .addOnSuccessListener(result -> future.complete(null))
        .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Void> leaveRoomAsync(String room_name, String username)
    {
        var future = new CompletableFuture<Void>();
        var document = m_firestore_db.collection(COLLECTION_ROOMS).document(room_name);
        m_firestore_db.runTransaction(transaction ->
        {
            var snapshot = transaction.get(document);
            if (!snapshot.exists())
                throw new RuntimeException("Il documento " + room_name + " non esiste");

            transaction.update(document, "users", FieldValue.arrayRemove(username));
            return null;
        })
        .addOnSuccessListener(result -> future.complete(null))
        .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public CompletableFuture<Void> joinRoomAsync(String room_name, String username)
    {
        var future = new CompletableFuture<Void>();
        var document = m_firestore_db.collection(COLLECTION_ROOMS).document(room_name);
        m_firestore_db.runTransaction(transaction ->
        {
            var snapshot = transaction.get(document);
            if (!snapshot.exists())
                throw new RuntimeException("Il documento '" + room_name + "' non esiste.");

            if(Boolean.TRUE.equals(snapshot.getBoolean("is_delete")))
                throw new RuntimeException("La stanza '" + room_name + "' è stata eliminata.");

            transaction.update(document, "users", FieldValue.arrayUnion(username));
            return null;
        })
        .addOnSuccessListener(result -> future.complete(null))
        .addOnFailureListener(future::completeExceptionally);
        return future;
    }

    public void observeAllRooms(RoomsListener listener)
    {
        removeAllRoomsListener();

        m_rooms_listener = m_firestore_db
            .collection(COLLECTION_ROOMS)
            .whereEqualTo("is_delete", false)
            .addSnapshotListener((snapshots, error) ->
            {
                if (error != null)
                {
                    listener.onError(error);
                    return;
                }
                if (snapshots != null)
                {
                    var room_entities = snapshots.toObjects(RoomEntity.class);
                    var rooms = new ArrayList<RoomParcel>();
                    for(var e : room_entities)
                        rooms.add(new RoomParcel(e));

                    listener.onRoomsUpdated(rooms);
                }
            });
    }

    public void observeRoom(String roomName, SingleRoomListener listener)
    {
        removeSingleRoomListener();

        m_single_room_listener = m_firestore_db
            .collection(COLLECTION_ROOMS)
            .document(roomName)
            .addSnapshotListener((snapshot, error) ->
            {
                if (error != null)
                {
                    listener.onError(error);
                    return;
                }
                if (snapshot != null && snapshot.exists())
                {
                    var room_entity = snapshot.toObject(RoomEntity.class);
                    if(room_entity != null)
                        listener.onRoomUpdated(new RoomParcel(room_entity));
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
