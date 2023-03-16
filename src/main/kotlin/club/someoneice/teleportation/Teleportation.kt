package club.someoneice.teleportation

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.boss.BossBar
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.Difficulty
import org.lwjgl.glfw.GLFW
import java.awt.Color

const val MODID = "teleportation"
val packageId: Identifier = Identifier(MODID, "teleportation_key")
val Effect: StatusEffect = EffectTeleportation()

class EffectTeleportation: StatusEffect(StatusEffectCategory.NEUTRAL, Color.PINK.rgb)

fun initialize() {
    Registry.register(Registries.STATUS_EFFECT, Identifier(MODID, "teleportation_effect"), Effect)
    ServerPlayNetworking.registerGlobalReceiver(packageId) { server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, sender: PacketSender ->
        player.addStatusEffect(StatusEffectInstance(Effect, 20, 0))
        if (player.world.difficulty != Difficulty.PEACEFUL) player.hungerManager.foodLevel -= 3
    }
}

fun initializeClient() {
    val keyTeleport: KeyBinding = KeyBindingHelper.registerKeyBinding(KeyBinding("key.teleportation.use", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.category.teleportation.use"))

    ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
        if (keyTeleport.wasPressed()) {
            val player = it.player!!
            if (player.isCreative || (player.hungerManager.foodLevel >= 6 && !player.isInsideWaterOrBubbleColumn && !player.hasStatusEffect(Effect))) {
                if (!player.isSneaky) player.addVelocity(player.rotationVector.x, player.rotationVector.y * 0.1 + 0.35, player.rotationVector.z)
                else player.addVelocity(0.0 - player.rotationVector.x, player.rotationVector.y * (-0.1) + 0.35, 0.0 - player.rotationVector.z)
                if (!player.isCreative) ClientPlayNetworking.send(packageId, PacketByteBufs.empty())
            }
        }
    })
}