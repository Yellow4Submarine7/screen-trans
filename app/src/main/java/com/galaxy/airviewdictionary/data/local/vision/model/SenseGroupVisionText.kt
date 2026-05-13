package com.galaxy.airviewdictionary.data.local.vision.model

import android.graphics.Rect
import com.galaxy.airviewdictionary.data.local.vision.WritingDirection
import com.galaxy.airviewdictionary.data.remote.ai.chatgpt.SenseGroup


/**
 * A [VisionText] that wraps a single [SenseGroup] (semantic chunk inside a [Sentence]),
 * used when the user is in SENSE_GROUP detection mode.
 *
 * - [representation] is the chunk's source text (verbatim from the sentence).
 * - [boundingBox] is the union of the bounding boxes of all words that fall inside
 *   the chunk's character range.
 * - [precomputedTranslation] is the translation returned by ChatGPTKit.chunk();
 *   when non-empty, downstream code skips the normal translation engine and uses
 *   this value directly.
 *
 * Downstream UI ([VisionTextView]) treats this as the "else" branch (not Sentence,
 * not Paragraph), which renders a single overlay box over the union region — exactly
 * what we want for a chunk highlight.
 */
data class SenseGroupVisionText(
    val parentSentence: Sentence,
    val senseGroup: SenseGroup,
    override val boundingBox: Rect,
    override val writingDirection: WritingDirection,
    override val fontHeight: Double,
) : VisionText {

    override val representation: String
        get() = senseGroup.text

    val precomputedTranslation: String
        get() = senseGroup.translation
}
