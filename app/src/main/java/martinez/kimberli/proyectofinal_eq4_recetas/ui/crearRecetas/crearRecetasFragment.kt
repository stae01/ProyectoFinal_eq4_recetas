package martinez.kimberli.proyectofinal_eq4_recetas.ui.crearRecetas

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cloudinary.Cloudinary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cloudinary.utils.ObjectUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import martinez.kimberli.proyectofinal_eq4_recetas.ImageCloudinary
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentCrearRecetaBinding

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

    private fun validarCamposReceta(nombre: String, tiempo: String, descripcion: String, ingredientes: String, pasos: String ): Boolean {
        when {
            nombre.isEmpty() -> {
                binding.etNombreReceta.error = "Ingresa el nombre de la receta"
                binding.etNombreReceta.requestFocus()
                return false
            }
            descripcion.isEmpty() -> {
                binding.etDescripcionBreve.error = "Agrega una descripción"
                binding.etDescripcionBreve.requestFocus()
                return false
            }
            ingredientes.isEmpty() -> {
                binding.etIngredientes.error = "Agrega ingredientes"
                binding.etIngredientes.requestFocus()
                return false
            }
            pasos.isEmpty() -> {
                binding.etPasosPreparacion.error = "Agrega los pasos"
                binding.etPasosPreparacion.requestFocus()
                return false
            }
            tiempo.isEmpty() -> {
                binding.etTiempoPreparacion.error = "Agrega tiempo de preparación"
                binding.etTiempoPreparacion.requestFocus()
                return false
            }
        }
        return true
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

        if (!validarCamposReceta(nombre, tiempo, descripcion, ingredientes, pasos)) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val fechaCreacion = System.currentTimeMillis()

        if (imageUri != null) {
            imageCloudinary.uploadImage(imageUri!!, false) { success, url ->
                requireActivity().runOnUiThread {
                    if (success && url != null) {
                        guardarRecetaFirestore(
                            nombre, tiempo, descripcion, ingredientes, pasos, etiquetas, link, publica, url
                        )
                    } else {
                        Toast.makeText(requireContext(), "Error al subir imagen de la receta", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            guardarRecetaFirestore(
                nombre, tiempo, descripcion, ingredientes, pasos, etiquetas, link, publica, null
            )
        }
    }

    private fun guardarRecetaFirestore(
        nombre: String, tiempo: String, descripcion: String, ingredientes: String, pasos: String,
        etiquetas: List<String>, link: String?, publica: Boolean, imageUrl: String?
    ) {
        viewModel.guardarReceta(
            nombre, tiempo, descripcion, ingredientes, pasos, etiquetas, link, publica, imageUrl
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