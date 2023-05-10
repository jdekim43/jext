package kr.jadekim.jext.redisson.codec

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import org.redisson.client.codec.BaseCodec
import org.redisson.client.protocol.Decoder
import org.redisson.client.protocol.Encoder
import org.redisson.codec.JsonCodec
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.reflect.KClass

class GsonCodec<T : Any>(
    val typeToken: TypeToken<T>,
    private val gson: Gson = Gson(),
) : BaseCodec(), JsonCodec<T> {

    constructor(clazz: Class<T>, gson: Gson = Gson()) : this(TypeToken.get(clazz), gson)

    constructor(klass: KClass<T>, gson: Gson = Gson()) : this(klass.java, gson)

    private val encoder = Encoder {
        val out = ByteBufAllocator.DEFAULT.buffer()

        return@Encoder try {
            val stream = ByteBufOutputStream(out)
            val writer = OutputStreamWriter(stream)
            gson.toJson(it, writer)
            writer.flush()
            stream.buffer()
        } catch (e: Exception) {
            out.release()
            throw e
        }
    }

    private val decoder = Decoder<Any> { buf, _ ->
        gson.fromJson(InputStreamReader(ByteBufInputStream(buf)), typeToken)
    }

    override fun getValueEncoder(): Encoder = encoder

    override fun getValueDecoder(): Decoder<Any> = decoder

    override fun getClassLoader(): ClassLoader {
        if (gson.javaClass.classLoader != null) {
            return gson.javaClass.classLoader
        }

        return super.getClassLoader()
    }
}