package c6h2cl2.discordbot.mmo_attacker

import org.json.JSONObject
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.User
import sx.blah.discord.handle.obj.IChannel
import java.io.File
import java.nio.file.Files
import java.util.concurrent.locks.*
import kotlin.concurrent.timer
import kotlin.concurrent.withLock

/**
 * @author C6H2Cl2
 */

var MAIN_ID = 0L
var SUB_ID = 0L
var MMO_ID = 0L
var MAIN_TOKEN = ""
var SUB_TOKEN = ""
val BOT = MMOAttackerBot()
var bootFlag = true

fun main(args: Array<String>) {
    val jsonObject = JSONObject(String(Files.readAllBytes(File("token.json").toPath())))
    val tokens = jsonObject.getJSONObject("tokens")
    val id = jsonObject.getJSONObject("id")
    MAIN_TOKEN = tokens.getString("main")
    SUB_TOKEN = tokens.getString("sub")
    MAIN_ID = id.getLong("main")
    SUB_ID = id.getLong("sub")
    MMO_ID = id.getLong("mmo")
    createDiscordClient()
    timer(initialDelay = 1000, period = 1000) {
        if (!bootFlag) {
            return@timer
        }
        BOT.run()
    }
    BOT.mainClient.logout()
    BOT.subClient.logout()
}

fun createDiscordClient() {
    try {
        var builder = ClientBuilder().withToken(MAIN_TOKEN)
                .registerListener(OnMessageListener())
        builder.login()
        BOT.mainClient = builder.build()
        builder = ClientBuilder().withToken(SUB_TOKEN)
                .registerListener(OnMessageListener())
        builder.login()
        BOT.subClient = builder.build()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

class OnMessageListener : IListener<MessageReceivedEvent> {

    override fun handle(event: MessageReceivedEvent) {
        val message = event.message
        val id = (message.author as User).longID
        if (!(id == MAIN_ID || id == SUB_ID || id == MMO_ID)) {
            return
        }
        val client = event.client
        val channel = event.channel
        val content = message.content
        if (content.startsWith("""$$""")) {
            //Command Start
            val commands = content.substring(2).split(' ')
            val flag = BOT.flag
            when (commands[0]) {
                "t" -> kotlin.run {
                    if (id == MAIN_ID) {
                        BOT.flag = flag or 0b01

                    } else {
                        BOT.flag = flag or 0b10
                    }
                }
            }
        }
    }
}

enum class BotMode {
    IDLE, TRAINING, LEVELING, KILLING
}

class MMOAttackerBot {
    lateinit var mainClient: IDiscordClient
    lateinit var subClient: IDiscordClient
    var mainChannnel: IChannel? = null
    var subChannnel: IChannel? = null
    /**
     * 1:TrainingActive-Main
     * 2:TrainingActive-Sub
     */
    var flag = 0b00
    val quizData = HashMap<String, String>()
    
    init{
        val file = File("training.json")
        if(file.exists()){
            val jsonObject = JSONObjet(String(Files.readAllBytes(file.toPath())))
            val quizJson = jsonObject.getJSONOArray("quiz")
            (0 until quizeData.length()).forEach{
                val data = quizeJson.getJSONObject(it)
                quizData.put(data.getString("key"), data.getString("value"))
            }
        } else {
            file.createNewFile()
        }
    }

    fun run() {
        if((flag and 0b01) == 0b01){
            
        }
    }
}