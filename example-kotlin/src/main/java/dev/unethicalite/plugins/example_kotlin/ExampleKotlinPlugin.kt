package dev.unethicalite.plugins.example_kotlin

import dev.unethicalite.api.plugins.Script
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension

@PluginDescriptor(name = "Example Kotlin Plugin")
@Extension
class ExampleKotlinPlugin : Script() {
    override fun loop(): Int {
        return 1000
    }

    override fun onStart(vararg scriptArgs: String) {
    }
}