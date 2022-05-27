package net.unethicalite.plugins.example_kotlin

import net.runelite.client.plugins.PluginDescriptor
import net.unethicalite.api.plugins.Script
import net.unethicalite.api.widgets.Widgets
import org.pf4j.Extension

@PluginDescriptor(name = "Example Kotlin Plugin")
@Extension
class ExampleKotlinPlugin : Script() {
    override fun loop(): Int {
        return 2000
    }

    override fun onStart(vararg scriptArgs: String) {
        Widgets.get(458, 4).interact("Build")
    }
}