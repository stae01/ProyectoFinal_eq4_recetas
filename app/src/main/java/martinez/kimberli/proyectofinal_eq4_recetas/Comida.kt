package martinez.kimberli.proyectofinal_eq4_recetas

data class Comida(
val nombre: String ="",
val categoria: String="",
var etiquetas: List<String>? = null,
var descripcion: String = "",
var ingredientes: String = "",
var pasos: String = "",
var publica: Boolean = false,
var tiempo: String = "",
var usuarioId: String = "",
var usuarioEmail: String = "",
val imagenUrl: String = "",
var isFavorite: Boolean=false,
val link: String = "",
val fechaCreacion: Long? = null,
val id: String = "",
)

