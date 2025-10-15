package martinez.kimberli.proyectofinal_eq4_recetas.ui.PerfilUsuario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PerfilUsurarioViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is perfil usuario Fragment"
    }
    val text: LiveData<String> = _text
}