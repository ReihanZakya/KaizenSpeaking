package com.example.kaizenspeaking.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kaizenspeaking.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInFragment : Fragment() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnGoogle: TextView
    private lateinit var btnRegister: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnSignIn = view.findViewById(R.id.button)
        btnGoogle = view.findViewById(R.id.button2)
        btnRegister = view.findViewById(R.id.btnDaftar)


        btnSignIn.setOnClickListener {
        }

        btnRegister.setOnClickListener {
            findNavController().navigate(R.id.nav_to_signUpFragment)
        }

        btnGoogle.setOnClickListener {
            // Implement Google Sign-In logic
            Toast.makeText(context, "Google Sign-In Coming Soon", Toast.LENGTH_SHORT).show()
        }

        return view
    }

}