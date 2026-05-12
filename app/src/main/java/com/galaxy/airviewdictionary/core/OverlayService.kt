package com.galaxy.airviewdictionary.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.galaxy.airviewdictionary.ACTION_SERVICE_CONTROL
import com.galaxy.airviewdictionary.EXTRA_SERVICE_STOP
import com.galaxy.airviewdictionary.FOREGROUND_SERVICE_NOTIFICATION_ID
import com.galaxy.airviewdictionary.NOTIFICATION_CHANNEL_ID
import com.galaxy.airviewdictionary.NOTIFICATION_CHANNEL_NAME
import com.galaxy.airviewdictionary.R
import com.galaxy.airviewdictionary.REQUEST_CODE_SERVICE_STOP
import com.galaxy.airviewdictionary.data.local.capture.CaptureRepository
import com.galaxy.airviewdictionary.data.local.preference.PreferenceRepository
import com.galaxy.airviewdictionary.data.local.screen.ScreenInfoHolder
import com.galaxy.airviewdictionary.data.local.secure.SecureRepository
import com.galaxy.airviewdictionary.data.local.tts.TTSRepository
import com.galaxy.airviewdictionary.data.local.vision.VisionRepository
import com.galaxy.airviewdictionary.data.remote.ai.CorrectionRepository
import com.galaxy.airviewdictionary.data.remote.billing.BillingRepository
import com.galaxy.airviewdictionary.data.remote.firebase.AnalyticsRepository
import com.galaxy.airviewdictionary.data.remote.firebase.RemoteConfigRepository
import com.galaxy.airviewdictionary.data.remote.translation.TranslationRepository
import com.galaxy.airviewdictionary.ui.screen.intro.SplashActivity
import com.galaxy.airviewdictionary.ui.screen.main.SettingsActivity
import com.galaxy.airviewdictionary.ui.screen.overlay.Event
import com.galaxy.airviewdictionary.ui.screen.overlay.languagelist.LanguageListViewModel
import com.galaxy.airviewdictionary.ui.screen.overlay.languagelist.LanguageListViewModelFactory
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuBarViewModel
import com.galaxy.airviewdictionary.ui.screen.overlay.menubar.MenuBarViewModelFactory
import com.galaxy.airviewdictionary.ui.screen.overlay.settings.SliderDialogViewModel
import com.galaxy.airviewdictionary.ui.screen.overlay.settings.SliderDialogViewModelFactory
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleView
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleViewModel
import com.galaxy.airviewdictionary.ui.screen.overlay.targethandle.TargetHandleViewModelFactory
import com.galaxy.airviewdictionary.ui.screen.overlay.voicelist.VoiceListViewModel
import com.galaxy.airviewdictionary.ui.screen.overlay.voicelist.VoiceListViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * 오버레이 서비스
 * 스크린 번역 핸들 뷰 [TargetHandleView] 등 오버레이 뷰 [com.galaxy.airviewdictionary.ui.screen.overlay.OverlayView] 들의 생명 주기를 관장 하는 서비스.
 * 오버레이 뷰 에서 필요로 하는 ViewModel 의 공급자 역할도 한다.
 */
@AndroidEntryPoint
class OverlayService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private val TAG = javaClass.simpleName

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    override val viewModelStore: ViewModelStore = ViewModelStore()

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).d("#################### OverlayService onCreate ####################")

        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Timber.tag(TAG).i("#### onStartCommand() ####")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    FOREGROUND_SERVICE_NOTIFICATION_ID,
                    buildNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
                )
            } else {
                startForeground(
                    FOREGROUND_SERVICE_NOTIFICATION_ID,
                    buildNotification(),
                )
            }
        } catch (e: SecurityException) {
            val intent = Intent(applicationContext, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        intent?.let {
            when (intent.getStringExtra(ACTION_SERVICE_CONTROL)) {
                EXTRA_SERVICE_STOP -> {
                    Timber.tag(TAG).d("EXTRA_SERVICE_STOP")
                    broadcastEvent(Event.Unbind)
                    viewModelStore.clear()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // API 24 이상
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true) // Deprecated 되었지만 API 23 지원 위해 사용
                    }
                    stopSelf()
                }

                else -> {}
            }
        }

        /**
         * 메모리 공간 부족 으로 서비스 가 종료 되었을 때, 다음 세가지 플래그 에 따라 서비스 는 재 실행 또는 생성을 결정 한다.
         * START_STICKY : 재생성 과 onStartCommand() 호출(with null intent)
         * START_NOT_STICKY : 서비스 재실행 하지 않음
         * START_REDELIVER_INTENT : 재생성 과 onStartCommand() 호출(with same intent)
         */
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        Timber.tag(TAG).i("#### onBind() ####")
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.tag(TAG).i("#### onUnbind() ####")
        broadcastEvent(Event.Unbind)
        return true // allowRebind
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Timber.tag(TAG).i("#### onConfigurationChanged() ####")
        ScreenInfoHolder.updateScreenInfoInService(this)
        broadcastEvent(Event.ConfigurationChanged)
    }

    override fun onDestroy() {
        Timber.tag(TAG).i("#### onDestroy() ####")
        viewModelStore.clear()
        super.onDestroy()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                   Event, Event Listeners                                   //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private var overlayServiceEventListeners = mutableListOf<OverlayServiceEventListener>()

    fun registerListener(listener: OverlayServiceEventListener) {
        unregisterListener(listener)
        overlayServiceEventListeners.add(listener)
    }

    fun broadcastEvent(event: Event) {
        val listenersSnapshot = ArrayList(overlayServiceEventListeners)
        listenersSnapshot.forEach { listener ->
            listener.onOverlayServiceEvent(this@OverlayService, event)
        }
    }

    fun unregisterListener(listener: OverlayServiceEventListener) {
        val tempList = ArrayList(overlayServiceEventListeners)
        tempList.remove(listener)
        overlayServiceEventListeners = tempList
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                          Repository                                        //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    lateinit var secureRepository: SecureRepository

    @Inject
    lateinit var remoteConfigRepository: RemoteConfigRepository

//    @Inject
//    lateinit var geoLocaleRepository: GeoLocaleRepository

    @Inject
    lateinit var billingRepository: BillingRepository

    @Inject
    lateinit var preferenceRepository: PreferenceRepository

    @Inject
    lateinit var captureRepository: CaptureRepository

    @Inject
    lateinit var visionRepository: VisionRepository

    @Inject
    lateinit var correctionRepository: CorrectionRepository

    @Inject
    lateinit var translationRepository: TranslationRepository

    @Inject
    lateinit var ttsRepository: TTSRepository

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                               TargetHandleViewModel Provider                               //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun getTargetHandleViewModel(): TargetHandleViewModel {
        val viewModelFactory = TargetHandleViewModelFactory(
            applicationContext = applicationContext,
            secureRepository = secureRepository,
            remoteConfigRepository = remoteConfigRepository,
            billingRepository = billingRepository,
            preferenceRepository = preferenceRepository,
            captureRepository = captureRepository,
            visionRepository = visionRepository.apply { addObserver(lifecycle) },
            correctionRepository = correctionRepository,
            translationRepository = translationRepository,
            ttsRepository = ttsRepository,
            analyticsRepository = analyticsRepository,
        )
        return ViewModelProvider(this, viewModelFactory)[TargetHandleViewModel::class.java]
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                  MenuBarViewModel Provider                                 //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun getMenuBarViewModel(): MenuBarViewModel {
        val viewModelFactory = MenuBarViewModelFactory(
            applicationContext = applicationContext,
            billingRepository = billingRepository,
            preferenceRepository = preferenceRepository,
            translationRepository = translationRepository,
        )
        return ViewModelProvider(this, viewModelFactory)[MenuBarViewModel::class.java]
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                LanguageListViewModel Provider                              //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun getLanguageListViewModel(): LanguageListViewModel {
        val viewModelFactory = LanguageListViewModelFactory(
            applicationContext = applicationContext,
            preferenceRepository = preferenceRepository,
            translationRepository = translationRepository,
        )
        return ViewModelProvider(this, viewModelFactory)[LanguageListViewModel::class.java]
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                SliderDialogViewModel Provider                              //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun getSliderDialogViewModel(): SliderDialogViewModel {
        val viewModelFactory = SliderDialogViewModelFactory(
            preferenceRepository = preferenceRepository,
            translationRepository = translationRepository,
            ttsRepository = ttsRepository,
        )
        return ViewModelProvider(this, viewModelFactory)[SliderDialogViewModel::class.java]
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                  VoiceListViewModel Provider                               //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun getVoiceListViewModel(): VoiceListViewModel {
        val viewModelFactory = VoiceListViewModelFactory(
            applicationContext = applicationContext,
            preferenceRepository = preferenceRepository,
            translationRepository = translationRepository,
            ttsRepository = ttsRepository,
        )
        return ViewModelProvider(this, viewModelFactory)[VoiceListViewModel::class.java]
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                         Notification                                       //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun buildNotification(): Notification {
        // NotificationChannel 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel( // NotificationChannel 을 시스템에 등록
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID, // NotificationChannel 의 고유 식별자
                        NOTIFICATION_CHANNEL_NAME, //  NotificationChannel 의 이름
                        NotificationManager.IMPORTANCE_DEFAULT // Notification 의 중요도 설정
                    )
                )
            }
        }

        // SettingsActivity로 이동하기 위한 PendingIntent 생성
        val settingsPendingIntent: PendingIntent =
            Intent(this, SettingsActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }

        // OverlayService를 실행하여 앱을 종료하는 PendingIntent 생성
        val exitPendingIntent = PendingIntent.getService(
            applicationContext,
            REQUEST_CODE_SERVICE_STOP,
            Intent(applicationContext, OverlayService::class.java).apply {
                putExtra(ACTION_SERVICE_CONTROL, EXTRA_SERVICE_STOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification 생성
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID) // NotificationChannel을 사용하여 Notification 생성
//            .setContentTitle(application.resources.getString(R.string.app_name)) // Notification의 제목 설정
            .setContentText(application.resources.getString(R.string.notification_foreground_service))
            .setSmallIcon(R.drawable.outline_translate_white_24) // Notification의 아이콘 설정
            .setContentIntent(settingsPendingIntent) // Notification을 탭할 때 실행할 PendingIntent 설정
            .addAction(
                0,
                application.resources.getString(R.string.service_menu_finish), // 'Exit' 액션 버튼
                exitPendingIntent // 'Exit' 액션을 수행할 PendingIntent 설정
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // ForegroundService 동작 설정
            .build()
    }
}


