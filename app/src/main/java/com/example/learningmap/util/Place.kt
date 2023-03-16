package com.example.learningmap.util

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.learningmap.R
import org.osmdroid.util.GeoPoint

data class Place (
    val id:String,
    val name:String,
    val coordinates: GeoPoint,
    val description:String,
    val imageResId: Int,
    val focusZoomLvl:Double
)

