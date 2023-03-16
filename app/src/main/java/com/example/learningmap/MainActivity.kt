package com.example.learningmap

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.example.learningmap.ui.theme.LearningMapTheme
import com.example.learningmap.util.CustomMarkerWindow
import com.example.learningmap.util.Place
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

lateinit var permissionContract: ActivityResultLauncher<Array<String>>
lateinit var homeScreenMapView:MapView

class MainActivity : ComponentActivity() {

    override fun onResume() {
        super.onResume()
        homeScreenMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        homeScreenMapView.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionContract = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            Toast.makeText(this, "Permission Asked ", Toast.LENGTH_SHORT).show()
        }

        Configuration.getInstance().userAgentValue = packageName
        homeScreenMapView = MapView(baseContext)

        setContent {
            LearningMapTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    FinalLearningApp(
                        mapView= homeScreenMapView,
                        onPlaceChangeAnimate = {onAnimate ->
                            runOnUiThread {
                                Log.d("MainActivityLogs", "Animating map in next step")
                                onAnimate()
                                Log.d("MainActivityLogs", "Ended Animating")
                            }
                    })
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun MainView(

    onAnimateToMyLocation: (onrun:()->Unit)->Unit

){


    val context = LocalContext.current
    val mapView = MapView(context)

    LaunchedEffect(key1 = Unit) {

        val mapEventReceiver = object:MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {

                mapView.addMarkertoMap(
                    context,
                    p!!,
                    "At ${p.altitude}, ${p.latitude}",
                    "you marked a marker !",
                    p.altitude.toString()
                )
                return true
            }

        }

        mapView.overlays.add(
            MapEventsOverlay( mapEventReceiver )
        )

        mapView.setMapConfigurations()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Magenta)
    ){

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Box(
                modifier= Modifier
                    .fillMaxWidth(0.7f)
                    .fillMaxHeight(0.8f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(6.dp)
            ){
                AndroidView(
                    factory = { mapView },
                    modifier= Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f),
                    {
                        it.setTileSource(TileSourceFactory.MAPNIK)
                        it.setMultiTouchControls(true)
                        it.controller.zoomTo(3, 1000L)

                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val mGpsMyLocationProvider = GpsMyLocationProvider(context)
                    val myLocOverlay = MyLocationNewOverlay(
                        mGpsMyLocationProvider, mapView
                    ).apply {
                        enableMyLocation()
                        enableFollowLocation()
                        val bitmapIcon = BitmapFactory.decodeResource(context.resources, R.drawable.person_man)
                        setPersonIcon(bitmapIcon)
                    }

                    mapView.overlays.add(myLocOverlay)
                    mapView.overlays.add(myLocOverlay)
                    onAnimateToMyLocation{
                            mapView.controller.animateTo(myLocOverlay.myLocation)
                    }

                },
                modifier= Modifier.fillMaxWidth(0.75f),
            ) {
                Text(text = "Locate Me")
            }


            Button(
                onClick = {
                    permissionContract.launch(
                        arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_NETWORK_STATE
                            )
                    )
                },
                modifier= Modifier.fillMaxWidth(0.75f),
            ) {
                Text(text = "Ask Permissions")
            }

            Button(
                onClick = {
                    mapView.addPinToMap(
                        context,
                        GeoPoint(26.9297201, 81.2037140),
                        "Pin",
                        "Hello My First Pin"
                    )
                },
                modifier= Modifier.fillMaxWidth(0.75f),
            ) {
                Text(text = "Add Pin")
            }

        }

    }

}

fun MapView.addMarkertoMap(
    context: Context,
    geoPoint:GeoPoint,
    markTitle:String,
    description:String,
    id:String
){
    val marker = Marker(this)
    marker.apply{
        position = geoPoint
        icon = ResourcesCompat.getDrawable(resources, R.drawable.map_marker, null)
        title = markTitle
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        this.subDescription = description
        this.id = id
    }

    overlays.add(marker)
    invalidate()

}

fun MapView.addMarkertoMap(
    context: Context,
    place:Place,
    onNextclick:()->Unit
){
    val marker = Marker(this)
    marker.apply{
        position = place.coordinates
        icon = ResourcesCompat.getDrawable(resources, R.drawable.map_marker, null)
        title = place.name
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        this.subDescription = place.description
        this.id = place.id
        infoWindow = CustomMarkerWindow(
            this@addMarkertoMap,
            place = place,
            onNextClick = onNextclick
        )
    }

    overlays.add(marker)
    invalidate()

}

fun MapView.addPinToMap(
    context: Context,
    geoPoint:GeoPoint,
    title:String,
    description:String
){
    val items = mutableListOf<OverlayItem>(
        OverlayItem( title, description, geoPoint )
    )

    val itemsOverlay = ItemizedIconOverlay(
        items,
        object:ItemizedIconOverlay.OnItemGestureListener<OverlayItem>{
            override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                return true
            }

            override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                return true
            }
        },
        context
    )

    overlays.add(itemsOverlay)

}

fun MapView.setMapConfigurations(){

    val dm : DisplayMetrics = context.resources.displayMetrics

    //Enable Roational Control Gestures
    val rotationalGestOverlay = RotationGestureOverlay(this)
    setMultiTouchControls(true)
    overlays.add(rotationalGestOverlay)

    /** Add Mini Map **/
//    val miniMapOverlays = MinimapOverlay(context, tileRequestCompleteHandler)
//    miniMapOverlays.height = dm.heightPixels / 5
//    miniMapOverlays.width = dm.widthPixels / 5
//    overlays.add(miniMapOverlays)

    //Add Compass
    val compassOverlay = CompassOverlay( context, this )
    compassOverlay.enableCompass()
    overlays.add(compassOverlay)


}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LearningMapTheme {
        Greeting("Pratyaksh Soni")
    }
}