package com.knightgame

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.*
import com.badlogic.gdx.graphics.g3d.utils.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Array
import kotlin.math.*

/** A small, self-contained 3D vertical slice: move, block, and defeat the raiders. */
class KnightGame : ApplicationAdapter() {
    private lateinit var camera: PerspectiveCamera
    private lateinit var modelBatch: ModelBatch
    private lateinit var environment: Environment
    private lateinit var font: BitmapFont
    private lateinit var batch: SpriteBatch
    private lateinit var arena: ModelInstance
    private lateinit var heroBody: ModelInstance
    private lateinit var heroHead: ModelInstance
    private lateinit var sword: ModelInstance
    private val foes = Array<Foe>()
    private val models = Array<Model>()
    private val hero = Vector3(0f, 0f, 5f)
    private val tmp = Vector3()
    private var yaw = 0f
    private var stamina = 100f
    private var health = 100f
    private var score = 0
    private var elapsed = 0f
    private var attackTime = 0f
    private var message = ""

    override fun create() {
        modelBatch = ModelBatch(); batch = SpriteBatch(); font = BitmapFont()
        environment = Environment().apply {
            set(ColorAttribute(ColorAttribute.AmbientLight, 0.42f, 0.46f, 0.55f, 1f))
            add(DirectionalLight().set(0.9f, 0.85f, 0.72f, -0.4f, -1f, -0.25f))
        }
        camera = PerspectiveCamera(67f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.near = 0.1f; camera.far = 100f
        val builder = ModelBuilder()
        fun material(color: Color) = Material(ColorAttribute.createDiffuse(color))
        arena = ModelInstance(builder.createBox(24f, .2f, 24f, material(Color(0.10f, .16f, .18f, 1f)), VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal)); models.add(arena.model)
        arena.transform.setToTranslation(0f, -.1f, 0f)
        heroBody = ModelInstance(builder.createCylinder(1.05f, 1.8f, 1.05f, 12, material(Color(0.20f, .28f, .38f, 1f)), VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal)); models.add(heroBody.model)
        heroHead = ModelInstance(builder.createSphere(.8f, .8f, .8f, 12, 8, material(Color(0.65f, .48f, .32f, 1f)), VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal)); models.add(heroHead.model)
        sword = ModelInstance(builder.createBox(.16f, 2.2f, .28f, material(Color(0.78f, .82f, .86f, 1f)), VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal)); models.add(sword.model)
        repeat(5) { foes.add(Foe(Vector3(floatArrayOf(-7f, -3f, 3f, 7f, 0f)[it], 0f, floatArrayOf(-6f, -4f, -6f, -2f, -9f)[it]))) }
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun keyDown(keycode: Int): Boolean { if (keycode == Input.Keys.SPACE && attackTime <= 0f) attackTime = .38f; return true }
        }
    }

    override fun render() {
        val dt = min(Gdx.graphics.deltaTime, .05f); elapsed += dt
        update(dt); Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClearColor(.035f, .055f, .09f, 1f); Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        camera.position.set(hero.x + 0f, 7.5f, hero.z + 10f); camera.lookAt(hero.x, 1f, hero.z); camera.update()
        modelBatch.begin(camera); modelBatch.render(arena, environment); drawHero(); foes.forEach { modelBatch.render(it.model, environment) }; modelBatch.end()
        batch.begin(); font.color = Color.WHITE; font.data.setScale(1.15f)
        font.draw(batch, "КЛЯТВА СЕВЕРНОЙ СТАЛИ", 28f, Gdx.graphics.height - 28f)
        font.data.setScale(.9f); font.draw(batch, "WASD — движение    SPACE — удар", 30f, 34f)
        font.draw(batch, "Здоровье: ${health.toInt()}%    Выносливость: ${stamina.toInt()}%    Побеждено: $score / 5", 30f, Gdx.graphics.height - 58f)
        if (message.isNotEmpty()) { font.color = Color.GOLD; font.draw(batch, message, Gdx.graphics.width / 2f - 100f, 90f) }
        batch.end()
    }

    private fun update(dt: Float) {
        attackTime = max(0f, attackTime - dt); stamina = min(100f, stamina + dt * 18f)
        val move = Vector3((if (Gdx.input.isKeyPressed(Input.Keys.D)) 1f else 0f) - (if (Gdx.input.isKeyPressed(Input.Keys.A)) 1f else 0f), 0f, (if (Gdx.input.isKeyPressed(Input.Keys.S)) 1f else 0f) - (if (Gdx.input.isKeyPressed(Input.Keys.W)) 1f else 0f))
        if (move.len2() > 0) { move.nor().scl(5f * dt); hero.add(move).x = hero.x.coerceIn(-10f, 10f); hero.z = hero.z.coerceIn(-10f, 10f) }
        foes.forEach { foe ->
            tmp.set(hero).sub(foe.position); val d = tmp.len()
            if (d < 9f && d > 1.5f) foe.position.mulAdd(tmp.nor(), dt * 1.25f)
            if (d < 1.65f && attackTime <= 0f) health = max(0f, health - dt * 9f)
            if (attackTime > .12f && attackTime < .3f && d < 2.7f && !foe.hit) { foe.hit = true; foe.hp--; if (foe.hp <= 0) { foe.dead = true; score++; message = "Попадание!" } }
            foe.model.transform.setToTranslation(foe.position.x, .8f, foe.position.z)
        }
        for (i in foes.size - 1 downTo 0) if (foes[i].dead) foes.removeIndex(i)
        if (foes.size == 0) message = "ПОБЕДА! Северное королевство спасено"
        if (health <= 0) message = "Поражение — нажмите F2, чтобы начать заново"
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) { health = 100f; score = 0; message = ""; foes.clear(); repeat(5) { foes.add(Foe(Vector3(-7f + it * 3.5f, 0f, -6f))) } }
    }

    private fun drawHero() {
        heroBody.transform.setToTranslation(hero.x, .9f, hero.z); heroHead.transform.setToTranslation(hero.x, 2.05f, hero.z)
        sword.transform.setToTranslation(hero.x + if (attackTime > 0) 1.2f else .9f, 1.1f, hero.z - .2f)
        sword.transform.rotate(Vector3.Y, if (attackTime > 0) -55f else -25f)
        modelBatch.render(heroBody, environment); modelBatch.render(heroHead, environment); modelBatch.render(sword, environment)
    }
    override fun dispose() { modelBatch.dispose(); batch.dispose(); font.dispose(); models.forEach { it.dispose() } }
}

private class Foe(val position: Vector3) {
    val model: ModelInstance
    var hp = 2; var hit = false; var dead = false
    init { val b = ModelBuilder(); val m = b.createBox(1.2f, 1.6f, 1.2f, Material(ColorAttribute.createDiffuse(Color(.48f, .12f, .12f, 1f))), VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal); model = ModelInstance(m); model.transform.setToTranslation(position.x, .8f, position.z) }
}
