package com.bilalkarademir.geziajandam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val artnameList = ArrayList<String>()
        val artIdList = ArrayList<Int>()

        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,artnameList)
        liste.adapter= arrayAdapter

        //Veritabanında veri çekme işlemleri

        try {

            val database = this.openOrCreateDatabase("Arts", Context.MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM arts ",null)
            val idIx = cursor.getColumnIndex("id")
            val artNameIx = cursor.getColumnIndex("artname")
            while (cursor.moveToNext()){

                artnameList.add(cursor.getString(artNameIx))
                artIdList.add(cursor.getInt(idIx))

            }
            arrayAdapter.notifyDataSetChanged()
            cursor.close()



        }catch (e:Exception){
            e.printStackTrace()
        }

        //Listeye Tıklanma işlemleri
        liste.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            val intent = Intent(this,Main2Activity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",artIdList[position])
            startActivity(intent)
        }




    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //Menüyü Aktiviteye bağlamak
        val menuInflate = menuInflater
        menuInflate.inflate(R.menu.gezi_ekle,menu)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId==R.id.gezi_ekle_item){
            val intent = Intent(this,Main2Activity::class.java)

            intent.putExtra("info","new")
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}
