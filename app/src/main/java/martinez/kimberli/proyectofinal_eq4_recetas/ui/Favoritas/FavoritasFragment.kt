package martinez.kimberli.proyectofinal_eq4_recetas.ui.Favoritas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
    private lateinit var etBuscarRecetas: EditText
    private var mostrarSoloMisRecetas = false
    private val todasComidas = mutableListOf<Comida>()
    private var filtroCategoria: String? = null
    private var comidasFiltradas = mutableListOf<Comida>()

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
        etBuscarRecetas = view.findViewById(R.id.etBuscarRecetas)

        btnTodas = view.findViewById(R.id.btnTodas)
        btnMisRecetas = view.findViewById(R.id.btnPropias)
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