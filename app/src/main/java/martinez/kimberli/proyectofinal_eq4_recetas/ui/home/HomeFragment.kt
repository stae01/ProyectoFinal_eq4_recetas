package martinez.kimberli.proyectofinal_eq4_recetas.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import martinez.kimberli.proyectofinal_eq4_recetas.Categorias.Categoria
import martinez.kimberli.proyectofinal_eq4_recetas.Categorias.CategoriaAdapter
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var categoriaAdapter: CategoriaAdapter

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        categoriaAdapter = CategoriaAdapter(categorias) { categoria ->

        }

        binding.categoriasRecycler.apply {
            adapter = categoriaAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
        return root


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val categorias = listOf(
        Categoria("Desayuno", R.drawable.ic_desayuno),
        Categoria("Mexicana", R.drawable.ic_mexicana),
        Categoria("Asiática", R.drawable.ic_asiatica),
        Categoria("Postres", R.drawable.ic_postres),
        Categoria("Mediterránea", R.drawable.ic_mediterranea),
        Categoria("Americana", R.drawable.ic_americana)
    )

}