package com.example.guardianconnect

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

object LocationHelper {
    @SuppressLint("MissingPermission")
    fun getLastKnownLocationSync(ctx: Context): Location? {
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = lm.getProviders(true)
        var best: Location? = null
        for (p in providers) {
            val l = lm.getLastKnownLocation(p) ?: continue
            if (best == null || l.accuracy < best.accuracy) best = l
        }
        return best
    }
}
