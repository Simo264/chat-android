package com.example.chat_android;

public interface SendMessageCallback
{
    void onProgress(int progress);
    void onSuccess();
    void onError(Exception e);
}
