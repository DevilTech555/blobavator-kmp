package com.navbyte.blobavatar.core

typealias TraitOverrides = Map<String, Any>

/**
 * Trait reader over one hashed seed.
 *
 * Kotlin Multiplatform port of `packages/blobatar/src/traits.ts` at blobatar 2.4.0.
 *
 * Every value is addressed by a string key rather than drawn from a
 * sequential stream, so trait keys are an append-only namespace: introducing
 * a new key in a later version leaves every other trait — and therefore every
 * existing blobatar — untouched.
 */
class Traits(
    val state: Int,
    private val overrides: Map<String, Any>? = null
) {
    /**
     * Uniform float in [0, 1).
     */
    operator fun invoke(key: String): Double {
        val v = overrides?.get(key)
        var o: Double? = null
        if (v is List<*>) {
            if (v.isNotEmpty()) {
                val index = (Hash.stream(state, key) * v.size).toInt()
                val chosen = v.getOrNull(index)
                if (chosen is Number) {
                    o = chosen.toDouble()
                }
            }
        } else if (v is Number) {
            o = v.toDouble()
        }

        if (o == null) return Hash.stream(state, key)
        if (o > 0.0) return if (o < 1.0) o else 0.999999
        return 0.0
    }

    /**
     * Uniform float in [min, max).
     */
    fun numIn(key: String, min: Double, max: Double): Double =
        min + invoke(key) * (max - min)

    /**
     * Uniform integer from `min` through `max`, inclusive.
     */
    fun intIn(key: String, min: Int, max: Int): Int =
        min + (invoke(key) * (max - min + 1)).toInt()

    /**
     * Uniform choice from `options`.
     */
    fun <T> pick(key: String, options: List<T>): T =
        options[(invoke(key) * options.size).toInt()]

    /**
     * True with probability `p`.
     */
    fun boolIn(key: String, p: Double = 0.5): Boolean =
        invoke(key) < p

    /**
     * Symmetric jitter in [-amount, amount).
     */
    fun jitter(key: String, amount: Double): Double =
        (invoke(key) * 2.0 - 1.0) * amount
}

/**
 * Builds the trait reader for a seed.
 */
fun traitsFor(
    seed: String,
    normalize: Boolean = true,
    overrides: TraitOverrides? = null
): Traits = Traits(Hash.seedState(seed, normalize = normalize), overrides)
