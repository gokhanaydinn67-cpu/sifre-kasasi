package com.sifre.kasasi

import android.content.Context
import org.json.JSONArray
import java.io.File

object Vault {
    private const val FILE = "vault.dat"

    fun load(context: Context): MutableList<Entry> {
        val f = File(context.filesDir, FILE)
        if (!f.exists()) return mutableListOf()
        return try {
            val json = Crypto.decrypt(f.readText())
            val arr = JSONArray(json)
            val list = mutableListOf<Entry>()
            for (i in 0 until arr.length()) {
                list.add(Entry.fromJson(arr.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun save(context: Context, list: List<Entry>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        val enc = Crypto.encrypt(arr.toString())
        File(context.filesDir, FILE).writeText(enc)
    }
}
