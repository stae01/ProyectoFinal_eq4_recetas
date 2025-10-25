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
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentCrearRecetaBinding

class crearRecetasFragment : Fragment() {

    private var _binding: FragmentCrearRecetaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: crearRecetasViewModel
    private var imageUri: Uri? = null

    private val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", " ",
            "api_key", " ",
            "api_secret", " "
        )
    )

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

        if (nombre.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(requireContext(), "Faltan campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Subida de imagen en background
        CoroutineScope(Dispatchers.IO).launch {
            var imageUrl: String? = null

            imageUri?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    inputStream?.use {
                        val uploadResult = cloudinary.uploader().upload(it, ObjectUtils.emptyMap())
                        imageUrl = uploadResult["secure_url"] as String
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            withContext(Dispatchers.Main) {
                viewModel.guardarReceta(
                    nombre,
                    tiempo,
                    descripcion,
                    ingredientes,
                    pasos,
                    etiquetas,
                    link,
                    publica,
                    imageUrl
                ) { success, message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    if (success) requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}