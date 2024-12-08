package com.example.kaizenspeaking.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.kaizenspeaking.MainActivity
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.ActivitySignInBinding
import com.example.kaizenspeaking.ui.auth.data.LoginBody
import com.example.kaizenspeaking.ui.auth.data.User
import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
import com.example.kaizenspeaking.ui.auth.utils.APIService
import com.example.kaizenspeaking.ui.auth.view_model.SignInActivityViewModel
import com.example.kaizenspeaking.ui.auth.view_model.SignInActivityViewModelFactory
import com.example.kaizenspeaking.utils.UserSession

class SignInActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var mViewModel: SignInActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(LayoutInflater.from(this))
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)

        binding.btnGoogle.setOnClickListener(this)
        binding.btnMasuk.setOnClickListener(this)
        binding.btnDaftar.setOnClickListener(this)
        binding.btnSignIn.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)
        binding.etEmail.onFocusChangeListener = this
        binding.etPassword.onFocusChangeListener = this
        binding.etPassword.setOnKeyListener(this)

        mViewModel = ViewModelProvider(
            this,
            SignInActivityViewModelFactory(AuthRepository(APIService.getService()), application)
        ).get(SignInActivityViewModel::class.java)
        setupObservers()

        binding.btnDaftar.setOnClickListener {
            // Implement navigation to SignUpActivity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupObservers() {
        mViewModel.getIsLoading().observe(this) {
            binding.progressBar.isVisible = it
        }


        mViewModel.getErrorMessage().observe(this) {
            val formErrorKeys = arrayOf("email", "password")
            val message = StringBuilder()

            it.map { entry ->
                if (formErrorKeys.contains(entry.key)) {
                    when (entry.key) {
                        "email" -> {
                            binding.etEmailTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

                        "password" -> {
                            binding.etPasswordTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

                        else -> {
                            message.append(entry.value).append("\n")
                        }
                    }
                } else {
                    message.append(entry.value).append("\n")
                }
            }

            if (message.isNotEmpty()) {
                handleLoginErrors(it)
            }
        }



        mViewModel.getUser().observe(this) { user ->
            if (user != null) {
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleLoginErrors(errorMap: HashMap<String, String>?) {
        errorMap?.let { errors ->
            arrayOf("email", "password")
            val generalErrorMessage = StringBuilder()

            errors.forEach { (key, value) ->
                when (key) {
                    "email" -> {
                        binding.etEmailTil.apply {
                            isErrorEnabled = true
                            error = value
                        }
                    }

                    "password" -> {
                        binding.etPasswordTil.apply {
                            isErrorEnabled = true
                            error = value
                        }
                    }

                    else -> generalErrorMessage.append(value).append("\n")
                }
            }

            if (generalErrorMessage.isNotEmpty()) {
                showGeneralErrorDialog(generalErrorMessage.toString())
            }
        }
    }


    private fun showGeneralErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_kaizen)
            .setTitle("LOGIN ERROR!")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun validateEmail(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val value = binding.etEmail.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Email Harus Diisi"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            errorMessage = "Email tidak valid"
        }
        if (errorMessage != null && shouldUpdateView) {
            binding.etEmailTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePassword(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val value = binding.etPassword.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Password Harus Diisi"
        } else if (value.length < 8) {
            errorMessage = "Password Harus Lebih dari 8 Karakter"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.etPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validate(): Boolean {
        var isValid = true
        if (!validateEmail()) isValid = false
        if (!validatePassword()) isValid = false

        return isValid
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btnMasuk -> {
                    submitForm()
                }

                R.id.btnGoogle -> {
                    handleGoogleSignUp()
                }

                R.id.btnSignUp -> {
                    val intent = Intent(this, SignUpActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun handleGoogleSignUp() {
        Toast.makeText(this, "Dalam Pengembangan", Toast.LENGTH_SHORT).show()
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {

                R.id.etEmail -> {
                    if (hasFocus) {
                        if (binding.etEmailTil.isErrorEnabled) {
                            binding.etEmailTil.isErrorEnabled = false
                        }
                    } else {
                        validateEmail()
                    }
                }

                R.id.etPassword -> {
                    if (hasFocus) {
                        if (binding.etPasswordTil.isErrorEnabled) {
                            binding.etPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        validatePassword()
                    }
                }
            }
        }
    }

    private fun showSuccessToast(userName: String) {
        Toast.makeText(
            this,
            "Selamat datang, $userName!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun submitForm() {
        if (validate()) {
            mViewModel.loginUser(
                LoginBody(
                    binding.etEmail.text!!.toString(),
                    binding.etPassword.text!!.toString()
                ), this
            )
        }
    }

    override fun onKey(v: View?, event: Int, keyEvent: KeyEvent?): Boolean {
        if (event == KeyEvent.KEYCODE_ENTER && keyEvent!!.action == KeyEvent.ACTION_UP) {
            submitForm()
        }
        return false
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}
