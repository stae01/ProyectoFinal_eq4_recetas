package martinez.kimberli.proyectofinal_eq4_recetas.iniciar_sesion

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import martinez.kimberli.proyectofinal_eq4_recetas.MainActivity
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.registro.RegistroActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class iniciar_sesion: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

         auth = Firebase.auth

        val email = findViewById<EditText>(R.id.correo_login)
        val password = findViewById<EditText>(R.id.password_login)
        val tvError = findViewById<TextView>(R.id.tvError)
        var button: Button = findViewById(R.id.btninicioSesion)
        var registrar: TextView = findViewById(R.id.tvRegistrarseLogin)

        tvError.visibility = View.INVISIBLE

        button.setOnClickListener {

            if (email.text.isNotEmpty() && password.text.isNotEmpty()) {
                login(email.text.toString(), password.text.toString())
            } else {
                showError("Campos vacios", true)
            }

        }
        registrar.setOnClickListener {
            val intento= Intent(this, RegistroActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intento)
        }

    }

        fun login(email: String, password: String) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("INFO", "signInWithEmail:success")
                        val user = auth.currentUser
                        showError(visibility = false)
                        goToMain(user!!)

                    } else {
                        showError("Correo o contraseña incorrectos", true)
                    }

                }
        }

        fun showError(text: String = "", visibility: Boolean) {

            val tvError = findViewById<TextView>(R.id.tvError)
            tvError.text = text
            tvError.visibility = if (visibility) View.VISIBLE else View.INVISIBLE

        }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            goToMain(currentUser)
        }

    }

    fun goToMain(user: FirebaseUser) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", user.email)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }
}