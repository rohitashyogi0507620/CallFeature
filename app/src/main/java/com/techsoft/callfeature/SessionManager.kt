package com.techsoft.callfeature

import android.content.Context
import android.content.SharedPreferences

class SessionManager {

    lateinit var componentName: String
    lateinit var context: Context
    lateinit var sharedPreferences: SharedPreferences

    fun setContext(componentName: String, context: Context) {
        this.componentName = componentName
        this.context = context
    }

    fun saveData(query: String?, value: String?) {
        sharedPreferences = context!!.getSharedPreferences(componentName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(query, value)
        editor.apply()
    }

    fun getData(query: String?): String? {
        sharedPreferences = context!!.getSharedPreferences(componentName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(query, null)
    }


}