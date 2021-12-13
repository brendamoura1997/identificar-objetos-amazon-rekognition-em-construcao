package com.example.validaimagemapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import org.json.JSONObject
import org.json.JSONTokener
import java.io.ByteArrayOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var btn: Button
    private lateinit var txv: TextView
    private lateinit var textView: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var textView4: TextView
    private lateinit var textView5: TextView
    private lateinit var textView6: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById<Button>(R.id.btnCarregarImagem);
        txv = findViewById<TextView>(R.id.txvSelecione);
        textView = findViewById<TextView>(R.id.textView);
        textView2 = findViewById<TextView>(R.id.textView2);
        textView3 = findViewById<TextView>(R.id.textView3);
        textView4 = findViewById<TextView>(R.id.textView4);
        textView5 = findViewById<TextView>(R.id.textView5);
        textView6 = findViewById<TextView>(R.id.textView6);

        btn.setOnClickListener{pickImage()}
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2) {

            if (android.os.Build.VERSION.SDK_INT >= 29){
                // To handle deprication use
                val source = ImageDecoder.createSource(this.contentResolver, data!!.data!!)
                val bmp = ImageDecoder.decodeBitmap(source)
                var output = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, output)
                val imgData = output.toByteArray()
                analizeImage(Base64.encodeToString(imgData, Base64.NO_WRAP))
            } else{
                // Use older version
                MediaStore.Images.Media.getBitmap(contentResolver, data!!.data!!)
            }

        }
    }

    private fun analizeImage(imgData: String) {
        btn.isEnabled = false
        btn.setBackgroundColor(Color.GRAY)
        txv.setText("Analisando imagem. Aguarde...")

        Fuel.post("#") //path oculto para proteção
            .header("Content-Type", "application/json")
            .jsonBody("{\"image\": \"$imgData\"}")
            .responseString { result ->
                when (result) {
                    is Result.Success -> {
                        validateImage(result.get())
                    }
                    is Result.Failure -> {
                        Log.d("App", result.getException().toString())
                    }
                }
            }
    }

    private fun validateImage(results: String) {
        val json = JSONObject(results)
        val data = json.getJSONObject("data")
        val labels = data.getJSONArray("Labels")
        var isValid = false

        //println(data.getJSONArray("Labels"))

        println("PASSEI1")

        for (i in 0 until labels.length()) {
            val item = labels.getJSONObject(i)


            //textView.setText()
            println("PASSEI2")
             println("ITEM: "+item.getJSONArray("Parents"))
            var pegaNome = item.getJSONArray("Parents");
            var pegaNomeString = pegaNome.toString();
            val delim = ":"

            val arr = pegaNomeString.split(delim)
            println(arr)




            //println(item.getString("Parents"))  // PLANT 2 RESULTADOS
             println(item.getString("Name"))      // RETORNA ROSE 1 RESULTADO

           //if (item.getString("Name") in arrayOf("")) {
                if (item.getDouble("Confidence") > 90.0) {
                    //Log.d("resultados" ,"resultados"+item.getString("Name"))
                    //textView.setText(item.getString("Name"))

                   // var testeArray = item.getString("Name")
                    //println(testeArray)


                    isValid = true
                    break
                }
            //}
        }



        runOnUiThread(Runnable {
            btn.isEnabled = true
            btn.setBackgroundColor(Color.BLUE)

            if (isValid) {
                txv.setText("A imagem é válida")
            } else {
                txv.setText("A imagem é inválida")
            }
        })
    }
}