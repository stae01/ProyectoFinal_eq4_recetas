package martinez.kimberli.proyectofinal_eq4_recetas.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Interfaz DAO (Data Access Object) para la entidad [Usuario].
 * Define los métodos para interactuar con la tabla de usuarios en la base de datos.
 */
@Dao
interface UsuarioDao {
    /**
     * Inserta un nuevo usuario en la base de datos. Si el usuario ya existe (basado en la clave primaria), lo reemplaza.
     * @param usuario El objeto [Usuario] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: Usuario)

    /**
     * Obtiene un usuario de la base de datos por su correo electrónico.
     * @param correo El correo electrónico del usuario a buscar.
     * @return El objeto [Usuario] si se encuentra, o `null` si no existe.
     */
    @Query("SELECT * FROM usuarios WHERE correoElectronico = :correo LIMIT 1")
    suspend fun getUsuarioByEmail(correo: String): Usuario?
}
