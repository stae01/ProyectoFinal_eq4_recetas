package martinez.kimberli.proyectofinal_eq4_recetas.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import martinez.kimberli.proyectofinal_eq4_recetas.R
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.kimberli.proyectofinal_eq4_recetas.Categoria
import martinez.kimberli.proyectofinal_eq4_recetas.CategoriaAdapter
import martinez.kimberli.proyectofinal_eq4_recetas.Comida

class HomeFragment : Fragment() {

    private lateinit var categoriasRecycler: RecyclerView
    private lateinit var comidasRecycler: RecyclerView
    private lateinit var categoriasAdapter: CategoriaAdapter
    private lateinit var comidasAdapter: ComidasAdapter
    private lateinit var welcomeTextView: TextView
    private val comidasList = mutableListOf<Comida>() // Declare as mutable list

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        welcomeTextView = view.findViewById(R.id.welcome_text_view)
        loadUserName()

        setupCategoriasRecycler(view)
        setupComidasRecycler(view)
    }

    private fun loadUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("users").child(userId).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userName = snapshot.getValue(String::class.java)
                        if (userName != null) {
                            welcomeTextView.text = "Hola, $userName!"
                        } else {
                            welcomeTextView.text = "Hola!"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        welcomeTextView.text = "Hola!"
                    }
                })
        } else {
            welcomeTextView.text = "Hola!"
        }
    }

    private fun setupCategoriasRecycler(view: View) {
        categoriasRecycler = view.findViewById(R.id.categorias_recycler)
        categoriasRecycler.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        val categorias = listOf(
            Categoria("Desayuno", R.drawable.ic_desayuno),
            Categoria("Asiática", R.drawable.ic_asiatica),
            Categoria("Postres", R.drawable.ic_postres),
            Categoria("Mexicana", R.drawable.ic_mexicana),
            Categoria("Americana", R.drawable.ic_americana),
            Categoria("Mediterranea", R.drawable.ic_mediterranea)
        )

        categoriasAdapter = CategoriaAdapter(categorias) { categoria ->
            // Manejar click en categoría
            filtrarPorCategoria(categoria.nombre)
        }
        categoriasRecycler.adapter = categoriasAdapter
    }

    private fun setupComidasRecycler(view: View) {
        comidasRecycler = view.findViewById(R.id.itemComidas_recycler)
        comidasRecycler.layoutManager = LinearLayoutManager(requireContext())

        // Initialize comidasList with default items
        comidasList.addAll(listOf(
            Comida(
                "Ramen AKai",
                "Sopas",
                "Saludable",
                R.drawable.ramen,
                false
            ),
            Comida(
                "Sopa Miso",
                "Sopas",
                "Asia",
                R.drawable.miso,
                false
            ),
            Comida(
                "Chilaquiles",
                "Mexicana",
                "Desayuno",
                R.drawable.chilaquiles,
                false
            ),
            Comida(
                "Pad Thai",
                "Asiática",
                "Asia",
                R.drawable.pad_thai,
                false
            ),
            Comida(
                "Pancakes",
                "Americana",
                "Desayuno",
                R.drawable.pancakes,
                false
            )
        ))

        loadFavoriteComidas() // Load favorite status from Firebase

        comidasAdapter = ComidasAdapter(comidasList) { comida, isFavorite ->
            // Manejar click en favorito
            comida.isFavorite = isFavorite
            val userId = auth.currentUser?.uid
            if (userId != null) {
                database.getReference("favoritas").child(userId).child("favorites").child(comida.nombre)
                    .setValue(isFavorite)
            }
        }
        comidasRecycler.adapter = comidasAdapter
    }

    private fun loadFavoriteComidas() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("favoritas").child(userId).child("favorites")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (comidaSnapshot in snapshot.children) {
                            val comidaName = comidaSnapshot.key
                            val isFavorite = comidaSnapshot.getValue(Boolean::class.java) ?: false

                            val index = comidasList.indexOfFirst { it.nombre == comidaName }
                            if (index != -1) {
                                comidasList[index].isFavorite = isFavorite
                            }
                        }
                        comidasAdapter.notifyDataSetChanged() // Notify adapter after updating data
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }

    private fun filtrarPorCategoria(categoria: String) {
        // Implementar filtro de categorías
        // Por ahora solo muestra un mensaje
    }
}

// Data classes





// Adapter para Comidas
class ComidasAdapter(
    private val comidas: List<Comida>,
    private val onFavoriteClick: (Comida, Boolean) -> Unit
) : RecyclerView.Adapter<ComidasAdapter.ComidaViewHolder>() {

    class ComidaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardComida)
        val imagen: ImageView = view.findViewById(R.id.imagenComida)
        val nombre: TextView = view.findViewById(R.id.nombreComida)
        val categoria: TextView = view.findViewById(R.id.categoriaComida)
        val etiqueta: Chip = view.findViewById(R.id.etiquetaComida)
        val iconFavorito: ImageView = view.findViewById(R.id.iconFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comida, parent, false)
        return ComidaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComidasAdapter.ComidaViewHolder, position: Int) {
        val comida = comidas[position]

        holder.nombre.text = comida.nombre
        holder.categoria.text = comida.categoria
        holder.etiqueta.text = comida.etiqueta
        holder.imagen.setImageResource(comida.imagenRes)

        // Actualizar icono de favorito
        updateFavoriteIcon(holder.iconFavorito, comida.isFavorite)

        // Click en favorito
        holder.iconFavorito.setOnClickListener {
            comida.isFavorite = !comida.isFavorite
            updateFavoriteIcon(holder.iconFavorito, comida.isFavorite)
            onFavoriteClick(comida, comida.isFavorite)
        }

        // Click en toda la card
        holder.cardView.setOnClickListener {
            // Navegar a detalle de comida
        }
    }

    private fun updateFavoriteIcon(icon: ImageView, isFavorite: Boolean) {
        if (isFavorite) {
            icon.setImageResource(R.drawable.ic_heart_filled)
        } else {
            icon.setImageResource(R.drawable.ic_heart_outline)
        }
    }

    override fun getItemCount() = comidas.size
}
