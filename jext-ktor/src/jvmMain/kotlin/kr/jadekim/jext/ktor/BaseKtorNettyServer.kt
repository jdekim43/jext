package kr.jadekim.jext.ktor

import io.ktor.server.netty.*
import kr.jadekim.logger.JLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseKtorNettyServer(
    serverName: String? = null,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    logger: JLogger? = null,
) : BaseKtorServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>(
    KtorServerFactory(Netty),
    serverName,
    coroutineContext,
    logger,
)