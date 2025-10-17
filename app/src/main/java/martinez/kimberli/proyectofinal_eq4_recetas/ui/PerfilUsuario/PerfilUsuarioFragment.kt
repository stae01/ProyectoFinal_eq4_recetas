package martinez.kimberli.proyectofinal_eq4_recetas.ui.PerfilUsuario

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentPerfilBinding
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

        etFechaNacimiento.setOnClickListener {
            mostrarDatePicker()
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