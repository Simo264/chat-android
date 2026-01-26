package com.example.chat_android;

import java.util.ArrayList;

public interface MessagesListener
{
    void onMessagesUpdated(ArrayList<MessageEntity> messages);
    void onError(Exception e);
}