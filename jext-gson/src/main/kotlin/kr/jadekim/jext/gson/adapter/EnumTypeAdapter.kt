package kr.jadekim.jext.gson.adapter

import com.google.gson.*
import java.lang.reflect.Type

class EnumTypeAdapter(private val transform: (Enum<*>) -> String = { it.name.lowercase() }) : JsonSerializer<Enum<*>>,
    JsonDeserializer<Enum<*>> {

    companion object {
        private val typesToClass: MutableMap<String, Class<out Enum<*>>?> = HashMap()
    }

    override fun serialize(src: Enum<*>?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        if (src == null) {
            JsonNull.INSTANCE
        } else {
            JsonPrimitive(transform(src))
        }

    override fun deserialize(json: JsonElement?, typeOfT: Type, context: JsonDeserializationContext): Enum<*>? {
        if (json == null || json.isJsonNull) {
            return null
        }

        if (!json.isJsonPrimitive || !json.asJsonPrimitive.isString) {
            throw JsonParseException("Expecting a String JsonPrimitive, getting $json")
        }

        return try {
            val enumClassName: String = typeOfT.typeName
            var clazz = typesToClass[enumClassName]
            if (clazz == null) {
                @Suppress("UNCHECKED_CAST")
                clazz = Class.forName(enumClassName) as Class<out Enum<*>>
                typesToClass[enumClassName] = clazz
            }
            java.lang.Enum.valueOf(clazz, json.asString.uppercase()) as Enum<*>
        } catch (e: ClassNotFoundException) {
            throw JsonParseException(e)
        }
    }
}