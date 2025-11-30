package martinez.kimberli.proyectofinal_eq4_recetas

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.ActivityEditarRecetaBinding
import java.util.*

class EditarRecetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarRecetaBinding
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var recetaKey: String? = null
    private var recetaOriginal: Comida? = null
    private var imagenUri: Uri? = null

    private var selectedHours: Int = 0
    private var selectedMinutes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recetaKey = intent.getStringExtra("recetaKey")
        if (recetaKey == null) {
            Toast.makeText(this, "No se ha proporcionado una receta para editar.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        configurarListeners()
        cargarDatosReceta()
    }

    private fun configurarListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { guardarReceta() }
        binding.btnEliminar.setOnClickListener { confirmarEliminacion() }
        binding.imgReceta.setOnClickListener { seleccionarImagen() }
        binding.etTiempo.setOnClickListener { showTimePickerDialog() }
    }

    private fun showTimePickerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, null)
        val hoursPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.picker_hours)
        val minutesPicker = dialogView.findViewById<android.widget.NumberPicker>(R.id.picker_minutes)

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 24
        hoursPicker.value = selectedHours

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.value = selectedMinutes

        AlertDialog.Builder(this)
            .setTitle("Tiempo de preparación")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { _, _ ->
                selectedHours = hoursPicker.value
                selectedMinutes = minutesPicker.value
                binding.etTiempo.setText("$selectedHours h $selectedMinutes min")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarDatosReceta() {
        val recetaRef = database.getReference("recetas").child(recetaKey!!)
        recetaRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recetaOriginal = snapshot.getValue(Comida::class.java)
                if (recetaOriginal != null) {
                    recetaOriginal!!.id = snapshot.key
                    poblarCampos(recetaOriginal!!)
                } else {
                    Toast.makeText(this@EditarRecetaActivity, "No se pudo cargar la receta.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditarRecetaActivity, "Error al cargar: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun poblarCampos(receta: Comida) {
        binding.etNombreReceta.setText(receta.nombre)
        binding.etDescripcion.setText(receta.descripcion)
        binding.etIngredientes.setText(receta.ingredientes)
        binding.etPreparacion.setText(if (!receta.preparacion.isNullOrBlank()) receta.preparacion else receta.pasos)

        // Parsear el tiempo
        receta.tiempo?.let {
            val parts = it.toString().split(" ")
            if (parts.size >= 2 && parts.getOrNull(1) == "h") {
                selectedHours = parts[0].toIntOrNull() ?: 0
            }
            if (parts.size >= 4 && parts.getOrNull(3) == "min") {
                selectedMinutes = parts[2].toIntOrNull() ?: 0
            }
        }
        binding.etTiempo.setText(receta.tiempo ?: "")


        binding.etCategoria.setText(receta.categoria)
        binding.etEtiquetas.setText(receta.etiquetas?.joinToString(", "))
        binding.etLink.setText(receta.link)

        Glide.with(this)
            .load(receta.imagenUrl)
            .placeholder(R.drawable.placeholder)
            .into(binding.imgReceta)
    }

    private fun seleccionarImagen() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imagenUri = it
                binding.imgReceta.setImageURI(it)
            }
        }
    }

    private fun guardarReceta() {
        if (imagenUri != null) {
            subirImagenYGuardarDatos()
        } else {
            guardarDatosEnFirebase(recetaOriginal?.imagenUrl)
        }
    }

    private fun subirImagenYGuardarDatos() {
        val storageRef = storage.getReference("recetas_imagenes/${UUID.randomUUID()}")
        imagenUri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { url ->
                        guardarDatosEnFirebase(url.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarDatosEnFirebase(urlImagenFinal: String?) {
        val nombre = binding.etNombreReceta.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        val tiempo = if (selectedHours > 0 || selectedMinutes > 0) {
            "$selectedHours h $selectedMinutes min"
        } else {
            binding.etTiempo.text.toString().trim()
        }

        val etiquetasList = binding.etEtiquetas.text.toString().trim().split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val recetaActualizada = Comida(
            id = recetaKey,
            nombre = nombre,
            descripcion = binding.etDescripcion.text.toString().trim(),
            ingredientes = binding.etIngredientes.text.toString().trim(),
            preparacion = binding.etPreparacion.text.toString().trim(),
            tiempo = tiempo,
            categoria = binding.etCategoria.text.toString().trim(),
            etiquetas = etiquetasList,
            link = binding.etLink.text.toString().trim(),
            imagenUrl = urlImagenFinal,
            usuarioId = auth.currentUser?.uid
        )

        database.getReference("recetas").child(recetaKey!!)
            .setValue(recetaActualizada)
            .addOnSuccessListener {
                Toast.makeText(this, "Receta actualizada con éxito", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmarEliminacion() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Receta")
            .setMessage("¿Estás seguro de que quieres eliminar esta receta? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarReceta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarReceta() {
        recetaOriginal?.imagenUrl?.let { url ->
            if (url.isNotEmpty()) {
                try {
                    val storageRef = storage.getReferenceFromUrl(url)
                    storageRef.delete().addOnFailureListener {
                        println("Error al eliminar imagen: ${it.message}")
                    }
                } catch (e: Exception) {
                    println("Error al obtener referencia de la imagen para eliminar: ${e.message}")
                }
            }
        }

        database.getReference("recetas").child(recetaKey!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}