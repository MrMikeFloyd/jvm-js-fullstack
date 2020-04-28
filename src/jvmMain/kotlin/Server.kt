import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.gzip
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.litote.kmongo.async.KMongo
import org.litote.kmongo.async.getCollection
import org.litote.kmongo.coroutine.deleteOne
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.eq

fun main() {
    // MongoDB client config for persistence layer
    val client = KMongo.createClient()
    val database = client.getDatabase("shoppingList")
    val collection = database.getCollection<ShoppingListItem>()

    embeddedServer(Netty, 9090) {
        // Add & Configure Content Negotiation, CORS and Compression
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            method(HttpMethod.Get)
            method(HttpMethod.Post)
            method(HttpMethod.Delete)
            anyHost()
        }
        install(Compression) {
            gzip()
        }
        // API endpoints
        routing {
            // Deliver JS content
            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }
            static("/") {
                resources("")
            }
            // Expose API endpoints for ShoppingList
            route(ShoppingListItem.path) {
                // Use path from model, group http verbs by common path
                get {
                    call.respond(collection.find().toList())
                }
                post {
                    collection.insertOne(call.receive<ShoppingListItem>())
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val id = call.parameters["id"]?.toInt() ?: error("Invalid delete request")
                    collection.deleteOne(ShoppingListItem::id eq id)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }.start(wait = true)

}
