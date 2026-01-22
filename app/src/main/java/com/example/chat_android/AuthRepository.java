package com.example.chat_android;

import com.google.firebase.auth.FirebaseAuth;

public class AuthRepository
{
    private static volatile AuthRepository instance;
    private final FirebaseAuth m_firebase_auth;

    private AuthRepository()
    {
        m_firebase_auth = FirebaseAuth.getInstance();
    }

    public static AuthRepository getInstance()
    {
        if (instance == null)
        {
            synchronized (AuthRepository.class)
            {
                if (instance == null)
                    instance = new AuthRepository();
            }
        }
        return instance;
    }

    public boolean isUserLoggedIn()
    {
        var user = m_firebase_auth.getCurrentUser();
        return user != null;
    }

    public void signOut()
    {
        m_firebase_auth.signOut();
    }

    public String getUsername()
    {
        var user = m_firebase_auth.getCurrentUser();
        assert user != null;

        var email = getUserEmail();
        return email.split("@")[0];
    }

    public String getUserEmail()
    {
        var user = m_firebase_auth.getCurrentUser();
        assert user != null;

        return user.getEmail();
    }

    public String getUserUid()
    {
        var user = m_firebase_auth.getCurrentUser();
        assert user != null;

        return user.getUid();
    }

    public String getUserCreationTime()
    {
        var user = m_firebase_auth.getCurrentUser();
        assert user != null;

        var creation_timestamp = user.getMetadata().getCreationTimestamp();
        var data_format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        return data_format.format(new java.util.Date(creation_timestamp));
    }
}
