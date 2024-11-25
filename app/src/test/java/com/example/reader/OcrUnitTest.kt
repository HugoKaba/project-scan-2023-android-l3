package com.example.reader

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.reader.features.result_details.OcrState
import com.example.reader.features.result_details.OcrViewModel
import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OcrUnitTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: OcrViewModel

    @Before
    fun setup() {
        viewModel = OcrViewModel()
    }

    @Test
    fun `test API OCR`() {
        val stateLiveData = MutableLiveData<OcrState>()

        var image = "/Users/hugokaba/Desktop/Coding/Android/Kotlin/android-project-scan-2023-android-l3-paris-groupe-3/app/src/main/res/drawable/test.jpeg"

        viewModel.recognizeImage(image)

        viewModel.ocrStateLiveData.observeForever { state ->
            when (state) {
                is OcrState.Success -> {
                    val jsonObject = JsonParser.parseString(state.response).asJsonObject
                    val processingTime =
                        jsonObject.getAsJsonPrimitive("ProcessingTimeInMilliseconds")?.asString
                    val processing =
                        """{"ParsedResults":[{"TextOverlay":{"Lines":[{"LineText":"La vie est trop","Words":[{"WordText":"La vie est trop","Left":28.0,"Top":17.0,"Height":175.0,"Width":34.0}],"MaxHeight":175.0,"MinTop":17.0},{"LineText":"courte pour être","Words":[{"WordText":"courte pour être","Left":63.0,"Top":8.0,"Height":195.0,"Width":33.0}],"MaxHeight":195.0,"MinTop":8.0},{"LineText":"autre chose","Words":[{"WordText":"autre chose","Left":98.0,"Top":36.0,"Height":136.0,"Width":26.0}],"MaxHeight":136.0,"MinTop":36.0},{"LineText":"qu'heureux","Words":[{"WordText":"qu'heureux","Left":129.0,"Top":44.0,"Height":118.0,"Width":30.0}],"MaxHeight":118.0,"MinTop":44.0}],"HasOverlay":true},"TextOrientation":"0","FileParseExitCode":1,"ParsedText":"La vie est trop\ncourte pour être\nautre chose\nqu'heureux","ErrorMessage":"","ErrorDetails":""}],"OCRExitCode":1,"IsErroredOnProcessing":false,"ProcessingTimeInMilliseconds":"$processingTime","SearchablePDFURL":"Searchable PDF not generated as it was not requested."}"""

                    Assert.assertEquals(
                        state.response, processing
                    )
                }
                is OcrState.Failure -> {

                }
                is OcrState.Loading -> {

                }
            }
        }

        println(viewModel.ocrStateLiveData.value)
    }

    @Test
    fun testFormatResult() {
        val fakeOcrResponse = """{"ParsedResults": [{"ParsedText": "курс/ Б\nkurs B"}]}"""

        val result = viewModel.formatResult(fakeOcrResponse)

        Assert.assertEquals(
            "курс/ Б\nkurs B", result.result
        )
        println("${result.result}")
    }

    @Test
    fun `test API OCR and Format Result`() {
        val stateLiveData = MutableLiveData<OcrState>()
        var image = "/Users/hugokaba/Desktop/Coding/Android/Kotlin/android-project-scan-2023-android-l3-paris-groupe-3/app/src/main/res/drawable/test.jpeg"
        viewModel.recognizeImage(image)
        viewModel.ocrStateLiveData.observeForever { state ->
            when (state) {
                is OcrState.Success -> {
                    val jsonObject = JsonParser.parseString(state.response).asJsonObject
                    val processingTime = jsonObject.getAsJsonPrimitive("ProcessingTimeInMilliseconds")?.asString
                    val processing = """{"ParsedResults":[{"TextOverlay":{"Lines":[{"LineText":"La vie est trop","Words":[{"WordText":"La vie est trop","Left":28.0,"Top":17.0,"Height":175.0,"Width":34.0}],"MaxHeight":175.0,"MinTop":17.0},{"LineText":"courte pour être","Words":[{"WordText":"courte pour être","Left":63.0,"Top":8.0,"Height":195.0,"Width":33.0}],"MaxHeight":195.0,"MinTop":8.0},{"LineText":"autre chose","Words":[{"WordText":"autre chose","Left":98.0,"Top":36.0,"Height":136.0,"Width":26.0}],"MaxHeight":136.0,"MinTop":36.0},{"LineText":"qu'heureux","Words":[{"WordText":"qu'heureux","Left":129.0,"Top":44.0,"Height":118.0,"Width":30.0}],"MaxHeight":118.0,"MinTop":44.0}],"HasOverlay":true},"TextOrientation":"0","FileParseExitCode":1,"ParsedText":"La vie est trop\ncourte pour être\nautre chose\nqu'heureux","ErrorMessage":"","ErrorDetails":""}],"OCRExitCode":1,"IsErroredOnProcessing":false,"ProcessingTimeInMilliseconds":"$processingTime","SearchablePDFURL":"Searchable PDF not generated as it was not requested."}"""
                    Assert.assertEquals(state.response, processing)
                    val fakeOcrResponse = """{"ParsedResults": [{"ParsedText": "курс/ Б\nkurs B"}]}"""
                    val result = viewModel.formatResult(state.response)
                    Assert.assertEquals("La vie est trop\n" +
                            "courte pour être\n" +
                            "autre chose\n" +
                            "qu'heureux", result.result)
                    println("${result.result}")
                }
                is OcrState.Failure -> {
                }
                is OcrState.Loading -> {
                }
            }
        }
        println(viewModel.ocrStateLiveData.value)
    }

}

