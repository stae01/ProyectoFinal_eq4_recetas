package martinez.kimberli.proyectofinal_eq4_recetas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Clase abstracta que define la base de datos de la aplicación utilizando Room.
 * Contiene las entidades de la base de datos y los DAOs para acceder a ellas.
 */
@Database(entities = [Usuario::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Proporciona el DAO para la entidad [Usuario].
     * @return Una instancia de [UsuarioDao].
     */
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos. Si no existe, la crea.
         * @param context El contexto de la aplicación.
         * @return La instancia de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recetas_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
