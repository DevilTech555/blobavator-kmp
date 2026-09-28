package com.navbyte.blobavatar.core

internal expect fun nfcNormalize(s: String): String

/**
 * Seed hashing and normalization.
 *
 * Kotlin Multiplatform port of `packages/blobatar/src/hash.ts` at blobatar 2.4.0.
 * Two guarantees this file exists to provide:
 *
 * 1. Avalanche — "alain" and "alaim" must produce visually unrelated
 *    blobatars. Plain FNV-1a does not give you this; the murmur3 finalizer does.
 * 2. Streaming — the seed is hashed once, then each trait key continues from
 *    that state. Trait values are therefore independent of one another, so
 *    adding a trait in a later version cannot disturb existing blobatars.
 *
 * The JavaScript uint32 semantics (multiplication overflow, signed shifts,
 * the [0, 1) stream range) are reproduced exactly.
 */
object Hash {
    private const val SEP: Byte = 0xff.toByte()

    /**
     * JavaScript `Math.imul` — 32-bit integer multiplication, returned as a signed int32.
     * In Kotlin / JVM, 32-bit integer multiplication automatically wraps with two's complement.
     */
    fun imul(a: Int, b: Int): Int = a * b

    /**
     * JavaScript's `| 0` view of an int: the low 32 bits, signed.
     */
    fun toInt32(v: Int): Int = v

    /**
     * Mixes bytes into a 32-bit state.
     */
    internal fun feed(h: Int, bytes: ByteArray): Int {
        var state = h
        for (b in bytes) {
            val byte = b.toInt() and 0xFF
            state = imul(state xor byte, 3432918353.toInt())
            state = toInt32((state shl 13) or (state ushr 19))
        }
        return state
    }

    /**
     * murmur3 fmix32 — a bijection on uint32 with full avalanche.
     */
    internal fun finalize(h: Int): Long {
        var state = h
        state = imul(state xor (state ushr 16), 2246822507.toInt())
        state = imul(state xor (state ushr 13), 3266489909.toInt())
        val fin = state xor (state ushr 16)
        return fin.toLong() and 0xFFFFFFFFL
    }

    /**
     * Normalizes a seed so that inputs a human considers equal hash equally.
     *
     * NFC first, so precomposed "é" and decomposed "é" agree; then trim, then
     * lowercase. Without this, `Alain@x.com` and `alain@x.com` produce different
     * blobatars for the same person.
     */
    fun normalizeSeed(seed: String): String = jsToLower(nfcNormalize(seed).trim())

    private fun jsToLower(s: String): String {
        var needsWork = false
        for (i in 0 until s.length) {
            val u = s[i].code
            if (u == 0x0130 || u == 0x03A3) {
                needsWork = true
                break
            }
        }
        if (!needsWork) return s.lowercase()

        val units = IntArray(s.length) { s[it].code }
        val out = StringBuilder()
        for (i in units.indices) {
            val u = units[i]
            if (u == 0x0130) {
                out.append("i\u0307")
            } else if (u == 0x03A3 && precededByCased(units, i) && !followedByCased(units, i)) {
                out.append('\u03C2')
            } else {
                out.append(u.toChar())
            }
        }
        return out.toString().lowercase()
    }

    private fun precededByCased(units: IntArray, i: Int): Boolean {
        for (j in i - 1 downTo 0) {
            val u = units[j]
            if (isCased(u)) return true
            if (!isCaseIgnorable(u)) return false
        }
        return false
    }

    private fun followedByCased(units: IntArray, i: Int): Boolean {
        for (j in i + 1 until units.size) {
            val u = units[j]
            if (isCased(u)) return true
            if (!isCaseIgnorable(u)) return false
        }
        return false
    }

    private fun isCaseIgnorable(u: Int): Boolean =
        (u in 0x0300..0x036F) || u == 0x00AD || u == 0x200B

    private fun isCased(u: Int): Boolean =
        (u in 0x41..0x5A) ||
        (u in 0x61..0x7A) ||
        (u in 0xC0..0x24F && u != 0xD7 && u != 0xF7) ||
        (u in 0x370..0x3FF) ||
        (u in 0x400..0x4FF) ||
        (u in 0x1E00..0x1FFF)

    /**
     * Hashes the seed once into a reusable state. Non-ASCII seeds are encoded to
     * UTF-8 bytes first, so hashing is over codepoints rather than UTF-16 units.
     */
    fun seedState(seed: String, normalize: Boolean = true): Int {
        val s = if (normalize) normalizeSeed(seed) else seed
        val bytes = s.encodeToByteArray()
        return feed(1779033703 xor s.length, bytes)
    }

    /**
     * Derives one uniform float in [0, 1) for `key`, independent of every other key.
     */
    fun stream(state: Int, key: String): Double {
        val withSep = feed(state, byteArrayOf(SEP))
        val withKey = feed(withSep, key.encodeToByteArray())
        return finalize(withKey).toDouble() / 4294967296.0
    }
}
