package com.example.artbook


import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.artbook.databinding.ActivityMain2Binding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception



class MainActivity2 : AppCompatActivity() {
    lateinit var binding: ActivityMain2Binding

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent> // abstract activity'i bir sonuç için başlat (galeriye gidip veri döndürecek)
    private lateinit var permissionLauncher: ActivityResultLauncher<String> // izin verildi mi verilmedi mi verisi (izin kontrolü geri döndürecek)
    var selectedBitmap :Bitmap? = null
    private lateinit var database : SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null )

        registerLauncher()

        val intent = intent // intent ile veri alacağımı söyledim
        val info = intent.getStringExtra("info") // string türündeli info isimli veriyi info değişkenine atadım
        if (info.equals("new")){ // eğer sanat eseri yeniyse
            binding.artName.setText("") // sanat eseri adını boşalt
            binding.artistName.setText("") // sanatçı adını boşalt
            binding.yearArg.setText("") // yılını boşalt ve
            binding.btnSave.visibility = View.VISIBLE // kaydet butonunu görünür kıl
            //binding.imgArt.setImageResource(R.drawable.images)

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources , R.drawable.images) //
            binding.imgArt.setImageBitmap(selectedImageBackground)

        }else{ // eski yani kayıtlıysa
            binding.btnSave.visibility = View.INVISIBLE // kayıtlı olduğu için kaydet butonu yok
            val selectedId = intent.getIntExtra("id",1) // id değerini aldım. eğer id gelmezse default 1 değerini aldım

            // intent ile gelen id değerine ulaşmak için where şartı koyuyorum.
            // o soru işaretinin selectedId ye eşit olması gerektiğini söyliycem ama benden bunu string tutan bir array ile yapmamı istediği için
            // arrayOf(selectedId.toString()) diye veriyorum
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))


            // gelen id ye göre ulaştığım eserin tüm bilgilerini almak için kolonlarını buluyorum
            val artNameIx = cursor.getColumnIndex("artname")
            val artistNameIx =cursor.getColumnIndex("artistname")
            val yearIx = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")

            // kolonlarındaki  string verileri  setText ile yazdırıyorum
            // Foroğrafı ise byte array ve bitmapFactory ile setImageBitmap diye aktarabiliyorum
            while (cursor.moveToNext()){
                binding.artName.setText(cursor.getString(artNameIx))
                binding.artistName.setText(cursor.getString(artistNameIx))
                binding.yearArg.setText(cursor.getString(yearIx))

                val byteArray = cursor.getBlob(imageIx) // cursor.getBlob bir byte dizisi geri dönecek

                // bu byte dizisini imageView'e koymak için bitmap'e çeviriyorum
                val bitmap = BitmapFactory.decodeByteArray(byteArray , 0 , byteArray.size) // offset = nerede başlayacak   , byteArray.size = nerede bitecek
                binding.imgArt.setImageBitmap(bitmap)

            }
            cursor.close() //imleci kapat

        }

    }

    fun save(view: View){ // kaydet butonuna basıldığında

        val artName = binding.artName.text.toString()
        val artistName = binding.artistName.toString()
        val year = binding.yearArg.text.toString()
        // eser ismi , artist ismi ve yılını aldım

        // bitmap kontrolü
        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!! , 300)

            // Görseli Database'lere görsel olarak kaydedemeyiz. Görseli veriye çevirmek gerekiyor
            val outputStream = ByteArrayOutputStream() //Görseli kaydetmek için Byte dizisi oluşturuyorum
            smallBitmap.compress(Bitmap.CompressFormat.PNG , 50 , outputStream) // görseli compress  metodunun format tipini küçültme oranını ve
            // OutputStream değişkenlerini veriyorum

            val byteArray = outputStream.toByteArray() //  gerekli işlem görselin 01 lere çevrilmiş oldu

            try {
               //database = this.openOrCreateDatabase("Arts", MODE_PRIVATE , null) yukarıda tanımladım zaten

                // oluşacak tablonun adını ve değişkenlerini söylüyorum.
                database.execSQL("CREATE TABLE IF NOT EXISTS arts ( id INTEGER PRIMARY KEY , artname VARCHAR , artistname VARCHAR , year VARCHAR , image BLOB )")

                // Değişkenlerin değerleri stabil olmadığı için statement metodu kullanıyorum ? leri değerlerin geleceği yerleri temsil ediyor
                val sqlString = "INSERT INTO arts ( artname , artistname , year , image) VALUES (? , ? , ? , ? )"
                val statement = database.compileStatement(sqlString)
                //statement da index 1 den başlar
                //statement.bindString 1.soru işareti ile artName değişkenini bağla diyorum
                statement.bindString(1,artName)
                statement.bindString(2,artistName)
                statement.bindString(3,year)
                //statement.bindBlob 4.soru işareti ile byteArray'i bağla değişkenini bağla diyorum
                statement.bindBlob(4,byteArray)
                statement.execute() // statement.execute demeden bağlama yapmıyor.

            }catch (e:Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@MainActivity2 ,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // bundan önce açık olan tüm activity'leri kapat
            startActivity(intent)
        }
    }
    fun selectImage(view:View){
        // SDK 33 VE SONRASI GALERİ İZİN KONTROLÜ
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU){

            // GALERİYE ERİŞİM İZNİ VERİLMEDİYESE (!=)
            if (ContextCompat.checkSelfPermission(this ,
                    android.Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED){


                // kullanıcıya hangi izni istediğimi ve mantığını gösteriyorum
                if (ActivityCompat.shouldShowRequestPermissionRationale(this , android.Manifest.permission.READ_MEDIA_IMAGES)){

                    //rationale
                    //SnackBar oluşturuyorum ve ne kadar gösterileceğini belirsiz yapıyorum (ok a basana kadar göster)
                    val snack = Snackbar.make(view , "Galeriye go", Snackbar.LENGTH_INDEFINITE)
                    //action ismi verip onClickListener da izin istiyorum
                    snack.setAction("İzin",View.OnClickListener {
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES )// hangi izni istediğimi verdim
                    })
                    snack.show()
                }else{  // göstermek zorunda değilsem direkt izin istiycem karar android de
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES) // hangi izni istediğimi verdim

                }

            }else{ // İZİN VERİLDİ DEMEK DİREKT GALERİYE GİDİP SEÇİLEN GÖRSELİN URI (yani yerini) alıyorum
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI )

                //sonuç için bunu başlat
                // bu activity'i açma amacı veri alıp geri dönmek için
                activityResultLauncher.launch(intentToGallery)

            }

        }else{ // SDK 33 ÖNCESİ İÇİN
            if (ContextCompat.checkSelfPermission(this ,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                if (ActivityCompat.shouldShowRequestPermissionRationale(this , android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                    //rationale
                    val snack = Snackbar.make(view , "Galeriye go", Snackbar.LENGTH_INDEFINITE)
                    snack.setAction("İzin",View.OnClickListener {  })
                    snack.show()
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE )

                }else{
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE )

                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                activityResultLauncher.launch(intentToGallery)

            }
        }
    }
    // görsel'i küçültüyorum. normalda olacak iş değil de burda öğreniyotuz işte
    private fun  makeSmallerBitmap(image:Bitmap , maximumSize : Int) : Bitmap{
        var width = image.width
        var height = image.height

        val  bitmapRadio : Double = width.toDouble() / height.toDouble()
        if(bitmapRadio >1 ){
            //landscape
            width = maximumSize
            val x = width*bitmapRadio
            height = x.toInt()
        }else{
            //portrait
            height = maximumSize
            val y = height*bitmapRadio
            width =  y.toInt()
        }

        return Bitmap.createScaledBitmap(image , width , height , true)
    }
    private fun registerLauncher(){

        // registerForActivityResult = hazır fonk.
        // (ActivityResultContracts.StartActivityForResult()  =>bir sonuç için activity başlat
        // {} arasında ise yapıldıktan sonra sonucunda ne olacak kısmı
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult() )  {result->

            if (result.resultCode == RESULT_OK){ // bu sonucun kodu okey mi yani kullanıcı galeride bir foto seçtimi

                val intentFromResult = result.data // result geriye nullable intent geri verdiği için bir değişkene atayıp
                if (intentFromResult != null){ // kontrol ediyorum

                    val imageData = intentFromResult.data  // intent deki datayı aldığımda nullable uri veriyor değişkene atıyorum ve

                    //binding.imgArt.setImageURI(imageData)
                    if (imageData != null){ // uri ı kontrol ediyorum

                        try { // sdk işlemleri tehlikelidir try catch yapısında yapılır

                            if (Build.VERSION.SDK_INT >=28){ // sdk 28 ve sonrası

                                // ImageDecoder.createSource = URI ı görsel yapıyoruz
                                val source = ImageDecoder.createSource( this@MainActivity2.contentResolver ,  imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source) // bitmap e aktarıyorum
                                binding.imgArt.setImageBitmap(selectedBitmap) // seçilen bitmap i XML deki İmageView'e koydum

                            }else{// SDK 27 VE ÖNCESİ

                                // direkt bitmap olarak aldık ve yine XML deki ImageView'e atadım
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this@MainActivity2.contentResolver , imageData)
                                binding.imgArt.setImageBitmap(selectedBitmap)
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        //izni isteme
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->

            if (result){

                // permission granted (izin verildiyse)
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI )
                activityResultLauncher.launch(intentToGallery) // intent istedi verdik
            }else{
                //permission denied (verilmediyse) Toast ile izine ihtiyaç var diyoruz
                Toast.makeText(this@MainActivity2 , "Permission needed!" , Toast.LENGTH_LONG).show()
            }
        }


    }


}

