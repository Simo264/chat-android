package com.example.chat_android;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public abstract class AuthFragment extends Fragment
{
    protected TextInputLayout m_layout_email;
    protected TextInputLayout m_layout_password;
    protected TextInputEditText m_input_email;
    protected TextInputEditText m_input_password;

    protected TextView m_text_title;
    protected TextView m_text_nav_link;
    protected Button m_button_submit;
    protected FirebaseAuth m_firebase_auth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        m_layout_email = view.findViewById(R.id.layoutEmail);
        m_layout_password = view.findViewById(R.id.passwordInputLayout);
        m_input_email = view.findViewById(R.id.inputEmail);
        m_input_password = view.findViewById(R.id.inputPassword);

        m_text_title = view.findViewById(R.id.textTitle);
        m_text_nav_link = view.findViewById(R.id.textNavigationLink);
        m_button_submit = view.findViewById(R.id.buttonSubmit);
        m_firebase_auth = FirebaseAuth.getInstance();

        // Pulizia automatica errori quando l'utente scrive
        setupErrorCleanup();

        initFragment();
    }

    private abstract class SimpleTextWatcher implements TextWatcher
    {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
    private void setupErrorCleanup()
    {
        m_input_email.addTextChangedListener(new SimpleTextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                m_layout_email.setError(null);
            }
        });
        m_input_password.addTextChangedListener(new SimpleTextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                m_layout_password.setError(null);
            }
        });
    }



    protected abstract void initFragment();
    protected abstract void authentication();
}