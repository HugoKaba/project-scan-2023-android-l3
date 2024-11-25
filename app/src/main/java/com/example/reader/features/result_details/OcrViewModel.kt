package com.example.reader.features.result_details

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.reader.DatabaseManager
import com.google.gson.JsonParser
import io.paperdb.Paper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.security.Timestamp
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

data class JsonResponse(val success: Boolean, val result: String?, val error: String?)

sealed class OcrState {
    data class Success(val response: String) : OcrState()
    data class Failure(val error: String) : OcrState()
    object Loading : OcrState()
}

data class Document(val id: String, var title : String, val content : String, val timestamp : String)

class OcrViewModel: ViewModel() {
    val ocrStateLiveData = MutableLiveData<OcrState>()

    private val apiKey: String = "K87519047888957"
    private val apiUrl: String = "https://api.ocr.space/parse/image"

    fun recognizeImage(imagePath: String){
        ocrStateLiveData.value = OcrState.Loading

        val file = File(imagePath)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaType()))
            .addFormDataPart("apikey", apiKey)
            .addFormDataPart("detectOrientation", "true")
            .addFormDataPart("scale", "true")
            .addFormDataPart("OCREngine", "2")
            .addFormDataPart("language", "eng")
            .build()

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build()


        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                ocrStateLiveData.postValue(
                    OcrState.Failure("OCR API request failed with code: ")
                )
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val result = response.body?.string()
                    val jsonObject = JsonParser.parseString(result).asJsonObject
                    val ocrExitCode = jsonObject.getAsJsonPrimitive("OCRExitCode")

                    if (ocrExitCode?.isNumber == true && ocrExitCode.asInt == 1) {
                        ocrStateLiveData.postValue( result?.let { OcrState.Success(it) })
                    } else {
                        ocrStateLiveData.postValue(
                            OcrState.Failure("OCR API request failed with code: $result")
                        )
                    }
                } else {
                    ocrStateLiveData.postValue(
                        OcrState.Failure("OCR API request failed with code: ${response.code}")
                    )
                }
            }
        })
    }

    fun formatResult(result: String): JsonResponse {
        return try {
            val jsonObject = JsonParser.parseString(result).asJsonObject
            val parsedResults = jsonObject.getAsJsonArray("ParsedResults")
            val firstResult = parsedResults?.firstOrNull()?.asJsonObject
            val parsedText = firstResult?.getAsJsonPrimitive("ParsedText")?.asString
            JsonResponse(success = true, result = parsedText, error = null)
        } catch (e: Exception) {
            e.printStackTrace()
            JsonResponse(success = false, result = null, error = e.message)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(titre : String, content : String){
        val myUuid = UUID.randomUUID()
        val myUuidAsString = myUuid.toString()
        val doc = Document(myUuidAsString, titre,content, DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now()))

        val manager = DatabaseManager()
        var docs : MutableList<Document> = manager.readDocument()
        docs.add(doc)
        manager.write("Docs", docs)
    }
}
