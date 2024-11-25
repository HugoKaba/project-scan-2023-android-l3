package com.example.reader.homepage

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.reader.DatabaseManager
import com.example.reader.databinding.ActivityMainBinding
import com.example.reader.features.result_details.ResultDetailActivity
import com.example.reader.settings.SettingsActivity
import io.paperdb.Paper
import java.io.File


class MainActivity :  AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var file : File
    private  val viewModel: HomePageViewModel by viewModels()
    private lateinit var adapter : ListAdapter

    private fun DispatchCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //val file = createImageFile()

        file = File(filesDir,"test.jpg")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,"com.example.reader.fileprovider",file))
        startActivityForResult(intent, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        Paper.init(this)
        val manager = DatabaseManager()
        val items = manager.readDocument()
        adapter = ListAdapter(items, this)
        binding.mainRecyclerView.adapter = adapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cameraFloatingActionButton.setOnClickListener{
            DispatchCamera()
        }

        binding.settingsIconButton.setOnClickListener {
            var intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Log.i("image", "onActivityResult: ${file.totalSpace} ")
            val compressionSize = 1024
            val compressedImage = viewModel.compressImage(file.absolutePath, compressionSize )
            file.writeBytes(compressedImage)
            Log.i("size", "onActivityResult: ${file.length()} ")
            var intent = Intent(this, ResultDetailActivity::class.java)
            intent.putExtra("image",file.absolutePath)
            startActivity(intent)
        }
    }


}