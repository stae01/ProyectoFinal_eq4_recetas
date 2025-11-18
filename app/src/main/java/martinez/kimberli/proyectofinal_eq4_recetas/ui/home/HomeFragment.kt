package martinez.kimberli.proyectofinal_eq4_recetas.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var categoriasRecycler: RecyclerView
    private lateinit var comidasRecycler: RecyclerView
    private lateinit var categoriasAdapter: CategoriaAdapter
    private lateinit var comidasAdapter: ComidasAdapter
    private val comidasList = mutableListOf<Comida>()
    private lateinit var welcomeTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var btnTodas: Button
    private lateinit var btnMisRecetas: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        welcomeTextView = view.findViewById(R.id.welcome_text_view)
        loadUserName()

        setupCategoriasRecycler(view)
        setupComidasRecycler(view)
        btnTodas = view.findViewById(R.id.btnTodas)
        btnMisRecetas = view.findViewById(R.id.btnPropias)

        btnTodas.setOnClickListener {
            setToggleSelected(true)
            mostrarTodasRecetas()
        }
        btnMisRecetas.setOnClickListener {
            setToggleSelected(false)
            mostrarMisRecetas()
        }
    }

    private fun loadUserName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.getReference("usuarios").child(userId).child("nombreCompleto")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userName = snapshot.getValue(String::class.java)
                        welcomeTextView.text = "Hola, ${userName ?: ""}!"
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
        categoriasRecycler.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

        val categorias = listOf(
            Categoria("Desayuno", R.drawable.ic_desayuno),
            Categoria("Asiática", R.drawable.ic_asiatica),
            Categoria("Postres", R.drawable.ic_postres),
            Categoria("Mexicana", R.drawable.ic_mexicana),
            Categoria("Americana", R.drawable.ic_americana),
            Categoria("Mediterranea", R.drawable.ic_mediterranea)
        )

        categoriasAdapter = CategoriaAdapter(categorias) { categoria ->
            filtrarPorCategoria(categoria.nombre)
        }
        categoriasRecycler.adapter = categoriasAdapter
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

    private fun mostrarTodasRecetas() {

    }
    private fun mostrarMisRecetas() {

    }
    private fun setupComidasRecycler(view: View) {
        comidasRecycler = view.findViewById(R.id.itemComidas_recycler)
        comidasRecycler.layoutManager = LinearLayoutManager(requireContext())

        // Inicializa la lista con comidas por defecto
        val comidasList = mutableListOf<Comida>(

            Comida("Ramen AKai", "Asiática", "Saludable", "holi","papa,zana","123",true,"20","23","kimi@",R.drawable.ramen, true),
            Comida("Sopa Miso", "Asiática", "Asia", "holi","papa,zana","123",true,"20","23","kimi@",R.drawable.miso, false),
            Comida("Chilaquiles", "Mexicana", "Desayuno", "holi","papa,zana","123",true,"20","23","kimi@",R.drawable.chilaquiles, false),
            Comida("Pad Thai", "Asiática", "Asia", "holi","papa,zana","123",true,"20","23","kimi@",R.drawable.pad_thai, false),
            Comida("Pancakes", "Americana", "Desayuno","holi","papa,zana","123",true,"20","23","kimi@", R.drawable.pancakes, true)
        )

        // Cargar las recetas desde Firebase
        val recetasRef = database.reference.child("recetas")

        recetasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val comidasFirebase = mutableListOf<Comida>()

                // Agregar las recetas cargadas desde Firebase
                for (recetaSnapshot in snapshot.children) {
                    val receta = recetaSnapshot.getValue(Comida::class.java)
                    receta?.let {
                        comidasFirebase.add(it)
                    }
                }

                // Agregar las comidas por defecto + las cargadas desde Firebase
                val allComidas = comidasList + comidasFirebase

                // Actualizar el adaptador con la lista combinada
                comidasAdapter = ComidasAdapter(allComidas) { comida, isFavorite ->
                    comida.isFavorite = isFavorite
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        database.getReference("favoritasUsuarios")
                            .child(userId).child("favorites")
                            .child(comida.nombre)
                            .setValue(isFavorite)
                    }
                }

                comidasRecycler.adapter = comidasAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores aquí
                Log.e("Firebase", "Error al cargar recetas: ${error.message}")
            }
        })


    }


    private fun loadFavoriteComidas() {
        val userId = auth.currentUser?.uid ?: return
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
                    comidasAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) { }
            })
    }

    private fun filtrarPorCategoria(categoria: String) {
        // Implementar filtro real aquí.
        // Por ejemplo:
        val comidasFiltradas = comidasList.filter { it.categoria == categoria }
        comidasAdapter = ComidasAdapter(comidasFiltradas) { comida, isFavorite ->
            // actualizar favorito como antes
        }
        comidasRecycler.adapter = comidasAdapter
    }
}



// Adapter para Comidas
class ComidasAdapter(
    private val comidas: List<Comida>,
    private val onFavoriteClick: (Comida, Boolean) -> Unit
) : RecyclerView.Adapter<ComidasAdapter.ComidaViewHolder>() {

    class ComidaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagen: ImageView = view.findViewById(R.id.comida_imagen)
        val cardView: CardView = view.findViewById(R.id.cardComida)
        val nombre: TextView = view.findViewById(R.id.comida_nombre)
        val categoria: TextView = view.findViewById(R.id.comida_categoria)
        val etiqueta: TextView = view.findViewById(R.id.comida_etiqueta)
        val iconFavorito: ImageView = view.findViewById(R.id.favorite_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComidaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comidas, parent, false)
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



