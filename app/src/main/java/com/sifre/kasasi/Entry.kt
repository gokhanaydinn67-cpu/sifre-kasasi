package com.sifre.kasasi

import org.json.JSONObject

data class Entry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val note: String = "",
    val favorite: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("username", username)
        put("password", password)
        put("note", note)
        put("favorite", favorite)
    }

    companion object {
        fun fromJson(o: JSONObject) = Entry(
            id = o.getString("id"),
            title = o.getString("title"),
            username = o.getString("username"),
            password = o.getString("password"),
            note = o.optString("note", ""),
            favorite = o.optBoolean("favorite", false)
        )
    }
}
