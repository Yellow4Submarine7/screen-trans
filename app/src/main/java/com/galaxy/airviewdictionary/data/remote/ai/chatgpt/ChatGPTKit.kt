package com.galaxy.airviewdictionary.data.remote.ai.chatgpt

import android.content.Context
import com.galaxy.airviewdictionary.data.local.secure.ApiKeyInfo
import com.galaxy.airviewdictionary.data.remote.ai.CorrectionKit
import com.galaxy.airviewdictionary.di.ChatGPTRetrofit
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatGPTKit @Inject constructor(@ApplicationContext val context: Context, @ChatGPTRetrofit private val chatGPTService: ChatGPTService) : CorrectionKit() {

    override fun available(): Boolean {
        return ApiKeyInfo.chatgptKeyAvailable(context)
    }

    /**
     * 사용자가 직접 Base URL 을 설정한 경우 그 값을, 아니면 공식 OpenAI 엔드포인트를 사용한다.
     * 사용자 입력은 trimEnd('/') 로 정규화하여 v1/chat/completions 가 항상 한 번만 붙도록 만든다.
     */
    private fun resolveEndpointUrl(): String {
        val custom = ApiKeyInfo.getApiBaseUrlChatgpt(context)?.trim().orEmpty()
        val base = if (custom.isNotEmpty()) custom else BASE_URL
        return base.trimEnd('/') + "/v1/chat/completions"
    }

    /**
     * 사용자가 직접 모델명을 설정한 경우 그 값을, 아니면 [DEFAULT_MODEL] 을 사용한다.
     * 제 3 자 OpenAI-compatible 프록시는 모델 라우팅이 제각각이므로 (예: gpt-4o-mini 라고
     * /v1/models 에 노출만 해두고 실제로는 routing 이 없음 → "unknown provider"), 사용자가
     * 자기 프록시에 맞는 모델명을 자유롭게 지정할 수 있어야 한다.
     */
    private fun resolveModel(): String {
        val custom = ApiKeyInfo.getApiModelChatgpt(context)?.trim().orEmpty()
        return if (custom.isNotEmpty()) custom else DEFAULT_MODEL
    }

    /**
     * Retrofit/OkHttp 실패를 사람이 읽을 수 있게 풀어 로그에 남긴다. HttpException 의 경우
     * 응답 바디까지 같이 찍어줘야 "왜 4xx/5xx 인지" — 특히 프록시가 돌려주는 에러 JSON —
     * 을 ADB 로 바로 확인할 수 있다.
     */
    private fun logApiFailure(where: String, e: Throwable) {
        if (e is HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (_: Throwable) { null }
            Timber.tag(TAG).w(e, "%s HTTP %d body=%s", where, e.code(), body ?: "<no body>")
        } else {
            Timber.tag(TAG).w(e, "%s failed", where)
        }
    }

    private fun getSystemMessage(languageCode: String): String {
        return when (languageCode) {
            "en" -> "Optimize English text by expanding contractions and fixing slang, informal words, and incomplete sentences."
            "zh" -> "Optimize Chinese text by replacing internet slang and abbreviations with standard expressions and ensuring proper sentence structure."
            "es" -> "Optimize Spanish text by expanding abbreviations, replacing informal phrases, and fixing incomplete sentences."
            "hi" -> "Optimize Hindi text by expanding informal contractions, fixing slang, and ensuring proper sentence structure."
            "ar" -> "Optimize Arabic text by replacing dialect slang with Modern Standard Arabic and fixing incomplete sentences."
            "pt" -> "Optimize Portuguese text by expanding contractions, replacing informal words, and fixing incomplete or fragmented sentences."
            "bn" -> "Optimize Bengali text by expanding contractions, fixing informal expressions, and ensuring clear sentence structure."
            "ru" -> "Optimize Russian text by expanding informal phrases, replacing slang, and fixing incomplete or ambiguous sentences."
            "ja" -> "Optimize Japanese text by replacing casual phrases, internet slang, and ambiguous terms with formal equivalents."
            "ko" -> "Optimize Korean text by expanding contractions, fixing informal phrases, and correcting fragmented or ambiguous sentences."
            "fr" -> "Optimize French text by expanding contractions, replacing slang, and fixing incomplete or casual phrases."
            "de" -> "Optimize German text by expanding abbreviations, fixing informal phrases, and ensuring complete sentence structure."
            "it" -> "Optimize Italian text by expanding contractions, replacing informal phrases, and fixing incomplete or fragmented sentences."
            "tr" -> "Optimize Turkish text by expanding contractions, replacing informal words, and fixing fragmented sentences."
            "vi" -> "Optimize Vietnamese text by replacing internet slang, expanding abbreviations, and ensuring sentence clarity."
            "ta" -> "Optimize Tamil text by expanding contractions, fixing informal words, and correcting sentence structure."
            "ur" -> "Optimize Urdu text by expanding contractions, fixing slang, and ensuring formal sentence structure."
            "fa" -> "Optimize Persian text by replacing slang, expanding informal contractions, and ensuring complete and proper sentences."
            "nl" -> "Optimize Dutch text by expanding abbreviations, replacing slang, and fixing incomplete or fragmented sentences."
            "th" -> "Optimize Thai text by replacing slang, expanding informal phrases, and fixing ambiguous or incomplete sentences."
            else -> "Optimize text by expanding contractions and fixing slang, informal words, and incomplete sentences."
        }
    }

    override suspend fun request(
        sourceLanguageCode: String,
        sourceText: String,
    ): String {
        // 요청 메시지 구성
        val systemMessage = mapOf(
            "role" to "system",
            "content" to getSystemMessage(sourceLanguageCode)
        )

        val userMessage = mapOf(
            "role" to "user",
            "content" to """{"sentence": "$sourceText"}""".trimIndent()
        )

        // JSON 요청 본문 생성
        val requestBody = mapOf(
            "model" to resolveModel(),
            "messages" to listOf(systemMessage, userMessage),
            "max_tokens" to 500
        )
        Timber.tag(TAG).d("requestBody : $requestBody")

        val jsonRequestBody = Gson().toJson(requestBody)
            .toRequestBody("application/json".toMediaType())

        // API 호출
        return try {
            val response: ChatGPTResponse = chatGPTService.send(
                url = resolveEndpointUrl(),
                apiKey = "Bearer ${ApiKeyInfo.getApiKeyChatgpt(context) ?: "unknown_key"}",
                body = jsonRequestBody
            )

            /*
                {
                  "id": "chatcmpl-AzLBT2DeI07xN0MykumYlB63SOitc",
                  "object": "chat.completion",
                  "created": 1739182803,
                  "model": "gpt-3.5-turbo-0125",
                  "choices": [
                    {
                      "index": 0,
                      "message": {
                        "role": "assistant",
                        "content": "Provide haptic feedback for detection.",
                        "refusal": null
                      },
                      "logprobs": null,
                      "finish_reason": "stop"
                    }
                  ],
                  "usage": {
                    "prompt_tokens": 44,
                    "completion_tokens": 8,
                    "total_tokens": 52,
                    "prompt_tokens_details": {
                      "cached_tokens": 0,
                      "audio_tokens": 0
                    },
                    "completion_tokens_details": {
                      "reasoning_tokens": 0,
                      "audio_tokens": 0,
                      "accepted_prediction_tokens": 0,
                      "rejected_prediction_tokens": 0
                    }
                  },
                  "service_tier": "default",
                  "system_fingerprint": null
                }
             */
            val resultText = response.choices[0].message.content
            Timber.tag("TargetHandleViewModel").i("ChatGPTKit resultText : $resultText")
            val splitResult = resultText.split("->")
            return when (splitResult.size) {
                2 -> splitResult[1].trim().removeSurrounding("\"")
                else -> splitResult[0].trim().removeSurrounding("\"")
            }
        } catch (e: Exception) {
            logApiFailure("request()", e)
            sourceText
        }
    }

    /**
     * 사용자가 [sentence] 안에서 [word] 를 가리키고 있을 때, 그 단어가 속한 의미 단위(意群,
     * sense group) 한 개만 식별하고 [targetLanguageCode] 로 번역한다.
     *
     * 이전 버전은 문장 전체를 한 번에 모든 청크로 쪼개고 클라이언트가 char offset 으로
     * 다시 찾아갔지만, 그 방식은 GPT 가 결국 "문장 그 자체"를 1개의 청크로 돌려주기 쉬워
     * 사실상 의미군 분리가 되지 않았다. 이번 설계는 OCR 로 얻은 단어를 직접 함께 넘겨
     * "이 단어를 포함하는 청크"만 콕 집어 달라고 요청한다.
     *
     * 실패(키 없음, 네트워크 오류, 파싱 실패, GPT 가 검증 가능한 형태로 응답 못함)시 null 반환.
     * 호출 측은 null 을 폴백 신호로 받아 SENTENCE 모드와 동일하게 동작해야 한다.
     */
    suspend fun senseGroupAt(
        word: String,
        sentence: String,
        sourceLanguageCode: String,
        targetLanguageCode: String,
    ): SenseGroup? {
        if (word.isBlank() || sentence.isBlank()) return null
        // 클라이언트 측 안전망: 단어가 문장에 들어있지 않으면 시도해도 의미 없음.
        if (!sentence.contains(word)) {
            Timber.tag(TAG).w("senseGroupAt: word [$word] not in sentence [$sentence]")
            return null
        }

        val systemMessage = mapOf(
            "role" to "system",
            "content" to (
                "You are a linguist. The user is reading a sentence and is pointing " +
                "at a specific word inside it. Your job is to identify the SENSE GROUP " +
                "(also known as a semantic chunk, thought unit, or 意群) that contains " +
                "that pointed word, and translate JUST that chunk into the target language. " +
                "\n\n" +
                "A sense group is a phrase-level unit smaller than the full sentence: " +
                "typically a noun phrase, a verb phrase, a prepositional phrase, or a " +
                "subordinate clause. It is the minimal contiguous span of words that " +
                "carries a self-contained meaning and gives the pointed word its " +
                "in-context interpretation." +
                "\n\n" +
                "Rules (all MUST be followed):" +
                "\n - The chunk MUST contain the pointed word." +
                "\n - The chunk MUST appear VERBATIM in the sentence — copy the substring " +
                "exactly, do not paraphrase, do not normalize punctuation, do not change " +
                "case." +
                "\n - Prefer the SMALLEST meaningful chunk. Do NOT return the entire " +
                "sentence unless the sentence is itself a single short phrase." +
                "\n - Translate ONLY the chunk, not the surrounding sentence." +
                "\n\n" +
                "Respond ONLY with valid JSON of the form:" +
                "\n{\"chunk\":\"<verbatim span from the sentence>\",\"translation\":\"<translation in target language>\"}"
            )
        )

        val userPayload = JSONObject().apply {
            put("word", word)
            put("sentence", sentence)
            put("source_language", sourceLanguageCode)
            put("target_language", targetLanguageCode)
        }.toString()

        val userMessage = mapOf(
            "role" to "user",
            "content" to userPayload
        )

        val requestBody = mapOf(
            "model" to resolveModel(),
            "messages" to listOf(systemMessage, userMessage),
            "max_tokens" to 400,
            "response_format" to mapOf("type" to "json_object"),
            "temperature" to 0.0,
        )
        Timber.tag(TAG).d("senseGroupAt() word=[$word] sentence=[$sentence]")

        val jsonRequestBody = Gson().toJson(requestBody)
            .toRequestBody("application/json".toMediaType())

        val response: ChatGPTResponse = try {
            chatGPTService.send(
                url = resolveEndpointUrl(),
                apiKey = "Bearer ${ApiKeyInfo.getApiKeyChatgpt(context) ?: "unknown_key"}",
                body = jsonRequestBody
            )
        } catch (e: Exception) {
            logApiFailure("senseGroupAt()", e)
            return null
        }

        val raw = response.choices.firstOrNull()?.message?.content
        if (raw.isNullOrBlank()) {
            Timber.tag(TAG).w("senseGroupAt() empty response")
            return null
        }

        return try {
            val json = JSONObject(raw)
            val chunkText = json.optString("chunk", "").trim()
            val translation = json.optString("translation", "").trim()
            if (chunkText.isEmpty()) {
                Timber.tag(TAG).w("senseGroupAt() empty chunk in response: $raw")
                return null
            }
            // 청크가 실제 문장에 들어있는지 검증하고, 들어있다면 char offset 산출.
            val start = sentence.indexOf(chunkText)
            if (start < 0) {
                Timber.tag(TAG).w("senseGroupAt() chunk [$chunkText] not found in sentence [$sentence]")
                return null
            }
            // 청크 안에 가리킨 단어가 실제로 들어있어야 한다. 안 들어있으면 GPT 가 다른 의미군을 골랐다는 뜻.
            if (!chunkText.contains(word)) {
                Timber.tag(TAG).w("senseGroupAt() chunk [$chunkText] does not contain word [$word]")
                return null
            }
            val end = start + chunkText.length
            Timber.tag(TAG).d("senseGroupAt() OK word=[$word] chunk=[$chunkText] tr=[$translation] range=$start..$end")
            SenseGroup(chunkText, translation, start..end)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "senseGroupAt() parse failed: $raw")
            null
        }
    }

    companion object {
        const val BASE_URL = "https://api.openai.com/"
        const val DEFAULT_MODEL = "gpt-4o-mini"
    }
}

