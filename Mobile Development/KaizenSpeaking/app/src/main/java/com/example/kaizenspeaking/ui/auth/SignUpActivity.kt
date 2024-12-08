package com.example.kaizenspeaking.ui.auth

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.kaizenspeaking.MainActivity
import com.example.kaizenspeaking.R
import com.example.kaizenspeaking.databinding.ActivitySignUpBinding
import com.example.kaizenspeaking.ui.auth.data.RegisterBody
import com.example.kaizenspeaking.ui.auth.data.ValidateEmailBody
import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
import com.example.kaizenspeaking.ui.auth.utils.APIService
import com.example.kaizenspeaking.ui.auth.view_model.SignUpActivityViewModel
import com.example.kaizenspeaking.ui.auth.view_model.SignUpActivityViewModelFactory


class SignUpActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener, TextWatcher {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mViewModel: SignUpActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setupListeners()

        mViewModel = ViewModelProvider(
            this,
            SignUpActivityViewModelFactory(AuthRepository(APIService.getService()), application)
        ).get(SignUpActivityViewModel::class.java)
        setupObservers()
    }

    private fun setupListeners() {
        // Set click listeners
        binding.btnSignUp.setOnClickListener(this)
        binding.btnSignUpGoogle.setOnClickListener(this)
        binding.btnMasuk.setOnClickListener(this)
        binding.btnBack.setOnClickListener(this)

        // Set focus listeners
        binding.etName.onFocusChangeListener = this
        binding.etEmail.onFocusChangeListener = this
        binding.etPassword.onFocusChangeListener = this
        binding.etConfirmPassword.onFocusChangeListener = this

        binding.etPassword.setOnKeyListener(this)
        binding.etConfirmPassword.setOnKeyListener(this)
        binding.etEmail.setOnKeyListener(this)

        // Add text change listeners for password validation
        binding.etPassword.addTextChangedListener(this)
        binding.etConfirmPassword.addTextChangedListener(this)
        binding.etEmail.addTextChangedListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnSignUp -> onSubmit()
            R.id.btnSignUpGoogle -> handleGoogleSignUp()
            R.id.btnMasuk -> navigateToSignIn()
            R.id.btn_back -> navigateToHome()
        }
    }

    private fun handleGoogleSignUp() {
        Toast.makeText(this, "Dalam Pengembangan", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupObservers() {
        mViewModel.getIsLoading().observe(this) {
            binding.progressBar.isVisible = it
        }


        mViewModel.getErrorMessage().observe(this) {
            val formErrorKeys = arrayOf("name", "email", "password")
            val message = StringBuilder()

            it.map { entry ->
                if (formErrorKeys.contains(entry.key)) {
                    when (entry.key) {
                        "name" -> {
                            binding.etNameTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

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

                    }
                } else {
                    message.append(entry.value).append("\n")
                }
                if (message.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_kaizen)
                        .setTitle("INFORMATION!")
                        .setMessage(message)
                        .setPositiveButton("OK") { dialog, _ -> dialog!!.dismiss() }.show()
                }
            }


        }

        mViewModel.getUser().observe(this) {
            if (it != null) {
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }
    }

    private fun validateFullName(): Boolean {
        var errorMessage: String? = null
        val value = binding.etName.text.toString()

        if (value.isEmpty()) {
            errorMessage = "Nama Harus Diisi"
        }

        binding.etNameTil.apply {
            isErrorEnabled = errorMessage != null
            error = errorMessage
        }

        return errorMessage == null
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
        } else if (!value.any { it.isUpperCase() }) {
            errorMessage = "Password Harus Mengandung Minimal 1 Huruf Besar"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.etPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validateConfirmPassword(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val password = binding.etPassword.text.toString()

        if (confirmPassword.isEmpty()) {
            errorMessage = "Confirm password Harus Diisi"
        } else if (confirmPassword.length < 8) {
            errorMessage = "Password Harus Lebih dari 8 Karakter"
        } else if (password != confirmPassword) {
            errorMessage = "Password Tidak Sama"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.etConfirmPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    private fun validatePasswordAndConfirmPassword(shouldUpdateView: Boolean = true): Boolean {
        var errorMessage: String? = null
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (password != confirmPassword) {
            errorMessage = "Password tidak sama"
        }

        if (errorMessage != null && shouldUpdateView) {
            binding.etConfirmPasswordTil.apply {
                isErrorEnabled = true
                error = errorMessage
            }
        }

        return errorMessage == null
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if (view != null) {
            when (view.id) {
                R.id.etName -> {
                    if (hasFocus) {
                        if (binding.etNameTil.isErrorEnabled) {
                            binding.etNameTil.isErrorEnabled = false
                        }
                    } else {
                        validateFullName()
                    }
                }

                R.id.etEmail -> {
                    if (hasFocus) {
                        if (binding.etEmailTil.isErrorEnabled) {
                            binding.etEmailTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateEmail()) {
                            ValidateEmailBody(binding.etEmail.text!!.toString())
                        }
                    }
                }

                R.id.etPassword -> {
                    if (hasFocus) {
                        if (binding.etPasswordTil.isErrorEnabled) {
                            binding.etPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword() && validatePasswordAndConfirmPassword()) {
                            binding.etPasswordTil.isErrorEnabled = false
                            binding.etPasswordTil.apply {
                                setStartIconDrawable(R.drawable.ic_check)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                        validatePassword()
                    }
                }

                R.id.etConfirmPassword -> {
                    if (hasFocus) {
                        if (binding.etConfirmPasswordTil.isErrorEnabled) {
                            binding.etConfirmPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword() && validatePasswordAndConfirmPassword()) {
                            binding.etConfirmPasswordTil.isErrorEnabled = false
                            binding.etConfirmPasswordTil.apply {
                                setStartIconDrawable(R.drawable.ic_check)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                        validateConfirmPassword()
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent?.action == KeyEvent.ACTION_UP) {
            onSubmit()
        }
        return false
    }


    private fun onSubmit() {
        if (validate()) {
            mViewModel.registerUser(
                RegisterBody(
                    binding.etName.text!!.toString(),
                    binding.etEmail.text!!.toString(),
                    binding.etEmail.text!!.toString(),
                    binding.etPassword.text!!.toString(),
                    "user"
                )
            )

        }
    }

    private fun validate(): Boolean {
        var isValid = true
        if (!validateEmail()) isValid = false
        if (!validateFullName()) isValid = false
        if (!validatePassword()) isValid = false
        if (!validateConfirmPassword()) isValid = false
        if (isValid && !validatePasswordAndConfirmPassword()) isValid = false

        return isValid
    }

    // TextWatcher interface methods
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (validatePassword(shouldUpdateView = false) && validateConfirmPassword(shouldUpdateView = false) && validatePasswordAndConfirmPassword(
                shouldUpdateView = false
            )
        ) {
            binding.etConfirmPasswordTil.apply {
                if (isErrorEnabled) isErrorEnabled = false
                setStartIconDrawable(R.drawable.ic_check)
                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
            }
        } else {
            if (binding.etConfirmPasswordTil.startIconDrawable != null) binding.etConfirmPasswordTil.startIconDrawable =
                null
        }
    }

    override fun afterTextChanged(s: Editable?) {}

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
}