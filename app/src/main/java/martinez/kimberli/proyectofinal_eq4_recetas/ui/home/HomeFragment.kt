package martinez.kimberli.proyectofinal_eq4_recetas.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
    private lateinit var welcomeTextView: TextView
    private lateinit var etBuscarRecetas: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var btnTodas: Button
    private lateinit var btnMisRecetas: Button

    private val favoritosIds = mutableSetOf<String>()

    private val todasComidas = mutableListOf<Comida>()
    private var comidasFiltradas = mutableListOf<Comida>()
    private var filtroCategoria: String? = null
    private var mostrarSoloMisRecetas = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        welcomeTextView = view.findViewById(R.id.welcome_text_view)
        etBuscarRecetas = view.findViewById(R.id.etBuscarRecetas)

        btnTodas = view.findViewById(R.id.btnTodas)
        btnMisRecetas = view.findViewById(R.id.btnPropias)

        loadUserName()
        setupCategoriasRecycler(view)
        setupComidasRecycler(view)
        cargarFavoritosDelUsuario()
        cargarTodasLasRecetas()

        btnTodas.setOnClickListener {
            setToggleSelected(true)
            mostrarSoloMisRecetas = false
            aplicarFiltros(etBuscarRecetas.text.toString())
        }
        btnMisRecetas.setOnClickListener {
            setToggleSelected(false)
            mostrarSoloMisRecetas = true
            aplicarFiltros(etBuscarRecetas.text.toString())
    }
    etBuscarRecetas.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            aplicarFiltros(s?.toString())
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
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
                    cargarTodasLasRecetas()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun setupCategoriasRecycler(view: View) {
        categoriasRecycler = view.findViewById(R.id.categorias_recycler)
        categoriasRecycler.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL, false)

        val categorias = listOf(
            Categoria("Desayuno y brunch", R.drawable.ic_desayuno),
            Categoria("Platos principales", R.drawable.tacos),
            Categoria("Aperitivos y entrantes", R.drawable.ic_aperitivos),
            Categoria("Sopas y cremas", R.drawable.ic_asiatica),
            Categoria("Guarniciones", R.drawable.ic_guarnicion),
            Categoria("Postres", R.drawable.ic_postres)
        )

        categoriasAdapter = CategoriaAdapter(categorias) { categoria ->
            if (filtroCategoria == categoria.nombre) {
                filtroCategoria = null
            } else {
                filtroCategoria = categoria.nombre
            }
            aplicarFiltros(etBuscarRecetas.text.toString())
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


    private fun setupComidasRecycler(view: View) {
        comidasRecycler = view.findViewById(R.id.itemComidas_recycler)
        comidasRecycler.layoutManager = LinearLayoutManager(requireContext())


        comidasAdapter = ComidasAdapter(comidasFiltradas) { comida, isFavorite ->
            comida.isFavorite = isFavorite
            val userId = auth.currentUser?.uid
            if (userId != null) {
                database.getReference("favoritasUsuarios")
                    .child(userId).child("favorites")
                    .child(comida.id)
                    .setValue(isFavorite)
            }
        }
        comidasRecycler.adapter = comidasAdapter
    }



    private fun cargarTodasLasRecetas() {
        val recetasRef = database.reference.child("recetas")

        recetasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                todasComidas.clear()
                for (recetaSnapshot in snapshot.children) {
                    val receta = recetaSnapshot.getValue(Comida::class.java)
                    receta?.let { todasComidas.add(it) }
                }
                aplicarFiltros(etBuscarRecetas.text.toString())
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error al cargar recetas: ${error.message}")
            }
        })
    }

    private fun aplicarFiltros(query: String? = null) {
        val userId = auth.currentUser?.uid

        var lista = todasComidas.toList()

        // Filtra solo mis recetas si corresponde
        if (mostrarSoloMisRecetas && userId != null) {
            lista = lista.filter { it.usuarioId == userId }
        }

        // Filtra por categoría si corresponde
        filtroCategoria?.let { cat ->
            lista = lista.filter { it.categoria == cat }
        }

        // Filtro de texto/búsqueda (nombre, categoría, o cualquier etiqueta)
        if (!query.isNullOrBlank()) {
            val qLower = query.lowercase()
            lista = lista.filter { comida ->
                comida.nombre.lowercase().contains(qLower) ||
                        comida.categoria.lowercase().contains(qLower) ||
                        (comida.etiquetas?.any { it.lowercase().contains(qLower) } == true)
            }
        }

        comidasFiltradas.clear()
        comidasFiltradas.addAll(lista)
        comidasAdapter.notifyDataSetChanged()
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

    override fun onBindViewHolder(holder: ComidaViewHolder, position: Int) {
        val comida = comidas[position]
        holder.nombre.text = comida.nombre
        holder.categoria.text = comida.categoria
        // Mostrar etiquetas (join a una sola línea)
        holder.etiqueta.text = comida.etiquetas?.joinToString(", ") ?: ""

        Log.d("DEBUG_CATEG", "nombre=${comida.nombre} categoria=${comida.categoria}")
        holder.categoria.text = comida.categoria

        // Mostrar imagen desde URL o placeholder local si está vacío
        if (comida.imagenUrl.isNotBlank()) {
            Glide.with(holder.imagen.context)
                .load(comida.imagenUrl)
                .placeholder(R.drawable.placeholder) // Coloca tu recurso placeholder aquí
                .into(holder.imagen)
        } else {
            holder.imagen.setImageResource(R.drawable.placeholder)
        }


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
            icon.setImageResource(R.drawable.ic_corazon)

        }
    }

    override fun getItemCount() = comidas.size
}



