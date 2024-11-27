//package com.example.kaizenspeaking.ui.auth
//
//import android.os.Bundle
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
//import com.example.kaizenspeaking.databinding.FragmentSignInBinding
//import com.example.kaizenspeaking.ui.auth.data.LoginBody
//import com.example.kaizenspeaking.ui.auth.repository.AuthRepository
//import com.example.kaizenspeaking.ui.auth.utils.APIService
//import com.example.kaizenspeaking.ui.auth.view_model.SignInActivityViewModel
//import com.example.kaizenspeaking.ui.auth.view_model.SignInActivityViewModelFactory
//
//class SignInFragment : Fragment(), View.OnClickListener,View.OnFocusChangeListener,View.OnKeyListener {
//
//    private lateinit var binding : FragmentSignInBinding
//    private lateinit var mViewModel : SignInActivityViewModel
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentSignInBinding.inflate(inflater, container, false)
//        val view = binding.root
//
//        binding.btnGoogle.setOnClickListener(this)
//        binding.btnMasuk.setOnClickListener(this)
//        binding.btnDaftar.setOnClickListener(this)
//        binding.btnSignIn.setOnClickListener(this)
//        binding.etEmail.onFocusChangeListener = this
//        binding.etPassword.onFocusChangeListener = this
//        binding.etPassword.setOnKeyListener(this)
//
//        mViewModel = ViewModelProvider(this,SignInActivityViewModelFactory(AuthRepository(APIService.getService()),requireActivity().application)).get(SignInActivityViewModel::class.java)
//        setupObservers()
//
//
//
//        binding.btnDaftar.setOnClickListener {
//            findNavController().navigate(R.id.nav_to_signUpFragment)
//        }
//
////        btnGoogle.setOnClickListener {
////            // Implement Google Sign-In logic
////            Toast.makeText(context, "Google Sign-In Coming Soon", Toast.LENGTH_SHORT).show()
////        }
//
//        return view
//    }
//
//    private fun setupObservers (){
//        mViewModel.getIsLoading().observe(viewLifecycleOwner){
//            binding.progressBar.isVisible = it
//        }
//
//
//        mViewModel.getErrorMessage().observe(viewLifecycleOwner){ errorMap ->
//            //name , email ,password
//            handleLoginErrors(errorMap)
//
//
//        }
//        mViewModel.getUser().observe(this.viewLifecycleOwner){ user ->
//            user?.let{
//                showSuccessToast(it.name)
//                findNavController().navigate(R.id.nav_to_homeLogin)
//            }
//        }
//
//
//
//    }
//
//    private fun handleLoginErrors(errorMap: HashMap<String, String>?) {
//        errorMap?.let { errors ->
//            val formErrorKeys = arrayOf("email", "password")
//            val generalErrorMessage = StringBuilder()
//
//            errors.forEach { (key, value) ->
//                when (key) {
//                    "email" -> {
//                        binding.etEmailTil.apply {
//                            isErrorEnabled = true
//                            error = value
//                        }
//                    }
//                    "password" -> {
//                        binding.etPasswordTil.apply {
//                            isErrorEnabled = true
//                            error = value
//                        }
//                    }
//                    else -> generalErrorMessage.append(value).append("\n")
//                }
//            }
//
//            if (generalErrorMessage.isNotEmpty()) {
//                showGeneralErrorDialog(generalErrorMessage.toString())
//            }
//        }
//    }
//
//    private fun showGeneralErrorDialog(message: String) {
//        AlertDialog.Builder(requireContext())
//            .setIcon(R.drawable.ic_info)
//            .setTitle("LOGIN ERROR")
//            .setMessage(message)
//            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }
//
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
//    private fun validate() : Boolean {
//        var isValid = true
//        if (!validateEmail()) isValid = false
//        if (!validatePassword()) isValid = false
//
//
//        return isValid
//    }
//
//    override fun onClick(view: View?) {
//        if(view != null){
//            when(view.id){
//                R.id.btnMasuk -> {
//                    submitForm()
//                }
//                R.id.btnSignUp -> {
//                    findNavController().navigate(R.id.nav_to_signUpFragment)
//                }
//            }
//        }
//    }
//
//    override fun onFocusChange(v: View?, hasFocus: Boolean) {
//        if (view != null) {
//            when (view?.id) {
//                R.id.etEmail -> {
//                    if(hasFocus){
//                        if (binding.etEmailTil.isErrorEnabled){
//                            binding.etEmailTil.isErrorEnabled = false
//                        }
//                    }else{
//                        validateEmail()
//                    }
//
//                }
//                R.id.etPassword -> {
//                    if (hasFocus){
//                        if (binding.etPasswordTil.isErrorEnabled){
//                            binding.etPasswordTil.isErrorEnabled = false
//                        }
//                    }else {
//                        validatePassword()
//                    }
//
//                }
//            }
//        }
//    }
//    private fun showSuccessToast(userName: String) {
//        Toast.makeText(
//            requireContext(),
//            "Selamat datang, $userName!",
//            Toast.LENGTH_SHORT
//        ).show()
//    }
//
//
//    private fun submitForm(){
//        if(validate()){
//            // verify user credential
//            val email = binding.etEmail.text.toString()
//            val password = binding.etPassword.text.toString()
//            mViewModel.loginUser(LoginBody(email,password))
//        }
//    }
//
//    override fun onKey(v: View?, event: Int, keyEvent : KeyEvent?): Boolean {
//        if(event == KeyEvent.KEYCODE_ENTER && keyEvent!!.action == KeyEvent.ACTION_UP){
//            submitForm()
//        }
//        return false
//    }
//
//}