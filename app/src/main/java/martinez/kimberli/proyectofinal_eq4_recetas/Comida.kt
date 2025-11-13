package martinez.kimberli.proyectofinal_eq4_recetas

data class Comida(
val nombre: String = "",
val categoria: String = "",
val etiqueta: String ="",
val imagenRes: Int = 0,
var isFavorite: Boolean = false
)

