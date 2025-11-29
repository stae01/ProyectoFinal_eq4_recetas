package martinez.kimberli.proyectofinal_eq4_recetas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.ActivityDetalleRecetaBinding


class DetalleRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRecetaBinding
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recetaId = intent.getStringExtra("recetaId") ?: return

        binding.btnBack.setOnClickListener {
            finish()
        }

        cargarRecetaDeFirebase(recetaId)
    }

    private fun cargarRecetaDeFirebase(recetaId: String) {
        val recetasRef = FirebaseDatabase.getInstance().getReference("recetas")
        recetasRef.orderByChild("id").equalTo(recetaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val receta = snap.getValue(Comida::class.java)
                        if (receta != null) {
                            receta.id
                            mostrarReceta(receta)
                            manejarAccionesPropias(receta, snap.key ?: "")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun mostrarReceta(receta: Comida) {
        binding.tvNombreReceta.text = receta.nombre
        binding.tvDescripcion.text = receta.descripcion
        binding.tvIngredientes.text = receta.ingredientes
        binding.tvPreparacion.text = receta.pasos
        binding.tvTiempo.text =
            if (!receta.tiempo.isNullOrBlank()) "⏱ Tiempo: ${receta.tiempo}" else "⏱ Tiempo: No especificado"

        binding.tvCategoria.text =
            if (!receta.categoria.isNullOrBlank()) "Categoría: ${receta.categoria}" else "Categoría: -"

        binding.tvEtiquetas.text =
            if (!receta.etiquetas.isNullOrEmpty()) {
                "Etiquetas: ${receta.etiquetas!!.joinToString(", ")}"
            } else {
                "Etiquetas: -"
            }

        binding.tvLink.text =
            if (!receta.link.isNullOrBlank()) "Ver link: ${receta.link}" else "Sin link extra"

        Glide.with(this)
            .load(receta.imagenUrl)
            .placeholder(R.drawable.placeholder)
            .into(binding.imgReceta)

        manejarFavorito(receta)

    }
    private fun manejarFavorito(receta: Comida) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val favRef = FirebaseDatabase.getInstance()
            .getReference("favoritasUsuarios")
            .child(userId)
            .child("favorites")
            .child(receta.id)

        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isFavorita = snapshot.getValue(Boolean::class.java) == true
                actualizarIconoFavorito(isFavorita)

                binding.favoriteIcon.setOnClickListener {
                    isFavorita= !isFavorita
                    favRef.setValue(isFavorita)
                    actualizarIconoFavorito(isFavorita)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarIconoFavorito(isFavorita: Boolean) {
        binding.favoriteIcon.setImageResource(
            if (isFavorita) R.drawable.ic_heart_filled else R.drawable.ic_corazon
        )
    }
    // ver icono editar/eliminar si la receta es propia
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

        // Editar abre pantalla de editar
        binding.btnEditar.setOnClickListener {
            val intent = Intent(this, EditarRecetaActivity::class.java)
            intent.putExtra("modo", "editar")
            intent.putExtra("recetaId", receta.id)
            startActivity(intent)
        }

        // Eliminar: dialogo de confirmación
        binding.btnEliminar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Seguro que deseas eliminar esta receta?")
                .setPositiveButton("Sí") { _, _ ->
                    eliminarReceta(recetaKey)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun eliminarReceta(recetaKey: String) {
        val recetasRef = database.getReference("recetas").child(recetaKey)
        recetasRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}