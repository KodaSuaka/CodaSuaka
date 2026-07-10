package com.example.codasuaka.data.remote.dto

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Gson JsonDeserializer untuk Int? yang toleran:
 * Jika value JSON bukan number (misal object User), return null.
 * Mencegah crash "Expected an int but got BEGIN_OBJECT".
 */
class NullableIntAdapter : JsonDeserializer<Int?>, JsonSerializer<Int?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Int? {
        return try {
            if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) {
                json.asInt
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun serialize(src: Int?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src)
    }
}
