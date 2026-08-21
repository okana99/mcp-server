package net.portswigger.mcp.schema

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HistoryItemJsonTest {
    @Test
    fun `small history items are unchanged`() {
        val item = HttpRequestResponse("request", "response", "notes")

        assertEquals(Json.encodeToString(item), encodeHistoryItem(item))
    }

    @Test
    fun `oversized history items are returned without truncation`() {
        val item = HttpRequestResponse(
            request = "\\\"😀".repeat(2_000),
            response = "😀".repeat(3_000),
            notes = "keep me"
        )
        val encoded = encodeHistoryItem(item)

        assertTrue(encoded.length > 5_000)
        assertEquals(Json.encodeToString(item), encoded)
    }
}
