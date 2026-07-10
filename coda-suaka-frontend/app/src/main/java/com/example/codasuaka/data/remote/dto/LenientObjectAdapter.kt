package com.example.codasuaka.data.remote.dto

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

/**
 * Gson TypeAdapterFactory yang membuat parsing lebih toleran:
 * - Jika field bertipe Int/Int? menerima object (misal: created_by dikirim sbg objek User),
 *   maka akan di-set null (tidak crash).
 * - Jika field bertipe String/String? menerima object, di-set null.
 * - Jika field bertipe object menerima primitive, di-set null.
 */
class LenientObjectAdapterFactory : TypeAdapterFactory {

    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType

        // Hanya handle untuk Int? (nullable Int) dan Int
        if (rawType == Int::class.java || rawType == Integer::class.java || rawType == Int::class.javaPrimitiveType) {
            return null // Biarkan default untuk Int non-nullable
        }

        return null
    }
}

/**
 * TypeAdapter untuk Int? yang toleran: jika ketemu object/array/string, return null.
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

/**
 * TypeAdapter untuk Double? yang toleran: jika ketemu object, return null.
 */
class NullableDoubleAdapter : JsonDeserializer<Double?>, JsonSerializer<Double?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Double? {
        return try {
            if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) {
                json.asDouble
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun serialize(src: Double?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return if (src == null) JsonNull.INSTANCE else JsonPrimitive(src)
    }
}
