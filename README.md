# Progetto Android: chat real-time discord-like

## Descrizione generale

Un'applicazione di messaggistica che permette agli utenti di creare e partecipare
a stanze di chat in tempo reale, ispirata a Discord. 
Gli utenti possono comunicare in canali (stanze), condividere contenuti multimediali 
e ricevere notifiche in tempo reale.

## Funzionalità principali

### 1. Autenticazione utenti

- Registrazione e login tramite email/password
- Autenticazione con Firebase Authentication
- Profilo utente personalizzabile (nome, avatar, stato)

### 2. Gestione stanze

- Creazione/cancellazione di stanze
- Lista delle stanze disponibili con ricerca
- Meccanismi di join/leave nelle stanze
- Possibilità eliminare stanze (solo per il creatore)
- Conteggio membri in tempo reale

### 3. Messaggistica real-time

- Invio e ricezione messaggi testuali (opzionalmente anche contenuti multimediali)
- Timestamp e informazioni mittente
- Stato dei messaggi (inviato, consegnato, letto)

### 4. Contenuti multimediali (opzionale)

- Invio di immagini dalla galleria o fotocamera
- Storage delle immagini su Firebase Storage

### 5. Notifiche

- Notifiche push per nuovi messaggi
- Notifiche solo quando l'app è in background

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

