package com.example.reader.features.result_details

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.reader.DatabaseManager
import com.example.reader.R
import com.example.reader.databinding.ActivityResultDetailBinding
import com.example.reader.homepage.MainActivity
import com.google.android.material.button.MaterialButton


class ResultDetailActivity<Intent> : AppCompatActivity(),TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityResultDetailBinding
    private var textSpeaker : TextToSpeech? = null
    private var audioManager: AudioManager? = null

    private  val viewModel: OcrViewModel by viewModels()
    val ocrStateLiveData = MutableLiveData<OcrState>()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?;
        binding.playIconButton.setOnClickListener {
            talk()
        }
        textSpeaker = TextToSpeech(this, this)
        val manager = DatabaseManager()
        var isRecyclerView = intent.getBooleanExtra("isRecyclerView",false)
        if(!isRecyclerView){
            val image = intent?.getStringExtra("image")
            viewModel.recognizeImage(image!!)
            viewModel.ocrStateLiveData.observeForever { state ->
                when (state) {
                    is OcrState.Success -> {
                        val result = viewModel.formatResult(state.response)
                        binding.resultTextView.visibility = View.VISIBLE
                        binding.loadingSpinner.visibility = View.GONE
                        binding.resultTextView.text = result.result
                        val titre: EditText = binding.titreEditText
                        binding.continueIconButton.setOnClickListener {
                            viewModel.save(binding.titreEditText.text.toString(),binding.resultTextView.text.toString())
                            val intent = android.content.Intent(this,MainActivity::class.java)
                            startActivity(intent)
                        }

                    }
                    is OcrState.Failure -> {
                        binding.resultTextView.text = viewModel.ocrStateLiveData.value.toString()
                    }
                    is OcrState.Loading -> {
                        binding.resultTextView.visibility = View.GONE
                        binding.loadingSpinner.visibility = View.VISIBLE
                    }
                }
            }

            binding.deleteIconButton.setOnClickListener {
                val intent = android.content.Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }else{
            val id = intent.getStringExtra("id")
            val items = manager.readDocument()
            val item = items.firstOrNull {it.id == id}
            binding.loadingSpinner.visibility = View.GONE
            binding.resultTextView.visibility = View.VISIBLE
            binding.resultTextView.setText(item?.content)
            binding.titreEditText.setText(item?.title)

            binding.continueIconButton.setOnClickListener {
                var index = items.indexOf(item)
                item?.title = binding.titreEditText.text.toString()
                items.set(index,item!!)
                manager.write("Docs", items)
                val intent = android.content.Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
            binding.deleteIconButton.setOnClickListener {
                items.remove(item)
                manager.write("Docs", items)
                val intent = android.content.Intent(this,MainActivity::class.java)
                startActivity(intent)
            }

        }

    }

    override fun onInit(status: Int) {
        val manager = DatabaseManager()
        if (status == TextToSpeech.SUCCESS) {
            val lang = manager.readLang()
            textSpeaker!!.setLanguage(lang)
            textSpeaker!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        val playButton : MaterialButton = binding.playIconButton as MaterialButton
                        playButton.setIconResource(R.drawable.play)
                    }
                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {}
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun talk(){
        if(textSpeaker!!.isSpeaking){
            textSpeaker!!.stop()
            val playButton : MaterialButton = binding.playIconButton as MaterialButton
            playButton.setIconResource(R.drawable.play)
        }else{
            val focusRequest : AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).build()
            audioManager!!.requestAudioFocus(focusRequest)

            Handler(mainLooper).postDelayed({
                val text = binding.resultTextView.text.toString()
                textSpeaker!!.speak(text, TextToSpeech.QUEUE_FLUSH,null,TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED)
                val playButton : MaterialButton = binding.playIconButton as MaterialButton
                playButton.setIconResource(R.drawable.stop)
            }, 1000)
        }
    }

    public override fun onDestroy() {
        // Shutdown TTS when
        // activity is destroyed
        if (textSpeaker != null) {
            textSpeaker!!.stop()
            textSpeaker!!.shutdown()
        }
        super.onDestroy()
    }
}