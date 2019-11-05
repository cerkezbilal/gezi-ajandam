package com.bilalkarademir.geziajandam

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class Main2Activity : AppCompatActivity() {

    var selectedPicture : Uri? = null
    var selectedBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val intent = intent
        val info = intent.getStringExtra("info")

        if(info.equals("new")){


            var tarih:Date = Date()
            var bugun:SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
            var son:String = bugun.format(tarih)

            textView.text ="Eklenme Tarihi: $son"

            editTextName.setText("")
            editTextInfo.setText("")
            buttonKaydet.visibility=View.VISIBLE
            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.gallery)
            imageViewResimSec.setImageBitmap(selectedImageBackground)


        }else{

            buttonKaydet.visibility=View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)


            //id sine göre verileri getirmek
            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artNameIx = cursor.getColumnIndex("artname")
            val artInfoIx = cursor.getColumnIndex("artinfo ")
            val infoIX = artInfoIx+3
            val yearIX = cursor.getColumnIndex("year")
            val imageIx = cursor.getColumnIndex("image")



            while (cursor.moveToNext()){



                editTextName.setText(cursor.getString(artNameIx))
               editTextInfo.setText(cursor.getString(infoIX ))
                textView.text=cursor.getString(yearIX)

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                imageViewResimSec.setImageBitmap(bitmap)



            }
            cursor.close()


        }


        
    }

    fun save (view:View){

        val artName = editTextName.text.toString()
        val artinfo = editTextInfo.text.toString()
        val year = textView.text.toString()

        //Bitmap Gelen resmi byte dizisine çevirmeliyiz
        if(selectedBitmap !=null){
            val smallBitmap = makeSmallBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray =  outputStream.toByteArray()

            try {
                //Database oluşturuyoru sqlite
                val database = this.openOrCreateDatabase("Arts",Context.MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR,artinfo VARCHAR, year VARCHAR ,image BLOB ) ")

                val sqlString = "INSERT INTO arts (artname,artinfo,year,image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,artName)
                statement.bindString(2,artinfo)
                statement.bindString(3,year)
                statement.bindBlob(4,byteArray)

                statement.execute()
            }catch (e:Exception){
                e.printStackTrace()
            }

            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)//Hafızasındaki tüm activityleri kaldırır
            startActivity(intent)
            //finish()


        }
    }
    //Resim boyutunu küçültmek
    fun makeSmallBitmap (image:Bitmap,maximumSize:Int): Bitmap{

        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble()/height.toDouble()
        if(bitmapRatio>1)
        {
            width=maximumSize
            var scaleHeight = width/bitmapRatio
            height=scaleHeight.toInt()

        }else{
            height = maximumSize
            var scaleWidth = height*bitmapRatio
            width = scaleWidth.toInt()

        }
        return Bitmap.createScaledBitmap(image,width,height,true)

    }




    fun selectedImage(view:View){

        //İzin alma işlemi
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

            //izin yok ise
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)

            //izin var ise
        }else{

            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentToGallery,2)
        }
    }
    //İlk defa izin alıyorsa bu fonksiyon dönecek
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if(requestCode==1){
            if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){

                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentToGallery,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
        //Galeriden resim seçme ve imageview e atmak
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==2 && resultCode==Activity.RESULT_OK && data != null){
            selectedPicture = data.data
            if(selectedPicture!=null){

                if(Build.VERSION.SDK_INT >=28){
                    val source = ImageDecoder.createSource(this.contentResolver,selectedPicture!!)
                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                    imageViewResimSec.setImageBitmap(selectedBitmap)

                }else{

                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPicture)
                    imageViewResimSec.setImageBitmap(selectedBitmap)
                }

            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }



}
