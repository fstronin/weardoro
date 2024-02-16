package com.fstronin.weardoro

import com.fstronin.weardoro.App.locale

open class AppException(format: String?, vararg args: Any?) : Exception() {
    init {
        val msg = String.format(
            locale,
            format!!,
            *args
        )
    }
}
