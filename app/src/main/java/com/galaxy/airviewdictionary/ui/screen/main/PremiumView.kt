package com.galaxy.airviewdictionary.ui.screen.main

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfo
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfoHolder
import com.galaxy.airviewdictionary.data.local.vision.TextDetectMode
import com.galaxy.airviewdictionary.data.remote.translation.TranslationKitType
import com.galaxy.airviewdictionary.extensions.openSubscriptionPage
import com.galaxy.airviewdictionary.ui.common.MP4Player
import kotlinx.coroutines.delay


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PremiumView(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val screenInfo: ScreenInfo = ScreenInfoHolder.get()

    val isDarkMode = isSystemInDarkTheme()
    val backgroundColor = if (isDarkMode) Color(0xFF010102) else Color(0xFFf2f1f4)
    val contentTitleColor = if (isDarkMode) Color.White else Color.Black
    val contentColor = if (isDarkMode) Color(0xFFfcfcfc) else Color(0xFF010000)

    val videoBoxDimen = if (isPortrait) 200.dp else 320.dp
    val videoDimen = if (isPortrait) 140.dp else 280.dp
    val mp4PlayerVisible = remember { mutableStateOf(false) }

    val purchaseState: Int by viewModel.billingRepository.purchaseStateFlow.collectAsStateWithLifecycle(
        lifecycle = lifecycleOwner.lifecycle,
        initialValue = Purchase.PurchaseState.UNSPECIFIED_STATE
    )

    val productDetails: ProductDetails? by viewModel.billingRepository.productDetailsFlow.collectAsStateWithLifecycle(
        lifecycle = lifecycleOwner.lifecycle,
        initialValue = null
    )

    val getPremiumTextResource = remember { mutableIntStateOf(R.string.premium_view_get_premium) }
    val price = remember { mutableStateOf("$---") }

    LaunchedEffect(purchaseState, productDetails) {
        if (purchaseState == Purchase.PurchaseState.PURCHASED) {
            getPremiumTextResource.intValue = R.string.premium_view_get_premium_purchased
        } else if (purchaseState == Purchase.PurchaseState.PENDING) {
            getPremiumTextResource.intValue = R.string.premium_view_get_premium_pending
        } else {
            if (productDetails == null) {
                getPremiumTextResource.intValue = R.string.premium_view_get_premium_not_available
                price.value = "$---"
            } else {
                getPremiumTextResource.intValue = R.string.premium_view_get_premium
                price.value = productDetails?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
                            ?: "Unknown price"
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(1000) // 1초 대기
        mp4PlayerVisible.value = true
    }

    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(remember { ScrollState(0) })
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Blue.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        start = Offset(screenInfo.width.toFloat(), 0f),
                        end = Offset(0f, screenInfo.height / 4f)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Red.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(screenInfo.width.toFloat(), screenInfo.height / 4f)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                ) {
                    Spacer(modifier = Modifier.height(if (isPortrait) 72.dp else 64.dp))

                    Row(
                        modifier = Modifier
                            .wrapContentHeight()
                            .align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.image_premium),
                            contentDescription = "image_premium",
                            modifier = Modifier
                                .size(32.dp)
                                .offset(y = (-3).dp)
                                .align(Alignment.CenterVertically)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        with(sharedTransitionScope) {
                            Text(
                                text = stringResource(id = getPremiumTextResource.value),
                                color = contentColor,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), //
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .sharedBounds(
                                        rememberSharedContentState(key = "menu_premium"),
                                        animatedVisibilityScope = animatedVisibilityScope
                                    ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(35.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.premium_view_title),
                        color = contentTitleColor,
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.premium_view_message),
                        color = contentTitleColor,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (purchaseState != Purchase.PurchaseState.PURCHASED && purchaseState != Purchase.PurchaseState.PENDING) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.premium_view_price_format, price.value),
                            color = contentTitleColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (productDetails != null) {
                                    viewModel.launchBillingFlow(context as Activity)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .align(Alignment.CenterHorizontally)
                                .semantics {
                                    contentDescription = context.getString(getPremiumTextResource.value)
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0Xff115ef7),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(text = stringResource(id = getPremiumTextResource.value))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.Center,
//                            text = "Supports  ${viewModel.translationRepository.getSupportedLanguages(TranslationKitType.AZURE).size}  languages.\nAccurately translates most languages around the world.",
                            text = stringResource(id = R.string.premium_view_description_azure),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight(),
//                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = TranslationKitType.AZURE.brandFatResourceId),
                                contentDescription = "AZURE brand image",
                                modifier = Modifier
                                    .sizeIn(maxHeight = 72.dp)
                                    .align(Alignment.Center),
                                contentScale = ContentScale.Fit
                            )
//                            Image(
//                                painter = painterResource(id = R.drawable.image_premium),
//                                contentDescription = "image_premium",
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .offset(y = 24.dp)
//                                    .align(Alignment.TopEnd)
//                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.Center,
//                            text = "Supports  ${viewModel.translationRepository.getSupportedLanguages(TranslationKitType.DEEPL).size}  languages.\nSophisticated translation that captures the meaning well.",
                            text = stringResource(id = R.string.premium_view_description_deepl),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight(),
                        ) {
                            Image(
                                painter = painterResource(id = TranslationKitType.DEEPL.brandFatResourceId),
                                contentDescription = "DEEPL brand image",
                                modifier = Modifier
                                    .sizeIn(maxHeight = 72.dp)
                                    .align(Alignment.Center),
                                contentScale = ContentScale.Fit
                            )
//                            Image(
//                                painter = painterResource(id = R.drawable.image_premium),
//                                contentDescription = "image_premium",
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .offset(y = 18.dp)
//                                    .align(Alignment.TopEnd)
//                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.premium_view_description_papago),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp, lineHeight = 15.sp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight(),
                        ) {
                            Image(
                                painter = painterResource(id = TranslationKitType.PAPAGO.brandFatResourceId),
                                contentDescription = "PAPAGO brand image",
                                modifier = Modifier
                                    .sizeIn(maxHeight = 72.dp)
                                    .align(Alignment.Center),
                                contentScale = ContentScale.Fit
                            )
//                            Image(
//                                painter = painterResource(id = R.drawable.image_premium),
//                                contentDescription = "image_premium",
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .offset(y = 32.dp)
//                                    .align(Alignment.TopEnd)
//                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
//                            text = "Supports  ${viewModel.translationRepository.getSupportedLanguages(TranslationKitType.GOOGLE).size}  languages.\nIt helps with the fastest translation.",
                            text = stringResource(id = R.string.premium_view_description_google),
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = TranslationKitType.GOOGLE.brandFatResourceId),
                                contentDescription = "GOOGLE brand image",
                                modifier = Modifier
                                    .sizeIn(maxHeight = 72.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(videoBoxDimen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && mp4PlayerVisible.value) {
                                MP4Player(
                                    resourceId = TextDetectMode.SENTENCE.videoResourceId,
                                    modifier = Modifier.size(videoDimen)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = stringResource(id = TextDetectMode.SENTENCE.descriptionResourceId),
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(videoBoxDimen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && mp4PlayerVisible.value) {
                                MP4Player(
                                    resourceId = TextDetectMode.SENSE_GROUP.videoResourceId,
                                    modifier = Modifier.size(videoDimen)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = stringResource(id = TextDetectMode.SENSE_GROUP.descriptionResourceId),
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(videoBoxDimen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && mp4PlayerVisible.value) {
                                MP4Player(
                                    resourceId = TextDetectMode.PARAGRAPH.videoResourceId,
                                    modifier = Modifier.size(videoDimen)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = stringResource(id = TextDetectMode.PARAGRAPH.descriptionResourceId),
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(videoBoxDimen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && mp4PlayerVisible.value) {
                                MP4Player(
                                    resourceId = TextDetectMode.SELECT.videoResourceId,
                                    modifier = Modifier.size(videoDimen)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = stringResource(id = TextDetectMode.SELECT.descriptionResourceId),
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(videoBoxDimen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && mp4PlayerVisible.value) {
                                MP4Player(
                                    resourceId = TextDetectMode.WORD.videoResourceId,
                                    modifier = Modifier.size(videoDimen)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = stringResource(id = TextDetectMode.WORD.descriptionResourceId),
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(videoBoxDimen),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.0f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && mp4PlayerVisible.value) {
                                MP4Player(
                                    resourceId = TextDetectMode.FIXED_AREA.videoResourceId,
                                    modifier = Modifier.size(videoDimen)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = stringResource(id = TextDetectMode.FIXED_AREA.descriptionResourceId),
                            textAlign = TextAlign.Center,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp)
                        )
                    }

                    if (purchaseState != Purchase.PurchaseState.PURCHASED && purchaseState != Purchase.PurchaseState.PENDING) {
                        Spacer(modifier = Modifier.height(42.dp))

                        Row(
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.image_premium),
                                contentDescription = "image_premium",
                                modifier = Modifier
                                    .size(42.dp)
                                    .offset(y = (-5).dp)
                                    .align(Alignment.CenterVertically)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = stringResource(id = getPremiumTextResource.value),
                                color = contentColor,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), //
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.premium_view_price_format, price.value),
                            color = contentTitleColor,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (productDetails != null) {
                                    viewModel.launchBillingFlow(context as Activity)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .align(Alignment.CenterHorizontally)
                                .semantics {
                                    contentDescription = context.getString(getPremiumTextResource.value)
                                },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0Xff115ef7),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(text = stringResource(id = getPremiumTextResource.value))
                        }
                    }

                    Button(
                        onClick = {
                            context.openSubscriptionPage()
                            viewModel.billingRepository.restartPurchaseQueryLoop()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .align(Alignment.CenterHorizontally)
                            .semantics {
                                contentDescription = "Unsubscribe"
                            },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(text = stringResource(id = R.string.premium_view_unsubscribe))
                    }

                    Spacer(modifier = Modifier.height(54.dp))
                }
            }
        }
    }

}





