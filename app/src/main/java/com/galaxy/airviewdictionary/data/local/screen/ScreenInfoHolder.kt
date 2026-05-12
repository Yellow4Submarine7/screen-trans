package com.galaxy.airviewdictionary.data.local.screen

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Size
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import timber.log.Timber

object ScreenInfoHolder {
    @Volatile
    private var _screenInfo: ScreenInfo = ScreenInfo(
        width = 0,
        height = 0,
        statusBarHeight = 0,
        navBarHeight = 0,
        orientation = 0,
        safePaddingLeft = 0,
        safePaddingTop = 0,
        safePaddingRight = 0,
        safePaddingBottom = 0
    )

    fun set(info: ScreenInfo) { _screenInfo = info }
    fun get(): ScreenInfo = _screenInfo

    fun collectAndStoreScreenInfo(activity: Activity) {
        // 1. 전체 윈도우 크기
        val metrics: Size = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            Size(bounds.width(), bounds.height())
        } else {
            val displayMetrics = activity.resources.displayMetrics
            Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

        // 2. 화면 방향
        val orientation = activity.resources.configuration.orientation

        // width/height/orientation 은 즉시 동기 적용. 인셋(아래)은 decorView 가 attach 된 뒤에야
        // 읽을 수 있어 비동기 post 가 필요하지만, 그 사이에 다른 코드가 (0,0) 을 보지 않게 막아 둔다.
        val prevInfo = get()
        set(
            ScreenInfo(
                width = metrics.width,
                height = metrics.height,
                statusBarHeight = prevInfo.statusBarHeight,
                navBarHeight = prevInfo.navBarHeight,
                orientation = orientation,
                safePaddingLeft = prevInfo.safePaddingLeft,
                safePaddingTop = prevInfo.safePaddingTop,
                safePaddingRight = prevInfo.safePaddingRight,
                safePaddingBottom = prevInfo.safePaddingBottom,
            )
        )

        // 3. WindowInsets로 상태바, 네비바, 세이프패딩 구하기
        activity.window.decorView.post {
            val insets = activity.window.decorView.rootWindowInsets

            val statusBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets?.getInsets(WindowInsets.Type.statusBars())?.top ?: 0
            } else {
                @Suppress("DEPRECATION")
                insets?.systemWindowInsetTop ?: 0
            }

            val navBarHeight = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
            } else {
                @Suppress("DEPRECATION")
                insets?.systemWindowInsetBottom ?: 0
            }

            val left = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets?.getInsets(WindowInsets.Type.systemBars())?.left ?: 0
            } else {
                0
            }

            val right = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets?.getInsets(WindowInsets.Type.systemBars())?.right ?: 0
            } else {
                0
            }

            val current = get()
            val screenInfo = ScreenInfo(
                width = current.width,
                height = current.height,
                statusBarHeight = statusBarHeight,
                navBarHeight = navBarHeight,
                orientation = current.orientation,
                safePaddingLeft = left,
                safePaddingTop = statusBarHeight,
                safePaddingRight = right,
                safePaddingBottom = navBarHeight
            )
            Timber.tag("ScreenInfoHolder").d("ScreenInfo $screenInfo")

            set(screenInfo)
        }
    }

    /**
     * Service 에서 Configuration 변경 시 호출.
     * 폴더블 폰의 inner ↔ outer 전환에서는 단순한 회전이 아니라 디스플레이 자체가 바뀌므로
     * 캐시된 width/height 를 그대로 두거나 swap 만 하면 안 되고, 현재 디스플레이의 실제 크기를
     * 다시 읽어 와야 한다.
     */
    fun updateScreenInfoInService(context: Context) {
        val orientation = context.resources.configuration.orientation
        val (width, height) = readCurrentSize(context)

        val prevInfo = get()
        val newInfo = ScreenInfo(
            width = width,
            height = height,
            statusBarHeight = prevInfo.statusBarHeight,
            navBarHeight = prevInfo.navBarHeight,
            orientation = orientation,
            safePaddingLeft = prevInfo.safePaddingLeft,
            safePaddingTop = prevInfo.safePaddingTop,
            safePaddingRight = prevInfo.safePaddingRight,
            safePaddingBottom = prevInfo.safePaddingBottom
        )
        Timber.tag("ScreenInfoHolder").d("ScreenInfo newInfo $newInfo")
        set(newInfo)
    }

    private fun readCurrentSize(context: Context): Pair<Int, Int> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val bounds = wm.currentWindowMetrics.bounds
                return bounds.width() to bounds.height()
            } catch (t: Throwable) {
                Timber.tag("ScreenInfoHolder").w(t, "currentWindowMetrics failed; falling back to displayMetrics")
            }
        }
        @Suppress("DEPRECATION")
        val dm = context.resources.displayMetrics
        return dm.widthPixels to dm.heightPixels
    }
}
