package com.galaxy.airviewdictionary.data.remote.ai

import com.galaxy.airviewdictionary.data.AVDRepository
import com.galaxy.airviewdictionary.data.remote.ai.chatgpt.ChatGPTKit
import com.galaxy.airviewdictionary.data.remote.ai.chatgpt.SenseGroup
import javax.inject.Inject
import javax.inject.Singleton


/**
 *
 */
@Singleton
class CorrectionRepository @Inject constructor(
    private val chatGPTKit: ChatGPTKit,
) : AVDRepository() {

    private fun getCorrectionKit(kitType: CorrectionKitType): CorrectionKit {
        return when (kitType) {
            CorrectionKitType.CHAT_GPT -> chatGPTKit
        }
    }

    suspend fun request(
        sourceLanguageCode: String,
        sourceText: String,
        correctionKitType: CorrectionKitType,
    ): String {
        val correctionKit: CorrectionKit = getCorrectionKit(correctionKitType)

        return correctionKit.request(
            sourceLanguageCode = sourceLanguageCode,
            sourceText = sourceText,
        )
    }

    /**
     * 사용자가 [sentence] 안에서 [word] 를 가리키고 있을 때, 그 단어를 포함하는 의미군(意群,
     * sense group)을 식별해 [targetLanguageCode] 로 번역해 반환한다. 실패 시 null —
     * 호출 측은 SENTENCE 모드로 폴백해야 한다. 자세한 계약은 [ChatGPTKit.senseGroupAt] 참고.
     */
    suspend fun senseGroupAt(
        word: String,
        sentence: String,
        sourceLanguageCode: String,
        targetLanguageCode: String,
    ): SenseGroup? {
        return chatGPTKit.senseGroupAt(
            word = word,
            sentence = sentence,
            sourceLanguageCode = sourceLanguageCode,
            targetLanguageCode = targetLanguageCode,
        )
    }

    override fun onZeroReferences() {

    }
}
