package martinez.kimberli.proyectofinal_eq4_recetas.registro

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import martinez.kimberli.proyectofinal_eq4_recetas.R
import martinez.kimberli.proyectofinal_eq4_recetas.MainActivity
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import android.view.View
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

import martinez.kimberli.proyectofinal_eq4_recetas.ImageCloudinary


class RegistroActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var etName: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var spinnerGender: Spinner
    private lateinit var btnRegister: Button
    private lateinit var btnBack: Button
    private lateinit var ivAvatar: ImageView
    private lateinit var ivAddPhoto: ImageView
    private lateinit var tvError: TextView

    private var selectedImageUri: Uri? = null
    private var uploadedPhotoUrl: String? = null
    private lateinit var cloudinary: ImageCloudinary


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)
        auth = Firebase.auth
        cloudinary = ImageCloudinary()
        cloudinary.initCloudinary(this)


        initViews()
        setupDatePicker()
        setupGeneroSpinner()
        setupImagePicker()
        setupButtons()
    }

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
        tvError = findViewById(R.id.tvError)

    }

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

    private fun setupButtons() {
        btnRegister.setOnClickListener {
            if (validarFormulario()) {
                crearCuentaConFirebase()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivAvatar.setImageURI(it)
            cloudinary.uploadImage(it,true) { success, url ->
                runOnUiThread {
                    if (success && url != null) {
                        uploadedPhotoUrl = url
                    } else {
                        uploadedPhotoUrl = null
                        Toast.makeText(this, "Error subiendo foto. Puedes intentarlo otra vez o continuar sin foto.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupImagePicker() {
        ivAddPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        ivAvatar.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }
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
            spinnerGender.selectedItemPosition == 0 -> { // Assuming "Género" is the first item
                Toast.makeText(this, "Seleccione su género", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun crearCuentaConFirebase() {
        val nombre = etName.text.toString().trim()
        val fecha = etBirthDate.text.toString().trim()
        val correo = etEmail.text.toString().trim()
        val contrasena = etPassword.text.toString()
        val genero = spinnerGender.selectedItem.toString()

        tvError.visibility = View.INVISIBLE
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: return@addOnCompleteListener

                    val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)

                    if (!uploadedPhotoUrl.isNullOrEmpty()) {
                        profileUpdatesBuilder.setPhotoUri(Uri.parse(uploadedPhotoUrl))
                    }

                    val profileUpdates = profileUpdatesBuilder.build()

                    user.updateProfile(profileUpdates).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            val usuario = Usuario(
                                uid = uid,
                                nombreCompleto = nombre,
                                fechaNacimiento = fecha,
                                correoElectronico = correo,
                                genero = genero,
                                fotoPerfilUrl = uploadedPhotoUrl ?: ""
                            )
                            val db = FirebaseDatabase.getInstance()
                            db.reference.child("usuarios").child(uid)
                                .setValue(usuario)
                                .addOnSuccessListener {
                                Toast.makeText(
                                this,
                                "Cuenta creada exitosamente para $nombre",
                                Toast.LENGTH_LONG
                            ).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                                 }
                                .addOnFailureListener { e ->
                                    tvError.text = "Error al guardar datos: ${e.message}"
                                    tvError.visibility = View.VISIBLE
                                }

                        } else {
                            tvError.text = "Error al actualizar el perfil."
                            tvError.visibility = View.VISIBLE
                        }
                    }
                } else {
                    tvError.text = task.exception?.message ?: "El registro ha fallado."
                    tvError.visibility = View.VISIBLE
                }
            }
    }
}


data class Usuario(
    val uid: String = "",
    val nombreCompleto: String,
    val fechaNacimiento: String,
    val correoElectronico: String,
    val genero: String,
    val fotoPerfilUrl: String = ""

)
