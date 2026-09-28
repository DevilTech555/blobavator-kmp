package com.navbyte.blobavatar.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.navbyte.blobavatar.core.BlobatarOptions
import com.navbyte.blobavatar.core.COLOR_EYE
import com.navbyte.blobavatar.core.COLOR_HEAD
import com.navbyte.blobavatar.core.Expression
import com.navbyte.blobavatar.core.Pose
import com.navbyte.blobavatar.core.ambientRampMilliseconds
import com.navbyte.blobavatar.core.easeInOut
import com.navbyte.blobavatar.core.easeOut
import com.navbyte.blobavatar.core.expressionEnterEase
import com.navbyte.blobavatar.core.expressionEnterMilliseconds
import com.navbyte.blobavatar.core.expressionExitMilliseconds
import com.navbyte.blobavatar.core.fadeHex
import com.navbyte.blobavatar.core.hoverEase
import com.navbyte.blobavatar.core.hoverEnterMilliseconds
import com.navbyte.blobavatar.core.hoverExitMilliseconds
import com.navbyte.blobavatar.core.identityPose
import com.navbyte.blobavatar.core.idle
import com.navbyte.blobavatar.core.lerpPose
import com.navbyte.blobavatar.core.motionAt
import com.navbyte.blobavatar.core.emotionDynamicsAt
import kotlinx.coroutines.isActive

/**
 * How ambient motion is activated.
 */
enum class BlobatarAnimation {
    /**
     * Ambient motion (breathe, bob, blink, saccades) ramps in while pointer hovers.
     */
    Hover,

    /**
     * Ambient motion remains continuously active.
     */
    Always,

    /**
     * Ambient motion is disabled.
     */
    None
}

typealias BlobavatarAnimation = BlobatarAnimation

private val AmbientEasing = Easing { easeOut(it.toDouble()).toFloat() }
private val HoverEasing = Easing { hoverEase(it.toDouble()).toFloat() }
private val ExprEnterEasing = Easing { expressionEnterEase(it.toDouble()).toFloat() }
private val ExprExitEasing = Easing { easeInOut(it.toDouble()).toFloat() }

/**
 * Lifecycle-safe animated blobavatar Composable with elapsed-time idle motion
 * and smooth expression morphing.
 *
 * @param name The identity string (email, username, uuid) to generate the avatar from.
 * @param modifier The layout modifier.
 * @param size Fixed square size, or null to fill parent constraints with 1:1 aspect ratio.
 * @param options Deterministic options (palette, tone, hue, backdrop, traits, expression).
 * @param animation Whether ambient motion reacts to hover or remains continuously active.
 * @param active Whether animations may tick and paint.
 * @param respectReducedMotion If true, static rendering is chosen when reduced motion is preferred.
 * @param contentDescription Optional accessibility label.
 */
@Composable
fun AnimatedBlobavatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    options: BlobatarOptions = BlobatarOptions(),
    animation: BlobatarAnimation = BlobatarAnimation.Hover,
    animateEmotions: Boolean = options.animateEmotions,
    active: Boolean = true,
    respectReducedMotion: Boolean = true,
    contentDescription: String? = null
) {
    if (!active) {
        Blobavatar(
            name = name,
            modifier = modifier,
            size = size,
            options = options,
            contentDescription = contentDescription
        )
        return
    }

    val optionsWithoutExpression = remember(options) {
        options.copy(expression = null)
    }

    val renderer = remember(name, optionsWithoutExpression) {
        AnimatedBlobatarRenderer(name, optionsWithoutExpression)
    }

    var hovered by remember { mutableStateOf(false) }

    val ambientAnim = remember {
        Animatable(if (animation == BlobatarAnimation.Always) 1f else 0f)
    }
    LaunchedEffect(animation, hovered) {
        val target = if (animation == BlobatarAnimation.Always || hovered) 1f else 0f
        ambientAnim.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = ambientRampMilliseconds, easing = AmbientEasing)
        )
    }

    val hoverAnim = remember { Animatable(0f) }
    LaunchedEffect(hovered) {
        val duration = if (hovered) hoverEnterMilliseconds else hoverExitMilliseconds
        hoverAnim.animateTo(
            targetValue = if (hovered) 1f else 0f,
            animationSpec = tween(durationMillis = duration, easing = HoverEasing)
        )
    }

    var fromPose by remember { mutableStateOf(options.expression?.pose ?: identityPose) }
    var toPose by remember { mutableStateOf(options.expression?.pose ?: identityPose) }

    var fromHead by remember {
        val p = renderer.paletteFor(options.expression)
        mutableStateOf(p[COLOR_HEAD]!!)
    }
    var toHead by remember {
        val p = renderer.paletteFor(options.expression)
        mutableStateOf(p[COLOR_HEAD]!!)
    }

    var fromEye by remember {
        val p = renderer.paletteFor(options.expression)
        mutableStateOf(p[COLOR_EYE]!!)
    }
    var toEye by remember {
        val p = renderer.paletteFor(options.expression)
        mutableStateOf(p[COLOR_EYE]!!)
    }

    val morphAnim = remember { Animatable(1f) }
    var targetIsIdle by remember { mutableStateOf(options.expression == null || options.expression == idle) }

    var fromExprName by remember { mutableStateOf(options.expression?.name) }
    var toExprName by remember { mutableStateOf(options.expression?.name) }

    LaunchedEffect(options.expression) {
        val currentMorph = morphAnim.value.toDouble()
        val curProgress = if (targetIsIdle) easeInOut(currentMorph) else expressionEnterEase(currentMorph)

        fromPose = lerpPose(fromPose, toPose, curProgress)
        fromHead = fadeHex(fromHead, toHead, curProgress)
        fromEye = fadeHex(fromEye, toEye, curProgress)
        fromExprName = toExprName
        toExprName = options.expression?.name

        toPose = options.expression?.pose ?: identityPose
        val nextPal = renderer.paletteFor(options.expression)
        toHead = nextPal[COLOR_HEAD]!!
        toEye = nextPal[COLOR_EYE]!!

        targetIsIdle = options.expression == null || options.expression == idle
        val duration = if (targetIsIdle) expressionExitMilliseconds else expressionEnterMilliseconds
        val easing = if (targetIsIdle) ExprExitEasing else ExprEnterEasing

        morphAnim.snapTo(0f)
        morphAnim.animateTo(1f, animationSpec = tween(durationMillis = duration, easing = easing))
    }

    var initialMillis by remember { mutableDoubleStateOf(-1.0) }
    var elapsedMillis by remember { mutableDoubleStateOf(0.0) }
    val needsClock = active && (
        animation == BlobatarAnimation.Always ||
        hovered ||
        ambientAnim.value > 0f ||
        hoverAnim.value > 0f ||
        morphAnim.isRunning ||
        (animateEmotions && options.expression != null) ||
        toPose.shake != 0.0 ||
        toPose.rock != 0.0
    )

    LaunchedEffect(needsClock) {
        if (!needsClock) return@LaunchedEffect
        while (isActive) {
            withFrameMillis { frameTimeMillis ->
                if (initialMillis < 0.0) {
                    initialMillis = frameTimeMillis.toDouble()
                }
                elapsedMillis = frameTimeMillis.toDouble() - initialMillis
            }
        }
    }

    val currentMorph = morphAnim.value.toDouble()
    val curProgress = if (targetIsIdle) easeInOut(currentMorph) else expressionEnterEase(currentMorph)
    val curPose = lerpPose(fromPose, toPose, curProgress)
    val curHeadHex = fadeHex(fromHead, toHead, curProgress)
    val curEyeHex = fadeHex(fromEye, toEye, curProgress)

    val activeExprName = when {
        toExprName == "cool" || toExprName == "mischievous" || toExprName == "crying" -> toExprName
        fromExprName == "cool" || fromExprName == "mischievous" || fromExprName == "crying" -> fromExprName
        else -> null
    }
    val accessoryAlpha = when {
        toExprName == "cool" || toExprName == "mischievous" || toExprName == "crying" -> morphAnim.value
        fromExprName == "cool" || fromExprName == "mischievous" || fromExprName == "crying" -> 1f - morphAnim.value
        else -> 0f
    }

    val emotionDynamics = emotionDynamicsAt(
        expression = options.expression,
        elapsedMilliseconds = elapsedMillis,
        enabled = animateEmotions
    )

    val frame = remember(
        elapsedMillis, ambientAnim.value, hoverAnim.value, curPose,
        curHeadHex, curEyeHex, emotionDynamics, activeExprName, accessoryAlpha
    ) {
        val motion = motionAt(
            seeds = renderer.motionSeeds,
            elapsedMilliseconds = elapsedMillis,
            amplitude = ambientAnim.value.toDouble(),
            shake = curPose.shake
        )
        AnimatedBlobatarFrame(
            motion = motion,
            pose = curPose,
            headColor = colorFromHex(curHeadHex),
            eyeColor = colorFromHex(curEyeHex),
            hover = hoverAnim.value.toDouble(),
            amplitude = ambientAnim.value.toDouble(),
            emotionDynamics = emotionDynamics,
            expressionName = activeExprName,
            accessoryAlpha = accessoryAlpha
        )
    }

    val pointerMod = modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Enter -> hovered = true
                    PointerEventType.Exit -> hovered = false
                    PointerEventType.Press -> hovered = true
                    PointerEventType.Release -> hovered = false
                }
            }
        }
    }

    val boxModifier = (if (size != null) pointerMod.size(size) else pointerMod.aspectRatio(1f))
        .semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        }

    Canvas(modifier = boxModifier) {
        renderer.draw(this, frame)
    }
}

@Composable
fun AnimatedBlobatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    options: BlobatarOptions = BlobatarOptions(),
    animation: BlobatarAnimation = BlobatarAnimation.Hover,
    animateEmotions: Boolean = options.animateEmotions,
    active: Boolean = true,
    respectReducedMotion: Boolean = true,
    contentDescription: String? = null
) = AnimatedBlobavatar(name, modifier, size, options, animation, animateEmotions, active, respectReducedMotion, contentDescription)
