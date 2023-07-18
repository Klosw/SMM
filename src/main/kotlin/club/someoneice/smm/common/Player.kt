package club.someoneice.smm.common

import club.someoneice.smm.command.sendMessage
import club.someoneice.smm.command.teleportToPath
import club.someoneice.smm.command.teleportToPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.stream.Collectors


/**
 * tp实体
 * @date 2023/04/16
 * @constructor 创建[TpEntity]
 * @param [tpInitiator] tp启动器
 * @param [tpAccept] tp接受
 * @param [tp] 是否是邀请 false 去对面 true 对面过来
 */
data class TpEntity(
    val tpInitiator: ServerPlayer,
    val tpAccept: ServerPlayer,
    val invitation: Boolean = false,
    val createTime: Date = Date(),
)

//上次死亡地点
object Back {
    //上次死亡地点
    private val playerDeath = HashMap<ServerPlayer, PlayerPath>()

    fun tpToDeathPosition(player: ServerPlayer) {
        playerDeath[player]?.let {
            player.teleportToPath(it)
            player.sendMessage("回到死亡地点!", true)
            //不删除上一个位置信息
            //playerDeath.remove(player)
        } ?: run {
            player.sendMessage("未找到上一个位置!", true)
        }
    }

    fun addDeathPosition(player: ServerPlayer, playerPath: PlayerPath) {
        playerDeath[player] = playerPath
    }

    //存储当前位置
    fun addDeathPosition(player: ServerPlayer) {
        playerDeath[player] =
            player.let { return@let PlayerPath(it.level as ServerLevel, it.x, it.y, it.z, it.xRot, it.yRot) }
    }
}

object Tp {
    private val tpList = ArrayList<TpEntity>()
    private const val TIME_OUT = 30 * 1000

    fun tpa(tpInitiator: ServerPlayer, tpAccept: ServerPlayer) {
        checkTpPlayerTimeOut()
        if (tpInitiator == tpAccept) {
            tpAccept.sendMessage("传送对象是自己!", true)
            return
        }
        removeTpPlayer(tpInitiator, tpAccept)
        addTpPlayer(tpInitiator, tpAccept)
        tpInitiator.sendMessage("已经向${tpAccept.scoreboardName}发起请求!")
        tpAccept.sendMessage("${tpInitiator.scoreboardName}请求传送过来!")
    }

    fun tpaHere(tpInitiator: ServerPlayer, tpAccept: ServerPlayer) {
        checkTpPlayerTimeOut()
        if (tpInitiator == tpAccept) {
            tpAccept.sendMessage("传送对象是自己!", true)
            return
        }
        removeTpPlayer(tpInitiator, tpAccept, true)
        addTpPlayer(tpInitiator, tpAccept, true)
        tpInitiator.sendMessage("已经向${tpAccept.scoreboardName}发起请求!")
        tpAccept.sendMessage("希望传送你到${tpInitiator.scoreboardName}那边去!")
    }

    /**
     * tp接受
     * @param [tpAccept] tp接受
     * @param [tpDeny] tp拒绝
     */
    fun tpAccept(tpAccept: ServerPlayer, accept: Boolean = true) {
        checkTpPlayerTimeOut()
        //接受所有
        val tpPlayers = getTpPlayers(tpAccept)
        if (tpPlayers.isEmpty()) {
            tpAccept.sendMessage("没有任何请求!", true)
        }

        tpPlayers.forEach {
            //拒绝
            if (accept) {
                if (it.invitation) {
                    it.tpAccept.teleportToPlayer(it.tpInitiator)
                } else {
                    it.tpInitiator.teleportToPlayer(it.tpAccept)
                }
                it.tpInitiator.sendMessage("${it.tpAccept.scoreboardName}接受了你的请求!")
                it.tpAccept.sendMessage("${it.tpInitiator.scoreboardName}传送到当前地点!")
            } else {
                it.tpInitiator.sendMessage("${it.tpAccept.scoreboardName}拒绝了你的请求!")
                it.tpAccept.sendMessage("已拒绝${it.tpInitiator.scoreboardName}的传送请求!")
            }
        }
        tpList.removeAll(tpPlayers)
    }


    /**
     * 检查tp超时
     */
    fun checkTpPlayerTimeOut() {
        val entities = tpList.stream().filter {
            return@filter Date().time - it.createTime.time > TIME_OUT
        }.collect(Collectors.toList())
        tpList.removeAll(entities)
    }

    fun getTpPlayers(tpAccept: ServerPlayer): List<TpEntity> {
        return tpList.stream().filter {
            return@filter it.tpAccept == tpAccept
        }.collect(Collectors.toList())
    }

    fun removeTpPlayer(tpAccept: ServerPlayer) {
        val tpEntities = tpList.stream().filter {
            return@filter it.tpAccept == tpAccept
        }.collect(Collectors.toList())
        tpList.removeAll(tpEntities)
    }

    //移除发起者和接受者 的重复
    fun removeTpPlayer(tpInitiator: ServerPlayer, tpAccept: ServerPlayer, invitation: Boolean = false) {
        val tpEntities = tpList.stream().filter {
            return@filter it.tpInitiator == tpInitiator && it.tpAccept == tpAccept && it.invitation == invitation
        }.collect(Collectors.toList())
        tpList.removeAll(tpEntities)
    }

    private fun addTpPlayer(tp: TpEntity) {
        tpList.add(tp)
    }

    fun addTpPlayer(tpInitiator: ServerPlayer, tpAccept: ServerPlayer, invitation: Boolean = false) {
        addTpPlayer(TpEntity(tpInitiator, tpAccept, invitation))
    }
}

object Player {
    //在线人数
    val playerList = HashMap<String, ServerPlayer>()
}

