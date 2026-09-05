plugins {
    application
    kotlin("jvm")
}

val gdxVersion = "1.13.1"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    runtimeOnly("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}

application {
    mainClass.set("com.knightgame.DesktopLauncherKt")
}

kotlin { jvmToolchain(17) }
