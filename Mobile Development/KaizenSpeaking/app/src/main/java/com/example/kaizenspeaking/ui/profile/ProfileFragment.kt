package com.example.kaizenspeaking.ui.profile

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.example.kaizenspeaking.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sembunyikan BottomNavigationView
        hideBottomNavigation()

        // Tambahkan fungsionalitas tombol kembali
        val backButton = view.findViewById<ImageView>(R.id.back_btn)
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Tambahkan klik listener untuk tvAbout
        val tvAbout = view.findViewById<TextView>(R.id.tvAbout)
        tvAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val description = getString(R.string.about_kaizen_speaking) // Mengambil teks dari resource string
        builder.setMessage(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() } // Tombol OK
            .setCancelable(true) // Membuat dialog dapat ditutup dengan klik di luar
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Tampilkan kembali BottomNavigationView saat fragment ini dihancurkan
        showBottomNavigation()
    }

    private fun hideBottomNavigation() {
        // Ambil referensi BottomNavigationView dari Activity utama
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.GONE
    }

    private fun showBottomNavigation() {
        // Tampilkan kembali BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.nav_view)
        bottomNav?.visibility = View.VISIBLE
    }
}
