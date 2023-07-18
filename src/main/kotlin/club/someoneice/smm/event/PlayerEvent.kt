package club.someoneice.smm.event

import club.someoneice.smm.common.Back
import club.someoneice.smm.common.Player.playerList
import club.someoneice.smm.common.PlayerPath
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@EventBusSubscriber
object PlayerEvent {
    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerLoggedInEvent) {
        playerList[event.entity.scoreboardName] = event.entity as ServerPlayer
    }

    @SubscribeEvent
    fun onPlayerLoggedOut(event: PlayerLoggedOutEvent) {
        playerList.remove(event.entity.scoreboardName)
    }

    @SubscribeEvent
    fun onPlayerDeath(event: LivingDeathEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            player.apply { Back.addDeathPosition(this, PlayerPath(level as ServerLevel, x, y, z, xRot, yRot)) }
        }
    }
}