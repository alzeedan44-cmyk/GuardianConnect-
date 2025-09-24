package com.example.guardianconnect

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Guardian(val name: String, val phone: String)

object ContactsManager {
    private const val PREF = "gc_prefs"
    private const val KEY_GUARDIANS = "guardians"
    private const val KEY_OWNER = "owner"

    fun getAllGuardians(ctx: Context): List<Guardian> {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_GUARDIANS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = mutableListOf<Guardian>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(Guardian(o.getString("name"), o.getString("phone")))
        }
        return out
    }

    fun addGuardian(ctx: Context, g: Guardian) {
        val list = getAllGuardians(ctx).toMutableList()
        list.add(g)
        saveList(ctx, list)
    }

    private fun saveList(ctx: Context, list: List<Guardian>) {
        val arr = JSONArray()
        list.forEach { o ->
            val jo = JSONObject()
            jo.put("name", o.name)
            jo.put("phone", o.phone)
            arr.put(jo)
        }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_GUARDIANS, arr.toString()).apply()
    }

    fun getPrimaryGuardian(ctx: Context): Guardian? {
        return getAllGuardians(ctx).firstOrNull()
    }

    fun getOwnerName(ctx: Context): String? = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_OWNER, null)
}
