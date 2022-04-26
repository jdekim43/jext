package kr.jadekim.jext.gson.annotation

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Exclude(
    val serialize: Boolean = true,
    val deserialize: Boolean = true
)

class ExcludeFieldSerializeStrategy : ExclusionStrategy {

    override fun shouldSkipField(f: FieldAttributes?): Boolean =
        f?.getAnnotation(Exclude::class.java)?.serialize == true

    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
}

class ExcludeFieldDeserializeStrategy : ExclusionStrategy {

    override fun shouldSkipField(f: FieldAttributes?): Boolean =
        f?.getAnnotation(Exclude::class.java)?.deserialize == true

    override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
}