package com.example.artbook


import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artbook.databinding.RowBinding


class ArtAdapter(val artList: ArrayList<Art>):RecyclerView.Adapter<ArtAdapter.ArtHolder>() {
    class ArtHolder ( val binding: RowBinding):RecyclerView.ViewHolder(binding.root ){
        // Önemliiii Adapter için tasarladığım Layout altında ki row isimli dosya yüzünden RowBinding oldu adı
        // aliVeli olsa AliVeliBinding
    }

    //bir görünüm return etmemi istiyor ben de tasarladığım row layout'unu vericem
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        // ArtHolder class'ında tanımladığım view'e Inflater tanımlıyorum ve onBindViewHolder da kullanılmak üzere return ediyorum
        val  binding = RowBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return ArtHolder(binding )
    }

    override fun getItemCount(): Int {
        return artList.size
    }

    // row adında ki layout'uma inflater ile bağlanmış durumdayım şimdi kullanıcının girdiği değerleri alıp gerekli işlemleri yapmam lazım
    override fun onBindViewHolder(holder: ArtHolder, position: Int) {

        holder.binding.tvRecyclerView.text = artList[position].name

        //hangi isme tıklandığını otomatik anlıyor.
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context , MainActivity2::class.java)
            intent.putExtra("info","old")// recyclerView'de ismi görünen sanat eserine tıklandığında  old yani eski(kayıtlı ) olduğunu söylüyorum
            intent.putExtra("id", artList[position].id) // id bilgisini gönderiyorum
            holder.itemView.context.startActivity(intent)
        }

    }

}