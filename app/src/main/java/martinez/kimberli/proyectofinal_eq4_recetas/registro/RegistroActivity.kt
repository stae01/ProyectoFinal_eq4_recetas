package martinez.kimberli.proyectofinal_eq4_recetas.registro

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import martinez.kimberli.proyectofinal_eq4_recetas.R
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Actividad para el registro de nuevos usuarios.
 * Permite a los usuarios ingresar sus datos personales, validar el formulario
 * y guardar la información en una base de datos local.
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var btnRegister: AppCompatButton
    private lateinit var btnBack: AppCompatButton
    private lateinit var ivAvatar: ImageView
    private lateinit var ivAddPhoto: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    /**
     * Se llama cuando la actividad es creada por primera vez.
     * Inicializa la vista, configura el selector de fecha, el spinner de género y los botones.
     * @param savedInstanceState Si la actividad se está recreando, este Bundle contiene los datos
     *                           que se suministraron más recientemente en onSaveInstanceState(Bundle).
     *                           De lo contrario, es nulo.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews()
        setupDatePicker()
        setupGeneroSpinner()
        setupButtons()
    }

    /**
     * Inicializa todas las vistas de la actividad obteniendo sus referencias por ID.
     */
    private fun initViews() {
        etName = findViewById(R.id.etName)
        etBirthDate = findViewById(R.id.etBirthDate)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        spinnerGender = findViewById(R.id.spinnerGender)
        btnRegister = findViewById(R.id.btnRegister)
        btnBack = findViewById(R.id.btnBack)
        ivAvatar = findViewById(R.id.ivAvatar)
        ivAddPhoto = findViewById(R.id.ivAddPhoto)
    }

    /**
     * Configura el selector de fecha para el campo de fecha de nacimiento.
     * Abre un DatePickerDialog al hacer clic en el campo.
     */
    private fun setupDatePicker() {
        etBirthDate.isFocusable = false
        etBirthDate.isClickable = true
        
        etBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val fecha = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                    etBirthDate.setText(fecha)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    /**
     * Configura el spinner para la selección de género con opciones predefinidas.
     */
    private fun setupGeneroSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerGender.adapter = adapter
        }
    }

    /**
     * Configura los listeners de clic para los botones de registro y retroceso.
     */
    private fun setupButtons() {
        btnRegister.setOnClickListener {
            if (validarFormulario()) {
                crearCuenta()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Valida los campos del formulario de registro.
     * Muestra mensajes de error si algún campo no cumple con los requisitos.
     * @return `true` si el formulario es válido, `false` en caso contrario.
     */
    private fun validarFormulario(): Boolean {
        val nombre = etName.text.toString().trim()
        val fecha = etBirthDate.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val contrasena = etPassword.text.toString()
        val confirmarContrasena = etConfirmPassword.text.toString()

        when {
            nombre.isEmpty() -> {
                etName.error = "Ingrese su nombre completo"
                etName.requestFocus()
                return false
            }
            fecha.isEmpty() -> {
                Toast.makeText(this, "Seleccione su fecha de nacimiento", Toast.LENGTH_SHORT).show()
                return false
            }
            correo.isEmpty() -> {
                etEmail.error = "Ingrese su correo electrónico"
                etEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                etEmail.error = "Ingrese un correo válido"
                etEmail.requestFocus()
                return false
            }
            contrasena.isEmpty() -> {
                etPassword.error = "Ingrese una contraseña"
                etPassword.requestFocus()
                return false
            }
            contrasena.length < 6 -> {
                etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                etPassword.requestFocus()
                return false
            }
            confirmarContrasena.isEmpty() -> {
                etConfirmPassword.error = "Confirme su contraseña"
                etConfirmPassword.requestFocus()
                return false
            }
            contrasena != confirmarContrasena -> {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                etConfirmPassword.requestFocus()
                return false
            }
            spinnerGender.selectedItemPosition == 0 -> { 
                Toast.makeText(this, "Seleccione su género", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    /**
     * Intenta crear una nueva cuenta de usuario.
     * Guarda el usuario en la base de datos si el correo electrónico no está ya registrado.
     * Muestra un mensaje de éxito o error.
     */
    private fun crearCuenta() {
        val nombre = etName.text.toString().trim()
        val fecha = etBirthDate.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val contrasena = etPassword.text.toString()
        val genero = spinnerGender.selectedItem.toString()

        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userMap = hashMapOf(
                            "nombreCompleto" to nombre,
                            "fechaNacimiento" to fecha,
                            "correoElectronico" to correo,
                            "genero" to genero
                        )
                        firestore.collection("users").document(it.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@RegistroActivity,
                                    "Cuenta creada exitosamente para $nombre",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@RegistroActivity,
                                    "Error al guardar datos adicionales: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this@RegistroActivity,
                        "Error al crear cuenta: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
