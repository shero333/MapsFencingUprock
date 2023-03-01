package com.example.mapsfencing.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.mapsfencing.models.User

class PreferenceManager(context: Context) {
    var usersession: SharedPreferences
    var editor: SharedPreferences.Editor
    private val IS_LOGGED_IN = "IsLoggedIn"
    private val ID = "id"
    private val MAIL = "Mail"
    private val NAME = "Name"
    private val PHONE = "PHONE"
    private val PASSWORD = "PASSWORD"

    init {
        usersession = context.applicationContext.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        editor = usersession.edit()
    }

    fun storeuser(user: User) {
        editor.putString(ID, user.id)
        editor.putString(MAIL, user.username)
        editor.putString(NAME, user.name)
        editor.putString(PHONE, user.contact)
        editor.putString(PASSWORD, user.password)
        editor.commit()
    }
    fun storeLoginStatus() {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.commit()
    }

    fun getuser(): User {
        val user = User()
        user.id = usersession.getString(ID, null)
        user.username = usersession.getString(MAIL, null)
        user.name = usersession.getString(NAME, null)
        user.contact = usersession.getString(PHONE, null)
        user.password = usersession.getString(PASSWORD, null)
        return user
    }

    fun isLoggedIn(): Boolean {
        return usersession.getBoolean(IS_LOGGED_IN, false)
    }
    fun logout() {
        editor.clear()
        editor.commit()
    }
}