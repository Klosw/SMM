package club.someoneice.smm.common

import net.minecraft.server.level.ServerLevel


data class PlayerPath(
    val level: ServerLevel,
    val x: Double,
    val y: Double,
    val z: Double,
    val RotX: Float,
    val RotY: Float,
)
