package com.galaxy.airviewdictionary.data.local.vision.model

import android.graphics.Rect
import com.galaxy.airviewdictionary.data.local.vision.WritingDirection
import com.galaxy.airviewdictionary.data.remote.ai.chatgpt.SenseGroup
import com.galaxy.airviewdictionary.extensions._unionWith


/**
 * 어러 개의 [Line] 으로 이루어진 문장
 */
data class Sentence(
    val lines: MutableList<Line>,
    override val writingDirection: WritingDirection,
    override val fontHeight: Double
) : VisionText {

    private var boundingBoxCache: Rect? = null
    private var linesHashCodeCache: Int? = null

    override val boundingBox: Rect
        get() {
            val currentWordsHashCode = lines.hashCode()
            if (boundingBoxCache == null || linesHashCodeCache != currentWordsHashCode) {
                boundingBoxCache = if (lines.isEmpty()) {
                    Rect()
                } else {
                    lines.map { it.boundingBox }.reduce { acc, rect -> acc._unionWith(rect) }
                }
                linesHashCodeCache = currentWordsHashCode
            }
            return boundingBoxCache!!
        }

    /**
     * lines 가 형성하는 다각형.
     */
    val boundingPolygon: Polygon
        get() {
            return Polygon.fromRects(lines.map { it.boundingBox })
        }

    override val representation: String
        get() = lines.joinToString(separator = " ") { it.representation }

    /**
     * SENSE_GROUP 모드 per-word 캐시. 한 번 GPT 가 어떤 단어에 대한 의미군을 돌려주면,
     * 그 결과를 단어의 sentence 내부 char offset 으로 키잉해 저장한다. 같은 OCR 패스에서
     * 사용자가 같은 단어를 다시 가리키면 GPT 를 다시 호출하지 않고 즉시 반환된다.
     *
     * key = [wordCharOffset] 의 결과 (정수). Word 객체 동일성 대신 offset 으로 키잉하는
     * 이유: data class 의 equality 가 representation 만으로 collapse 될 위험을 피하고,
     * 또 같은 단어가 한 문장에 여러 번 등장해도 위치별로 별개 의미군을 캐시하기 위함.
     *
     * ConcurrentHashMap 으로 만들어 IO 스레드의 write 와 메인 스레드의 read 가 안전하게 공존.
     */
    val senseGroupCache: java.util.concurrent.ConcurrentHashMap<Int, SenseGroup> =
        java.util.concurrent.ConcurrentHashMap()

    /**
     * Returns the character offset of [word] inside [representation], or `null`
     * if the word is not part of this sentence. Offsets follow the same join
     * convention used by [representation] / [Line.representation] — a single
     * space between adjacent words (and between adjacent lines).
     */
    fun wordCharOffset(word: Word): Int? {
        var offset = 0
        for ((lineIndex, line) in lines.withIndex()) {
            if (lineIndex > 0) offset += 1 // separator between lines
            for ((wordIndex, w) in line.words.withIndex()) {
                if (wordIndex > 0) offset += 1 // separator between words
                if (w === word) return offset
                offset += w.representation.length
            }
        }
        return null
    }
}