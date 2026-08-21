package net.portswigger.mcp.schema

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal inline fun <reified T> encodeHistoryItem(item: T): String =
    Json.encodeToString(item)
