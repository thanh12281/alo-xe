package com.aloxe.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Nơi lưu mọi cài đặt của người dùng ngay trên máy (không gửi lên đâu cả).
 * Dùng SharedPreferences cho đơn giản, đủ dùng với lượng dữ liệu nhỏ này.
 */
class Prefs(context: Context) {

    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("alo_xe_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_INCLUDE = "keywords_include"
        private const val KEY_EXCLUDE = "keywords_exclude"
        private const val KEY_ENABLED_GROUPS = "enabled_groups"
        private const val KEY_KNOWN_GROUPS = "known_groups"

        val DEFAULT_INCLUDE = listOf(
            "bao xe", "tiễn liền", "bxe", "cần xe", "đón tại", "ship khách", "chở khách"
        )
        val DEFAULT_EXCLUDE = listOf(
            "bán xe", "cho thuê xe dài hạn", "sale", "khuyến mãi"
        )
    }

    // ---- Từ khóa ----

    fun getIncludeKeywords(): List<String> =
        sp.getString(KEY_INCLUDE, null)?.lines()?.filter { it.isNotBlank() }
            ?: DEFAULT_INCLUDE

    fun setIncludeKeywords(lines: List<String>) {
        sp.edit().putString(KEY_INCLUDE, lines.joinToString("\n")).apply()
    }

    fun getExcludeKeywords(): List<String> =
        sp.getString(KEY_EXCLUDE, null)?.lines()?.filter { it.isNotBlank() }
            ?: DEFAULT_EXCLUDE

    fun setExcludeKeywords(lines: List<String>) {
        sp.edit().putString(KEY_EXCLUDE, lines.joinToString("\n")).apply()
    }

    // ---- Nhóm theo dõi ----
    // Nếu danh sách "enabled" rỗng -> theo dõi TẤT CẢ nhóm Zalo.
    // Nếu có ít nhất 1 nhóm được bật -> chỉ theo dõi đúng các nhóm đó.

    fun getEnabledGroups(): Set<String> =
        sp.getStringSet(KEY_ENABLED_GROUPS, emptySet()) ?: emptySet()

    fun setEnabledGroups(groups: Set<String>) {
        sp.edit().putStringSet(KEY_ENABLED_GROUPS, groups).apply()
    }

    fun getKnownGroups(): Set<String> =
        sp.getStringSet(KEY_KNOWN_GROUPS, emptySet()) ?: emptySet()

    /** Gọi mỗi khi thấy một tiêu đề nhóm mới, để màn hình "Chọn nhóm" có gì mà hiển thị. */
    fun rememberGroup(title: String) {
        if (title.isBlank()) return
        val current = getKnownGroups().toMutableSet()
        if (current.add(title)) {
            sp.edit().putStringSet(KEY_KNOWN_GROUPS, current).apply()
        }
    }

    fun shouldMonitorGroup(title: String): Boolean {
        val enabled = getEnabledGroups()
        return enabled.isEmpty() || enabled.contains(title)
    }
}
