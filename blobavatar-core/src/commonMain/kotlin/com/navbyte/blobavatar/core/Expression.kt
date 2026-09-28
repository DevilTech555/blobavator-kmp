package com.navbyte.blobavatar.core

/**
 * Expression poses for the generation-2 blobatar engine.
 *
 * Kotlin Multiplatform port of `packages/blobatar/src/expression.ts` at blobatar 2.4.0.
 */
data class Pose(
    val esx: Double = 1.0,
    val esy: Double = 1.0,
    val tilt: Double = 0.0,
    val edy: Double = 0.0,
    val edx: Double = 0.0,
    val esx2: Double = 0.0,
    val esy2: Double = 0.0,
    val tilt2: Double = 0.0,
    val edy2: Double = 0.0,
    val lock: Double = 0.0,
    val heat: Double = 0.0,
    val shake: Double = 0.0,
    val rock: Double = 0.0,
    val bdy: Double = 0.0
)

/**
 * A named pose and its optional palette tint target.
 */
data class Expression(
    val name: String,
    val pose: Pose,
    val tint: Tint? = null
)

val identityPose = Pose()

val idle = Expression("idle", identityPose)

val happy = Expression(
    "happy",
    Pose(
        esx = 1.72,
        esy = 0.3,
        tilt = 8.0,
        edy = -1.5,
        edx = 1.5,
        esx2 = 0.08,
        esy2 = 0.05,
        tilt2 = -16.0,
        lock = 1.0,
        bdy = -2.2
    )
)

val sad = Expression(
    "sad",
    Pose(
        esx = 0.6,
        esy = 0.56,
        tilt = 26.0,
        edy = 3.6,
        edx = 1.9,
        esx2 = -0.05,
        esy2 = -0.07,
        tilt2 = -7.0,
        lock = 1.0,
        bdy = 2.6
    )
)

val mad = Expression(
    "mad",
    Pose(
        esx = 1.85,
        esy = 0.26,
        tilt = -33.0,
        edy = 0.4,
        edx = 0.6,
        esy2 = -0.03,
        tilt2 = 5.0,
        lock = 1.0,
        heat = 0.62,
        shake = 0.55,
        bdy = 0.8
    ),
    tint = hot
)

val surprised = Expression(
    "surprised",
    Pose(
        esx = 1.34,
        esy = 1.2,
        tilt = -6.0,
        edy = -1.05,
        edx = 0.5,
        esx2 = 0.05,
        esy2 = 0.07,
        tilt2 = 3.0,
        lock = 1.0,
        bdy = -1.4
    )
)

val wink = Expression(
    "wink",
    Pose(
        esx = 1.32,
        esy = 0.76,
        tilt = 5.0,
        edy = -0.6,
        edx = 0.8,
        esx2 = 0.26,
        esy2 = -0.56,
        tilt2 = -11.0,
        lock = 1.0,
        bdy = -1.1
    )
)

val sleepy = Expression(
    "sleepy",
    Pose(
        esx = 1.14,
        esy = 0.22,
        edy = 2.4,
        edx = 0.3,
        esx2 = -0.04,
        esy2 = 0.03,
        tilt2 = 4.0,
        lock = 1.0,
        bdy = 1.2
    )
)

val smug = Expression(
    "smug",
    Pose(
        esx = 1.3,
        esy = 0.42,
        tilt = 18.0,
        edy = -0.5,
        edx = 0.5,
        esx2 = 0.06,
        esy2 = -0.06,
        tilt2 = -36.0,
        lock = 1.0,
        bdy = -1.0
    )
)

val unsure = Expression(
    "unsure",
    Pose(
        esx = 0.95,
        esy = 1.02,
        tilt = 4.0,
        edy = -0.2,
        edx = 0.3,
        esx2 = 0.24,
        esy2 = -0.44,
        tilt2 = -18.0,
        lock = 1.0
    )
)

val scared = Expression(
    "scared",
    Pose(
        esx = 0.78,
        esy = 0.96,
        tilt = -12.0,
        edy = -1.5,
        edx = -0.8,
        esx2 = -0.04,
        esy2 = 0.05,
        tilt2 = 4.0,
        lock = 1.0,
        shake = 0.35,
        bdy = -0.6
    )
)

val love = Expression(
    "love",
    Pose(
        esx = 0.86,
        esy = 1.28,
        tilt = -14.0,
        edy = -0.5,
        edx = -0.35,
        esx2 = 0.05,
        esy2 = 0.06,
        tilt2 = 6.0,
        lock = 1.0,
        heat = 0.6,
        bdy = -1.6
    ),
    tint = rose
)

val shy = Expression(
    "shy",
    Pose(
        esx = 0.62,
        esy = 0.5,
        tilt = 10.0,
        edy = 1.4,
        edx = -0.2,
        esx2 = -0.05,
        esy2 = -0.04,
        tilt2 = -8.0,
        lock = 1.0,
        heat = 0.55,
        bdy = 0.9
    ),
    tint = blush
)

val sick = Expression(
    "sick",
    Pose(
        esx = 1.25,
        esy = 0.34,
        tilt = 20.0,
        edy = 1.8,
        edx = 0.8,
        esx2 = 0.05,
        esy2 = -0.05,
        tilt2 = -6.0,
        lock = 1.0,
        heat = 0.6,
        shake = 0.18,
        bdy = 1.4
    ),
    tint = bile
)

val thinking = Expression(
    "thinking",
    Pose(
        esx = 1.15,
        esy = 0.62,
        edy = 4.2,
        edx = 0.4,
        esx2 = 0.02,
        esy2 = 0.06,
        edy2 = -8.4,
        lock = 1.0,
        rock = 0.8,
        bdy = -0.4
    )
)

val excited = Expression(
    "excited",
    Pose(
        esx = 1.6,
        esy = 1.5,
        tilt = -5.0,
        edy = -2.2,
        edx = 0.5,
        esx2 = 0.05,
        esy2 = 0.05,
        tilt2 = 10.0,
        lock = 1.0,
        heat = 0.45,
        bdy = -2.0
    ),
    tint = gold
)

val cool = Expression(
    "cool",
    Pose(
        esx = 1.35,
        esy = 0.45,
        tilt = 12.0,
        edy = 0.2,
        edx = 0.6,
        esx2 = 0.05,
        esy2 = -0.05,
        tilt2 = -24.0,
        lock = 1.0,
        bdy = -0.6
    )
)

val dizzy = Expression(
    "dizzy",
    Pose(
        esx = 1.4,
        esy = 0.6,
        tilt = 42.0,
        edy = 1.8,
        edx = 0.8,
        esx2 = -0.6,
        esy2 = 0.8,
        tilt2 = -65.0,
        edy2 = -1.2,
        lock = 1.0,
        heat = 0.4,
        shake = 0.25,
        rock = 0.7,
        bdy = 0.8
    ),
    tint = bile
)

val zen = Expression(
    "zen",
    Pose(
        esx = 1.4,
        esy = 0.22,
        tilt = 10.0,
        edy = 0.6,
        edx = 0.4,
        esx2 = 0.0,
        esy2 = 0.0,
        tilt2 = -20.0,
        lock = 1.0,
        bdy = -1.5
    )
)

val mischievous = Expression(
    "mischievous",
    Pose(
        esx = 1.45,
        esy = 0.42,
        tilt = -25.0,
        edy = -0.8,
        edx = 1.2,
        esx2 = 0.08,
        esy2 = -0.08,
        tilt2 = 8.0,
        lock = 1.0,
        heat = 0.65,
        bdy = -0.5
    ),
    tint = violet
)

val mindblown = Expression(
    "mindblown",
    Pose(
        esx = 1.85,
        esy = 1.7,
        tilt = -4.0,
        edy = -3.5,
        edx = 0.2,
        esx2 = 0.05,
        esy2 = 0.05,
        tilt2 = 8.0,
        lock = 1.0,
        heat = 0.6,
        shake = 0.45,
        bdy = -2.5
    ),
    tint = frost
)

val crying = Expression(
    "crying",
    Pose(
        esx = 1.2,
        esy = 0.35,
        tilt = 30.0,
        edy = 3.8,
        edx = 1.2,
        esx2 = -0.05,
        esy2 = 0.05,
        tilt2 = -15.0,
        lock = 1.0,
        heat = 0.55,
        shake = 0.3,
        bdy = 3.2
    ),
    tint = blush
)

val silly = Expression(
    "silly",
    Pose(
        esx = 1.7,
        esy = 1.6,
        tilt = 28.0,
        edy = -1.0,
        edx = -0.6,
        esx2 = -0.9,
        esy2 = -1.1,
        tilt2 = -48.0,
        edy2 = 2.5,
        lock = 1.0,
        rock = 0.6,
        bdy = 0.5
    )
)

val bored = Expression(
    "bored",
    Pose(
        esx = 1.25,
        esy = 0.25,
        tilt = 6.0,
        edy = 2.0,
        edx = 0.2,
        esx2 = 0.02,
        esy2 = 0.02,
        tilt2 = -12.0,
        lock = 1.0,
        bdy = 1.8
    )
)

val nervous = Expression(
    "nervous",
    Pose(
        esx = 1.2,
        esy = 1.1,
        tilt = -8.0,
        edy = -0.4,
        edx = 0.3,
        esx2 = -0.05,
        esy2 = 0.05,
        tilt2 = 4.0,
        lock = 1.0,
        heat = 0.4,
        shake = 0.5,
        bdy = 1.0
    ),
    tint = blush
)

val standardExpressions: List<Expression> = listOf(
    idle,
    happy,
    sad,
    mad,
    surprised,
    wink,
    sleepy,
    smug,
    unsure,
    scared,
    love,
    shy,
    sick,
    thinking
)

val expressions: List<Expression> = standardExpressions + listOf(
    excited,
    cool,
    dizzy,
    zen,
    mischievous,
    mindblown,
    crying,
    silly,
    bored,
    nervous
)

/**
 * Bakes a static pose into the eye geometry and body offset.
 */
fun bakePose(layout: BlobatarLayout, pose: Pose): BlobatarLayout {
    return BlobatarLayout(
        shape = layout.shape,
        body = layout.body,
        face = layout.face,
        eyes = layout.eyes.mapIndexed { i, eye ->
            val side = if (i == 0) -1.0 else 1.0
            Eye(
                cx = eye.cx + pose.edx * side,
                cy = eye.cy + pose.edy + (if (i == 1) pose.edy2 else 0.0),
                rx = eye.rx * (pose.esx + (if (i == 1) pose.esx2 else 0.0)),
                ry = eye.ry * (pose.esy + (if (i == 1) pose.esy2 else 0.0)),
                n = eye.n,
                rot = eye.rot * (1.0 - pose.lock) + (pose.tilt + (if (i == 1) pose.tilt2 else 0.0)) * side
            )
        },
        petals = layout.petals,
        extra = layout.extra,
        draw = layout.draw,
        bodyOffsetY = pose.bdy
    )
}

/**
 * Resolves a tinting expression against the palette it is actually wearing.
 */
fun expressionPalette(palette: Palette, expression: Expression): Palette {
    val target = expression.tint ?: return palette
    val (head, eye) = tinted(palette[COLOR_HEAD]!!, palette[COLOR_EYE]!!, target)
    return palette + mapOf(
        COLOR_HEAD to mixHex(palette[COLOR_HEAD]!!, head, expression.pose.heat),
        COLOR_EYE to mixHex(palette[COLOR_EYE]!!, eye, expression.pose.heat)
    )
}
