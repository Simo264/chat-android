# Progetto Android: chat real-time discord-like

## Descrizione generale

Un'applicazione di messaggistica che permette agli utenti di creare e partecipare
a stanze di chat in tempo reale, ispirata a Discord.
Gli utenti possono comunicare in canali (stanze), condividere contenuti multimediali
e ricevere notifiche in tempo reale.

## Funzionalità principali

### 1. Autenticazione utenti

- Registrazione, login e logout tramite Firebase Authentication
- Visualizzazione dei metadati dell'utente in un'activity dedicata (username, email, uid, data creazione)

### 2. Gestione stanze

- Creazione di nuove stanze
- Eliminazione stanze (solo per il proprietario)
- Lista delle stanze disponibili con filtri: visualizza tutte le stanze, 
visualizza stanze create dall'utente, visualizza stanze attive in cui l'utente 
partecipa
- Possibilità di entrare/uscire da qualsiasi stanza in qualsiasi momento
- Aggiornamenti in tempo reale del numero di partecipanti per ogni stanza, 
delle stanze presenti e nuove stanze create, della lista degli utenti presenti 
in una specifica stanza

### 3. Messaggistica real-time

- Invio di messaggi testuali
- Invio di foto e video dalla galleria
- Invio di foto e video dalla fotocamera
- Visualizzazione in tempo reale dei messaggi
- I messaggi vengono ricevuti da tutti gli utenti presenti nella stanza
- Possibilità di eliminare uno specifico messaggio selezionandolo
- Storage dei contenuti multimediali su Firebase Storage

## Struttura del database (firestore)

Il progetto utilizza Firebase Firestore come database documentale. 
La struttura è organizzata in due collezioni principali: `rooms` e `messages`. 

La collezione `rooms` gestisce l'esistenza, i metadati e lo stato di ogni stanza:

- **id-documento**: rappresenta il nome della stanza (es: "Generale", "Sviluppatori")
- **nome**: il nome identificativo della stanza
- **creator_name**: username dell'utente che ha creato la stanza
- **users**: elenco degli utenti attualmente presenti nella stanza
- **is_delete**: flag per il soft-delete 

La collezione `messages` gestisce i contenuti delle chat. 
Ogni documento in questa collezione funge da contenitore per i messaggi di una 
specifica stanza.

- **id-documento**: rappresenta il nome della stanza (corrispondente all'ID 
nella collezione `rooms`)

All'interno di ogni documento stanza, i messaggi sono salvati in questa raccolta 
dedicata.

- **from**: username del mittente
- **text**: il contenuto testuale del messaggio
- **media_url**: l'url pubblico della risorsa caricata su Firebase Storage
- **media_type**: il tipo del contenuto (immagine o video)
- **timestamp**: data di invio 

Nota: quando una stanza viene eliminata, il campo `is_delete` in `rooms` 
viene impostato a `true`. 
Questo permette di mantenere l'ID storico impedendo nuove creazioni con lo stesso 
nome a meno di sovrascrittura.
Durante l'eliminazione definitiva di una stanza da parte del creatore, 
la sotto-collezione `room_messages` viene svuotata fisicamente per ottimizzare 
lo storage.


## Architettura del sistema

Il progetto adotta un'architettura client-server distribuita dove le 
applicazioni Android rappresentano i client e Firebase funge da backend 
centralizzato nel cloud. 
Questa scelta sfrutta i servizi Backend-as-a-Service (BaaS) offerti da Firebase, 
eliminando la necessità di implementare e gestire manualmente un server.

Ogni dispositivo Android che esegue l'applicazione costituisce un client 
indipendente che comunica con Firebase attraverso l'SDK ufficiale. 
Firebase, posizionato nel cloud di Google, gestisce tutte le operazioni server-side:
autenticazione degli utenti, persistenza dei dati, sincronizzazione real-time tra i 
client, archiviazione dei file multimediali e invio delle notifiche push. 
I client non comunicano direttamente tra loro, ma scambiano informazioni 
esclusivamente attraverso Firebase che agisce come intermediario centralizzato.

Il modello di comunicazione è basato su eventi e reattività. 
I client non effettuano richieste periodiche (polling) per verificare nuovi dati, 
ma rimangono in ascolto passivo. 
Firebase notifica proattivamente i cambiamenti solo quando effettivamente avvengono, 
riducendo drasticamente il consumo di batteria e bandwidth.

