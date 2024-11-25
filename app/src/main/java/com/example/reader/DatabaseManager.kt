package com.example.reader

import com.example.reader.features.result_details.Document
import io.paperdb.Paper
import java.util.Locale

class DatabaseManager {

    fun write(key : String, value : Any){
        Paper.book().write(key,value)
    }
    fun readDocument():  MutableList<Document>{
        return Paper.book().read<MutableList<Document>>("Docs", mutableListOf<Document>())!!
    }

    fun readLang():  Locale{
        return Paper.book().read<Locale>("lang", Locale.FRANCE)!!
    }
}