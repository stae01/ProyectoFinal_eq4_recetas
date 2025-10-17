package martinez.kimberli.proyectofinal_eq4_recetas.ui.crearRecetas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentCrearRecetaBinding

class crearRecetasFragment : Fragment() {

    private var _binding: FragmentCrearRecetaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val crearRecetasViewModel =
            ViewModelProvider(this).get(crearRecetasViewModel::class.java)

        _binding = FragmentCrearRecetaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.tituloAgregar
        crearRecetasViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}