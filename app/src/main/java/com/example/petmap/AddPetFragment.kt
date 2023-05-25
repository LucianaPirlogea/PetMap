package com.example.petmap

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.petmap.databinding.FragmentAddPetBinding
import com.example.petmap.models.Pet
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson

class AddPetFragment : Fragment() {

    lateinit var binding: FragmentAddPetBinding
    var currentLocation: Location? = null
    val storageRef = Firebase.storage.reference
    val gson = Gson()
    lateinit var listener: AddPetFragmentListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPetBinding.inflate(inflater, container, false)

        currentLocation = arguments?.getParcelable<Location>("current_location")

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as AddPetFragmentListener
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val latitude = view.findViewById<TextView>(R.id.location_lat);
        val longitude = view.findViewById<TextView>(R.id.location_long);

        val latVal = currentLocation?.latitude.toString()
        val longVal = currentLocation?.longitude.toString()
        latitude.text = "Latitude: $latVal";
        longitude.text = "Longitude: $longVal";

        val date = view.findViewById<TextView>(R.id.date_time)
        val animal = view.findViewById<TextView>(R.id.animal)

        val currentDate = Date()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val timeFormat = SimpleDateFormat("HH:mm:ss")

        val formattedDate = dateFormat.format(currentDate)
        val formattedTime = timeFormat.format(currentDate)

        date.text = "Date: $formattedDate\nTime: $formattedTime"

        val path = "pets/" + "$formattedDate" + "$formattedTime" + ".json"

        var addPetButton = view.findViewById<Button>(R.id.button)
        addPetButton.setOnClickListener {
            var pet = Pet(latitude = latVal, longitude = longVal, date = date.text.toString(), animal = animal.text.toString())
            val petJson = gson.toJson(pet)
            val petBytes = petJson.toByteArray()
            val petRef = storageRef.child(path)
            petRef.putBytes(petBytes)
                .addOnSuccessListener {
                    listener.onAddPetCompleted()
                }
                .addOnFailureListener {
                }

        }
    }

    interface AddPetFragmentListener {
        fun onAddPetCompleted()
    }

}