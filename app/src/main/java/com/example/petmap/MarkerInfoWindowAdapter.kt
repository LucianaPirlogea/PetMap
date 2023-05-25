package com.example.petmap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.petmap.models.Pet
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {
        val pet = p0?.tag as? Pet ?: return null

        val view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_contents, null
        )
        view.findViewById<TextView>(
            R.id.animal
        ).text = pet.animal
        view.findViewById<TextView>(
            R.id.date
        ).text = pet.date

        return view
    }

    override fun getInfoWindow(p0: Marker): View? {

        return null
    }
}