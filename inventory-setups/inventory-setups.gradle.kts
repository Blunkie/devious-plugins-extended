version = "0.0.1"

project.extra["PluginName"] = "Inventory Setups"
project.extra["PluginDescription"] = "Inventory setups with auto banking"

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
