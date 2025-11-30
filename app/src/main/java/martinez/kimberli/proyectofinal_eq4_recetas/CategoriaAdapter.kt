package martinez.kimberli.proyectofinal_eq4_recetas

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class CategoriaAdapter (
    private val categorias: List<Categoria>,
    private val onClick: (Categoria?) -> Unit
) : RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class CategoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.categoria_img)
        val nombre: TextView = view.findViewById(R.id.nombre_categoria)
        val cardView: CardView = view.findViewById(R.id.card_view_categoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categorias, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorias[position]
        holder.nombre.text = categoria.nombre
        holder.img.setImageResource(categoria.iconoRes)

        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
            holder.nombre.setTextColor(Color.WHITE)
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE)
            holder.nombre.setTextColor(Color.BLACK)
        }

        holder.itemView.setOnClickListener {
            if (selectedPosition == position) {
                selectedPosition = RecyclerView.NO_POSITION
                onClick(null)
            } else {
                selectedPosition = position
                onClick(categoria)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = categorias.size
}
