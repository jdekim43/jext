package kr.jadekim.jext.ktor.koin.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

abstract class AbstractRouter {

    protected lateinit var koin: Koin

    context(KoinComponent)
            operator fun invoke(): Route.() -> Unit = {
        koin = getKoin()
        configure()
    }

    context(KoinWrapper, Route)
            operator fun invoke() {
        koin = _getKoin()
        configure()
    }

    abstract fun Route.configure()
}

class KoinWrapper(
    val koin: Koin,
) {

    internal fun _getKoin() = koin
}

fun Router(body: context(KoinWrapper, RouterUtil) Route.() -> Unit) = object : AbstractRouter() {

    override fun Route.configure() {
        body(KoinWrapper(koin), RouterUtil, this)
    }
}

object RouterUtil {

    context(KoinWrapper)
    fun Route.route(path: String, router: AbstractRouter) {
        route(path) {
            router()
        }
    }

    context(KoinWrapper)
    fun Route.get(path: String, router: AbstractRouter) {
        route(path, HttpMethod.Get) {
            router()
        }
    }

    context(KoinWrapper)
    fun Route.post(path: String, router: AbstractRouter) {
        route(path, HttpMethod.Post) {
            router()
        }
    }

    context(KoinWrapper)
    fun Route.put(path: String, router: AbstractRouter) {
        route(path, HttpMethod.Put) {
            router()
        }
    }

    context(KoinWrapper)
    fun Route.patch(path: String, router: AbstractRouter) {
        route(path, HttpMethod.Patch) {
            router()
        }
    }

    context(KoinWrapper)
    fun Route.delete(path: String, router: AbstractRouter) {
        route(path, HttpMethod.Delete) {
            router()
        }
    }

    inline fun <reified T> Route.handle(crossinline handler: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit) =
        handle {
            handler(context.receive())
        }
}