package dev.lucianosantos.intervaltimer

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dev.lucianosantos.intervaltimer.core.BaseActivity
import dev.lucianosantos.intervaltimer.core.service.ICountDownTimerService
import dev.lucianosantos.intervaltimer.core.service.NotificationHelper
import dev.lucianosantos.intervaltimer.theme.IntervalTimerTheme

class MainActivity : BaseActivity() {
    override val serviceName: Class<*>
        get() = CountDownTimerServiceMobile::class.java

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all { it ->
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!allPermissionsGranted()) {
            requestPermissions()
        }
        val fromNotification = intent.getBooleanExtra(NotificationHelper.EXTRA_LAUNCH_FROM_NOTIFICATION, false)
        MobileAds.initialize(this)
        setContent {
            val navController = rememberNavController()
            IntervalTimerTheme {
                MainApp(
                    countDownTimerService = countDownTimerServiceProxy,
                    navController = navController
                )
                LaunchedEffect(Unit) {
                    if (fromNotification) {
                        navController.navigate(TimerRunning.route)
                    }
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    navController: NavHostController,
    countDownTimerService: ICountDownTimerService
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BannerAd(
                modifier = Modifier.fillMaxWidth(),
                adId = "ca-app-pub-1325449258005309/4736998612"
            )
        }
    ) { innerPadding ->
        MobileNavHost(
            countDownTimerService = countDownTimerService,
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun BannerAd(modifier: Modifier, adId: String) {
    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                    adUnitId = adId
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
@Preview
fun BannerAdPreview() {
        BannerAd(modifier = Modifier.fillMaxSize(), adId = "ca-app-pub-3940256099942544/6300978111")
}