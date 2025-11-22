package martinez.kimberli.proyectofinal_eq4_recetas.ui.crearRecetas

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.util.UUID

class crearRecetasViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun guardarRecetaEnFirebase(
        recetaMap: HashMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        val db = FirebaseDatabase.getInstance()
        db.reference.child("recetas").push().setValue(recetaMap)
            .addOnSuccessListener { callback(true, "Receta guardada correctamente") }
            .addOnFailureListener { callback(false, "Error: ${it.message}") }
    }
}