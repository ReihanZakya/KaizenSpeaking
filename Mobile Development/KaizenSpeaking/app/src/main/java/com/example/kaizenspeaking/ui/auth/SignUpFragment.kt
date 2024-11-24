package com.example.kaizenspeaking.ui.auth

import android.os.Bundle
import android.util.Patterns
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

class SignUpFragment : Fragment() {
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnGoogle: TextView
    private lateinit var btnLogin: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.button)
        btnGoogle = view.findViewById(R.id.button2)
        btnLogin = view.findViewById(R.id.btnMasuk)
        

        btnRegister.setOnClickListener {
        }

        btnLogin.setOnClickListener {
            findNavController().navigate(R.id.nav_to_signInFragment)
        }

        btnGoogle.setOnClickListener {
            // Implement Google Sign-Up logic
            Toast.makeText(context, "Google Sign-Up Coming Soon", Toast.LENGTH_SHORT).show()
        }

        return view
    }

}


