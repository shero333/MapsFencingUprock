package com.example.mapsfencing.notification_services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mapsfencing.R
import org.json.JSONException
import org.json.JSONObject

class MyNotificationSender(
    private val userNotificationToken: String,
    private val title: String,
    private val body: String,
    mContext: Context?,
    mActivity: Activity
) {
    private val mActivity: Activity
    private val postUrl = "https://fcm.googleapis.com/fcm/send"
    private val fcmServerKey = "AAAAQ68CG4A:APA91bHuCvWUyigEJQmNFDafhyVYMWo6bIqe6FZM25EHYBcX0Fb08eD6Bd_xSbl3j5lPWbgFDKErXikukLRNHDEEzM9y99KR2x_l3rBCXxooGECNfQotd1QAQ2s9Hu-TbqhJ64_fsCFa"

    init {
        this.mActivity = mActivity
    }

    fun send_notification() {
        val requestQueue: RequestQueue = Volley.newRequestQueue(mActivity)
        val `object` = JSONObject()
        try {
            `object`.put("to", userNotificationToken)
            val notificationObject = JSONObject()
            notificationObject.put("title", title)
            notificationObject.put("body", body)
            notificationObject.put("icon", R.drawable.user_location_icon)
            `object`.put("notification", notificationObject)
            val request: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, postUrl, `object`,
                Response.Listener { response: JSONObject? ->
                    Log.i("NotificationException", "send_notification: ${response.toString()}")
                },
                Response.ErrorListener { error: VolleyError? ->
                    Log.i("NotificationException", "send_notification: ${error?.message}")
                }) {
                override fun getHeaders(): Map<String, String> {
                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=$fcmServerKey"
                    return header
                }
            }
            requestQueue.add(request)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("NotificationException", "send_notification: ${e.localizedMessage}")
        }
    }
}