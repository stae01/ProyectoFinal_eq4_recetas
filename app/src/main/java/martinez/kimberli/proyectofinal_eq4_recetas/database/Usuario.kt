package martinez.kimberli.proyectofinal_eq4_recetas.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Clase de datos que representa un usuario en la base de datos.
 * Cada usuario tiene un ID único, nombre completo, fecha de nacimiento, correo electrónico,
 * contraseña y género.
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val nombreCompleto: String,
    val fechaNacimiento: String,
    val correoElectronico: String,
    val contrasena: String,
    val genero: String
)
