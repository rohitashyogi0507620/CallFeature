package com.techsoft.callfeature

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.techsoft.callfeature.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var active = MutableLiveData<Boolean>(false)
    var mobilenumber: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.testBtnCall.setOnClickListener {

            mobilenumber = binding.testEdMobilenumber.text.toString().trim()
            multiPermissionCallback.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE
                )
            )

        }

        active.observe(this, Observer {
            if (it) {
                val handler = Handler()
                handler.postDelayed({

                    var sessionManager = SessionManager()
                    sessionManager!!.setContext("RMCALLDATA", applicationContext)
                    var phonenumber = sessionManager.getData("Number")
                    var duration = sessionManager.getData("Duration")
                    var type = sessionManager.getData("Type")
                    var time = sessionManager.getData("Time")

                    if (phonenumber != null && !phonenumber.isEmpty()) {

                        phonenumber = phonenumber!!.replace(" ", "")
                        if (phonenumber!!.contains("+91")) {
                            phonenumber = phonenumber.replace("+91", "")
                        }
                        Log.d(
                            "MobileNumber",
                            "History Number : $phonenumber Dial Number $mobilenumber"
                        )

                        if (!mobilenumber.isEmpty() && phonenumber.equals(mobilenumber)) {
                            binding.testTxtCalldata.setText("Number $phonenumber \n Duration : $duration \n Type :$type \n Time $time")
                        } else {
                            binding.testTxtCalldata.setText("")
                        }
                    } else {
                        binding.testTxtCalldata.setText("")
                    }
                    binding.progessBar.visibility = View.GONE

                }, 3000)

            }


        })




    }

    private val multiPermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            //handle individual results if desired
            map.entries.forEach { entry ->
                when (entry.key) {

                }
            }
            binding.progessBar.visibility = View.VISIBLE
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$mobilenumber")
            startActivity(callIntent)

        }


    override fun onStart() {
        super.onStart()
        active.postValue(true)
    }

    override fun onStop() {
        super.onStop()
        active.postValue(false)
    }

}