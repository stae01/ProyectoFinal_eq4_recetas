package martinez.kimberli.proyectofinal_eq4_recetas.iniciar_sesion

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import martinez.kimberli.proyectofinal_eq4_recetas.MainActivity
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.registro.RegistroActivity

class iniciar_sesion: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etEmailLogin: EditText
    private lateinit var etPasswordLogin: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        etEmailLogin = findViewById(R.id.correo_login)
        etPasswordLogin = findViewById(R.id.password_login)

        val btnInicioSesion: Button = findViewById(R.id.btninicioSesion)

        btnInicioSesion.setOnClickListener {
            val email = etEmailLogin.text.toString().trim()
            val password = etPasswordLogin.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa tu correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

            val tvRegistrarse: TextView = findViewById(R.id.tvRegistrarseLogin)
            tvRegistrarse.setOnClickListener {
                val intent = Intent(this, RegistroActivity::class.java)
                startActivity(intent)
            }
        }

}
