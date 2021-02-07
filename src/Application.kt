package com.example
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

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)
//TODO add device mapping via html
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {
    val path = System.getProperty("user.dir")
    println("userDir = $path")
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
//                            if (text=="GetFile"){
//                                outgoing.send(Frame.Binary())
//                            }
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
//            dynamic{
//
//            }
            get("/demo") {
                call.respondFile(File("resources/RoboPortal/socketBot.html"))
//                call.respondFile(File("RoboPortal/socketBot.html"))
            }
            get("/admin.html") {
                if (curUser=="Admin") call.respondFile(File("resources/RoboPortal/admin.html"))
                else call.respondFile(File("resources/RoboPortal/index.html"))
//                if (curUser=="Admin") call.respondFile(File("/admin.html"))
//                else call.respondFile(File("/index.html"))
            }
            get("Logout"){
                val receivedParams = call.request.queryParameters["login"]
                println("logout from ${receivedParams}")
                curUser = ""
                call.respondFile(File("resources/RoboPortal/index.html"))
//                call.respondFile(File("/index.html"))
            }
            post("/Login") {
                val receivedParams = call.receiveParameters()
                val login = receivedParams["login"]
                val pass = receivedParams["password"]
                if (login == "Admin" && pass == "sudo"){
                    call.respondFile(File("resources/RoboPortal/admin.html"))
//                    call.respondFile(File("/admin.html"))
                    curUser = "Admin"
                }
                else {
                    call.respondFile(File("resources/RoboPortal/userPage.html"))
//                    call.respondFile(File("/userPage.html"))
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
//                call.respondFile(File("/admin.html"))
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
                println("file upload") //отладочный вывод
                var loginStr = "" //строка для логина
                var file: File? = null  //файл для приема загружаемого файла
                val path = System.getProperty("user.dir") //текущий каталог проекта
                println("userDir = $path")
                val multipart = call.receiveMultipart() //начинаем прием файла и других данных
                multipart.forEachPart { part ->  // для каждой части данных
                    if(part is PartData.FileItem) {  //если часть данных - это часть файла
                        val name = part.originalFileName!! //получаем его имя
                        //задаем новый путь к файлу
                        file = File("$path\\resources\\UploadImages\\$name")
                        part.streamProvider().use { its -> //используем провайдер потока для записи данных в файл
                            // записываем принимаемые данные в файл, используем буферизацию
                            file!!.outputStream().buffered().use {
                                its.copyTo(it)  //копируем из принятой части в файл
                            }
                        }
                    } //иначе, если это обычные данные (в нашем случае - логин)
                    else if (part is PartData.FormItem) {
                        loginStr = part.value //записываем его в переменную
                        println("login for upload = ${loginStr}") //выводим для отладки
                    }
                    part.dispose() //очищаем принятую часть данных
                    val userCol = getCollectionFromDB("User") //получаем ссылку на нужную коллекцию из БД
                    //меняем в нужной записи поле с именем файла
                    userCol.updateOne(Filters.eq("login", loginStr), Updates.set("online", file!!.name))
                }
                call.respondFile(File("resources/RoboPortal/admin.html")) //перегружаем админскую страницу
            }
        }
    }
    server.start(wait = true)
}

