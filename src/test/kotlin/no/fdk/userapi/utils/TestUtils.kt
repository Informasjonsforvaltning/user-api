package no.fdk.userapi.utils

import org.springframework.http.HttpStatus
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

fun apiGet(path: String, headers: Map<String,String>): Map<String,Any> {

    return try {
        val connection = URL("http://localhost:$API_TEST_PORT$path").openConnection() as HttpURLConnection
        headers.forEach { connection.setRequestProperty(it.key, it.value) }
        connection.connect()

        if(isOK(connection.responseCode)) {
            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            mapOf(
                "body"   to responseBody,
                "header" to connection.headerFields.toString(),
                "status" to connection.responseCode)
        } else {
            mapOf(
                "status" to connection.responseCode,
                "header" to " ",
                "body"   to " "
            )
        }
    } catch (e: Exception) {
        mapOf(
            "status" to e.toString(),
            "header" to " ",
            "body"   to " "
        )
    }
}

private fun isOK(response: Int?): Boolean =
    if(response == null) false
    else HttpStatus.resolve(response)?.is2xxSuccessful == true
