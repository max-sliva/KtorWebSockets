import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File
import org.bson.Document

fun main(args: Array<String>) {
    var curUser: String = ""
//    var users = ArrayList<Document>()
    val server = embeddedServer(Netty, port = 80) {//создаем сервер
       install(WebSockets)
        routing {
            webSocket("/") {
                println("onConnect session = $this")
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            println("From site: $text")
                            if (text == "GetUsers"){
                                outgoing.send(Frame.Text("Hello. What can I do?"))
                            }
                            else if (text.toLowerCase() in arrayListOf("добрый день", "здравствуйте", "привет")){
                                outgoing.send(Frame.Text("Добрый день. Чем помочь?"))
                            }
                            else if (text!="StartBot"){
                                outgoing.send(Frame.Text("Не понимаю. I don't understand."))
                            }
                        }
                    }
                }
            }
            webSocket("Users"){
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val text = frame.readText()
                            println("From site2: $text")
                            if (text=="GetUsers"){
                                if (curUser=="Admin") {
                                    val users = getUsers()
//                                println("users = ${users[1]}")
                                    outgoing.send(Frame.Text(users))
                                }
                            }
                            if (text=="Excel"){
                                println("Excel")
                                val file = toExcel(arrayOf("ФИО", "Логин", "Статус"), getUsersList())
                                // get filename from request url
//                                if (file != null) {
                                    if(file!!.exists()) {
                                        call.respondFile(file)
                                    } else call.respond(HttpStatusCode.NotFound)
//                                }
                            }
                            if (text=="Word"){
                                println("Word")
                                val file = toWordDocx(arrayOf("ФИО", "Логин", "Статус"), getUsersList())
                            }
                        }
                    }
                }

            }
            static {
                defaultResource("index.html", "RoboPortal")
                resources("RoboPortal")
            }
            get("/demo") {
                call.respondFile(File("resources/RoboPortal/socketBot.html"))
            }
            get("/admin.html") {
                if (curUser=="Admin") call.respondFile(File("resources/RoboPortal/admin.html"))
                else call.respondFile(File("resources/RoboPortal/index.html"))
            }
            get("Logout"){
                val receivedParams = call.request.queryParameters["login"]
                println("logout from ${receivedParams}")
                curUser = ""
                call.respondFile(File("resources/RoboPortal/index.html"))
            }
            post("/Login") {
                val receivedParams = call.receiveParameters()
                val login = receivedParams["login"]
                val pass = receivedParams["password"]
                if (login == "Admin" && pass == "sudo"){
                    call.respondFile(File("resources/RoboPortal/admin.html"))
                    curUser = "Admin"
                }
                else {
                    call.respondFile(File("resources/RoboPortal/userPage.html"))
                }
                print("login=$login ")
                println("pass=$pass")
            }
            post("AddUser"){
                val receivedParams = call.receiveParameters()
                val fio = receivedParams["FIO"]
                val status = receivedParams["status"]
                val login = receivedParams["login"]
                val pass = receivedParams["password"]
                val newAcc = Account(login!!, fio!!, status!!, pass!!)
                println("newAcc = $newAcc")
                addUser(newAcc)
                call.respondFile(File("resources/RoboPortal/admin.html"))
            }
            post ("ToMSOffice") {
                val receivedParams = call.receiveParameters()
                println("To office = ")
                when (receivedParams["toOffice"]){
                    "Excel" -> {
                        println("Excel")
                        val file = toExcel(arrayOf("ФИО", "Логин", "Статус"), getUsersList())
                        println(file!!.name)
                        if(file!!.exists()) {
                            call.respondFile(file)
                        } else call.respond(HttpStatusCode.NotFound)
                    }
                    "Word" -> {
                        println("Word")
                        val file = toWordDocx(arrayOf("ФИО", "Логин", "Статус"), getUsersList())
                        if(file!!.exists()) {
                            call.respondFile(file)
                        } else call.respond(HttpStatusCode.NotFound)
                    }
                }
            }
            post("Upload") { _ ->
                // retrieve all multipart data (suspending)
                println("file upload")
                var loginStr = ""
                var file: File? = null
                val path = System.getProperty("user.dir")
                val multipart = call.receiveMultipart()
                multipart.forEachPart { part ->
                    // if part is a file (could be form item)
                    if(part is PartData.FileItem) {
                        // retrieve file name of upload
                        val name = part.originalFileName!!
                        file = File("$path\\resources\\RoboPortal\\Images\\$name")
                        // use InputStream from part to save file
                        part.streamProvider().use { its ->
                            // copy the stream to the file with buffering
                            file!!.outputStream().buffered().use {
                                // note that this is blocking
                                its.copyTo(it)
                            }
                        }
                    }
                    else if (part is PartData.FormItem) {
                        loginStr = part.value
                        println("login for upload = ${loginStr}")
                    }
                    part.dispose()
                    val userCol = getCollectionFromDB("User")
                    userCol.updateOne(Filters.eq("login", loginStr), Updates.set("online", file!!.name))
                }
                call.respondFile(File("resources/RoboPortal/admin.html"))
            }
        }
    }
    server.start(wait = true)
}

