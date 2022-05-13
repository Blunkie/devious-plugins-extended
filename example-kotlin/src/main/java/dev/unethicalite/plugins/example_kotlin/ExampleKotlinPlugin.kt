package dev.unethicalite.plugins.example_kotlin

import dev.unethicalite.api.movement.Movement
import dev.unethicalite.api.plugins.Script
import net.runelite.api.coords.WorldPoint
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension

@PluginDescriptor(name = "Example Kotlin Plugin")
@Extension
class ExampleKotlinPlugin : Script() {
    override fun loop(): Int {
        Movement.walkTo(WorldPoint(3143, 3440, 0))
        return 2000
    }

    override fun onStart(vararg scriptArgs: String) {

    }
}