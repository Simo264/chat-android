package com.example.chat_android;

import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class SignupFragment extends AuthFragment
{
    @Override
    protected void initFragment() {
        m_text_title.setText(getString(R.string.title_signup));
        m_button_submit.setText(getString(R.string.button_submit));
        m_button_submit.setOnClickListener(v -> {
           authentication();
        });

        m_text_nav_link.setText(Html.fromHtml(getString(R.string.go_to_login), Html.FROM_HTML_MODE_LEGACY));
        m_text_nav_link.setOnClickListener(v -> {
            // Torna indietro al LoginFragment
            getParentFragmentManager().popBackStack();
        });
    }

    @Override
    protected void authentication()
    {
        var email = m_input_email.getText().toString().trim();
        var password = m_input_password.getText().toString().trim();
        var isValid = true;
        if (email.isEmpty())
        {
            m_layout_email.setError(getString(R.string.error_empty_fields));
            isValid = false;
        }
        if (password.isEmpty())
        {
            m_layout_password.setError(getString(R.string.error_empty_fields));
            isValid = false;
        }
        else if (password.length() < 6)
        {
            m_layout_password.setError(getString(R.string.error_password_too_short));
            isValid = false;
        }

        if (!isValid)
            return;

        m_firebase_auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task ->
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(getContext(), getString(R.string.success_signup), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthUserCollisionException)
                    {
                        m_layout_email.setError(getString(R.string.error_email_already_exists));
                    }
                    else if (e instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        m_layout_email.setError(getString(R.string.error_invalid_email_format));
                    }
                    else
                    {
                        Toast.makeText(getContext(), getString(R.string.error_registration_failed), Toast.LENGTH_LONG).show();
                    }
                }
            });

        return;
    }
}