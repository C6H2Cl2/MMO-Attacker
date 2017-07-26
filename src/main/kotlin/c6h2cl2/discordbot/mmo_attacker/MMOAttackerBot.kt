package c6h2cl2.discordbot.mmo_attacker

import net.dv8tion.jda.bot.JDABot
import net.dv8tion.jda.client.JDAClient
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.AccountType.CLIENT
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ShutdownEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.EventListener
import org.json.JSONObject
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*
import kotlin.concurrent.timer

/**
 * @author C6H2Cl2
 */

var MAIN_ID = 0L
var SUB_ID = 0L
var MMO_ID = 0L
var BOT_ID = 0L
var MAIN_TOKEN = ""
var SUB_TOKEN = ""
var BOT_TOKEN = ""
val BOT = MMOAttackerBot()
var runFlag = true
var timer = timer(initialDelay = 10000, period = 1000) {
    if (!runFlag) {
        cancel()
        finishLoop()
    }
    BOT.run()
}

fun main(args: Array<String>) {
    val jsonObject = JSONObject(String(Files.readAllBytes(File("token.json").toPath())))
    val tokens = jsonObject.getJSONObject("tokens")
    val id = jsonObject.getJSONObject("id")
    MAIN_TOKEN = tokens.getString("main")
    SUB_TOKEN = tokens.getString("sub")
    BOT_TOKEN = tokens.getString("bot")
    MAIN_ID = id.getLong("main")
    SUB_ID = id.getLong("sub")
    MMO_ID = id.getLong("mmo")
    BOT_ID = id.getLong("bot")
    createDiscordClient()
}

fun createDiscordClient() {
    BOT.mainClient = JDABuilder(CLIENT)
            .setToken(MAIN_TOKEN)
            .buildBlocking()
            .asClient()
    BOT.subClient = JDABuilder(CLIENT)
            .setToken(SUB_TOKEN)
            .buildBlocking()
            .asClient()
    BOT.botClient = JDABuilder(AccountType.BOT)
            .setToken(BOT_TOKEN)
            .addEventListener(MMOAttackerEventListener())
            .buildBlocking()
            .asBot()
}

fun finishLoop() {
    timer.cancel()
    timer.purge()
}

class MMOAttackerEventListener : EventListener {
    override fun onEvent(event: Event?) {
        if (event is MessageReceivedEvent) {
            messageReceived(event)
        }
        if (event is ShutdownEvent) {
            BOT.save()
        }
    }

    fun messageReceived(event: MessageReceivedEvent) {
        val message = event.message
        println("Received Message: ${message.content}")
        println("Received Message Raw: ${message.rawContent}")
        val id = message.author.idLong
        if (!(id == MAIN_ID || id == SUB_ID || id == MMO_ID)) {
            return
        }
        val client = event.jda
        val channel = event.channel as? TextChannel ?: return
        val content = message.content
        if (content.startsWith("""$:""")) {
            //Command Start
            val commands = content.substring(2).split(' ')
            println("Command Accepted: ${commands.toTypedArray().contentToString()}")
            val flag = BOT.flag
            when (commands[0]) {
                "t" -> kotlin.run {
                    if (id == MAIN_ID) {
                        BOT.flag = (flag or 0b01)
                        BOT.mainChannel = channel
                    } else {
                        BOT.flag = flag or 0b10
                        BOT.subChannel = channel
                    }
                }
                "stop" -> kotlin.run {
                    BOT.mainClient.jda.shutdownNow()
                    BOT.subClient.jda.shutdownNow()
                    BOT.botClient.jda.shutdownNow()
                    runFlag = false
                }
            }
        }
    }
}

//class LogoutListener: IListener<>

enum class BotMode {
    IDLE, TRAINING, LEVELING, KILLING
}

class MMOAttackerBot {
    lateinit var mainClient: JDAClient
    lateinit var subClient: JDAClient
    lateinit var botClient: JDABot
    var mainChannel: TextChannel? = null
    var subChannel: TextChannel? = null
    /**
     * 1:TrainingActive-Main
     * 2:TrainingActive-Sub
     */
    var flag = 0b00
    val quizData = HashMap<String, String>()

    init {
        val file = File("training.json")
        if (file.exists()) {
            val jsonObject = JSONObject(String(Files.readAllBytes(file.toPath())))
            val quizJson = jsonObject.getJSONArray("quiz")
            (0 until quizJson.length()).forEach {
                val data = quizJson.getJSONObject(it)
                quizData.put(data.getString("key"), data.getString("value"))
            }
        } else {
            file.createNewFile()
        }
    }

    fun run() {
        if ((flag and 0b01) == 0b01) {
            mainChannel?.sendMessage("!!q")
        }
        if ((flag and 0b10) == 0b10) {
            subChannel?.sendMessage("!!q")
        }
    }

    fun save() {
        val jsonObject = JSONObject()
        quizData.forEach { key, value ->
            jsonObject.append("quiz", JSONObject().put(key, value))
        }
        Files.write(File("training.json").toPath(), jsonObject.toString().toByteArray(Charset.forName("utf-8")))
    }
}