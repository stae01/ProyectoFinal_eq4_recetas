package martinez.kimberli.proyectofinal_eq4_recetas

import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import android.net.Uri
import android.util.Log
class ImageCloudinary {
    private val CLOUD_NAME = "deoqbjysb"


    fun initCloudinary(context: Context) {
        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = CLOUD_NAME
        MediaManager.init(context, config)
    }
    fun uploadImage(imageUri: Uri,isProfilePhoto: Boolean, callback: (Boolean, String?) -> Unit) {
        val UPLOAD_PRESET = if (isProfilePhoto) "perfil" else "recetas"
        MediaManager.get()
        MediaManager.get().upload(imageUri).unsigned(UPLOAD_PRESET).callback(object : UploadCallback {
            override fun onStart(requestId: String?) {
                Log.i("CLOUDINARY", "Iniciando subida...")
            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                Log.i("CLOUDINARY", "Progreso: $bytes / $totalBytes")
            }

            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                val url = (resultData?.get("secure_url") ?: "") as String
                Log.i("CLOUDINARY", "Imagen subida: $url")
                callback(true, url)
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                Log.e("CLOUDINARY", "Error: ${error?.description}")
                callback(false, null)
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                Log.i("CLOUDINARY", "Reprogramado")
            }
        }).dispatch()
    }
}