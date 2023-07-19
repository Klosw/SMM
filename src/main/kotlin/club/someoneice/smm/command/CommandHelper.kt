package club.someoneice.smm.command

import club.someoneice.smm.FastSupplier
import club.someoneice.smm.command.Command.COM_BACK
import club.someoneice.smm.command.Command.COM_PLAYER
import club.someoneice.smm.command.Command.COM_RTP
import club.someoneice.smm.command.Command.COM_TPA
import club.someoneice.smm.command.Command.COM_TPA_HERE
import club.someoneice.smm.command.Command.COM_TP_ACCEPT
import club.someoneice.smm.command.Command.COM_TP_DENY
import club.someoneice.smm.common.Back
import club.someoneice.smm.common.PlayerPath
import club.someoneice.smm.common.Tp
import club.someoneice.smm.config.Config
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.LiteralContents
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.util.Lazy
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import oshi.util.Memoizer.memoize
import java.util.*
import java.util.function.Supplier

object Command {
    const val COM_PLAYER = "player"
    const val COM_TPA = "tpa"
    const val COM_TPA_HERE = "tpahere"
    const val COM_TP_ACCEPT = "tpaccept"
    const val COM_TP_DENY = "tpdeny"
    const val COM_BACK = "back"
    const val COM_RTP = "rtp"
}

@EventBusSubscriber
class CommandHelper {

    /**
     * tp到 别人
     * @param [event] 事件
     */
    fun tpa(event: CommandDispatcher<CommandSourceStack>) {
        event.register(
            Commands.literal(COM_TPA).then(argument(COM_PLAYER, EntityArgument.players()).executes { cmd ->
                //需要传送到的人
                val playerName = EntityArgument.getPlayer(cmd, COM_PLAYER)
                //发起传送的人
                val playerAsk = cmd.source.playerOrException
                Tp.tpa(playerAsk, playerName)
                return@executes 0
            })

        )
    }

    /**
     * tp到我的位置
     * @param [event] 事件
     */
    fun tpaHere(event: CommandDispatcher<CommandSourceStack>) {
        event.register(
            Commands.literal(COM_TPA_HERE).then(argument(COM_PLAYER, EntityArgument.players()).executes { cmd ->
                val playerName = EntityArgument.getPlayer(cmd, COM_PLAYER)
                val playerAsk = cmd.source.playerOrException
                Tp.tpaHere(playerAsk, playerName)
                return@executes 0
            })
        )
    }

    /**
     * tp接受
     * @param [event] 事件
     */
    fun tpAccept(event: CommandDispatcher<CommandSourceStack>) {
        event.register(Commands.literal(COM_TP_ACCEPT).executes { cmd ->
            val playerAsk = cmd.source.playerOrException
            Tp.tpAccept(playerAsk)
            return@executes 0
        })
    }

    /**
     * tp拒绝
     * @param [event] 事件
     */
    fun tpDeny(event: CommandDispatcher<CommandSourceStack>) {
        event.register(Commands.literal(COM_TP_DENY).executes { cmd ->
            val playerAsk: ServerPlayer = cmd.source.playerOrException
            Tp.tpAccept(playerAsk, false)
            return@executes 0
        })
    }

    /**
     * 返回上次死亡地点
     * @param [event] 事件
     */
    fun back(event: CommandDispatcher<CommandSourceStack>) {
        event.register(Commands.literal(COM_BACK).executes { back ->
            Back.tpToDeathPosition(back.source.playerOrException)
            return@executes 0
        })
    }

    /**
     * 随机传送
     * @param [event] 事件
     */
    fun rtp(event: CommandDispatcher<CommandSourceStack>) {
        event.register(Commands.literal(COM_RTP).executes { rtp ->
            val player = rtp.source.playerOrException
            val world = player.level()
            if (Config.rtp.get()) {
                Back.addDeathPosition(player)
                val random = Random()
                var x = random.nextInt(Config.PosX.get())
                var z = random.nextInt(Config.PosZ.get())
                if (random.nextBoolean()) x = -x
                if (random.nextBoolean()) z = -z
                //以当前位置进行加减
                x += player.x.toInt()
                z += player.z.toInt()
                var y = 255
                player.sendMessage("正在查找安全位置!", true)
                while (world.getBlockState(BlockPos(x, y - 2, z)) == Blocks.AIR.defaultBlockState()) y -= 1
                player.connection.teleport(x.toDouble(), y.toDouble(), z.toDouble(), player.xRot, player.yRot)
            } else {
                rtp.source.sendFailure(StringTextComponent("Server Close RTP."))
            }

            return@executes 0
        })
    }
}

/**
 * 字符串文本组件
 * @param [s] s
 * @return [MutableComponent]
 */
fun StringTextComponent(s: String): MutableComponent {
    return MutableComponent.create(LiteralContents(s))
}

/**
 * 发送消息
 * @param [message] 消息
 */
fun ServerPlayer.sendMessage(message: String, msgToast: Boolean = false) {
    //这个false是在聊天栏显示 true 在屏幕前
    sendSystemMessage(MutableComponent.create(LiteralContents(message)), msgToast)
}

fun ServerPlayer.teleportToPath(path: PlayerPath) {
    path.let {
        this.teleportTo(it.level, it.x, it.y, it.z, it.RotX, it.RotY)
    }
}

fun ServerPlayer.teleportToPlayer(to: ServerPlayer) {
    teleportTo(to.level() as ServerLevel, to.x, to.y, to.z, to.xRot, to.yRot)
}


fun ServerPlayer.sendMessage(component: Component, uuid: UUID) {
    //这个false是在聊天栏显示
    sendSystemMessage(component, false)
}

/**
 * 发送消息
 * @param [message] 消息
 */
fun CommandSourceStack.sendMessage(message: String) {
    //这个true是在控制台打印
    val mutableComponent = MutableComponent.create(LiteralContents(message))
    sendSuccess(FastSupplier(mutableComponent), true)
}
