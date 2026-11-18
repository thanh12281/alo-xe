package com.aloxe.app.service

import java.text.Normalizer

/**
 * So khớp từ khóa đơn giản, chạy ngay trên máy, không gọi mạng.
 * - Bỏ dấu tiếng Việt và viết thường trước khi so khớp, để "Bao Xe" cũng khớp với "bao xe".
 * - Khớp ít nhất 1 từ khóa NHẬN, và không chứa từ khóa LOẠI TRỪ nào.
 */
object KeywordMatcher {

    fun matches(
        text: String,
        includeKeywords: List<String>,
        excludeKeywords: List<String>
    ): Boolean {
        if (text.isBlank()) return false
        val normalizedText = normalize(text)

        val hasExcluded = excludeKeywords.any { kw ->
            kw.isNotBlank() && normalizedText.contains(normalize(kw))
        }
        if (hasExcluded) return false

        if (includeKeywords.isEmpty()) return false
        return includeKeywords.any { kw ->
            kw.isNotBlank() && normalizedText.contains(normalize(kw))
        }
    }

    private fun normalize(input: String): String {
        val lower = input.lowercase()
        val decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{Mn}+"), "")
    }
}
