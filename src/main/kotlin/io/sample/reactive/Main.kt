package io.sample.reactive

import com.fasterxml.jackson.databind.ObjectMapper
import net.framework.api.rest.client.SimpleApiClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import twitter4j.Twitter
import twitter4j.TwitterFactory
import java.time.Duration
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.HashMap

val logger: Logger = Logger.getLogger("reactor-sample")

fun main(args: Array<String>) {
    val mono: Mono<KV> = Mono.just(callRestApi(getJsonUris()["simple-kv"].orEmpty(), KV::class.java))
        .doFinally { logger.info(it.name) }
        .take(Duration.ofMillis(30000))

    mono.subscribe(
        { logger.info(getMapper().writeValueAsString(it)) },
        {
            logger.log(Level.SEVERE, "Error occurred when fetching json from remote")
            throw it
        },
        {  },
        { it.request(10) }
    )

    val kvs: Mono<KVs> = Mono.just(callRestApi(getJsonUris()["kvs"].orEmpty(), KVs::class.java))
            .doFinally { logger.info(it.name) }
            .take(Duration.ofMillis(30000))

    kvs.subscribe(
        // onSuccess
        { it.objects.forEach { `object` ->
                `object`.value.let { println("name : ${it}") }
            }
        },
        // onError
        { logger.log(Level.SEVERE, "Error occurred when fetching json from remote") },
        // onComplete
        { logger.info("Completed fetching data") }
    )

}

data class KV (
    var key: String,
    var value: String
)

data class KVs (
        var objects: List<KV>
)

fun getJsonUris(): Map<String, String> {
    return HashMap<String, String>().apply {
        put("simple-kv", "https://raw.githubusercontent.com/simonNozaki/jsons/main/simple-kv.json")
        put("kvs", "https://raw.githubusercontent.com/simonNozaki/jsons/main/kvs.json")
        put("current-price", "https://api.coindesk.com/v1/bpi/currentprice.json")
    }
}

fun printStackTrace(): Unit = Thread.currentThread().stackTrace.forEach { logger.info("${it.className}#${it.methodName}") }

fun getMapper(): ObjectMapper = ObjectMapper()

fun <T> callRestApi(url: String, clazz: Class<T>): T {
    return SimpleApiClient
        .setTargetUri(url)
        .get()
        .invoke(clazz)
}


fun getTwitter(): Twitter = TwitterFactory.getSingleton()