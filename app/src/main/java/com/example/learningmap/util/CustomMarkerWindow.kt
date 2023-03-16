package com.example.learningmap.util

import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.learningmap.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomMarkerWindow(
    private val mapView: MapView,
    val place:Place,
    val onNextClick:()->Unit
): InfoWindow(R.layout.info_window, mapView) {

    lateinit var windowTitle:TextView
    lateinit var windowDescription:TextView
    lateinit var windowPlaceImage:ImageView
    lateinit var windowNextButton: CardView


    override fun onOpen(item: Any?) {

        mView.apply {
            windowTitle = findViewById(R.id.placeTitle)
            windowDescription = findViewById(R.id.placeDescription)
            windowPlaceImage = findViewById(R.id.placeImage)
            windowNextButton = findViewById(R.id.nextButton)
        }

        windowTitle.text= place.name
        windowDescription.text = place.description
        windowPlaceImage.setImageResource(place.imageResId)

        windowNextButton.setOnClickListener {
            onNextClick()
        }

        mView.setOnClickListener {
            this.close()
        }
    }

    override fun onClose() {

    }
}