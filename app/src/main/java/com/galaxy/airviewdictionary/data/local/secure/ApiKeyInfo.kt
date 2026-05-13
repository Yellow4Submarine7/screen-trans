package com.galaxy.airviewdictionary.data.local.secure

import android.content.Context


/**
 * api key 정보.
 */
object ApiKeyInfo {

    fun getApiKeyAzure(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_AZURE)?.get()
    }

    fun setApiKeyAzure(context: Context, apiKeyAzure: String) {
        SecureStore.set(context, SecureStoreKey.API_KEY_AZURE, apiKeyAzure)
    }

    fun getApiKeyVersionAzure(context: Context): Int? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_VERSION_AZURE)?.get()?.toInt()
    }

    fun setApiKeyVersionAzure(context: Context, apiKeyVersionAzure: Int) {
        SecureStore.set(context, SecureStoreKey.API_KEY_VERSION_AZURE, apiKeyVersionAzure.toString())
    }

    fun getApiKeyDeepl(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_DEEPL)?.get()
    }

    fun setApiKeyDeepl(context: Context, apiKeyDeepl: String) {
        SecureStore.set(context, SecureStoreKey.API_KEY_DEEPL, apiKeyDeepl)
    }

    fun getApiKeyVersionDeepl(context: Context): Int? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_VERSION_DEEPL)?.get()?.toInt()
    }

    fun setApiKeyVersionDeepl(context: Context, apiKeyVersionDeepl: Int) {
        SecureStore.set(context, SecureStoreKey.API_KEY_VERSION_DEEPL, apiKeyVersionDeepl.toString())
    }

    fun getApiKeyPapago(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_PAPAGO)?.get()
    }

    fun setApiKeyPapago(context: Context, apiKeyPapago: String) {
        SecureStore.set(context, SecureStoreKey.API_KEY_PAPAGO, apiKeyPapago)
    }

    fun getApiKeyVersionPapago(context: Context): Int? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_VERSION_PAPAGO)?.get()?.toInt()
    }

    fun setApiKeyVersionPapago(context: Context, apiKeyVersionPapago: Int) {
        SecureStore.set(context, SecureStoreKey.API_KEY_VERSION_PAPAGO, apiKeyVersionPapago.toString())
    }

    fun getApiKeyYandex(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_YANDEX)?.get()
    }

    fun setApiKeyYandex(context: Context, apiKeyYandex: String) {
        SecureStore.set(context, SecureStoreKey.API_KEY_YANDEX, apiKeyYandex)
    }

    fun getApiKeyVersionYandex(context: Context): Int? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_VERSION_YANDEX)?.get()?.toInt()
    }

    fun setApiKeyVersionYandex(context: Context, apiKeyVersionYandex: Int) {
        SecureStore.set(context, SecureStoreKey.API_KEY_VERSION_YANDEX, apiKeyVersionYandex.toString())
    }

    fun getApiKeyChatgpt(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_CHATGPT)?.get()
    }

    fun setApiKeyChatgpt(context: Context, apiKeyChatgpt: String) {
        SecureStore.set(context, SecureStoreKey.API_KEY_CHATGPT, apiKeyChatgpt)
    }

    fun getApiKeyVersionChatgpt(context: Context): Int? {
        return SecureStore.get(context, SecureStoreKey.API_KEY_VERSION_CHATGPT)?.get()?.toInt()
    }

    fun setApiKeyVersionChatgpt(context: Context, apiKeyVersionChatgpt: Int) {
        SecureStore.set(context, SecureStoreKey.API_KEY_VERSION_CHATGPT, apiKeyVersionChatgpt.toString())
    }

    fun getApiBaseUrlChatgpt(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_BASE_URL_CHATGPT)?.get()
    }

    fun setApiBaseUrlChatgpt(context: Context, baseUrl: String) {
        SecureStore.set(context, SecureStoreKey.API_BASE_URL_CHATGPT, baseUrl)
    }

    fun getApiModelChatgpt(context: Context): String? {
        return SecureStore.get(context, SecureStoreKey.API_MODEL_CHATGPT)?.get()
    }

    fun setApiModelChatgpt(context: Context, model: String) {
        SecureStore.set(context, SecureStoreKey.API_MODEL_CHATGPT, model)
    }


    fun apiKeyAvailable(context: Context): Boolean {
        return getApiKeyAzure(context) != null && getApiKeyAzure(context)!!.isNotEmpty()
                && getApiKeyVersionAzure(context) != null
                && getApiKeyDeepl(context) != null && getApiKeyDeepl(context)!!.isNotEmpty()
                && getApiKeyVersionDeepl(context) != null
                && getApiKeyPapago(context) != null && getApiKeyPapago(context)!!.isNotEmpty()
                && getApiKeyVersionPapago(context) != null
                && getApiKeyYandex(context) != null && getApiKeyYandex(context)!!.isNotEmpty()
                && getApiKeyVersionYandex(context) != null
                && getApiKeyChatgpt(context) != null && getApiKeyChatgpt(context)!!.isNotEmpty()
                && getApiKeyVersionChatgpt(context) != null
    }

    fun chatgptKeyAvailable(context: Context): Boolean {
        return getApiKeyChatgpt(context).let { !it.isNullOrEmpty() }
    }

}
