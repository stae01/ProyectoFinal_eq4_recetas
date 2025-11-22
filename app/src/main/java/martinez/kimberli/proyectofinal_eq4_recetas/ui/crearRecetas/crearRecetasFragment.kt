package martinez.kimberli.proyectofinal_eq4_recetas.ui.crearRecetas

import android.net.Uri
import android.os.Bundle
import android.view.View

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import martinez.kimberli.proyectofinal_eq4_recetas.ImageCloudinary
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentCrearRecetaBinding
import java.util.UUID

class crearRecetasFragment : Fragment() {

    private var _binding: FragmentCrearRecetaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: crearRecetasViewModel
    private var imageUri: Uri? = null

    private lateinit var imageCloudinary: ImageCloudinary


    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivReceta.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentCrearRecetaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(crearRecetasViewModel::class.java)
        imageCloudinary = ImageCloudinary()
        imageCloudinary.initCloudinary(requireContext())
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        // Subir imagen
        binding.ivReceta.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Agregar etiquetas
        binding.btnAgregarEtiqueta.setOnClickListener {
            val texto = binding.etEtiqueta.text.toString().trim()
            if (texto.isNotEmpty()) {
                val chip = Chip(requireContext())
                chip.text = texto
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    binding.chipGroupEtiquetas.removeView(chip)
                }
                binding.chipGroupEtiquetas.addView(chip)
                binding.etEtiqueta.text.clear()
            }
        }

        // Guardar receta
        binding.btnGuardar.setOnClickListener { guardarReceta() }

        // Cancelar
        binding.btnCancelar.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun guardarReceta() {
        val nombre = binding.etNombreReceta.text.toString().trim()
        val tiempo = binding.etTiempoPreparacion.text.toString().trim()
        val descripcion = binding.etDescripcionBreve.text.toString().trim()
        val ingredientes = binding.etIngredientes.text.toString().trim()
        val pasos = binding.etPasosPreparacion.text.toString().trim()
        val link = binding.etLink.text.toString().trim()
        val publica = binding.switchCompartir.isChecked

        val etiquetas = mutableListOf<String>()
        for (i in 0 until binding.chipGroupEtiquetas.childCount) {
            val chip = binding.chipGroupEtiquetas.getChildAt(i) as Chip
            etiquetas.add(chip.text.toString())
        }

        // Recoge la categoría seleccionada del ChipGroup
        val chipGroup = binding.chipGroupCategorias
        val selectedChipId = chipGroup.checkedChipId
        val categoriaSeleccionada: String? = if (selectedChipId != View.NO_ID) {
            val selectedChip: Chip = chipGroup.findViewById(selectedChipId)
            selectedChip.text.toString()
        } else null

        // Validación de datos obligatorios
        if (nombre.isEmpty() || descripcion.isEmpty() || categoriaSeleccionada == null) {
            Toast.makeText(requireContext(), "Faltan campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Sube primero la imagen si se eligió alguna
        if (imageUri != null) {
            imageCloudinary.uploadImage(imageUri!!, false) { success, url ->
                requireActivity().runOnUiThread {
                    if (success && url != null) {
                        guardarRecetaFirebase(
                            nombre,
                            categoriaSeleccionada,
                            tiempo,
                            descripcion,
                            ingredientes,
                            pasos,
                            etiquetas,
                            link,
                            publica,
                            url
                        )
                    } else {
                        Toast.makeText(requireContext(), "Error al subir imagen de la receta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            guardarRecetaFirebase(
                nombre,
                categoriaSeleccionada,
                tiempo,
                descripcion,
                ingredientes,
                pasos,
                etiquetas,
                link,
                publica,
                null
            )
        }
    }

    private fun guardarRecetaFirebase(
        nombre: String,
        categoria: String,
        tiempo: String,
        descripcion: String,
        ingredientes: String,
        pasos: String,
        etiquetas: List<String>,
        link: String?,
        publica: Boolean,
        imageUrl: String?
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        // Llenar el mapa para que coincida con tu modelo y Firebase
        val receta = hashMapOf(
            "nombre" to nombre,
            "categoria" to categoria,
            "etiquetas" to etiquetas,
            "tiempo" to tiempo,
            "descripcion" to descripcion,
            "ingredientes" to ingredientes,
            "pasos" to pasos,
            "publica" to publica,
            "imagenUrl" to (imageUrl ?: ""),
            "usuarioId" to user.uid,
            "usuarioEmail" to user.email,
            "link" to (link ?: ""),
            "id" to UUID.randomUUID().toString(),
            "fechaCreacion" to ServerValue.TIMESTAMP,
            "isFavorite" to false
        )

        viewModel.guardarRecetaEnFirebase(
            receta
        ) { success, message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            if (success) requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
