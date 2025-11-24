package martinez.kimberli.proyectofinal_eq4_recetas

data class Comida(
val nombre: String,
val categoria: String,
val etiqueta: String,
var descripcion: String = "",
var ingredientes: String = "",
var pasos: String = "",
var publica: Boolean = false,
var tiempo: String = "",
var usuarioId: String = "",
var usuarioEmail: String = "",
val imagenRes: Int,
var isFavorite: Boolean,
    var imagenUrl: String = "",
var etiquetas: List<String> = listOf()
)

