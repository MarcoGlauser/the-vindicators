package com.example.vindicator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.vindicator.services.ImageToWebService
import com.example.vindicator.services.Produce
import com.example.vindicator.services.ProduceDataProvider
import java.io.FileNotFoundException
import java.io.IOException
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

                        val textViewKm = findViewById<TextView>(R.id.textview_km)
                        textViewKm.text =
                            (produce.co2_emissions_per_kg / 400).toString() + " km per kg"
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

        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread(java.lang.Runnable {
                    mCamera?.takePicture(null, null, pictureCallback)

                })

            }
        }, 3000, 5000)
    }


    fun getCameraInstance(): Camera {
        return try {
            Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            throw RuntimeException("Camera not available", e)
        }
    }

}
