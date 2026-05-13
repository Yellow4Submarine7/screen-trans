package com.galaxy.airviewdictionary.data.remote.ai.chatgpt

/**
 * A semantic chunk inside a sentence — e.g. "who lives next door" inside
 * "The man who lives next door is a doctor". Returned by ChatGPTKit.chunk().
 *
 * @property text         The chunk text exactly as it appears in the source sentence.
 * @property translation  Pre-computed translation of the chunk.
 * @property charRange    Character offsets [start, end) of the chunk inside the source sentence.
 */
data class SenseGroup(
    val text: String,
    val translation: String,
    val charRange: IntRange,
)
