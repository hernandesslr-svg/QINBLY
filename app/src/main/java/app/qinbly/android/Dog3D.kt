package app.qinbly.android

import android.content.Context
import android.view.Choreographer
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Filament
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.Gltfio
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.atan2

/** Vira true se o 3D falhar ao iniciar; aí o Cachorro volta para as imagens 2D. */
internal var qinblyDog3DFailed by mutableStateOf(false)

/** Ajuste de sentido: se a orelha for para o lado errado ao virar a cabeça, troque o sinal. */
internal const val QINBLY_DOG3D_YAW_SIGN = 1f
internal const val QINBLY_DOG3D_PITCH_SIGN = 1f

private fun m4Identity() = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

private fun m4Mul(a: FloatArray, b: FloatArray): FloatArray {
    val r = FloatArray(16)
    for (c in 0 until 4) for (row in 0 until 4) {
        var s = 0f
        for (k in 0 until 4) s += a[k * 4 + row] * b[c * 4 + k]
        r[c * 4 + row] = s
    }
    return r
}

private fun m4Translate(x: Float, y: Float, z: Float) =
    floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, x, y, z, 1f)

private fun m4Scale(s: Float) = floatArrayOf(s, 0f, 0f, 0f, 0f, s, 0f, 0f, 0f, 0f, s, 0f, 0f, 0f, 0f, 1f)

private fun m4RotX(deg: Float): FloatArray {
    val a = Math.toRadians(deg.toDouble())
    val c = cos(a).toFloat(); val s = sin(a).toFloat()
    return floatArrayOf(1f, 0f, 0f, 0f, 0f, c, s, 0f, 0f, -s, c, 0f, 0f, 0f, 0f, 1f)
}

private fun m4RotY(deg: Float): FloatArray {
    val a = Math.toRadians(deg.toDouble())
    val c = cos(a).toFloat(); val s = sin(a).toFloat()
    return floatArrayOf(c, 0f, -s, 0f, 0f, 1f, 0f, 0f, s, 0f, c, 0f, 0f, 0f, 0f, 1f)
}

private fun m4RotZ(deg: Float): FloatArray {
    val a = Math.toRadians(deg.toDouble())
    val c = cos(a).toFloat(); val s = sin(a).toFloat()
    return floatArrayOf(c, s, 0f, 0f, -s, c, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
}

/**
 * Cachorro em 3D (Filament): orelhas, nariz e língua de um arquivo .glb que giram junto com a cabeça.
 * Câmera ortográfica em pixels; a posição vem dos olhos (ML Kit) e a rotação do MediaPipe.
 */
internal class QinblyDog3DRenderer(private val context: Context) {
    @Volatile var state: QinblyFaceState? = null
    @Volatile var mirror: Boolean = false

    private var engine: Engine? = null
    private var renderer: Renderer? = null
    private var scene: Scene? = null
    private var view: View? = null
    private var camera: Camera? = null
    private var swapChain: SwapChain? = null
    private var uiHelper: UiHelper? = null
    private var assetLoader: AssetLoader? = null
    private var resourceLoader: ResourceLoader? = null
    private var provider: UbershaderProvider? = null
    private var asset: FilamentAsset? = null
    private var light = 0
    private var indirect: IndirectLight? = null
    private var earR = 0
    private var earL = 0
    private var tongue = 0
    private var earRIn = true
    private var earLIn = true
    private var tongueIn = true
    private var viewW = 0
    private var viewH = 0
    private var running = false
    private val choreographer = Choreographer.getInstance()

    private val frameCb = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return
            choreographer.postFrameCallback(this)
            try { drawFrame(frameTimeNanos) } catch (_: Throwable) { }
        }
    }

    fun attach(tv: TextureView) {
        try {
            Filament.init()
            Gltfio.init()
            val e = Engine.create()
            engine = e
            renderer = e.createRenderer().also {
                val co = it.clearOptions
                co.clear = true
                it.clearOptions = co
            }
            scene = e.createScene()
            view = e.createView().also {
                it.blendMode = View.BlendMode.TRANSLUCENT
                it.scene = scene
            }
            camera = e.createCamera(e.entityManager.create()).also { view?.camera = it }

            light = EntityManager.get().create()
            LightManager.Builder(LightManager.Type.DIRECTIONAL)
                .color(1f, 1f, 1f)
                .intensity(90000f)
                .direction(0f, -0.35f, -1f)
                .castShadows(false)
                .build(e, light)
            scene?.addEntity(light)
            indirect = IndirectLight.Builder()
                .irradiance(1, floatArrayOf(1f, 1f, 1f))
                .intensity(32000f)
                .build(e)
            scene?.indirectLight = indirect

            val p = UbershaderProvider(e)
            provider = p
            val loader = AssetLoader(e, p, EntityManager.get())
            assetLoader = loader
            val bytes = context.assets.open("dog/cachorro.glb").use { it.readBytes() }
            val buf = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
            buf.put(bytes)
            buf.flip()
            val a = loader.createAsset(buf) ?: throw IllegalStateException("glb")
            val rl = ResourceLoader(e)
            resourceLoader = rl
            rl.loadResources(a)
            a.releaseSourceData()
            asset = a
            scene?.addEntities(a.entities)
            earR = a.getFirstEntityByName("dog_ear_right")
            earL = a.getFirstEntityByName("dog_ear_left")
            tongue = a.getFirstEntityByName("dog_tongue")

            val ui = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
            uiHelper = ui
            ui.isOpaque = false
            ui.renderCallback = object : UiHelper.RendererCallback {
                override fun onNativeWindowChanged(surface: Surface) {
                    val eng = engine ?: return
                    swapChain?.let { eng.destroySwapChain(it) }
                    swapChain = eng.createSwapChain(surface, ui.swapChainFlags)
                }

                override fun onDetachedFromSurface() {
                    val eng = engine
                    swapChain?.let { eng?.destroySwapChain(it) }
                    swapChain = null
                    eng?.flushAndWait()
                }

                override fun onResized(width: Int, height: Int) {
                    viewW = width
                    viewH = height
                    view?.viewport = Viewport(0, 0, width, height)
                    camera?.setProjection(Camera.Projection.ORTHO, 0.0, width.toDouble(), 0.0, height.toDouble(), 0.1, 3000.0)
                    camera?.lookAt(0.0, 0.0, 1000.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
                }
            }
            ui.attachTo(tv)
            running = true
            choreographer.postFrameCallback(frameCb)
        } catch (t: Throwable) {
            qinblyDog3DFailed = true
            destroy()
        }
    }

    private fun setIn(entity: Int, want: Boolean, current: Boolean): Boolean {
        if (entity == 0 || want == current) return current
        if (want) scene?.addEntity(entity) else scene?.removeEntity(entity)
        return want
    }

    private fun drawFrame(frameTimeNanos: Long) {
        val e = engine ?: return
        val sc = swapChain ?: return
        val r = renderer ?: return
        val v = view ?: return
        val ui = uiHelper ?: return
        if (!ui.isReadyToRender || viewW <= 0 || viewH <= 0) return
        updateTransform(e)
        if (r.beginFrame(sc, frameTimeNanos)) {
            r.render(v)
            r.endFrame()
        }
    }

    private fun updateTransform(e: Engine) {
        val a = asset ?: return
        val tm = e.transformManager
        val inst = tm.getInstance(a.root)
        val st = state
        val eyes = st?.eyes
        if (st == null || eyes.isNullOrEmpty()) {
            tm.setTransform(inst, m4Scale(0.0001f))
            return
        }
        val k = max(viewW.toFloat() / st.width, viewH.toFloat() / st.height)
        val offX = (viewW - st.width * k) / 2f
        val offY = (viewH - st.height * k) / 2f
        fun mx(x: Float): Float { val p = x * k + offX; return if (mirror) viewW - p else p }
        fun my(y: Float): Float = y * k + offY
        val (pa, pb) = eyes[0]
        val ax = mx(pa.x); val ay = my(pa.y)
        val bx = mx(pb.x); val by = my(pb.y)
        val lx: Float; val ly: Float; val rx: Float; val ry: Float
        if (ax <= bx) { lx = ax; ly = ay; rx = bx; ry = by } else { lx = bx; ly = by; rx = ax; ry = ay }
        val dist = hypot(rx - lx, ry - ly)
        if (dist < 4f) {
            tm.setTransform(inst, m4Scale(0.0001f))
            return
        }
        val midX = (lx + rx) / 2f
        val midY = (ly + ry) / 2f
        val roll = Math.toDegrees(atan2((ry - ly).toDouble(), (rx - lx).toDouble())).toFloat()
        val f = st.track.face
        val mirrorSign = if (mirror) -1f else 1f
        val yaw = (f?.yaw ?: 0f) * QINBLY_DOG3D_YAW_SIGN * mirrorSign
        val pitch = (f?.pitch ?: 0f) * QINBLY_DOG3D_PITCH_SIGN
        var m = m4Translate(midX, viewH - midY, 0f)
        m = m4Mul(m, m4RotZ(-roll))
        m = m4Mul(m, m4RotY(yaw))
        m = m4Mul(m, m4RotX(pitch))
        m = m4Mul(m, m4Scale(dist))
        tm.setTransform(inst, m)
        // sem cabeça invisível para esconder: some a orelha do lado que vira para trás
        earRIn = setIn(earR, yaw < 38f, earRIn)
        earLIn = setIn(earL, yaw > -38f, earLIn)
        tongueIn = setIn(tongue, (f?.jawOpen ?: 0f) > 0.2f, tongueIn)
    }

    fun destroy() {
        running = false
        try { choreographer.removeFrameCallback(frameCb) } catch (_: Throwable) { }
        try { uiHelper?.detach() } catch (_: Throwable) { }
        val e = engine
        try {
            if (e != null) {
                asset?.let { assetLoader?.destroyAsset(it) }
                assetLoader?.destroy()
                resourceLoader?.destroy()
                provider?.destroyMaterials()
                provider?.destroy()
                indirect?.let { e.destroyIndirectLight(it) }
                if (light != 0) e.destroyEntity(light)
                view?.let { e.destroyView(it) }
                scene?.let { e.destroyScene(it) }
                renderer?.let { e.destroyRenderer(it) }
                swapChain?.let { e.destroySwapChain(it) }
                e.destroy()
            }
        } catch (_: Throwable) { }
        engine = null
        asset = null
        swapChain = null
    }
}

/** Camada 3D do Cachorro por cima da câmera (use no lugar do overlay 2D). */
@Composable
internal fun QinblyDog3DOverlay(state: QinblyFaceState, mirror: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val holder = remember { QinblyDog3DRenderer(context.applicationContext) }
    holder.state = state
    holder.mirror = mirror
    DisposableEffect(Unit) { onDispose { holder.destroy() } }
    AndroidView(
        factory = { ctx -> TextureView(ctx).also { holder.attach(it) } },
        modifier = modifier
    )
}
