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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private lateinit var mtimer: Timer
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private val TAG = "Vindicator"
    private val pictureCallback = Camera.PictureCallback { data, _ ->
        try {
            mCamera?.startPreview()


            val photo = scaleImage(data,0.5)
            val bytes = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 60, bytes)
            val compressedBytes = bytes.toByteArray()
            Log.d(TAG, data.size.toString())
            Log.d(TAG, compressedBytes.size.toString())
            ImageToWebService.instance.getInformationForImage(compressedBytes) {

                ProduceDataProvider().loadProduce(it).addOnSuccessListener {
                    val (produceContainerView, badgeSeasonal) = resetView()

                    if (it != null) {
                        val produce: Produce? = it.toObject(Produce::class.java)
                        if (produce != null) {
                            mtimer.cancel()
                            mtimer = Timer()
                            produceContainerView.visibility = View.VISIBLE
                            produceContainerView.setBackgroundColor(
                                getProduceBackgroundColor(
                                    produce
                                )
                            )

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
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "Error accessing file: ${e.message}")
        }
    }

    private fun scaleImage(data: ByteArray, scalingFactor: Double): Bitmap{
        val src: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
        val m = Matrix()
        val width = src.width
        val height = src.height
        Log.d(TAG, "$width $height")
        m.setScale(scalingFactor.toFloat(), scalingFactor.toFloat())
        val resizedBitmap = Bitmap.createBitmap(src, 0, 0, width, height, m, false)
        val resizedWidth = resizedBitmap.width
        val resizedHeight = resizedBitmap.height
        Log.d(TAG, "$resizedWidth $resizedHeight")
        return resizedBitmap
    }

    private fun resetView(): Pair<GridLayout, TextView> {
        val produceContainerView = findViewById<GridLayout>(R.id.produce_container)
        produceContainerView.visibility = View.GONE
        val badgeSeasonal = findViewById<TextView>(R.id.badge_seasonal)
        badgeSeasonal.visibility = View.GONE
        return Pair(produceContainerView, badgeSeasonal)
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

        mtimer = Timer()
        takePicturesInIntervalAndSearchProduce()

        val produceContainerView = findViewById<GridLayout>(R.id.produce_container)
        produceContainerView.setOnClickListener {
            takePicturesInIntervalAndSearchProduce()
            resetView()
        }
    }

    private fun takePicturesInIntervalAndSearchProduce() {
        mtimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread(Runnable {
                    mCamera?.takePicture(null, null, pictureCallback)

                })

            }
        }, 100, 2000)
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
