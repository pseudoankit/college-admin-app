package com.android.collegeadminapp.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class MyVolleySingleton private constructor(context: Context) {
    private var requestQueue: RequestQueue?
    private val ctx = context
    private fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.applicationContext)
        }
        return requestQueue
    }

    fun <T> addToRequestQueue(req: Request<T>?) {
        getRequestQueue()!!.add<T>(req)
    }

    companion object {
        private var instance: MyVolleySingleton? = null
        @Synchronized
        fun getInstance(context: Context): MyVolleySingleton? {
            if (instance == null) {
                instance = MyVolleySingleton(context)
            }
            return instance
        }
    }

    init {
        requestQueue = getRequestQueue()
    }
}