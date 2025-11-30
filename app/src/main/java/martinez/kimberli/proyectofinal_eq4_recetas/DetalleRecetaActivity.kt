package martinez.kimberli.proyectofinal_eq4_recetas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.ActivityDetalleRecetaBinding
import martinez.kimberli.proyectofinal_eq4_recetas.ui.crearRecetas.EditarRecetaActivity

class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRecetaBinding
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var recetaKey: String? = null
    private var isFavoritaActual = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recetaKey = intent.getStringExtra("recetaKey")
        if (recetaKey == null) {
            Toast.makeText(this, "Error: No se proporcionó la receta.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        recetaKey?.let { cargarRecetaDeFirebase(it) }
    }

    private fun cargarRecetaDeFirebase(recetaKey: String) {
        val recetaRef = database.getReference("recetas").child(recetaKey)
        recetaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(applicationContext, "Receta no encontrada", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }
                val receta = snapshot.getValue(Comida::class.java)
                if (receta != null) {
                    receta.id = snapshot.key
                    mostrarReceta(receta)
                    manejarAccionesPropias(receta, snapshot.key!!)
                } else {
                    Toast.makeText(applicationContext, "Error al leer la receta", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarReceta(receta: Comida) {
        binding.tvNombreReceta.text = receta.nombre
        binding.tvDescripcion.text = receta.descripcion
        binding.tvIngredientes.text = receta.ingredientes
        binding.tvPreparacion.text = if (!receta.preparacion.isNullOrBlank()) receta.preparacion else receta.pasos
        
        val tiempoStr = receta.tiempo?.toString() ?: ""
        binding.tvTiempo.text = if (tiempoStr.isNotBlank()) "⏱ Tiempo: $tiempoStr" else "⏱ Tiempo: No especificado"

        binding.tvCategoria.text =
            if (!receta.categoria.isNullOrBlank()) "Categoría: ${receta.categoria}" else "Categoría: -"

        binding.tvEtiquetas.text =
            if (!receta.etiquetas.isNullOrEmpty()) {
                "Etiquetas: ${receta.etiquetas.joinToString(", ")}"
            } else {
                "Etiquetas: -"
            }

        binding.tvLink.text =
            if (!receta.link.isNullOrBlank()) "Ver link: ${receta.link}" else "Sin link extra"

        Glide.with(this)
            .load(receta.imagenUrl)
            .placeholder(R.drawable.placeholder)
            .error(android.R.drawable.ic_dialog_alert)
            .into(binding.imgReceta)

        auth.currentUser?.let { user ->
            receta.id?.let { recetaId ->
                configurarListenerFavorito(user, recetaId)
            }
        }
    }

    private fun configurarListenerFavorito(user: FirebaseUser, recetaId: String) {
        val favRef = database.getReference("favoritasUsuarios").child(user.uid).child("favorites").child(recetaId)

        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isFavoritaActual = snapshot.getValue(Boolean::class.java) == true
                actualizarIconoFavorito(isFavoritaActual)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        binding.favoriteIcon.setOnClickListener {
            isFavoritaActual = !isFavoritaActual
            favRef.setValue(isFavoritaActual)
            actualizarIconoFavorito(isFavoritaActual)
        }
    }

    private fun actualizarIconoFavorito(isFavorita: Boolean) {
        val icon = if (isFavorita) R.drawable.ic_heart_filled else R.drawable.ic_corazon
        binding.favoriteIcon.setImageResource(icon)
        binding.favoriteIcon.imageTintList = ContextCompat.getColorStateList(this, R.color.orange)
    }

    private fun manejarAccionesPropias(receta: Comida, recetaKey: String) {
        val currentUserId = auth.currentUser?.uid
        val esPropia = !receta.usuarioId.isNullOrBlank() && receta.usuarioId == currentUserId

        if (esPropia) {
            binding.btnEditar.visibility = View.VISIBLE
            binding.btnEliminar.visibility = View.VISIBLE
        } else {
            binding.btnEditar.visibility = View.GONE
            binding.btnEliminar.visibility = View.GONE
            return
        }

        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, EditarRecetaActivity::class.java)
            intent.putExtra("recetaKey", recetaKey)
            startActivity(intent)
        }

        binding.btnEliminar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Seguro que deseas eliminar esta receta?")
                .setPositiveButton("Sí") { _, _ -> eliminarReceta(recetaKey) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun eliminarReceta(recetaKey: String) {
        val recetasRef = database.getReference("recetas").child(recetaKey)
        recetasRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Receta eliminada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}