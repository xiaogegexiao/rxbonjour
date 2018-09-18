package com.android.sample

import android.os.Bundle
import android.util.Log
import com.trello.rxlifecycle2.android.ActivityEvent
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import com.xiao.rxbonjour.RxBonjour

class MainActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        RxBonjour.startDiscovery(this, "_http._tcp.")
                .compose(bindUntilEvent(ActivityEvent.PAUSE))
                .subscribe({ it ->
                    Log.d("NSD", "received new device ${it.serviceName}")
                }, {
                    Log.e("NSD", it.message, it)
                })
    }
}
