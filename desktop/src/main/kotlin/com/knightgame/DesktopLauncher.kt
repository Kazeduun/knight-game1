package com.knightgame

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Клятва Северной Стали")
        setWindowedMode(1280, 720)
        useVsync(true)
        setForegroundFPS(120)
        setResizable(true)
    }
    Lwjgl3Application(KnightGame(), config)
}
