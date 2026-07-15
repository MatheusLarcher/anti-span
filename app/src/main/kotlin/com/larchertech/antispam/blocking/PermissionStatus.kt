package com.larchertech.antispam.blocking

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

/** Status ao vivo de cada permissão/papel necessário — consultado direto do sistema, sem cache. */
object PermissionStatus {

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun hasCallScreeningRole(context: Context): Boolean {
        val roleManager = context.getSystemService<RoleManager>() ?: return false
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService<PowerManager>() ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /** Requisitos obrigatórios pro bloqueio de ligação funcionar de verdade. */
    fun isCallProtectionComplete(context: Context): Boolean {
        return hasContactsPermission(context) && hasCallScreeningRole(context)
    }
}
