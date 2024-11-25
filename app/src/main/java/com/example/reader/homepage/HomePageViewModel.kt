package com.example.reader.homepage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import java.io.ByteArrayOutputStream

class HomePageViewModel : ViewModel(){
    fun compressImage(imagePath: String, maxSizeInBytes: Int): ByteArray {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)

        // Calculer le facteur d'échelle pour ajuster la taille de l'image
        options.inSampleSize = calculateInSampleSize(options, maxSizeInBytes)

        // Décoder l'image avec le facteur d'échelle calculé
        options.inJustDecodeBounds = false
        val compressedBitmap = BitmapFactory.decodeFile(imagePath, options)

        // Convertir le bitmap en tableau d'octets compressé
        val outputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        return outputStream.toByteArray()
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, maxSizeInBytes: Int): Int {
        val originalSize = options.outWidth * options.outHeight
        var inSampleSize = 1

        while ((originalSize / (inSampleSize * inSampleSize * 4)) > maxSizeInBytes) {
            inSampleSize *= 2
        }

        return inSampleSize
    }
}