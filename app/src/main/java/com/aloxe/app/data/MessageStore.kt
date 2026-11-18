package com.aloxe.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class MatchedMessage(
    val groupTitle: String,
    val content: String,
    val timestampMillis: Long
)

/**
 * Lưu lịch sử các tin nhắn đã khớp từ khóa vào một file JSON nhỏ trong bộ nhớ riêng
 * của app (người khác không đọc được). Không dùng thư viện ngoài, dùng org.json có sẵn
 * trong Android để bản build đầu tiên ít rủi ro lỗi phụ thuộc nhất có thể.
 */
class MessageStore(private val context: Context) {

    private val file: File
        get() = File(context.filesDir, "history.json")

    private val maxEntries = 200

    @Synchronized
    fun add(message: MatchedMessage) {
        val list = readAll().toMutableList()
        list.add(0, message)
        val trimmed = if (list.size > maxEntries) list.subList(0, maxEntries) else list
        writeAll(trimmed)
    }

    @Synchronized
    fun readAll(): List<MatchedMessage> {
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            val arr = JSONArray(text)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                MatchedMessage(
                    groupTitle = o.optString("groupTitle"),
                    content = o.optString("content"),
                    timestampMillis = o.optLong("timestampMillis")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeAll(list: List<MatchedMessage>) {
        val arr = JSONArray()
        list.forEach { m ->
            val o = JSONObject()
            o.put("groupTitle", m.groupTitle)
            o.put("content", m.content)
            o.put("timestampMillis", m.timestampMillis)
            arr.put(o)
        }
        file.writeText(arr.toString())
    }
}
