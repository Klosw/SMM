package club.someoneice.smm.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerAboutToStartEvent
import net.minecraftforge.event.server.ServerStoppedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

/**
 * Register The Command.
 */
@EventBusSubscriber(modid = "omo", bus = EventBusSubscriber.Bus.FORGE)
object CommandRegistry {
    var init = false
    private fun register(event: CommandDispatcher<CommandSourceStack>) {
        val cmd = CommandHelper()
        cmd.tpa(event)
        cmd.tpAccept(event)
        cmd.tpDeny(event)
        cmd.tpaHere(event)
        cmd.back(event)
        cmd.rtp(event)
    }

    @SubscribeEvent
    fun register(event: ServerAboutToStartEvent) {
        if (!init) {
            register(event.server.commands.dispatcher)
            init = true
        }
    }

    @SubscribeEvent
    fun register(event: RegisterCommandsEvent) {
        if (init) register(event.dispatcher)
    }

    @SubscribeEvent
    fun onServerStopped(event: ServerStoppedEvent?) {
        init = false
    }
}