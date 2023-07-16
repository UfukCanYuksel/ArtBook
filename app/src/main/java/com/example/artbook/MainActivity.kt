package com.example.artbook

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.artbook.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var artList : ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        artList = ArrayList<Art>() // adaptör'e göndereceğim diziyi oluşturdum. dataBaseden veri çekip doldurma zamanı

        artAdapter = ArtAdapter(artList) // adapter için nesne oluşturuyorum ve şuan boş olan diziyi yolluyorum eğer öğe eklenirse altta database işlemlerinde
        // kullandığım  artAdapter.notifyDataSetChanged() sayesinde veri diziye aktarılacak ve dizi dolacak sanat eseri ekli değilken ne göstereyim ki

        binding.recyclerView.layoutManager = LinearLayoutManager(this) // recyclerView'e adaptörü bağlayabilmek için  layoutManager oluşturuyorum
        binding.recyclerView.adapter = artAdapter // ve recyclerView'e adaptörü bağlıyorum

        try {

            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE , null) // Arts database'i varsa aç yoksa oluştur
            val cursor = database.rawQuery("SELECT * FROM arts" , null ) // içindeki arts tablosuna ulaştım
            val artNameIx = cursor.getColumnIndex("artname") // artName kolonunu bul ve değişkene ata
            val idIx = cursor.getColumnIndex("id") // id kolonunu bul ve değişkene ata

            while (cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(idIx)

                val  art = Art(name , id) // id ve name'i bir değişkene atayıyorum ve
                artList.add(art) // adaptör'e gidecek dizime ekliyorum
            }
            artAdapter.notifyDataSetChanged() // data geldikçe güncelle
            cursor.close() // bu imleci kapat

        }catch (e:Exception){
            e.printStackTrace()
        }

        // Go butonu ile yeni sanat eseri eklemeye gidicem
        binding.btnGo.setOnClickListener { //
            val intent = Intent(this@MainActivity , MainActivity2::class.java)
            intent.putExtra("info","new")// yeni sana eseri yolluycam diyorum
            startActivity(intent)
        }

    }


}