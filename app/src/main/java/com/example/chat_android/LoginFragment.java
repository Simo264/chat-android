package com.example.chat_android;

import android.content.Intent;
import android.text.Html;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginFragment extends AuthFragment
{
    @Override
    protected void initFragment()
    {
        m_text_title.setText(getString(R.string.title_login));

        m_button_submit.setOnClickListener(v -> {
            authentication();
        });

        m_text_nav_link.setText(Html.fromHtml(getString(R.string.go_to_signup), Html.FROM_HTML_MODE_LEGACY));
        m_text_nav_link.setOnClickListener(v -> {
            getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SignupFragment())
                .addToBackStack(null)
                .commit();
        });
    }
    @Override
    protected void authentication()
    {
        var email = m_input_email.getText().toString().trim();
        var password = m_input_password.getText().toString().trim();
        if (email.isEmpty())
        {
            m_layout_email.setError(getString(R.string.error_empty_fields));
            return;
        }
        if (password.isEmpty())
        {
            m_layout_password.setError(getString(R.string.error_empty_fields));
            return;
        }

        m_firebase_auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
                else
                {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthInvalidUserException)
                    {
                        m_layout_email.setError(getString(R.string.error_user_not_found));
                    }
                    else if (e instanceof FirebaseAuthInvalidCredentialsException)
                    {
                        m_layout_password.setError(getString(R.string.error_wrong_auth));
                    }
                    else
                    {
                        Toast.makeText(getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}