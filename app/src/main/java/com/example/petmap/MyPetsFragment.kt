import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petmap.databinding.FragmentMyPetsBinding
import com.example.petmap.models.Pet
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

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
                reference.downloadUrl.addOnSuccessListener { uri ->
                    // Create a Pet object with the retrieved download URL
                    val pet = Pet(uri.toString(), "", "", "")
                    petList.add(pet)
                    petListAdapter.notifyDataSetChanged()
                }
            }
        }.addOnFailureListener { exception ->
            // Handle the exception
        }
    }
}
