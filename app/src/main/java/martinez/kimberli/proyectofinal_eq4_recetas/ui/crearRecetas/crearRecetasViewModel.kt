package martinez.kimberli.proyectofinal_eq4_recetas.ui.crearRecetas

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class crearRecetasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun guardarReceta(
        nombre: String,
        tiempo: String,
        descripcion: String,
        ingredientes: String,
        pasos: String,
        etiquetas: List<String>,
        link: String?,
        publica: Boolean,
        imageUrl: String?,
        callback: (Boolean, String) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            callback(false, "Usuario no autenticado")
            return
        }

        val receta = hashMapOf(
            "nombre" to nombre,
            "tiempo" to tiempo,
            "descripcion" to descripcion,
            "ingredientes" to ingredientes,
            "pasos" to pasos,
            "etiquetas" to etiquetas,
            "link" to link,
            "publica" to publica,
            "imagenUrl" to imageUrl,
            "usuarioId" to user.uid,
            "usuarioEmail" to user.email,
            "id" to UUID.randomUUID().toString()
        )

        db.collection("recetas")
            .add(receta)
            .addOnSuccessListener { callback(true, "Receta guardada correctamente") }
            .addOnFailureListener { e -> callback(false, "Error: ${e.message}") }
    }
}