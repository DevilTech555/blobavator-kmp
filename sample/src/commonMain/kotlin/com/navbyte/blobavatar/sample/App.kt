package com.navbyte.blobavatar.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.navbyte.blobavatar.compose.AnimatedBlobavatar
import com.navbyte.blobavatar.compose.Blobavatar
import com.navbyte.blobavatar.compose.BlobatarAnimation
import com.navbyte.blobavatar.compose.colorFromHex
import com.navbyte.blobavatar.core.Backdrop
import com.navbyte.blobavatar.core.Blobavatar
import com.navbyte.blobavatar.core.BlobavatarOptions
import com.navbyte.blobavatar.core.COLOR_BG
import com.navbyte.blobavatar.core.COLOR_EYE
import com.navbyte.blobavatar.core.COLOR_HEAD
import com.navbyte.blobavatar.core.Expression
import com.navbyte.blobavatar.core.expressions
import com.navbyte.blobavatar.core.idle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

private val SamplePresets = listOf(
    "alain@example.com",
    "ada@lovelace.dev",
    "grace@hopper.org",
    "linus@kernel.org",
    "saturn.v",
    "aurora.borealis",
    "quantum.pixel",
    "neo.matrix",
    "tove.jansson",
    "kasper.ghost"
)

private val ExpressionEmojis = mapOf(
    "idle" to "😐",
    "happy" to "😊",
    "love" to "😍",
    "wink" to "😉",
    "thinking" to "🤔",
    "smug" to "😏",
    "shy" to "😳",
    "surprised" to "😲",
    "sleepy" to "😴",
    "scared" to "😱",
    "unsure" to "😕",
    "sad" to "😢",
    "mad" to "😠",
    "sick" to "🤢",
    "excited" to "🤩",
    "cool" to "😎",
    "dizzy" to "😵",
    "zen" to "😌",
    "mischievous" to "😈",
    "mindblown" to "🤯",
    "crying" to "😭",
    "silly" to "🤪",
    "bored" to "🥱",
    "nervous" to "😬"
)

private enum class StudioTab(val title: String, val icon: String) {
    Emotions("Emotions", "🎭"),
    Appearance("Style & Plate", "🎨"),
    Colors("Palette & Hue", "🌈"),
    Code("Export & SVG", "💻")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6366F1),
            secondary = Color(0xFFA855F7),
            background = Color(0xFF090D16),
            surface = Color(0xFF131B2E),
            surfaceVariant = Color(0xFF1E293B)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var name by remember { mutableStateOf("alain@example.com") }
            var selectedBackdrop by remember { mutableStateOf(Backdrop.CIRCLE) }
            var selectedExpression by remember { mutableStateOf<Expression>(idle) }
            var autoCycleEmotions by remember { mutableStateOf(true) }
            var cycleSpeedMs by remember { mutableLongStateOf(1800L) }
            var animationMode by remember { mutableStateOf(BlobatarAnimation.Always) }
            var animateEmotions by remember { mutableStateOf(true) }

            var customHueEnabled by remember { mutableStateOf(false) }
            var customHue by remember { mutableDoubleStateOf(210.0) }
            var customToneEnabled by remember { mutableStateOf(false) }
            var customTone by remember { mutableDoubleStateOf(0.7) }
            var contrastEnforced by remember { mutableStateOf(true) }

            var selectedTab by remember { mutableStateOf(StudioTab.Emotions) }
            var copiedNotice by remember { mutableStateOf<String?>(null) }

            // Automatic emotion cycling loop
            LaunchedEffect(autoCycleEmotions, cycleSpeedMs) {
                if (!autoCycleEmotions) return@LaunchedEffect
                while (isActive) {
                    delay(cycleSpeedMs)
                    val currentIdx = expressions.indexOfFirst { it.name == selectedExpression.name }
                    val nextIdx = if (currentIdx >= 0) (currentIdx + 1) % expressions.size else 0
                    selectedExpression = expressions[nextIdx]
                }
            }

            // Copied banner auto dismiss
            LaunchedEffect(copiedNotice) {
                if (copiedNotice != null) {
                    delay(2500)
                    copiedNotice = null
                }
            }

            val options = remember(
                selectedBackdrop, selectedExpression, customHueEnabled,
                customHue, customToneEnabled, customTone, contrastEnforced, animateEmotions
            ) {
                BlobavatarOptions(
                    background = selectedBackdrop,
                    expression = selectedExpression,
                    hue = if (customHueEnabled) customHue else null,
                    tone = if (customToneEnabled) customTone else null,
                    contrast = contrastEnforced,
                    animateEmotions = animateEmotions
                )
            }

            val resolvedParts = remember(name, options) {
                Blobavatar.parts(name, options)
            }
            val layout = resolvedParts.first
            val palette = resolvedParts.second
            val headHex = palette[COLOR_HEAD] ?: "#6366F1"
            val eyeHex = palette[COLOR_EYE] ?: "#FFFFFF"
            val bgHex = palette[COLOR_BG] ?: "#1E293B"

            val scrollState = rememberScrollState()

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isWideScreen = maxWidth >= 860.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = if (isWideScreen) 32.dp else 16.dp, vertical = 24.dp)
                ) {
                    // Top App Header
                    StudioHeader(
                        onRandomize = {
                            val r = Random.nextInt(1000, 9999)
                            name = "user-$r@blobavatar.dev"
                        },
                        onReset = {
                            name = "alain@example.com"
                            selectedBackdrop = Backdrop.CIRCLE
                            selectedExpression = idle
                            autoCycleEmotions = true
                            customHueEnabled = false
                            customToneEnabled = false
                            contrastEnforced = true
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Preset Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRESETS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        SamplePresets.forEach { preset ->
                            val isSelected = (name == preset)
                            FilterChip(
                                selected = isSelected,
                                onClick = { name = preset },
                                label = { Text(preset, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6366F1),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF131B2E),
                                    labelColor = Color(0xFF94A3B8)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF1E293B),
                                    selectedBorderColor = Color(0xFF818CF8)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Copied Notice Notification Banner
                    AnimatedVisibility(
                        visible = copiedNotice != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF22C55E).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = copiedNotice ?: "",
                                color = Color(0xFF4ADE80),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // Main Studio Content Area: Split on wide screens, Stacked on narrow screens
                    if (isWideScreen) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Stage: Avatar Showcase
                            Box(modifier = Modifier.weight(1.05f)) {
                                AvatarStage(
                                    name = name,
                                    onNameChange = { name = it },
                                    options = options,
                                    animationMode = animationMode,
                                    animateEmotions = animateEmotions,
                                    onToggleAnimateEmotions = { animateEmotions = it },
                                    headHex = headHex,
                                    eyeHex = eyeHex,
                                    bgHex = bgHex,
                                    layoutShape = layout.shape,
                                    selectedExpression = selectedExpression,
                                    autoCycleEmotions = autoCycleEmotions,
                                    onToggleAutoCycle = { autoCycleEmotions = it },
                                    cycleSpeedMs = cycleSpeedMs,
                                    onSpeedChange = { cycleSpeedMs = it },
                                    onPrevExpression = {
                                        val idx = expressions.indexOfFirst { it.name == selectedExpression.name }
                                        val prev = if (idx > 0) idx - 1 else expressions.size - 1
                                        selectedExpression = expressions[prev]
                                    },
                                    onNextExpression = {
                                        val idx = expressions.indexOfFirst { it.name == selectedExpression.name }
                                        val next = (idx + 1) % expressions.size
                                        selectedExpression = expressions[next]
                                    }
                                )
                            }

                            // Right Stage: Tabbed Inspector Controls
                            Box(modifier = Modifier.weight(1.25f)) {
                                InspectorStage(
                                    selectedTab = selectedTab,
                                    onSelectTab = { selectedTab = it },
                                    name = name,
                                    options = options,
                                    selectedBackdrop = selectedBackdrop,
                                    onBackdropSelect = { selectedBackdrop = it },
                                    selectedExpression = selectedExpression,
                                    onExpressionSelect = { selectedExpression = it },
                                    animationMode = animationMode,
                                    onAnimationModeSelect = { animationMode = it },
                                    animateEmotions = animateEmotions,
                                    onAnimateEmotionsChange = { animateEmotions = it },
                                    customHueEnabled = customHueEnabled,
                                    onCustomHueEnabledChange = { customHueEnabled = it },
                                    customHue = customHue,
                                    onCustomHueChange = { customHue = it },
                                    customToneEnabled = customToneEnabled,
                                    onCustomToneEnabledChange = { customToneEnabled = it },
                                    customTone = customTone,
                                    onCustomToneChange = { customTone = it },
                                    contrastEnforced = contrastEnforced,
                                    onContrastEnforcedChange = { contrastEnforced = it },
                                    layoutShape = layout.shape,
                                    headHex = headHex,
                                    eyeHex = eyeHex,
                                    bgHex = bgHex,
                                    onCopy = { copiedNotice = it }
                                )
                            }
                        }
                    } else {
                        // Narrow layout: Stacked vertically
                        AvatarStage(
                            name = name,
                            onNameChange = { name = it },
                            options = options,
                            animationMode = animationMode,
                            animateEmotions = animateEmotions,
                            onToggleAnimateEmotions = { animateEmotions = it },
                            headHex = headHex,
                            eyeHex = eyeHex,
                            bgHex = bgHex,
                            layoutShape = layout.shape,
                            selectedExpression = selectedExpression,
                            autoCycleEmotions = autoCycleEmotions,
                            onToggleAutoCycle = { autoCycleEmotions = it },
                            cycleSpeedMs = cycleSpeedMs,
                            onSpeedChange = { cycleSpeedMs = it },
                            onPrevExpression = {
                                val idx = expressions.indexOfFirst { it.name == selectedExpression.name }
                                val prev = if (idx > 0) idx - 1 else expressions.size - 1
                                selectedExpression = expressions[prev]
                            },
                            onNextExpression = {
                                val idx = expressions.indexOfFirst { it.name == selectedExpression.name }
                                val next = (idx + 1) % expressions.size
                                selectedExpression = expressions[next]
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        InspectorStage(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                            name = name,
                            options = options,
                            selectedBackdrop = selectedBackdrop,
                            onBackdropSelect = { selectedBackdrop = it },
                            selectedExpression = selectedExpression,
                            onExpressionSelect = { selectedExpression = it },
                            animationMode = animationMode,
                            onAnimationModeSelect = { animationMode = it },
                            animateEmotions = animateEmotions,
                            onAnimateEmotionsChange = { animateEmotions = it },
                            customHueEnabled = customHueEnabled,
                            onCustomHueEnabledChange = { customHueEnabled = it },
                            customHue = customHue,
                            onCustomHueChange = { customHue = it },
                            customToneEnabled = customToneEnabled,
                            onCustomToneEnabledChange = { customToneEnabled = it },
                            customTone = customTone,
                            onCustomToneChange = { customTone = it },
                            contrastEnforced = contrastEnforced,
                            onContrastEnforcedChange = { contrastEnforced = it },
                            layoutShape = layout.shape,
                            headHex = headHex,
                            eyeHex = eyeHex,
                            bgHex = bgHex,
                            onCopy = { copiedNotice = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Variety Gallery Showcase (Cards with Circle Default)
                    VarietyGallery(onSelectSeed = { name = it })
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header Component
// ---------------------------------------------------------------------------
@Composable
private fun StudioHeader(
    onRandomize: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Blobavatar",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "KMP STUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA5B4FC),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = "Deterministic geometric avatars for Kotlin Multiplatform",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onRandomize,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🎲 Randomize", fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onReset,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset", fontSize = 13.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Left Stage: Avatar Showcase Card & Emotion Player
// ---------------------------------------------------------------------------
@Composable
private fun AvatarStage(
    name: String,
    onNameChange: (String) -> Unit,
    options: BlobavatarOptions,
    animationMode: BlobatarAnimation,
    animateEmotions: Boolean,
    onToggleAnimateEmotions: (Boolean) -> Unit,
    headHex: String,
    eyeHex: String,
    bgHex: String,
    layoutShape: String,
    selectedExpression: Expression,
    autoCycleEmotions: Boolean,
    onToggleAutoCycle: (Boolean) -> Unit,
    cycleSpeedMs: Long,
    onSpeedChange: (Long) -> Unit,
    onPrevExpression: () -> Unit,
    onNextExpression: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ambient Backdrop Glow Frame
            val headColor = colorFromHex(headHex)
            val glowBrush = Brush.radialGradient(
                colors = listOf(headColor.copy(alpha = 0.25f), Color.Transparent),
                radius = 280f
            )

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF090D16))
                    .background(glowBrush)
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedBlobavatar(
                    name = name,
                    size = 200.dp,
                    options = options,
                    animation = animationMode,
                    animateEmotions = animateEmotions
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Emotion & Slideshow Player Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (autoCycleEmotions) Color(0xFF1E1B4B) else Color(0xFF0B101D)
                ),
                border = BorderStroke(
                    1.dp,
                    if (autoCycleEmotions) Color(0xFF6366F1).copy(alpha = 0.6f) else Color(0xFF1E293B)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Current Emotion Tag
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val emoji = ExpressionEmojis[selectedExpression.name] ?: "✨"
                            Text(emoji, fontSize = 20.sp)
                            Column {
                                Text(
                                    text = selectedExpression.name.uppercase(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                val activeIdx = expressions.indexOfFirst { it.name == selectedExpression.name } + 1
                                Text(
                                    text = if (autoCycleEmotions) "Auto-Cycle Playing ($activeIdx/${expressions.size})" else "Static Pose ($activeIdx/${expressions.size})",
                                    fontSize = 11.sp,
                                    color = if (autoCycleEmotions) Color(0xFF818CF8) else Color(0xFF64748B)
                                )
                            }
                        }

                        // Play / Pause Toggle Button
                        Button(
                            onClick = { onToggleAutoCycle(!autoCycleEmotions) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (autoCycleEmotions) Color(0xFF6366F1) else Color(0xFF1E293B),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (autoCycleEmotions) "⏸ Pause" else "▶ Auto Cycle", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Scrub Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = onPrevExpression,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("◀ Prev", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onNextExpression,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Next ▶", fontSize = 11.sp)
                            }
                        }

                        // Cycle Speed Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                1200L to "1.2s",
                                1800L to "1.8s",
                                2500L to "2.5s"
                            ).forEach { (speed, label) ->
                                val isSelected = (cycleSpeedMs == speed)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onSpeedChange(speed) },
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dedicated Emotion Personality Motion Quick Toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (animateEmotions) Color(0xFF6366F1).copy(alpha = 0.15f) else Color(0xFF131B2E),
                        border = BorderStroke(
                            1.dp,
                            if (animateEmotions) Color(0xFF6366F1).copy(alpha = 0.5f) else Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🎭 Emotion Personality Motion",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (animateEmotions) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (animateEmotions) "ACTIVE" else "OFF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (animateEmotions) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Switch(
                                checked = animateEmotions,
                                onCheckedChange = onToggleAnimateEmotions,
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seed Input Field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Avatar Seed (Name, Email, UUID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF090D16),
                    unfocusedContainerColor = Color(0xFF090D16),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Color Swatch Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorPill(label = "Head", hex = headHex, modifier = Modifier.weight(1f))
                ColorPill(label = "Eye", hex = eyeHex, modifier = Modifier.weight(1f))
                if (options.background != Backdrop.NONE) {
                    ColorPill(label = "Plate", hex = bgHex, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Right Stage: Tabbed Inspector Controls
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InspectorStage(
    selectedTab: StudioTab,
    onSelectTab: (StudioTab) -> Unit,
    name: String,
    options: BlobavatarOptions,
    selectedBackdrop: Backdrop,
    onBackdropSelect: (Backdrop) -> Unit,
    selectedExpression: Expression,
    onExpressionSelect: (Expression) -> Unit,
    animationMode: BlobatarAnimation,
    onAnimationModeSelect: (BlobatarAnimation) -> Unit,
    animateEmotions: Boolean,
    onAnimateEmotionsChange: (Boolean) -> Unit,
    customHueEnabled: Boolean,
    onCustomHueEnabledChange: (Boolean) -> Unit,
    customHue: Double,
    onCustomHueChange: (Double) -> Unit,
    customToneEnabled: Boolean,
    onCustomToneEnabledChange: (Boolean) -> Unit,
    customTone: Double,
    onCustomToneChange: (Double) -> Unit,
    contrastEnforced: Boolean,
    onContrastEnforcedChange: (Boolean) -> Unit,
    layoutShape: String,
    headHex: String,
    eyeHex: String,
    bgHex: String,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tab Navigation Header
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color(0xFF0D1424),
                contentColor = Color(0xFF818CF8),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                        color = Color(0xFF6366F1),
                        height = 3.dp
                    )
                },
                divider = { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF1E293B))) }
            ) {
                StudioTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectTab(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(tab.icon, fontSize = 14.sp)
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    )
                }
            }

            // Tab Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                when (selectedTab) {
                    StudioTab.Emotions -> {
                        Text(
                            text = "${expressions.size} Deterministic Expressions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Select any expression or let auto-cycle morph them continuously.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Grid of Expression Cards with Emojis
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            expressions.forEach { expr ->
                                val isSelected = (selectedExpression.name == expr.name)
                                val emoji = ExpressionEmojis[expr.name] ?: "✨"

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onExpressionSelect(expr) },
                                    color = if (isSelected) Color(0xFF6366F1) else Color(0xFF090D16),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFA5B4FC) else Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(emoji, fontSize = 14.sp)
                                        Text(
                                            text = expr.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Ambient Idle Animation Modes
                        Text(
                            text = "Ambient Idle Motion",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BlobatarAnimation.entries.forEach { mode ->
                                val isSelected = (animationMode == mode)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onAnimationModeSelect(mode) },
                                    label = { Text(mode.name, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6366F1),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF090D16),
                                        labelColor = Color(0xFF94A3B8)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Emotion Personality Animations Flag Toggle
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                            border = BorderStroke(
                                1.dp,
                                if (animateEmotions) Color(0xFF6366F1).copy(alpha = 0.6f) else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Emotion Personality Motion",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (animateEmotions) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = if (animateEmotions) "ACTIVE" else "OFF",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (animateEmotions) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Procedural micro-animations for each emotion (heartbeat for love, joyful hop for happy, tremor for mad, nods for sleepy, etc.)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Switch(
                                    checked = animateEmotions,
                                    onCheckedChange = onAnimateEmotionsChange,
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
                                )
                            }
                        }
                    }

                    StudioTab.Appearance -> {
                        Text(
                            text = "Backdrop Plate Geometry",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Circle plate is enabled by default. Choose between circular, squircle, or square frames.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Backdrop Plate Selection Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                Backdrop.CIRCLE to "Circle (Default)",
                                Backdrop.SQUIRCLE to "Squircle",
                                Backdrop.SQUARE to "Square",
                                Backdrop.NONE to "None (Transparent)"
                            ).forEach { (plate, title) ->
                                val isSelected = (selectedBackdrop == plate)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onBackdropSelect(plate) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1E1B4B) else Color(0xFF090D16)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF6366F1) else Color(0xFF1E293B)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(
                                                    when (plate) {
                                                        Backdrop.CIRCLE -> CircleShape
                                                        Backdrop.SQUIRCLE -> RoundedCornerShape(10.dp)
                                                        Backdrop.SQUARE -> RoundedCornerShape(4.dp)
                                                        Backdrop.NONE -> RoundedCornerShape(4.dp)
                                                    }
                                                )
                                                .background(
                                                    if (plate == Backdrop.NONE) Color.Transparent
                                                    else Color(0xFF6366F1)
                                                )
                                                .border(1.dp, Color(0xFF818CF8), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Silhouette Info
                        Text(
                            text = "Avatar Silhouette & Geometry",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF090D16),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Active Silhouette: $layoutShape", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text("Generated from Murmur3 32-bit avalanche hash", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Surface(
                                    color = Color(0xFF6366F1).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = layoutShape.uppercase(),
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    StudioTab.Colors -> {
                        Text(
                            text = "Perceptually Uniform OKLCh Color Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Fine-tune hue and tone or leave on Auto to let the seed derive colors deterministically.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Hue Control
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Hue Override", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                        Text(
                                            text = if (customHueEnabled) "${customHue.toInt()}°" else "Auto (Seed-driven)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF818CF8)
                                        )
                                    }
                                    Switch(
                                        checked = customHueEnabled,
                                        onCheckedChange = onCustomHueEnabledChange,
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
                                    )
                                }
                                if (customHueEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Slider(
                                        value = customHue.toFloat(),
                                        onValueChange = { onCustomHueChange(it.toDouble()) },
                                        valueRange = 0f..360f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF818CF8),
                                            activeTrackColor = Color(0xFF6366F1)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Tone Control
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Tone Override (Pastel to Ink)", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                        Text(
                                            text = if (customToneEnabled) "${(customTone * 100).toInt()}%" else "Auto (Seed-driven)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF818CF8)
                                        )
                                    }
                                    Switch(
                                        checked = customToneEnabled,
                                        onCheckedChange = onCustomToneEnabledChange,
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
                                    )
                                }
                                if (customToneEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Slider(
                                        value = customTone.toFloat(),
                                        onValueChange = { onCustomToneChange(it.toDouble()) },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF818CF8),
                                            activeTrackColor = Color(0xFF6366F1)
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // WCAG Contrast Toggle
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                            border = BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("WCAG Contrast Floor", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                    Text("Enforces 4.5:1 relative luminance contrast for eyes and plate floor", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Switch(
                                    checked = contrastEnforced,
                                    onCheckedChange = onContrastEnforcedChange,
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
                                )
                            }
                        }
                    }

                    StudioTab.Code -> {
                        Text(
                            text = "Ready-to-Use Code & SVG Export",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Copy pure SVG markup or Compose Multiplatform code snippet.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Compose Code Snippet
                        Text("Compose Multiplatform Usage", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA5B4FC))
                        Spacer(modifier = Modifier.height(6.dp))
                        val composeSnippet = """Blobavatar(
    name = "$name",
    size = 96.dp,
    options = BlobavatarOptions(
        background = Backdrop.${options.background?.name ?: "CIRCLE"},
        expression = ${options.expression?.name ?: "idle"},
        animateEmotions = $animateEmotions
    )
)"""
                        CodeBox(
                            code = composeSnippet,
                            onCopy = { onCopy("Copied Compose code to clipboard!") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Pure SVG Output
                        Text("Headless SVG Output", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(6.dp))
                        val svg = remember(name, options) {
                            Blobavatar.toSvg(name, options, size = 120)
                        }
                        CodeBox(
                            code = svg,
                            onCopy = { onCopy("Copied SVG markup to clipboard!") }
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers & Subcomponents
// ---------------------------------------------------------------------------
@Composable
private fun ColorPill(label: String, hex: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFF090D16),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colorFromHex(hex))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )
            Column {
                Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Text(text = hex.uppercase(), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CodeBox(code: String, onCopy: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14)),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onCopy() }
                ) {
                    Text(
                        text = "Copy 📋",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF38BDF8),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Bottom Showcase Gallery
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VarietyGallery(onSelectSeed: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Avatar Variety Gallery",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Deterministic variety generated across seeds with default Circle backdrop plate",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SamplePresets.forEach { preset ->
                Card(
                    modifier = Modifier
                        .width(135.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectSeed(preset) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Blobavatar(
                            name = preset,
                            size = 85.dp,
                            options = BlobavatarOptions(background = Backdrop.CIRCLE)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = preset.substringBefore("@"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
