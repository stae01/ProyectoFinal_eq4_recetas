package martinez.kimberli.proyectofinal_eq4_recetas.ui.PerfilUsuario

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import martinez.kimberli.proyectofinal_eq4_recetas.ImageCloudinary
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentPerfilBinding
import martinez.kimberli.proyectofinal_eq4_recetas.iniciar_sesion.iniciar_sesion
import java.util.Calendar

class PerfilUsuarioFragment : Fragment()  {

    private var _binding: FragmentPerfilBinding? = null
    private lateinit var etFechaNacimiento: EditText

    private var imageUri: Uri? = null
    private lateinit var imageCloudinary: ImageCloudinary  // Se maneja Cloudinary

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imgFotoPerfil.setImageURI(it)
        }
    }

    // Esta propiedad es solo válida entre onCreateView y onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val perfilUsurarioViewModel =
            ViewModelProvider(this).get(PerfilUsurarioViewModel::class.java)

        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        imageCloudinary = ImageCloudinary()
        imageCloudinary.initCloudinary(requireContext())

        val textView: TextView = binding.textperfil
        perfilUsurarioViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        etFechaNacimiento = view.findViewById(R.id.etFechaNacimiento)

        super.onViewCreated(view, savedInstanceState)

        val etNombre: EditText = view.findViewById(R.id.etNombre)
        val etGenero: EditText = view.findViewById(R.id.etGenero)
        val etCorreo: EditText = view.findViewById(R.id.etCorreo)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseDatabase.getInstance().getReference("usuarios")

        currentUser?.let { user ->
            val userRef = db.child(user.uid)

            // Obtener datos del usuario
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    etNombre.setText(snapshot.child("nombreCompleto").getValue(String::class.java))
                    etFechaNacimiento.setText(snapshot.child("fechaNacimiento").getValue(String::class.java))
                    etGenero.setText(snapshot.child("genero").getValue(String::class.java))
                    etCorreo.setText(user.email)

                    // Cargar foto de perfil si existe
                    val fotoUrl = snapshot.child("fotoUrl").value?.toString()
                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(fotoUrl)
                            .into(binding.imgFotoPerfil)
                    }


                } else {
                    Toast.makeText(requireContext(), "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error al cargar los datos del usuario", Toast.LENGTH_SHORT).show()
            }
        }

        etFechaNacimiento.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnCambiarFoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }


        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevaFecha = etFechaNacimiento.text.toString().trim()
            val nuevoGenero = etGenero.text.toString().trim()

            if (nuevoNombre.isNotEmpty() && nuevaFecha.isNotEmpty() && nuevoGenero.isNotEmpty()) {
                if (imageUri != null) {
                    imageCloudinary.uploadImage(
                        imageUri!!,
                        isProfilePhoto = true
                    ) { success, url ->
                        if (success && url != null) {
                            // Actualizar Firebase con los nuevos datos, incluyendo la imagen
                            val userUpdates = mutableMapOf<String, Any>(
                                "nombreCompleto" to nuevoNombre,
                                "fechaNacimiento" to nuevaFecha,
                                "genero" to nuevoGenero,
                                "fotoUrl" to url // Actualizar la URL de la imagen
                            )

                            currentUser?.let { user ->
                                db.child(user.uid).updateChildren(userUpdates)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(requireContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Si no se cambió la imagen, solo actualiza la información sin la foto
                    val userUpdates = mutableMapOf<String, Any>(
                        "nombreCompleto" to nuevoNombre,
                        "fechaNacimiento" to nuevaFecha,
                        "genero" to nuevoGenero
                    )

                    currentUser?.let { user ->
                        db.child(user.uid).updateChildren(userUpdates)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
            
      
        binding.btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), iniciar_sesion::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                etFechaNacimiento.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}