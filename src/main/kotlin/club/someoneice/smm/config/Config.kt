package club.someoneice.smm.config

import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue
import net.minecraftforge.common.ForgeConfigSpec.IntValue


class Config() {
    val builder: ForgeConfigSpec.Builder = ForgeConfigSpec.Builder()
    val common: ForgeConfigSpec = init()

    companion object {
        lateinit var rtp: BooleanValue
        lateinit var PosX: IntValue
        lateinit var PosZ: IntValue
    }

    fun init(): ForgeConfigSpec {
        builder.comment("General settings").push("general")

        rtp = builder.comment("Can player use RTP").define("RTP", true)
        PosX = builder.comment("RTP max X").defineInRange("Random Max X", 5000, 0, Int.MAX_VALUE)
        PosZ = builder.comment("RTP max Z").defineInRange("Random Max Z", 5000, 0, Int.MAX_VALUE)

        builder.pop()
        return builder.build()
    }
}