import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.petmap.models.Pet
import com.example.petmap.databinding.PetBinding

class MyPetsAdapter(private var petList: List<Pet>) :
    RecyclerView.Adapter<MyPetsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            PetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(petList[position])
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    fun filterList(filteredList: List<Pet>) {
        petList = filteredList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: PetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pet: Pet) {
            binding.apply {
                latitude.text = pet.latitude
                longitude.text = pet.longitude
                date.text = pet.date
                animal.text = pet.animal
            }
        }
    }
}
