//package com.example.kaizenspeaking.ui.auth
//
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.content.res.ColorStateList
//import android.graphics.Color
//import android.media.audiofx.BassBoost
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.util.Patterns
//import android.view.KeyEvent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.core.view.isVisible
//import androidx.fragment.app.Fragment
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.fragment.findNavController
//import com.example.kaizenspeaking.R
//import com.example.kaizenspeaking.databinding.FragmentSignUpBinding
//import com.example.kaizenspeaking.ui.auth.data.RegisterBody
//import com.example.kaizenspeaking.ui.auth.data.User
//import com.example.kaizenspeaking.ui.auth.data.ValidateEmailBody
//import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
//import com.example.kaizenspeaking.ui.auth.utils.APIService
//import com.example.kaizenspeaking.ui.auth.utils.RequesStatus
//import com.example.kaizenspeaking.ui.auth.view_model.SignUpFragmentViewModel
//import com.example.kaizenspeaking.ui.auth.view_model.SignUpFragmentViewModelFactory
//import com.example.kaizenspeaking.ui.home.HomeFragment
//import android.provider.Settings
//
//class SignUpFragment : Fragment(), View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener,TextWatcher  {
//    private var _binding: FragmentSignUpBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var mViewModel : SignUpFragmentViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupListeners()
//        val factory = SignUpFragmentViewModelFactory(AuthRepository(APIService.getService()),requireActivity().application)
//        mViewModel = ViewModelProvider(this, factory).get(SignUpFragmentViewModel::class.java)
//        setupObservers()
//    }
//
//    private fun setupListeners() {
//        // Set click listeners
//        binding.btnSignUp.setOnClickListener(this)
//        binding.btnSignUpGoogle.setOnClickListener(this)
//        binding.btnMasuk.setOnClickListener(this)
//        binding.btnBack.setOnClickListener(this)
//
//        // Set focus listeners
//        binding.etName.onFocusChangeListener = this
//        binding.etEmail.onFocusChangeListener = this
//        binding.etPassword.onFocusChangeListener = this
//        binding.etConfirmPassword.onFocusChangeListener = this
//
//        binding.etPassword.setOnKeyListener(this)
//        binding.etConfirmPassword.setOnKeyListener(this)
//        binding.btnSignUp.setOnClickListener(this)
//
//    }
//
//    override fun onClick(view: View?) {
//        if(view != null && view.id == R.id.btnSignUp)
//            onSubmit()
//        when (view?.id) {
//            R.id.button -> validateAndRegister()
//            R.id.button2 -> handleGoogleSignUp()
//            R.id.btnMasuk -> navigateToSignIn()
//            R.id.btn_back -> navigateToHome()
//        }
//    }
//
//    private fun validateAndRegister() {
//        val isValidName = validateFullName()
//        val isValidEmail = validateEmail()
//        val isValidPassword = validatePassword()
//        val isValidConfirmPassword = validateConfirmPassword()
//        val isPasswordMatch = validatePasswordAndConfirmPassword()
//
//        if (isValidName && isValidEmail && isValidPassword &&
//            isValidConfirmPassword && isPasswordMatch) {
//            Toast.makeText(context, "Registration validation successful", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun handleGoogleSignUp() {
//        Toast.makeText(context, "Google Sign-Up Coming Soon", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun navigateToSignIn() {
//        findNavController().navigate(R.id.nav_to_signInFragment)
//    }
//
//    private fun navigateToHome(){
//        findNavController().navigate(R.id.navigation_home)
//    }
//
//    private fun setupObservers (){
//        mViewModel.getIsLoading().observe(viewLifecycleOwner){
//            binding.progressBar.isVisible = it
//        }
//
//
//        mViewModel.getIsUniqueEmail().observe(viewLifecycleOwner){
//            if(it){
//                binding.etEmailTil.apply {
//                   if(isErrorEnabled) isErrorEnabled = false
//                   setStartIconDrawable(R.drawable.ic_info)
//                    setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
//
//                }
//            }
//            else{
//                binding.etEmailTil.apply {
//                    if(startIconDrawable != null) startIconDrawable = null
//                    isErrorEnabled = true
//                    error = "Email sudah digunakan"
//                }
//            }
//        }
//        mViewModel.getErrorMessage().observe(viewLifecycleOwner){
//            //name , email ,password
//            val formErrorKeys = arrayOf("name","email","password")
//            val message = StringBuilder()
//            it.map{ entry ->
//                if(formErrorKeys.contains(entry.key)){
//                    when (entry.key){
//                        "name" -> {
//                            binding.etNameTil.apply {
//                                isErrorEnabled = true
//                                error = entry.value
//                            }
//                        }
//                        "email" -> {
//                            binding.etEmailTil.apply {
//                                isErrorEnabled = true
//                                error = entry.value
//                            }
//                        }
//                        "password" ->{
//                            binding.etPasswordTil.apply {
//                                isErrorEnabled = true
//                                error = entry.value
//                            }
//
//                        }
//                    }
//                }else{
//                    message.append(entry.value).append("\n")
//                }
//
//                if(message.isNotEmpty()){
//                    AlertDialog.Builder(requireContext())
//                        .setIcon(R.drawable.ic_info)
//                        .setTitle("INFORMATION")
//                        .setMessage(message)
//                        .setPositiveButton("OK"){dialog,_->dialog!!.dismiss() }
//                        .show()
//
//                }
//            }
//
//        }
//        mViewModel.getUser().observe(this.viewLifecycleOwner){ user ->
//            if (user != null){
//                findNavController().navigate(R.id.navigation_home)
//            }
//        }
//
//
//
//    }
//
//    private fun validateFullName(): Boolean {
//        var errorMessage: String? = null
//        val value = binding.etName.text.toString()
//
//        if (value.isEmpty()) {
//            errorMessage = "Nama Harus Diisi"
//        }
//
//        binding.etNameTil.apply {
//            isErrorEnabled = errorMessage != null
//            error = errorMessage
//        }
//
//        return errorMessage == null
//    }
//
//    private fun validateEmail(shouldUpdateView :Boolean = true): Boolean {
//        var errorMessage: String? = null
//        val value = binding.etEmail.text.toString()
//
//        if (value.isEmpty()) {
//            errorMessage = "Email Harus Diisi"
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
//            errorMessage = "Email tidak valid"
//        }
//
//        binding.etEmailTil.apply {
//            isErrorEnabled = errorMessage != null
//            error = errorMessage
//        }
//
//        return errorMessage == null
//    }
//
//    private fun validatePassword(shouldUpdateView :Boolean = true): Boolean {
//        var errorMessage: String? = null
//        val value = binding.etPassword.text.toString()
//
//        if (value.isEmpty()) {
//            errorMessage = "Password Harus Diisi"
//        } else if (value.length < 8) {
//            errorMessage = "Password Harus Lebih dari 8 Karakter"
//        }
//
//        if(errorMessage != null && shouldUpdateView){
//            binding.etPasswordTil.apply {
//                isErrorEnabled = true
//                error = errorMessage
//            }
//        }
//
//
//        return errorMessage == null
//    }
//
//    private fun validateConfirmPassword(shouldUpdateView :Boolean = true): Boolean {
//        var errorMessage: String? = null
//        val confirmPassword = binding.etConfirmPassword.text.toString()
//        val password = binding.etPassword.text.toString()
//        if (password != confirmPassword){
//            errorMessage ="Password Tidak Sama"
//        }
//        if (errorMessage != null && shouldUpdateView){
//            binding.etConfirmPasswordTil.apply {
//                isErrorEnabled = true
//                error = errorMessage
//            }
//        }
//        if (confirmPassword.isEmpty()) {
//            errorMessage = "Confirm password Harus Diisi"
//        } else if (confirmPassword.length < 8) {
//            errorMessage = "Password Harus Lebih dari 8 Karakter"
//        }
//
//        return errorMessage == null
//    }
//
//    private fun validatePasswordAndConfirmPassword(shouldUpdateView :Boolean = true): Boolean {
//        var errorMessage: String? = null
//        val password = binding.etPassword.text.toString()
//        val confirmPassword = binding.etConfirmPassword.text.toString()
//
//        if (password != confirmPassword) {
//            errorMessage = "Password tidak sama"
//        }
//        if (errorMessage != null && shouldUpdateView){
//            binding.etConfirmPasswordTil.apply {
//                isErrorEnabled = true
//                error = errorMessage
//            }
//        }
//
//        return errorMessage == null
//    }
//
//    override fun onFocusChange(view: View?, hasFocus: Boolean) {
//        if (view !=null) {
//            when (view.id) {
//                R.id.etName -> {
//                    if (hasFocus){
//                        if (binding.etNameTil.isErrorEnabled){
//                            binding.etNameTil.isErrorEnabled = false
//                        }
//                    } else{
//                        validateFullName()
//                    }
//                }
////                R.id.etEmail -> mViewModel.validateEmailAddres(ValidateEmailBody(binding.etEmail.text!!.toString()))
//                R.id.etPassword -> {
//                    if (hasFocus){
//                        if (binding.etPasswordTil.isErrorEnabled){
//                            binding.etPasswordTil.isErrorEnabled = false
//                        }
//                    } else{
//                        if(validateConfirmPassword()&& validatePassword()&& validatePasswordAndConfirmPassword()){
//                            if (binding.etPasswordTil.isErrorEnabled){
//                                binding.etPasswordTil.isErrorEnabled = false
//                            }
//                            binding.etConfirmPasswordTil.apply {
//                                setStartIconDrawable(R.drawable.ic_check)
//                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
//                            }
//                        }
//                        validatePassword()
//                    }
//                }
//                R.id.etConfirmPassword -> {
//                    if (hasFocus){
//                        if (binding.etConfirmPasswordTil.isErrorEnabled){
//                            binding.etConfirmPasswordTil.isErrorEnabled = false
//                        }
//                    } else{
//                        validateConfirmPassword()
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onKey(v: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
//        if (KeyEvent.KEYCODE_ENTER == keyCode && keyEvent!!.action == KeyEvent.ACTION_UP){
//            onSubmit()
//        }
//            // do registration
//
//        return false
//        }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//
//    }
//
//    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//        if (validatePassword(shouldUpdateView = false)&& validateConfirmPassword(shouldUpdateView = false)&&validatePasswordAndConfirmPassword(shouldUpdateView = false)){
//            binding.etConfirmPasswordTil.apply {
//                if(isErrorEnabled) isErrorEnabled = false
//                setStartIconDrawable(R.drawable.ic_check)
//                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
//            }
//        }
//        else{
//            if (binding.etConfirmPasswordTil.startIconDrawable != null)
//                binding.etConfirmPasswordTil.startIconDrawable = null
//
//        }
//    }
//
//    override fun afterTextChanged(s: Editable?) {
//
//    }
//
//    @SuppressLint("HardwareIds")
//    private fun onSubmit(){
//        if(validate()){
//            val email = binding.etEmail.text.toString()
//            val name = binding.etName.text.toString()
//            val password = binding.etPassword.text.toString()
//
//            // Get the device ID using Settings.Secure.ANDROID_ID
//            val deviceId = Settings.Secure.getString(
//                requireContext().contentResolver,
//                Settings.Secure.ANDROID_ID
//            )
//
//            // Create RegisterBody with the specified parameters
//            val registerBody = RegisterBody(
//                username = email,           // username from email
//                email = email,              // email from email field
//                password = password,        // password from password field
//                full_name = name,           // full_name from name field
//                nickname = email,           // nickname  email
//                role = "user",              // default role
//                device_id = deviceId        // device ID
//            )
//
//            mViewModel.registerUser(registerBody)
//        }
//    }
//
//    private fun validate() : Boolean {
//        var isValid = true
//        if (!validateEmail()) isValid = false
//        if (!validateFullName()) isValid = false
//        if (!validateEmail()) isValid = false
//        if (!validatePassword()) isValid = false
//        if (!validateConfirmPassword()) isValid = false
//        if (isValid && !validatePasswordAndConfirmPassword()) isValid = false
//
//
//        return isValid
//    }
//}