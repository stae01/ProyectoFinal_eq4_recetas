package martinez.kimberli.proyectofinal_eq4_recetas.ui.Favoritas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.kimberli.proyectofinal_eq4_recetas.Comida
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentFavoritasBinding
import martinez.kimberli.proyectofinal_eq4_recetas.ui.home.ComidasAdapter

class FavoritasFragment : Fragment() {

    private lateinit var comidasRecycler: RecyclerView
    private lateinit var comidasAdapter: ComidasAdapter

    private val recetasFavoritas = mutableListOf<Comida>()
    private val favoritosIds = mutableSetOf<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var btnTodas: Button
    private lateinit var btnMisRecetas: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_favoritas, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        comidasRecycler = view.findViewById(R.id.favoritos_recycler)
        comidasRecycler.layoutManager = LinearLayoutManager(requireContext())
        comidasAdapter = ComidasAdapter(recetasFavoritas) { comida, isFavorite ->
            val userId = auth.currentUser?.uid ?: return@ComidasAdapter
            database.reference.child("favoritasUsuarios")
                .child(userId).child("favorites")
                .child(comida.id)
                .setValue(isFavorite)
        }
        comidasRecycler.adapter = comidasAdapter

        cargarFavoritosDelUsuario()
    }
    private fun setToggleSelected(TodasSelected: Boolean) {
        if (TodasSelected) {
            btnTodas.setBackgroundResource(R.drawable.toggle_seleccionado_bg)
            btnTodas.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            btnMisRecetas.setBackgroundResource(R.drawable.toggle_noseleccionado_bg)
            btnMisRecetas.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        } else {
            btnMisRecetas.setBackgroundResource(R.drawable.toggle_seleccionado_bg)
            btnMisRecetas.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            btnTodas.setBackgroundResource(R.drawable.toggle_noseleccionado_bg)
            btnTodas.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun cargarFavoritosDelUsuario() {
        val userId = auth.currentUser?.uid ?: return
        database.reference.child("favoritasUsuarios").child(userId).child("favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoritosIds.clear()
                    for (favSnapshot in snapshot.children) {
                        if (favSnapshot.getValue(Boolean::class.java) == true) {
                            favoritosIds.add(favSnapshot.key ?: "")
                        }
                    }
                    cargarDatosRecetasFavoritas()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun cargarDatosRecetasFavoritas() {
        val recetasRef = database.reference.child("recetas")
        recetasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recetasFavoritas.clear()
                for (recetaSnapshot in snapshot.children) {
                    val receta = recetaSnapshot.getValue(Comida::class.java)
                    if (receta != null && favoritosIds.contains(receta.id)) {
                        receta.isFavorite = true
                        recetasFavoritas.add(receta)
                    }
                }
                comidasAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}