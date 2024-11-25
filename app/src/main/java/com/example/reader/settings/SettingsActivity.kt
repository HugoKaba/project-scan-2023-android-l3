package com.example.reader.settings

import android.os.Bundle
import android.provider.ContactsContract.Data
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.reader.DatabaseManager
import com.example.reader.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    lateinit var binding : ActivitySettingsBinding

    val langages = mapOf(
        "Anglais" to Locale.ENGLISH,
        "Canadien" to Locale.CANADA,
        "Chinese(Simplified)" to Locale.CHINESE,
        "Canadien (français)" to Locale.CANADA_FRENCH,
        "Francais" to Locale.FRENCH,
        "Allemand" to Locale.GERMAN,
        "Italien" to Locale.ITALIAN,
        "Japonais" to Locale.JAPANESE,
        "Coréen" to Locale.KOREAN,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val manager = DatabaseManager()
        val menu = binding.langageItems
        var langagesList : Array<String> = langages.keys.toTypedArray()
        val adaptateur = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, langagesList)
        menu.setAdapter(adaptateur)

        menu.setOnItemClickListener { _, _, position, _ ->
            val langueSelectionnee = adaptateur.getItem(position)
            val codeLangue = langages[langueSelectionnee]
            manager.write("lang", codeLangue!!)
        }
    }
}
