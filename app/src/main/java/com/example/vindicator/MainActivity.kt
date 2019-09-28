package com.example.vindicator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.vindicator.services.ImageToWebService
import com.example.vindicator.services.Produce
import com.example.vindicator.services.ProduceDataProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private val TAG = "Vindicator"
    private val pictureCallback = Camera.PictureCallback { data, _ ->
        try {
            mCamera?.startPreview()

            val informationForImage = ImageToWebService.instance.getInformationForImage(data)

            ProduceDataProvider().loadProduce(informationForImage).addOnSuccessListener {
                val produceContainerView = findViewById<GridLayout>(R.id.produce_container)
                produceContainerView.visibility = View.GONE
                val badgeSeasonal = findViewById<TextView>(R.id.badge_seasonal)
                badgeSeasonal.visibility = View.GONE

                if (it != null) {
                    val produce: Produce? = it.toObject(Produce::class.java)
                    if (produce != null) {
                        produceContainerView.visibility = View.VISIBLE
                        produceContainerView.setBackgroundColor(getProduceBackgroundColor(produce))

                        val produceNameView = findViewById<TextView>(R.id.produce_name)
                        produceNameView.text = produce.name_de

                        if (produce.in_season) {
                            badgeSeasonal.visibility = View.VISIBLE
                        }

                        val iconOriginView = findViewById<TextView>(R.id.icon_origin)
                        iconOriginView.text = getProduceTransportIcon(produce)
                    }
                } else {
                    throw java.lang.RuntimeException("could no deserialize produce ${it?.data}")
                }
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: ${e.message}")
        }
    }

    private fun getProduceBackgroundColor(produce: Produce): Int {
        val isInSeasonOrLocal: Boolean = produce.in_season || produce.country.equals("Schweiz")
        val isNotInSeasonOrEuropean: Boolean =
            !produce.in_season && produce.continent.equals("Europe")
        return when {
            isInSeasonOrLocal -> Color.parseColor("#43a047")
            isNotInSeasonOrEuropean -> Color.parseColor("#ffa000")
            else -> Color.parseColor("#d73a49")
        }
    }

    private fun getProduceTransportIcon(produce: Produce): String {
        val bla = when {
            produce.transport_mode.equals("plane") -> R.string.fa_plane_solid
            produce.transport_mode.equals("ship") -> R.string.fa_ship_solid
            produce.transport_mode.equals("truck") -> R.string.fa_truck_solid
            else -> R.string.fa_tractor_solid
        }

        return resources.getString(bla)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.main_activity)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                4343
            )
        }

        // Create an instance of Camera
        mCamera = getCameraInstance()

        mCamera!!.setDisplayOrientation(90)

        mPreview = mCamera?.let {
            // Create our Preview view
            CameraPreview(this, it)
        }

        // Set the Preview view as the content of our activity.
        mPreview?.also {
            val preview: FrameLayout = findViewById(R.id.camera_preview)
            preview.addView(it)
        }
        val captureButton: Button = findViewById(R.id.button_capture)

        captureButton.setOnClickListener {
            // get an image from the camera
            mCamera?.takePicture(null, null, pictureCallback)

        }
    }


    fun getCameraInstance(): Camera {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            throw RuntimeException("Camera not available", e)
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MyCameraApp"
        )
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory")
                    return null
                }
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyM-Mdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timeStamp.jpg")
            }
            MEDIA_TYPE_VIDEO -> {
                File("${mediaStorageDir.path}${File.separator}VID_$timeStamp.mp4")
            }
            else -> null
        }
    }
}
