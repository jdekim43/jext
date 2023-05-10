package kr.jadekim.jext.exposed.koin

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.rds.RdsUtilities
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest
import software.amazon.awssdk.services.sts.StsClient
import java.net.URI

private val logger = LoggerFactory.getLogger("kr.jadekim.jext.exposed.koin.RdsUtils")

fun rdsInit(urlString: String, username: String) {
    if (!urlString.startsWith("jdbc:mysql:aws")) {
        return
    }

    val uri = URI("mysql://" + urlString.substring("jdbc:mysql:aws://".length))

    logger.info("Init RDS ($username - $urlString)")

    val region = resolveRegion(uri.host)
    val provider = DefaultCredentialsProvider.create()
    val identity = StsClient.builder().credentialsProvider(provider).build().callerIdentity
    val utilities = RdsUtilities.builder()
        .credentialsProvider(provider)
        .region(region)
        .build()
    val token = utilities.generateAuthenticationToken { builder: GenerateAuthenticationTokenRequest.Builder ->
        builder.hostname(uri.host)
            .port(uri.port).username(username)
    }

    logger.info("Initialized RDS ($username - $urlString) with $identity ($token)")
}

private fun resolveRegion(host: String): Region {
    val tokens = host.split('.').asReversed()
    if (tokens.size < 4) {
        return Region.AP_NORTHEAST_2
    }

    return Region.of(tokens[3]) ?: Region.AP_NORTHEAST_2
}
