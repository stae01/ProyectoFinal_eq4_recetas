package martinez.kimberli.proyectofinal_eq4_recetas.iniciar_sesion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import martinez.kimberli.proyectofinal_eq4_recetas.MainActivity
import martinez.kimberli.proyectofinal_eq4_recetas.R

class iniciar_sesion: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)


            var button: Button= findViewById(R.id.btninicioSesion)

            button.setOnClickListener {
                var intento= Intent(this, MainActivity::class.java)
                this.startActivity(intento)
            }
        }

}