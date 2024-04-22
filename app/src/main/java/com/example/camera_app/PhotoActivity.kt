package com.example.camera_app

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.camera_app.databinding.PhotoActivityBinding
import java.io.File
import java.io.FileOutputStream

class PhotoActivity : AppCompatActivity() {

    private lateinit var viewBinding: PhotoActivityBinding
    private var originalImageUri: String? = null
    private lateinit var editImageUri: String
    private var editted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = PhotoActivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        originalImageUri = intent.getStringExtra("photo_uri")

        val imageUri = intent.getStringExtra("photo_uri")

        editImageUri = imageUri.toString()

        viewBinding.photoView.setImageURI(Uri.parse(imageUri))

        viewBinding.returnButton.setOnClickListener {
            finish()
        }

        viewBinding.saveButton.setOnClickListener {
            savePhoto()
        }
        
        viewBinding.greyButton.setOnClickListener {
            editPhotoGrey()
        }
        
        viewBinding.negativeButton.setOnClickListener {
            editPhotoNegative()
        }

        viewBinding.sepiaButton.setOnClickListener {
            editPhotoSepia()
        }
        
        viewBinding.sobelButton.setOnClickListener {
            editPhotoSobel()
        }

        viewBinding.UndoButton.setOnClickListener {
            undoEdit()
        }
    }

    private fun undoEdit() {
        viewBinding.photoView.setImageURI(Uri.parse(originalImageUri))
        editted = false
    }

    private fun editPhotoSobel() {
        val uri = Uri.parse(originalImageUri)
        val originalBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        val exif = uri.let { contentResolver.openInputStream(it) }?.use { inputStream ->
            ExifInterface(inputStream)
        }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val orientedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        val width = orientedBitmap.width
        val height = orientedBitmap.height

        val sobelBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val sobelX = arrayOf(-1, 0, 1, -2, 0, 2, -1, 0, 1)
        val sobelY = arrayOf(-1, -2, -1, 0, 0, 0, 1, 2, 1)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                var pixelX = 0
                var pixelY = 0
                for (i in -1..1) {
                    for (j in -1..1) {
                        val color = orientedBitmap.getPixel(x + i, y + j)
                        val grayscale = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3
                        pixelX += grayscale * sobelX[(i + 1) * 3 + j + 1]
                        pixelY += grayscale * sobelY[(i + 1) * 3 + j + 1]
                    }
                }
                val magnitude = Math.sqrt((pixelX * pixelX + pixelY * pixelY).toDouble()).toInt().coerceIn(0, 255)
                sobelBitmap.setPixel(x, y, Color.rgb(magnitude, magnitude, magnitude))
            }
        }

        val file = File(externalCacheDir, "temp.jpg")
        val out = FileOutputStream(file)
        sobelBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        editImageUri = Uri.fromFile(file).toString()
        editted = true

        viewBinding.photoView.setImageBitmap(sobelBitmap)
    }

    private fun editPhotoSepia() {
        val uri = Uri.parse(originalImageUri)
        val originalBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        val exif = uri.let { contentResolver.openInputStream(it) }?.use { inputStream ->
            ExifInterface(inputStream)
        }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val orientedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        val sepiaBitmap = Bitmap.createBitmap(orientedBitmap.width, orientedBitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(sepiaBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix()
        colorMatrix.set(
            floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixColorFilter
        canvas.drawBitmap(orientedBitmap, 0f, 0f, paint)

        val file = File(externalCacheDir, "temp.jpg")
        val out = FileOutputStream(file)
        sepiaBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        editImageUri = Uri.fromFile(file).toString()
        editted = true

        viewBinding.photoView.setImageBitmap(sepiaBitmap)
    }

    private fun editPhotoNegative() {
        val uri = Uri.parse(originalImageUri)
        val originalBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        val exif = uri.let { contentResolver.openInputStream(it) }?.use { inputStream ->
            ExifInterface(inputStream)
        }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val orientedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        val negativeBitmap = Bitmap.createBitmap(orientedBitmap.width, orientedBitmap.height, Bitmap.Config.ARGB_8888)

        for (y in 0 until orientedBitmap.height) {
            for (x in 0 until orientedBitmap.width) {
                val color = orientedBitmap.getPixel(x, y)
                val red = 255 - Color.red(color)
                val green = 255 - Color.green(color)
                val blue = 255 - Color.blue(color)
                negativeBitmap.setPixel(x, y, Color.rgb(red, green, blue))
            }
        }

        val file = File(externalCacheDir, "temp.jpg")
        val out = FileOutputStream(file)
        negativeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        editImageUri = Uri.fromFile(file).toString()
        editted = true

        viewBinding.photoView.setImageBitmap(negativeBitmap)
    }

    private fun editPhotoGrey() {
        val uri = Uri.parse(originalImageUri)
        val originalBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

        val exif = uri.let { contentResolver.openInputStream(it) }?.use { inputStream ->
            ExifInterface(inputStream)
        }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val orientedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

        val grayScaleBitmap = Bitmap.createBitmap(orientedBitmap.width, orientedBitmap.height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(grayScaleBitmap)
        val paint = Paint()
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f)
        }
        val colorMatrixColorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorMatrixColorFilter
        canvas.drawBitmap(orientedBitmap, 0f, 0f, paint)

        val file = File(externalCacheDir, "temp.jpg")
        val out = FileOutputStream(file)
        grayScaleBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()

        editImageUri = Uri.fromFile(file).toString()
        editted = true

        viewBinding.photoView.setImageBitmap(grayScaleBitmap)
    }

    private fun savePhoto() {
        val imageUri = if (editted) editImageUri else originalImageUri
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, imageUri)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/")
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            uri?.let {
                val outputStream = resolver.openOutputStream(it)
                val inputStream = imageUri?.let { uri -> resolver.openInputStream(Uri.parse(uri)) }
                if (outputStream != null) {
                    inputStream?.copyTo(outputStream)
                }
                inputStream?.close()
                outputStream?.close()
            }
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}