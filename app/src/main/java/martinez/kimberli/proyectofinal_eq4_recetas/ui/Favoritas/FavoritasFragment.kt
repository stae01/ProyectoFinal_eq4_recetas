package martinez.kimberli.proyectofinal_eq4_recetas.ui.Favoritas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentFavoritasBinding

class FavoritasFragment : Fragment() {

    private var _binding: FragmentFavoritasBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val favoritasViewModel =
            ViewModelProvider(this).get(FavoritasViewModel::class.java)

        _binding = FragmentFavoritasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.tituloFavoritos
        favoritasViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}