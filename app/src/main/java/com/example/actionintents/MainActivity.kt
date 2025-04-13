package com.example.actionintents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    // Request code for file chooser
    private val REQUEST_PICK_PHOTO = 1001
    // This will hold the local copy of the imported photo
    private var importedPhotoFile: File? = null

    // UI elements
    private lateinit var btnImport: Button
    private lateinit var tvPhotoName: TextView
    private lateinit var btnShare: Button
    private lateinit var ivPhoto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnImport = findViewById(R.id.btnImportPhoto)
        tvPhotoName = findViewById(R.id.tvPhotoName)
        btnShare = findViewById(R.id.btnSharePhoto)
        ivPhoto = findViewById(R.id.ivPhoto)

        // Launch the image chooser
        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_PICK_PHOTO)
        }

        // When the photo name box is clicked, show options
        tvPhotoName.setOnClickListener {
            if (importedPhotoFile != null) {
                val options = arrayOf("Open in App", "Open with External App")
                AlertDialog.Builder(this)
                    .setTitle("Choose an option")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> openPhotoInternally()
                            1 -> openPhotoExternally()
                        }
                    }.show()
            }
        }

        // Share button to share the photo
        btnShare.setOnClickListener {
            importedPhotoFile?.let {
                sharePhoto(it)
            }
        }
    }

    // Handle result from the file chooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Copy the image to internal storage
                val fileName = "importedPhoto_${System.currentTimeMillis()}.jpg"
                importedPhotoFile = File(filesDir, fileName)

                copyUriToFile(uri, importedPhotoFile!!)
                // Update the text box to show the file name
                tvPhotoName.text = fileName
            }
        }
    }

    // Helper: Copy data from the chosen URI to a destination file
    private fun copyUriToFile(uri: Uri, destFile: File) {
        contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    // Open the photo internally by displaying it in an ImageView
    private fun openPhotoInternally() {
        importedPhotoFile?.let {
            ivPhoto.setImageURI(null)
            ivPhoto.setImageURI(Uri.fromFile(it))
        }
    }


    // Use FileProvider to open the photo with another app (using the system chooser)
    private fun openPhotoExternally() {
        importedPhotoFile?.let {
            // Get a secure content URI using FileProvider
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", it)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open image with"))
        }
    }

    // Share the photo via ACTION_SEND
    private fun sharePhoto(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Photo"))
    }
}
