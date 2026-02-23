package com.simats.pathovision.utils

object UrlUtils {
    fun resolveMediaUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return Constants.BASE_MEDIA_URL + path.trimStart('/')
    }
}
