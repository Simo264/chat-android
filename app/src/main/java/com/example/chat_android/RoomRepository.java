package com.example.chat_android;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class RoomRepository
{
    public static final String FIRESTORE_TAG = "FIRESTORE_DEBUG";
    private static final String COLLECTION_NAME = "rooms";
    private FirebaseFirestore m_firestore_db;

    public RoomRepository()
    {
        m_firestore_db = FirebaseFirestore.getInstance();
    }


    /**
     * Crea una nuova stanza in Firestore utilizzando il nome come ID documento.
     * L'operazione è ATOMICA: se la stanza esiste già, il Future fallisce.
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
    public CompletableFuture<Room> createRoomAsync(String room_name, String creator_uid)
    {
        var future = new CompletableFuture<Room>();
        var roomRef = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        m_firestore_db.runTransaction(transaction -> {
            var snapshot = transaction.get(roomRef);
            if (snapshot.exists())
                throw new RuntimeException("La stanza '" + room_name + "' esiste già!");

            var new_room = new Room(room_name, creator_uid, new ArrayList<String>());
            new_room.user_uids.add(creator_uid);

            transaction.set(roomRef, new_room);
            return new_room;
        }).addOnSuccessListener(result -> {
            future.complete(result);
        }).addOnFailureListener(e -> {
            future.completeExceptionally(e);
        });

        return future;
    }

    /**
     * Elimina una stanza esistente dopo aver verificato i permessi e lo stato.
     * L'operazione è ATOMICA e fallisce se:
     * - la stanza non esiste
     * - l'utente (uid) non è il creatore originale
     * - la stanza contiene ancora altri utenti
     *
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
        var roomRef = m_firestore_db.collection(COLLECTION_NAME).document(room_name);
        m_firestore_db.runTransaction(transaction -> {
            var snapshot = transaction.get(roomRef);
            if (!snapshot.exists())
                throw new RuntimeException("Impossibile eliminare: la stanza '" + room_name + "' non esiste.");

            var room = snapshot.toObject(Room.class);
            if(room == null)
                throw new RuntimeException("La stanza non è valida.");

            // Solo il creatore può eliminare la stanza
            if (!room.creator_uid.equals(uid))
                throw new RuntimeException("Non hai il permesso di eliminare la stanza.");

            if (room.getUserCount() >= 1)
                throw new RuntimeException("Impossibile eliminare: ci sono ancora utenti all'interno.");

            transaction.update(roomRef, "is_delete", true);
            return null;
        }).addOnSuccessListener(result -> {
            future.complete(null);
        }).addOnFailureListener(e -> {
            future.completeExceptionally(e);
        });

        return future;
    }

    /**
     * Recupera l'elenco completo di tutte le stanze presenti nella collezione.
     * Converte i documenti Firestore in una lista di oggetti Room modificabile.
     *
     * ESEMPIO:
     * repository.getAllRoomsAsync()
     *      .thenAccept(rooms -> { ... });
     *
     * @return Un CompletableFuture contenente un ArrayList<Room>.
     * Ritorna una lista vuota se non ci sono stanze.
     */
    public CompletableFuture<ArrayList<Room>> getAllRoomsAsync()
    {
        var future = new CompletableFuture<ArrayList<Room>>();
        m_firestore_db.collection(COLLECTION_NAME)
            .whereEqualTo("is_delete", false)
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

    /**
     * Recupera i dati dettagliati di una singola stanza tramite il suo nome (ID).
     *
     * ESEMPIO:
     * repository.getRoomInfoAsync("Sala_01")
     *      .thenAccept(room -> { ... })
     *      .exceptionally(ex -> { Log.e(TAG, "Errore: " + ex.getMessage()); return null; });
     *
     * @param roomName Il nome univoco della stanza da cercare.
     * @return Un CompletableFuture con l'oggetto Room richiesto.
     * Fallisce se la stanza non esiste.
     */
    public CompletableFuture<Room> getRoomInfoAsync(String roomName)
    {
        var future = new CompletableFuture<Room>();
        m_firestore_db.collection(COLLECTION_NAME)
            .document(roomName)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Room room = documentSnapshot.toObject(Room.class);

                if (documentSnapshot.exists() && room != null && !room.is_delete)
                {
                    future.complete(room);
                }
                else
                {
                    // Se è cancellato logicamente, lo trattiamo come se non esistesse
                    future.completeExceptionally(new RuntimeException("Stanza '" + roomName + "' non disponibile."));
                }
            })
            .addOnFailureListener(e -> {
                future.completeExceptionally(e);
            });
        return future;
    }
}
