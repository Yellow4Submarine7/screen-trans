package com.galaxy.airviewdictionary.data.local.vision

import com.galaxy.airviewdictionary.R

enum class TextDetectMode(
    val text: String,
    val iconResourceId: Int,
    val descriptionResourceId: Int,
    val videoResourceId: Int,
) {
    WORD(
        text = "Word detection",
        iconResourceId = R.drawable.ic_detect_mode_word,
        descriptionResourceId = R.string.premium_view_description_word_detection,
        videoResourceId = R.raw.word_resized_rounded,
    ),
    SENTENCE(
        text = "Sentence detection",
        iconResourceId = R.drawable.ic_detect_mode_sentence,
        descriptionResourceId = R.string.premium_view_description_sentence_detection,
        videoResourceId = R.raw.sentence_resized_rounded,
    ),
    SENSE_GROUP(
        text = "Sense group detection",
        iconResourceId = R.drawable.ic_detect_mode_sense_group,
        descriptionResourceId = R.string.premium_view_description_sense_group_detection,
        videoResourceId = R.raw.sentence_resized_rounded, // placeholder — reuse sentence video for now
    ),
    PARAGRAPH(
        text = "Paragraph detection",
        iconResourceId = R.drawable.ic_detect_mode_paragraph,
        descriptionResourceId = R.string.premium_view_description_paragraph_detection,
        videoResourceId = R.raw.paragraph_resized_rounded,
    ),
    SELECT(
        text = "Area selection",
        iconResourceId = R.drawable.ic_detect_mode_select,
        descriptionResourceId = R.string.premium_view_description_select_detection,
        videoResourceId = R.raw.select_resized_rounded,
    ),
    FIXED_AREA(
        text = "Fixed-Area translation",
        iconResourceId = R.drawable.ic_detect_mode_fixedarea,
        descriptionResourceId = R.string.premium_view_description_fixed_area_detection,
        videoResourceId = R.raw.fixed_area_resized,
    ),
}
