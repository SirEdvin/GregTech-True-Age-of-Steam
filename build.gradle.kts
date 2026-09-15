import net.darkhax.curseforgegradle.TaskPublishCurseForge
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.ByteArrayOutputStream

plugins {
    java
    id("site.siredvin.root") version "0.9.0"
    id("site.siredvin.release") version "0.9.0"
    id("site.siredvin.publishing") version "0.9.0"
    id("site.siredvin.mod-publishing") version "0.9.0"
    id("site.siredvin.forge") version "0.9.0"
}

val minecraftVersion: String by extra
val modVersion: String by extra
val modBaseName: String by extra
val modName: String by extra
val modUrl: String by extra
val modAuthor: String by extra
val modLicense: String by extra
val forgeVersion: String by extra
val gtceuVersion: String by extra
val ldlibVersion: String by extra
val registrateVersion: String by extra
val configurationVersion: String by extra
val jeiVersion: String by extra
val emiVersion: String by extra
val kubejsVersion: String by extra
val rhinoVersion: String by extra
val architecturyVersion: String by extra

subprojectShaking {
    withKotlin.set(false)
    javaVersion.set(JavaVersion.VERSION_17)
}
subprojectShaking.setupSubproject(project)

version = modVersion
base {
    archivesName.set("$modBaseName-forge-$minecraftVersion")
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
}

forgeShaking {
    commonProjectName.set("")
    useAT.set(false)
    useMixins.set(false)
    useJarJar.set(false)
    extraVersionMappings.set(mapOf("gtceu" to "gtceu"))
    extraRawVersionMappings.set(
        mapOf(
            "forgeMajor" to forgeVersion.substringBefore('.'),
            "minecraft" to minecraftVersion,
            "modLicense" to modLicense,
            "modId" to modBaseName,
            "modName" to modName,
            "modUrl" to modUrl,
            "modAuthor" to modAuthor,
        ),
    )
    shake()
}

githubShaking {
    projectRepo.set("GregTech-True-Age-of-Steam")
    modBranch.set("main")
    useFabric.set(false)
    useForge.set(false)
    useRoot.set(true)
    rootJarName.set("jar")
    shake()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://mvn.siredvin.site/minecraft") {
        name = "SirEdvin's Minecraft repository"
        content {
            includeGroup("site.siredvin")
            includeGroupByRegex("site\\.siredvin(\\..*)?")
            includeGroup("net.minecraftforge")
            includeGroup("net.minecraftforge.gradle")
            includeGroup("net.neoforged")
            includeGroup("net.neoforged.moddev")
            includeGroup("org.parchmentmc")
            includeGroup("org.parchmentmc.feather")
            includeGroup("org.parchmentmc.data")
            includeGroup("org.spongepowered")
            includeGroup("org.spongepowered.gradle.vanilla")
            includeGroup("net.fabricmc")
            includeGroup("fabric-loom")
        }
    }
    maven {
        name = "GTCEu Maven"
        url = uri("https://maven.gtceu.com")
        content { includeGroup("com.gregtechceu.gtceu") }
    }
    maven {
        name = "FirstDarkDev"
        url = uri("https://maven.firstdark.dev/snapshots/")
    }
    maven {
        name = "Registrate"
        url = uri("https://maven.tterrag.com/")
        content { includeGroup("com.tterrag.registrate") }
    }
    maven {
        name = "BlameJared"
        url = uri("https://maven.blamejared.com/")
    }
    maven("https://maven.theillusivec4.top/")
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content { includeGroup("curse.maven") }
    }
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
        content { includeGroup("maven.modrinth") }
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
    }
    maven {
        name = "Lat's Maven (Rhino, KubeJS)"
        url = uri("https://maven.latvian.dev/releases")
        content { includeGroup("dev.latvian.mods") }
    }
    maven {
        name = "Shedaniel maven"
        url = uri("https://maven.shedaniel.me/")
        content {
            includeGroupAndSubgroups("me.shedaniel")
            includeGroup("dev.architectury")
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")

    compileOnly("org.jetbrains:annotations:26.0.1")

    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion"))
    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion"))
    runtimeOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"))
    runtimeOnly(fg.deobf("dev.emi:emi-forge:$emiVersion+$minecraftVersion"))
    runtimeOnly(fg.deobf("curse.maven:jade-324717:5390389"))
    runtimeOnly(fg.deobf("curse.maven:kubejs-aisle-tool-1236291:7543948"))

    val gtceuDependency = (dependencies.create("com.gregtechceu.gtceu:gtceu-$minecraftVersion:$gtceuVersion:slim") as ExternalModuleDependency).apply {
        isTransitive = false
    }
    val ldlibDependency = (dependencies.create("com.lowdragmc.ldlib:ldlib-forge-$minecraftVersion:$ldlibVersion") as ExternalModuleDependency).apply {
        isTransitive = false
    }
    implementation(fg.deobf(gtceuDependency))
    implementation(fg.deobf(ldlibDependency))
    implementation(fg.deobf("com.tterrag.registrate:Registrate:$registrateVersion"))
    implementation(fg.deobf("dev.toma.configuration:configuration-forge-$minecraftVersion:$configurationVersion"))
    implementation(fg.deobf("dev.latvian.mods:kubejs-forge:$kubejsVersion"))
    implementation(fg.deobf("dev.latvian.mods:rhino-forge:$rhinoVersion"))
    implementation(fg.deobf("dev.architectury:architectury-forge:$architecturyVersion"))

    runtimeOnly(fg.deobf("maven.modrinth:ae2:15.2.13"))

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

mixin {
    add(sourceSets.main.get(), "mixins.$modBaseName.refmap.json")
    config("$modBaseName.mixins.json")
}

extensions.configure<UserDevExtension>("minecraft") {
    runs {
        named("client") {
            arg("--refresh-dependencies")
            property("forge.enabledGameTestNamespaces", modBaseName)
        }
        named("server") {
            arg("--world")
            arg("world-extra")
            property("forge.enabledGameTestNamespaces", modBaseName)
        }
        named("data") {
            arg("--existing-mod")
            arg("gtceu")
        }
        create("multiblockPreviews") {
            workingDirectory(file("run"))
            arg("--width")
            arg("1280")
            arg("--height")
            arg("720")
            property("forge.enabledGameTestNamespaces", modBaseName)
            property("gttruesteam.generateMultiblockPreviews", "true")
            property("gttruesteam.multiblockPreviewDebug", providers.gradleProperty("multiblockPreviewDebug").orElse("false").get())
            property("gttruesteam.multiblockPreviewFilter", providers.gradleProperty("multiblockPreviewFilter").orElse("").get())
            property("gttruesteam.multiblockPreviewFormat", providers.gradleProperty("multiblockPreviewFormat").orElse("gif").get())
            property("gttruesteam.multiblockPreviewOutput", file("build/multiblock-previews").absolutePath)
        }
    }
}

tasks.named<Jar>("jar") {
    manifest.attributes("MixinConfigs" to "$modBaseName.mixins.json")
}

tasks.named<ProcessResources>("processResources") {
    val properties = mapOf(
        "modLicense" to modLicense,
        "modId" to modBaseName,
        "modName" to modName,
        "modUrl" to modUrl,
        "modAuthor" to modAuthor,
    )
    inputs.properties(properties)
    filesMatching("META-INF/mods.toml") {
        expand(properties)
    }
}

val configureMultiblockPreviewTask: JavaExec.() -> Unit = {
    val useVirtualDisplay = providers.gradleProperty("multiblockPreviewVirtualDisplay")
        .map(String::toBoolean)
        .orElse(true)
    var xvfbProcess: Process? = null
    gradle.buildFinished {
        xvfbProcess?.destroy()
    }
    doFirst {
        if (!useVirtualDisplay.get()) {
            return@doFirst
        }

        val xvfbOutput = ByteArrayOutputStream()
        exec {
            commandLine("sh", "-c", "command -v Xvfb")
            standardOutput = xvfbOutput
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }
        val xvfbExecutable = xvfbOutput.toString().trim()
        if (xvfbExecutable.isEmpty()) {
            throw GradleException("Xvfb is required for generateMultiblockPreviews. Install xvfb, or run with -PmultiblockPreviewVirtualDisplay=false to use the active display.")
        }

        val displayNumber = (90..199).firstOrNull { !file("/tmp/.X$it-lock").exists() }
            ?: throw GradleException("No free Xvfb display number found in range :90-:199")
        val display = ":$displayNumber"
        val logFile = layout.buildDirectory.file("multiblock-previews/xvfb.log").get().asFile
        logFile.parentFile.mkdirs()
        xvfbProcess = ProcessBuilder(xvfbExecutable, display, "-screen", "0", "1280x720x24", "-nolisten", "tcp")
            .redirectErrorStream(true)
            .redirectOutput(logFile)
            .start()
        Thread.sleep(500)
        if (xvfbProcess?.isAlive != true) {
            throw GradleException("Xvfb failed to start; see $logFile")
        }

        environment("DISPLAY", display)
        logger.lifecycle("Running multiblock previews on virtual display $display")
    }
}

tasks.withType<JavaExec>().matching { it.name == "runMultiblockPreviews" }.configureEach(configureMultiblockPreviewTask)

tasks.register("generateMultiblockPreviews") {
    group = "documentation"
    description = "Runs a client to generate preview screenshots for addon multiblocks."
    dependsOn(tasks.matching { it.name == "runMultiblockPreviews" })
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
    options.compilerArgs.add("-Aquiet=true")
}

modPublishing {
    output.set(tasks.named<Jar>("jar").get())
    shake()
}

tasks.named<TaskPublishCurseForge>("publishCurseForge") {
    uploadArtifacts.forEach { it.addEnvironment("Client", "Server") }
}

publishingShaking {
    shake()
    project.publishing {
        publications {
            named<MavenPublication>("maven") {
                fg.component(this)
            }
        }
    }
}
