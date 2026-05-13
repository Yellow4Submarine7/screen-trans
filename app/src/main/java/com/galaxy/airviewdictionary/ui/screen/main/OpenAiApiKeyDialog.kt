package com.galaxy.airviewdictionary.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.data.local.secure.ApiKeyInfo

/**
 * Internal-testing dialog that lets the user paste their own OpenAI API key.
 *
 * The key is stored through [ApiKeyInfo.setApiKeyChatgpt] (which writes to the
 * encrypted [com.galaxy.airviewdictionary.data.local.secure.SecureStore]) and
 * the version is bumped to 1 so [ApiKeyInfo.chatgptKeyAvailable] returns true.
 *
 * The field intentionally renders as plain text rather than a password — the
 * tester wants to verify they pasted correctly. This UI is expected to be
 * removed before public release in favor of the backend-distributed key flow.
 */
@Composable
fun OpenAiApiKeyDialog(
    onDismissRequest: () -> Unit,
    onSaved: () -> Unit,
    onCleared: () -> Unit,
) {
    val context = LocalContext.current
    val initialKey = remember { ApiKeyInfo.getApiKeyChatgpt(context).orEmpty() }
    val initialBaseUrl = remember { ApiKeyInfo.getApiBaseUrlChatgpt(context).orEmpty() }
    val initialModel = remember { ApiKeyInfo.getApiModelChatgpt(context).orEmpty() }
    var keyText by remember { mutableStateOf(initialKey) }
    var baseUrlText by remember { mutableStateOf(initialBaseUrl) }
    var modelText by remember { mutableStateOf(initialModel) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.settings_menu_openai_api_key))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(text = stringResource(id = R.string.settings_menu_openai_api_key_hint))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                    ),
                    visualTransformation = VisualTransformation.None,
                )
                Text(
                    text = stringResource(id = R.string.settings_menu_openai_api_key_helper),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = baseUrlText,
                    onValueChange = { baseUrlText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(text = stringResource(id = R.string.settings_menu_openai_base_url))
                    },
                    placeholder = {
                        Text(text = stringResource(id = R.string.settings_menu_openai_base_url_hint))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                    ),
                    visualTransformation = VisualTransformation.None,
                )
                Text(
                    text = stringResource(id = R.string.settings_menu_openai_base_url_helper),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = modelText,
                    onValueChange = { modelText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text(text = stringResource(id = R.string.settings_menu_openai_model))
                    },
                    placeholder = {
                        Text(text = stringResource(id = R.string.settings_menu_openai_model_hint))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                    ),
                    visualTransformation = VisualTransformation.None,
                )
                Text(
                    text = stringResource(id = R.string.settings_menu_openai_model_helper),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.settings_menu_openai_api_key_note),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimmedKey = keyText.trim()
                val trimmedBaseUrl = baseUrlText.trim()
                val trimmedModel = modelText.trim()
                // Base URL / Model 은 키 유무와 독립적으로 항상 저장 (빈 문자열이면 ChatGPTKit 가 기본값으로 폴백).
                ApiKeyInfo.setApiBaseUrlChatgpt(context, trimmedBaseUrl)
                ApiKeyInfo.setApiModelChatgpt(context, trimmedModel)
                if (trimmedKey.isEmpty()) {
                    // Treat empty Save as Clear so the user can wipe the key.
                    ApiKeyInfo.setApiKeyChatgpt(context, "")
                    onCleared()
                } else {
                    ApiKeyInfo.setApiKeyChatgpt(context, trimmedKey)
                    ApiKeyInfo.setApiKeyVersionChatgpt(context, 1)
                    onSaved()
                }
            }) {
                Text(text = stringResource(id = R.string.settings_menu_openai_api_key_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.settings_menu_openai_api_key_cancel))
            }
        },
    )
}
