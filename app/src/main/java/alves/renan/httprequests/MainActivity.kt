package alves.renan.httprequests

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var ip: String = ""
    private var word: String = ""
    private lateinit var receiver: BroadcastReceiver
    private var extras = false
    private val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    ip = intent.getStringExtra("ip")
                    word = intent.getStringExtra("word")

                    extras = true
                }

                btnRequest.performClick()
            }
        }

        val filter = IntentFilter()
        filter.addAction("http.request.EXTRAS")

        registerReceiver(receiver, filter)

        btnRequest.setOnClickListener { makeRequest() }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun makeRequest() {
        if (!extras) {
            ip = txtInputIp.text.toString()
            word = txtInputWord.text.toString()
        } else {
            txtInputIp.setText(ip)
            txtInputWord.setText(word)
        }

        txtResponse.text = "Counting...."
        toast("Making the http request...")

        doAsync {
            val initialTime = System.currentTimeMillis()

            val data = FormBody.Builder()
                    .add("word", word)

            val request = Request.Builder()
                    .url("http://$ip:8080")
                    .post(data.build())
                    .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val finalTime = System.currentTimeMillis() - initialTime

                    val totalTime = SimpleDateFormat("mm:ss.SSS").format(Date(finalTime))

                    uiThread {
                        txtResponse.text = "${response.body()?.string()} matches for the word \'$word\'. The time was: $totalTime"
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Call", "$call")
                    Log.d("Failed", "Error")
                }
            })

        }
    }
}
