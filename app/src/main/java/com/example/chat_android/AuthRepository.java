package com.example.chat_android;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository
{
    private FirebaseAuth m_firebase_auth;

    public AuthRepository()
    {
        m_firebase_auth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser()
    {
        return m_firebase_auth.getCurrentUser();
    }

    public boolean isUserLoggedIn()
    {
        return this.getCurrentUser() != null;
    }

    public void signOut()
    {
        m_firebase_auth.signOut();
    }
}
