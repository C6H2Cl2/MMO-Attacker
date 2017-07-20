package c6h2cl2.discordbot.mmo_attacker

import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.User
import sx.blah.discord.handle.obj.IChannel

/**
 * @author C6H2Cl2
 */

const val OWNER_ID = 217261423041052672L

fun main(args: Array<String>) {
    val client = createDiscordClient(args[0])!!
    println("Client Created")
}

fun createDiscordClient(token: String): IDiscordClient? {
    try {
        val builder = ClientBuilder().withToken(token)
                .registerListener(OnMessageListener())
        builder.login()
        println("Listener Registered")
        return builder.build()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

class OnMessageListener : IListener<MessageReceivedEvent> {
    private var trainingChannnel: IChannel? = null
    private var mode = BotMode.IDLE
    private val states = HashMap<String, Any>()

    override fun handle(event: MessageReceivedEvent) {
        val message = event.message
        val client = event.client
        val channel = event.channel
        println("Message Received: $message")
        if ((message.author as User).longID == OWNER_ID && message.content.startsWith("\$\$")) {
            val commands = message.content.substring(2).split(' ')
            when (commands[0]) {
                "status" -> channel.sendMessage("!!status")
                "item" -> kotlin.run {
                    var command = "!!item"
                    if (commands.size != 1) {
                        (1 until commands.size).forEach {
                            command += " ${commands[it]}"
                        }
                    }
                    channel.sendMessage(command)
                }
                "attack" -> channel.sendMessage("!!attack")
            }
        }
    }

    private fun train(event: MessageReceivedEvent) {

    }
}

enum class BotMode {
    IDLE, TRAINING, LEVELING, KILLING
}