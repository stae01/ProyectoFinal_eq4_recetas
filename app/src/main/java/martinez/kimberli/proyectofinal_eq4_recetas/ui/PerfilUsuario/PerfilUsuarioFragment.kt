package martinez.kimberli.proyectofinal_eq4_recetas.ui.PerfilUsuario

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentPerfilBinding
import martinez.kimberli.proyectofinal_eq4_recetas.iniciar_sesion.iniciar_sesion
import java.util.Calendar

class PerfilUsuarioFragment : Fragment()  {

    private var _binding: FragmentPerfilBinding? = null
    private lateinit var etFechaNacimiento: EditText

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val perfilUsurarioViewModel =
            ViewModelProvider(this).get(PerfilUsurarioViewModel::class.java)

        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
        val db = FirebaseFirestore.getInstance()

        currentUser?.let { user ->
            val userRef = db.collection("users").document(user.uid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    etNombre.setText(document.getString("nombre"))
                    etFechaNacimiento.setText(document.getString("fechaNacimiento"))
                    etGenero.setText(document.getString("genero"))
                    etCorreo.setText(user.email)
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

        binding.btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevaFecha = etFechaNacimiento.text.toString().trim()
            val nuevoGenero = etGenero.text.toString().trim()

            if (nuevoNombre.isNotEmpty() && nuevaFecha.isNotEmpty() && nuevoGenero.isNotEmpty()) {
                val userUpdates = hashMapOf(
                    "nombre" to nuevoNombre,
                    "fechaNacimiento" to nuevaFecha,
                    "genero" to nuevoGenero
                )

                currentUser?.let { user ->
                    db.collection("users").document(user.uid)
                        .update(userUpdates as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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