import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petmap.databinding.FragmentMyPetsBinding
import com.example.petmap.models.Pet
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.json.JSONObject

class MyPetsFragment : Fragment() {

    private lateinit var binding: FragmentMyPetsBinding
    private lateinit var storageRef: StorageReference
    private lateinit var petListAdapter: MyPetsAdapter
    private var petList: MutableList<Pet> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyPetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchBar()
        retrieveDataFromFirebaseStorage()
        binding.buttonShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT,convertPetListToString(petList))

            val chooser = Intent.createChooser(intent, "Share using...")
            startActivity(chooser)
        }
    }

    private fun setupRecyclerView() {
        petListAdapter = MyPetsAdapter(petList)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = petListAdapter
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString().trim().lowercase()
                val filteredList = petList.filter { pet ->
                    pet.animal.lowercase().contains(searchQuery)
                }
                petListAdapter.filterList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun retrieveDataFromFirebaseStorage() {
        storageRef = FirebaseStorage.getInstance().reference.child("pets")
        storageRef.listAll().addOnSuccessListener { result ->
            petList.clear()
            for (reference in result.items) {
                val jsonFileRef = reference.parent?.child(reference.name)
                jsonFileRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener { bytes ->
                    val json = String(bytes)
                    val petInfo = parseJson(json)
                    petList.add(petInfo)
                    petListAdapter.notifyDataSetChanged()
                }
            }
        }.addOnFailureListener { exception ->
        }
    }

    private fun parseJson(json: String): Pet {
        val jsonObject = JSONObject(json)
        val latitude = "Latitude: " + jsonObject.getString("latitude")
        val longitude = "Longitude: " + jsonObject.getString("longitude")
        val date = jsonObject.getString("date")
        val animal = jsonObject.getString("animal")

        return Pet(latitude, longitude, date, animal)
    }

    fun convertPetListToString(petList: List<Pet>): String? {
        var result: String? = ""
        for(pet in petList){
            result += pet.animal + "\n" + pet.date + "\n\n"
        }
        return result
    }

}
