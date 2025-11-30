package martinez.kimberli.proyectofinal_eq4_recetas

import com.google.firebase.database.Exclude

data class Comida(
    var id: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val ingredientes: String? = null,
    val preparacion: String? = null,
    val pasos: String? = null, 
    val tiempo: String? = null,
    val categoria: String? = null,
    val etiquetas: List<String>? = null,
    val link: String? = null,
    var imagenUrl: String? = null,
    val usuarioId: String? = null,
    val publica: Boolean? = null,
    val usuarioEmail: String? = null,
    val fechaCreacion: Long? = null
) {
    @get:Exclude
    var isFavorite: Boolean = false
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null)
}