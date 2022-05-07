package dev.unethicalite.plugins.example_kotlin

import dev.unethicalite.api.plugins.Script
import dev.unethicalite.plugins.example_utils.ExampleUtils
import net.runelite.client.plugins.PluginDependency
import net.runelite.client.plugins.PluginDescriptor
import org.pf4j.Extension

@PluginDependency(ExampleUtils::class)
@PluginDescriptor(name = "Example Kotlin Plugin")
@Extension
class ExampleKotlinPlugin : Script() {
    override fun loop(): Int {
        return 1000
    }

    override fun onStart(vararg scriptArgs: String) {
    }
}