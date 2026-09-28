package com.navbyte.blobavatar.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParityTest {

    private fun closeEnough(actual: Double, expected: Double, tol: Double = 1e-9): Boolean {
        if (actual == expected) return true
        if (actual.isNaN() && expected.isNaN()) return true
        val scale = max(1.0, max(abs(actual), abs(expected)))
        return abs(actual - expected) <= tol * scale
    }

    private fun loadVectors(): JsonObject {
        val stream = javaClass.classLoader.getResourceAsStream("reference-vectors.json")
            ?: error("Resource reference-vectors.json not found")
        val text = stream.bufferedReader().use { it.readText() }
        return Json.parseToJsonElement(text).jsonObject
    }

    @Test
    fun testHashVectors() {
        val vectors = loadVectors()
        val hashList = vectors["hash"]!!.jsonArray

        for (elem in hashList) {
            val obj = elem.jsonObject
            val seed = obj["seed"]!!.jsonPrimitive.content
            val normalized = obj["normalized"]!!.jsonPrimitive.content
            val state = obj["state"]!!.jsonPrimitive.int

            assertEquals(normalized, Hash.normalizeSeed(seed), "normalizeSeed($seed)")
            assertEquals(state, Hash.seedState(seed), "seedState($seed)")

            val streams = obj["streams"]!!.jsonObject
            for ((key, v) in streams) {
                val expected = v.jsonPrimitive.double
                val actual = Hash.stream(state, key)
                assertEquals(expected, actual, "stream($seed, $key)")
            }
        }
    }

    @Test
    fun testOverrideVectors() {
        val vectors = loadVectors()
        val overrideList = vectors["overrides"]!!.jsonArray

        for (elem in overrideList) {
            val obj = elem.jsonObject
            val seed = obj["seed"]!!.jsonPrimitive.content
            val overridesJson = obj["overrides"]!!.jsonObject
            val overrides = mutableMapOf<String, Any>()
            for ((k, v) in overridesJson) {
                if (v is kotlinx.serialization.json.JsonArray) {
                    overrides[k] = v.map { it.jsonPrimitive.double }
                } else {
                    overrides[k] = v.jsonPrimitive.double
                }
            }

            val t = traitsFor(seed, overrides = if (overrides.isEmpty()) null else overrides)
            val values = obj["values"]!!.jsonObject
            for ((k, v) in values) {
                val expected = v.jsonPrimitive.double
                val actual = t(k)
                assertEquals(expected, actual, "$seed $k: $actual != $expected")
            }
        }
    }

    @Test
    fun testPaletteVectors() {
        val vectors = loadVectors()
        val paletteList = vectors["palette"]!!.jsonArray

        for (elem in paletteList) {
            val obj = elem.jsonObject
            val hue = obj["hue"]!!.jsonPrimitive.double
            val tone = obj["tone"]!!.jsonPrimitive.double
            val hex = obj["hex"]!!.jsonObject

            val enforced = palette(hue, true, tone)
            assertEquals(hex["bg"]!!.jsonPrimitive.content, enforced[COLOR_BG], "bg hex h=$hue t=$tone")
            assertEquals(hex["head"]!!.jsonPrimitive.content, enforced[COLOR_HEAD], "head hex h=$hue t=$tone")
            assertEquals(hex["eye"]!!.jsonPrimitive.content, enforced[COLOR_EYE], "eye hex h=$hue t=$tone")

            val unenforced = ramp(hue, false, tone)
            val raw = obj["rampUnenforced"]!!.jsonObject
            for (key in listOf("bg", "head", "eye")) {
                val exp = raw[key]!!.jsonObject
                val actual = unenforced[key]!!
                assertTrue(
                    closeEnough(actual.l, exp["l"]!!.jsonPrimitive.double),
                    "$key.l h=$hue t=$tone"
                )
                assertTrue(
                    closeEnough(actual.c, exp["c"]!!.jsonPrimitive.double),
                    "$key.c h=$hue t=$tone"
                )
                assertTrue(
                    closeEnough(actual.h, exp["h"]!!.jsonPrimitive.double),
                    "$key.h h=$hue t=$tone"
                )
            }
        }
    }

    @Test
    fun testLayoutCases() {
        val vectors = loadVectors()
        val cases = vectors["cases"]!!.jsonArray

        var failures = 0
        var firstFailure: String? = null

        for (c in cases) {
            val m = c.jsonObject
            val seed = m["seed"]!!.jsonPrimitive.content
            val optsJson = m["options"]!!.jsonObject

            val traitsMap = if (optsJson.containsKey("traits")) {
                val map = mutableMapOf<String, Any>()
                for ((k, v) in optsJson["traits"]!!.jsonObject) {
                    if (v is kotlinx.serialization.json.JsonArray) {
                        map[k] = v.map { it.jsonPrimitive.double }
                    } else {
                        map[k] = v.jsonPrimitive.double
                    }
                }
                map
            } else null

            val opts = BlobatarOptions(
                traits = traitsMap,
                hue = optsJson["hue"]?.jsonPrimitive?.double,
                tone = optsJson["tone"]?.jsonPrimitive?.double,
                contrast = optsJson["contrast"]?.jsonPrimitive?.boolean ?: true,
                normalize = optsJson["normalize"]?.jsonPrimitive?.boolean ?: true
            )

            val l = layoutFor(seed, opts)
            val expectedShape = m["shape"]!!.jsonPrimitive.content
            if (l.shape != expectedShape) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: shape ${l.shape} != $expectedShape"
                continue
            }

            val body = m["body"]!!.jsonObject
            if (!closeEnough(l.body.cx, body["cx"]!!.jsonPrimitive.double)) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: body.cx"
                continue
            }
            if (!closeEnough(l.body.cy, body["cy"]!!.jsonPrimitive.double)) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: body.cy"
                continue
            }
            if (!closeEnough(l.body.rx, body["rx"]!!.jsonPrimitive.double)) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: body.rx"
                continue
            }
            if (!closeEnough(l.body.ry, body["ry"]!!.jsonPrimitive.double)) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: body.ry"
                continue
            }
            if (!closeEnough(l.body.n, body["n"]!!.jsonPrimitive.double)) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: body.n"
                continue
            }
            if (!closeEnough(l.body.rot, body["rot"]!!.jsonPrimitive.double)) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: body.rot"
                continue
            }

            val radii = body["radii"]!!.jsonArray
            for (i in radii.indices) {
                if (!closeEnough(l.body.radii[i], radii[i].jsonPrimitive.double)) {
                    failures++
                    if (firstFailure == null) firstFailure = "seed=$seed: radii[$i]"
                    break
                }
            }

            val extra = m["extra"]!!.jsonArray.map { it.jsonPrimitive.content }
            val actualExtra = l.extra.map { it.toPathData() }
            if (actualExtra != extra) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: extra $actualExtra != $extra"
                continue
            }

            val bodyPath = m["bodyPath"]!!.jsonPrimitive.content
            if (l.bodyPath().toPathData() != bodyPath) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: bodyPath ${l.bodyPath().toPathData()} != $bodyPath"
                continue
            }

            val eyePaths = m["eyePaths"]!!.jsonArray.map { it.jsonPrimitive.content }
            val actualEyes = l.eyePaths().map { it.toPathData() }
            if (actualEyes != eyePaths) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: eyePaths $actualEyes != $eyePaths"
                continue
            }

            val pal = m["palette"]!!.jsonObject
            val (_, resolved) = partsFor(seed, opts)
            if (resolved[COLOR_BG] != pal["bg"]!!.jsonPrimitive.content ||
                resolved[COLOR_HEAD] != pal["head"]!!.jsonPrimitive.content ||
                resolved[COLOR_EYE] != pal["eye"]!!.jsonPrimitive.content
            ) {
                failures++
                if (firstFailure == null) firstFailure = "seed=$seed: palette mismatch"
                continue
            }
        }

        assertEquals(0, failures, firstFailure)
    }

    @Test
    fun testExpressionVectors() {
        val vectors = loadVectors()
        val expressionMap = standardExpressions.associateBy { it.name }
        assertEquals(14, standardExpressions.size)

        val expressionVectors = vectors["expressions"]?.jsonObject ?: return
        assertEquals(expressionMap.keys.sorted(), expressionVectors.keys.sorted())

        for ((name, dataElem) in expressionVectors) {
            val expr = expressionMap[name]!!
            val data = dataElem.jsonObject
            val expectedPose = data["pose"]!!.jsonObject

            assertEquals(expectedPose["esx"]?.jsonPrimitive?.double ?: 1.0, expr.pose.esx, "$name.esx")
            assertEquals(expectedPose["esy"]?.jsonPrimitive?.double ?: 1.0, expr.pose.esy, "$name.esy")
            assertEquals(expectedPose["tilt"]?.jsonPrimitive?.double ?: 0.0, expr.pose.tilt, "$name.tilt")
            assertEquals(expectedPose["edy"]?.jsonPrimitive?.double ?: 0.0, expr.pose.edy, "$name.edy")
            assertEquals(expectedPose["edx"]?.jsonPrimitive?.double ?: 0.0, expr.pose.edx, "$name.edx")
            assertEquals(expectedPose["esx2"]?.jsonPrimitive?.double ?: 0.0, expr.pose.esx2, "$name.esx2")
            assertEquals(expectedPose["esy2"]?.jsonPrimitive?.double ?: 0.0, expr.pose.esy2, "$name.esy2")
            assertEquals(expectedPose["tilt2"]?.jsonPrimitive?.double ?: 0.0, expr.pose.tilt2, "$name.tilt2")
            assertEquals(expectedPose["edy2"]?.jsonPrimitive?.double ?: 0.0, expr.pose.edy2, "$name.edy2")
            assertEquals(expectedPose["lock"]?.jsonPrimitive?.double ?: 0.0, expr.pose.lock, "$name.lock")
            assertEquals(expectedPose["heat"]?.jsonPrimitive?.double ?: 0.0, expr.pose.heat, "$name.heat")
            assertEquals(expectedPose["shake"]?.jsonPrimitive?.double ?: 0.0, expr.pose.shake, "$name.shake")
            assertEquals(expectedPose["rock"]?.jsonPrimitive?.double ?: 0.0, expr.pose.rock, "$name.rock")
            assertEquals(expectedPose["bdy"]?.jsonPrimitive?.double ?: 0.0, expr.pose.bdy, "$name.bdy")
        }
    }

    @Test
    fun testMotionSeeds() {
        val alain = motionSeedsFor("alain")
        assertEquals(508, alain.phase)
        assertEquals(1170, alain.bob)
        assertEquals(3619, alain.blink)
        assertEquals(2957, alain.blinkPhase)
        assertEquals(6179, alain.saccade)
        assertEquals(1350, alain.saccadePhase)
        assertEquals(1.95, alain.lookX)
        assertEquals(-0.97, alain.lookY)
        assertEquals(1.95, alain.lookMagnitudeX)
        assertEquals(0.97, alain.lookMagnitudeY)

        val ada = motionSeedsFor("ada")
        assertEquals(455, ada.phase)
        assertEquals(328, ada.bob)
        assertEquals(5328, ada.blink)
        assertEquals(58, ada.blinkPhase)
        assertEquals(5844, ada.saccade)
        assertEquals(561, ada.saccadePhase)
        assertEquals(-1.78, ada.lookX)
        assertEquals(-1.36, ada.lookY)
        assertEquals(1.78, ada.lookMagnitudeX)
        assertEquals(1.36, ada.lookMagnitudeY)
    }

    @Test
    fun testSvgRendering() {
        val svg = SvgRenderer.toSvg("alain", size = 64)
        assertTrue(svg.startsWith("<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\" width=\"64\" height=\"64\">"))
        assertTrue(svg.endsWith("</svg>"))
        assertTrue(svg.contains("<g fill="))
    }

    @Test
    fun testExtendedExpressions() {
        assertEquals(24, expressions.size)
        val names = expressions.map { it.name }
        assertEquals(24, names.toSet().size, "All expression names must be unique")
        assertTrue(names.containsAll(listOf(
            "excited", "cool", "dizzy", "zen", "mischievous",
            "mindblown", "crying", "silly", "bored", "nervous"
        )))
    }

    @Test
    fun testSvgAccessories() {
        val coolSvg = SvgRenderer.toSvg("alain", BlobatarOptions(expression = cool))
        assertTrue(coolSvg.contains("#090D16"), "Cool SVG should contain cooling glasses frames")
        assertTrue(coolSvg.contains("#38BDF8"), "Cool SVG should contain cooling glasses glint")

        val mischSvg = SvgRenderer.toSvg("alain", BlobatarOptions(expression = mischievous))
        assertTrue(mischSvg.contains("#E11D48"), "Mischievous SVG should contain horns fill")
        assertTrue(mischSvg.contains("#FDA4AF"), "Mischievous SVG should contain horns accent")

        val cryingSvg = SvgRenderer.toSvg("alain", BlobatarOptions(expression = crying))
        assertTrue(cryingSvg.contains("#38BDF8"), "Crying SVG should contain tears fill")
        assertTrue(cryingSvg.contains("#E0F2FE"), "Crying SVG should contain tears highlight")
    }
}
