package kr.jadekim.jext.ktor.auth.provider

import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import kr.jadekim.server.http.exception.UnauthorizedException


//TODO: Bearer 인증이 OAuth 등에서 사용하는건데 구현은 일반 토큰처럼 하고 있음. 수정 필요.

@Deprecated("")
data class BearerCredential(val token: String) : Credential

@Deprecated("")
class BearerAuthenticationProvider internal constructor(
    configuration: Configuration
) : AuthenticationProvider(configuration) {

    internal val authenticationFunction = configuration.authenticationFunction

    class Configuration internal constructor(name: String?) : AuthenticationProvider.Config(name) {

        internal var authenticationFunction: AuthenticationFunction<BearerCredential> = { null }

        fun validate(body: suspend ApplicationCall.(BearerCredential) -> Principal?) {
            authenticationFunction = body
        }
    }

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val credentials = call.request.bearerAuthenticationCredentials()
        val principal = credentials?.let { authenticationFunction(call, it) }

        val cause = when {
            credentials == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge(name ?: "BearerToken", cause) { _, _ ->
                throw UnauthorizedException(credentials?.token ?: "")
            }
        }

        if (principal != null) {
            context.principal(principal)
        }
    }
}

@Deprecated("")
fun AuthenticationConfig.bearer(
    name: String? = null,
    configure: BearerAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = BearerAuthenticationProvider(BearerAuthenticationProvider.Configuration(name).apply(configure))

    register(provider)
}

@Deprecated("")
fun ApplicationRequest.bearerAuthenticationCredentials(): BearerCredential? {
    when (val authHeader = parseAuthorizationHeader()) {
        is HttpAuthHeader.Single -> {
            if (!authHeader.authScheme.equals("Bearer", ignoreCase = true)) {
                return null
            }

            return BearerCredential(authHeader.blob)
        }
        else -> return null
    }
}
