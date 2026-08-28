package com.djisyncflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.hardware.usb.UsbManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.documentfile.provider.DocumentFile
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.ActivityEventEntity
import com.djisyncflow.data.DEFAULT_DJI_FLIGHT_RECORD_PATHS
import com.djisyncflow.data.DEFAULT_DJI_FLIGHT_RECORD_URI
import com.djisyncflow.data.DeliveryMode
import com.djisyncflow.data.LogFileEntity
import com.djisyncflow.data.LogStatus
import com.djisyncflow.data.isReadyForSync
import com.djisyncflow.dji.DjiSdkController
import com.djisyncflow.dji.DjiSdkState
import com.djisyncflow.sync.NetworkSyncMonitor
import com.djisyncflow.sync.SyncScheduler
import com.djisyncflow.ui.MainViewModel
import com.djisyncflow.ui.UsbKitProgress
import com.djisyncflow.ui.UsbKitStage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.DateFormat
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private const val MSURVEY_DRONELOG_URL = "https://msurvey.orange.com/dronelog"
private const val OFFICIAL_DOC_BASE_URL = "https://drone-compagnon.orange.intra/docs"
private const val PINPOINT_NEARBY_RADIUS_KM = 15.0

private data class CompagnonTheme(
    val id: String,
    val label: String,
    val description: String,
    val colors: List<Color>,
)

private val CompagnonThemes = listOf(
    CompagnonTheme(
        id = "compagnon",
        label = "Compagnon",
        description = "Graphite Orange, très lisible",
        colors = listOf(Color(0xFF17191A), Color(0xFF0B0D0E), Color(0xFF020303)),
    ),
    CompagnonTheme(
        id = "graphite",
        label = "Graphite",
        description = "Noir lisible, reflets froids",
        colors = listOf(Color(0xFF1D2528), Color(0xFF0E1518), Color(0xFF030607)),
    ),
    CompagnonTheme(
        id = "orange_night",
        label = "Orange nuit",
        description = "Ambiance terrain sombre",
        colors = listOf(Color(0xFF2E1B0D), Color(0xFF07171B), Color(0xFF020608)),
    ),
    CompagnonTheme(
        id = "high_readability",
        label = "Contraste",
        description = "Lecture prioritaire en extérieur",
        colors = listOf(Color(0xFF202326), Color(0xFF111416), Color(0xFF050607)),
    ),
)
private val ModuleIconSteel = Color(0xFFDDE7EB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrangeDroneCompagnonTheme {
                val viewModel: MainViewModel = viewModel()
                var showLaunchSplash by remember { mutableStateOf(true) }
                Box(modifier = Modifier.fillMaxSize()) {
                    OrangeDroneCompagnonScreen(
                        viewModel = viewModel,
                        showInitialOverlays = !showLaunchSplash,
                    )
                    OrangeLaunchSplash(
                        visible = showLaunchSplash,
                        onFinished = { showLaunchSplash = false },
                    )
                }
            }
        }
        window.decorView.postDelayed({
            bootstrapAppServices()
        }, 1500L)
    }

    private fun bootstrapAppServices() {
        if (!appServicesStarted.compareAndSet(false, true)) return
        runCatching { SyncScheduler.schedulePeriodic(applicationContext) }
        runCatching { NetworkSyncMonitor(applicationContext).start() }
        runCatching { DjiSdkController.start(applicationContext) }
    }

    companion object {
        private val appServicesStarted = AtomicBoolean(false)
    }
}

@Composable
private fun OrangeLaunchSplash(
    visible: Boolean,
    onFinished: () -> Unit,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        started = true
        delay(1900L)
        onFinished()
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(80)),
        exit = fadeOut(tween(360, easing = FastOutSlowInEasing)),
    ) {
        val logoScale by animateFloatAsState(
            targetValue = if (started) 1f else 0.92f,
            animationSpec = tween(680, easing = FastOutSlowInEasing),
            label = "launch_logo_scale",
        )
        val contentAlpha by animateFloatAsState(
            targetValue = if (started) 1f else 0.82f,
            animationSpec = tween(420, easing = FastOutSlowInEasing),
            label = "launch_content_alpha",
        )
        val titleOffset by animateFloatAsState(
            targetValue = if (started) 0f else 10f,
            animationSpec = tween(620, easing = FastOutSlowInEasing),
            label = "launch_title_offset",
        )
        val lineProgress by animateFloatAsState(
            targetValue = if (started) 1f else 0.2f,
            animationSpec = tween(900, delayMillis = 180, easing = FastOutSlowInEasing),
            label = "launch_line_progress",
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF101314), Color(0xFF050607)))),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = contentAlpha
                    }
                    .padding(horizontal = 32.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.orange_drone_compagnon_mark),
                    contentDescription = null,
                    modifier = Modifier
                        .size(132.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                        },
                )
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.graphicsLayer {
                        translationY = titleOffset
                    },
                ) {
                    Text(
                        text = "Orange",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                        ),
                        color = Color(0xFFFF7900),
                    )
                    Text(
                        text = "Drone Compagnon",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                        ),
                        color = Color.White,
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .width(168.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(lineProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFFFF7900)),
                    )
                }
            }
        }
    }
}

private enum class AppModule(val label: String, val tileDescription: String) {
    HOME("Accueil", "Boîte à outils terrain pour RC DJI"),
    SYNC_LOG("Dépôt MSurvey", "Déposer un log de vol via MSurvey Orange"),
    PLAY_LOG("Consultation des logs", "Consulter, filtrer et analyser les logs de vol"),
    MEDIA("Médias du vol", "Photos et vidéos associées au vol sélectionné"),
    USB_KIT("Export logs et médias", "Exporter via MSurvey ou clé USB avec les médias du vol"),
    SCREEN("Fond d’écran", "Créer un fond d’écran RC validé Orange"),
    INFO("Consignes internes", "Afficher les consignes utiles au télépilote"),
    PINPOINT("Point d’intérêt", "Exporter les sites Orange ANFR en KML pour la carte DJI"),
    CONDITIONS("Conditions de vol", "Aide météo, METAR/TAF et lecture terrain simplifiée"),
    SETTINGS("Réglages", "Réglages dossiers, USB, radio et application"),
}

private fun DjiSdkState.hasConnectedDrone(): Boolean =
    registered && productConnected

private enum class ResponsiveClass {
    Compact,
    Medium,
    Expanded,
}

private val visibleMainModules = listOf(
    AppModule.HOME,
    AppModule.PLAY_LOG,
    AppModule.USB_KIT,
    AppModule.SCREEN,
    AppModule.INFO,
    AppModule.PINPOINT,
    AppModule.CONDITIONS,
    AppModule.SETTINGS,
)

private enum class InfoStartPanel {
    Documents,
    FieldTest,
    AdminLogs,
}

private enum class AdminLogFilter(val label: String) {
    All("Tout"),
    Errors("Erreurs"),
    Usb("USB"),
    Mail("Mail"),
    Dji("DJI"),
}

private fun responsiveClassFor(maxWidth: Dp, maxHeight: Dp): ResponsiveClass =
    when {
        maxWidth < 820.dp || maxHeight < 620.dp -> ResponsiveClass.Compact
        maxWidth < 1180.dp || maxHeight < 760.dp -> ResponsiveClass.Medium
        else -> ResponsiveClass.Expanded
    }

@Composable
private fun currentResponsiveClass(maxWidth: Dp? = null, maxHeight: Dp? = null): ResponsiveClass {
    val configuration = LocalConfiguration.current
    val configWidth = configuration.screenWidthDp.dp
    val configHeight = configuration.screenHeightDp.dp
    val effectiveWidth = listOfNotNull(maxWidth, configWidth).minOrNull() ?: configWidth
    val effectiveHeight = listOfNotNull(maxHeight, configHeight).minOrNull() ?: configHeight
    return responsiveClassFor(effectiveWidth, effectiveHeight)
}

@Composable
private fun isShortLandscapeScreen(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp && configuration.screenHeightDp < 620
}

private enum class DrawerSide { Start, End }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun OrangeDroneCompagnonScreen(
    viewModel: MainViewModel,
    showInitialOverlays: Boolean = true,
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val djiSdkState by viewModel.djiSdkState.collectAsStateWithLifecycle()
    val usbKitProgress by viewModel.usbKitProgress.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf(settings) }
    var selectedModule by remember { mutableStateOf(AppModule.HOME) }
    var showAboutInfo by remember { mutableStateOf(false) }
    var showFirstLaunchAssistant by remember { mutableStateOf(!settings.onboardingCompleted) }
    var showModuleDrawer by remember { mutableStateOf(false) }
    var moduleDrawerSide by remember { mutableStateOf(DrawerSide.End) }
    var infoStartPanel by remember { mutableStateOf(InfoStartPanel.Documents) }
    var sunlightMode by remember { mutableStateOf(false) }
    var lastUsbAutoPromptTick by remember { mutableStateOf(-1) }
    var lastUsbAutoExportKey by remember { mutableStateOf("") }
    var pendingPinPointExport by remember { mutableStateOf<PinPointExportRequest?>(null) }
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()

    LaunchedEffect(settings) {
        draft = settings
        if (!settings.onboardingCompleted) {
            showFirstLaunchAssistant = true
        }
    }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            viewModel.saveFolder(
                uri = it,
                label = it.lastPathSegment ?: it.toString(),
                currentDraft = draft,
            )
        }
    }
    val usbFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            viewModel.saveUsbExportFolder(
                uri = it,
                label = it.lastPathSegment ?: it.toString(),
                currentDraft = draft,
            )
            selectedModule = AppModule.USB_KIT
        }
    }
    val mediaFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            viewModel.saveMediaFolder(
                uri = it,
                label = it.lastPathSegment ?: it.toString(),
                currentDraft = draft,
            )
        }
    }
    val screenExportFolderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            viewModel.saveScreenExportFolder(
                uri = it,
                label = it.lastPathSegment ?: it.toString(),
            )
        }
    }
    val pinPointKmlExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.google-earth.kml+xml"),
    ) { uri ->
        val request = pendingPinPointExport
        if (uri != null && request != null) {
            val exported = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(request.kml.toByteArray(Charsets.UTF_8))
                } ?: error("Flux d’écriture indisponible")
            }
            Toast.makeText(
                context,
                if (exported.isSuccess) "KML exporté pour l’application de vol DJI." else "Export KML impossible : ${exported.exceptionOrNull()?.message ?: "erreur inconnue"}",
                Toast.LENGTH_LONG,
            ).show()
        }
        pendingPinPointExport = null
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val runtimePermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    val requestAppPermissions = {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }.toTypedArray()
        runtimePermissions.launch(permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(settings.onboardingCompleted) {
        if (!settings.onboardingCompleted) return@LaunchedEffect
        requestAppPermissions()
    }

    LaunchedEffect(usbProbeTick, settings.usbExportUri) {
        val usbState = usbDestinationState(context, settings.usbExportUri)
        if (
            usbState == UsbDestinationState.MountedNotAuthorized &&
            lastUsbAutoPromptTick != usbProbeTick
        ) {
            lastUsbAutoPromptTick = usbProbeTick
            selectedModule = AppModule.SETTINGS
            usbFolderPicker.launch(null)
        }
    }

    LaunchedEffect(usbProbeTick, settings.folderUri, settings.usbExportUri, logs, usbKitProgress.stage) {
        val latestLog = logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
        val autoExportKey = listOfNotNull(settings.usbExportUri, latestLog?.id?.toString(), latestLog?.lastModifiedMillis?.toString())
            .joinToString("|")
        val usbReady = usbDestinationState(context, settings.usbExportUri) == UsbDestinationState.Ready
        val idle = usbKitProgress.stage == UsbKitStage.IDLE ||
            usbKitProgress.stage == UsbKitStage.DONE ||
            usbKitProgress.stage == UsbKitStage.ERROR
        if (
            settings.folderUri.isNotBlank() &&
            settings.usbExportUri.isNotBlank() &&
            latestLog != null &&
            usbReady &&
            idle &&
            autoExportKey.isNotBlank() &&
            autoExportKey != lastUsbAutoExportKey
        ) {
            lastUsbAutoExportKey = autoExportKey
            selectedModule = AppModule.USB_KIT
            viewModel.recoverDroneMedia(latestLog.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground(
            themeId = settings.appTheme,
            homeBackdrop = selectedModule == AppModule.HOME && !showAboutInfo,
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (selectedModule == AppModule.HOME && !showAboutInfo) {
                    val screenClass = currentResponsiveClass()
                    val shortLandscape = isShortLandscapeScreen()
                    val headerPadding = when (screenClass) {
                        ResponsiveClass.Compact -> PaddingValues(
                            start = if (shortLandscape) 8.dp else 10.dp,
                            top = 0.dp,
                            end = if (shortLandscape) 6.dp else 8.dp,
                            bottom = 0.dp,
                        )
                        ResponsiveClass.Medium -> PaddingValues(start = 14.dp, top = 1.dp, end = 10.dp, bottom = 1.dp)
                        ResponsiveClass.Expanded -> PaddingValues(start = 18.dp, top = 2.dp, end = 14.dp, bottom = 2.dp)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(headerPadding),
                    ) {
                        HeaderBar(
                            selectedModule = selectedModule,
                            settings = settings,
                            logs = logs,
                            djiSdkState = djiSdkState,
                            sunlightMode = sunlightMode,
                            onOpenSettings = {
                                showAboutInfo = false
                                selectedModule = AppModule.SETTINGS
                            },
                            onOpenInfo = { showAboutInfo = true },
                            onToggleSunlightMode = { sunlightMode = !sunlightMode },
                            onOpenMenu = {
                                moduleDrawerSide = DrawerSide.End
                                showModuleDrawer = true
                            },
                            onModuleChange = {
                                showAboutInfo = false
                                selectedModule = it
                            },
                        )
                    }
                }
            },
        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val screenClass = currentResponsiveClass(maxWidth, maxHeight)
                val outerPadding = when (screenClass) {
                    ResponsiveClass.Compact -> 6.dp
                    ResponsiveClass.Medium -> 8.dp
                    ResponsiveClass.Expanded -> 10.dp
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(outerPadding),
                    verticalArrangement = Arrangement.spacedBy(
                        when (screenClass) {
                            ResponsiveClass.Compact -> 6.dp
                            ResponsiveClass.Medium -> 9.dp
                            ResponsiveClass.Expanded -> 12.dp
                        },
                    ),
                ) {
                    if (showAboutInfo) {
                        AboutReturnBar(
                            onModuleChange = {
                                showAboutInfo = false
                                selectedModule = it
                            },
                            onOpenMenu = {
                                moduleDrawerSide = DrawerSide.Start
                                showModuleDrawer = true
                            },
                            onOpenSettings = {
                                showAboutInfo = false
                                selectedModule = AppModule.SETTINGS
                            },
                        )
                    } else if (selectedModule != AppModule.HOME) {
                        ModuleReturnBar(
                            module = selectedModule,
                            onModuleChange = { selectedModule = it },
                            onOpenMenu = {
                                moduleDrawerSide = DrawerSide.Start
                                showModuleDrawer = true
                            },
                            onOpenSettings = {
                                showAboutInfo = false
                                selectedModule = AppModule.SETTINGS
                            },
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = if (showAboutInfo) "about" else selectedModule.name,
                            transitionSpec = {
                                val forward = targetState > initialState
                                val enterOffset: (Int) -> Int = { width -> if (forward) width / 10 else -width / 10 }
                                val exitOffset: (Int) -> Int = { width -> if (forward) -width / 12 else width / 12 }
                                (fadeIn(tween(140)) + slideInHorizontally(tween(220, easing = FastOutSlowInEasing), enterOffset))
                                    .togetherWith(fadeOut(tween(120)) + slideOutHorizontally(tween(180, easing = FastOutSlowInEasing), exitOffset))
                                    .using(SizeTransform(clip = false))
                            },
                            label = "module_content_transition",
                        ) {
                            ModuleContent(
                                target = it,
                                selectedModule = selectedModule,
                                showAboutInfo = showAboutInfo,
                                settings = settings,
                                draft = draft,
                                logs = logs,
                                events = events,
                                djiSdkState = djiSdkState,
                                usbKitProgress = usbKitProgress,
                                actionMessage = actionMessage,
                                sunlightMode = sunlightMode,
                                infoStartPanel = infoStartPanel,
                                onInfoStartPanelConsumed = { infoStartPanel = InfoStartPanel.Documents },
                                onRefreshLogs = viewModel::refreshLogFolder,
                                onSelectModule = {
                                    showAboutInfo = false
                                    selectedModule = it
                                },
                                onOpenFullDiagnostic = {
                                    showAboutInfo = false
                                    infoStartPanel = InfoStartPanel.AdminLogs
                                    selectedModule = AppModule.INFO
                                },
                                onDecodeLog = viewModel::decodeLog,
                                onPickScreenExportFolder = { screenExportFolderPicker.launch(null) },
                                onSaveScreenProject = viewModel::saveScreenProject,
                                onAddScreenExportHistory = viewModel::addScreenExportHistory,
                                onRetryDjiSdk = viewModel::retryDjiSdk,
                                onExportKml = { request ->
                                    pendingPinPointExport = request
                                    pinPointKmlExporter.launch(request.fileName)
                                },
                                onSyncNow = { viewModel.syncNow(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                onRetryErrors = { viewModel.retryErrors(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                onCheckUpdate = viewModel::checkForUpdateNow,
                                onInstallUpdate = viewModel::installAvailableUpdate,
                                onExportLog = { logId -> viewModel.exportLatestUsb(draft.copy(deliveryMode = DeliveryMode.USB), logId) },
                                onRecoverDroneMedia = { logId -> viewModel.recoverDroneMedia(logId) },
                                onOpenMsurvey = { selectedModule = AppModule.SYNC_LOG },
                                onOpenSettings = {
                                    showAboutInfo = false
                                    selectedModule = AppModule.SETTINGS
                                },
                                onDraftChange = { draft = it },
                                onPickFolder = { folderPicker.launch(null) },
                                onPickUsbFolder = { usbFolderPicker.launch(null) },
                                onPickMediaFolder = { mediaFolderPicker.launch(null) },
                                onThemeChange = viewModel::saveAppTheme,
                                onSaveSettings = { viewModel.saveSettings(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                onTestDestination = { viewModel.testDestination(draft.copy(deliveryMode = DeliveryMode.USB)) },
                            )
                        }
                    }
                }
            }
        }
        if (showInitialOverlays && showFirstLaunchAssistant && !settings.onboardingCompleted) {
            FirstLaunchSetupAssistant(
                draft = draft,
                logs = logs,
                djiSdkState = djiSdkState,
                onDraftChange = { draft = it },
                onThemeChange = { themeId ->
                    draft = draft.copy(appTheme = themeId)
                    viewModel.saveAppTheme(themeId)
                },
                onRequestPermissions = requestAppPermissions,
                onRetryDjiSdk = viewModel::retryDjiSdk,
                onPickFolder = { folderPicker.launch(null) },
                onPickUsbFolder = { usbFolderPicker.launch(null) },
                onTestUsb = { viewModel.testDestination(draft.copy(deliveryMode = DeliveryMode.USB)) },
                onOpenSettings = {
                    selectedModule = AppModule.SETTINGS
                    showAboutInfo = false
                    showFirstLaunchAssistant = false
                },
                onDismiss = { showFirstLaunchAssistant = false },
                onFinish = {
                    val next = draft.copy(
                        deliveryMode = DeliveryMode.USB,
                        onboardingCompleted = true,
                    )
                    draft = next
                    viewModel.saveSettings(next)
                    showFirstLaunchAssistant = false
                },
            )
        }
        AppModuleDrawer(
            visible = showModuleDrawer,
            selectedModule = selectedModule,
            side = moduleDrawerSide,
            onDismiss = { showModuleDrawer = false },
            onModuleChange = { module ->
                showModuleDrawer = false
                showAboutInfo = false
                selectedModule = module
            },
        )
    }
}

@Composable
private fun AppBackground(themeId: String, homeBackdrop: Boolean) {
    val theme = CompagnonThemes.firstOrNull { it.id == themeId } ?: CompagnonThemes.first()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.colors)),
    ) {
        if (homeBackdrop) {
            Image(
                painter = painterResource(R.drawable.home_site_sunset),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.72f },
                contentScale = ContentScale.Crop,
            )
            HomeBackdropOverlay()
        } else {
            OrangeGlassModuleBackdrop()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (homeBackdrop) 0.04f else 0.006f),
                            Color.Transparent,
                            if (homeBackdrop) Color.Black.copy(alpha = 0.32f) else Color(0xFF071419).copy(alpha = 0.20f),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun ModuleContent(
    target: String,
    selectedModule: AppModule,
    showAboutInfo: Boolean,
    settings: AppSettings,
    draft: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    usbKitProgress: UsbKitProgress,
    actionMessage: String,
    sunlightMode: Boolean,
    infoStartPanel: InfoStartPanel,
    onInfoStartPanelConsumed: () -> Unit,
    onRefreshLogs: () -> Unit,
    onSelectModule: (AppModule) -> Unit,
    onOpenFullDiagnostic: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onPickScreenExportFolder: () -> Unit,
    onSaveScreenProject: (String) -> Unit,
    onAddScreenExportHistory: (String) -> Unit,
    onRetryDjiSdk: () -> Unit,
    onExportKml: (PinPointExportRequest) -> Unit,
    onSyncNow: () -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
    onExportLog: (Long?) -> Unit,
    onRecoverDroneMedia: (Long?) -> Unit,
    onOpenMsurvey: () -> Unit,
    onOpenSettings: () -> Unit,
    onDraftChange: (AppSettings) -> Unit,
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onPickMediaFolder: () -> Unit,
    onThemeChange: (String) -> Unit,
    onSaveSettings: () -> Unit,
    onTestDestination: () -> Unit,
) {
    if (showAboutInfo || target == "about") {
        AboutInfoContent(
            settings = settings,
            logs = logs,
            events = events,
            djiSdkState = djiSdkState,
            onOpenFullDiagnostic = onOpenFullDiagnostic,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    when (selectedModule) {
        AppModule.HOME -> HomeContent(
            settings = settings,
            logs = logs,
            djiSdkState = djiSdkState,
            sunlightMode = sunlightMode,
            onRefreshLogs = onRefreshLogs,
            onSelectModule = onSelectModule,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.PLAY_LOG -> PlayLogContent(
            logs = logs,
            actionMessage = actionMessage,
            onDecodeLog = onDecodeLog,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.SCREEN -> OrangeScreenContent(
            settings = settings,
            onPickScreenExportFolder = onPickScreenExportFolder,
            onSaveScreenProject = onSaveScreenProject,
            onAddScreenExportHistory = onAddScreenExportHistory,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.INFO -> InfoContent(
            settings = settings,
            logs = logs,
            events = events,
            djiSdkState = djiSdkState,
            onRetryDjiSdk = onRetryDjiSdk,
            initialPanel = infoStartPanel,
            onInitialPanelConsumed = onInfoStartPanelConsumed,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.PINPOINT -> PinPointContent(
            onExportKml = onExportKml,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.CONDITIONS -> FlightConditionsContent(
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.MEDIA -> MediaConsultationContent(
            logs = logs,
            djiSdkState = djiSdkState,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.SYNC_LOG -> MsurveyOverviewContent(
            settings = settings,
            logs = logs,
            events = events,
            actionMessage = actionMessage,
            onSyncNow = onSyncNow,
            onRetryErrors = onRetryErrors,
            onCheckUpdate = onCheckUpdate,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.USB_KIT -> UsbKitContent(
            settings = settings,
            logs = logs,
            events = events,
            djiSdkState = djiSdkState,
            usbKitProgress = usbKitProgress,
            actionMessage = actionMessage,
            onSyncNow = onSyncNow,
            onExportLog = onExportLog,
            onRecoverDroneMedia = onRecoverDroneMedia,
            onRetryErrors = onRetryErrors,
            onCheckUpdate = onCheckUpdate,
            onOpenMsurvey = onOpenMsurvey,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.fillMaxSize(),
        )
        AppModule.SETTINGS -> SettingsContent(
            draft = draft,
            settings = settings,
            logs = logs,
            djiSdkState = djiSdkState,
            actionMessage = actionMessage,
            onDraftChange = onDraftChange,
            onPickFolder = onPickFolder,
            onPickUsbFolder = onPickUsbFolder,
            onPickMediaFolder = onPickMediaFolder,
            onPickScreenExportFolder = onPickScreenExportFolder,
            onThemeChange = onThemeChange,
            onOpenModule = onSelectModule,
            onRetryDjiSdk = onRetryDjiSdk,
            onRefreshLogs = onRefreshLogs,
            onSave = onSaveSettings,
            onTest = onTestDestination,
            onInstallUpdate = onInstallUpdate,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun AppModuleDrawer(
    visible: Boolean,
    selectedModule: AppModule,
    side: DrawerSide,
    onDismiss: () -> Unit,
    onModuleChange: (AppModule) -> Unit,
) {
    val panelAlignment = if (side == DrawerSide.Start) Alignment.TopStart else Alignment.TopEnd
    val panelOffset: (Int) -> Int = { width -> if (side == DrawerSide.Start) -width else width }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)),
        exit = fadeOut(tween(140)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        ) {
            AnimatedVisibility(
                visible = visible,
                modifier = Modifier.align(panelAlignment),
                enter = slideInHorizontally(
                    animationSpec = tween(240, easing = FastOutSlowInEasing),
                    initialOffsetX = panelOffset,
                ) + fadeIn(tween(120)),
                exit = slideOutHorizontally(
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    targetOffsetX = panelOffset,
                ) + fadeOut(tween(120)),
            ) {
                AppModuleDrawerPanel(
                    selectedModule = selectedModule,
                    onDismiss = onDismiss,
                    onModuleChange = onModuleChange,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(12.dp)
                        .width(360.dp)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun AppModuleDrawerPanel(
    selectedModule: AppModule,
    onDismiss: () -> Unit,
    onModuleChange: (AppModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        containerAlpha = 0.78f,
        borderAlpha = 0.34f,
        highlightAlpha = 0.12f,
        shape = RoundedCornerShape(24.dp),
        fillContainer = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Menu",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "Accès rapide aux outils terrain",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f),
                    )
                }
                Text(
                    text = "Fermer",
                    modifier = Modifier
                        .glassControlSurface(MaterialTheme.shapes.small, accent = Color.White, containerAlpha = 0.10f)
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleMainModules, key = { it.name }) { module ->
                    DrawerModuleRow(
                        module = module,
                        selected = selectedModule == module,
                        onClick = { onModuleChange(module) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerModuleRow(
    module: AppModule,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = module.homeTilePalette()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(120),
        label = "drawer_module_press_${module.name}",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .glassControlSurface(
                shape = RoundedCornerShape(16.dp),
                active = selected,
                accent = palette.secondary,
                containerAlpha = if (selected) 0.18f else 0.10f,
                borderAlpha = if (selected) 0.42f else 0.18f,
                highlightAlpha = 0.14f,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompagnonIcon(
            module = module,
            modifier = Modifier.size(30.dp),
            color = palette.icon,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = module.label,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = moduleSubtitle(module),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.58f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(palette.secondary),
            )
        }
    }
}

@Composable
private fun HomeBackdropOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.30f),
                    Color.Black.copy(alpha = 0.58f),
                    Color.Black.copy(alpha = 0.84f),
                ),
            ),
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.018f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.42f),
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Orange.copy(alpha = 0.30f),
                    Orange.copy(alpha = 0.105f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.18f, size.height * 1.04f),
                radius = size.width * 0.56f,
            ),
            radius = size.width * 0.56f,
            center = Offset(size.width * 0.18f, size.height * 1.04f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.070f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.82f, size.height * 0.10f),
                radius = size.width * 0.34f,
            ),
            radius = size.width * 0.34f,
            center = Offset(size.width * 0.82f, size.height * 0.10f),
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.032f),
                    Orange.copy(alpha = 0.13f),
                    Color.White.copy(alpha = 0.018f),
                    Color.Transparent,
                ),
                start = Offset(-size.width * 0.10f, size.height * 0.86f),
                end = Offset(size.width * 1.06f, size.height * 0.34f),
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.055f),
                    Color.White.copy(alpha = 0.020f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.50f, size.height * 0.46f),
                radius = size.width * 0.42f,
            ),
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.50f, size.height * 0.46f),
        )
        repeat(4) { index ->
            val progress = index / 3f
            drawLine(
                color = Color.White.copy(alpha = 0.030f - progress * 0.008f),
                start = Offset(size.width * (-0.05f + progress * 0.12f), size.height * (0.28f + progress * 0.10f)),
                end = Offset(size.width * (0.98f + progress * 0.08f), size.height * (0.18f + progress * 0.08f)),
                strokeWidth = 1.1f,
                cap = StrokeCap.Round,
            )
        }
        val corporateBand = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.020f),
                Color.Transparent,
            ),
            start = Offset(size.width * 0.70f, -size.height * 0.10f),
            end = Offset(size.width * 0.36f, size.height * 1.05f),
        )
        drawRect(brush = corporateBand)
        repeat(6) { index ->
            val progress = index / 5f
            val y = size.height * (0.62f + progress * 0.28f)
            drawLine(
                color = Orange.copy(alpha = 0.020f + progress * 0.022f),
                start = Offset(-size.width * 0.06f, y),
                end = Offset(size.width * 1.04f, y - size.height * (0.11f + progress * 0.06f)),
                strokeWidth = 0.8f + progress * 1.1f,
                cap = StrokeCap.Round,
            )
        }
        repeat(7) { index ->
            val x = size.width * (index / 6f)
            drawLine(
                color = Color.White.copy(alpha = 0.005f),
                start = Offset(x, 0f),
                end = Offset(x + size.width * 0.12f, size.height),
                strokeWidth = 1f,
            )
        }
    }
}

@Composable
private fun OrangeGlassModuleBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF314650),
                    Color(0xFF22353D),
                    Color(0xFF17262D),
                    Color(0xFF2C3B42),
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.10f),
                    Color.Transparent,
                    Orange.copy(alpha = 0.070f),
                    Color.Transparent,
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Orange.copy(alpha = 0.20f),
                    Orange.copy(alpha = 0.060f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.92f, size.height * 0.08f),
                radius = size.width * 0.48f,
            ),
            radius = size.width * 0.48f,
            center = Offset(size.width * 0.92f, size.height * 0.08f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4BB4E6).copy(alpha = 0.13f),
                    Color(0xFF4BB4E6).copy(alpha = 0.045f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.18f, size.height * 1.04f),
                radius = size.width * 0.52f,
            ),
            radius = size.width * 0.52f,
            center = Offset(size.width * 0.18f, size.height * 1.04f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.080f),
                    Color.White.copy(alpha = 0.025f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.56f, size.height * 0.30f),
                radius = size.width * 0.46f,
            ),
            radius = size.width * 0.46f,
            center = Offset(size.width * 0.56f, size.height * 0.30f),
        )
        repeat(4) { index ->
            val progress = index / 3f
            val y = size.height * (0.36f + progress * 0.10f)
            val path = Path().apply {
                moveTo(-size.width * 0.10f, y)
                cubicTo(
                    size.width * 0.18f,
                    y - size.height * (0.12f + progress * 0.02f),
                    size.width * 0.58f,
                    y + size.height * (0.08f - progress * 0.02f),
                    size.width * 1.10f,
                    y - size.height * (0.08f + progress * 0.03f),
                )
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.060f - progress * 0.008f),
                        Orange.copy(alpha = 0.060f - progress * 0.004f),
                        Color.White.copy(alpha = 0.030f),
                        Color.Transparent,
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                ),
                style = Stroke(width = size.height * (0.028f + progress * 0.010f), cap = StrokeCap.Round),
            )
        }
        repeat(7) { index ->
            val progress = index / 6f
            drawLine(
                color = Orange.copy(alpha = 0.022f + progress * 0.006f),
                start = Offset(size.width * (-0.04f + progress * 0.06f), size.height * (0.84f - progress * 0.04f)),
                end = Offset(size.width * (1.04f - progress * 0.02f), size.height * (0.58f - progress * 0.035f)),
                strokeWidth = 0.9f,
                cap = StrokeCap.Round,
            )
        }
        repeat(9) { index ->
            val x = size.width * (index / 8f)
            drawLine(
                color = Color.White.copy(alpha = 0.010f),
                start = Offset(x, 0f),
                end = Offset(x + size.width * 0.10f, size.height),
                strokeWidth = 0.8f,
            )
        }
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.040f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.26f),
                ),
            ),
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF020608).copy(alpha = 0.30f),
                ),
                center = Offset(size.width * 0.50f, size.height * 0.45f),
                radius = size.width * 0.80f,
            ),
        )
    }
}

@Composable
private fun HeaderBar(
    selectedModule: AppModule,
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    sunlightMode: Boolean,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
    onToggleSunlightMode: () -> Unit,
    onOpenMenu: () -> Unit,
    onModuleChange: (AppModule) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val context = LocalContext.current
        val usbProbeTick = rememberUsbProbeTick()
        val screenClass = currentResponsiveClass(maxWidth)
        val shortLandscape = isShortLandscapeScreen()
        val compact = screenClass == ResponsiveClass.Compact
        val medium = screenClass == ResponsiveClass.Medium
        val latestFlight = remember(logs) {
            logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
        }
        val usbState = remember(settings.usbExportUri, usbProbeTick) {
            usbDestinationState(context, settings.usbExportUri)
        }
        val usbAccessible = usbState == UsbDestinationState.Ready
        val usbAvailableBytes = remember(settings.usbExportUri, usbAccessible, usbProbeTick) {
            if (usbAccessible) resolveUsbAvailableBytes(settings.usbExportUri) else null
        }
        val titleSize = when {
            shortLandscape -> 22.sp
            compact -> 27.sp
            medium -> 31.sp
            else -> 36.sp
        }
        val buttonSize = when {
            shortLandscape -> 36.dp
            compact -> 44.dp
            medium -> 48.dp
            else -> 52.dp
        }
        val iconSize = when {
            shortLandscape -> 20.dp
            compact -> 24.dp
            medium -> 27.dp
            else -> 30.dp
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (shortLandscape) 6.dp else if (compact) 8.dp else 10.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Orange",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = titleSize),
                    color = Orange,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Text(
                    text = " Drone Compagnon",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = titleSize),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            HeaderStatusCluster(
                latestFlight = latestFlight,
                djiSdkState = djiSdkState,
                usbState = usbState,
                usbAvailableBytes = usbAvailableBytes,
                compact = compact || shortLandscape,
                onOpenModule = onModuleChange,
            )
            SunlightHeaderButton(
                sunlightMode = sunlightMode,
                onClick = onToggleSunlightMode,
                controlSize = buttonSize,
                iconSize = iconSize,
            )
            SettingsHeaderButton(onClick = onOpenSettings, controlSize = buttonSize, iconSize = iconSize)
            InfoHeaderButton(onClick = onOpenInfo, controlSize = buttonSize, iconSize = iconSize)
            ModuleMenuButton(
                onClick = onOpenMenu,
                controlSize = buttonSize,
                iconSize = if (compact) 23.dp else 28.dp,
            )
        }
    }
}

@Composable
private fun HeaderStatusCluster(
    latestFlight: LogFileEntity?,
    djiSdkState: DjiSdkState,
    usbState: UsbDestinationState,
    usbAvailableBytes: Long?,
    compact: Boolean,
    onOpenModule: (AppModule) -> Unit,
) {
    val droneConnected = djiSdkState.hasConnectedDrone()
    val latestFlightAccent = if (latestFlight != null) Color(0xFF33D16D) else Color(0xFFFFB020)
    val droneAccent = if (droneConnected) Color(0xFF33D16D) else Color(0xFFFF4D4D)
    val usbAccent = usbState.color
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
    ) {
        HeaderStatusChip(
            label = "Dernier vol",
            value = latestFlight?.let {
                (it.flightStartTimeMillis ?: it.lastModifiedMillis).let(::formatShortTime)
            } ?: "Aucun",
            ledColor = latestFlightAccent,
            compact = compact,
        ) { closePopup ->
            HeaderStatusPopup(
                title = "Dernier vol",
                accent = latestFlightAccent,
                actionLabel = "Ouvrir les logs",
                onAction = {
                    closePopup()
                    onOpenModule(AppModule.PLAY_LOG)
                },
            ) {
                if (latestFlight == null) {
                    HeaderStatusLine("État", "Aucun vol détecté")
                    HeaderStatusLine("Action", "Choisir le dossier des logs ou actualiser")
                } else {
                    val startMillis = latestFlight.flightStartTimeMillis ?: latestFlight.lastModifiedMillis
                    HeaderStatusLine("Début", formatDate(startMillis))
                    HeaderStatusLine(
                        "Fin",
                        latestFlight.flightDurationSeconds?.let { flightEndDateLabel(latestFlight) } ?: "Inconnue",
                    )
                    HeaderStatusLine(
                        "Durée",
                        latestFlight.flightDurationSeconds?.let(::formatDuration) ?: "Inconnue",
                    )
                    HeaderStatusLine("Fichier", latestFlight.fileName)
                    HeaderStatusLine("Taille", formatBytes(latestFlight.sizeBytes))
                    HeaderStatusLine("Statut", latestFlight.status)
                }
            }
        }

        HeaderStatusChip(
            label = "Drone",
            value = if (droneConnected) "Connecté" else "Non connecté",
            ledColor = droneAccent,
            compact = compact,
        ) { closePopup ->
            HeaderStatusPopup(
                title = "Drone",
                accent = droneAccent,
                actionLabel = "Ouvrir l’export",
                onAction = {
                    closePopup()
                    onOpenModule(AppModule.USB_KIT)
                },
            ) {
                HeaderStatusLine("État", if (droneConnected) "Connecté" else "Non connecté")
                HeaderStatusLine(
                    "SDK",
                    when {
                        djiSdkState.registered -> "Enregistré"
                        djiSdkState.initialized -> "Initialisé"
                        djiSdkState.available -> "Initialisation"
                        else -> "Indisponible"
                    },
                )
                HeaderStatusLine(
                    "Modèle",
                    djiSdkState.productType
                        ?: djiSdkState.productId?.takeIf { droneConnected && it > 0 }?.let { "DJI #$it" }
                        ?: "-",
                )
                HeaderStatusLine("Firmware drone", djiSdkState.aircraftFirmwareVersion ?: "-")
                HeaderStatusLine("Radiocommande", djiSdkState.remoteControllerType ?: "-")
                HeaderStatusLine("Firmware RC", djiSdkState.remoteControllerFirmwareVersion ?: "-")
                djiSdkState.firmwareWarning?.let { HeaderStatusLine("Compatibilité", it) }
                HeaderStatusLine(
                    "Message",
                    djiSdkState.message.ifBlank {
                        if (droneConnected) "Drone prêt" else "En attente du drone"
                    },
                )
            }
        }

        HeaderStatusChip(
            label = "USB",
            value = usbState.shortLabel,
            ledColor = usbAccent,
            compact = compact,
        ) { closePopup ->
            HeaderStatusPopup(
                title = "Clé USB",
                accent = usbAccent,
                actionLabel = when (usbState) {
                    UsbDestinationState.Ready -> "Ouvrir l’export"
                    UsbDestinationState.MountedNotAuthorized -> "Monter la clé"
                    UsbDestinationState.NotConfigured,
                    UsbDestinationState.Unavailable -> "Réglages USB"
                },
                onAction = {
                    closePopup()
                    onOpenModule(
                        if (usbState == UsbDestinationState.Ready) {
                            AppModule.USB_KIT
                        } else {
                            AppModule.SETTINGS
                        },
                    )
                },
            ) {
                HeaderStatusLine("État", usbState.destinationLabel)
                HeaderStatusLine(
                    "Accès",
                    when (usbState) {
                        UsbDestinationState.Ready -> "Dossier autorisé"
                        UsbDestinationState.MountedNotAuthorized -> "Dossier racine à autoriser"
                        UsbDestinationState.NotConfigured -> "Aucune clé détectée"
                        UsbDestinationState.Unavailable -> "Clé absente ou démontée"
                    },
                )
                HeaderStatusLine(
                    "Espace",
                    usbAvailableBytes?.let(::formatStorageBytes) ?: if (usbState == UsbDestinationState.Ready) {
                        "Non lisible"
                    } else {
                        "-"
                    },
                )
            }
        }
    }
}

@Composable
private fun HeaderStatusChip(
    label: String,
    value: String,
    ledColor: Color,
    compact: Boolean,
    popupContent: @Composable (closePopup: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .glassControlSurface(
                    shape = MaterialTheme.shapes.small,
                    accent = ledColor,
                    containerAlpha = if (compact) 0.13f else 0.15f,
                    borderAlpha = 0.22f,
                    highlightAlpha = 0.16f,
                )
                .clickable { expanded = true }
                .padding(
                    horizontal = if (compact) 7.dp else 10.dp,
                    vertical = if (compact) 5.dp else 7.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(if (compact) 13.dp else 15.dp)) {
                    drawCircle(ledColor.copy(alpha = 0.30f), radius = size.minDimension / 2f)
                    drawCircle(ledColor, radius = size.minDimension * 0.30f)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (compact) 8.sp else 10.sp),
                    color = Color.White.copy(alpha = 0.62f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = if (compact) 10.sp else 12.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(if (compact) 280.dp else 330.dp)
                .glassMenuSurface(MaterialTheme.shapes.small),
        ) {
            Box(modifier = Modifier.padding(10.dp)) {
                popupContent { expanded = false }
            }
        }
    }
}

@Composable
private fun HeaderStatusPopup(
    title: String,
    accent: Color,
    actionLabel: String,
    onAction: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(containerAlpha = 0.72f, borderAlpha = 0.30f, highlightAlpha = 0.08f) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    drawCircle(accent.copy(alpha = 0.22f), radius = size.minDimension / 2f)
                    drawCircle(accent, radius = size.minDimension * 0.30f)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            content()
            Text(
                text = actionLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassControlSurface(
                        shape = MaterialTheme.shapes.small,
                        active = true,
                        accent = Orange,
                        containerAlpha = 0.20f,
                        borderAlpha = 0.46f,
                        highlightAlpha = 0.18f,
                    )
                    .clickable(onClick = onAction)
                    .padding(vertical = 9.dp),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun HeaderStatusLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color.White.copy(alpha = 0.54f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(58.dp),
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.88f),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SunlightHeaderButton(
    sunlightMode: Boolean,
    onClick: () -> Unit,
    controlSize: Dp = 52.dp,
    iconSize: Dp = 30.dp,
) {
    val activeScale by animateFloatAsState(
        targetValue = if (sunlightMode) 1.0f else 0.92f,
        animationSpec = tween(durationMillis = 180),
        label = "sunlight_icon_scale",
    )
    Box(
        modifier = Modifier
            .size(controlSize)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = sunlightMode,
                accent = Orange,
                containerAlpha = if (sunlightMode) 0.24f else 0.15f,
                borderAlpha = if (sunlightMode) 0.52f else 0.24f,
                highlightAlpha = 0.18f,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.solaris_ui_light_mode),
            contentDescription = "Mode plein soleil",
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = activeScale
                    scaleY = activeScale
                },
            colorFilter = ColorFilter.tint(if (sunlightMode) Color.White else Orange),
        )
    }
}

@Composable
private fun SettingsHeaderButton(
    onClick: () -> Unit,
    controlSize: Dp = 52.dp,
    iconSize: Dp = 30.dp,
) {
    Box(
        modifier = Modifier
            .size(controlSize)
            .glassControlSurface(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.solaris_header_settings),
            contentDescription = "Réglages",
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(Color.White),
        )
    }
}

@Composable
private fun BackHomeHeaderButton(
    onClick: () -> Unit,
    controlSize: Dp = 44.dp,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = Modifier
            .size(controlSize)
            .glassControlSurface(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(iconSize)) {
            val strokeWidth = 3.dp.toPx()
            drawLine(
                Color.White,
                Offset(size.width * 0.68f, size.height * 0.16f),
                Offset(size.width * 0.28f, size.height * 0.50f),
                strokeWidth,
                StrokeCap.Round,
            )
            drawLine(
                Color.White,
                Offset(size.width * 0.28f, size.height * 0.50f),
                Offset(size.width * 0.68f, size.height * 0.84f),
                strokeWidth,
                StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun HomeHeaderButton(
    onClick: () -> Unit,
    controlSize: Dp = 44.dp,
    iconSize: Dp = 24.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "home_button_press",
    )
    Box(
        modifier = Modifier
            .size(controlSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = false,
                accent = Orange,
                containerAlpha = 0.14f,
                borderAlpha = 0.24f,
                highlightAlpha = 0.16f,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.solaris_home),
            contentDescription = "Accueil",
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(Color.White),
        )
    }
}

@Composable
private fun InfoHeaderButton(
    onClick: () -> Unit,
    controlSize: Dp = 52.dp,
    iconSize: Dp = 31.dp,
) {
    Box(
        modifier = Modifier
            .size(controlSize)
            .glassControlSurface(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.solaris_header_info_line),
            contentDescription = "Informations",
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(Color.White),
        )
    }
}

@Composable
private fun ModuleMenuButton(
    onClick: () -> Unit,
    controlSize: Dp = 52.dp,
    iconSize: Dp = 28.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "menu_button_press",
    )
    Box(
        modifier = Modifier
            .size(controlSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .glassControlSurface(MaterialTheme.shapes.small)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.solaris_header_menu_bar),
            contentDescription = "Menu",
            modifier = Modifier.size(iconSize),
            colorFilter = ColorFilter.tint(Color.White),
        )
    }
}

private fun moduleSubtitle(module: AppModule): String =
    when (module) {
        AppModule.HOME -> "Boîte à outils terrain pour RC DJI"
        AppModule.SYNC_LOG -> "Dépôt sécurisé MSurvey Orange"
        AppModule.PLAY_LOG -> "Consultation des logs de vol"
        AppModule.MEDIA -> "Médias associés au vol sélectionné"
        AppModule.USB_KIT -> "Export MSurvey ou clé USB"
        AppModule.SCREEN -> "Personnalisation des radiocommandes"
        AppModule.INFO -> "Consignes internes Orange"
        AppModule.PINPOINT -> "Export KML vers carte DJI"
        AppModule.CONDITIONS -> "Aide météo et décision terrain"
        AppModule.SETTINGS -> "Réglages centralisés"
    }

private data class ModuleHeroMetric(
    val label: String,
    val value: String,
    val accent: Color = Color.White,
)

@Composable
private fun ModuleHero(
    module: AppModule,
    status: String,
    description: String,
    metrics: List<ModuleHeroMetric>,
    modifier: Modifier = Modifier,
    accent: Color = module.homeAccentColor(),
) {
    GlassCard(modifier = modifier, containerAlpha = 0.54f, borderAlpha = 0.28f, highlightAlpha = 0.10f) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = 0.16f),
                            Color.White.copy(alpha = 0.045f),
                            Color.Transparent,
                        ),
                    ),
                ),
        ) {
            val compact = maxWidth < 620.dp
            val iconBoxSize = if (compact) 48.dp else 58.dp
            val iconSize = if (compact) 31.dp else 38.dp
            Column(
                modifier = Modifier.padding(if (compact) 12.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(iconBoxSize)
                            .glassControlSurface(
                                shape = MaterialTheme.shapes.small,
                                active = true,
                                accent = accent,
                                containerAlpha = 0.13f,
                                borderAlpha = 0.34f,
                                highlightAlpha = 0.16f,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CompagnonIcon(
                            module = module,
                            modifier = Modifier.size(iconSize),
                            color = accent,
                            thin = true,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = module.label,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = if (compact) 22.sp else 28.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (compact) 13.sp else 14.sp),
                            color = Color.White.copy(alpha = 0.74f),
                            maxLines = if (compact) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = status,
                        modifier = Modifier
                            .glassControlSurface(
                                shape = MaterialTheme.shapes.small,
                                active = true,
                                accent = accent,
                                containerAlpha = 0.12f,
                                borderAlpha = 0.28f,
                                highlightAlpha = 0.12f,
                            )
                            .padding(horizontal = if (compact) 9.dp else 12.dp, vertical = if (compact) 6.dp else 8.dp),
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = if (compact) 13.sp else 15.sp),
                        color = accent,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                    )
                }
                if (metrics.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        metrics.take(3).forEach { metric ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .glassControlSurface(
                                        shape = MaterialTheme.shapes.small,
                                        accent = metric.accent,
                                        containerAlpha = 0.10f,
                                        borderAlpha = 0.14f,
                                        highlightAlpha = 0.08f,
                                    )
                                    .padding(horizontal = 10.dp, vertical = if (compact) 6.dp else 7.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp),
                            ) {
                                Text(
                                    text = metric.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.56f),
                                    maxLines = 1,
                                )
                                Text(
                                    text = metric.value,
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = if (compact) 13.sp else 14.sp),
                                    color = metric.accent,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleReturnBar(
    module: AppModule,
    onModuleChange: (AppModule) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        val screenClass = currentResponsiveClass(maxWidth)
        val shortLandscape = isShortLandscapeScreen()
        val compact = screenClass == ResponsiveClass.Compact || shortLandscape
        val controlSize = when {
            shortLandscape -> 34.dp
            compact -> 38.dp
            screenClass == ResponsiveClass.Medium -> 42.dp
            else -> 46.dp
        }
        val iconSize = when {
            shortLandscape -> 19.dp
            compact -> 21.dp
            screenClass == ResponsiveClass.Medium -> 23.dp
            else -> 25.dp
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        ) {
            ModuleMenuButton(
                onClick = onOpenMenu,
                controlSize = controlSize,
                iconSize = iconSize,
            )
            HomeHeaderButton(
                onClick = { onModuleChange(AppModule.HOME) },
                controlSize = controlSize,
                iconSize = iconSize,
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .glassControlSurface(
                        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
                        active = false,
                        accent = module.homeTilePalette().secondary,
                        containerAlpha = 0.16f,
                        borderAlpha = 0.24f,
                        highlightAlpha = 0.14f,
                    )
                    .padding(
                        horizontal = if (compact) 10.dp else 13.dp,
                        vertical = if (compact) 6.dp else 7.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 12.dp),
            ) {
                CompagnonIcon(
                    module = module,
                    modifier = Modifier.size(if (compact) 23.dp else 28.dp),
                    color = module.homeTilePalette().icon,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 0.dp else 1.dp),
                ) {
                    Text(
                        text = module.label,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = if (compact) 19.sp else 23.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = moduleSubtitle(module),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = if (compact) 11.sp else 12.sp),
                        color = Color.White.copy(alpha = 0.62f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = tileInitials(module),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(module.homeTilePalette().secondary.copy(alpha = 0.16f))
                        .border(1.dp, module.homeTilePalette().secondary.copy(alpha = 0.30f), MaterialTheme.shapes.small)
                        .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 4.dp else 5.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = if (compact) 10.sp else 11.sp),
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .width(if (compact) 3.dp else 4.dp)
                        .height(if (compact) 28.dp else 34.dp)
                        .clip(CircleShape)
                        .background(module.homeTilePalette().secondary.copy(alpha = 0.90f)),
                )
            }
            if (module != AppModule.SETTINGS) {
                SettingsHeaderButton(onClick = onOpenSettings, controlSize = controlSize, iconSize = iconSize)
            }
        }
    }
}

@Composable
private fun AboutReturnBar(
    onModuleChange: (AppModule) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        val screenClass = currentResponsiveClass(maxWidth)
        val shortLandscape = isShortLandscapeScreen()
        val compact = screenClass == ResponsiveClass.Compact || shortLandscape
        val controlSize = when {
            shortLandscape -> 34.dp
            compact -> 38.dp
            screenClass == ResponsiveClass.Medium -> 42.dp
            else -> 46.dp
        }
        val iconSize = when {
            shortLandscape -> 19.dp
            compact -> 21.dp
            screenClass == ResponsiveClass.Medium -> 23.dp
            else -> 25.dp
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
        ) {
            ModuleMenuButton(
                onClick = onOpenMenu,
                controlSize = controlSize,
                iconSize = iconSize,
            )
            HomeHeaderButton(
                onClick = { onModuleChange(AppModule.HOME) },
                controlSize = controlSize,
                iconSize = iconSize,
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .glassControlSurface(
                        shape = RoundedCornerShape(if (compact) 18.dp else 22.dp),
                        active = false,
                        accent = OdsColor.Info,
                        containerAlpha = 0.11f,
                        borderAlpha = 0.18f,
                        highlightAlpha = 0.12f,
                    )
                    .padding(
                        horizontal = if (compact) 10.dp else 14.dp,
                        vertical = if (compact) 6.dp else 8.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.solaris_header_info_line),
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 23.dp else 28.dp),
                    colorFilter = ColorFilter.tint(OdsColor.Info),
                )
                Text(
                    text = "À propos",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = if (compact) 19.sp else 23.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            SettingsHeaderButton(onClick = onOpenSettings, controlSize = controlSize, iconSize = iconSize)
        }
    }
}

@Composable
private fun HomeContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    sunlightMode: Boolean,
    onRefreshLogs: () -> Unit,
    onSelectModule: (AppModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(settings.folderUri) {
        if (settings.folderUri.isNotBlank()) {
            delay(650)
            onRefreshLogs()
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val landscape = maxWidth > maxHeight && maxWidth >= 700.dp
        if (landscape) {
            HomeDashboardPanel(
                settings = settings,
                logs = logs,
                djiSdkState = djiSdkState,
                sunlightMode = sunlightMode,
                onRefreshLogs = onRefreshLogs,
                onSelectModule = onSelectModule,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    HomeDashboardPanel(
                        settings = settings,
                        logs = logs,
                        djiSdkState = djiSdkState,
                        sunlightMode = sunlightMode,
                        onRefreshLogs = onRefreshLogs,
                        onSelectModule = onSelectModule,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDashboardPanel(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    sunlightMode: Boolean,
    onRefreshLogs: () -> Unit,
    onSelectModule: (AppModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tools = listOf(
        AppModule.PLAY_LOG,
        AppModule.USB_KIT,
        AppModule.SCREEN,
        AppModule.INFO,
        AppModule.PINPOINT,
        AppModule.CONDITIONS,
    )
    val configuration = LocalConfiguration.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val screenClass = currentResponsiveClass(maxWidth, maxHeight)
        val compact = screenClass == ResponsiveClass.Compact
        val landscape = maxWidth > 780.dp || configuration.screenWidthDp > configuration.screenHeightDp
        val shortLandscape = compact && configuration.screenWidthDp > configuration.screenHeightDp
        val columns = if (landscape) 3 else 2
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (compact) 8.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            if (shortLandscape) {
                HomeToolRows(
                    tools = tools,
                    settings = settings,
                    logs = logs,
                    djiSdkState = djiSdkState,
                    columns = 3,
                    sunlightMode = sunlightMode,
                    onSelectModule = onSelectModule,
                    compact = true,
                    dense = true,
                    expandRows = true,
                    modifier = Modifier.weight(1f),
                )
            } else {
                if (landscape) {
                    HomeToolRows(
                        tools = tools,
                        settings = settings,
                        logs = logs,
                        djiSdkState = djiSdkState,
                        columns = columns,
                        sunlightMode = sunlightMode,
                        onSelectModule = onSelectModule,
                        compact = compact,
                        expandRows = true,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    HomeToolRows(
                        tools = tools,
                        settings = settings,
                        logs = logs,
                        djiSdkState = djiSdkState,
                        columns = 2,
                        sunlightMode = sunlightMode,
                        onSelectModule = onSelectModule,
                        compact = compact,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeDashboardSummaryBar(
    detailsExpanded: Boolean,
    hiddenCount: Int,
    compact: Boolean,
    onToggleDetails: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        containerAlpha = 0.38f,
        borderAlpha = 0.18f,
        highlightAlpha = 0.06f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 12.dp else 16.dp, vertical = if (compact) 9.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cockpit terrain",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (compact) 18.sp else 22.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (hiddenCount > 0) {
                        "$hiddenCount info(s) secondaire(s) masquée(s)"
                    } else {
                        "Toutes les informations visibles"
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (compact) 11.sp else 13.sp),
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (hiddenCount > 0 || detailsExpanded) {
                Text(
                    text = if (detailsExpanded) "Réduire" else "Plus d’infos",
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.White.copy(alpha = 0.13f))
                        .clickable(onClick = onToggleDetails)
                        .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 7.dp else 9.dp),
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = if (compact) 12.sp else 14.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardCompactBar(
    detailsExpanded: Boolean,
    hiddenCount: Int,
    onToggleDetails: () -> Unit,
) {
    val shortLandscape = isShortLandscapeScreen()
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        containerAlpha = 0.40f,
        borderAlpha = 0.18f,
        highlightAlpha = 0.06f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = if (shortLandscape) 4.dp else 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Cockpit terrain",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (shortLandscape) 15.sp else 17.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (hiddenCount > 0) "$hiddenCount info(s) masquée(s)" else "Informations visibles",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (shortLandscape) 10.sp else 11.sp),
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = if (detailsExpanded) "Réduire" else "Plus",
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White.copy(alpha = 0.13f))
                    .clickable(onClick = onToggleDetails)
                    .padding(horizontal = 9.dp, vertical = if (shortLandscape) 4.dp else 6.dp),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = if (shortLandscape) 11.sp else 12.sp),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

private data class HomeStatus(
    val label: String,
    val value: String,
    val detail: String,
    val fileName: String? = null,
    val iconModule: AppModule,
    val state: HomeStatusState,
    val action: HomeStatusAction,
)

private enum class HomeStatusState {
    Ok,
    Warning,
    Error,
    Navigate,
}

private sealed interface HomeStatusAction {
    data class OpenModule(val module: AppModule) : HomeStatusAction
    data object RefreshLogs : HomeStatusAction
}

@Composable
private fun HomeStatusMiniStrip(
    cards: List<HomeStatus>,
    sunlightMode: Boolean,
    onSelectModule: (AppModule) -> Unit,
    onRefreshLogs: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        cards.take(3).forEach { card ->
            HomeStatusMiniCard(
                status = card,
                sunlightMode = sunlightMode,
                onClick = {
                    when (val action = card.action) {
                        is HomeStatusAction.OpenModule -> onSelectModule(action.module)
                        HomeStatusAction.RefreshLogs -> onRefreshLogs()
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
        repeat(3 - cards.take(3).size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeStatusMiniCard(
    status: HomeStatus,
    sunlightMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shortLandscape = isShortLandscapeScreen()
    GlassCard(
        modifier = modifier
            .height(if (shortLandscape) 48.dp else 62.dp)
            .clickable(onClick = onClick),
        containerAlpha = if (sunlightMode) 0.70f else 0.54f,
        borderAlpha = if (sunlightMode) 0.50f else 0.30f,
        highlightAlpha = 0.08f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (shortLandscape) 6.dp else 8.dp, vertical = if (shortLandscape) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (shortLandscape) 5.dp else 7.dp),
        ) {
            CompagnonIcon(
                module = status.iconModule,
                modifier = Modifier.size(if (shortLandscape) 24.dp else 30.dp),
                color = statusIconColor(status.iconModule),
                thin = true,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (shortLandscape) 9.sp else 11.sp),
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = status.value,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = if (shortLandscape) 12.sp else 14.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!shortLandscape) {
                StatusIndicator(status.state, compact = true)
            }
        }
    }
}

@Composable
private fun HomeStatusRows(
    cards: List<HomeStatus>,
    sunlightMode: Boolean,
    onSelectModule: (AppModule) -> Unit,
    onRefreshLogs: () -> Unit,
    compact: Boolean = false,
    relaxed: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(if (relaxed) 14.dp else if (compact) 7.dp else 10.dp),
    ) {
        cards.forEach { card ->
            HomeStatusCard(
                status = card,
                sunlightMode = sunlightMode,
                compact = compact,
                relaxed = relaxed,
                modifier = if (relaxed) Modifier.weight(1f) else Modifier,
                onClick = {
                    when (val action = card.action) {
                        is HomeStatusAction.OpenModule -> onSelectModule(action.module)
                        HomeStatusAction.RefreshLogs -> onRefreshLogs()
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeStatusCard(
    status: HomeStatus,
    sunlightMode: Boolean,
    compact: Boolean = false,
    relaxed: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshCard = status.action == HomeStatusAction.RefreshLogs
    GlassCard(
        modifier = modifier
            .then(
                if (relaxed) {
                    Modifier.fillMaxHeight()
                } else {
                    Modifier.height(if (compact) 72.dp else 84.dp)
                },
            )
            .clickable(onClick = onClick),
        containerAlpha = if (sunlightMode) 0.70f else 0.54f,
        borderAlpha = if (sunlightMode) 0.52f else 0.34f,
        highlightAlpha = if (sunlightMode) 0.10f else 0.08f,
    ) {
        Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (compact) 10.dp else 16.dp, vertical = if (compact) 7.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 18.dp),
        ) {
            StatusIcon(status.iconModule, compact = compact)
            Column(
                modifier = Modifier.weight(if (compact) 2.70f else 1.34f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = status.value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = when {
                            compact && status.value.length > 10 -> 18.sp
                            compact -> 21.sp
                            status.value.length > 10 -> 23.sp
                            status.label == "Dernier vol" -> 23.sp
                            else -> 26.sp
                        },
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!compact) {
                Column(
                    modifier = Modifier.weight(1.02f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = status.detail,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    status.fileName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                            color = Color.White.copy(alpha = 0.68f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (isRefreshCard) {
                StatusArrow(compact = compact)
            } else {
                StatusIndicator(status.state, compact = compact)
            }
        }
    }
}

@Composable
private fun StatusIcon(module: AppModule, compact: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (compact) 46.dp else 58.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.06f)),
        contentAlignment = Alignment.Center,
    ) {
        CompagnonIcon(
            module = module,
            modifier = Modifier.size(if (compact) 34.dp else 44.dp),
            color = statusIconColor(module),
            thin = true,
        )
    }
}

private fun statusIconColor(module: AppModule): Color =
    when (module) {
        AppModule.SYNC_LOG -> OdsColor.Success
        AppModule.PLAY_LOG -> OdsColor.Info
        AppModule.MEDIA -> OdsColor.Info
        AppModule.USB_KIT -> OdsColor.Warning
        AppModule.SCREEN -> Color(0xFF8B5CF6)
        AppModule.PINPOINT -> OdsColor.Orange
        AppModule.CONDITIONS -> OdsColor.Success
        AppModule.INFO,
        AppModule.HOME,
        AppModule.SETTINGS -> ModuleIconSteel
    }

@Composable
private fun StatusIndicator(state: HomeStatusState, compact: Boolean = false) {
    Canvas(modifier = Modifier.size(if (compact) 30.dp else 36.dp)) {
        when (state) {
            HomeStatusState.Ok -> {
                drawCircle(Color(0xFF153B2D), radius = size.minDimension / 2f)
                drawLine(
                    color = Color(0xFF33D16D),
                    start = Offset(size.width * 0.30f, size.height * 0.52f),
                    end = Offset(size.width * 0.45f, size.height * 0.68f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color(0xFF33D16D),
                    start = Offset(size.width * 0.45f, size.height * 0.68f),
                    end = Offset(size.width * 0.74f, size.height * 0.34f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            HomeStatusState.Warning -> {
                drawCircle(Color(0xFF4A3115), radius = size.minDimension / 2f)
                drawLine(
                    color = Orange,
                    start = Offset(size.width * 0.50f, size.height * 0.25f),
                    end = Offset(size.width * 0.50f, size.height * 0.58f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawCircle(Orange, radius = 2.5.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.74f))
            }
            HomeStatusState.Error -> {
                drawCircle(Color(0xFF431B1B), radius = size.minDimension / 2f)
                drawLine(
                    color = Color(0xFFFF4D4D),
                    start = Offset(size.width * 0.32f, size.height * 0.32f),
                    end = Offset(size.width * 0.68f, size.height * 0.68f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color(0xFFFF4D4D),
                    start = Offset(size.width * 0.68f, size.height * 0.32f),
                    end = Offset(size.width * 0.32f, size.height * 0.68f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            HomeStatusState.Navigate -> {
                drawCircle(Color.White.copy(alpha = 0.08f), radius = size.minDimension / 2f)
                drawLine(
                    color = Color.White.copy(alpha = 0.72f),
                    start = Offset(size.width * 0.35f, size.height * 0.24f),
                    end = Offset(size.width * 0.64f, size.height * 0.50f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.72f),
                    start = Offset(size.width * 0.64f, size.height * 0.50f),
                    end = Offset(size.width * 0.35f, size.height * 0.76f),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun StatusArrow(compact: Boolean = false) {
    Canvas(modifier = Modifier.size(if (compact) 30.dp else 36.dp)) {
        val stroke = 4.dp.toPx()
        drawLine(
            color = Color.White.copy(alpha = 0.72f),
            start = Offset(size.width * 0.35f, size.height * 0.24f),
            end = Offset(size.width * 0.64f, size.height * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White.copy(alpha = 0.72f),
            start = Offset(size.width * 0.64f, size.height * 0.50f),
            end = Offset(size.width * 0.35f, size.height * 0.76f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun HomeToolRows(
    tools: List<AppModule>,
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    columns: Int,
    sunlightMode: Boolean,
    onSelectModule: (AppModule) -> Unit,
    compact: Boolean = false,
    dense: Boolean = false,
    expandRows: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (dense) 4.dp else if (compact) 8.dp else 12.dp),
    ) {
        tools.chunked(columns).forEach { rowTools ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (expandRows) Modifier.weight(1f) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(if (dense) 4.dp else if (compact) 8.dp else 12.dp),
            ) {
                rowTools.forEach { module ->
                    HomeToolTile(
                        module = module,
                        insight = module.homeInsight(settings, logs, djiSdkState),
                        sunlightMode = sunlightMode,
                        compact = compact,
                        dense = dense,
                        fillParent = expandRows,
                        onClick = { onSelectModule(module) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - rowTools.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeToolTile(
    module: AppModule,
    insight: HomeToolInsight,
    sunlightMode: Boolean,
    compact: Boolean = false,
    dense: Boolean = false,
    fillParent: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = module.homeTilePalette()
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.982f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "tile_press_${module.name}",
    )
    GlassCard(
        modifier = modifier
            .then(
                if (fillParent) {
                    Modifier.fillMaxHeight()
                } else {
                    Modifier.aspectRatio(if (dense) 4.15f else if (compact) 2.15f else 1.16f)
                },
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        containerAlpha = if (sunlightMode) 0.58f else 0.42f,
        borderAlpha = if (sunlightMode) 0.56f else 0.46f,
        highlightAlpha = if (sunlightMode) 0.14f else 0.12f,
        shape = RoundedCornerShape(if (compact) 18.dp else 26.dp),
        glowColor = palette.secondary.copy(alpha = 0.07f),
        fillContainer = true,
    ) {
        if (dense) {
            Box(modifier = Modifier.fillMaxSize()) {
                HomeTileStaticSurface(palette = palette, sunlightMode = sunlightMode)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CompagnonIcon(
                        module = module,
                        modifier = Modifier.size(30.dp),
                        color = palette.icon,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = module.label,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = insight.metric,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.62f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                HomeTileStaticSurface(palette = palette, sunlightMode = sunlightMode)
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(if (compact) 94.dp else 132.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CompagnonIcon(
                        module = module,
                        modifier = Modifier.size(if (compact) 58.dp else 86.dp),
                        color = palette.icon,
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (compact) 10.dp else 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text(
                        text = module.label,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = when {
                                compact && module.label.length > 18 -> 18.sp
                                compact && module.label.length > 12 -> 19.sp
                                compact -> 21.sp
                                module.label.length > 20 -> 25.sp
                                module.label.length > 14 -> 27.sp
                                else -> 29.sp
                            },
                        ),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = if (compact) 10.dp else 18.dp),
                    )
                }
            }
        }
    }
}

private data class HomeToolInsight(
    val status: String,
    val metric: String,
    val description: String,
    val action: String,
    val stateColor: Color,
)

private fun AppModule.homeInsight(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
): HomeToolInsight {
    return when (this) {
        AppModule.PLAY_LOG -> HomeToolInsight(
            status = "",
            metric = "Logs de vol",
            description = "",
            action = "Ouvrir",
            stateColor = OdsColor.Info,
        )
        AppModule.USB_KIT -> HomeToolInsight(
            status = "",
            metric = "USB / MSurvey",
            description = "",
            action = "Exporter",
            stateColor = OdsColor.Warning,
        )
        AppModule.SCREEN -> HomeToolInsight(
            status = "",
            metric = "Fond RC",
            description = "",
            action = "Créer",
            stateColor = OdsColor.Info,
        )
        AppModule.INFO -> HomeToolInsight(
            status = "",
            metric = "Consignes",
            description = "",
            action = "Lire",
            stateColor = ModuleIconSteel,
        )
        AppModule.PINPOINT -> HomeToolInsight(
            status = "",
            metric = "KML",
            description = "",
            action = "Exporter",
            stateColor = OdsColor.Warning,
        )
        AppModule.CONDITIONS -> HomeToolInsight(
            status = "",
            metric = "Météo",
            description = "",
            action = "Analyser",
            stateColor = OdsColor.Success,
        )
        else -> HomeToolInsight(
            status = "",
            metric = "Module",
            description = "",
            action = "Ouvrir",
            stateColor = homeAccentColor(),
        )
    }
}

@Composable
private fun HomeTileStatusPill(status: String, color: Color, compact: Boolean) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.Black.copy(alpha = 0.24f))
            .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = if (compact) 4.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HomeTileStatusDot(color, size = if (compact) 7.dp else 8.dp)
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = if (compact) 10.sp else 12.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun HomeTileStatusDot(color: Color, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

private data class HomeTilePalette(
    val primary: Color,
    val secondary: Color,
    val icon: Color = secondary,
)

@Composable
private fun HomeTileStaticSurface(
    palette: HomeTilePalette,
    sunlightMode: Boolean,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
            color = palette.primary.copy(alpha = if (sunlightMode) 0.28f else 0.18f),
            start = Offset(size.width * 0.08f, size.height - 1.5f),
            end = Offset(size.width * 0.62f, size.height - 1.5f),
            strokeWidth = 2.0f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = palette.secondary.copy(alpha = if (sunlightMode) 0.24f else 0.15f),
            start = Offset(size.width * 0.66f, size.height - 1.5f),
            end = Offset(size.width * 0.92f, size.height - 1.5f),
            strokeWidth = 2.0f,
            cap = StrokeCap.Round,
        )
    }
}

private fun AppModule.homeTilePalette(): HomeTilePalette =
    when (this) {
        AppModule.PLAY_LOG -> HomeTilePalette(OdsColor.Orange, Color(0xFF4BB4E6))
        AppModule.USB_KIT -> HomeTilePalette(OdsColor.Orange, Color(0xFFFFD200), icon = OdsColor.Orange)
        AppModule.SCREEN -> HomeTilePalette(OdsColor.White, Color(0xFFFFB4E6))
        AppModule.INFO -> HomeTilePalette(OdsColor.White, Color(0xFF8F8F8F))
        AppModule.PINPOINT -> HomeTilePalette(OdsColor.Orange, Color(0xFFA885D8))
        AppModule.CONDITIONS -> HomeTilePalette(OdsColor.Orange, Color(0xFF50BE87))
        AppModule.MEDIA -> HomeTilePalette(OdsColor.Orange, Color(0xFF4BB4E6))
        AppModule.SYNC_LOG -> HomeTilePalette(OdsColor.Orange, Color(0xFF50BE87))
        AppModule.SETTINGS -> HomeTilePalette(OdsColor.White, Color(0xFF8F8F8F))
        AppModule.HOME -> HomeTilePalette(OdsColor.Orange, Color(0xFF4BB4E6))
    }

private fun AppModule.homeAccentColor(): Color = homeTilePalette().primary

@Composable
private fun CompagnonIcon(
    module: AppModule,
    modifier: Modifier = Modifier,
    color: Color = Orange,
    thin: Boolean = false,
) {
    Image(
        painter = painterResource(module.solarisIconRes()),
        contentDescription = null,
        modifier = modifier.then(if (thin) Modifier else Modifier),
        colorFilter = ColorFilter.tint(color),
    )
}

private fun AppModule.solarisIconRes(): Int =
    when (this) {
        AppModule.SYNC_LOG -> R.drawable.solaris_cloud_backup
        AppModule.PLAY_LOG -> R.drawable.solaris_drone_top_view
        AppModule.MEDIA -> R.drawable.solaris_drone_top_view
        AppModule.USB_KIT -> R.drawable.solaris_port_usb_key
        AppModule.SCREEN -> R.drawable.solaris_image
        AppModule.INFO -> R.drawable.solaris_header_info_line
        AppModule.PINPOINT -> R.drawable.solaris_location_pin_compass
        AppModule.CONDITIONS -> R.drawable.solaris_weather_partly_cloudy
        AppModule.SETTINGS -> R.drawable.solaris_gears
        AppModule.HOME -> R.drawable.solaris_home
    }

@Composable
private fun ToolTile(
    module: AppModule,
    onClick: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        containerAlpha = 0.44f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tileInitials(module),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = module.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = module.tileDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.76f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun tileInitials(module: AppModule): String =
    when (module) {
        AppModule.SYNC_LOG -> "SL"
        AppModule.PLAY_LOG -> "CL"
        AppModule.MEDIA -> "CM"
        AppModule.USB_KIT -> "USB"
        AppModule.SCREEN -> "SC"
        AppModule.INFO -> "CI"
        AppModule.PINPOINT -> "PP"
        AppModule.CONDITIONS -> "CV"
        AppModule.HOME -> "DK"
        AppModule.SETTINGS -> "SE"
    }

@Composable
private fun MsurveyOverviewContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    actionMessage: String,
    onSyncNow: () -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlassCard(containerAlpha = 0.46f, borderAlpha = 0.24f, highlightAlpha = 0.07f) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompagnonIcon(
                    module = AppModule.SYNC_LOG,
                    modifier = Modifier.size(34.dp),
                    color = statusIconColor(AppModule.SYNC_LOG),
                    thin = true,
                )
                Text(
                    text = "Dépôt sécurisé via MSurvey Orange",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        MsurveyDepositContent(
            modifier = Modifier.weight(1f),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MsurveyDepositContent(
    modifier: Modifier = Modifier,
) {
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        filePathCallback?.onReceiveValue(uris.toTypedArray())
        filePathCallback = null
    }
    GlassCard(modifier = modifier, containerAlpha = 0.38f, borderAlpha = 0.24f, highlightAlpha = 0.05f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MSURVEY DroneLog",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "Le formulaire Orange gère le dépôt et le traitement du log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FlightExportStatusBadge("WEB", Orange)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
                        WebView(context).apply {
                            setLayerType(View.LAYER_TYPE_HARDWARE, null)
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadsImagesAutomatically = true
                            settings.allowContentAccess = true
                            settings.allowFileAccess = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            webViewClient = WebViewClient()
                            webChromeClient = object : WebChromeClient() {
                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    callback: ValueCallback<Array<Uri>>?,
                                    fileChooserParams: WebChromeClient.FileChooserParams?,
                                ): Boolean {
                                    filePathCallback?.onReceiveValue(emptyArray())
                                    filePathCallback = callback
                                    filePicker.launch(arrayOf("text/plain", "application/octet-stream", "*/*"))
                                    return true
                                }
                            }
                            loadUrl(MSURVEY_DRONELOG_URL)
                        }
                    },
                    update = { webView ->
                        if (webView.url.isNullOrBlank()) {
                            webView.loadUrl(MSURVEY_DRONELOG_URL)
                        }
                    },
                )
            }
        }
    }
}

private enum class DroneWeatherProfile(
    val label: String,
    val windLimitKmh: Int,
    val gustLimitKmh: Int,
) {
    Mavic3E("Mavic 3E", 43, 54),
    Matrice4D("Matrice 4D", 43, 54),
    Matrice400("Matrice 400", 54, 65),
}

private data class FlightConditionMetric(
    val label: String,
    val value: String,
    val unit: String,
    val detail: String,
    val accent: Color,
)

private data class FlightWeatherLocation(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

private data class FlightWeatherSearchTarget(
    val label: String,
    val query: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

private data class FlightWeatherForecast(
    val timeLabel: String,
    val condition: String,
    val temperature: String,
    val wind: String,
    val gusts: String,
    val rainRisk: String,
    val flightLabel: String,
    val accent: Color,
)

private data class FlightAviationReportSheet(
    val title: String,
    val badge: String,
    val raw: String,
    val decoded: String,
    val accent: Color,
)

private data class AviationStation(
    val icao: String,
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

private data class AviationReportPayload(
    val raw: String,
    val decoded: String,
)

private data class FlightWeatherReport(
    val location: FlightWeatherLocation,
    val station: AviationStation,
    val score: Int,
    val summary: String,
    val metrics: List<FlightConditionMetric>,
    val forecasts: List<FlightWeatherForecast>,
    val metarRaw: String,
    val metarDecoded: String,
    val tafRaw: String,
    val tafDecoded: String,
    val cachedAtMillis: Long? = null,
    val cacheNotice: String? = null,
)

private data class FlightWeatherBase(
    val report: FlightWeatherReport,
    val station: AviationStation,
)

private sealed interface FlightWeatherState {
    data object Loading : FlightWeatherState
    data class Ready(val report: FlightWeatherReport) : FlightWeatherState
    data class Error(val message: String) : FlightWeatherState
}

private val FrenchAviationStations = listOf(
    AviationStation("LFSB", "Bâle-Mulhouse", 47.59, 7.53),
    AviationStation("LFST", "Strasbourg", 48.55, 7.64),
    AviationStation("LFJL", "Metz-Nancy-Lorraine", 48.98, 6.25),
    AviationStation("LFPG", "Paris Charles-de-Gaulle", 49.01, 2.55),
    AviationStation("LFPO", "Paris Orly", 48.72, 2.38),
    AviationStation("LFQQ", "Lille", 50.57, 3.10),
    AviationStation("LFRN", "Rennes", 48.07, -1.73),
    AviationStation("LFRS", "Nantes", 47.15, -1.61),
    AviationStation("LFBD", "Bordeaux", 44.83, -0.70),
    AviationStation("LFBO", "Toulouse-Blagnac", 43.63, 1.36),
    AviationStation("LFLL", "Lyon Saint-Exupéry", 45.73, 5.08),
    AviationStation("LFLC", "Clermont-Ferrand", 45.79, 3.16),
    AviationStation("LFML", "Marseille Provence", 43.44, 5.22),
    AviationStation("LFMN", "Nice Côte d’Azur", 43.66, 7.21),
    AviationStation("LFMT", "Montpellier", 43.58, 3.96),
)

@Composable
private fun FlightConditionsContent(
    modifier: Modifier = Modifier,
) {
    val appContext = LocalContext.current.applicationContext
    var location by remember { mutableStateOf("Rupt-sur-Moselle") }
    var weatherSearchQuery by remember { mutableStateOf(location) }
    var showWeatherLocationSearch by remember { mutableStateOf(false) }
    var activeTarget by remember {
        mutableStateOf(FlightWeatherSearchTarget(label = location, query = location))
    }
    var profile by remember { mutableStateOf(DroneWeatherProfile.Matrice4D) }
    var selectedMetric by remember { mutableStateOf<FlightConditionMetric?>(null) }
    var selectedForecast by remember { mutableStateOf<FlightWeatherForecast?>(null) }
    var selectedAviationReport by remember { mutableStateOf<FlightAviationReportSheet?>(null) }
    var recentWeatherTargets by remember {
        mutableStateOf(listOf(FlightWeatherSearchTarget(label = "Rupt-sur-Moselle", query = "Rupt-sur-Moselle")))
    }
    val weatherCommunes by produceState<List<PinPointCommune>>(initialValue = emptyList()) {
        value = withContext(Dispatchers.IO) { loadPinPointCommunes(appContext) }
    }
    val weatherCommuneSuggestions = remember(weatherSearchQuery, weatherCommunes) {
        val normalized = normalizeSearchText(weatherSearchQuery)
        if (normalized.length < 2) {
            emptyList()
        } else {
            weatherCommunes.asSequence()
                .filter { commune ->
                    normalizeSearchText(commune.name).contains(normalized) ||
                        commune.code.contains(normalized) ||
                        commune.postalCodes.any { it.contains(normalized) }
                }
                .take(12)
                .toList()
        }
    }
    @SuppressLint("ProduceStateDoesNotAssignValue")
    val weatherState by produceState<FlightWeatherState>(
        initialValue = FlightWeatherState.Loading,
        activeTarget,
        profile,
    ) {
        value = FlightWeatherState.Loading
        value = withContext(Dispatchers.IO) {
            fetchFlightWeatherWithTimeout(appContext, activeTarget, profile)
        }
    }
    val fallbackMetrics = remember(profile) { fallbackFlightConditionMetrics(profile) }
    val report = (weatherState as? FlightWeatherState.Ready)?.report
    val metrics = report?.metrics ?: fallbackMetrics
    val summaryText = when (val state = weatherState) {
        FlightWeatherState.Loading -> "Actualisation des conditions..."
        is FlightWeatherState.Ready -> state.report.summary
        is FlightWeatherState.Error -> state.message
    }
    val statusAccent = when {
        report == null -> Color(0xFF8BE7FF)
        report.score >= 80 -> Color(0xFF00E88A)
        report.score >= 55 -> Color(0xFFFFB23D)
        else -> Color(0xFFFF4D4D)
    }
    fun activateWeatherTarget(target: FlightWeatherSearchTarget) {
        location = target.label
        weatherSearchQuery = target.label
        activeTarget = target
        showWeatherLocationSearch = false
        recentWeatherTargets = (listOf(target) + recentWeatherTargets)
            .distinctBy { normalizeSearchText(it.label) }
            .take(5)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FlightSkyGoHeader(
                location = location,
                activeLocationLabel = report?.let { "${it.location.label} · ${it.station.icao}" } ?: activeTarget.label,
                profile = profile,
                summary = summaryText,
                accent = statusAccent,
                cacheNotice = report?.cacheNotice,
                onOpenLocationSearch = {
                    weatherSearchQuery = location
                    showWeatherLocationSearch = true
                },
                onRefresh = {
                    val query = location.ifBlank { "Rupt-sur-Moselle" }
                    activateWeatherTarget(FlightWeatherSearchTarget(label = query, query = query))
                },
                onProfileChange = { profile = it },
            )
        }
        item {
            FlightWeatherQuickActions(
                activeTarget = activeTarget,
                recentTargets = recentWeatherTargets,
                report = report,
                onTargetSelected = ::activateWeatherTarget,
            )
        }
        item {
            FlightConditionMetricGrid(
                metrics = metrics,
                onMetricClick = { selectedMetric = it },
            )
        }
        item {
            FlightForecastStrip(
                forecasts = report?.forecasts.orEmpty(),
                onForecastClick = { selectedForecast = it },
            )
        }
        item {
            FlightWeatherReportsSection(
                report = report,
                onReportClick = { selectedAviationReport = it },
            )
        }
        item {
            CopyrightFooter()
        }
    }

    selectedMetric?.let { metric ->
        FlightMetricDetailDialog(
            metric = metric,
            profile = profile,
            report = report,
            onDismiss = { selectedMetric = null },
        )
    }
    selectedForecast?.let { forecast ->
        FlightForecastDetailDialog(
            forecast = forecast,
            onDismiss = { selectedForecast = null },
        )
    }
    selectedAviationReport?.let { sheet ->
        FlightAviationReportDialog(
            sheet = sheet,
        onDismiss = { selectedAviationReport = null },
        )
    }
    if (showWeatherLocationSearch) {
        FlightWeatherLocationSearchDialog(
            query = weatherSearchQuery,
            suggestions = weatherCommuneSuggestions,
            onQueryChange = { weatherSearchQuery = it },
            onCommuneSelected = { commune ->
                activateWeatherTarget(commune.toFlightWeatherSearchTarget())
            },
            onSearch = {
                val query = weatherSearchQuery.trim()
                if (query.isBlank()) {
                    showWeatherLocationSearch = false
                } else {
                    activateWeatherTarget(FlightWeatherSearchTarget(label = query, query = query))
                }
            },
            onDismiss = { showWeatherLocationSearch = false },
        )
    }
}

@Composable
private fun FlightSkyGoHeader(
    location: String,
    activeLocationLabel: String,
    profile: DroneWeatherProfile,
    summary: String,
    accent: Color,
    cacheNotice: String?,
    onOpenLocationSearch: () -> Unit,
    onRefresh: () -> Unit,
    onProfileChange: (DroneWeatherProfile) -> Unit,
) {
    GlassCard(containerAlpha = 0.64f, borderAlpha = 0.26f, highlightAlpha = 0.08f, glowColor = accent.copy(alpha = 0.12f)) {
        Column(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 980f,
                    ),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Conditions de vol",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = activeLocationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.64f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = profile.label,
                    modifier = Modifier
                        .glassControlSurface(
                            shape = MaterialTheme.shapes.small,
                            active = true,
                            accent = accent,
                            containerAlpha = 0.12f,
                            borderAlpha = 0.24f,
                            highlightAlpha = 0.10f,
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlightWeatherLocationField(
                    location = location,
                    onOpenSearch = onOpenLocationSearch,
                    modifier = Modifier.weight(1f),
                )
                SecondaryFieldButton(
                    text = "Actualiser",
                    onClick = onRefresh,
                    modifier = Modifier.width(142.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DroneWeatherProfile.entries.forEach { item ->
                    FilterChip(
                        selected = profile == item,
                        onClick = { onProfileChange(item) },
                        label = { Text(item.label, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accent.copy(alpha = 0.22f),
                            selectedLabelColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.07f),
                            labelColor = Color.White,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = profile == item,
                            borderColor = Color.White.copy(alpha = 0.18f),
                            selectedBorderColor = accent.copy(alpha = 0.92f),
                        ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = summary.uppercase(Locale.FRANCE),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                        color = accent,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Données indicatives. La décision de vol reste sous la responsabilité du télépilote et de la préparation réglementaire Orange Drone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (!cacheNotice.isNullOrBlank()) {
                Text(
                    text = cacheNotice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xFFFFB23D).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFFFFB23D).copy(alpha = 0.26f), MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun FlightWeatherQuickActions(
    activeTarget: FlightWeatherSearchTarget,
    recentTargets: List<FlightWeatherSearchTarget>,
    report: FlightWeatherReport?,
    onTargetSelected: (FlightWeatherSearchTarget) -> Unit,
) {
    GlassCard(containerAlpha = 0.34f, borderAlpha = 0.20f, highlightAlpha = 0.06f, glowColor = Color(0xFF50BE87).copy(alpha = 0.06f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WeatherActionChip(
                label = report?.station?.icao?.let { "Station $it" } ?: "Station --",
                value = report?.station?.label ?: "Aviation",
                accent = Color(0xFF8BE7FF),
                active = true,
                onClick = {},
            )
            recentTargets.forEach { target ->
                WeatherActionChip(
                    label = target.label.substringBefore(" ("),
                    value = if (target.label == activeTarget.label) "Actif" else "Récent",
                    accent = Orange,
                    active = target.label == activeTarget.label,
                    onClick = { onTargetSelected(target) },
                )
            }
        }
    }
}

@Composable
private fun WeatherActionChip(
    label: String,
    value: String,
    accent: Color,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(172.dp)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = active,
                accent = accent,
                containerAlpha = 0.10f,
                borderAlpha = 0.22f,
                highlightAlpha = 0.10f,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.58f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightForecastStrip(
    forecasts: List<FlightWeatherForecast>,
    onForecastClick: (FlightWeatherForecast) -> Unit,
    modifier: Modifier = Modifier,
) {
    val forecastScroll = rememberScrollState()
    GlassCard(modifier = modifier, containerAlpha = 0.46f, borderAlpha = 0.22f, highlightAlpha = 0.07f, glowColor = Color(0xFF00E88A).copy(alpha = 0.06f)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Prévisions",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = if (forecasts.isEmpty()) "En attente" else "Glisser pour voir plus loin",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.56f),
                )
            }
            if (forecasts.isEmpty()) {
                Text(
                    text = "Prévisions en attente de réseau.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.68f),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(184.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.White.copy(alpha = 0.045f), Color.Transparent),
                            ),
                        ),
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(forecastScroll)
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        forecasts.forEach { forecast ->
                            FlightForecastCard(
                                forecast = forecast,
                                onClick = { onForecastClick(forecast) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightForecastCard(
    forecast: FlightWeatherForecast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(156.dp)
            .height(180.dp)
            .glassControlSurface(
                shape = RoundedCornerShape(18.dp),
                active = true,
                accent = forecast.accent,
                containerAlpha = 0.11f,
                borderAlpha = 0.20f,
                highlightAlpha = 0.08f,
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = forecast.timeLabel,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = forecast.flightLabel,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(forecast.accent.copy(alpha = 0.16f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.86f),
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
        Text(
            text = forecast.temperature,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 42.sp),
            color = forecast.accent,
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(forecast.condition, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Color.White.copy(alpha = 0.80f), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            ForecastMiniLine(label = "Vent", value = forecast.wind)
            ForecastMiniLine(label = "Raf.", value = forecast.gusts.removePrefix("Raf. "))
            ForecastMiniLine(label = "Pluie", value = forecast.rainRisk)
        }
    }
}

@Composable
private fun ForecastMiniLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = Color.White.copy(alpha = 0.50f), maxLines = 1)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Color.White.copy(alpha = 0.72f), fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun FlightConditionMetricGrid(
    metrics: List<FlightConditionMetric>,
    onMetricClick: (FlightConditionMetric) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val spacing = 8.dp
        val columns = when {
            maxWidth >= 760.dp -> ((metrics.size + 1) / 2).coerceAtLeast(1)
            maxWidth >= 520.dp -> 3
            else -> 2
        }
        val tileSize = ((maxWidth - spacing * (columns - 1)) / columns.toFloat()).coerceIn(148.dp, 286.dp)
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            metrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    rowMetrics.forEach { metric ->
                        FlightConditionMetricCard(
                            metric = metric,
                            onClick = { onMetricClick(metric) },
                            modifier = Modifier.size(tileSize),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightConditionMetricCard(
    metric: FlightConditionMetric,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(220.dp),
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        containerAlpha = 0.50f,
        borderAlpha = 0.26f,
        highlightAlpha = 0.08f,
        glowColor = metric.accent.copy(alpha = 0.08f),
        fillContainer = true,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tileWidth = maxWidth
            val isAeronauticalNightTile = metric.label == "Nuit aéronautique"
            val cardPadding = when {
                tileWidth >= 270.dp -> 15.dp
                tileWidth >= 210.dp -> 12.dp
                else -> 12.dp
            }
            val labelSize = when {
                tileWidth >= 270.dp -> 16.sp
                tileWidth >= 210.dp -> 15.sp
                else -> 13.sp
            }
            val valueSize = when {
                tileWidth >= 270.dp -> 68.sp
                tileWidth >= 240.dp -> 62.sp
                tileWidth >= 190.dp -> 56.sp
                else -> 48.sp
            }
            val nightTimeSize = valueSize
            val unitSize = when {
                tileWidth >= 270.dp -> 20.sp
                tileWidth >= 210.dp -> 18.sp
                else -> 15.sp
            }
            val detailSize = when {
                tileWidth >= 270.dp -> 14.sp
                tileWidth >= 210.dp -> 13.sp
                else -> 12.sp
            }
            val unitBottomPadding = if (tileWidth >= 270.dp) 12.dp else 10.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardPadding),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = metric.label.uppercase(Locale.FRANCE),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = labelSize),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isAeronauticalNightTile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(if (tileWidth >= 240.dp) 8.dp else 4.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(
                                text = "Lever",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = detailSize),
                                color = Color.White.copy(alpha = 0.62f),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = nightTimeSize),
                                color = metric.accent,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(
                                text = "Coucher",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = detailSize),
                                color = Color.White.copy(alpha = 0.62f),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                            )
                            Text(
                                text = metric.unit,
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = nightTimeSize),
                                color = metric.accent,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = metric.value,
                                style = MaterialTheme.typography.displaySmall.copy(fontSize = valueSize),
                                color = metric.accent,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                            )
                            if (metric.unit.isNotBlank()) {
                                Text(
                                    text = metric.unit,
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = unitSize),
                                    color = Color.White.copy(alpha = 0.62f),
                                    modifier = Modifier.padding(bottom = unitBottomPadding),
                                    maxLines = 1,
                                )
                            }
                        }
                        Text(
                            text = metric.detail,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = detailSize),
                            color = Color.White.copy(alpha = 0.64f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private fun fallbackFlightConditionMetrics(profile: DroneWeatherProfile): List<FlightConditionMetric> =
    listOf(
        FlightConditionMetric("Vent", "--", "km/h", "Limite ${profile.windLimitKmh} km/h", Color(0xFF00E88A)),
        FlightConditionMetric("Rafales", "--", "km/h", "Limite ${profile.gustLimitKmh} km/h", Color(0xFF00E88A)),
        FlightConditionMetric("Température", "--", "°C", "En attente réseau", Color(0xFF8BE7FF)),
        FlightConditionMetric("Précipitations", "--", "mm", "En attente réseau", Color(0xFF8BE7FF)),
        FlightConditionMetric("Nébulosité", "--", "%", "Ciel couvert par les nuages", Color(0xFF9AA8FF)),
        FlightConditionMetric("Visibilité", "--", "km", "METAR le plus proche", Color(0xFFFFB23D)),
        FlightConditionMetric("Nuit aéronautique", "--:--", "--:--", "", Color(0xFFFFB23D)),
    )

private fun fetchFlightWeatherWithTimeout(
    context: Context,
    target: FlightWeatherSearchTarget,
    profile: DroneWeatherProfile,
): FlightWeatherState {
    weatherNetworkProblem(context)?.let { message ->
        Log.w("OrangeWeather", message)
        return cachedFlightWeatherReport(context, target, profile, message)
            ?.let { FlightWeatherState.Ready(it) }
            ?: FlightWeatherState.Error(message)
    }
    val executor = Executors.newSingleThreadExecutor()
    return try {
        val base = executor.submit<FlightWeatherBase> {
            fetchFlightWeatherBase(context, target, profile)
        }.get(12, TimeUnit.SECONDS)
        val metar = fetchAviationReportWithTimeout(context, "metar", base.station, base.report.location)
        val taf = fetchAviationReportWithTimeout(context, "taf", base.station, base.report.location)
        val report = base.report.copy(
                station = base.station,
                metarRaw = metar.raw,
                metarDecoded = metar.decoded,
                tafRaw = taf.raw,
                tafDecoded = taf.decoded,
                cachedAtMillis = System.currentTimeMillis(),
                cacheNotice = null,
            )
        cacheFlightWeatherReport(context, target, profile, report)
        FlightWeatherState.Ready(
            report,
        )
    } catch (error: TimeoutException) {
        Log.w("OrangeWeather", "Open-Meteo timeout for ${target.query}", error)
        val message = "Météo indisponible : Open-Meteo trop lent."
        cachedFlightWeatherReport(context, target, profile, message)
            ?.let { FlightWeatherState.Ready(it) }
            ?: FlightWeatherState.Error(message)
    } catch (error: Exception) {
        Log.w("OrangeWeather", "Weather fetch failed for ${target.query}", error)
        val message = "Météo indisponible : vérifier la connexion réseau de la radiocommande."
        cachedFlightWeatherReport(context, target, profile, message)
            ?.let { FlightWeatherState.Ready(it) }
            ?: FlightWeatherState.Error(message)
    } finally {
        executor.shutdownNow()
    }
}

private fun fetchFlightWeatherBase(
    context: Context,
    target: FlightWeatherSearchTarget,
    profile: DroneWeatherProfile,
): FlightWeatherBase {
    val targetLatitude = target.latitude
    val targetLongitude = target.longitude
    val location = if (targetLatitude != null && targetLongitude != null) {
        FlightWeatherLocation(
            label = target.label,
            latitude = targetLatitude,
            longitude = targetLongitude,
        )
    } else {
        fetchOpenMeteoLocation(context, target.query)
    }
    val station = FrenchAviationStations.minBy {
        distanceMetersBetween(location.latitude, location.longitude, it.latitude, it.longitude)
    }
    return FlightWeatherBase(
        report = fetchOpenMeteoForecast(context, location, profile),
        station = station,
    )
}

private fun fetchOpenMeteoLocation(context: Context, query: String): FlightWeatherLocation {
    val encoded = URLEncoder.encode(query.ifBlank { "Rupt-sur-Moselle" }, "UTF-8")
    val json = JSONObject(
        readUrlText(
            context = context,
            url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=1&language=fr&format=json",
            connectTimeoutMillis = 3500,
            readTimeoutMillis = 4500,
        ),
    )
    val result = json.optJSONArray("results")?.optJSONObject(0)
        ?: error("Commune introuvable")
    val city = result.optString("name", query)
    val admin = result.optString("admin1").takeIf { it.isNotBlank() }
    return FlightWeatherLocation(
        label = listOfNotNull(city, admin).joinToString(" · "),
        latitude = result.getDouble("latitude"),
        longitude = result.getDouble("longitude"),
    )
}

private fun fetchOpenMeteoForecast(
    context: Context,
    location: FlightWeatherLocation,
    profile: DroneWeatherProfile,
): FlightWeatherReport {
    val url = "https://api.open-meteo.com/v1/forecast" +
        "?latitude=${location.latitude}&longitude=${location.longitude}" +
        "&current=temperature_2m,precipitation,weather_code,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m" +
        "&hourly=temperature_2m,precipitation_probability,weather_code,wind_speed_10m,wind_gusts_10m" +
        "&daily=sunrise,sunset&timezone=auto&wind_speed_unit=kmh&precipitation_unit=mm"
    val root = JSONObject(
        readUrlText(
            context = context,
            url = url,
            connectTimeoutMillis = 3500,
            readTimeoutMillis = 6000,
        ),
    )
    val current = root.getJSONObject("current")
    val daily = root.optJSONObject("daily")
    val wind = current.optDouble("wind_speed_10m", Double.NaN)
    val gusts = current.optDouble("wind_gusts_10m", Double.NaN)
    val temp = current.optDouble("temperature_2m", Double.NaN)
    val rain = current.optDouble("precipitation", Double.NaN)
    val cloud = current.optDouble("cloud_cover", Double.NaN)
    val visibilityMeters = current.optDouble("visibility", Double.NaN)
    val windDirection = current.optDouble("wind_direction_10m", Double.NaN)
    val weatherCode = current.optInt("weather_code", -1)
    val sunset = daily?.optJSONArray("sunset")?.optString(0).orEmpty()
    val sunrise = daily?.optJSONArray("sunrise")?.optString(0).orEmpty()
    val night = timePlusMinutesLabel(sunset, 30)
    val morning = timePlusMinutesLabel(sunrise, -30)
    val windOk = wind.isFinite() && wind <= profile.windLimitKmh
    val gustOk = gusts.isFinite() && gusts <= profile.gustLimitKmh
    val precipOk = rain.isFinite() && rain <= 0.2
    val visibilityOk = visibilityMeters.isFinite() && visibilityMeters >= 5000.0
    val currentTimeLabel = hourLabel(current.optString("time"))
    val isAeronauticalNight = isInsideAeronauticalNight(currentTimeLabel, morning, night)
    val weatherScore = listOf(windOk, gustOk, precipOk, visibilityOk).count { it } * 25
    val score = if (isAeronauticalNight) 0 else weatherScore
    val summary = when {
        isAeronauticalNight -> "Nuit aéronautique détectée"
        score >= 80 -> "Lecture météo terrain"
        score >= 55 -> "Paramètres météo à examiner"
        else -> "Paramètres météo sensibles"
    }
    val windCardinal = if (windDirection.isFinite()) cardinalDirection(windDirection) else "-"
    val metrics = listOf(
        FlightConditionMetric("Vent", wind.formatRounded(), "km/h", "$windCardinal · limite ${profile.windLimitKmh} km/h", if (windOk) Color(0xFF00E88A) else Color(0xFFFF4D4D)),
        FlightConditionMetric("Rafales", gusts.formatRounded(), "km/h", "Limite ${profile.gustLimitKmh} km/h", if (gustOk) Color(0xFF00E88A) else Color(0xFFFF4D4D)),
        FlightConditionMetric("Température", temp.formatRounded(), "°C", weatherCodeLabel(weatherCode), Color(0xFF8BE7FF)),
        FlightConditionMetric("Précipitations", rain.formatOneDecimal(), "mm", if (precipOk) "Pas de pluie significative" else "Pluie détectée", if (precipOk) Color(0xFF00E88A) else Color(0xFFFFB23D)),
        FlightConditionMetric("Nébulosité", cloud.formatRounded(), "%", "Ciel couvert par les nuages", Color(0xFF9AA8FF)),
        FlightConditionMetric("Visibilité", (visibilityMeters / 1000.0).formatOneDecimal(), "km", if (visibilityOk) "Bonne visibilité" else "Visibilité réduite", if (visibilityOk) Color(0xFF00E88A) else Color(0xFFFF4D4D)),
        FlightConditionMetric(
            "Nuit aéronautique",
            morning,
            night,
            if (isAeronauticalNight) "Vol strictement interdit pendant la nuit aéronautique" else "Vol possible hors nuit aéronautique",
            if (isAeronauticalNight) Color(0xFFFF4D4D) else Color(0xFFFFB23D),
        ),
    )
    val forecasts = parseHourlyFlightForecasts(root, current.optString("time"), profile, morning, night)
    return FlightWeatherReport(
        location = location,
        station = FrenchAviationStations.first(),
        score = score,
        summary = summary,
        metrics = metrics,
        forecasts = forecasts,
        metarRaw = "",
        metarDecoded = "",
        tafRaw = "",
        tafDecoded = "",
    )
}

private fun fetchAviationReportWithTimeout(
    context: Context,
    kind: String,
    station: AviationStation,
    location: FlightWeatherLocation,
): AviationReportPayload {
    val executor = Executors.newSingleThreadExecutor()
    return try {
        executor.submit<AviationReportPayload> {
            fetchAviationReportWithFallback(context, kind, station, location)
        }.get(8, TimeUnit.SECONDS)
    } catch (error: TimeoutException) {
        Log.w("OrangeWeather", "$kind timeout for ${station.icao}", error)
        aviationUnavailablePayload(kind, station, emptyList(), "Délai réseau dépassé.")
    } catch (error: Exception) {
        Log.w("OrangeWeather", "$kind failed for ${station.icao}", error)
        aviationUnavailablePayload(kind, station, emptyList(), "Aucun bulletin aviation exploitable.")
    } finally {
        executor.shutdownNow()
    }
}

private fun fetchAviationReportWithFallback(
    context: Context,
    kind: String,
    requestedStation: AviationStation,
    location: FlightWeatherLocation,
): AviationReportPayload {
    val candidates = aviationFallbackCandidates(requestedStation, location)
    candidates.forEach { candidate ->
        fetchAviationReportFromJson(context, kind, requestedStation, candidate)?.let { return it }
        fetchAviationReportFromRaw(context, kind, requestedStation, candidate)?.let { return it }
    }
    return aviationUnavailablePayload(
        kind = kind,
        requestedStation = requestedStation,
        candidates = candidates,
        reason = "Aucun bulletin publié sur les stations de secours.",
    )
}

private fun aviationFallbackCandidates(
    requestedStation: AviationStation,
    location: FlightWeatherLocation,
): List<AviationStation> {
    val nearby = FrenchAviationStations
        .sortedBy { distanceMetersBetween(location.latitude, location.longitude, it.latitude, it.longitude) }
    return (listOf(requestedStation) + nearby)
        .distinctBy { it.icao }
        .take(5)
}

private fun fetchAviationReportFromJson(
    context: Context,
    kind: String,
    requestedStation: AviationStation,
    candidate: AviationStation,
): AviationReportPayload? =
    runCatching {
        val root = JSONArray(
            readUrlText(
                context = context,
                url = "https://aviationweather.gov/api/data/$kind?ids=${candidate.icao}&format=json",
                connectTimeoutMillis = 2200,
                readTimeoutMillis = 2600,
            ),
        )
        val first = root.optJSONObject(0) ?: return@runCatching null
        val raw = first.optString(if (kind == "taf") "rawTAF" else "rawOb").cleanAviationRaw()
            ?: return@runCatching null
        val decoded = decodeAviationRaw(kind.uppercase(Locale.FRANCE), first, raw)
        aviationPayloadWithFallbackNote(
            kind = kind,
            requestedStation = requestedStation,
            candidate = candidate,
            raw = raw,
            decoded = decoded,
            usedRawFallback = false,
        )
    }.getOrNull()

private fun fetchAviationReportFromRaw(
    context: Context,
    kind: String,
    requestedStation: AviationStation,
    candidate: AviationStation,
): AviationReportPayload? =
    runCatching {
        val raw = readUrlText(
            context = context,
            url = "https://aviationweather.gov/api/data/$kind?ids=${candidate.icao}&format=raw",
            connectTimeoutMillis = 2200,
            readTimeoutMillis = 2600,
        ).cleanAviationRaw() ?: return@runCatching null
        val decoded = decodePlainAviationRaw(kind.uppercase(Locale.FRANCE), candidate, raw)
        aviationPayloadWithFallbackNote(
            kind = kind,
            requestedStation = requestedStation,
            candidate = candidate,
            raw = raw,
            decoded = decoded,
            usedRawFallback = true,
        )
    }.getOrNull()

private fun aviationPayloadWithFallbackNote(
    kind: String,
    requestedStation: AviationStation,
    candidate: AviationStation,
    raw: String,
    decoded: String,
    usedRawFallback: Boolean,
): AviationReportPayload {
    val notes = buildList {
        if (candidate.icao != requestedStation.icao) {
            add("Secours|${requestedStation.icao} indisponible, lecture ${candidate.icao} · ${candidate.label}")
        }
        if (usedRawFallback) {
            add("Source|Lecture brute ${kind.uppercase(Locale.FRANCE)} utilisée en secours")
        }
    }
    val fullDecoded = (notes + decoded.lines().filter { it.isNotBlank() }).joinToString("\n")
    return AviationReportPayload(raw = raw, decoded = fullDecoded)
}

private fun aviationUnavailablePayload(
    kind: String,
    requestedStation: AviationStation,
    candidates: List<AviationStation>,
    reason: String,
): AviationReportPayload {
    val tried = candidates.ifEmpty { listOf(requestedStation) }.joinToString(", ") { it.icao }
    val label = kind.uppercase(Locale.FRANCE)
    return AviationReportPayload(
        raw = "Non disponible",
        decoded = buildString {
            appendLine("Station|${requestedStation.icao} · ${requestedStation.label}")
            appendLine("Secours|Stations essayées : $tried")
            append("Lecture|$label indisponible. $reason")
        },
    )
}

private fun String.cleanAviationRaw(): String? {
    val raw = lineSequence()
        .map { it.trim() }
        .filter { line ->
            line.isNotBlank() &&
                !line.equals("Non disponible", ignoreCase = true) &&
                !line.contains("No data", ignoreCase = true) &&
                !line.startsWith("[") &&
                !line.startsWith("{")
        }
        .joinToString(" ")
        .replace(Regex("""\s+"""), " ")
        ?.trim()
    return raw?.takeIf { it.isNotBlank() }
}

private fun parseHourlyFlightForecasts(
    root: JSONObject,
    currentTime: String,
    profile: DroneWeatherProfile,
    morningEnd: String,
    eveningStart: String,
): List<FlightWeatherForecast> {
    val hourly = root.optJSONObject("hourly") ?: return emptyList()
    val times = hourly.optJSONArray("time") ?: return emptyList()
    val temperatures = hourly.optJSONArray("temperature_2m")
    val rainRisks = hourly.optJSONArray("precipitation_probability")
    val weatherCodes = hourly.optJSONArray("weather_code")
    val winds = hourly.optJSONArray("wind_speed_10m")
    val gusts = hourly.optJSONArray("wind_gusts_10m")
    val count = times.length()
    val startIndex = (0 until count).firstOrNull { index ->
        val time = times.optString(index)
        currentTime.isBlank() || time >= currentTime
    } ?: 0
    return (startIndex until count)
        .take(18)
        .map { index ->
            val timeLabel = hourLabel(times.optString(index))
            val wind = winds?.optDouble(index, Double.NaN) ?: Double.NaN
            val gust = gusts?.optDouble(index, Double.NaN) ?: Double.NaN
            val rainRisk = rainRisks?.optDouble(index, Double.NaN) ?: Double.NaN
            val code = weatherCodes?.optInt(index, -1) ?: -1
            val isNight = isInsideAeronauticalNight(timeLabel, morningEnd, eveningStart)
            val ok = !isNight &&
                wind.isFinite() &&
                gust.isFinite() &&
                wind <= profile.windLimitKmh &&
                gust <= profile.gustLimitKmh &&
                (!rainRisk.isFinite() || rainRisk < 45.0)
            FlightWeatherForecast(
                timeLabel = timeLabel,
                condition = if (isNight) "${weatherCodeLabel(code)} · nuit aéro" else weatherCodeLabel(code),
                temperature = "${(temperatures?.optDouble(index, Double.NaN) ?: Double.NaN).formatRounded()}°",
                wind = "${wind.formatRounded()} km/h",
                gusts = "Raf. ${gust.formatRounded()}",
                rainRisk = if (rainRisk.isFinite()) "${rainRisk.roundToInt()}%" else "--",
                flightLabel = if (isNight) "INTERDIT" else if (ok) "OK" else "SURV.",
                accent = if (isNight) Color(0xFFFF4D4D) else if (ok) Color(0xFF00E88A) else Color(0xFFFFB23D),
            )
        }
}

private const val WEATHER_CACHE_PREFS = "orange_drone_weather_cache"
private const val WEATHER_CACHE_MAX_AGE_MILLIS = 6L * 60L * 60L * 1000L

private fun weatherCacheKey(target: FlightWeatherSearchTarget, profile: DroneWeatherProfile): String {
    val locationKey = listOfNotNull(
        target.latitude?.let { String.format(Locale.US, "%.4f", it) },
        target.longitude?.let { String.format(Locale.US, "%.4f", it) },
    ).joinToString("_").ifBlank { normalizeSearchText(target.query.ifBlank { target.label }) }
    return "${profile.name}_$locationKey"
}

private fun cacheFlightWeatherReport(
    context: Context,
    target: FlightWeatherSearchTarget,
    profile: DroneWeatherProfile,
    report: FlightWeatherReport,
) {
    runCatching {
        context.getSharedPreferences(WEATHER_CACHE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(weatherCacheKey(target, profile), flightWeatherReportToJson(report).toString())
            .apply()
    }.onFailure {
        Log.w("OrangeWeather", "Unable to cache weather report", it)
    }
}

private fun cachedFlightWeatherReport(
    context: Context,
    target: FlightWeatherSearchTarget,
    profile: DroneWeatherProfile,
    reason: String,
): FlightWeatherReport? =
    runCatching {
        val raw = context.getSharedPreferences(WEATHER_CACHE_PREFS, Context.MODE_PRIVATE)
            .getString(weatherCacheKey(target, profile), null)
            ?: return@runCatching null
        val report = flightWeatherReportFromJson(JSONObject(raw)) ?: return@runCatching null
        val ageMillis = System.currentTimeMillis() - (report.cachedAtMillis ?: 0L)
        if (ageMillis !in 0..WEATHER_CACHE_MAX_AGE_MILLIS) return@runCatching null
        report.copy(
            summary = "${report.summary} · hors-ligne",
            cacheNotice = "$reason Dernier bulletin enregistré utilisé (${formatWeatherCacheAge(ageMillis)}).",
        )
    }.onFailure {
        Log.w("OrangeWeather", "Unable to read cached weather report", it)
    }.getOrNull()

private fun flightWeatherReportToJson(report: FlightWeatherReport): JSONObject =
    JSONObject().apply {
        put("cachedAtMillis", report.cachedAtMillis ?: System.currentTimeMillis())
        put("location", JSONObject().apply {
            put("label", report.location.label)
            put("latitude", report.location.latitude)
            put("longitude", report.location.longitude)
        })
        put("station", JSONObject().apply {
            put("icao", report.station.icao)
            put("label", report.station.label)
            put("latitude", report.station.latitude)
            put("longitude", report.station.longitude)
        })
        put("score", report.score)
        put("summary", report.summary.removeSuffix(" · hors-ligne"))
        put("metarRaw", report.metarRaw)
        put("metarDecoded", report.metarDecoded)
        put("tafRaw", report.tafRaw)
        put("tafDecoded", report.tafDecoded)
        put("metrics", JSONArray().apply {
            report.metrics.forEach { metric ->
                put(JSONObject().apply {
                    put("label", metric.label)
                    put("value", metric.value)
                    put("unit", metric.unit)
                    put("detail", metric.detail)
                    put("accent", metric.accent.toArgb())
                })
            }
        })
        put("forecasts", JSONArray().apply {
            report.forecasts.forEach { forecast ->
                put(JSONObject().apply {
                    put("timeLabel", forecast.timeLabel)
                    put("condition", forecast.condition)
                    put("temperature", forecast.temperature)
                    put("wind", forecast.wind)
                    put("gusts", forecast.gusts)
                    put("rainRisk", forecast.rainRisk)
                    put("flightLabel", forecast.flightLabel)
                    put("accent", forecast.accent.toArgb())
                })
            }
        })
    }

private fun flightWeatherReportFromJson(root: JSONObject): FlightWeatherReport? =
    runCatching {
        val location = root.getJSONObject("location")
        val station = root.getJSONObject("station")
        FlightWeatherReport(
            location = FlightWeatherLocation(
                label = location.getString("label"),
                latitude = location.getDouble("latitude"),
                longitude = location.getDouble("longitude"),
            ),
            station = AviationStation(
                icao = station.getString("icao"),
                label = station.getString("label"),
                latitude = station.getDouble("latitude"),
                longitude = station.getDouble("longitude"),
            ),
            score = root.optInt("score", 0),
            summary = root.optString("summary", "Données météo"),
            metrics = jsonMetricList(root.optJSONArray("metrics")),
            forecasts = jsonForecastList(root.optJSONArray("forecasts")),
            metarRaw = root.optString("metarRaw", "Non disponible"),
            metarDecoded = root.optString("metarDecoded", ""),
            tafRaw = root.optString("tafRaw", "Non disponible"),
            tafDecoded = root.optString("tafDecoded", ""),
            cachedAtMillis = root.optLong("cachedAtMillis", 0L).takeIf { it > 0L },
        )
    }.getOrNull()

private fun jsonMetricList(array: JSONArray?): List<FlightConditionMetric> =
    if (array == null) {
        emptyList()
    } else {
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            FlightConditionMetric(
                label = item.optString("label"),
                value = item.optString("value"),
                unit = item.optString("unit"),
                detail = item.optString("detail"),
                accent = Color(item.optInt("accent", Color.White.toArgb())),
            )
        }
    }

private fun jsonForecastList(array: JSONArray?): List<FlightWeatherForecast> =
    if (array == null) {
        emptyList()
    } else {
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            FlightWeatherForecast(
                timeLabel = item.optString("timeLabel"),
                condition = item.optString("condition"),
                temperature = item.optString("temperature"),
                wind = item.optString("wind"),
                gusts = item.optString("gusts"),
                rainRisk = item.optString("rainRisk"),
                flightLabel = item.optString("flightLabel"),
                accent = Color(item.optInt("accent", Color.White.toArgb())),
            )
        }
    }

private fun formatWeatherCacheAge(ageMillis: Long): String {
    val minutes = (ageMillis / 60_000L).coerceAtLeast(0L)
    return when {
        minutes < 2L -> "à l’instant"
        minutes < 60L -> "il y a ${minutes} min"
        else -> "il y a ${minutes / 60L} h ${minutes % 60L} min"
    }
}

private fun decodeAviationRaw(kind: String, json: JSONObject, raw: String): String {
    if (kind == "TAF") return decodeTafRaw(json, raw)
    return decodeMetarRaw(kind, json, raw)
}

private fun decodeMetarRaw(kind: String, json: JSONObject, raw: String): String {
    val station = listOf(json.optString("icaoId"), json.optString("name")).filter { it.isNotBlank() }.joinToString(" · ")
    val wind = if (json.has("wspd")) {
        aviationWindLabel(json.opt("wdir"), json.optInt("wspd", 0), json.optInt("wgst", 0))
    } else {
        "Vent non détaillé"
    }
    val visibility = aviationVisibilityLabel(json.opt("visib")?.toString(), raw)
    val temp = json.opt("temp")?.toString()?.takeIf { it.isNotBlank() }?.let { "$it°C" } ?: "Non précisée"
    val category = json.optString("fltCat").takeIf { it.isNotBlank() }
    val weather = aviationWeatherLabel(json.optString("wxString"), raw)
    return buildString {
        appendLine("Station|${station.ifBlank { kind }}")
        appendLine("Vent|$wind")
        appendLine("Visibilité|$visibility${category?.let { " · $it" }.orEmpty()}")
        appendLine("Température|$temp")
        append("Ciel / météo|$weather")
    }
}

private fun decodeTafRaw(json: JSONObject, raw: String): String {
    val station = listOf(json.optString("icaoId"), json.optString("name")).filter { it.isNotBlank() }.joinToString(" · ")
    val forecasts = json.optJSONArray("fcsts")
    val first = forecasts?.optJSONObject(0)
    val next = forecasts?.let { array ->
        (1 until array.length()).mapNotNull { index -> array.optJSONObject(index) }.firstOrNull()
    }
    val validity = aviationValidityLabel(json.optLong("validTimeFrom", 0), json.optLong("validTimeTo", 0))
    val firstWind = first?.let { aviationWindLabel(it.opt("wdir"), it.optInt("wspd", 0), it.optInt("wgst", 0)) } ?: "Vent non détaillé"
    val visibility = aviationVisibilityLabel(first?.opt("visib")?.toString(), raw)
    val sky = aviationForecastSkyLabel(first, raw)
    val evolution = next?.let { forecast ->
        val change = forecast.optString("fcstChange").takeIf { it.isNotBlank() } ?: "Évolution"
        val from = aviationEpochLabel(forecast.optLong("timeFrom", 0))
        val to = aviationEpochLabel(forecast.optLong("timeTo", 0))
        val wind = aviationWindLabel(forecast.opt("wdir"), forecast.optInt("wspd", 0), forecast.optInt("wgst", 0))
        "$change $from-$to : $wind"
    } ?: "Pas d'évolution significative publiée"
    return buildString {
        appendLine("Station|${station.ifBlank { "TAF" }}")
        appendLine("Validité|$validity")
        appendLine("Vent prévu|$firstWind")
        appendLine("Visibilité|$visibility")
        appendLine("Ciel / météo|$sky")
        append("Évolution|$evolution")
    }
}

private fun decodePlainAviationRaw(kind: String, station: AviationStation, raw: String): String =
    if (kind == "TAF") decodePlainTafRaw(station, raw) else decodePlainMetarRaw(kind, station, raw)

private fun decodePlainMetarRaw(kind: String, station: AviationStation, raw: String): String {
    val tokens = raw.aviationTokens()
    val wind = aviationPlainWindLabel(tokens)
    val visibility = aviationPlainVisibilityLabel(tokens)
    val temp = aviationPlainTemperatureLabel(tokens)
    val weather = aviationWeatherLabel("", raw)
    return buildString {
        appendLine("Station|${station.icao} · ${station.label}")
        appendLine("Vent|$wind")
        appendLine("Visibilité|$visibility")
        appendLine("Température|$temp")
        append("Ciel / météo|${if (weather.isNotBlank()) weather else "$kind brut disponible"}")
    }
}

private fun decodePlainTafRaw(station: AviationStation, raw: String): String {
    val tokens = raw.aviationTokens()
    val validity = tokens.firstOrNull { it.matches(Regex("""^\d{4}/\d{4}$""")) } ?: "Non précisée"
    val wind = aviationPlainWindLabel(tokens)
    val visibility = aviationPlainVisibilityLabel(tokens)
    val sky = aviationWeatherLabel("", raw).ifBlank { "Prévision brute disponible" }
    val evolution = tokens.firstOrNull { token ->
        token.startsWith("FM") || token == "TEMPO" || token == "BECMG" || token.startsWith("PROB")
    } ?: "Pas d'évolution significative extraite"
    return buildString {
        appendLine("Station|${station.icao} · ${station.label}")
        appendLine("Validité|$validity")
        appendLine("Vent prévu|$wind")
        appendLine("Visibilité|$visibility")
        appendLine("Ciel / météo|$sky")
        append("Évolution|$evolution")
    }
}

private fun String.aviationTokens(): List<String> =
    replace("\n", " ")
        .split(Regex("""\s+"""))
        .map { it.trim() }
        .filter { it.isNotBlank() }

private fun aviationPlainWindLabel(tokens: List<String>): String {
    val match = tokens.firstNotNullOfOrNull { token ->
        Regex("""^(VRB|\d{3})(\d{2,3})(G(\d{2,3}))?KT$""").matchEntire(token)
    } ?: return "Vent non détaillé"
    val direction = match.groupValues[1]
    val speedKt = match.groupValues[2].toIntOrNull() ?: 0
    val gustKt = match.groupValues.getOrNull(4)?.toIntOrNull() ?: 0
    return aviationWindLabel(direction, speedKt, gustKt)
}

private fun aviationPlainVisibilityLabel(tokens: List<String>): String =
    when {
        tokens.any { it == "CAVOK" } -> "CAVOK · visibilité 10 km ou plus"
        else -> tokens.firstOrNull { it.matches(Regex("""^\d{4}$""")) }
            ?.toIntOrNull()
            ?.let { meters ->
                val kilometers = meters / 1000.0
                "${kilometers.formatOneDecimal()} km"
            } ?: "Non précisée"
    }

private fun aviationPlainTemperatureLabel(tokens: List<String>): String {
    val match = tokens.firstNotNullOfOrNull { token ->
        Regex("""^(M?\d{2})/(M?\d{2})$""").matchEntire(token)
    } ?: return "Non précisée"
    fun decode(value: String): String = if (value.startsWith("M")) "-${value.drop(1).toIntOrNull() ?: 0}" else value.toIntOrNull()?.toString() ?: value
    return "${decode(match.groupValues[1])}°C / rosée ${decode(match.groupValues[2])}°C"
}

private fun aviationWindLabel(directionValue: Any?, speedKt: Int, gustKt: Int): String {
    val direction = directionValue?.toString()?.takeIf { it.isNotBlank() && it != "null" }?.let { raw ->
        raw.toIntOrNull()?.let { "%03d°".format(Locale.FRANCE, it) } ?: raw
    } ?: "VRB"
    val speed = "$speedKt kt (${(speedKt * 1.852).roundToInt()} km/h)"
    val gusts = if (gustKt > 0) ", rafales $gustKt kt (${(gustKt * 1.852).roundToInt()} km/h)" else ""
    return "$direction $speed$gusts"
}

private fun aviationVisibilityLabel(value: String?, raw: String): String =
    when {
        raw.contains("CAVOK") -> "CAVOK · visibilité 10 km ou plus"
        value.isNullOrBlank() || value == "null" -> "Non précisée"
        value == "6+" -> "6+ · bonne visibilité"
        else -> value
    }

private fun aviationWeatherLabel(value: String, raw: String): String {
    val normalized = value.takeIf { it.isNotBlank() && it != "NSW" } ?: rawWeatherSummary(raw)
    return when (normalized) {
        "CAVOK" -> "CAVOK · pas de nuages bas significatifs"
        "NSW", "" -> "Pas de phénomène significatif"
        else -> normalized
    }
}

private fun aviationForecastSkyLabel(forecast: JSONObject?, raw: String): String {
    val weather = aviationWeatherLabel(forecast?.optString("wxString").orEmpty(), raw)
    val clouds = forecast?.optJSONArray("clouds")?.optJSONObject(0)?.optString("cover").orEmpty()
    val sky = when (clouds) {
        "NSC" -> "pas de nuages significatifs"
        "" -> null
        else -> clouds
    }
    return listOfNotNull(weather, sky).distinct().joinToString(" · ")
}

private fun aviationValidityLabel(fromSeconds: Long, toSeconds: Long): String {
    val from = aviationEpochLabel(fromSeconds)
    val to = aviationEpochLabel(toSeconds)
    return if (from != "--" && to != "--") "$from → $to" else "Non précisée"
}

private fun aviationEpochLabel(seconds: Long): String =
    if (seconds > 0) SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(seconds * 1000L)) else "--"

@Suppress("DEPRECATION")
private fun bestInternetNetworks(context: Context): List<Network> {
    val manager = context.getSystemService(ConnectivityManager::class.java) ?: return emptyList()
    val validated = manager.allNetworks.filter { network ->
        manager.getNetworkCapabilities(network)?.let { capabilities ->
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } == true
    }
    val preferred = validated.sortedByDescending { network ->
        val capabilities = manager.getNetworkCapabilities(network)
        when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> 3
            network == manager.activeNetwork -> 2
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> 1
            else -> 0
        }
    }
    return preferred.distinct()
}

@Suppress("DEPRECATION")
private fun weatherNetworkProblem(context: Context): String? {
    val manager = context.getSystemService(ConnectivityManager::class.java)
        ?: return "Service réseau Android indisponible."
    val hasValidatedInternet = manager.allNetworks.any { network ->
        manager.getNetworkCapabilities(network)?.let { capabilities ->
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } == true
    }
    if (hasValidatedInternet) return null

    val activeCapabilities = manager.activeNetwork?.let(manager::getNetworkCapabilities)
    return if (activeCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
        "Réseau Internet non validé par Android : vérifier Wi-Fi Internet ou partage de connexion."
    } else {
        "Aucun accès Internet détecté : connecter la radiocommande à un réseau Internet."
    }
}

private fun readUrlText(
    context: Context,
    url: String,
    connectTimeoutMillis: Int = 3500,
    readTimeoutMillis: Int = 5000,
): String {
    val targetUrl = URL(url)
    val networks = bestInternetNetworks(context)
    val failures = mutableListOf<String>()
    networks.forEach { network ->
        runCatching {
            return readUrlTextOnConnection(targetUrl, network, connectTimeoutMillis, readTimeoutMillis)
        }.onFailure { error ->
            failures += "${network}: ${error.javaClass.simpleName}"
        }
    }
    return runCatching {
        readUrlTextOnConnection(targetUrl, null, connectTimeoutMillis, readTimeoutMillis)
    }.getOrElse { error ->
        if (failures.isNotEmpty()) {
            Log.w("OrangeWeather", "Weather HTTP fallback failed after ${failures.joinToString()}", error)
        }
        throw error
    }
}

private fun readUrlTextOnConnection(
    targetUrl: URL,
    network: Network?,
    connectTimeoutMillis: Int,
    readTimeoutMillis: Int,
): String {
    val connection = ((network?.openConnection(targetUrl) ?: targetUrl.openConnection()) as HttpURLConnection).apply {
        connectTimeout = connectTimeoutMillis
        readTimeout = readTimeoutMillis
        requestMethod = "GET"
        setRequestProperty("User-Agent", "OrangeDroneCompagnon/${BuildConfig.VERSION_NAME}")
        setRequestProperty("Accept", "application/json,text/plain,*/*")
        setRequestProperty("Connection", "close")
    }
    return try {
        val status = connection.responseCode
        if (status !in 200..299) {
            error("HTTP $status for ${targetUrl.host}")
        }
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun weatherCodeLabel(code: Int): String =
    when (code) {
        0 -> "Ciel dégagé"
        1, 2 -> "Peu nuageux"
        3 -> "Couvert"
        45, 48 -> "Brouillard"
        51, 53, 55, 61, 63, 65 -> "Pluie"
        71, 73, 75, 77 -> "Neige"
        80, 81, 82 -> "Averses"
        95, 96, 99 -> "Orage"
        else -> "Conditions météo"
    }

private fun rawWeatherSummary(raw: String): String =
    when {
        raw.contains("TS") -> "orage possible"
        raw.contains("RA") -> "pluie"
        raw.contains("SN") -> "neige"
        raw.contains("FG") -> "brouillard"
        raw.contains("CAVOK") -> "CAVOK"
        else -> ""
    }

private fun cardinalDirection(degrees: Double): String {
    val directions = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
    val index = (((degrees % 360.0) / 45.0).roundToInt() % directions.size).coerceAtLeast(0)
    return directions[index]
}

private fun timePlusMinutesLabel(value: String, minutes: Int): String {
    val time = Regex("""T(\d{2}):(\d{2})""").find(value) ?: return "--:--"
    val hour = time.groupValues[1].toIntOrNull() ?: return "--:--"
    val minute = time.groupValues[2].toIntOrNull() ?: return "--:--"
    val total = (hour * 60 + minute + minutes).floorMod(24 * 60)
    return "%02d:%02d".format(Locale.FRANCE, total / 60, total % 60)
}

private fun hourLabel(value: String): String =
    Regex("""T(\d{2}):(\d{2})""").find(value)?.let { match ->
        "${match.groupValues[1]}:${match.groupValues[2]}"
    } ?: "--:--"

private fun isInsideAeronauticalNight(timeLabel: String, morningEnd: String, eveningStart: String): Boolean {
    val time = minutesFromHourLabel(timeLabel) ?: return false
    val morning = minutesFromHourLabel(morningEnd) ?: return false
    val evening = minutesFromHourLabel(eveningStart) ?: return false
    return if (evening <= morning) {
        time >= evening && time <= morning
    } else {
        time >= evening || time <= morning
    }
}

private fun minutesFromHourLabel(label: String): Int? {
    val match = Regex("""^(\d{2}):(\d{2})$""").find(label.trim()) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private fun Int.floorMod(modulus: Int): Int =
    ((this % modulus) + modulus) % modulus

private fun Double.formatRounded(): String =
    if (isFinite()) roundToInt().toString() else "--"

private fun Double.formatOneDecimal(): String =
    if (isFinite()) String.format(Locale.FRANCE, "%.1f", this) else "--"

@Composable
private fun FlightWeatherReportsSection(
    report: FlightWeatherReport?,
    onReportClick: (FlightAviationReportSheet) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val metarRaw = report?.metarRaw ?: "En attente"
        val metarDecoded = report?.metarDecoded
            ?: "Station|Station la plus proche\nLecture|Vent, visibilité, nébulosité et phénomènes significatifs."
        val tafRaw = report?.tafRaw ?: "En attente"
        val tafDecoded = report?.tafDecoded
            ?: "Prévision|Créneau de mission\nLecture|Prévision aviation synthétisée en clair."
        val metarSheet = FlightAviationReportSheet("METAR", "Observation", metarRaw, metarDecoded, Color(0xFF8BE7FF))
        val tafSheet = FlightAviationReportSheet("TAF", "Prévision", tafRaw, tafDecoded, Color(0xFFFFB23D))
        if (maxWidth < 720.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlightWeatherTextCard(sheet = metarSheet, onClick = { onReportClick(metarSheet) })
                FlightWeatherTextCard(sheet = tafSheet, onClick = { onReportClick(tafSheet) })
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FlightWeatherTextCard(sheet = metarSheet, onClick = { onReportClick(metarSheet) }, modifier = Modifier.weight(1f))
                FlightWeatherTextCard(sheet = tafSheet, onClick = { onReportClick(tafSheet) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FlightWeatherTextCard(
    sheet: FlightAviationReportSheet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = sheet.decoded.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { line ->
            val parts = line.split("|", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else "Lecture" to line
        }
    GlassCard(
        modifier = modifier
            .heightIn(min = 238.dp)
            .clickable(onClick = onClick),
        containerAlpha = 0.46f,
        borderAlpha = 0.24f,
        highlightAlpha = 0.08f,
        glowColor = sheet.accent.copy(alpha = 0.08f),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(sheet.accent.copy(alpha = 0.13f), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 680f,
                    ),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(sheet.accent),
                    )
                    Text(
                        sheet.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                }
                Text(
                    text = sheet.badge.uppercase(Locale.FRANCE),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(sheet.accent.copy(alpha = 0.16f))
                        .border(1.dp, sheet.accent.copy(alpha = 0.28f), MaterialTheme.shapes.small)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.86f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        Brush.horizontalGradient(
                            listOf(sheet.accent.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.18f)),
                        ),
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.08f), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Message brut", style = MaterialTheme.typography.labelSmall, color = sheet.accent.copy(alpha = 0.86f), fontWeight = FontWeight.Black)
                Text(
                    sheet.raw,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    color = Color.White.copy(alpha = 0.86f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                rows.take(7).forEach { (label, value) ->
                    AviationBriefRow(label = label, value = value, accent = sheet.accent)
                }
            }
            Text(
                text = "Toucher pour lire en détail",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.46f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AviationBriefRow(label: String, value: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), MaterialTheme.shapes.small)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(Locale.FRANCE),
            modifier = Modifier.width(112.dp),
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = 0.94f),
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = Color.White.copy(alpha = 0.84f),
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightMetricDetailDialog(
    metric: FlightConditionMetric,
    profile: DroneWeatherProfile,
    report: FlightWeatherReport?,
    onDismiss: () -> Unit,
) {
    val source = when (metric.label) {
        "Vent", "Rafales", "Température", "Précipitations", "Nébulosité", "Nuit aéronautique" -> "Open-Meteo"
        "Visibilité" -> "Open-Meteo + lecture aviation la plus proche"
        else -> "Météo"
    }
    val threshold = when (metric.label) {
        "Vent" -> "Seuil ${profile.label} : ${profile.windLimitKmh} km/h"
        "Rafales" -> "Seuil ${profile.label} : ${profile.gustLimitKmh} km/h"
        "Précipitations" -> "Alerte si pluie significative au moment du vol"
        "Visibilité" -> "Confort de lecture terrain : viser 5 km ou plus"
        "Nuit aéronautique" -> "Interdiction stricte entre coucher +30 min et lever -30 min"
        else -> "Information terrain sans seuil bloquant"
    }
    OrangeGlassDialog(
        title = metric.label,
        accent = metric.accent,
        onDismiss = onDismiss,
    ) {
        DialogHeroValue(metric = metric)
        DialogInfoLine("Lecture", metric.detail.ifBlank { "Donnée disponible dans la tuile." }, metric.accent)
        DialogInfoLine("Seuil", threshold, metric.accent)
        DialogInfoLine("Source", source, metric.accent)
        DialogInfoLine("Lieu", report?.location?.label ?: "En attente météo", metric.accent)
        DialogInfoLine("Station aviation", report?.station?.let { "${it.icao} · ${it.label}" } ?: "En attente", metric.accent)
    }
}

@Composable
private fun FlightForecastDetailDialog(
    forecast: FlightWeatherForecast,
    onDismiss: () -> Unit,
) {
    OrangeGlassDialog(
        title = "Prévision ${forecast.timeLabel}",
        accent = forecast.accent,
        onDismiss = onDismiss,
    ) {
        Text(
            text = forecast.flightLabel,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(forecast.accent.copy(alpha = 0.16f))
                .border(1.dp, forecast.accent.copy(alpha = 0.34f), MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        DialogInfoLine("Température", forecast.temperature, forecast.accent)
        DialogInfoLine("Condition", forecast.condition, forecast.accent)
        DialogInfoLine("Vent", forecast.wind, forecast.accent)
        DialogInfoLine("Rafales", forecast.gusts.removePrefix("Raf. "), forecast.accent)
        DialogInfoLine("Risque pluie", forecast.rainRisk, forecast.accent)
    }
}

@Composable
private fun FlightAviationReportDialog(
    sheet: FlightAviationReportSheet,
    onDismiss: () -> Unit,
) {
    val rows = sheet.decoded.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { line ->
            val parts = line.split("|", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else "Lecture" to line
        }
    OrangeGlassDialog(
        title = "${sheet.title} · ${sheet.badge}",
        accent = sheet.accent,
        onDismiss = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(Color.Black.copy(alpha = 0.28f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), MaterialTheme.shapes.small)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Message brut",
                style = MaterialTheme.typography.labelSmall,
                color = sheet.accent,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = sheet.raw,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = Color.White.copy(alpha = 0.88f),
                fontWeight = FontWeight.Bold,
            )
        }
        rows.forEach { (label, value) ->
            DialogInfoLine(label, value, sheet.accent)
        }
    }
}

@Composable
private fun OrangeGlassDialog(
    title: String,
    accent: Color,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", color = accent, fontWeight = FontWeight.Black)
            }
        },
        title = {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        },
        containerColor = Color(0xFF071014).copy(alpha = 0.98f),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        shape = RoundedCornerShape(18.dp),
    )
}

@Composable
private fun DialogHeroValue(metric: FlightConditionMetric) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (metric.label == "Nuit aéronautique") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogInfoLine("Lever", metric.value, metric.accent)
                DialogInfoLine("Coucher", metric.unit, metric.accent)
            }
        } else {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 56.sp),
                color = metric.accent,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
            if (metric.unit.isNotBlank()) {
                Text(
                    text = metric.unit,
                    modifier = Modifier.padding(bottom = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.70f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun DialogInfoLine(label: String, value: String, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label.uppercase(Locale.FRANCE),
            style = MaterialTheme.typography.labelSmall,
            color = accent.copy(alpha = 0.92f),
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.84f),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                containerAlpha = 0.10f,
                borderAlpha = 0.16f,
                highlightAlpha = 0.10f,
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.66f), maxLines = 1)
        Text(value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1)
    }
}

@Composable
private fun SecondaryFieldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(48.dp)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                containerAlpha = if (enabled) 0.13f else 0.06f,
                borderAlpha = if (enabled) 0.22f else 0.12f,
                highlightAlpha = 0.14f,
            ),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.34f),
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private data class InfoDocument(
    val title: String,
    val description: String,
    val badge: String,
    val url: String,
)

private val OfficialInfoDocuments = emptyList<InfoDocument>()

@Composable
private fun InfoContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    onRetryDjiSdk: () -> Unit = {},
    initialPanel: InfoStartPanel = InfoStartPanel.Documents,
    onInitialPanelConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedDocument by remember { mutableStateOf(OfficialInfoDocuments.firstOrNull()) }
    var adminSelected by remember { mutableStateOf(false) }
    var testSelected by remember { mutableStateOf(false) }

    LaunchedEffect(initialPanel) {
        when (initialPanel) {
            InfoStartPanel.Documents -> Unit
            InfoStartPanel.FieldTest -> {
                adminSelected = false
                testSelected = true
                onInitialPanelConsumed()
            }
            InfoStartPanel.AdminLogs -> {
                adminSelected = true
                testSelected = false
                onInitialPanelConsumed()
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 760.dp) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ModuleHero(
                        module = AppModule.INFO,
                        status = when {
                            adminSelected -> "Diagnostic"
                            testSelected -> "Pré-test"
                            else -> "Consignes"
                        },
                        description = "Consignes officielles non disponibles pour le moment. Les outils de diagnostic restent accessibles.",
                        metrics = listOf(
                            ModuleHeroMetric("Consignes", OfficialInfoDocuments.size.toString(), Color(0xFF9AA8FF)),
                            ModuleHeroMetric("Logs", logs.size.toString(), Orange),
                            ModuleHeroMetric(
                                "Drone",
                                if (djiSdkState.hasConnectedDrone()) "Connecté" else "Non connecté",
                                if (djiSdkState.hasConnectedDrone()) Color(0xFF33D16D) else Color.White.copy(alpha = 0.80f),
                            ),
                        ),
                    )
                }
                item {
                    InfoDocumentsMenu(
                        selectedDocument = selectedDocument,
                        adminSelected = adminSelected,
                        testSelected = testSelected,
                        onSelectDocument = {
                            selectedDocument = it
                            adminSelected = false
                            testSelected = false
                        },
                        onOpenAdmin = {
                            adminSelected = true
                            testSelected = false
                        },
                        onOpenTest = {
                            adminSelected = false
                            testSelected = true
                        },
                    )
                }
                item {
                    if (adminSelected) {
                        AdminLogsPanel(
                            settings = settings,
                            logs = logs,
                            events = events,
                            djiSdkState = djiSdkState,
                            onRetryDjiSdk = onRetryDjiSdk,
                        )
                    } else if (testSelected) {
                        FieldTestGuidePanel(
                            settings = settings,
                            logs = logs,
                            djiSdkState = djiSdkState,
                        )
                    } else {
                        InfoEmptyConsignesPanel()
                    }
                }
                item {
                    CopyrightFooter()
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.44f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ModuleHero(
                        module = AppModule.INFO,
                        status = when {
                            adminSelected -> "Diagnostic"
                            testSelected -> "Pré-test"
                            else -> "Consignes"
                        },
                        description = "Consignes officielles non disponibles pour le moment. Diagnostic et préparation terrain restent accessibles.",
                        metrics = listOf(
                            ModuleHeroMetric("Consignes", OfficialInfoDocuments.size.toString(), Color(0xFF9AA8FF)),
                            ModuleHeroMetric("Logs", logs.size.toString(), Orange),
                            ModuleHeroMetric(
                                "Drone",
                                if (djiSdkState.hasConnectedDrone()) "Connecté" else "Non connecté",
                                if (djiSdkState.hasConnectedDrone()) Color(0xFF33D16D) else Color.White.copy(alpha = 0.80f),
                            ),
                        ),
                    )
                    InfoDocumentsMenu(
                        selectedDocument = selectedDocument,
                        adminSelected = adminSelected,
                        testSelected = testSelected,
                        onSelectDocument = {
                            selectedDocument = it
                            adminSelected = false
                            testSelected = false
                        },
                        onOpenAdmin = {
                            adminSelected = true
                            testSelected = false
                        },
                        onOpenTest = {
                            adminSelected = false
                            testSelected = true
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (adminSelected) {
                    AdminLogsPanel(
                        settings = settings,
                        logs = logs,
                        events = events,
                        djiSdkState = djiSdkState,
                        onRetryDjiSdk = onRetryDjiSdk,
                        modifier = Modifier.weight(0.56f),
                    )
                } else if (testSelected) {
                    FieldTestGuidePanel(
                        settings = settings,
                        logs = logs,
                        djiSdkState = djiSdkState,
                        modifier = Modifier.weight(0.56f),
                    )
                } else {
                    InfoEmptyConsignesPanel(
                        modifier = Modifier.weight(0.56f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminLogsPanel(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    onRetryDjiSdk: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var diagnosticReport by remember { mutableStateOf("") }
    var diagnosticMessage by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AdminLogFilter.All) }
    val filteredEvents = remember(events, filter) {
        events.filter { event ->
            val message = event.message.lowercase(Locale.FRANCE)
            when (filter) {
                AdminLogFilter.All -> true
                AdminLogFilter.Errors -> event.level.equals("Erreur", ignoreCase = true) || "erreur" in message || "impossible" in message
                AdminLogFilter.Usb -> "usb" in message || "clé" in message
                AdminLogFilter.Mail -> "mail" in message || "smtp" in message || "envoi" in message
                AdminLogFilter.Dji -> "dji" in message || "drone" in message || "sdk" in message
            }
        }
    }
    val filteredLogs = remember(logs, filter) {
        logs.filter { log ->
            when (filter) {
                AdminLogFilter.All -> true
                AdminLogFilter.Errors -> log.status == LogStatus.ERROR || !log.lastError.isNullOrBlank()
                AdminLogFilter.Usb -> false
                AdminLogFilter.Mail -> false
                AdminLogFilter.Dji -> log.decodeError?.contains("dji", ignoreCase = true) == true ||
                    log.decodeStatus?.contains("dji", ignoreCase = true) == true
            }
        }
    }
    val diagnosticExporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) {
            val exported = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(diagnosticReport.toByteArray(Charsets.UTF_8))
                } ?: error("Sortie de fichier indisponible")
            }
            diagnosticMessage = if (exported.isSuccess) {
                "Diagnostic exporté."
            } else {
                "Export diagnostic impossible : ${exported.exceptionOrNull()?.message ?: "erreur inconnue"}"
            }
        }
    }
    GlassCard(modifier = modifier, containerAlpha = 0.58f, borderAlpha = 0.34f, highlightAlpha = 0.12f) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = "Diagnostic complet",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
            }
            item {
                Text(
                    text = "Vue support complète : événements applicatifs, erreurs et fichiers de vol détectés.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.76f),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OrangeButton(
                        onClick = {
                            diagnosticReport = buildDiagnosticReport(
                                context = context,
                                settings = settings,
                                logs = logs,
                                events = events,
                                djiSdkState = djiSdkState,
                            )
                            val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                            diagnosticExporter.launch("orange-drone-compagnon-diagnostic-$stamp.txt")
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Exporter diagnostic .txt")
                    }
                    SecondaryFieldButton(
                        text = "Relancer SDK DJI",
                        onClick = onRetryDjiSdk,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (diagnosticMessage.isNotBlank()) {
                    Text(
                        text = diagnosticMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MiniMetric("Événements", events.size.toString(), Modifier.weight(1f))
                    MiniMetric("Logs vol", logs.size.toString(), Modifier.weight(1f))
                    MiniMetric("Erreurs", logs.count { it.status == LogStatus.ERROR }.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AdminLogFilter.entries.forEach { option ->
                        ToggleChip(
                            label = option.label,
                            selected = filter == option,
                            onClick = { filter = option },
                        )
                    }
                }
            }
            item {
                Text(
                    text = "Événements",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (filteredEvents.isEmpty()) {
                item {
                    AdminEmptyLine("Aucun événement pour ce filtre.")
                }
            } else {
                items(filteredEvents, key = { it.id }) { event ->
                    ActivityRow(event)
                }
            }
            item {
                HistoryTitle()
            }
            if (filteredLogs.isEmpty()) {
                item {
                    AdminEmptyLine("Aucun log de vol pour ce filtre.")
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    LogRow(log)
                }
            }
        }
    }
}

@Composable
private fun AdminEmptyLine(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.07f))
            .padding(14.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.72f),
    )
}

@Composable
private fun FieldTestGuidePanel(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) {
        usbDestinationState(context, settings.usbExportUri)
    }
    val logFolderState = remember(settings.folderUri, logs.size) {
        logFolderReadiness(context, settings.folderUri)
    }
    val latestLog = remember(logs) {
        logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    }

    GlassCard(modifier = modifier, containerAlpha = 0.58f, borderAlpha = 0.34f, highlightAlpha = 0.12f) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Test terrain",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "À dérouler avant le test réel sur RC : chaque ligne doit être verte ou comprise avant de partir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.74f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "Dossier logs",
                    value = when (logFolderState) {
                        FolderReadiness.Ready -> "OK"
                        FolderReadiness.Missing -> "Automatique"
                        FolderReadiness.Unavailable -> "À vérifier"
                    },
                    detail = settings.folderLabel.ifBlank { "Emplacement DJI automatique" },
                    ok = logFolderState == FolderReadiness.Ready,
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "Dernier log",
                    value = latestLog?.flightStartTimeMillis?.let(::formatDate)
                        ?: latestLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatDate)
                        ?: "Aucun",
                    detail = latestLog?.fileName ?: "Lancer une synchronisation pour scanner l'emplacement DJI.",
                    ok = latestLog != null,
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "Export des logs",
                    value = usbState.shortLabel,
                    detail = usbState.destinationLabel,
                    ok = usbState == UsbDestinationState.Ready,
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "Drone DJI",
                    value = if (djiSdkState.hasConnectedDrone()) "Connecté" else "Non connecté",
                    detail = djiSdkState.lastError ?: djiSdkState.message,
                    ok = djiSdkState.hasConnectedDrone(),
                )
            }
            item {
                FieldTestStep("1", "Ouvrir Réglages", "Vérifier le dossier logs et l'état USB.")
                FieldTestStep("2", "Ouvrir MSurvey", "Déposer le log via le formulaire MSurvey Orange.")
                FieldTestStep("3", "Ouvrir Export des logs", "Brancher la clé, sélectionner le dernier log, puis exporter.")
                FieldTestStep("4", "Si problème", "Exporter le diagnostic depuis Admin logs et prendre une photo de l'écran l’application de vol DJI.")
            }
        }
    }
}

@Composable
private fun ReadinessSummaryRow(
    title: String,
    value: String,
    detail: String,
    ok: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.07f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (ok) Color(0xFF33D16D).copy(alpha = 0.22f) else Color(0xFFFFB020).copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (ok) "OK" else "!",
                color = if (ok) Color(0xFF33D16D) else Color(0xFFFFB020),
                fontWeight = FontWeight.Black,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(detail, color = Color.White.copy(alpha = 0.66f), style = MaterialTheme.typography.bodySmall)
        }
        Text(value, color = Color.White, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun FieldTestStep(
    index: String,
    title: String,
    detail: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Orange.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(index, color = Orange, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(detail, color = Color.White.copy(alpha = 0.70f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun InfoDocumentsMenu(
    selectedDocument: InfoDocument?,
    adminSelected: Boolean,
    testSelected: Boolean,
    onSelectDocument: (InfoDocument) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, containerAlpha = 0.58f, borderAlpha = 0.34f, highlightAlpha = 0.12f) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Consignes internes",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Aucune consigne officielle n’est embarquée pour le moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.74f),
            )
            if (OfficialInfoDocuments.isEmpty()) {
                InfoDocumentRow(
                    document = InfoDocument(
                        title = "Aucune consigne disponible",
                        description = "Les consignes seront ajoutées uniquement après validation officielle.",
                        badge = "0",
                        url = "",
                    ),
                    selected = !adminSelected && !testSelected,
                    onClick = {},
                    enabled = false,
                )
            } else {
                OfficialInfoDocuments.forEach { document ->
                    InfoDocumentRow(
                        document = document,
                        selected = !adminSelected && !testSelected && document == selectedDocument,
                        onClick = { onSelectDocument(document) },
                    )
                }
            }
            InfoDocumentRow(
                document = InfoDocument(
                    title = "Test terrain",
                    description = "Parcours rapide avant mission : logs, e-mail, USB, drone et médias.",
                    badge = "TEST",
                    url = "",
                ),
                selected = testSelected,
                onClick = onOpenTest,
            )
            InfoDocumentRow(
                document = InfoDocument(
                    title = "Admin logs",
                    description = "Diagnostic support : événements internes, erreurs et historique complet des logs.",
                    badge = "ADM",
                    url = "",
                ),
                selected = adminSelected,
                onClick = onOpenAdmin,
            )
        }
    }
}

@Composable
private fun InfoDocumentRow(
    document: InfoDocument,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (selected && enabled) Orange.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.07f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(38.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (selected && enabled) Orange else Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = document.badge,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.58f),
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.70f),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = document.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = if (!enabled) Color.Transparent else if (selected) Orange else Color.White.copy(alpha = 0.52f),
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun InfoEmptyConsignesPanel(
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, containerAlpha = 0.58f, borderAlpha = 0.34f, highlightAlpha = 0.12f) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Aucune consigne officielle",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Cette section est volontairement vide pour éviter d’afficher des informations non validées.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
            )
            Text(
                text = "Les consignes internes seront ajoutées uniquement après validation officielle Orange.",
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)), MaterialTheme.shapes.small)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun InfoQrPanel(
    document: InfoDocument,
    modifier: Modifier = Modifier,
) {
    val qrBitmap = remember(document.url) { createQrBitmap(document.url) }

    GlassCard(modifier = modifier, containerAlpha = 0.58f, borderAlpha = 0.34f, highlightAlpha = 0.12f) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = document.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f),
            )
            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR code ${document.title}",
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.White)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.White.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "QR indisponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Text(
                text = "VPN Orange requis",
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(Orange.copy(alpha = 0.18f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = document.url,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AboutInfoContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    onOpenFullDiagnostic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) {
        usbDestinationState(context, settings.usbExportUri)
    }
    val logFolderState = remember(settings.folderUri, logs.size) {
        logFolderReadiness(context, settings.folderUri)
    }
    val latestLog = remember(logs) {
        logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    }
    val latestEvents = remember(events) {
        events.sortedByDescending { it.createdAtMillis }.take(4)
    }
    val logFolderLabel = when (logFolderState) {
        FolderReadiness.Ready -> "OK"
        FolderReadiness.Missing -> "À choisir"
        FolderReadiness.Unavailable -> "À réautoriser"
    }
    val logFolderAccent = when (logFolderState) {
        FolderReadiness.Ready -> OdsColor.Success
        FolderReadiness.Missing -> OdsColor.Warning
        FolderReadiness.Unavailable -> OdsColor.Error
    }
    val usbAccent = when (usbState) {
        UsbDestinationState.Ready -> OdsColor.Success
        UsbDestinationState.MountedNotAuthorized -> OdsColor.Warning
        UsbDestinationState.NotConfigured,
        UsbDestinationState.Unavailable -> OdsColor.Error
    }
    val droneConnected = djiSdkState.hasConnectedDrone()
    val droneAccent = if (droneConnected) OdsColor.Success else OdsColor.Error
    BoxWithConstraints(modifier = modifier) {
        val wideLandscape = maxWidth >= 900.dp && maxWidth > maxHeight
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (wideLandscape) 18.dp else 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AboutIntroCard()
            }
            item {
                if (wideLandscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Column(
                            modifier = Modifier.weight(0.46f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            AboutDiagnosticSection(
                                logFolderLabel = logFolderLabel,
                                logFolderDetail = settings.folderLabel.ifBlank { "Non configuré" },
                                logFolderAccent = logFolderAccent,
                                latestLog = latestLog,
                                usbLabel = usbState.destinationLabel,
                                usbDetail = settings.usbExportLabel.ifBlank { "Dossier USB non autorisé" },
                                usbAccent = usbAccent,
                                droneLabel = if (droneConnected) "Connecté" else "Non connecté",
                                droneDetail = djiSdkState.productId?.let { "DJI #$it" } ?: djiSdkState.message,
                                droneAccent = droneAccent,
                                squareTiles = true,
                            )
                            AboutInternalLogsSection(
                                latestEvents = latestEvents,
                                onOpenFullDiagnostic = onOpenFullDiagnostic,
                                maxEvents = 2,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(0.54f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            AboutSourcesSection()
                            CopyrightPanel()
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AboutDiagnosticSection(
                            logFolderLabel = logFolderLabel,
                            logFolderDetail = settings.folderLabel.ifBlank { "Non configuré" },
                            logFolderAccent = logFolderAccent,
                            latestLog = latestLog,
                            usbLabel = usbState.destinationLabel,
                            usbDetail = settings.usbExportLabel.ifBlank { "Dossier USB non autorisé" },
                            usbAccent = usbAccent,
                            droneLabel = if (droneConnected) "Connecté" else "Non connecté",
                            droneDetail = djiSdkState.productId?.let { "DJI #$it" } ?: djiSdkState.message,
                            droneAccent = droneAccent,
                            squareTiles = false,
                        )
                        AboutSourcesSection()
                        AboutInternalLogsSection(
                            latestEvents = latestEvents,
                            onOpenFullDiagnostic = onOpenFullDiagnostic,
                        )
                        CopyrightPanel()
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutIntroCard() {
    GlassCard(containerAlpha = 0.48f, borderAlpha = 0.34f, highlightAlpha = 0.14f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .glassControlSurface(
                            shape = RoundedCornerShape(22.dp),
                            active = true,
                            accent = OdsColor.Info,
                            containerAlpha = 0.14f,
                            borderAlpha = 0.30f,
                            highlightAlpha = 0.16f,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "i",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 38.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "Orange Drone Compagnon",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Diagnostic, sources et informations support de l’application terrain.",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AboutVersionBadge()
            }
            Text(
                text = "Objectif : simplifier la fin de mission, sécuriser les traces de vol et limiter les manipulations techniques pour les télépilotes.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "© Loïc Arnold · loic.arnold@orange.com",
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(OdsColor.Orange.copy(alpha = 0.16f))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AboutVersionBadge() {
    Column(
        modifier = Modifier
            .glassControlSurface(
                shape = RoundedCornerShape(18.dp),
                active = true,
                accent = OdsColor.Orange,
                containerAlpha = 0.13f,
                borderAlpha = 0.28f,
                highlightAlpha = 0.14f,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = "Version",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.66f),
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun AboutDiagnosticSection(
    logFolderLabel: String,
    logFolderDetail: String,
    logFolderAccent: Color,
    latestLog: LogFileEntity?,
    usbLabel: String,
    usbDetail: String,
    usbAccent: Color,
    droneLabel: String,
    droneDetail: String,
    droneAccent: Color,
    squareTiles: Boolean,
) {
    InfoSectionCard(title = "Diagnostic") {
        if (squareTiles) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoStatusTile(
                    title = "Logs",
                    value = logFolderLabel,
                    detail = logFolderDetail,
                    accent = logFolderAccent,
                    square = true,
                    modifier = Modifier.weight(1f),
                )
                InfoStatusTile(
                    title = "Vol",
                    value = latestLog?.flightStartTimeMillis?.let(::formatShortDate) ?: "Aucun",
                    detail = latestLog?.flightDurationSeconds?.let(::formatDuration) ?: latestLog?.fileName ?: "Aucun log",
                    accent = if (latestLog != null) OdsColor.Success else OdsColor.Warning,
                    square = true,
                    modifier = Modifier.weight(1f),
                )
                InfoStatusTile(
                    title = "USB",
                    value = usbLabel.removePrefix("Clé USB "),
                    detail = usbDetail,
                    accent = usbAccent,
                    square = true,
                    modifier = Modifier.weight(1f),
                )
                InfoStatusTile(
                    title = "Drone",
                    value = droneLabel,
                    detail = droneDetail,
                    accent = droneAccent,
                    square = true,
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoStatusTile(
                    title = "Dossier logs",
                    value = logFolderLabel,
                    detail = logFolderDetail,
                    accent = logFolderAccent,
                    modifier = Modifier.weight(1f),
                )
                InfoStatusTile(
                    title = "Dernier log",
                    value = latestLog?.flightStartTimeMillis?.let(::formatShortDate) ?: "Aucun",
                    detail = latestLog?.flightDurationSeconds?.let(::formatDuration) ?: latestLog?.fileName ?: "Aucun log détecté",
                    accent = if (latestLog != null) OdsColor.Success else OdsColor.Warning,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoStatusTile(
                    title = "USB",
                    value = usbLabel,
                    detail = usbDetail,
                    accent = usbAccent,
                    modifier = Modifier.weight(1f),
                )
                InfoStatusTile(
                    title = "Drone",
                    value = droneLabel,
                    detail = droneDetail,
                    accent = droneAccent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AboutSourcesSection() {
    InfoSectionCard(title = "Version, sources et API") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoStatusTile(
                title = "Version",
                value = BuildConfig.VERSION_NAME,
                detail = "Build ${BuildConfig.VERSION_CODE}",
                accent = OdsColor.Orange,
                modifier = Modifier.weight(1f),
            )
            InfoStatusTile(
                title = "Application",
                value = "ODC",
                detail = "Orange Drone Compagnon",
                accent = OdsColor.Info,
                modifier = Modifier.weight(1f),
            )
            InfoStatusTile(
                title = "Publication",
                value = "APK",
                detail = "GitHub Release",
                accent = OdsColor.Success,
                modifier = Modifier.weight(1f),
            )
        }
        InfoSourceGrid(
            sources = listOf(
                InfoSourceItem("DJI SDK V5", "Connexion drone et médias", OdsColor.Info),
                InfoSourceItem("FlightRecord", "Lecture locale .txt", OdsColor.Orange),
                InfoSourceItem("MSurvey", "Dépôt sécurisé Orange", OdsColor.Success),
                InfoSourceItem("Open-Meteo", "Météo terrain", Color(0xFF50BE87)),
                InfoSourceItem("AviationWeather", "METAR / TAF", Color(0xFFFFD200)),
                InfoSourceItem("Sites Orange", "CSV vers KML DJI", Color(0xFFA885D8)),
            ),
        )
    }
}

@Composable
private fun AboutInternalLogsSection(
    latestEvents: List<ActivityEventEntity>,
    onOpenFullDiagnostic: () -> Unit,
    maxEvents: Int = 3,
) {
    InfoSectionCard(title = "Logs internes") {
        if (latestEvents.isEmpty()) {
            Text(
                text = "Aucun événement interne enregistré.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f),
            )
        } else {
            latestEvents.take(maxEvents).forEach { event ->
                CompactActivityLine(event)
            }
        }
        OrangeButton(
            onClick = onOpenFullDiagnostic,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Ouvrir le diagnostic complet")
        }
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(containerAlpha = 0.42f, borderAlpha = 0.28f, highlightAlpha = 0.12f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            content()
        }
    }
}

@Composable
private fun InfoStatusTile(
    title: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier,
    square: Boolean = false,
) {
    Column(
        modifier = modifier
            .then(if (square) Modifier.aspectRatio(1f) else Modifier.heightIn(min = 94.dp))
            .glassControlSurface(
                shape = RoundedCornerShape(20.dp),
                active = true,
                accent = accent,
                containerAlpha = 0.12f,
                borderAlpha = 0.20f,
                highlightAlpha = 0.12f,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(accent),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = if (square) 19.sp else 20.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = if (square) 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.64f),
            maxLines = if (square) 3 else 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class InfoSourceItem(
    val title: String,
    val detail: String,
    val accent: Color,
)

@Composable
private fun InfoSourceGrid(
    sources: List<InfoSourceItem>,
) {
    sources.chunked(3).forEach { rowSources ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            rowSources.forEach { source ->
                InfoSourceTile(
                    source = source,
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(3 - rowSources.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoSourceTile(
    source: InfoSourceItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(88.dp)
            .glassControlSurface(
                shape = RoundedCornerShape(18.dp),
                active = true,
                accent = source.accent,
                containerAlpha = 0.10f,
                borderAlpha = 0.16f,
                highlightAlpha = 0.10f,
            )
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(source.accent),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = source.title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = source.detail,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.66f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun InfoSourceLine(title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(OdsColor.Orange),
        )
        Text(
            text = title,
            modifier = Modifier.weight(0.44f),
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = body,
            modifier = Modifier.weight(0.56f),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.72f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CompactActivityLine(event: ActivityEventEntity) {
    val accent = when {
        event.level.equals("Erreur", ignoreCase = true) -> OdsColor.Error
        event.level.equals("Alerte", ignoreCase = true) -> OdsColor.Warning
        event.level.equals("Succes", ignoreCase = true) -> OdsColor.Success
        else -> OdsColor.Info
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "${event.level} · ${formatDate(event.createdAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CopyrightPanel() {
    GlassCard(containerAlpha = 0.36f, borderAlpha = 0.24f, highlightAlpha = 0.10f) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "© Loïc Arnold",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "loic.arnold@orange.com",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.68f),
                )
            }
            Text(
                text = "Orange Drone Compagnon",
                style = MaterialTheme.typography.bodySmall,
                color = OdsColor.Orange,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ComingSoonToolContent(
    module: AppModule,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(containerAlpha = 0.56f) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Text(
                        text = "Module prévu dans Orange Drone Compagnon.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.66f),
                    )
                }
            }
        }
        item {
            CopyrightFooter()
        }
    }
}

@Composable
private fun PinPointContent(
    onExportKml: (PinPointExportRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dataset by produceState<PinPointDataset?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            loadPinPointDataset(context)
        }
    }
    var selectedDepartments by remember { mutableStateOf(emptySet<String>()) }
    var query by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(PinPointSearchMode.Department) }
    var positionSource by remember { mutableStateOf(PinPointPositionSource.RcGps) }
    var communeQuery by remember { mutableStateOf("") }
    var nearbySelection by remember { mutableStateOf<PinPointNearbySelection?>(null) }
    var nearbyMessage by remember { mutableStateOf("") }
    @SuppressLint("ProduceStateDoesNotAssignValue")
    val positionCommunes by produceState<List<PinPointCommune>>(initialValue = emptyList(), searchMode) {
        value = if (searchMode == PinPointSearchMode.Position) {
            withContext(Dispatchers.IO) { loadPinPointCommunes(context) }
        } else {
            emptyList()
        }
    }
    if (dataset == null) {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                GlassCard(containerAlpha = 0.56f) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Point d’intérêt",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Chargement de la carte de France et du référentiel sites Orange...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f),
                        )
                    }
                }
            }
        }
        return
    }
    val pinPointDataset = dataset ?: return
    val departments = pinPointDataset.departments
    val communes = positionCommunes
    val allSites = pinPointDataset.sites
    val siteCounts = pinPointDataset.siteCountsByDepartment
    val filteredDepartments = remember(query, departments) {
        val normalized = normalizeSearchText(query)
        if (normalized.isBlank()) {
            departments
        } else {
            departments.filter {
                normalizeSearchText(it.code).contains(normalized) ||
                    normalizeSearchText(it.name).contains(normalized) ||
                    normalizeSearchText(it.region).contains(normalized)
            }
        }
    }
    val selectedSites = remember(selectedDepartments, allSites, nearbySelection) {
        val departmentSites = allSites.filter { it.departmentCode in selectedDepartments }
        (departmentSites + nearbySelection?.sites.orEmpty()).distinctBy { it.uniqueKey }
    }
    val mapDepartments = remember(selectedDepartments, selectedSites) {
        selectedDepartments + selectedSites.map { it.departmentCode }
    }
    val selectedDepartmentLabels = remember(selectedDepartments, departments) {
        departments
            .filter { it.code in selectedDepartments }
            .sortedBy { it.code }
            .joinToString(", ") { it.code }
            .ifBlank { "Aucun département" }
    }
    val filteredCommunes = remember(communeQuery, communes) {
        val normalized = normalizeSearchText(communeQuery)
        if (normalized.length < 2) {
            emptyList()
        } else {
            communes.filter { commune ->
                normalizeSearchText(commune.name).contains(normalized) ||
                    commune.code.contains(normalized) ||
                    commune.postalCodes.any { it.contains(normalized) }
            }.take(8)
        }
    }
    fun addNearbySites(label: String, latitude: Double, longitude: Double) {
        val nearbySites = allSites.filter { site ->
            distanceMetersBetween(latitude, longitude, site.latitude, site.longitude) <= PINPOINT_NEARBY_RADIUS_KM * 1000.0
        }
        nearbySelection = PinPointNearbySelection(
            label = label,
            latitude = latitude,
            longitude = longitude,
            radiusKm = PINPOINT_NEARBY_RADIUS_KM,
            sites = nearbySites,
        )
        nearbyMessage = if (nearbySites.isEmpty()) {
            "Aucun site Orange trouvé à ${PINPOINT_NEARBY_RADIUS_KM.toInt()} km autour de $label."
        } else {
            "${nearbySites.size} site(s) ajouté(s) à ${PINPOINT_NEARBY_RADIUS_KM.toInt()} km autour de $label."
        }
    }
    fun addNearbyFromGps() {
        nearbyMessage = "Recherche de la position RC..."
        coroutineScope.launch {
            val location = findCurrentOrLastKnownLocation(context)
            if (location == null) {
                nearbyMessage = "Position RC indisponible. Vérifiez que la localisation est autorisée et active."
                return@launch
            }
            addNearbySites("la position RC", location.latitude, location.longitude)
        }
    }
    fun selectCurrentDepartmentFromGps() {
        nearbyMessage = "Recherche du département de la RC..."
        coroutineScope.launch {
            val location = findCurrentOrLastKnownLocation(context)
            if (location == null) {
                nearbyMessage = "Position RC indisponible. Impossible d’identifier le département."
                return@launch
            }
            val department = departments.firstOrNull { it.contains(location.latitude, location.longitude) }
            if (department == null) {
                nearbyMessage = "Position RC hors référentiel France métropolitaine."
                return@launch
            }
            selectedDepartments = setOf(department.code)
            query = ""
            nearbyMessage = "Département RC sélectionné : ${department.code} - ${department.name}."
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val cockpit = maxWidth > maxHeight && maxWidth >= 900.dp
        if (cockpit) {
            PinPointCockpitLayout(
                dataset = pinPointDataset,
                filteredDepartments = filteredDepartments,
                siteCounts = siteCounts,
                selectedDepartments = selectedDepartments,
                mapDepartments = mapDepartments,
                selectedSites = selectedSites,
                selectedDepartmentLabels = selectedDepartmentLabels,
                nearbySelection = nearbySelection,
                nearbyMessage = nearbyMessage,
                searchMode = searchMode,
                positionSource = positionSource,
                communeQuery = communeQuery,
                filteredCommunes = filteredCommunes,
                query = query,
                onQueryChange = { query = it },
                onSearchModeChange = { searchMode = it },
                onPositionSourceChange = { positionSource = it },
                onCommuneQueryChange = { communeQuery = it },
                onSelectDepartments = { selectedDepartments = it },
                onSelectCurrentDepartment = ::selectCurrentDepartmentFromGps,
                onToggleDepartment = { selectedDepartments = selectedDepartments.toggle(it) },
                onAddNearbyFromGps = ::addNearbyFromGps,
                onAddNearbyFromCommune = { commune ->
                    communeQuery = commune.displayName
                    addNearbySites(commune.displayName, commune.latitude, commune.longitude)
                },
                onClearNearbySelection = {
                    nearbySelection = null
                    nearbyMessage = ""
                },
                onExportKml = onExportKml,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            PinPointScrollableLayout(
                dataset = pinPointDataset,
                filteredDepartments = filteredDepartments,
                siteCounts = siteCounts,
                selectedDepartments = selectedDepartments,
                mapDepartments = mapDepartments,
                selectedSites = selectedSites,
                selectedDepartmentLabels = selectedDepartmentLabels,
                nearbySelection = nearbySelection,
                nearbyMessage = nearbyMessage,
                searchMode = searchMode,
                positionSource = positionSource,
                communeQuery = communeQuery,
                filteredCommunes = filteredCommunes,
                query = query,
                onQueryChange = { query = it },
                onSearchModeChange = { searchMode = it },
                onPositionSourceChange = { positionSource = it },
                onCommuneQueryChange = { communeQuery = it },
                onSelectDepartments = { selectedDepartments = it },
                onSelectCurrentDepartment = ::selectCurrentDepartmentFromGps,
                onToggleDepartment = { selectedDepartments = selectedDepartments.toggle(it) },
                onAddNearbyFromGps = ::addNearbyFromGps,
                onAddNearbyFromCommune = { commune ->
                    communeQuery = commune.displayName
                    addNearbySites(commune.displayName, commune.latitude, commune.longitude)
                },
                onClearNearbySelection = {
                    nearbySelection = null
                    nearbyMessage = ""
                },
                onExportKml = onExportKml,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PinPointScrollableLayout(
    dataset: PinPointDataset,
    filteredDepartments: List<PinPointDepartment>,
    siteCounts: Map<String, Int>,
    selectedDepartments: Set<String>,
    mapDepartments: Set<String>,
    selectedSites: List<PinPointSite>,
    selectedDepartmentLabels: String,
    nearbySelection: PinPointNearbySelection?,
    nearbyMessage: String,
    searchMode: PinPointSearchMode,
    positionSource: PinPointPositionSource,
    communeQuery: String,
    filteredCommunes: List<PinPointCommune>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchModeChange: (PinPointSearchMode) -> Unit,
    onPositionSourceChange: (PinPointPositionSource) -> Unit,
    onCommuneQueryChange: (String) -> Unit,
    onSelectDepartments: (Set<String>) -> Unit,
    onSelectCurrentDepartment: () -> Unit,
    onToggleDepartment: (String) -> Unit,
    onAddNearbyFromGps: () -> Unit,
    onAddNearbyFromCommune: (PinPointCommune) -> Unit,
    onClearNearbySelection: () -> Unit,
    onExportKml: (PinPointExportRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PinPointSummaryCard(
                selectedDepartments = selectedDepartments,
                selectedSites = selectedSites,
            )
        }
        item {
            PinPointSelectionPanel(
                filteredDepartments = filteredDepartments,
                siteCounts = siteCounts,
                selectedDepartments = selectedDepartments,
                query = query,
                onQueryChange = onQueryChange,
                searchMode = searchMode,
                onSearchModeChange = onSearchModeChange,
                positionSource = positionSource,
                onPositionSourceChange = onPositionSourceChange,
                communeQuery = communeQuery,
                onCommuneQueryChange = onCommuneQueryChange,
                filteredCommunes = filteredCommunes,
                onSelectDepartments = onSelectDepartments,
                onSelectCurrentDepartment = onSelectCurrentDepartment,
                onToggleDepartment = onToggleDepartment,
                onAddNearbyFromGps = onAddNearbyFromGps,
                onAddNearbyFromCommune = onAddNearbyFromCommune,
                onClearNearbySelection = onClearNearbySelection,
                nearbySelection = nearbySelection,
                nearbyMessage = nearbyMessage,
                maxDepartments = filteredDepartments.size,
            )
        }
        item {
            PinPointFranceMap(
                dataset = dataset,
                selectedDepartments = mapDepartments,
                selectedSitesCount = selectedSites.size,
                siteCounts = siteCounts,
                onToggleDepartment = onToggleDepartment,
            )
        }
        item {
            PinPointExportPanel(
                selectedDepartments = selectedDepartments,
                selectedSites = selectedSites,
                selectedDepartmentLabels = selectedDepartmentLabels,
                nearbySelection = nearbySelection,
                totalSites = dataset.sites.size,
                onExportKml = onExportKml,
            )
        }
    }
}

@Composable
private fun PinPointSummaryCard(
    selectedDepartments: Set<String>,
    selectedSites: List<PinPointSite>,
) {
    ModuleHero(
        module = AppModule.PINPOINT,
        status = if (selectedSites.isEmpty()) "Sélection" else "${selectedSites.size} site(s)",
        description = "Générer un KML exploitable dans la carte DJI par département ou autour de la position terrain.",
        metrics = listOf(
            ModuleHeroMetric("Départements", selectedDepartments.size.toString(), Orange),
            ModuleHeroMetric("Sites", selectedSites.size.toString(), Color(0xFFFFB23D)),
            ModuleHeroMetric("Format", "KML", Color(0xFF33D16D)),
        ),
    )
}

@Composable
private fun PinPointSelectionPanel(
    filteredDepartments: List<PinPointDepartment>,
    siteCounts: Map<String, Int>,
    selectedDepartments: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    searchMode: PinPointSearchMode,
    onSearchModeChange: (PinPointSearchMode) -> Unit,
    positionSource: PinPointPositionSource,
    onPositionSourceChange: (PinPointPositionSource) -> Unit,
    communeQuery: String,
    onCommuneQueryChange: (String) -> Unit,
    filteredCommunes: List<PinPointCommune>,
    onSelectDepartments: (Set<String>) -> Unit,
    onSelectCurrentDepartment: () -> Unit,
    onToggleDepartment: (String) -> Unit,
    onAddNearbyFromGps: () -> Unit,
    onAddNearbyFromCommune: (PinPointCommune) -> Unit,
    onClearNearbySelection: () -> Unit,
    nearbySelection: PinPointNearbySelection?,
    nearbyMessage: String,
    maxDepartments: Int,
) {
    GlassCard(containerAlpha = 0.50f) {
        PinPointSearchControls(
            filteredDepartments = filteredDepartments,
            siteCounts = siteCounts,
            selectedDepartments = selectedDepartments,
            query = query,
            onQueryChange = onQueryChange,
            searchMode = searchMode,
            onSearchModeChange = onSearchModeChange,
            positionSource = positionSource,
            onPositionSourceChange = onPositionSourceChange,
            communeQuery = communeQuery,
            onCommuneQueryChange = onCommuneQueryChange,
            filteredCommunes = filteredCommunes,
            onSelectDepartments = onSelectDepartments,
            onSelectCurrentDepartment = onSelectCurrentDepartment,
            onToggleDepartment = onToggleDepartment,
            onAddNearbyFromGps = onAddNearbyFromGps,
            onAddNearbyFromCommune = onAddNearbyFromCommune,
            onClearNearbySelection = onClearNearbySelection,
            nearbySelection = nearbySelection,
            nearbyMessage = nearbyMessage,
            maxDepartments = maxDepartments,
        )
    }
}

@Composable
private fun PinPointSearchControls(
    filteredDepartments: List<PinPointDepartment>,
    siteCounts: Map<String, Int>,
    selectedDepartments: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    searchMode: PinPointSearchMode,
    onSearchModeChange: (PinPointSearchMode) -> Unit,
    positionSource: PinPointPositionSource,
    onPositionSourceChange: (PinPointPositionSource) -> Unit,
    communeQuery: String,
    onCommuneQueryChange: (String) -> Unit,
    filteredCommunes: List<PinPointCommune>,
    onSelectDepartments: (Set<String>) -> Unit,
    onSelectCurrentDepartment: () -> Unit,
    onToggleDepartment: (String) -> Unit,
    onAddNearbyFromGps: () -> Unit,
    onAddNearbyFromCommune: (PinPointCommune) -> Unit,
    onClearNearbySelection: () -> Unit,
    nearbySelection: PinPointNearbySelection?,
    nearbyMessage: String,
    maxDepartments: Int,
) {
    Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Recherche",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        PinPointSearchModeTabs(
            selectedMode = searchMode,
            onModeChange = onSearchModeChange,
            modifier = Modifier.fillMaxWidth(),
        )
        if (searchMode == PinPointSearchMode.Department) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher un département") },
                singleLine = true,
                colors = pinPointTextFieldColors(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PinPointQuickActionButton(
                    text = "Tout filtré",
                    onClick = { onSelectDepartments(filteredDepartments.map { it.code }.toSet()) },
                    modifier = Modifier.weight(1f),
                    highlighted = filteredDepartments.isNotEmpty(),
                )
                PinPointQuickActionButton(
                    text = "Département RC",
                    onClick = onSelectCurrentDepartment,
                    modifier = Modifier.weight(1f),
                )
                PinPointQuickActionButton(
                    text = "Effacer",
                    onClick = { onSelectDepartments(emptySet()) },
                    modifier = Modifier.weight(1f),
                    destructive = selectedDepartments.isNotEmpty(),
                )
            }
            Text(
                text = "${filteredDepartments.size} département(s) affiché(s)",
                style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.62f),
                )
            if (nearbyMessage.isNotBlank() && nearbySelection == null) {
                Text(
                    text = nearbyMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedDepartments.isNotEmpty()) Orange else Color.White.copy(alpha = 0.70f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            filteredDepartments.take(maxDepartments).chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { department ->
                        PinPointDepartmentChip(
                            department = department,
                            selected = department.code in selectedDepartments,
                            sitesCount = siteCounts[department.code] ?: 0,
                            onClick = { onToggleDepartment(department.code) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(2 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            if (filteredDepartments.size > maxDepartments) {
                Text(
                    text = "Affinez la recherche pour afficher les autres départements.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            PinPointPositionControls(
                positionSource = positionSource,
                onPositionSourceChange = onPositionSourceChange,
                communeQuery = communeQuery,
                onCommuneQueryChange = onCommuneQueryChange,
                filteredCommunes = filteredCommunes,
                nearbySelection = nearbySelection,
                nearbyMessage = nearbyMessage,
                onAddNearbyFromGps = onAddNearbyFromGps,
                onAddNearbyFromCommune = onAddNearbyFromCommune,
                onClearNearbySelection = onClearNearbySelection,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PinPointSearchModeTabs(
    selectedMode: PinPointSearchMode,
    onModeChange: (PinPointSearchMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.06f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PinPointTabButton(
            text = "Par département",
            selected = selectedMode == PinPointSearchMode.Department,
            onClick = { onModeChange(PinPointSearchMode.Department) },
            modifier = Modifier.weight(1f),
        )
        PinPointTabButton(
            text = "Autour position",
            selected = selectedMode == PinPointSearchMode.Position,
            onClick = { onModeChange(PinPointSearchMode.Position) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PinPointTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = selected,
                accent = Orange,
                containerAlpha = if (selected) 0.19f else 0.08f,
                borderAlpha = if (selected) 0.46f else 0.16f,
                highlightAlpha = 0.13f,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.78f),
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
    }
}

@Composable
private fun PinPointQuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    destructive: Boolean = false,
) {
    val accent = if (destructive) OdsColor.Warning else Orange
    Box(
        modifier = modifier
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = highlighted || destructive,
                accent = accent,
                containerAlpha = if (highlighted || destructive) 0.16f else 0.08f,
                borderAlpha = if (highlighted || destructive) 0.34f else 0.16f,
                highlightAlpha = 0.10f,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
            color = if (highlighted || destructive) Color.White else Color.White.copy(alpha = 0.78f),
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PinPointPositionControls(
    positionSource: PinPointPositionSource,
    onPositionSourceChange: (PinPointPositionSource) -> Unit,
    communeQuery: String,
    onCommuneQueryChange: (String) -> Unit,
    filteredCommunes: List<PinPointCommune>,
    nearbySelection: PinPointNearbySelection?,
    nearbyMessage: String,
    onAddNearbyFromGps: () -> Unit,
    onAddNearbyFromCommune: (PinPointCommune) -> Unit,
    onClearNearbySelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PinPointSourceButton(
                text = "GPS RC",
                selected = positionSource == PinPointPositionSource.RcGps,
                onClick = { onPositionSourceChange(PinPointPositionSource.RcGps) },
                modifier = Modifier.weight(1f),
            )
            PinPointSourceButton(
                text = "Commune",
                selected = positionSource == PinPointPositionSource.Commune,
                onClick = { onPositionSourceChange(PinPointPositionSource.Commune) },
                modifier = Modifier.weight(1f),
            )
        }
        if (positionSource == PinPointPositionSource.RcGps) {
            OrangeButton(
                onClick = onAddNearbyFromGps,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 11.dp),
            ) {
                Text("Ajouter les sites à 15 km de la RC", maxLines = 1)
            }
        } else {
            OutlinedTextField(
                value = communeQuery,
                onValueChange = onCommuneQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nom, code postal ou INSEE") },
                singleLine = true,
                colors = pinPointTextFieldColors(),
            )
            filteredCommunes.forEach { commune ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassControlSurface(
                            shape = MaterialTheme.shapes.small,
                            containerAlpha = 0.09f,
                            borderAlpha = 0.14f,
                            highlightAlpha = 0.10f,
                        )
                        .clickable { onAddNearbyFromCommune(commune) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = commune.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "Rayon 15 km · code INSEE ${commune.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.62f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = "Ajouter",
                        style = MaterialTheme.typography.labelMedium,
                        color = Orange,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            if (communeQuery.trim().length >= 2 && filteredCommunes.isEmpty()) {
                Text(
                    text = "Aucune commune trouvée.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.64f),
                )
            }
        }
        if (nearbySelection != null || nearbyMessage.isNotBlank()) {
            Text(
                text = nearbySelection?.let {
                    "Position : ${it.label} · ${it.sites.size} site(s) à ${it.radiusKm.toInt()} km"
                } ?: nearbyMessage,
                style = MaterialTheme.typography.bodySmall,
                color = if (nearbySelection != null) Orange else Color.White.copy(alpha = 0.70f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        OrangeButton(
            onClick = onClearNearbySelection,
            modifier = Modifier.fillMaxWidth(),
            enabled = nearbySelection != null,
            contentPadding = PaddingValues(vertical = 9.dp),
        ) {
            Text("Retirer la recherche par position", maxLines = 1)
        }
    }
}

@Composable
private fun PinPointSourceButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = selected,
                accent = Orange,
                containerAlpha = if (selected) 0.16f else 0.08f,
                borderAlpha = if (selected) 0.36f else 0.14f,
                highlightAlpha = 0.12f,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = if (selected) Orange else Color.White.copy(alpha = 0.80f),
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
    }
}

@Composable
private fun pinPointTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Orange,
    unfocusedLabelColor = Color.White.copy(alpha = 0.70f),
    focusedBorderColor = Orange,
    unfocusedBorderColor = Color.White.copy(alpha = 0.28f),
    cursorColor = Orange,
)

@Composable
private fun PinPointExportPanel(
    selectedDepartments: Set<String>,
    selectedSites: List<PinPointSite>,
    selectedDepartmentLabels: String,
    nearbySelection: PinPointNearbySelection?,
    totalSites: Int,
    onExportKml: (PinPointExportRequest) -> Unit,
) {
    GlassCard(containerAlpha = 0.52f) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Export carte DJI",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            InfoLine("Départements", selectedDepartmentLabels)
            nearbySelection?.let {
                InfoLine("Position", "${it.label} · ${it.sites.size} site(s) à ${it.radiusKm.toInt()} km")
            }
            InfoLine("Sites exportables", selectedSites.size.toString())
            InfoLine("Référentiel", "$totalSites sites Orange")
            Text(
                text = "La carte n’affiche que les départements pour rester lisible sur radiocommande. Le KML contient bien tous les points des départements sélectionnés.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.68f),
            )
            OrangeButton(
                onClick = {
                    onExportKml(buildPinPointExportRequest(selectedDepartments, selectedSites, nearbySelection))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedSites.isNotEmpty(),
            ) {
                Text("Exporter le fichier KML", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PinPointCockpitLayout(
    dataset: PinPointDataset,
    filteredDepartments: List<PinPointDepartment>,
    siteCounts: Map<String, Int>,
    selectedDepartments: Set<String>,
    mapDepartments: Set<String>,
    selectedSites: List<PinPointSite>,
    selectedDepartmentLabels: String,
    nearbySelection: PinPointNearbySelection?,
    nearbyMessage: String,
    searchMode: PinPointSearchMode,
    positionSource: PinPointPositionSource,
    communeQuery: String,
    filteredCommunes: List<PinPointCommune>,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchModeChange: (PinPointSearchMode) -> Unit,
    onPositionSourceChange: (PinPointPositionSource) -> Unit,
    onCommuneQueryChange: (String) -> Unit,
    onSelectDepartments: (Set<String>) -> Unit,
    onSelectCurrentDepartment: () -> Unit,
    onToggleDepartment: (String) -> Unit,
    onAddNearbyFromGps: () -> Unit,
    onAddNearbyFromCommune: (PinPointCommune) -> Unit,
    onClearNearbySelection: () -> Unit,
    onExportKml: (PinPointExportRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1.25f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GlassCard(containerAlpha = 0.50f) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CompagnonIcon(
                        module = AppModule.PINPOINT,
                        modifier = Modifier.size(38.dp),
                        color = statusIconColor(AppModule.PINPOINT),
                        thin = true,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Point d’intérêt",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                        )
                        Text(
                            text = "${mapDepartments.size} département(s) · ${selectedSites.size} site(s) · KML carte DJI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            PinPointFranceMap(
                dataset = dataset,
                selectedDepartments = mapDepartments,
                selectedSitesCount = selectedSites.size,
                siteCounts = siteCounts,
                onToggleDepartment = onToggleDepartment,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                showDetails = false,
            )
        }

        Column(
            modifier = Modifier
                .weight(0.78f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GlassCard(
                modifier = Modifier.weight(1f),
                containerAlpha = 0.50f,
            ) {
                PinPointSearchControls(
                    filteredDepartments = filteredDepartments,
                    siteCounts = siteCounts,
                    selectedDepartments = selectedDepartments,
                    query = query,
                    onQueryChange = onQueryChange,
                    searchMode = searchMode,
                    onSearchModeChange = onSearchModeChange,
                    positionSource = positionSource,
                    onPositionSourceChange = onPositionSourceChange,
                    communeQuery = communeQuery,
                    onCommuneQueryChange = onCommuneQueryChange,
                    filteredCommunes = filteredCommunes,
                    onSelectDepartments = onSelectDepartments,
                    onSelectCurrentDepartment = onSelectCurrentDepartment,
                    onToggleDepartment = onToggleDepartment,
                    onAddNearbyFromGps = onAddNearbyFromGps,
                    onAddNearbyFromCommune = onAddNearbyFromCommune,
                    onClearNearbySelection = onClearNearbySelection,
                    nearbySelection = nearbySelection,
                    nearbyMessage = nearbyMessage,
                    maxDepartments = 6,
                )
            }

            GlassCard(containerAlpha = 0.52f) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfoLine("Sélection", selectedDepartmentLabels)
                    nearbySelection?.let {
                        InfoLine("Position", "${it.label} · ${it.sites.size} site(s) à ${it.radiusKm.toInt()} km")
                    }
                    InfoLine("Sites", selectedSites.size.toString())
                    InfoLine("Référentiel", "${dataset.sites.size} sites")
                    OrangeButton(
                        onClick = {
                            onExportKml(buildPinPointExportRequest(selectedDepartments, selectedSites, nearbySelection))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedSites.isNotEmpty(),
                        contentPadding = PaddingValues(vertical = 10.dp),
                    ) {
                        Text("Exporter KML", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PinPointFranceMap(
    dataset: PinPointDataset,
    selectedDepartments: Set<String>,
    selectedSitesCount: Int,
    modifier: Modifier = Modifier,
    siteCounts: Map<String, Int> = emptyMap(),
    onToggleDepartment: ((String) -> Unit)? = null,
    showDetails: Boolean = true,
) {
    GlassCard(modifier = modifier, containerAlpha = 0.42f, borderAlpha = 0.28f, highlightAlpha = 0.08f) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
                .padding(14.dp),
        ) {
            val compact = maxWidth < 720.dp || !showDetails
            val overseasDepartments = dataset.departments.filter { it.code in PinPointOverseasDepartmentCodes }
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(if (compact) 1f else 1.15f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.Black.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val mapBounds = dataset.bounds
                        val padding = 18f
                        val availableWidth = size.width - padding * 2f
                        val availableHeight = size.height - padding * 2f
                        val centerLatitudeRad = ((mapBounds.minLat + mapBounds.maxLat) / 2.0) * PI / 180.0
                        val longitudeScale = cos(centerLatitudeRad)
                        val projectedMinX = mapBounds.minLon * longitudeScale
                        val projectedWidth = (mapBounds.maxLon - mapBounds.minLon) * longitudeScale
                        val projectedHeight = mapBounds.maxLat - mapBounds.minLat
                        val mapScale = min(
                            availableWidth / projectedWidth.toFloat(),
                            availableHeight / projectedHeight.toFloat(),
                        )
                        val drawnWidth = projectedWidth.toFloat() * mapScale
                        val drawnHeight = projectedHeight.toFloat() * mapScale
                        val left = (size.width - drawnWidth) / 2f
                        val top = (size.height - drawnHeight) / 2f
                        fun toScreen(point: PinPointGeoPoint): Offset {
                            val projectedX = point.longitude * longitudeScale
                            val x = ((projectedX - projectedMinX) * mapScale).toFloat()
                            val y = ((mapBounds.maxLat - point.latitude) * mapScale).toFloat()
                            return Offset(left + x, top + y)
                        }
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Orange.copy(alpha = 0.18f), Color.Transparent),
                                center = Offset(size.width * 0.54f, size.height * 0.55f),
                                radius = size.width * 0.48f,
                            ),
                        )
                        dataset.departments
                            .filter { it.code !in PinPointOverseasDepartmentCodes }
                            .forEach { department ->
                                val selected = department.code in selectedDepartments
                                department.polygons.forEach { polygon ->
                                    val path = Path()
                                    polygon.forEachIndexed { index, point ->
                                        val screen = toScreen(point)
                                        if (index == 0) path.moveTo(screen.x, screen.y) else path.lineTo(screen.x, screen.y)
                                    }
                                    path.close()
                                    drawPath(
                                        path = path,
                                        color = if (selected) Orange.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.055f),
                                    )
                                    drawPath(
                                        path = path,
                                        color = if (selected) Orange.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.20f),
                                        style = Stroke(width = if (selected) 1.65f else 0.85f),
                                    )
                                }
                            }
                    }
                    if (overseasDepartments.isNotEmpty()) {
                        PinPointOverseasMiniMap(
                            departments = overseasDepartments,
                            selectedDepartments = selectedDepartments,
                            onToggleDepartment = onToggleDepartment,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = if (compact) 14.dp else 18.dp)
                                .width(if (compact) 74.dp else 86.dp),
                        )
                    }
                }
                if (!compact) {
                    Column(
                        modifier = Modifier.weight(0.85f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Carte de sélection",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "${selectedDepartments.size} département(s) sélectionné(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.76f),
                        )
                        Text(
                            text = "$selectedSitesCount site(s) exportable(s)",
                            style = MaterialTheme.typography.titleMedium,
                            color = Orange,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = "La liste détaillée n’est pas affichée pour garder l’écran lisible sur radiocommande.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.58f),
                        )
                    }
                }
            }
        }
    }
}

private val PinPointOverseasDepartmentCodes = setOf("971", "974", "976")

@Composable
private fun PinPointOverseasMiniMap(
    departments: List<PinPointDepartment>,
    selectedDepartments: Set<String>,
    onToggleDepartment: ((String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
    ) {
        departments.forEach { department ->
            val selected = department.code in selectedDepartments
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(enabled = onToggleDepartment != null) { onToggleDepartment?.invoke(department.code) }
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                PinPointDepartmentMiniMap(
                    department = department,
                    selected = selected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                )
            }
        }
    }
}

@Composable
private fun PinPointDepartmentMiniMap(
    department: PinPointDepartment,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (department.polygons.isEmpty()) return@Canvas
            val mapBounds = listOf(department).pinPointBounds()
            val padding = 6f
            val availableWidth = size.width - padding * 2f
            val availableHeight = size.height - padding * 2f
            val centerLatitudeRad = ((mapBounds.minLat + mapBounds.maxLat) / 2.0) * PI / 180.0
            val longitudeScale = cos(centerLatitudeRad)
            val projectedMinX = mapBounds.minLon * longitudeScale
            val projectedWidth = (mapBounds.maxLon - mapBounds.minLon) * longitudeScale
            val projectedHeight = mapBounds.maxLat - mapBounds.minLat
            val mapScale = min(
                availableWidth / projectedWidth.toFloat().coerceAtLeast(0.0001f),
                availableHeight / projectedHeight.toFloat().coerceAtLeast(0.0001f),
            )
            val drawnWidth = projectedWidth.toFloat() * mapScale
            val drawnHeight = projectedHeight.toFloat() * mapScale
            val left = (size.width - drawnWidth) / 2f
            val top = (size.height - drawnHeight) / 2f
            fun toScreen(point: PinPointGeoPoint): Offset {
                val projectedX = point.longitude * longitudeScale
                val x = ((projectedX - projectedMinX) * mapScale).toFloat()
                val y = ((mapBounds.maxLat - point.latitude) * mapScale).toFloat()
                return Offset(left + x, top + y)
            }
            department.polygons.forEach { polygon ->
                val path = Path()
                polygon.forEachIndexed { index, point ->
                    val screen = toScreen(point)
                    if (index == 0) path.moveTo(screen.x, screen.y) else path.lineTo(screen.x, screen.y)
                }
                path.close()
                drawPath(
                    path = path,
                    color = if (selected) Orange.copy(alpha = 0.58f) else Color.White.copy(alpha = 0.10f),
                )
                drawPath(
                    path = path,
                    color = if (selected) Orange.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.38f),
                    style = Stroke(width = if (selected) 1.5f else 0.9f),
                )
            }
        }
    }
}

@Composable
private fun PinPointDepartmentChip(
    department: PinPointDepartment,
    selected: Boolean,
    sitesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) Orange.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            drawCircle(if (selected) Orange else Color.White.copy(alpha = 0.28f), radius = size.minDimension / 2f)
            if (selected) {
                drawCircle(Color.White, radius = size.minDimension * 0.22f)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${department.code} - ${department.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${department.region} · $sitesCount site(s)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private data class PinPointDepartment(
    val code: String,
    val name: String,
    val region: String,
    val polygons: List<List<PinPointGeoPoint>>,
)

internal data class PinPointCommune(
    val code: String,
    val name: String,
    val postalCodes: List<String>,
    val latitude: Double,
    val longitude: Double,
)

internal val PinPointCommune.displayName: String
    get() = "$name (${postalCodes.firstOrNull().orEmpty().ifBlank { code }})"

private fun PinPointCommune.toFlightWeatherSearchTarget(): FlightWeatherSearchTarget =
    FlightWeatherSearchTarget(
        label = displayName,
        query = name,
        latitude = latitude,
        longitude = longitude,
    )

private data class PinPointSite(
    val id: String,
    val name: String,
    val departmentCode: String,
    val inseeCode: String,
    val commune: String,
    val address: String,
    val postalCode: String,
    val latitude: Double,
    val longitude: Double,
    val technology: String,
    val supportType: String,
    val heightMeters: String,
    val owner: String,
)

private val PinPointSite.uniqueKey: String
    get() = id.ifBlank { "${departmentCode}_${latitude}_${longitude}_${name}" }

private val PinPointSite.kmlDisplayName: String
    get() = buildList {
        add("Orange")
        commune.takeIf { it.isNotBlank() }?.let(::add)
        postalCode.takeIf { it.isNotBlank() }?.let { add("($it)") }
        id.takeIf { it.isNotBlank() }?.let { add("- $it") }
    }.joinToString(" ")

private data class PinPointNearbySelection(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val sites: List<PinPointSite>,
)

private data class PinPointGeoPoint(
    val latitude: Double,
    val longitude: Double,
)

private data class PinPointBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
)

private data class PinPointDataset(
    val departments: List<PinPointDepartment>,
    val communes: List<PinPointCommune>,
    val sites: List<PinPointSite>,
    val bounds: PinPointBounds,
    val siteCountsByDepartment: Map<String, Int>,
)

private data class PinPointExportRequest(
    val fileName: String,
    val kml: String,
)

private enum class PinPointSearchMode {
    Department,
    Position,
}

private enum class PinPointPositionSource {
    RcGps,
    Commune,
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) this - value else this + value

private fun normalizeSearchText(value: String): String =
    Normalizer.normalize(value.trim().lowercase(Locale.FRANCE), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

private fun buildPinPointExportRequest(
    selectedDepartments: Set<String>,
    selectedSites: List<PinPointSite>,
    nearbySelection: PinPointNearbySelection?,
): PinPointExportRequest {
    val fileSuffix = when {
        selectedDepartments.isNotEmpty() && nearbySelection != null -> "selection_plus_15km"
        selectedDepartments.isNotEmpty() -> selectedDepartments.pinPointDepartmentSortKey().joinToString("-")
        nearbySelection != null -> "${nearbySelection.label.toPinPointFileToken()}_15km"
        else -> "vide"
    }
    val exportDepartments = selectedSites.map { it.departmentCode }.toSet()
    return PinPointExportRequest(
        fileName = "Orange_PinPoint_$fileSuffix.kml",
        kml = buildPinPointKml(selectedSites, exportDepartments),
    )
}

private fun Set<String>.pinPointDepartmentSortKey(): List<String> =
    sortedWith(compareBy<String> { it.toIntOrNull() ?: if (it == "2A") 20 else if (it == "2B") 21 else 999 }.thenBy { it })

private fun buildPinPointKml(sites: List<PinPointSite>, selectedDepartments: Set<String>): String {
    val departmentLabel = selectedDepartments.pinPointDepartmentSortKey().joinToString(", ").ifBlank { "aucun" }
    return buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        appendLine("""<kml xmlns="http://www.opengis.net/kml/2.2">""")
        appendLine("  <Document>")
        appendLine("    <name>Orange PinPoint - départements $departmentLabel</name>")
        appendLine("    <description>Sites Orange exportés depuis Orange Drone Compagnon pour import dans la carte DJI.</description>")
        appendLine("""    <Style id="orange-site"><IconStyle><color>ff0079ff</color><scale>1.05</scale><Icon><href>http://maps.google.com/mapfiles/kml/paddle/orange-circle.png</href></Icon></IconStyle></Style>""")
        sites.groupBy { it.departmentCode }
            .toSortedMap(compareBy<String> { it.toIntOrNull() ?: if (it == "2A") 20 else if (it == "2B") 21 else 999 }.thenBy { it })
            .forEach { (departmentCode, departmentSites) ->
                appendLine("    <Folder>")
                appendLine("      <name>Département ${departmentCode.escapeXml()} - ${departmentSites.size} site(s)</name>")
                departmentSites
                    .sortedWith(compareBy<PinPointSite> { it.commune }.thenBy { it.name })
                    .forEach { site ->
                        appendLine("      <Placemark>")
                        appendLine("        <name>${site.kmlDisplayName.escapeXml()}</name>")
                        appendLine("        <styleUrl>#orange-site</styleUrl>")
                        appendLine("        <description>${site.pinPointKmlDescription().escapeXml()}</description>")
                        appendLine("        <ExtendedData>")
                        appendLine("""          <Data name="code_site"><value>${site.id.escapeXml()}</value></Data>""")
                        appendLine("""          <Data name="departement"><value>${site.departmentCode.escapeXml()}</value></Data>""")
                        appendLine("""          <Data name="commune"><value>${site.commune.escapeXml()}</value></Data>""")
                        appendLine("""          <Data name="code_postal"><value>${site.postalCode.escapeXml()}</value></Data>""")
                        appendLine("        </ExtendedData>")
                        appendLine("        <Point><coordinates>${site.longitude},${site.latitude},0</coordinates></Point>")
                        appendLine("      </Placemark>")
                    }
                appendLine("    </Folder>")
        }
        appendLine("  </Document>")
        appendLine("</kml>")
    }
}

private fun PinPointSite.pinPointKmlDescription(): String = buildString {
    append("Code site : $id\n")
    append("Département : $departmentCode\n")
    append("Commune : $commune\n")
    append("Code INSEE : $inseeCode\n")
    append("Adresse : $address\n")
    append("Code postal : $postalCode\n")
    append("Support : $supportType\n")
    append("Hauteur : $heightMeters m\n")
    append("Propriétaire : $owner\n")
    append("Technologie : $technology")
}

private fun findBestLastKnownLocation(context: Context): Location? {
    val fineGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fineGranted && !coarseGranted) return null
    val locationManager = context.getSystemService(LocationManager::class.java)
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    )
    return providers
        .mapNotNull { provider ->
            runCatching {
                if (provider in locationManager.getProviders(true)) {
                    locationManager.getLastKnownLocation(provider)
                } else {
                    null
                }
            }.getOrNull()
        }
        .maxByOrNull { it.time }
}

@SuppressLint("MissingPermission")
private suspend fun findCurrentOrLastKnownLocation(context: Context): Location? {
    findBestLastKnownLocation(context)?.let { return it }

    val fineGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!fineGranted && !coarseGranted) return null

    val locationManager = context.getSystemService(LocationManager::class.java)
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).filter { provider ->
        runCatching { provider in locationManager.getProviders(true) }.getOrDefault(false)
    }

    providers.forEach { provider ->
        val currentLocation = withTimeoutOrNull(2_500L) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }
                }
                runCatching {
                    @Suppress("DEPRECATION")
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }.onFailure {
                    locationManager.removeUpdates(listener)
                    if (continuation.isActive) continuation.resume(null)
                }
                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(listener)
                }
            }
        }
        if (currentLocation != null) return currentLocation
    }

    return findBestLastKnownLocation(context)
}

private fun PinPointDepartment.contains(latitude: Double, longitude: Double): Boolean =
    polygons.any { polygon -> polygon.containsPinPoint(latitude, longitude) }

private fun List<PinPointGeoPoint>.containsPinPoint(latitude: Double, longitude: Double): Boolean {
    if (size < 3) return false
    var inside = false
    var previous = last()
    for (current in this) {
        val crossesLatitude = (current.latitude > latitude) != (previous.latitude > latitude)
        if (crossesLatitude) {
            val crossingLongitude = (previous.longitude - current.longitude) *
                (latitude - current.latitude) /
                (previous.latitude - current.latitude) +
                current.longitude
            if (longitude < crossingLongitude) inside = !inside
        }
        previous = current
    }
    return inside
}

private fun distanceMetersBetween(
    latitudeA: Double,
    longitudeA: Double,
    latitudeB: Double,
    longitudeB: Double,
): Double {
    val earthRadiusMeters = 6_371_000.0
    val lat1 = latitudeA * PI / 180.0
    val lat2 = latitudeB * PI / 180.0
    val deltaLat = (latitudeB - latitudeA) * PI / 180.0
    val deltaLon = (longitudeB - longitudeA) * PI / 180.0
    val haversine = sin(deltaLat / 2.0) * sin(deltaLat / 2.0) +
        cos(lat1) * cos(lat2) * sin(deltaLon / 2.0) * sin(deltaLon / 2.0)
    return earthRadiusMeters * 2.0 * asin(min(1.0, sqrt(haversine)))
}

private fun String.escapeXml(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

private fun String.toPinPointFileToken(): String =
    lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .ifBlank { "position" }
        .take(32)

@Volatile
private var cachedPinPointDataset: PinPointDataset? = null

@Volatile
private var cachedPinPointCommunes: List<PinPointCommune>? = null

private fun loadPinPointDataset(context: android.content.Context): PinPointDataset {
    cachedPinPointDataset?.let { return it }
    return synchronized(PinPointDataset::class.java) {
        cachedPinPointDataset ?: run {
            val departments = loadPinPointDepartments(context)
            val departmentCodes = departments.map { it.code }.toSet()
            val sites = loadPinPointSites(context, emptyMap()).filter { it.departmentCode in departmentCodes }
            PinPointDataset(
                departments = departments,
                communes = emptyList(),
                sites = sites,
                bounds = departments.filter { it.code !in PinPointOverseasDepartmentCodes }.pinPointBounds(),
                siteCountsByDepartment = sites.groupingBy { it.departmentCode }.eachCount(),
            ).also { cachedPinPointDataset = it }
        }
    }
}

private fun loadPinPointDepartments(context: android.content.Context): List<PinPointDepartment> {
    val root = JSONObject(context.assets.open("departements_france.geojson").bufferedReader().use { it.readText() })
    val features = root.getJSONArray("features")
    val mappedDepartments = buildList {
        for (index in 0 until features.length()) {
            val feature = features.getJSONObject(index)
            val properties = feature.getJSONObject("properties")
            val code = properties.getString("code")
            val name = properties.getString("nom")
            val geometry = feature.getJSONObject("geometry")
            val polygons = parsePinPointGeometry(geometry)
            if (polygons.isNotEmpty()) {
                add(
                    PinPointDepartment(
                        code = code,
                        name = name,
                        region = departmentRegion(code),
                        polygons = polygons,
                    ),
                )
            }
        }
    }.sortedBy { it.code }
    val existingCodes = mappedDepartments.map { it.code }.toSet()
    val overseasDepartments = listOf(
        PinPointDepartment(code = "971", name = "Guadeloupe", region = "Guadeloupe", polygons = emptyList()),
        PinPointDepartment(code = "974", name = "La Réunion", region = "La Réunion", polygons = emptyList()),
        PinPointDepartment(code = "976", name = "Mayotte", region = "Mayotte", polygons = emptyList()),
    ).filter { it.code !in existingCodes }
    return (mappedDepartments + overseasDepartments).sortedWith(compareBy<PinPointDepartment> { it.code.toIntOrNull() ?: 999 }.thenBy { it.code })
}

private fun parsePinPointGeometry(geometry: JSONObject): List<List<PinPointGeoPoint>> {
    val type = geometry.getString("type")
    val coordinates = geometry.getJSONArray("coordinates")
    return when (type) {
        "Polygon" -> listOfNotNull(parsePinPointPolygon(coordinates))
        "MultiPolygon" -> buildList {
            for (index in 0 until coordinates.length()) {
                parsePinPointPolygon(coordinates.getJSONArray(index))?.let(::add)
            }
        }
        else -> emptyList()
    }
}

private fun parsePinPointPolygon(rings: JSONArray): List<PinPointGeoPoint>? {
    if (rings.length() == 0) return null
    val exteriorRing = rings.getJSONArray(0)
    return buildList {
        for (index in 0 until exteriorRing.length()) {
            val coordinate = exteriorRing.getJSONArray(index)
            add(PinPointGeoPoint(latitude = coordinate.getDouble(1), longitude = coordinate.getDouble(0)))
        }
    }.takeIf { it.size >= 3 }
}

private fun loadPinPointSites(context: android.content.Context, communeNamesByInsee: Map<String, String>): List<PinPointSite> {
    return context.assets.open("sites_orange_present.csv").bufferedReader(Charsets.UTF_8).useLines { lines ->
        val iterator = lines.iterator()
        if (!iterator.hasNext()) return@useLines emptyList()
        val headers = parsePinPointCsvLine(iterator.next().removePrefix("\uFEFF"))
        val headerIndex = headers.mapIndexed { index, value -> value to index }.toMap()
        fun field(row: List<String>, name: String): String =
            headerIndex[name]?.let { row.getOrNull(it) }.orEmpty().trim()
        buildList {
            iterator.forEachRemaining { line ->
                val row = parsePinPointCsvLine(line)
                val department = field(row, "Departement")
                val latitude = field(row, "Latitude").replace(',', '.').toDoubleOrNull()
                val longitude = field(row, "Longitude").replace(',', '.').toDoubleOrNull()
                val supportId = field(row, "Support_ID")
                if (department.isNotBlank() && latitude != null && longitude != null && supportId.isNotBlank()) {
                    val codeInsee = field(row, "Code_INSEE")
                    val communeName = communeNamesByInsee[codeInsee].orEmpty()
                    add(
                        PinPointSite(
                            id = supportId,
                            name = "Orange - $supportId - ${communeName.ifBlank { codeInsee }}",
                            departmentCode = department,
                            inseeCode = codeInsee,
                            commune = communeName.ifBlank { codeInsee },
                            address = field(row, "Adresse"),
                            postalCode = field(row, "Code_postal"),
                            latitude = latitude,
                            longitude = longitude,
                            technology = field(row, "Technologies_systemes"),
                            supportType = field(row, "Nature_support"),
                            heightMeters = field(row, "Hauteur_m"),
                            owner = field(row, "Proprietaire"),
                        ),
                    )
                }
            }
        }
    }
}

private fun loadPinPointCommunes(context: android.content.Context): List<PinPointCommune> {
    cachedPinPointCommunes?.let { return it }
    return synchronized(PinPointCommune::class.java) {
        cachedPinPointCommunes ?: runCatching {
            val communes = JSONArray(context.assets.open("communes_france.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
            buildList {
                for (index in 0 until communes.length()) {
                    val commune = communes.getJSONObject(index)
                    val code = commune.optString("code")
                    val name = commune.optString("nom")
                    val centre = commune.optJSONObject("centre")
                    val coordinates = centre?.optJSONArray("coordinates")
                    val longitude = coordinates?.optDouble(0)
                    val latitude = coordinates?.optDouble(1)
                    if (code.isNotBlank() && name.isNotBlank() && latitude != null && longitude != null) {
                        val postalCodesArray = commune.optJSONArray("codesPostaux")
                        val postalCodes = buildList {
                            if (postalCodesArray != null) {
                                for (postalIndex in 0 until postalCodesArray.length()) {
                                    postalCodesArray.optString(postalIndex).takeIf { it.isNotBlank() }?.let(::add)
                                }
                            }
                        }
                        add(
                            PinPointCommune(
                                code = code,
                                name = name,
                                postalCodes = postalCodes,
                                latitude = latitude,
                                longitude = longitude,
                            ),
                        )
                    }
                }
            }.sortedWith(compareBy<PinPointCommune> { it.name }.thenBy { it.code })
        }.getOrDefault(emptyList()).also { cachedPinPointCommunes = it }
    }
}

private fun parsePinPointCsvLine(line: String): List<String> {
    val values = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var index = 0
    while (index < line.length) {
        val char = line[index]
        when {
            char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                current.append('"')
                index++
            }
            char == '"' -> inQuotes = !inQuotes
            char == ';' && !inQuotes -> {
                values += current.toString()
                current.clear()
            }
            else -> current.append(char)
        }
        index++
    }
    values += current.toString()
    return values
}

private fun List<PinPointDepartment>.pinPointBounds(): PinPointBounds {
    val points = flatMap { department -> department.polygons.flatten() }
    if (points.isEmpty()) {
        return PinPointBounds(minLat = 0.0, maxLat = 1.0, minLon = 0.0, maxLon = 1.0)
    }
    return PinPointBounds(
        minLat = points.minOf { it.latitude },
        maxLat = points.maxOf { it.latitude },
        minLon = points.minOf { it.longitude },
        maxLon = points.maxOf { it.longitude },
    )
}

private fun departmentRegion(code: String): String =
    when (code) {
        "01", "03", "07", "15", "26", "38", "42", "43", "63", "69", "73", "74" -> "Auvergne-Rhône-Alpes"
        "21", "25", "39", "58", "70", "71", "89", "90" -> "Bourgogne-Franche-Comté"
        "22", "29", "35", "56" -> "Bretagne"
        "18", "28", "36", "37", "41", "45" -> "Centre-Val de Loire"
        "2A", "2B" -> "Corse"
        "08", "10", "51", "52", "54", "55", "57", "67", "68", "88" -> "Grand Est"
        "02", "59", "60", "62", "80" -> "Hauts-de-France"
        "75", "77", "78", "91", "92", "93", "94", "95" -> "Île-de-France"
        "14", "27", "50", "61", "76" -> "Normandie"
        "16", "17", "19", "23", "24", "33", "40", "47", "64", "79", "86", "87" -> "Nouvelle-Aquitaine"
        "09", "11", "12", "30", "31", "32", "34", "46", "48", "65", "66", "81", "82" -> "Occitanie"
        "44", "49", "53", "72", "85" -> "Pays de la Loire"
        "04", "05", "06", "13", "83", "84" -> "Provence-Alpes-Côte d’Azur"
        else -> "France"
    }

@Composable
private fun MediaConsultationContent(
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    modifier: Modifier = Modifier,
) {
    val recentFlights = remember(logs) {
        logs.sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }.take(12)
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(containerAlpha = 0.56f) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CompagnonIcon(
                            module = AppModule.MEDIA,
                            modifier = Modifier.size(42.dp),
                            color = statusIconColor(AppModule.MEDIA),
                            thin = true,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Médias du vol",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Photos et vidéos regroupées par vol pour retrouver rapidement les contenus de mission.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f),
                            )
                        }
                    }
                    InfoLine("Drone", if (djiSdkState.hasConnectedDrone()) "Connecté" else "Non connecté")
                    InfoLine("Source", "Radiocommande, drone branché ou support mémoire accessible")
                    InfoLine("Classement", "Les médias du drone seront présentés sous chaque vol détecté")
                }
            }
        }
        item {
            Text(
                text = "Vols disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        if (recentFlights.isEmpty()) {
            item {
                GlassCard(containerAlpha = 0.30f) {
                    Text(
                        text = "Aucun log de vol détecté. L’app scanne automatiquement l’emplacement DJI FlightRecord ; synchronisez ou utilisez le dossier manuel seulement si l’accès direct est bloqué.",
                        modifier = Modifier.padding(14.dp),
                        color = Color.White.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            items(recentFlights, key = { it.id }) { log ->
                MediaFlightFolderRow(log = log, djiConnected = djiSdkState.hasConnectedDrone())
            }
        }
        item {
            CopyrightFooter()
        }
    }
}

@Composable
private fun MediaFlightFolderRow(
    log: LogFileEntity,
    djiConnected: Boolean,
) {
    GlassCard(containerAlpha = 0.32f) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompagnonIcon(
                module = AppModule.MEDIA,
                modifier = Modifier.size(34.dp),
                color = if (djiConnected) Color(0xFF33D16D) else Color.White.copy(alpha = 0.62f),
                thin = true,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = log.flightStartTimeMillis?.let(::formatDate)
                        ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate)
                        ?: log.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = log.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.64f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = log.flightDurationSeconds?.let(::formatDuration) ?: "-",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = if (djiConnected) "Médias à scanner" else "Drone requis",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (djiConnected) Color(0xFF33D16D) else Color.White.copy(alpha = 0.58f),
                )
            }
        }
    }
}

@Composable
private fun SettingsContent(
    draft: AppSettings,
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    actionMessage: String,
    onDraftChange: (AppSettings) -> Unit,
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onPickMediaFolder: () -> Unit,
    onPickScreenExportFolder: () -> Unit,
    onThemeChange: (String) -> Unit,
    onOpenModule: (AppModule) -> Unit,
    onRetryDjiSdk: () -> Unit,
    onRefreshLogs: () -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    onInstallUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) {
        usbDestinationState(context, settings.usbExportUri)
    }
    val logFolderState = remember(settings.folderUri, logs.size) {
        logFolderReadiness(context, settings.folderUri)
    }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ModuleHero(
                module = AppModule.SETTINGS,
                status = if (logFolderState == FolderReadiness.Ready && usbState == UsbDestinationState.Ready) "Prêt" else "À vérifier",
                description = "Centraliser les autorisations dossiers, la clé USB, l’apparence et le diagnostic terrain.",
                metrics = listOf(
                    ModuleHeroMetric(
                        "Logs",
                        if (logFolderState == FolderReadiness.Ready) "OK" else "À choisir",
                        if (logFolderState == FolderReadiness.Ready) Color(0xFF33D16D) else Orange,
                    ),
                    ModuleHeroMetric(
                        "USB",
                        usbState.shortLabel,
                        usbState.color,
                    ),
                    ModuleHeroMetric(
                        "DJI",
                        if (djiSdkState.hasConnectedDrone()) "Connecté" else "Non connecté",
                        if (djiSdkState.hasConnectedDrone()) Color(0xFF33D16D) else Color.White.copy(alpha = 0.80f),
                    ),
                ),
            )
        }
        if (!settings.onboardingCompleted) {
            item {
                OnboardingCard(draft, logs)
            }
        }
        item {
            ConfigurationCard(draft, onDraftChange, onPickFolder, onPickUsbFolder, onPickMediaFolder, onSave, onTest)
        }
        item {
            EquipmentDiagnosticCard(
                settings = settings,
                djiSdkState = djiSdkState,
                onRetryDjiSdk = onRetryDjiSdk,
            )
        }
        item {
            FieldTestReadinessCard(
                settings = settings,
                logs = logs,
                djiSdkState = djiSdkState,
                onPickFolder = onPickFolder,
                onPickUsbFolder = onPickUsbFolder,
                onRefreshLogs = onRefreshLogs,
                onTestUsb = onTest,
                onOpenModule = onOpenModule,
            )
        }
        item {
            AppearanceCard(
                currentThemeId = settings.appTheme,
                onThemeChange = onThemeChange,
            )
        }
        item {
            GlassCard(containerAlpha = 0.50f) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Fond d’écran", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    InfoLine("Dossier export", settings.screenExportLabel.ifBlank { "Non configuré" })
                    OrangeButton(onClick = onPickScreenExportFolder, modifier = Modifier.fillMaxWidth()) {
                        Text("Choisir dossier export fonds d’écran")
                    }
                }
            }
        }
        item {
            GlassCard(containerAlpha = 0.44f) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Application", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    InfoLine("Version", BuildConfig.VERSION_NAME)
                    InfoLine("Mobile SDK DJI", "Clé Android intégrée")
                    InfoLine("Décodage FlightRecord", if (settings.djiApiKey.isBlank()) "Clé intégrée" else "Clé personnalisée enregistrée")
                    InfoLine("Mode partage", "USB prioritaire")
                    if (settings.latestVersionName.isNotBlank()) {
                        InfoLine(
                            "Mise à jour",
                            if (settings.latestApkSha256.isNotBlank()) {
                                "${settings.latestVersionName} · APK vérifié${settings.latestApkSizeBytes.takeIf { it > 0 }?.let { " · ${formatBytes(it)}" }.orEmpty()}"
                            } else {
                                "${settings.latestVersionName} · vérification incomplète"
                            },
                        )
                        OrangeButton(
                            onClick = onInstallUpdate,
                            enabled = settings.latestApkSha256.isNotBlank() && settings.latestApkUrl.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Télécharger et installer la mise à jour")
                        }
                    }
                    if (actionMessage.isNotBlank()) {
                        Text(
                            text = actionMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.84f),
                        )
                    }
                }
            }
        }
        item {
            CopyrightFooter()
        }
    }
}

@Composable
private fun EquipmentDiagnosticCard(
    settings: AppSettings,
    djiSdkState: DjiSdkState,
    onRetryDjiSdk: () -> Unit = {},
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) {
        usbDestinationState(context, settings.usbExportUri)
    }
    val usbAvailableBytes = remember(settings.usbExportUri, usbState, usbProbeTick) {
        if (usbState == UsbDestinationState.Ready) resolveUsbAvailableBytes(settings.usbExportUri) else null
    }
    val usbDiagnostic = remember(settings.usbExportUri, usbProbeTick) {
        usbDiagnosticInfo(context, settings.usbExportUri)
    }
    GlassCard(containerAlpha = 0.50f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Diagnostic équipements", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            InfoLine("Clé USB", usbState.destinationLabel)
            InfoLine("Espace USB", usbAvailableBytes?.let(::formatStorageBytes) ?: "-")
            InfoLine("Autorisation USB", if (settings.usbExportUri.isBlank()) "Dossier non choisi" else settings.usbExportLabel.ifBlank { "Dossier enregistré" })
            InfoLine("Volumes Android", usbDiagnostic.volumeSummary)
            InfoLine("Périphériques USB", usbDiagnostic.deviceSummary)
            InfoLine("Droit dossier", usbDiagnostic.permissionSummary)
            InfoLine("SDK DJI", djiSdkState.sdkVersion?.let { "$it · ${djiSdkState.message}" } ?: djiSdkState.message)
            InfoLine("Drone", if (djiSdkState.hasConnectedDrone()) "Connecté" else "Non connecté")
            InfoLine("Modèle DJI", djiSdkState.productType ?: "-")
            InfoLine("Firmware drone", djiSdkState.aircraftFirmwareVersion ?: "-")
            InfoLine("Radiocommande", djiSdkState.remoteControllerType ?: "-")
            InfoLine("Firmware RC", djiSdkState.remoteControllerFirmwareVersion ?: "-")
            djiSdkState.firmwareWarning?.let { InfoLine("Compatibilité DJI", it) }
            djiSdkState.lastError?.let { InfoLine("Erreur DJI", it) }
            SecondaryFieldButton(
                text = "Relancer SDK DJI",
                onClick = onRetryDjiSdk,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Si la clé est branchée mais non autorisée, appuyez sur « Choisir dossier sur clé USB » et sélectionnez la racine de la clé. Si le drone reste non connecté, fermez l’application de vol DJI puis relancez Orange Drone Compagnon avec la radiocommande connectée au drone.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
private fun FieldTestReadinessCard(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    onPickFolder: () -> Unit = {},
    onPickUsbFolder: () -> Unit = {},
    onRefreshLogs: () -> Unit = {},
    onTestUsb: () -> Unit = {},
    onOpenModule: (AppModule) -> Unit = {},
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) {
        usbDestinationState(context, settings.usbExportUri)
    }
    val logFolderState = remember(settings.folderUri, logs.size) {
        logFolderReadiness(context, settings.folderUri)
    }
    val latestLog = remember(logs) {
        logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    }
    val readyItems = listOf(
        FieldTestItem(
            title = "Logs DJI",
            detail = when {
                settings.folderUri.isBlank() -> "Emplacement FlightRecord automatique non configuré"
                logFolderState == FolderReadiness.Ready -> settings.folderLabel.ifBlank { "Emplacement DJI accessible" }
                else -> "Emplacement automatique non accessible ; utiliser le dossier manuel en secours"
            },
            state = if (settings.folderUri.isNotBlank() && logFolderState == FolderReadiness.Ready) {
                FieldTestState.Ok
            } else {
                FieldTestState.Blocking
            },
        ),
        FieldTestItem(
            title = "Logs détectés",
            detail = latestLog?.let {
                "${logs.size} log(s) · dernier : ${it.fileName}"
            } ?: "Aucun log détecté pour le moment",
            state = if (logs.isNotEmpty()) FieldTestState.Ok else FieldTestState.Warning,
        ),
        FieldTestItem(
            title = "Dépôt MSurvey",
            detail = "Formulaire Orange intégré dans MSurvey",
            state = FieldTestState.Ok,
        ),
        FieldTestItem(
            title = "Clé USB",
            detail = usbState.destinationLabel,
            state = when (usbState) {
                UsbDestinationState.Ready -> FieldTestState.Ok
                UsbDestinationState.MountedNotAuthorized -> FieldTestState.Warning
                UsbDestinationState.NotConfigured,
                UsbDestinationState.Unavailable -> FieldTestState.Blocking
            },
        ),
        FieldTestItem(
            title = "Drone",
            detail = if (djiSdkState.hasConnectedDrone()) {
                djiSdkState.productId?.let { "Connecté · modèle #$it" } ?: "Connecté"
            } else {
                djiSdkState.message
            },
            state = if (djiSdkState.hasConnectedDrone()) FieldTestState.Ok else FieldTestState.Warning,
        ),
        FieldTestItem(
            title = "Médias drone",
            detail = if (settings.mediaFolderUri.isNotBlank()) {
                settings.mediaFolderLabel.ifBlank { "Ancienne autorisation média locale" }
            } else {
                "Les photos et vidéos seront récupérées depuis le drone connecté"
            },
            state = FieldTestState.Warning,
        ),
    )
    val blocking = readyItems.count { it.state == FieldTestState.Blocking }
    val warnings = readyItems.count { it.state == FieldTestState.Warning }
    GlassCard(containerAlpha = 0.54f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pré-test terrain",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = when {
                            blocking > 0 -> "$blocking point(s) bloquant(s) avant le test"
                            warnings > 0 -> "$warnings point(s) à vérifier sur la RC"
                            else -> "Prêt pour le test de demain"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                }
                FlightExportStatusBadge(
                    text = if (blocking == 0) "TESTABLE" else "À PRÉPARER",
                    color = if (blocking == 0) Color(0xFF33D16D) else Color(0xFFFFB020),
                )
            }
            readyItems.forEach { item ->
                FieldTestReadinessRow(item)
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val compact = maxWidth < 620.dp
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldTestActionRow(
                            onPickFolder = onPickFolder,
                            onPickUsbFolder = onPickUsbFolder,
                            onRefreshLogs = onRefreshLogs,
                            onTestUsb = onTestUsb,
                            compact = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            SecondaryFieldButton("MSurvey", { onOpenModule(AppModule.SYNC_LOG) }, Modifier.weight(1f))
                            SecondaryFieldButton("Export des logs", { onOpenModule(AppModule.USB_KIT) }, Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FieldTestActionRow(
                            onPickFolder = onPickFolder,
                            onPickUsbFolder = onPickUsbFolder,
                            onRefreshLogs = onRefreshLogs,
                            onTestUsb = onTestUsb,
                            compact = false,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            SecondaryFieldButton("Ouvrir MSurvey", { onOpenModule(AppModule.SYNC_LOG) }, Modifier.weight(1f))
                            SecondaryFieldButton("Ouvrir Export des logs", { onOpenModule(AppModule.USB_KIT) }, Modifier.weight(1f))
                        }
                    }
                }
            }
            Text(
                text = "Parcours conseillé demain : ouvrir l’app, vérifier le diagnostic, brancher la clé, autoriser la racine de la clé si Android le demande, déposer le log via MSurvey ou lancer Export des logs.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.70f),
            )
        }
    }
}

@Composable
private fun FieldTestActionRow(
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onRefreshLogs: () -> Unit,
    onTestUsb: () -> Unit,
    compact: Boolean,
) {
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryFieldButton("Dossier logs", onPickFolder, Modifier.weight(1f))
                SecondaryFieldButton("Clé USB", onPickUsbFolder, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SecondaryFieldButton("Scanner", onRefreshLogs, Modifier.weight(1f))
                SecondaryFieldButton("Tester USB", onTestUsb, Modifier.weight(1f))
            }
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SecondaryFieldButton("Dossier logs", onPickFolder, Modifier.weight(1f))
            SecondaryFieldButton("Clé USB", onPickUsbFolder, Modifier.weight(1f))
            SecondaryFieldButton("Scanner", onRefreshLogs, Modifier.weight(1f))
            SecondaryFieldButton("Tester USB", onTestUsb, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FieldTestReadinessRow(item: FieldTestItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(MaterialTheme.shapes.small)
                .background(item.state.color),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.state.label,
            style = MaterialTheme.typography.labelSmall,
            color = item.state.color,
            fontWeight = FontWeight.Bold,
        )
    }
}

private data class FieldTestItem(
    val title: String,
    val detail: String,
    val state: FieldTestState,
)

private enum class FieldTestState(val label: String, val color: Color) {
    Ok("OK", Color(0xFF33D16D)),
    Warning("À vérifier", Color(0xFFFFB020)),
    Blocking("Bloquant", Color(0xFFFF4D4D)),
}

private enum class FolderReadiness {
    Missing,
    Ready,
    Unavailable,
}

@Composable
private fun AppearanceCard(
    currentThemeId: String,
    onThemeChange: (String) -> Unit,
) {
    GlassCard(containerAlpha = 0.50f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Apparence", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(
                text = "Choisir un fond intégré à l’app.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f),
            )
            CompagnonThemes.chunked(2).forEach { rowThemes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowThemes.forEach { theme ->
                        ThemeChoice(
                            theme = theme,
                            selected = theme.id == currentThemeId,
                            onClick = { onThemeChange(theme.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(2 - rowThemes.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeChoice(
    theme: CompagnonTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(92.dp)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = selected,
                accent = Orange,
                containerAlpha = if (selected) 0.18f else 0.11f,
                borderAlpha = if (selected) 0.52f else 0.22f,
                highlightAlpha = 0.14f,
            )
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Brush.verticalGradient(theme.colors)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = theme.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (selected) {
                StatusIndicator(HomeStatusState.Ok)
            }
        }
    }
}

@Composable
private fun UsbKitContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    usbKitProgress: UsbKitProgress,
    actionMessage: String,
    onSyncNow: () -> Unit,
    onExportLog: (Long?) -> Unit,
    onRecoverDroneMedia: (Long?) -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    onOpenMsurvey: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestLog = logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    var selectedLogId by remember { mutableStateOf<Long?>(null) }
    var journalExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(latestLog?.id) {
        if (selectedLogId == null && latestLog != null) {
            selectedLogId = latestLog.id
        }
    }
    val selectedLog = selectedLogId?.let { id -> logs.firstOrNull { it.id == id } } ?: latestLog
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 2.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FlightExportUsbCockpit(
                settings = settings,
                logs = logs,
                selectedLog = selectedLog,
                djiSdkState = djiSdkState,
                progress = usbKitProgress,
                onLogSelected = { selectedLogId = it },
                onExportSelected = { onExportLog(selectedLog?.id) },
                onRecoverDroneMedia = { onRecoverDroneMedia(selectedLog?.id) },
                onSyncNow = onSyncNow,
                onOpenMsurvey = onOpenMsurvey,
                onOpenSettings = onOpenSettings,
            )
        }
        if (journalExpanded) {
            item {
                FlightExportUsbJournalActions(
                    onRetryErrors = onRetryErrors,
                    onCheckUpdate = onCheckUpdate,
                )
            }
            item {
                ActivityTitle()
            }
            items(events.take(8), key = { it.id }) { event ->
                ActivityRow(event)
            }
            item {
                HistoryTitle()
            }
            items(logs.take(8), key = { it.id }) { log ->
                LogRow(log)
            }
        }
        item {
            CopyrightFooter()
        }
    }
}

@Composable
private fun FlightExportUsbCockpit(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    selectedLog: LogFileEntity?,
    djiSdkState: DjiSdkState,
    progress: UsbKitProgress,
    onLogSelected: (Long) -> Unit,
    onExportSelected: () -> Unit,
    onRecoverDroneMedia: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenMsurvey: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    UsbKitProgressCard(
        settings = settings,
        logs = logs,
        djiSdkState = djiSdkState,
        progress = progress,
        selectedLog = selectedLog,
        onLogSelected = onLogSelected,
        onOpenSettings = onOpenSettings,
        onSyncNow = onSyncNow,
        onOpenMsurvey = onOpenMsurvey,
        onExportSelected = onExportSelected,
        onRecoverDroneMedia = onRecoverDroneMedia,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 430.dp),
    )
}

@Composable
private fun FlightExportMissionPanel(
    @Suppress("UNUSED_PARAMETER") settings: AppSettings,
    logs: List<LogFileEntity>,
    selectedLog: LogFileEntity?,
    @Suppress("UNUSED_PARAMETER") djiSdkState: DjiSdkState,
    onLogSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var logMenuExpanded by remember { mutableStateOf(false) }
    val selectableLogs = remember(logs) {
        logs.sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }.take(40)
    }
    GlassCard(modifier = modifier, containerAlpha = 0.42f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .glassControlSurface(
                            shape = MaterialTheme.shapes.medium,
                            active = selectedLog != null,
                            accent = Orange,
                            containerAlpha = 0.12f,
                            borderAlpha = 0.22f,
                            highlightAlpha = 0.14f,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CompagnonIcon(AppModule.PLAY_LOG, Modifier.size(42.dp), thin = true, color = Orange)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Vol sélectionné",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = if (selectedLog != null) {
                            "Dernier vol préselectionné, modifiable manuellement."
                        } else {
                            "Aucun log disponible : actualiser le dossier des logs."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.70f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MissionInfoPill(
                    label = "Début",
                    value = selectedLog?.flightStartTimeMillis?.let(::formatShortTime)
                        ?: selectedLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatShortTime)
                        ?: "--:--",
                    modifier = Modifier.weight(1f),
                    accentColor = Orange,
                )
                MissionInfoPill(
                    label = "Date",
                    value = selectedLog?.flightStartTimeMillis?.let(::formatShortDate)
                        ?: selectedLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatShortDate)
                        ?: "--",
                    modifier = Modifier.weight(1f),
                )
                MissionInfoPill(
                    label = "Durée",
                    value = selectedLog?.flightDurationSeconds?.let(::formatDuration) ?: "--",
                    modifier = Modifier.weight(1f),
                )
            }

            Box {
                Button(
                    onClick = { logMenuExpanded = true },
                    enabled = selectableLogs.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .glassControlSurface(
                            shape = MaterialTheme.shapes.small,
                            active = selectableLogs.isNotEmpty(),
                            accent = Orange,
                            containerAlpha = if (selectableLogs.isNotEmpty()) 0.14f else 0.06f,
                            borderAlpha = if (selectableLogs.isNotEmpty()) 0.30f else 0.14f,
                            highlightAlpha = 0.16f,
                        ),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = Color.White.copy(alpha = 0.42f),
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedLog?.fileName ?: "Choisir un log de vol",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (selectedLog == null) "Aucun log disponible" else "Toucher pour choisir un autre vol",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                DropdownMenu(
                    expanded = logMenuExpanded,
                    onDismissRequest = { logMenuExpanded = false },
                    modifier = Modifier.glassMenuSurface(MaterialTheme.shapes.small),
                ) {
                    selectableLogs.forEach { log ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = log.fileName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = log.flightStartTimeMillis?.let(::formatDate)
                                            ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate)
                                            ?: "Date inconnue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.66f),
                                    )
                                }
                            },
                            onClick = {
                                onLogSelected(log.id)
                                logMenuExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightExportUsbActionsPanel(
    actionMessage: String,
    @Suppress("UNUSED_PARAMETER") eventCount: Int,
    @Suppress("UNUSED_PARAMETER") journalExpanded: Boolean,
    progress: UsbKitProgress,
    selectedLog: LogFileEntity?,
    @Suppress("UNUSED_PARAMETER") onToggleJournal: () -> Unit,
    onRecoverDroneMedia: () -> Unit,
    onExportSelected: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenMsurvey: () -> Unit,
    onOpenSettings: () -> Unit,
    usbReady: Boolean,
    usbState: UsbDestinationState,
    modifier: Modifier = Modifier,
) {
    val hasLog = selectedLog != null
    val busy = progress.stage == UsbKitStage.DETECTING_MEDIA || progress.stage == UsbKitStage.TRANSFERRING
    var pendingExportConfirmation by remember { mutableStateOf<FlightExportConfirmMode?>(null) }
    val mediaCountText = when {
        progress.detectedMediaCount > 0 -> "${progress.detectedMediaCount} média(s) détecté(s)"
        progress.totalDroneMediaCount > 0 -> "${progress.totalDroneMediaCount} média(s) à analyser"
        else -> "Les médias du même créneau seront recherchés"
    }
    val confirmedExport = {
        when (pendingExportConfirmation) {
            FlightExportConfirmMode.LogOnly -> onExportSelected()
            FlightExportConfirmMode.LogAndMedia -> onRecoverDroneMedia()
            null -> Unit
        }
        pendingExportConfirmation = null
    }
    val primaryText = when {
        !hasLog -> "Actualiser les logs"
        usbState == UsbDestinationState.NotConfigured -> "Configurer la clé"
        usbState == UsbDestinationState.MountedNotAuthorized -> "Autoriser la clé"
        usbState == UsbDestinationState.Unavailable -> "Reconnecter la clé"
        progress.stage == UsbKitStage.DETECTING_MEDIA -> "Scan en cours"
        progress.stage == UsbKitStage.TRANSFERRING -> "Transfert ${progress.transferPercent}%"
        progress.stage == UsbKitStage.DONE -> "Exporter à nouveau"
        else -> "Exporter log + médias"
    }
    val primaryAction = when {
        !hasLog -> onSyncNow
        !usbReady -> onOpenSettings
        else -> { { pendingExportConfirmation = FlightExportConfirmMode.LogAndMedia } }
    }
    val guidance = when {
        !hasLog -> "Aucun log sélectionné : scanner le dossier log de vol."
        usbState == UsbDestinationState.NotConfigured -> "Clé USB non configurée : choisir la racine de la clé dans Réglages."
        usbState == UsbDestinationState.MountedNotAuthorized -> "Clé détectée : autoriser son dossier racine pour permettre l’écriture."
        usbState == UsbDestinationState.Unavailable -> "Clé absente ou démontée : rebrancher la clé puis attendre quelques secondes."
        progress.stage == UsbKitStage.TRANSFERRING -> "Transfert en cours : garder la radiocommande et le drone allumés."
        progress.stage == UsbKitStage.DONE -> "Export terminé : vérifier le dossier OrangeDroneCompagnon sur la clé."
        else -> "Prêt : exporter le log et les médias du vol sélectionné."
    }
    GlassCard(modifier = modifier, containerAlpha = 0.42f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Actions rapides",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
            )

            if (actionMessage.isNotBlank()) {
                Text(
                    text = actionMessage,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
                    color = Color.White.copy(alpha = 0.70f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = guidance,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
            )

            FlightExportLiveProgress(progress = progress)

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stacked = maxWidth < 620.dp
                if (stacked) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlightExportPrimaryButton(
                            text = primaryText,
                            onClick = primaryAction,
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SecondaryFieldButton(
                                text = "MSurvey",
                                onClick = onOpenMsurvey,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !busy,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FlightExportPrimaryButton(
                            text = primaryText,
                            onClick = primaryAction,
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                        )
                        SecondaryFieldButton(
                            text = "MSurvey",
                            onClick = onOpenMsurvey,
                            modifier = Modifier.weight(1f),
                            enabled = !busy,
                        )
                    }
                }
            }
        }
    }
    if (pendingExportConfirmation != null && selectedLog != null) {
        FlightExportConfirmationDialog(
            mode = pendingExportConfirmation ?: FlightExportConfirmMode.LogAndMedia,
            log = selectedLog,
            usbState = usbState,
            mediaCountText = mediaCountText,
            onDismiss = { pendingExportConfirmation = null },
            onConfirm = confirmedExport,
        )
    }
}

@Composable
private fun FlightExportConfirmationDialog(
    mode: FlightExportConfirmMode,
    log: LogFileEntity,
    usbState: UsbDestinationState,
    mediaCountText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF07161B),
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(alpha = 0.82f),
        title = {
            Text(
                text = if (mode == FlightExportConfirmMode.LogOnly) "Prêt pour l’export du log" else "Prêt pour l’export",
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (mode == FlightExportConfirmMode.LogOnly) {
                        "Exporter uniquement ce log de vol vers la clé USB ?"
                    } else {
                        "Exporter le log de vol et les médias associés au créneau suivant ?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                )
                FlightExportDialogLine("Log", log.fileName)
                FlightExportDialogLine("Début", log.flightStartTimeMillis?.let(::formatDate) ?: "Date de début inconnue")
                FlightExportDialogLine("Fin", flightEndDateLabel(log))
                FlightExportDialogLine("Durée", log.flightDurationSeconds?.let(::formatDuration) ?: "Durée inconnue")
                if (mode == FlightExportConfirmMode.LogAndMedia) {
                    FlightExportDialogLine("Médias", mediaCountText)
                }
                FlightExportDialogLine("Destination", usbState.destinationLabel)
                Text(
                    text = if (mode == FlightExportConfirmMode.LogOnly) {
                        "Le fichier DJI original ne sera jamais modifié ni supprimé."
                    } else {
                        "La copie peut prendre du temps si les médias doivent être récupérés depuis le drone. Garder la radiocommande, le drone et la clé USB branchés pendant l’opération."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.64f),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color.White),
            ) {
                Text(if (mode == FlightExportConfirmMode.LogOnly) "Exporter le log" else "Lancer l’export", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color.White.copy(alpha = 0.76f))
            }
        },
    )
}

@Composable
private fun FlightExportDialogLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.52f),
            modifier = Modifier.width(82.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun flightEndDateLabel(log: LogFileEntity): String {
    val start = log.flightStartTimeMillis ?: return "Date de fin inconnue"
    val durationMillis = ((log.flightDurationSeconds ?: return "Date de fin inconnue") * 1000.0)
        .toLong()
        .coerceAtLeast(0L)
    return formatDate(start + durationMillis)
}

@Composable
private fun FlightExportMediaPreviewCard(
    settings: AppSettings,
    selectedLog: LogFileEntity?,
    djiSdkState: DjiSdkState,
    progress: UsbKitProgress,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preview by produceState<FlightExportMediaPreview>(
        initialValue = FlightExportMediaPreview.Loading,
        settings.mediaFolderUri,
        selectedLog?.id,
        selectedLog?.flightStartTimeMillis,
        selectedLog?.flightDurationSeconds,
        progress.detectedMediaCount,
        progress.totalDroneMediaCount,
    ) {
        value = withContext(Dispatchers.IO) {
            buildFlightExportMediaPreview(context, settings, selectedLog)
        }
    }
    val accent = when (preview) {
        is FlightExportMediaPreview.Ready -> Color(0xFF33D16D)
        is FlightExportMediaPreview.Empty -> Color(0xFFFFB020)
        is FlightExportMediaPreview.Loading -> Orange
        else -> Color.White.copy(alpha = 0.70f)
    }

    GlassCard(modifier = modifier, containerAlpha = 0.36f, borderAlpha = 0.18f, highlightAlpha = 0.05f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Médias du vol",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = selectedLog?.fileName ?: "Aucun log sélectionné",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.62f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                FlightExportStatusBadge(
                    text = when (preview) {
                        is FlightExportMediaPreview.Ready -> "Trouvés"
                        is FlightExportMediaPreview.Empty -> "Aucun"
                        is FlightExportMediaPreview.NotConfigured -> "Drone"
                        is FlightExportMediaPreview.NoWindow -> "Timing"
                        is FlightExportMediaPreview.NoLog -> "Log"
                        is FlightExportMediaPreview.Loading -> "Scan"
                    },
                    color = accent,
                )
            }

            when (val state = preview) {
                FlightExportMediaPreview.Loading -> {
                    Text(
                        text = "Analyse des médias disponibles pour ce vol...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                    FlightExportMiniMeter(value = 0.42f, color = Orange)
                }
                FlightExportMediaPreview.NoLog -> {
                    Text(
                        text = "Sélectionner un log de vol pour afficher les médias liés.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                }
                FlightExportMediaPreview.NotConfigured -> {
                    Text(
                        text = "Les photos et vidéos seront récupérées depuis le drone connecté au moment de l’export.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                    SecondaryFieldButton(
                        text = "Vérifier drone / USB",
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is FlightExportMediaPreview.NoWindow -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                }
                is FlightExportMediaPreview.Empty -> {
                    FlightExportPreviewSummary(
                        windowLabel = state.windowLabel,
                        photoCount = 0,
                        videoCount = 0,
                        totalBytes = 0,
                    )
                    Text(
                        text = if (djiSdkState.hasConnectedDrone()) {
                            "Aucun média local trouvé. Le module peut tenter une récupération depuis le drone branché."
                        } else {
                            "Aucun média local trouvé dans le créneau. Brancher le drone si les médias sont restés sur sa carte."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.66f),
                    )
                }
                is FlightExportMediaPreview.Ready -> {
                    FlightExportPreviewSummary(
                        windowLabel = state.windowLabel,
                        photoCount = state.photoCount,
                        videoCount = state.videoCount,
                        totalBytes = state.totalBytes,
                    )
                    state.samples.take(4).forEach { item ->
                        FlightExportMediaPreviewRow(item)
                    }
                    if (progress.detectedMediaCount > 0 || progress.totalDroneMediaCount > 0) {
                        Text(
                            text = "Scan drone : ${progress.detectedMediaCount}/${progress.totalDroneMediaCount} média(s) prêts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.70f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightExportPreviewSummary(
    windowLabel: String,
    photoCount: Int,
    videoCount: Int,
    totalBytes: Long,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = windowLabel,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.66f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlightExportPreviewMetric("Photos", photoCount.toString(), Modifier.weight(1f))
            FlightExportPreviewMetric("Vidéos", videoCount.toString(), Modifier.weight(1f))
            FlightExportPreviewMetric("Taille", formatStorageBytes(totalBytes), Modifier.weight(1f))
        }
    }
}

@Composable
private fun FlightExportPreviewMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.62f),
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 17.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightExportMediaPreviewRow(item: FlightExportMediaPreviewItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (item.kind == "Vidéo") Orange else Color(0xFF33D16D)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${item.kind} · ${formatStorageBytes(item.sizeBytes)} · ${formatShortTime(item.modifiedMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.58f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FlightExportMiniMeter(value: Float, color: Color) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
    ) {
        val y = size.height / 2f
        drawLine(
            color = Color.White.copy(alpha = 0.10f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width * value.coerceIn(0f, 1f), y),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
        )
    }
}

private sealed interface FlightExportMediaPreview {
    data object Loading : FlightExportMediaPreview
    data object NoLog : FlightExportMediaPreview
    data object NotConfigured : FlightExportMediaPreview
    data class NoWindow(val message: String) : FlightExportMediaPreview
    data class Empty(val windowLabel: String) : FlightExportMediaPreview
    data class Ready(
        val windowLabel: String,
        val photoCount: Int,
        val videoCount: Int,
        val totalBytes: Long,
        val samples: List<FlightExportMediaPreviewItem>,
    ) : FlightExportMediaPreview
}

private data class FlightExportMediaPreviewItem(
    val name: String,
    val sizeBytes: Long,
    val modifiedMillis: Long,
    val kind: String,
)

private fun buildFlightExportMediaPreview(
    context: Context,
    settings: AppSettings,
    log: LogFileEntity?,
): FlightExportMediaPreview {
    if (log == null) return FlightExportMediaPreview.NoLog
    if (settings.mediaFolderUri.isBlank()) return FlightExportMediaPreview.NotConfigured
    val window = flightExportMediaWindowMillis(log)
        ?: return FlightExportMediaPreview.NoWindow("Date ou durée du vol indisponible : impossible de rapprocher les médias.")
    val mediaRoot = DocumentFile.fromTreeUri(context, Uri.parse(settings.mediaFolderUri))
        ?: return FlightExportMediaPreview.NoWindow("Médias locaux inaccessibles : l’export drone reste prioritaire.")

    var photoCount = 0
    var videoCount = 0
    var totalBytes = 0L
    val samples = mutableListOf<FlightExportMediaPreviewItem>()
    mediaRoot.walkPreviewFiles(FLIGHT_EXPORT_MEDIA_PREVIEW_MAX_FILES).forEach { media ->
        val name = media.name ?: return@forEach
        if (!isFlightExportPreviewMedia(name)) return@forEach
        val modified = media.lastModified()
        if (modified !in window.first..window.second) return@forEach
        val kind = flightExportPreviewMediaKind(name)
        if (kind == "Vidéo") videoCount++ else photoCount++
        val size = media.length().coerceAtLeast(0L)
        totalBytes += size
        if (samples.size < 6) {
            samples += FlightExportMediaPreviewItem(
                name = name,
                sizeBytes = size,
                modifiedMillis = modified,
                kind = kind,
            )
        }
    }
    val windowLabel = "Créneau ${formatShortTime(window.first)} - ${formatShortTime(window.second)}"
    return if (photoCount + videoCount == 0) {
        FlightExportMediaPreview.Empty(windowLabel)
    } else {
        FlightExportMediaPreview.Ready(
            windowLabel = windowLabel,
            photoCount = photoCount,
            videoCount = videoCount,
            totalBytes = totalBytes,
            samples = samples,
        )
    }
}

private fun flightExportMediaWindowMillis(log: LogFileEntity): Pair<Long, Long>? {
    val start = log.flightStartTimeMillis ?: return null
    val durationMillis = ((log.flightDurationSeconds ?: return null) * 1000.0)
        .toLong()
        .coerceAtLeast(0L)
    return (start - FLIGHT_EXPORT_MEDIA_BEFORE_MARGIN_MS) to (start + durationMillis + FLIGHT_EXPORT_MEDIA_AFTER_MARGIN_MS)
}

private fun DocumentFile.walkPreviewFiles(maxFiles: Int): Sequence<DocumentFile> = sequence {
    val stack = ArrayDeque<DocumentFile>()
    stack.add(this@walkPreviewFiles)
    var emitted = 0
    while (stack.isNotEmpty() && emitted < maxFiles) {
        val folder = stack.removeLast()
        folder.listFiles().forEach { child ->
            when {
                child.isDirectory -> stack.add(child)
                child.isFile && emitted < maxFiles -> {
                    emitted++
                    yield(child)
                }
            }
        }
    }
}

private fun isFlightExportPreviewMedia(name: String): Boolean {
    val lower = name.lowercase(Locale.US)
    return lower.endsWith(".jpg") ||
        lower.endsWith(".jpeg") ||
        lower.endsWith(".png") ||
        lower.endsWith(".dng") ||
        lower.endsWith(".mp4") ||
        lower.endsWith(".mov")
}

private fun flightExportPreviewMediaKind(name: String): String {
    val lower = name.lowercase(Locale.US)
    return if (lower.endsWith(".mp4") || lower.endsWith(".mov")) "Vidéo" else "Photo"
}

private const val FLIGHT_EXPORT_MEDIA_PREVIEW_MAX_FILES = 2_500
private const val FLIGHT_EXPORT_MEDIA_BEFORE_MARGIN_MS = 5 * 60 * 1000L
private const val FLIGHT_EXPORT_MEDIA_AFTER_MARGIN_MS = 10 * 60 * 1000L

private enum class FlightExportConfirmMode {
    LogOnly,
    LogAndMedia,
}

@Composable
private fun FlightExportPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                active = true,
                accent = Orange,
                containerAlpha = if (enabled) 0.22f else 0.08f,
                borderAlpha = if (enabled) 0.58f else 0.18f,
                highlightAlpha = 0.18f,
            ),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.72f),
        ),
    ) {
        Text(text, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun FlightExportActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                containerAlpha = if (enabled) 0.13f else 0.06f,
                borderAlpha = if (enabled) 0.22f else 0.12f,
                highlightAlpha = 0.14f,
            ),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.38f),
        ),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FlightExportLiveProgress(progress: UsbKitProgress) {
    val busy = progress.stage == UsbKitStage.DETECTING_MEDIA || progress.stage == UsbKitStage.TRANSFERRING
    val infiniteTransition = rememberInfiniteTransition(label = "flight-export-progress")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan-progress",
    )
    val value = when {
        progress.stage == UsbKitStage.TRANSFERRING -> progress.transferPercent.coerceIn(0, 100) / 100f
        progress.stage == UsbKitStage.DETECTING_MEDIA -> scanProgress
        progress.stage == UsbKitStage.DONE -> 1f
        progress.stage == UsbKitStage.MEDIA_READY -> 0.74f
        progress.stage == UsbKitStage.LOG_READY -> 0.24f
        else -> 0f
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (progress.stage == UsbKitStage.TRANSFERRING) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Transfert total",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${progress.transferPercent.coerceIn(0, 100)}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                    color = Orange,
                    fontWeight = FontWeight.Black,
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(MaterialTheme.shapes.small),
        ) {
            val stroke = size.height
            val y = size.height / 2f
            drawLine(
                color = Color.White.copy(alpha = 0.10f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            if (value > 0.01f) {
                drawLine(
                    color = when {
                        progress.stage == UsbKitStage.ERROR -> Color(0xFFFF4D4D)
                        busy -> Orange
                        progress.stage == UsbKitStage.DONE -> Color(0xFF33D16D)
                        else -> Color.White.copy(alpha = 0.44f)
                    },
                    start = Offset(0f, y),
                    end = Offset(size.width * value.coerceIn(0f, 1f), y),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
        Text(
            text = when (progress.stage) {
                UsbKitStage.IDLE -> "Prêt à lancer une action."
                UsbKitStage.LOG_READY -> "Log sélectionné, en attente de la suite."
                UsbKitStage.DETECTING_MEDIA -> "Analyse des médias du vol..."
                UsbKitStage.MEDIA_READY -> "${progress.detectedMediaCount} média(s) prêt(s) sur ${progress.totalDroneMediaCount}."
                UsbKitStage.NO_MEDIA -> "Aucun média trouvé dans le timing du vol."
                UsbKitStage.TRANSFERRING -> "Copie en cours : ${progress.transferPercent}%"
                UsbKitStage.DONE -> "Export terminé."
                UsbKitStage.ERROR -> "Action bloquée : ${progress.message}"
            },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.64f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (progress.stage == UsbKitStage.TRANSFERRING && progress.currentMediaTotal > 0) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${progress.currentMediaIndex}/${progress.currentMediaTotal} · ${progress.currentMediaName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "${progress.currentMediaPercent.coerceIn(0, 100)}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.White.copy(alpha = 0.88f),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(MaterialTheme.shapes.small),
                ) {
                    val stroke = size.height
                    val y = size.height / 2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.10f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.84f),
                        start = Offset(0f, y),
                        end = Offset(size.width * (progress.currentMediaPercent.coerceIn(0, 100) / 100f), y),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round,
                    )
                }
                val mediaSizeText = when {
                    progress.currentMediaTotalBytes > 0 ->
                        "${formatBytes(progress.currentMediaBytes)} / ${formatBytes(progress.currentMediaTotalBytes)}"
                    progress.currentMediaBytes > 0 -> formatBytes(progress.currentMediaBytes)
                    else -> "Taille en cours de calcul"
                }
                val speedText = if (progress.currentMediaSpeedBytesPerSecond > 0) {
                    "${formatBytes(progress.currentMediaSpeedBytesPerSecond)}/s"
                } else {
                    "Débit en calcul"
                }
                val etaText = progress.currentMediaRemainingSeconds?.let(::formatDurationShort) ?: "Temps restant --"
                Text(
                    text = "$mediaSizeText · $speedText · $etaText",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FlightExportStatusMiniChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                accent = color,
                containerAlpha = 0.10f,
                borderAlpha = 0.14f,
                highlightAlpha = 0.10f,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.58f),
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightExportJournalChip(
    text: String,
    count: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                containerAlpha = 0.12f,
                borderAlpha = 0.18f,
                highlightAlpha = 0.12f,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 0) "$text · $count" else text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun FlightExportUsbJournalActions(
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FlightExportActionButton(
            text = "Réessayer erreurs",
            onClick = onRetryErrors,
            modifier = Modifier.weight(1f),
        )
        FlightExportActionButton(
            text = "Mise à jour",
            onClick = onCheckUpdate,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MissionInfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.07f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = Color.White.copy(alpha = 0.58f),
            maxLines = 1,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            color = accentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UsbKitProgressCard(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    progress: UsbKitProgress,
    selectedLog: LogFileEntity?,
    onLogSelected: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenMsurvey: () -> Unit,
    onExportSelected: () -> Unit,
    onRecoverDroneMedia: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) { usbDestinationState(context, settings.usbExportUri) }
    val usbReady = usbState == UsbDestinationState.Ready
    val mediaDetected = progress.stage == UsbKitStage.MEDIA_READY ||
        progress.stage == UsbKitStage.TRANSFERRING ||
        progress.stage == UsbKitStage.DONE
    val transferring = progress.stage == UsbKitStage.TRANSFERRING
    val selectableLogs = remember(logs) {
        logs.sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }.take(40)
    }
    var selectedStep by remember { mutableStateOf(FlightExportStepKey.Log) }
    LaunchedEffect(progress.stage) {
        selectedStep = when (progress.stage) {
            UsbKitStage.DETECTING_MEDIA,
            UsbKitStage.MEDIA_READY,
            UsbKitStage.NO_MEDIA -> FlightExportStepKey.Media
            UsbKitStage.TRANSFERRING,
            UsbKitStage.DONE,
            UsbKitStage.ERROR -> FlightExportStepKey.Export
            else -> selectedStep
        }
    }
    val logDate = selectedLog?.flightStartTimeMillis?.let(::formatShortDate)
        ?: selectedLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatShortDate)
    val logTime = selectedLog?.flightStartTimeMillis?.let(::formatShortTime)
        ?: selectedLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatShortTime)
    val logDuration = selectedLog?.flightDurationSeconds?.let(::formatDuration)
    val logMeta = listOfNotNull(logDate, logTime, logDuration, selectedLog?.sizeBytes?.let(::formatBytes))
        .joinToString(" · ")
    val steps = listOf(
        FlightExportStep(
            key = FlightExportStepKey.Log,
            label = "Log",
            detail = when {
                progress.selectedLogName.isNotBlank() -> progress.selectedLogName
                selectedLog != null -> selectedLog.fileName
                else -> "Aucun log disponible"
            },
            extraDetail = logMeta.ifBlank {
                if (selectableLogs.isNotEmpty()) "Dernier vol sélectionné automatiquement" else "Scanner ou choisir un dossier de logs"
            },
            status = when {
                logTime != null -> logTime
                progress.selectedLogName.isNotBlank() || selectedLog != null -> "PRÊT"
                else -> "À FAIRE"
            },
            state = if (progress.selectedLogName.isNotBlank() || selectedLog != null) FlightExportStepState.Done else FlightExportStepState.Warning,
            iconModule = AppModule.PLAY_LOG,
            actionLabel = if (selectableLogs.isNotEmpty()) "Choisir" else null,
        ),
        FlightExportStep(
            key = FlightExportStepKey.Usb,
            label = "USB",
            detail = usbState.timelineDetail,
            extraDetail = settings.usbExportLabel.ifBlank { usbState.destinationLabel },
            status = usbState.timelineStatus,
            state = if (usbReady) FlightExportStepState.Done else FlightExportStepState.Warning,
            iconModule = AppModule.USB_KIT,
        ),
        FlightExportStep(
            key = FlightExportStepKey.Media,
            label = "Médias",
            detail = when {
                progress.stage == UsbKitStage.DETECTING_MEDIA -> "Détection en cours..."
                mediaDetected -> "${progress.detectedMediaCount} média(s) détecté(s)"
                progress.stage == UsbKitStage.NO_MEDIA -> "0 média dans le timing"
                djiSdkState.hasConnectedDrone() -> "Drone connecté, prêt à scanner"
                else -> djiSdkState.message
            },
            status = when {
                progress.stage == UsbKitStage.DETECTING_MEDIA -> "SCAN"
                mediaDetected -> "PRÊT"
                progress.stage == UsbKitStage.NO_MEDIA -> "VIDE"
                else -> "ATTENTE"
            },
            extraDetail = when {
                progress.totalDroneMediaCount > 0 -> "${progress.detectedMediaCount}/${progress.totalDroneMediaCount} fichier(s)"
                else -> "Photos/vidéos du même créneau horaire"
            },
            state = when {
                progress.stage == UsbKitStage.DETECTING_MEDIA -> FlightExportStepState.Active
                mediaDetected -> FlightExportStepState.Done
                progress.stage == UsbKitStage.NO_MEDIA || progress.stage == UsbKitStage.ERROR -> FlightExportStepState.Warning
                else -> FlightExportStepState.Idle
            },
            iconModule = AppModule.MEDIA,
        ),
        FlightExportStep(
            key = FlightExportStepKey.Export,
            label = "Export",
            detail = when {
                transferring && progress.currentMediaTotal > 0 ->
                    "${progress.currentMediaIndex}/${progress.currentMediaTotal} · ${progress.currentMediaName}"
                transferring -> "${progress.transferPercent}%"
                progress.stage == UsbKitStage.DONE -> "Terminé"
                mediaDetected -> "Prêt à transférer"
                progress.stage == UsbKitStage.LOG_READY -> "Log prêt"
                else -> "En attente"
            },
            status = when {
                transferring -> "${progress.transferPercent}%"
                progress.stage == UsbKitStage.DONE -> "OK"
                else -> "0%"
            },
            extraDetail = if (transferring && progress.currentMediaTotalBytes > 0) {
                "${formatBytes(progress.currentMediaBytes)} / ${formatBytes(progress.currentMediaTotalBytes)}"
            } else {
                "Copie vers la clé USB"
            },
            state = when {
                transferring -> FlightExportStepState.Active
                progress.stage == UsbKitStage.DONE -> FlightExportStepState.Done
                progress.stage == UsbKitStage.ERROR -> FlightExportStepState.Warning
                else -> FlightExportStepState.Idle
            },
            iconModule = AppModule.USB_KIT,
        ),
    )
    val progressRatio = when {
        progress.stage == UsbKitStage.DONE -> 1f
        transferring -> (0.74f + progress.transferPercent.coerceIn(0, 100) / 400f).coerceIn(0.74f, 0.99f)
        mediaDetected -> 0.74f
        usbReady -> 0.49f
        selectedLog != null || progress.selectedLogName.isNotBlank() -> 0.24f
        else -> 0f
    }
    GlassCard(modifier = modifier, containerAlpha = 0.42f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
    Column(
        modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Progression de l’export",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                FlightExportStatusBadge(
                    text = when {
                        progress.stage == UsbKitStage.DONE -> "TERMINÉ"
                        transferring -> "EN COURS"
                        progress.stage == UsbKitStage.ERROR -> "ERREUR"
                        else -> "PRÉPARATION"
                    },
                    color = when {
                        progress.stage == UsbKitStage.DONE -> Color(0xFF33D16D)
                        progress.stage == UsbKitStage.ERROR -> Color(0xFFFF4D4D)
                        transferring -> Orange
                        else -> Color.White.copy(alpha = 0.72f)
                    },
                )
            }
            FlightExportTimeline(
                steps = steps,
                progressRatio = progressRatio,
                transferring = transferring,
                selectedStep = selectedStep,
                onStepSelected = { selectedStep = it },
            )
            FlightExportStepDetailPanel(
                selectedStep = selectedStep,
                selectedLog = selectedLog,
                selectableLogs = selectableLogs,
                settings = settings,
                usbState = usbState,
                djiSdkState = djiSdkState,
                progress = progress,
                onLogSelected = onLogSelected,
                onOpenSettings = onOpenSettings,
                onSyncNow = onSyncNow,
                onOpenMsurvey = onOpenMsurvey,
                onExportSelected = onExportSelected,
                onRecoverDroneMedia = onRecoverDroneMedia,
            )
        }
    }
}

@Composable
private fun FlightExportStepDetailPanel(
    selectedStep: FlightExportStepKey,
    selectedLog: LogFileEntity?,
    selectableLogs: List<LogFileEntity>,
    settings: AppSettings,
    usbState: UsbDestinationState,
    djiSdkState: DjiSdkState,
    progress: UsbKitProgress,
    onLogSelected: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenMsurvey: () -> Unit,
    onExportSelected: () -> Unit,
    onRecoverDroneMedia: () -> Unit,
) {
    var logMenuExpanded by remember { mutableStateOf(false) }
    var pendingExportConfirmation by remember { mutableStateOf<FlightExportConfirmMode?>(null) }
    val usbReady = usbState == UsbDestinationState.Ready
    val busy = progress.stage == UsbKitStage.DETECTING_MEDIA || progress.stage == UsbKitStage.TRANSFERRING
    val mediaCountText = when {
        progress.detectedMediaCount > 0 -> "${progress.detectedMediaCount} média(s) détecté(s)"
        progress.totalDroneMediaCount > 0 -> "${progress.totalDroneMediaCount} média(s) à analyser"
        else -> "Les médias du même créneau seront recherchés"
    }
    val title = when (selectedStep) {
        FlightExportStepKey.Log -> "Log de vol"
        FlightExportStepKey.Usb -> "Clé USB"
        FlightExportStepKey.Media -> "Médias"
        FlightExportStepKey.Export -> "Progression"
    }
    val detail = when (selectedStep) {
        FlightExportStepKey.Log -> selectedLog?.fileName ?: "Aucun log détecté"
        FlightExportStepKey.Usb -> usbState.destinationLabel
        FlightExportStepKey.Media -> when {
            progress.detectedMediaCount > 0 -> "${progress.detectedMediaCount} média(s) détecté(s)"
            progress.totalDroneMediaCount > 0 -> "${progress.detectedMediaCount}/${progress.totalDroneMediaCount} média(s)"
            djiSdkState.hasConnectedDrone() -> "Drone connecté, médias récupérables"
            else -> "Connecter le drone pour récupérer les médias"
        }
        FlightExportStepKey.Export -> progress.message
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassControlSurface(
                shape = MaterialTheme.shapes.medium,
                active = true,
                accent = when (selectedStep) {
                    FlightExportStepKey.Log -> Color(0xFF33D16D)
                    FlightExportStepKey.Usb -> usbState.color
                    FlightExportStepKey.Media -> Orange
                    FlightExportStepKey.Export -> if (progress.stage == UsbKitStage.ERROR) Color(0xFFFF4D4D) else Orange
                },
                containerAlpha = 0.12f,
                borderAlpha = 0.22f,
                highlightAlpha = 0.12f,
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = Color.White.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FlightExportStatusBadge(
                text = when (selectedStep) {
                    FlightExportStepKey.Log -> selectedLog?.flightStartTimeMillis?.let(::formatShortTime) ?: "À choisir"
                    FlightExportStepKey.Usb -> usbState.shortLabel
                    FlightExportStepKey.Media -> if (progress.detectedMediaCount > 0) progress.detectedMediaCount.toString() else "Auto"
                    FlightExportStepKey.Export -> flightExportStageBadge(progress)
                },
                color = when (selectedStep) {
                    FlightExportStepKey.Log -> if (selectedLog != null) Color(0xFF33D16D) else Color(0xFFFFB020)
                    FlightExportStepKey.Usb -> usbState.color
                    FlightExportStepKey.Media -> if (progress.detectedMediaCount > 0) Color(0xFF33D16D) else Orange
                    FlightExportStepKey.Export -> if (progress.stage == UsbKitStage.ERROR) Color(0xFFFF4D4D) else Orange
                },
            )
        }

        when (selectedStep) {
            FlightExportStepKey.Log -> {
                Box(modifier = Modifier.fillMaxWidth()) {
                    FlightExportGlassButton(
                        text = if (selectableLogs.isEmpty()) "Actualiser les logs" else "Choisir un autre log",
                        onClick = { logMenuExpanded = true },
                        enabled = selectableLogs.isNotEmpty(),
                        primary = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DropdownMenu(
                        expanded = logMenuExpanded,
                        onDismissRequest = { logMenuExpanded = false },
                        modifier = Modifier.glassMenuSurface(MaterialTheme.shapes.small),
                    ) {
                        selectableLogs.forEach { log ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = log.fileName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = listOfNotNull(
                                                log.flightStartTimeMillis?.let(::formatDate)
                                                    ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate),
                                                log.flightDurationSeconds?.let(::formatDuration),
                                                formatBytes(log.sizeBytes),
                                            ).joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.66f),
                                        )
                                    }
                                },
                                onClick = {
                                    onLogSelected(log.id)
                                    logMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                if (selectableLogs.isEmpty()) {
                    FlightExportGlassButton("Scanner le dossier", onSyncNow, Modifier.fillMaxWidth())
                }
                FlightExportGlassButton(
                    text = "Déposer via MSurvey",
                    onClick = onOpenMsurvey,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedLog != null && !busy,
                    primary = false,
                )
            }
            FlightExportStepKey.Usb -> {
                FlightExportGlassButton(
                    text = when (usbState) {
                        UsbDestinationState.Ready -> "Changer de clé"
                        UsbDestinationState.MountedNotAuthorized -> "Monter la clé"
                        UsbDestinationState.NotConfigured,
                        UsbDestinationState.Unavailable -> "Configurer la clé"
                    },
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    primary = false,
                )
            }
            FlightExportStepKey.Media -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FlightExportPreviewMetric("Détectés", progress.detectedMediaCount.toString(), Modifier.weight(1f))
                    FlightExportPreviewMetric("Total", progress.totalDroneMediaCount.toString(), Modifier.weight(1f))
                    FlightExportPreviewMetric("Drone", if (djiSdkState.hasConnectedDrone()) "OK" else "--", Modifier.weight(1f))
                }
                FlightExportGlassButton("Configurer médias", onOpenSettings, Modifier.fillMaxWidth())
            }
            FlightExportStepKey.Export -> {
                FlightExportLiveProgress(progress = progress)
                FlightExportGlassButton(
                    text = when {
                        selectedLog == null -> "Scanner les logs"
                        !usbReady -> when (usbState) {
                            UsbDestinationState.MountedNotAuthorized -> "Monter la clé USB"
                            else -> "Configurer la clé USB"
                        }
                        progress.stage == UsbKitStage.DONE -> "Exporter à nouveau"
                        else -> "Lancer l’export USB"
                    },
                    onClick = when {
                        selectedLog == null -> onSyncNow
                        !usbReady -> onOpenSettings
                        else -> { { pendingExportConfirmation = FlightExportConfirmMode.LogAndMedia } }
                    },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                    primary = true,
                )
            }
        }
    }
    if (pendingExportConfirmation != null && selectedLog != null) {
        FlightExportConfirmationDialog(
            mode = pendingExportConfirmation ?: FlightExportConfirmMode.LogAndMedia,
            log = selectedLog,
            usbState = usbState,
            mediaCountText = mediaCountText,
            onDismiss = { pendingExportConfirmation = null },
            onConfirm = {
                when (pendingExportConfirmation) {
                    FlightExportConfirmMode.LogOnly -> onExportSelected()
                    FlightExportConfirmMode.LogAndMedia -> onRecoverDroneMedia()
                    null -> Unit
                }
                pendingExportConfirmation = null
            },
        )
    }
}

private fun flightExportStageBadge(progress: UsbKitProgress): String {
    return when (progress.stage) {
        UsbKitStage.IDLE -> "Prêt"
        UsbKitStage.LOG_READY -> "Log"
        UsbKitStage.DETECTING_MEDIA -> "Scan"
        UsbKitStage.MEDIA_READY -> "Médias"
        UsbKitStage.NO_MEDIA -> "Aucun"
        UsbKitStage.TRANSFERRING -> "${progress.transferPercent.coerceIn(0, 100)}%"
        UsbKitStage.DONE -> "Terminé"
        UsbKitStage.ERROR -> "Erreur"
    }
}

@Composable
private fun FlightExportGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    val accent = if (primary) Orange else Orange.copy(alpha = 0.72f)
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = modifier
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.48f)
            .glassControlSurface(
                shape = shape,
                active = primary && enabled,
                accent = accent,
                containerAlpha = if (primary) 0.20f else 0.14f,
                borderAlpha = if (primary) 0.48f else 0.24f,
                highlightAlpha = if (primary) 0.11f else 0.08f,
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightExportTimeline(
    steps: List<FlightExportStep>,
    progressRatio: Float,
    transferring: Boolean,
    selectedStep: FlightExportStepKey,
    onStepSelected: (FlightExportStepKey) -> Unit,
) {
    val nodeWidth = 168.dp
    val iconSlotSize = 72.dp
    val trackCenterY = iconSlotSize / 2
    val horizontalInset = nodeWidth / 2
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp),
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val insetPx = with(density) { horizontalInset.toPx() }
        Canvas(modifier = Modifier.matchParentSize()) {
            val y = trackCenterY.toPx()
            val startX = insetPx
            val endX = size.width - insetPx
            drawLine(
                color = Color.White.copy(alpha = 0.16f),
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = if (transferring) Orange else Color(0xFF33D16D),
                start = Offset(startX, y),
                end = Offset(startX + ((endX - startX) * progressRatio.coerceIn(0f, 1f)), y),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        steps.forEachIndexed { index, step ->
            val fraction = if (steps.size == 1) 0f else index / (steps.size - 1f)
            val xDp = with(density) { ((widthPx - insetPx * 2f) * fraction + insetPx).toDp() }
            FlightExportTimelineNode(
                step = step,
                iconSlotSize = iconSlotSize,
                selected = step.key == selectedStep,
                onClick = { onStepSelected(step.key) },
                modifier = Modifier
                    .width(nodeWidth)
                    .align(Alignment.TopStart)
                    .graphicsLayer {
                        translationX = xDp.toPx() - size.width / 2f
                    },
            )
        }
    }
}

@Composable
private fun FlightExportTimelineNode(
    step: FlightExportStep,
    iconSlotSize: Dp,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = step.state.color
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(iconSlotSize)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (step.state == FlightExportStepState.Active || selected) {
                    Canvas(modifier = Modifier.size(72.dp)) {
                        drawCircle(color.copy(alpha = if (selected) 0.22f else 0.16f), radius = size.minDimension / 2f)
                        drawCircle(color.copy(alpha = if (selected) 0.14f else 0.10f), radius = size.minDimension / 2.8f)
                    }
                }
                Canvas(modifier = Modifier.size(64.dp)) {
                    drawCircle(Color(0xFF111A1C), radius = size.minDimension / 2f)
                    drawCircle(
                        color = color,
                        radius = size.minDimension / 2.2f,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                    )
                    if (selected) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.62f),
                            radius = size.minDimension / 2.04f,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }
                }
                CompagnonIcon(step.iconModule, Modifier.size(36.dp), thin = true, color = color)
            }
        }
        Text(
            text = step.label,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
        FlightExportStatusBadge(text = step.status, color = color)
        Text(
            text = step.detail,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color.White.copy(alpha = 0.64f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = step.extraDetail,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color.White.copy(alpha = 0.48f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        step.actionLabel?.let { label ->
            FlightExportStatusBadge(text = label, color = Color.White.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun FlightExportStatusBadge(
    text: String,
    color: Color,
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = color,
            fontWeight = FontWeight.Black,
            maxLines = 1,
        )
    }
}

private data class FlightExportStep(
    val key: FlightExportStepKey,
    val label: String,
    val detail: String,
    val extraDetail: String = "",
    val status: String,
    val state: FlightExportStepState,
    val iconModule: AppModule,
    val actionLabel: String? = null,
)

private enum class FlightExportStepKey {
    Log,
    Usb,
    Media,
    Export,
}

private enum class FlightExportStepState(val color: Color) {
    Done(Color(0xFF33D16D)),
    Active(Color(0xFFFF7900)),
    Warning(Color(0xFFFFB020)),
    Idle(Color(0xFFDCE8EC)),
}

@Composable
private fun SingleColumnContent(
    draft: AppSettings,
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    actionMessage: String,
    onDraftChange: (AppSettings) -> Unit,
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onPickMediaFolder: () -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    onSyncNow: () -> Unit,
    onExportLatest: () -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!settings.onboardingCompleted) {
            item {
                OnboardingCard(draft, logs)
            }
        }
        item {
            ConfigurationCard(draft, onDraftChange, onPickFolder, onPickUsbFolder, onPickMediaFolder, onSave, onTest)
        }
        item {
            StatusCard(settings, logs, actionMessage, onSyncNow, onExportLatest, onRetryErrors, onCheckUpdate, onInstallUpdate)
        }
        item {
            ActivityTitle()
        }
        items(events, key = { it.id }) { event ->
            ActivityRow(event)
        }
        item {
            HistoryTitle()
        }
        items(logs, key = { it.id }) { log ->
            LogRow(
                log = log,
                onShare = { shareLog(context, log) },
            )
        }
        item {
            CopyrightFooter()
        }
    }
}

@Composable
private fun WideContent(
    draft: AppSettings,
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    actionMessage: String,
    onDraftChange: (AppSettings) -> Unit,
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onPickMediaFolder: () -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    onSyncNow: () -> Unit,
    onExportLatest: () -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1.05f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!settings.onboardingCompleted) {
                item {
                    OnboardingCard(draft, logs)
                }
            }
            item {
                ConfigurationCard(draft, onDraftChange, onPickFolder, onPickUsbFolder, onPickMediaFolder, onSave, onTest)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(0.95f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                StatusCard(settings, logs, actionMessage, onSyncNow, onExportLatest, onRetryErrors, onCheckUpdate, onInstallUpdate)
            }
            item {
                ActivityTitle()
            }
            items(events, key = { it.id }) { event ->
                ActivityRow(event)
            }
            item {
                HistoryTitle()
            }
            items(logs, key = { it.id }) { log ->
                LogRow(
                    log = log,
                    onShare = { shareLog(context, log) },
                )
            }
            item {
                CopyrightFooter()
            }
        }
    }
}

@Composable
private fun OnboardingCard(
    draft: AppSettings,
    logs: List<LogFileEntity>,
) {
    GlassCard(containerAlpha = 0.62f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Mise en route", style = MaterialTheme.typography.titleMedium, color = Color.White)
            SetupLine("Logs DJI automatiques", draft.folderUri.isNotBlank())
            SetupLine("Identifiant radiocommande", draft.radioId.isNotBlank())
            SetupLine("Destination USB", draft.usbExportUri.isNotBlank())
            SetupLine("Premier scan", logs.isNotEmpty())
            Text(
                text = if (draft.isReadyForSync()) {
                    "Prêt pour le mode automatique. Les logs sont recherchés dans l'emplacement DJI connu."
                } else {
                    "Configurez l’UAS et la destination USB. Le dossier logs DJI est prérempli automatiquement."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f),
            )
        }
    }
}

@Composable
private fun FirstLaunchSetupAssistant(
    draft: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    onDraftChange: (AppSettings) -> Unit,
    onThemeChange: (String) -> Unit,
    onRequestPermissions: () -> Unit,
    onRetryDjiSdk: () -> Unit,
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onTestUsb: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    onFinish: () -> Unit,
) {
    val logReady = draft.folderUri.isNotBlank()
    val usbReady = draft.usbExportUri.isNotBlank()
    val uasReady = draft.radioId.isNotBlank()
    val scanReady = logs.isNotEmpty()
    val canFinish = uasReady && logReady && usbReady
    var step by remember { mutableStateOf(0) }
    var permissionsRequested by remember { mutableStateOf(false) }
    val droneReady = djiSdkState.hasConnectedDrone()
    val lastStep = 8
    val stepTitle = when (step) {
        0 -> "Bienvenue"
        1 -> "Autorisations"
        2 -> "Nom de l’UAS"
        3 -> "Drone"
        4 -> "Médias du drone"
        5 -> "Logs automatiques"
        6 -> "USB"
        7 -> "Thème"
        else -> "Validation"
    }
    val canGoNext = when (step) {
        2 -> uasReady
        5 -> logReady
        6 -> usbReady
        lastStep -> canFinish
        else -> true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF071013).copy(alpha = 0.98f),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Assistant de configuration",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 25.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Étape ${step + 1} / ${lastStep + 1} · $stepTitle",
                    style = MaterialTheme.typography.labelLarge,
                    color = Orange,
                    fontWeight = FontWeight.Black,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .animateContentSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SetupWizardProgress(currentStep = step, totalSteps = lastStep + 1)
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val forward = targetState > initialState
                        (fadeIn(tween(120)) + slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { width ->
                            if (forward) width / 3 else -width / 3
                        }).togetherWith(
                            fadeOut(tween(100)) + slideOutHorizontally(tween(180, easing = FastOutSlowInEasing)) { width ->
                                if (forward) -width / 4 else width / 4
                            },
                        ).using(SizeTransform(clip = false))
                    },
                    label = "setup_step_transition",
                ) { visibleStep ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        when (visibleStep) {
                            0 -> {
                        Text(
                            text = "Bienvenue sur Orange Drone Compagnon, l’assistant prépare la radiocommande pour l’usage terrain.",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            lineHeight = 28.sp,
                        )
                        Text(
                            text = "On avance étape par étape : autorisations, UAS, drone, médias, détection automatique des logs DJI, clé USB, thème, puis validation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.76f),
                        )
                    }
                            1 -> SetupWizardPermissionStep(
                        requested = permissionsRequested,
                        onRequestPermissions = {
                            permissionsRequested = true
                            onRequestPermissions()
                        },
                    )
                            2 -> {
                        Text(
                            text = "Indiquez le nom ou l’identifiant de l’UAS. Il servira à ranger les exports proprement.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.76f),
                        )
                        GlassTextField(
                            value = draft.radioId,
                            onValueChange = { onDraftChange(draft.copy(radioId = it)) },
                            label = "Nom de l’UAS",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                            3 -> SetupWizardDroneStep(
                        djiSdkState = djiSdkState,
                        droneReady = droneReady,
                        onRetryDjiSdk = onRetryDjiSdk,
                    )
                            4 -> SetupWizardDroneMediaStep(
                        droneReady = droneReady,
                        includeMedia = draft.usbIncludeMedia,
                        onToggleIncludeMedia = { onDraftChange(draft.copy(usbIncludeMedia = !draft.usbIncludeMedia)) },
                    )
                            5 -> SetupWizardFolderChoice(
                        title = "Logs de vol DJI",
                        description = "L’app utilise automatiquement l’emplacement DJI Enterprise connu. Le choix manuel reste disponible uniquement si Android bloque l’accès direct.",
                        value = draft.folderLabel,
                        emptyValue = "Emplacement DJI automatique",
                        ready = logReady,
                        readyLabel = "Automatique",
                        action = "Modifier le dossier",
                        onAction = onPickFolder,
                    )
                            6 -> SetupWizardFolderChoice(
                        title = "USB",
                        description = "Branchez la clé USB sur la radiocommande, attendez qu’elle apparaisse, puis choisissez le dossier d’export.",
                        value = draft.usbExportLabel,
                        emptyValue = "Aucune clé USB choisie",
                        ready = usbReady,
                        readyLabel = "Obligatoire",
                        action = "Choisir dossier USB",
                        onAction = onPickUsbFolder,
                    )
                            7 -> SetupWizardThemeChoice(
                        selectedThemeId = draft.appTheme,
                        onThemeChange = onThemeChange,
                    )
                            else -> {
                        Text(
                            text = "Dernière vérification avant de démarrer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.76f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniMetric("UAS", if (draft.radioId.isBlank()) "À faire" else "OK", Modifier.weight(1f))
                            MiniMetric("Drone", if (droneReady) "OK" else "À connecter", Modifier.weight(1f))
                            MiniMetric("Médias", if (draft.usbIncludeMedia) "Drone" else "Logs", Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniMetric("Logs", if (logReady) "OK" else "À faire", Modifier.weight(1f))
                            MiniMetric("USB", if (usbReady) "OK" else "À faire", Modifier.weight(1f))
                            MiniMetric("Thème", CompagnonThemes.firstOrNull { it.id == draft.appTheme }?.label ?: "OK", Modifier.weight(1f))
                        }
                        SecondaryFieldButton(
                            text = "Tester USB",
                            onClick = onTestUsb,
                            enabled = usbReady,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OrangeButton(
                onClick = {
                    if (step < lastStep) {
                        step += 1
                    } else {
                        onFinish()
                    }
                },
                enabled = canGoNext,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Text(
                    text = when {
                        step < lastStep && canGoNext -> "Suivant →"
                        step < lastStep -> "À compléter"
                        canFinish -> "Terminer"
                        else -> "Terminer après logs + USB"
                    },
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (step > 0) {
                    TextButton(onClick = { step -= 1 }) {
                        Text("Retour", color = Color.White.copy(alpha = 0.82f), fontWeight = FontWeight.Bold)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Plus tard", color = Color.White.copy(alpha = 0.70f), fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onOpenSettings) {
                    Text("Réglages", color = Color.White.copy(alpha = 0.70f), fontWeight = FontWeight.Bold)
                }
            }
        },
    )
}

@Composable
private fun SetupWizardProgress(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        if (index <= currentStep) Orange else Color.White.copy(alpha = 0.16f),
                    ),
            )
        }
    }
}

@Composable
private fun SetupWizardPermissionStep(
    requested: Boolean,
    onRequestPermissions: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Autorisations Android",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Autorisez la localisation, le Bluetooth et les notifications. Cela permet à l’app de lire l’environnement terrain, dialoguer avec la radiocommande et afficher les alertes utiles.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.76f),
        )
        SetupWizardActionPanel(
            title = if (requested) "Demande lancée" else "À faire maintenant",
            status = if (requested) "OK" else "Autoriser",
            detail = if (requested) {
                "Si Android a affiché une fenêtre, choisissez Autoriser. Vous pouvez continuer ensuite."
            } else {
                "Appuyez ici, puis acceptez les demandes Android qui apparaissent."
            },
            ready = requested,
            action = if (requested) "Redemander" else "Autoriser l’app",
            onAction = onRequestPermissions,
        )
    }
}

@Composable
private fun SetupWizardDroneStep(
    djiSdkState: DjiSdkState,
    droneReady: Boolean,
    onRetryDjiSdk: () -> Unit,
) {
    val emulatorMode = !djiSdkState.available && djiSdkState.lastError?.contains("Émulateur", ignoreCase = true) == true
    val detail = when {
        droneReady -> "Drone DJI connecté. L’app pourra récupérer les médias du vol quand ils seront disponibles."
        emulatorMode -> "Sur émulateur, la connexion drone ne peut pas être validée. Sur la radiocommande DJI : allumez le drone, connectez la radiocommande, puis ouvrez l’app."
        djiSdkState.registered -> "Connectez le drone et la radiocommande, puis attendez le statut Drone connecté."
        else -> "Connectez le drone, vérifiez le réseau de la radiocommande, puis relancez le contrôle DJI si nécessaire."
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Drone",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Connectez le drone avant de partir sur le terrain : l’app vérifie l’état DJI et vous confirme quand la liaison est prête.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.76f),
        )
        SetupWizardActionPanel(
            title = "État DJI",
            status = when {
                droneReady -> "Connecté"
                emulatorMode -> "Émulateur"
                djiSdkState.registered -> "En attente"
                else -> "À connecter"
            },
            detail = "$detail\n\nMessage actuel : ${djiSdkState.message}",
            ready = droneReady || emulatorMode,
            action = "Relancer DJI",
            onAction = onRetryDjiSdk,
        )
    }
}

@Composable
private fun SetupWizardDroneMediaStep(
    droneReady: Boolean,
    includeMedia: Boolean,
    onToggleIncludeMedia: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Médias du drone",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Les photos et vidéos seront récupérées depuis le drone connecté, puis copiées vers la clé USB avec le log de vol.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.76f),
        )
        SetupWizardActionPanel(
            title = "Source médias",
            status = if (droneReady) "Drone prêt" else "Connecter le drone",
            detail = if (droneReady) {
                "Le drone est connecté. Les médias du vol pourront être récupérés au moment de l’export."
            } else {
                "Allumez le drone, connectez la radiocommande, puis gardez l’option médias activée si vous voulez exporter les photos et vidéos."
            },
            ready = droneReady,
            action = if (includeMedia) "Inclure médias : oui" else "Inclure médias : non",
            onAction = onToggleIncludeMedia,
        )
    }
}

@Composable
private fun SetupWizardThemeChoice(
    selectedThemeId: String,
    onThemeChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Thème de l’app",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = "Choisissez l’ambiance la plus lisible pour votre usage terrain.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.76f),
        )
        CompagnonThemes.chunked(2).forEach { rowThemes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowThemes.forEach { theme ->
                    SetupWizardThemeTile(
                        theme = theme,
                        selected = theme.id == selectedThemeId,
                        onClick = { onThemeChange(theme.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowThemes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SetupWizardThemeTile(
    theme: CompagnonTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = if (selected) Orange else Color.White.copy(alpha = 0.42f)
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        containerAlpha = if (selected) 0.48f else 0.34f,
        borderAlpha = if (selected) 0.46f else 0.20f,
        highlightAlpha = 0.08f,
        glowColor = accent.copy(alpha = if (selected) 0.12f else 0.04f),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                theme.colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(color)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), MaterialTheme.shapes.small),
                    )
                }
            }
            Text(
                text = theme.label,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (selected) "Sélectionné" else theme.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) Orange else Color.White.copy(alpha = 0.66f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SetupWizardActionPanel(
    title: String,
    status: String,
    detail: String,
    ready: Boolean,
    action: String,
    onAction: () -> Unit,
) {
    val accent = if (ready) Color(0xFF33D16D) else Orange
    GlassCard(containerAlpha = 0.40f, borderAlpha = 0.24f, highlightAlpha = 0.08f, glowColor = accent.copy(alpha = 0.08f)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleSmall,
                    color = accent,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
            }
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
            )
            SecondaryFieldButton(
                text = action,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SetupWizardFolderChoice(
    title: String,
    description: String,
    value: String,
    emptyValue: String,
    ready: Boolean,
    readyLabel: String,
    action: String,
    onAction: () -> Unit,
) {
    val accent = if (ready) Color(0xFF33D16D) else Orange
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.76f),
        )
        GlassCard(containerAlpha = 0.40f, borderAlpha = 0.24f, highlightAlpha = 0.08f, glowColor = accent.copy(alpha = 0.08f)) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (ready) "OK" else readyLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = if (ready) "Configuré" else "À choisir",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.66f),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = value.ifBlank { emptyValue },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                SecondaryFieldButton(
                    text = action,
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SetupAssistantStepTile(
    index: String,
    title: String,
    status: String,
    detail: String,
    done: Boolean,
    action: String,
    onAction: () -> Unit,
    actionEnabled: Boolean = true,
) {
    val accent = if (done) Color(0xFF33D16D) else Orange
    GlassCard(
        containerAlpha = 0.40f,
        borderAlpha = if (done) 0.34f else 0.24f,
        highlightAlpha = 0.08f,
        glowColor = accent.copy(alpha = 0.08f),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(accent.copy(alpha = 0.18f))
                    .border(BorderStroke(1.dp, accent.copy(alpha = 0.46f)), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = index,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                    )
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.70f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SecondaryFieldButton(
                text = action,
                onClick = onAction,
                enabled = actionEnabled,
                modifier = Modifier.width(104.dp),
            )
        }
    }
}

@Composable
private fun SetupLine(label: String, done: Boolean) {
    Text(
        text = "${if (done) "OK" else "À faire"} - $label",
        style = MaterialTheme.typography.bodyMedium,
        color = if (done) Color(0xFFC9F7D0) else Color.White.copy(alpha = 0.80f),
    )
}

@Composable
private fun HistoryTitle() {
    Text(
        text = "Historique",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ConfigurationCard(
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
    onPickFolder: () -> Unit,
    onPickUsbFolder: () -> Unit,
    onPickMediaFolder: () -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
) {
    GlassCard(containerAlpha = 0.48f, borderAlpha = 0.24f, highlightAlpha = 0.08f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Réglages essentiels", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)

            Text(
                text = if (draft.folderLabel.isBlank()) "Emplacement DJI automatique" else draft.folderLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            SecondaryFieldButton(text = "Modifier le dossier logs", onClick = onPickFolder, modifier = Modifier.fillMaxWidth())

            GlassTextField(
                value = draft.radioId,
                onValueChange = { onDraftChange(draft.copy(radioId = it)) },
                label = "Identifiant radiocommande",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Text(
                text = "Destination principale : export USB",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.82f),
            )
            UsbFields(
                draft = draft.copy(deliveryMode = DeliveryMode.USB),
                onDraftChange = { onDraftChange(it.copy(deliveryMode = DeliveryMode.USB)) },
                onPickUsbFolder = onPickUsbFolder,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ToggleChip(
                    label = "Mode auto silencieux",
                    selected = draft.silentMode,
                    onClick = { onDraftChange(draft.copy(silentMode = !draft.silentMode)) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryFieldButton(text = "Enregistrer", onClick = onSave, modifier = Modifier.weight(1f))
                SecondaryFieldButton(text = "Tester USB", onClick = onTest, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun UsbFields(
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
    onPickUsbFolder: () -> Unit,
) {
    Text(
        text = if (draft.usbExportLabel.isBlank()) "Aucune clé USB choisie" else draft.usbExportLabel,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.88f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )

    SecondaryFieldButton(text = "Choisir dossier sur clé USB", onClick = onPickUsbFolder, modifier = Modifier.fillMaxWidth())

    Text(
        text = "Médias du vol : récupérés depuis le drone connecté, puis copiés vers la clé USB avec le log de vol.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.78f),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleChip(
            label = "Inclure médias du vol",
            selected = draft.usbIncludeMedia,
            onClick = { onDraftChange(draft.copy(usbIncludeMedia = !draft.usbIncludeMedia)) },
        )
        ToggleChip(
            label = "Ne pas écraser",
            selected = draft.skipExistingRemoteFiles,
            onClick = { onDraftChange(draft.copy(skipExistingRemoteFiles = !draft.skipExistingRemoteFiles)) },
        )
    }
}

@Composable
private fun ToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = chipColors(selected),
        border = chipBorder(selected),
    )
}

@Composable
private fun chipColors(selected: Boolean) =
    FilterChipDefaults.filterChipColors(
        containerColor = Color.White.copy(alpha = 0.075f),
        labelColor = Color.White,
        selectedContainerColor = Orange.copy(alpha = 0.24f),
        selectedLabelColor = Color.White,
    )

@Composable
private fun chipBorder(selected: Boolean) =
    FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = Color.White.copy(alpha = 0.24f),
        selectedBorderColor = Orange.copy(alpha = 0.86f),
    )

@Composable
private fun StatusCard(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    actionMessage: String,
    onSyncNow: () -> Unit,
    onExportLatest: () -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val sent = logs.count { it.status == LogStatus.SENT }
    val errors = logs.count { it.status == LogStatus.ERROR }
    val pending = logs.count { it.status == LogStatus.PENDING || it.status == LogStatus.ERROR }
    val ready = settings.isReadyForSync()
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Etat", style = MaterialTheme.typography.titleMedium, color = Color.White)
            InfoLine("Mode", if (ready) "Pret - automatique actif" else "Configuration incomplete")
            InfoLine("Dossier surveille", settings.folderLabel.ifBlank { "Non configure" })
            InfoLine("Radiocommande", settings.radioId.ifBlank { "Non configuree" })
            InfoLine("Logs detectes", logs.size.toString())
            InfoLine("Logs envoyes", sent.toString())
            InfoLine("Logs en attente", pending.toString())
            InfoLine("Logs en erreur", errors.toString())
            InfoLine("Cle USB", if (settings.usbExportUri.isNotBlank()) "Autorisee" else "A choisir")
            InfoLine("Photos/videos", if (settings.usbIncludeMedia && settings.mediaFolderUri.isNotBlank()) "Inclus" else "Logs seulement")
            InfoLine("Structure export", "Radio > date > vol > logs/medias")
            InfoLine(
                "Derniere synchronisation",
                settings.lastSyncAtMillis.takeIf { it > 0 }?.let(::formatDate) ?: "Jamais",
            )
            InfoLine(
                "Mise a jour",
                settings.latestVersionName.takeIf { it.isNotBlank() }?.let { "Disponible : $it" } ?: "A jour ou non verifiee",
            )

            if (actionMessage.isNotBlank()) {
                Text(
                    actionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrangeButton(onClick = onSyncNow, modifier = Modifier.weight(1f)) {
                    Text("Exporter tout")
                }
                OrangeButton(onClick = onExportLatest, modifier = Modifier.weight(1f)) {
                    Text("Dernier vol")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrangeButton(onClick = onRetryErrors, modifier = Modifier.weight(1f)) {
                    Text("Reessayer erreurs")
                }
                OrangeButton(onClick = onCheckUpdate, modifier = Modifier.weight(1f)) {
                    Text("Mise a jour")
                }
            }
            if (settings.latestVersionName.isNotBlank() && settings.latestApkSha256.isNotBlank()) {
                OrangeButton(onClick = onInstallUpdate, modifier = Modifier.fillMaxWidth()) {
                    Text("Télécharger et installer ${settings.latestVersionName}")
                }
            }
        }
    }
}

@Composable
private fun ActivityTitle() {
    Text(
        text = "Journal",
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun ActivityRow(event: ActivityEventEntity) {
    GlassCard(containerAlpha = 0.26f) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "${event.level} - ${formatDate(event.createdAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun OrangeScreenContent(
    settings: AppSettings,
    onPickScreenExportFolder: () -> Unit,
    onSaveScreenProject: (String) -> Unit,
    onAddScreenExportHistory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val backgrounds = remember {
        listOf(
            ScreenBackground("Orange propriété", R.drawable.screen_bg_orange_property),
            ScreenBackground("Neige", R.drawable.screen_bg_snow),
            ScreenBackground("Tour 77", R.drawable.screen_bg_tower_77),
            ScreenBackground("Tour 33", R.drawable.screen_bg_tower_33),
            ScreenBackground("Site DJI", R.drawable.screen_bg_dji_site),
        )
    }
    var selectedResolution by remember { mutableStateOf(ScreenResolution.RcPlus2) }
    var selectedBackground by remember { mutableStateOf(backgrounds.first()) }
    var customPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showOrangeLogo by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(true) }
    var overlayText by remember { mutableStateOf("") }
    var textColor by remember { mutableStateOf(ScreenTextColor.White) }
    var textScale by remember { mutableStateOf(1.0f) }
    var textLayer by remember { mutableStateOf(ScreenLayerTransform(offsetX = -260f, offsetY = 250f, scale = 1.1f)) }
    var logoLayer by remember { mutableStateOf(ScreenLayerTransform(offsetX = 0f, offsetY = -110f, scale = 1f)) }
    var qrLayer by remember { mutableStateOf(ScreenLayerTransform(offsetX = 250f, offsetY = -95f, scale = 0.92f)) }
    var exportRequest by remember { mutableStateOf<ScreenExportRequest?>(null) }
    var exportMessage by remember { mutableStateOf("") }
    val customBitmap = remember(customPhotoUri) { customPhotoUri?.let { loadBitmapFromUri(context, it) } }

    LaunchedEffect(settings.screenProject, backgrounds) {
        decodeScreenProject(settings.screenProject, backgrounds)?.let { project ->
            selectedResolution = project.resolution
            selectedBackground = project.background
            showOrangeLogo = false
            showQrCode = project.showQrCode
            overlayText = ""
            textColor = ScreenTextColor.White
            textScale = 1f
            qrLayer = project.qrLayer
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            customPhotoUri = uri
        }
    }
    val jpgExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri ->
        val request = exportRequest
        if (uri != null && request != null) {
            exportMessage = if (exportScreenJpg(context, uri, request)) {
                "Fond d’écran JPG exporté."
            } else {
                "Export impossible."
            }
        }
        exportRequest = null
    }
    fun currentRequest(): ScreenExportRequest =
        ScreenExportRequest(
            resolution = selectedResolution,
            backgroundResId = if (customPhotoUri == null) selectedBackground.resId else null,
            customPhotoUri = customPhotoUri,
            backgroundLayer = ScreenLayerTransform(),
            textLayer = textLayer,
            logoLayer = logoLayer,
            qrLayer = qrLayer,
            showOrangeLogo = false,
            showQrCode = showQrCode,
            overlayText = "",
            textColor = textColor,
            textScale = textScale,
        )
    val startExport = {
        val name = "orange-drone-compagnon-${selectedResolution.fileSuffix}-${System.currentTimeMillis()}.jpg"
        val request = currentRequest()
        if (settings.screenExportUri.isNotBlank()) {
            val exportedToFolder = exportScreenJpgToFolder(context, Uri.parse(settings.screenExportUri), name, request)
            exportMessage = if (exportedToFolder) {
                onAddScreenExportHistory("${formatDate(System.currentTimeMillis())} - $name")
                "Fond d’écran exporté dans ${settings.screenExportLabel.ifBlank { "le dossier choisi" }}."
            } else {
                exportRequest = request
                jpgExporter.launch(name)
                "Dossier direct inaccessible. Choisissez l'emplacement d'export."
            }
        } else {
            exportRequest = request
            jpgExporter.launch(name)
        }
    }

    @Composable
    fun ExportCard(modifier: Modifier = Modifier) {
        GlassCard(containerAlpha = 0.54f, modifier = modifier) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfoLine("Format", "${selectedResolution.label} - ${selectedResolution.width} x ${selectedResolution.height}")
                InfoLine("Sortie", settings.screenExportLabel.ifBlank { "Choix au moment de l'export" })
                OrangeButton(onClick = startExport, modifier = Modifier.fillMaxWidth()) {
                    Text("Exporter le JPG", fontWeight = FontWeight.Black)
                }
                if (exportMessage.isNotBlank()) {
                    Text(
                        text = exportMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.86f),
                    )
                }
            }
        }
    }

    @Composable
    fun ScreenQuickActionsBar(modifier: Modifier = Modifier, vertical: Boolean = false) {
        @Composable
        fun ActionButton(
            label: String,
            selected: Boolean = false,
            onClick: () -> Unit,
            modifier: Modifier = Modifier,
        ) {
            Box(
                modifier = modifier
                    .height(if (vertical) 36.dp else 50.dp)
                    .glassControlSurface(
                        shape = MaterialTheme.shapes.small,
                        active = selected,
                        accent = Orange,
                        containerAlpha = if (selected) 0.24f else 0.11f,
                        borderAlpha = if (selected) 0.62f else 0.32f,
                        highlightAlpha = 0.16f,
                    )
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (vertical) {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ActionButton("RC2", selectedResolution == ScreenResolution.Rc2, { selectedResolution = ScreenResolution.Rc2 }, Modifier.fillMaxWidth())
                ActionButton("RC Plus 2", selectedResolution == ScreenResolution.RcPlus2, { selectedResolution = ScreenResolution.RcPlus2 }, Modifier.fillMaxWidth())
                ActionButton("Export", false, startExport, Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionButton("RC2", selectedResolution == ScreenResolution.Rc2, { selectedResolution = ScreenResolution.Rc2 }, Modifier.weight(1f))
                ActionButton("RC Plus 2", selectedResolution == ScreenResolution.RcPlus2, { selectedResolution = ScreenResolution.RcPlus2 }, Modifier.weight(1f))
                ActionButton("Export", false, startExport, Modifier.weight(1f))
            }
        }
    }

    @Composable
    fun FormatPhotoCard(modifier: Modifier = Modifier, showResolution: Boolean = true, compact: Boolean = false) {
        GlassCard(containerAlpha = 0.46f, modifier = modifier) {
            Column(
                modifier = Modifier.padding(if (compact) 8.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
            ) {
                Text(
                    "Format et photo",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (compact) 16.sp else 20.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                if (showResolution) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToggleChip(
                            label = "RC Plus 2",
                            selected = selectedResolution == ScreenResolution.RcPlus2,
                            onClick = { selectedResolution = ScreenResolution.RcPlus2 },
                        )
                        ToggleChip(
                            label = "RC 2",
                            selected = selectedResolution == ScreenResolution.Rc2,
                            onClick = { selectedResolution = ScreenResolution.Rc2 },
                        )
                    }
                }
                ScreenBackgroundCarousel(
                    backgrounds = backgrounds,
                    selected = selectedBackground,
                    customPhotoUri = customPhotoUri,
                    compact = compact,
                    onSelect = {
                        selectedBackground = it
                        customPhotoUri = null
                    },
                    onPickCustom = { photoPicker.launch("image/*") },
                )
            }
        }
    }

    @Composable
    fun QrCard(modifier: Modifier = Modifier, compact: Boolean = false) {
        GlassCard(containerAlpha = 0.46f, modifier = modifier) {
            Column(
                modifier = Modifier.padding(if (compact) 8.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
            ) {
                Text(
                    "QR code",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (compact) 16.sp else 20.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                ToggleChip(
                    label = if (showQrCode) "QR affiché" else "QR masqué",
                    selected = showQrCode,
                    onClick = { showQrCode = !showQrCode },
                )
                val qrActions: @Composable RowScope.() -> Unit = {
                    ToggleChip("Taille +", false) { qrLayer = qrLayer.copy(scale = (qrLayer.scale + 0.12f).coerceAtMost(2.4f)) }
                    ToggleChip("Taille -", false) { qrLayer = qrLayer.copy(scale = (qrLayer.scale - 0.12f).coerceAtLeast(0.45f)) }
                    ToggleChip(
                        label = "Reset",
                        selected = false,
                        onClick = {
                            qrLayer = ScreenLayerTransform(offsetX = 250f, offsetY = -95f, scale = 0.92f)
                        },
                    )
                }
                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), content = qrActions)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = qrActions)
                }
                OrangeButton(
                    onClick = {
                        val project = ScreenProjectState(
                            resolution = selectedResolution,
                            background = selectedBackground,
                            showOrangeLogo = false,
                            showQrCode = showQrCode,
                            overlayText = "",
                            textColor = textColor,
                            textScale = textScale,
                            backgroundLayer = ScreenLayerTransform(),
                            textLayer = textLayer,
                            logoLayer = logoLayer,
                            qrLayer = qrLayer,
                        )
                        onSaveScreenProject(encodeScreenProject(project))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 38.dp else 48.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                ) {
                    Text("Enregistrer ce modèle")
                }
            }
        }
    }

    @Composable
    fun ScreenPanelDivider() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.10f)),
        )
    }

    @Composable
    fun ScreenToolPanel(modifier: Modifier = Modifier, compact: Boolean = false) {
        GlassCard(
            modifier = modifier,
            containerAlpha = 0.48f,
            borderAlpha = 0.26f,
            highlightAlpha = 0.08f,
        ) {
            Column(
                modifier = Modifier.padding(if (compact) 9.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 11.dp),
            ) {
                ScreenQuickActionsBar(vertical = compact)
                if (exportMessage.isNotBlank()) {
                    Text(
                        text = exportMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                }

                ScreenPanelDivider()

                Text(
                    "Fond d'écran",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (compact) 16.sp else 19.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                ScreenBackgroundCarousel(
                    backgrounds = backgrounds,
                    selected = selectedBackground,
                    customPhotoUri = customPhotoUri,
                    compact = true,
                    onSelect = {
                        selectedBackground = it
                        customPhotoUri = null
                    },
                    onPickCustom = { photoPicker.launch("image/*") },
                )

                ScreenPanelDivider()

                Text(
                    "QR code",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (compact) 16.sp else 19.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                ToggleChip(
                    label = if (showQrCode) "QR affiché" else "QR masqué",
                    selected = showQrCode,
                    onClick = { showQrCode = !showQrCode },
                )
                val qrActions: @Composable RowScope.() -> Unit = {
                    ToggleChip("Taille +", false) { qrLayer = qrLayer.copy(scale = (qrLayer.scale + 0.12f).coerceAtMost(2.4f)) }
                    ToggleChip("Taille -", false) { qrLayer = qrLayer.copy(scale = (qrLayer.scale - 0.12f).coerceAtLeast(0.45f)) }
                    ToggleChip(
                        label = "Reset",
                        selected = false,
                        onClick = {
                            qrLayer = ScreenLayerTransform(offsetX = 250f, offsetY = -95f, scale = 0.92f)
                        },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    content = qrActions,
                )
                OrangeButton(
                    onClick = {
                        val project = ScreenProjectState(
                            resolution = selectedResolution,
                            background = selectedBackground,
                            showOrangeLogo = false,
                            showQrCode = showQrCode,
                            overlayText = "",
                            textColor = textColor,
                            textScale = textScale,
                            backgroundLayer = ScreenLayerTransform(),
                            textLayer = textLayer,
                            logoLayer = logoLayer,
                            qrLayer = qrLayer,
                        )
                        onSaveScreenProject(encodeScreenProject(project))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (compact) 36.dp else 42.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                ) {
                    Text("Enregistrer ce modèle", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    @Composable
    fun HistoryCard(modifier: Modifier = Modifier) {
        val history = settings.screenExportHistory.split("||").filter { it.isNotBlank() }
        if (history.isNotEmpty()) {
            GlassCard(containerAlpha = 0.54f) {
                Column(
                    modifier = modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Exports récents", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    history.forEach { entry ->
                        Text(entry, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.76f))
                    }
                }
            }
        }
    }

    @Composable
    fun ControlsColumn(modifier: Modifier = Modifier, includeFooter: Boolean = true, compactActions: Boolean = false) {
        BoxWithConstraints(modifier = modifier) {
            val narrowPanel = maxWidth < 440.dp
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = if (includeFooter) 48.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(if (narrowPanel) 8.dp else 10.dp),
            ) {
                if (compactActions) {
                    item { ScreenToolPanel(compact = narrowPanel) }
                } else {
                    item { ExportCard() }
                }
                if (!compactActions) {
                    item { FormatPhotoCard(showResolution = true, compact = narrowPanel) }
                    item { QrCard(compact = narrowPanel) }
                }
                item { HistoryCard() }
                if (includeFooter) {
                    item { CopyrightFooter() }
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val configuration = LocalConfiguration.current
        val screenClass = currentResponsiveClass(maxWidth, maxHeight)
        val landscape = configuration.screenWidthDp > configuration.screenHeightDp && maxWidth > 360.dp
        val previewMaxHeight = when {
            landscape && screenClass == ResponsiveClass.Compact -> 205.dp
            landscape -> 320.dp
            screenClass == ResponsiveClass.Compact -> 260.dp
            else -> 360.dp
        }

        if (landscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.54f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ScreenWallpaperEditor(
                        resolution = selectedResolution,
                        background = selectedBackground,
                        customBitmap = customBitmap,
                        showQrCode = showQrCode,
                        qrLayer = qrLayer,
                        onQrLayerChange = { qrLayer = it },
                        maxPreviewHeight = previewMaxHeight,
                    )
                }
                ControlsColumn(
                    modifier = Modifier
                        .weight(0.46f)
                        .fillMaxHeight(),
                    includeFooter = false,
                    compactActions = true,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ModuleHero(
                        module = AppModule.SCREEN,
                        status = selectedResolution.label,
                        description = "Créer un fond d’écran RC avec modèle Orange, QR code et export JPG.",
                        metrics = listOf(
                            ModuleHeroMetric("Format", "${selectedResolution.width}x${selectedResolution.height}", OdsColor.White),
                            ModuleHeroMetric("QR", if (showQrCode) "Actif" else "Masqué", if (showQrCode) OdsColor.Success else OdsColor.TextMuted),
                            ModuleHeroMetric("Export", settings.screenExportLabel.ifBlank { "Android" }, OdsColor.Orange),
                        ),
                    )
                }
                item {
                    ScreenWallpaperEditor(
                        resolution = selectedResolution,
                        background = selectedBackground,
                        customBitmap = customBitmap,
                        showQrCode = showQrCode,
                        qrLayer = qrLayer,
                        onQrLayerChange = { qrLayer = it },
                        maxPreviewHeight = previewMaxHeight,
                    )
                }
                item { ExportCard() }
                item { FormatPhotoCard() }
                item { QrCard() }
                item { HistoryCard() }
                item { CopyrightFooter() }
            }
        }
    }
}

@Composable
private fun ScreenBackgroundCarousel(
    backgrounds: List<ScreenBackground>,
    selected: ScreenBackground,
    customPhotoUri: Uri?,
    compact: Boolean = false,
    onSelect: (ScreenBackground) -> Unit,
    onPickCustom: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp),
    ) {
        backgrounds.forEach { background ->
            val isSelected = customPhotoUri == null && selected == background
            Box(
                modifier = Modifier
                    .size(width = if (compact) 104.dp else 150.dp, height = if (compact) 58.dp else 86.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.Black.copy(alpha = 0.36f))
                    .clickable { onSelect(background) },
            ) {
                Image(
                    painter = painterResource(background.resId),
                    contentDescription = background.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Orange.copy(alpha = 0.22f)),
                    )
                }
                Text(
                    text = background.label,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.62f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
        Box(
            modifier = Modifier
                .size(width = if (compact) 66.dp else 98.dp, height = if (compact) 58.dp else 86.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (customPhotoUri != null) Orange.copy(alpha = 0.82f) else Color.Black.copy(alpha = 0.38f))
                .clickable(onClick = onPickCustom),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ScreenWallpaperEditor(
    resolution: ScreenResolution,
    background: ScreenBackground,
    customBitmap: Bitmap?,
    showQrCode: Boolean,
    qrLayer: ScreenLayerTransform,
    onQrLayerChange: (ScreenLayerTransform) -> Unit,
    maxPreviewHeight: Dp = 360.dp,
) {
    GlassCard(containerAlpha = 0.44f, borderAlpha = 0.30f, highlightAlpha = 0.12f) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Aperçu fond d'écran",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${resolution.label}  ${resolution.width} x ${resolution.height}",
                    modifier = Modifier
                        .glassControlSurface(
                            shape = MaterialTheme.shapes.small,
                            active = true,
                            accent = Orange,
                            containerAlpha = 0.13f,
                            borderAlpha = 0.24f,
                            highlightAlpha = 0.12f,
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxPreviewHeight)
                    .aspectRatio(resolution.width.toFloat() / resolution.height.toFloat())
                    .clip(RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)), RoundedCornerShape(18.dp))
                    .background(Color.Black),
            ) {
                if (customBitmap != null) {
                    Image(
                        bitmap = customBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource(background.resId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.06f),
                                    Color.Black.copy(alpha = 0.44f),
                                ),
                            ),
                        ),
                )
                Text(
                    text = "${resolution.label} - ${resolution.width} x ${resolution.height}",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .glassControlSurface(
                            shape = MaterialTheme.shapes.small,
                            active = true,
                            accent = Orange,
                            containerAlpha = 0.18f,
                            borderAlpha = 0.28f,
                            highlightAlpha = 0.12f,
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.90f),
                    fontWeight = FontWeight.Bold,
                )
                if (showQrCode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size((maxPreviewHeight * 0.58f).coerceIn(150.dp, 230.dp))
                            .transformableLayer(qrLayer, onQrLayerChange),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.screen_qr_code),
                            contentDescription = "QR code",
                            modifier = Modifier.size((maxPreviewHeight * 0.40f).coerceIn(104.dp, 178.dp)),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
            Text(
                text = if (showQrCode) {
                    "Déplacer le QR : glisser. Ajuster la taille : pincer ou utiliser les boutons."
                } else {
                    "QR masqué : le fond sera exporté sans QR code."
                },
                modifier = Modifier.padding(horizontal = 2.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.68f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Modifier.transformableLayer(
    transform: ScreenLayerTransform,
    onTransformChange: (ScreenLayerTransform) -> Unit,
): Modifier {
    val latestTransform by rememberUpdatedState(transform)
    val latestOnTransformChange by rememberUpdatedState(onTransformChange)
    return this
        .graphicsLayer {
            translationX = transform.offsetX
            translationY = transform.offsetY
            scaleX = transform.scale
            scaleY = transform.scale
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                val current = latestTransform
                latestOnTransformChange(
                    current.copy(
                        offsetX = current.offsetX + pan.x,
                        offsetY = current.offsetY + pan.y,
                        scale = (current.scale * zoom).coerceIn(0.35f, 4.5f),
                    ),
                )
            }
        }
}

private data class ScreenBackground(val label: String, val resId: Int)

private enum class ScreenResolution(val label: String, val width: Int, val height: Int, val fileSuffix: String) {
    RcPlus2("RC Plus 2", 1900, 1200, "rc-plus-2"),
    Rc2("RC 2", 1920, 1080, "rc-2"),
}

private enum class ScreenTextColor(val token: String, val composeColor: Color, val androidColor: Int) {
    White("white", Color.White, android.graphics.Color.WHITE),
    BrandOrange("orange", Color(0xFFFF7900), 0xFFFF7900.toInt()),
    Black("black", Color.Black, android.graphics.Color.BLACK),
}

private data class ScreenLayerTransform(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
) {
    companion object
}

private data class ScreenExportRequest(
    val resolution: ScreenResolution,
    val backgroundResId: Int?,
    val customPhotoUri: Uri?,
    val backgroundLayer: ScreenLayerTransform,
    val textLayer: ScreenLayerTransform,
    val logoLayer: ScreenLayerTransform,
    val qrLayer: ScreenLayerTransform,
    val showOrangeLogo: Boolean,
    val showQrCode: Boolean,
    val overlayText: String,
    val textColor: ScreenTextColor,
    val textScale: Float,
)

private data class ScreenProjectState(
    val resolution: ScreenResolution,
    val background: ScreenBackground,
    val showOrangeLogo: Boolean,
    val showQrCode: Boolean,
    val overlayText: String,
    val textColor: ScreenTextColor,
    val textScale: Float,
    val backgroundLayer: ScreenLayerTransform,
    val textLayer: ScreenLayerTransform,
    val logoLayer: ScreenLayerTransform,
    val qrLayer: ScreenLayerTransform,
)

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? =
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

private fun exportScreenJpg(context: android.content.Context, outputUri: Uri, request: ScreenExportRequest): Boolean =
    runCatching {
        val output = Bitmap.createBitmap(request.resolution.width, request.resolution.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val background = request.customPhotoUri?.let { loadBitmapFromUri(context, it) }
            ?: request.backgroundResId?.let { BitmapFactory.decodeResource(context.resources, it) }
            ?: return false
        drawCoverBitmap(canvas, background, output.width, output.height, request.backgroundLayer)
        val shade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.LinearGradient(
                0f,
                0f,
                0f,
                output.height.toFloat(),
                intArrayOf(0x15000000, 0x8A000000.toInt()),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, output.width.toFloat(), output.height.toFloat(), shade)
        if (request.showQrCode) {
            BitmapFactory.decodeResource(context.resources, R.drawable.screen_qr_code)?.let {
                drawExportBitmap(canvas, it, output.width, output.height, request.qrLayer, 0.19f)
            }
        }
        context.contentResolver.openOutputStream(outputUri, "w")?.use { stream ->
            output.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        } ?: return false
    }.getOrDefault(false)

private fun exportScreenJpgToFolder(
    context: android.content.Context,
    folderUri: Uri,
    fileName: String,
    request: ScreenExportRequest,
): Boolean =
    runCatching {
        val folder = DocumentFile.fromTreeUri(context, folderUri) ?: return false
        val target = folder.createFile("image/jpeg", fileName) ?: return false
        val exported = exportScreenJpg(context, target.uri, request)
        if (!exported) runCatching { target.delete() }
        exported
    }.getOrDefault(false)

private fun ensureGeolocaliseText(value: String): String {
    val clean = value.ifBlank { "EQUIPEMENT\nGEOLOCALISE" }
    return if (clean.uppercase(Locale.FRANCE).contains("GEOLOCALISE")) clean else "$clean\nGEOLOCALISE"
}

private fun encodeScreenProject(project: ScreenProjectState): String =
    listOf(
        project.resolution.name,
        project.background.label,
        project.showOrangeLogo.toString(),
        project.showQrCode.toString(),
        project.overlayText.replace("|", " ").replace("\n", "\\n"),
        project.textColor.name,
        project.textScale.toString(),
        project.backgroundLayer.compact(),
        project.textLayer.compact(),
        project.logoLayer.compact(),
        project.qrLayer.compact(),
    ).joinToString("|")

private fun decodeScreenProject(value: String, backgrounds: List<ScreenBackground>): ScreenProjectState? =
    runCatching {
        val parts = value.split("|")
        if (parts.size < 11) return@runCatching null
        ScreenProjectState(
            resolution = runCatching { ScreenResolution.valueOf(parts[0]) }.getOrDefault(ScreenResolution.RcPlus2),
            background = backgrounds.firstOrNull { it.label == parts[1] } ?: backgrounds.first(),
            showOrangeLogo = parts[2].toBooleanStrictOrNull() ?: true,
            showQrCode = parts[3].toBooleanStrictOrNull() ?: false,
            overlayText = parts[4].replace("\\n", "\n"),
            textColor = runCatching { ScreenTextColor.valueOf(parts[5]) }.getOrDefault(ScreenTextColor.White),
            textScale = parts[6].toFloatOrNull() ?: 1f,
            backgroundLayer = ScreenLayerTransform.fromCompact(parts[7]),
            textLayer = ScreenLayerTransform.fromCompact(parts[8]),
            logoLayer = ScreenLayerTransform.fromCompact(parts[9]),
            qrLayer = ScreenLayerTransform.fromCompact(parts[10]),
        )
    }.getOrNull()

private fun ScreenLayerTransform.compact(): String = "$offsetX,$offsetY,$scale"

private fun ScreenLayerTransform.Companion.fromCompact(value: String): ScreenLayerTransform {
    val parts = value.split(",")
    return ScreenLayerTransform(
        offsetX = parts.getOrNull(0)?.toFloatOrNull() ?: 0f,
        offsetY = parts.getOrNull(1)?.toFloatOrNull() ?: 0f,
        scale = parts.getOrNull(2)?.toFloatOrNull() ?: 1f,
    )
}

private fun drawCoverBitmap(
    canvas: android.graphics.Canvas,
    bitmap: Bitmap,
    width: Int,
    height: Int,
    transform: ScreenLayerTransform,
) {
    val scale = maxOf(width / bitmap.width.toFloat(), height / bitmap.height.toFloat()) * transform.scale
    val drawWidth = bitmap.width * scale
    val drawHeight = bitmap.height * scale
    val left = (width - drawWidth) / 2f + transform.offsetX * width / 1000f
    val top = (height - drawHeight) / 2f + transform.offsetY * height / 650f
    canvas.drawBitmap(bitmap, null, Rect(left.toInt(), top.toInt(), (left + drawWidth).toInt(), (top + drawHeight).toInt()), null)
}

private fun drawExportText(
    canvas: android.graphics.Canvas,
    width: Int,
    height: Int,
    transform: ScreenLayerTransform,
    text: String,
    textColor: ScreenTextColor,
    textScale: Float,
) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor.androidColor
        textSize = 58f * textScale * transform.scale
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        setShadowLayer(10f, 0f, 3f, android.graphics.Color.BLACK)
    }
    val x = width * 0.50f + transform.offsetX * width / 1000f
    val y = height * 0.50f + transform.offsetY * height / 650f
    ensureGeolocaliseText(text).lines().forEachIndexed { index, line ->
        canvas.drawText(line, x, y + index * paint.textSize * 1.1f, paint)
    }
}

private fun drawExportBitmap(
    canvas: android.graphics.Canvas,
    bitmap: Bitmap,
    width: Int,
    height: Int,
    transform: ScreenLayerTransform,
    baseWidthRatio: Float,
) {
    val drawWidth = width * baseWidthRatio * transform.scale
    val drawHeight = drawWidth * bitmap.height / bitmap.width
    val centerX = width * 0.50f + transform.offsetX * width / 1000f
    val centerY = height * 0.50f + transform.offsetY * height / 650f
    canvas.drawBitmap(
        bitmap,
        null,
        Rect(
            (centerX - drawWidth / 2f).toInt(),
            (centerY - drawHeight / 2f).toInt(),
            (centerX + drawWidth / 2f).toInt(),
            (centerY + drawHeight / 2f).toInt(),
        ),
        null,
    )
}

private fun exportLogSummaryPdf(context: android.content.Context, outputUri: Uri, log: LogFileEntity): Boolean =
    runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(Pdf.PAGE_WIDTH, Pdf.PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        drawFlightReportPage(context, page.canvas, log)
        document.finishPage(page)

        val telemetryPage = document.startPage(PdfDocument.PageInfo.Builder(Pdf.PAGE_WIDTH, Pdf.PAGE_HEIGHT, 2).create())
        drawFlightReportTelemetryPage(context, telemetryPage.canvas, log)
        document.finishPage(telemetryPage)

        val detailsPage = document.startPage(PdfDocument.PageInfo.Builder(Pdf.PAGE_WIDTH, Pdf.PAGE_HEIGHT, 3).create())
        drawFlightReportDetailsPage(context, detailsPage.canvas, log)
        document.finishPage(detailsPage)

        context.contentResolver.openOutputStream(outputUri)?.use { document.writeTo(it) }
        document.close()
        true
    }.getOrDefault(false)

private object Pdf {
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842
    const val MARGIN = 34f
    const val ORANGE = 0xFFFF7900.toInt()
    const val NAVY = 0xFF061A20.toInt()
    const val PANEL = 0xFF0D252C.toInt()
    const val PANEL_LIGHT = 0xFF15343C.toInt()
    const val TEXT = 0xFFFFFFFF.toInt()
    const val MUTED = 0xFFC7D0D4.toInt()
    const val GREEN = 0xFF2ECC71.toInt()
    const val BLUE = 0xFF20BDF2.toInt()
    const val PURPLE = 0xFFA45CFF.toInt()
}

private fun drawFlightReportPage(context: android.content.Context, canvas: android.graphics.Canvas, log: LogFileEntity) {
    drawPdfBackground(canvas)
    drawPdfHeader(context, canvas, "Rapport de vol", log.fileName, 1)
    drawPdfMetricGrid(canvas, log)
    drawPdfExecutiveSummaryPanel(canvas, log, RectF(34f, 286f, 561f, 444f))
    drawPdfIdentityPanel(canvas, log, RectF(34f, 462f, 561f, 596f))
    drawPdfObservationPanel(canvas, log, RectF(34f, 614f, 561f, 788f))
    drawPdfFooter(canvas)
}

private fun drawFlightReportTelemetryPage(context: android.content.Context, canvas: android.graphics.Canvas, log: LogFileEntity) {
    drawPdfBackground(canvas)
    drawPdfHeader(context, canvas, "Télémétrie détaillée", log.fileName, 2)
    drawPdfTelemetrySection(canvas, log)
    drawPdfFooter(canvas)
}

private fun drawFlightReportDetailsPage(context: android.content.Context, canvas: android.graphics.Canvas, log: LogFileEntity) {
    drawPdfBackground(canvas)
    drawPdfHeader(context, canvas, "Annexe technique", log.fileName, 3)

    val technicalRows = listOf(
        "Fichier" to log.fileName,
        "Statut analyse" to displayDecodeStatus(log.decodeStatus),
        "Version FlightRecord" to (log.djiLogVersion?.toString() ?: "-"),
        "Départ" to (log.flightStartTimeMillis?.let(::formatDate) ?: "-"),
        "Durée" to (log.flightDurationSeconds?.let(::formatDuration) ?: "-"),
        "Distance cumulée" to (log.totalDistanceMeters?.let(::formatMeters) ?: "-"),
        "Distance max au départ" to (buildRealTelemetrySeries(FlightChartKind.DISTANCE, log).maxOrNull()?.let(::formatMeters) ?: "-"),
        "Hauteur max" to (log.maxHeightMeters?.let(::formatMeters) ?: "-"),
        "Vitesse horizontale max" to (log.maxHorizontalSpeedMetersPerSecond?.let(::formatSpeed) ?: "-"),
        "Vitesse verticale max" to (log.maxVerticalSpeedMetersPerSecond?.let(::formatSpeed) ?: "-"),
        "Altitude décollage" to (log.takeoffAltitudeMeters?.let(::formatMeters) ?: "-"),
        "Drone" to listOfNotNull(log.aircraftName, log.productType).joinToString(" / ").ifBlank { "-" },
        "Série drone" to (log.aircraftSerial ?: "-"),
        "Série radiocommande" to (log.rcSerial ?: "-"),
        "Série caméra" to (log.cameraSerial ?: "-"),
        "Série batterie" to (log.batterySerial ?: "-"),
        "Application DJI" to listOfNotNull(log.appPlatform, log.appVersion).joinToString(" ").ifBlank { "-" },
        "Home point" to formatCoordinates(log.homeLatitude, log.homeLongitude),
        "Premier point" to formatCoordinates(log.trajectoryStartLatitude, log.trajectoryStartLongitude),
        "Dernier point" to formatCoordinates(log.trajectoryEndLatitude, log.trajectoryEndLongitude),
        "Points GPS" to ((log.trajectoryPointCount ?: parseTrajectoryPoints(log.trajectoryPoints).size).toString()),
    )
    drawPdfPanel(canvas, RectF(34f, 154f, 561f, 418f), Pdf.PANEL)
    canvas.drawText("Données extraites", 50f, 184f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    drawPdfTwoColumnRows(canvas, technicalRows.take(14), 50f, 204f, 495f)

    drawPdfPanel(canvas, RectF(34f, 438f, 561f, 604f), Pdf.PANEL)
    canvas.drawText("Observations", 50f, 468f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    var y = 496f
    buildPdfObservations(log).take(5).forEach {
        y = drawPdfWrappedText(canvas, "• $it", 54f, y, 490f, pdfBodyPaint(Pdf.MUTED, 10.8f))
        y += 7f
    }
    log.decodeError?.takeIf { it.isNotBlank() }?.let {
        y = drawPdfWrappedText(canvas, "• Analyse : $it", 54f, y, 490f, pdfBodyPaint(Pdf.ORANGE, 10.8f, Typeface.BOLD))
    }

    drawPdfPanel(canvas, RectF(34f, 624f, 561f, 788f), Pdf.PANEL)
    canvas.drawText("Exploitation recommandée", 50f, 654f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    y = 682f
    listOf(
        "Conserver ce PDF avec le FlightRecord DJI original.",
        "Utiliser Export des logs pour copier le log et les médias du même créneau horaire.",
        "Réanalyser le log après mise à jour de l'application pour enrichir les courbes.",
        "Ce rapport ne remplace pas les procédures internes Orange ni l'analyse réglementaire.",
    ).forEach {
        y = drawPdfWrappedText(canvas, "• $it", 54f, y, 500f, pdfBodyPaint(Pdf.MUTED))
        y += 8f
    }
    drawPdfFooter(canvas)
}

private fun pdfColorForFlightEvent(severity: String): Int =
    when (severity.lowercase(Locale.FRANCE)) {
        "critical" -> 0xFFFF4D4D.toInt()
        "warning" -> 0xFFFFC233.toInt()
        else -> Pdf.GREEN
    }

private fun drawPdfBackground(canvas: android.graphics.Canvas) {
    canvas.drawColor(Pdf.NAVY)
    val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Pdf.ORANGE
        alpha = 42
    }
    canvas.drawCircle(Pdf.PAGE_WIDTH / 2f, Pdf.PAGE_HEIGHT + 26f, 240f, glow)
}

private fun drawPdfHeader(
    context: android.content.Context,
    canvas: android.graphics.Canvas,
    title: String,
    subtitle: String,
    pageNumber: Int,
) {
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.orange_drone_compagnon_wordmark)
    logo?.let {
        val target = Rect(34, 26, 224, 84)
        canvas.drawBitmap(it, null, target, Paint(Paint.ANTI_ALIAS_FLAG))
    }
    canvas.drawText(title, 34f, 120f, pdfTitlePaint(26f))
    drawPdfWrappedText(canvas, subtitle, 34f, 144f, 360f, pdfBodyPaint(Pdf.MUTED))
    canvas.drawText("Page $pageNumber", 510f, 48f, pdfBodyPaint(Pdf.MUTED, 11f, Typeface.BOLD))
}

private fun drawPdfMetricGrid(canvas: android.graphics.Canvas, log: LogFileEntity) {
    val metrics = listOf(
        "Départ" to (log.flightStartTimeMillis?.let(::formatDate) ?: "-"),
        "Durée" to (log.flightDurationSeconds?.let(::formatDuration) ?: "-"),
        "Distance" to (log.totalDistanceMeters?.let(::formatMeters) ?: "-"),
        "Hauteur max" to (log.maxHeightMeters?.let(::formatMeters) ?: "-"),
        "Vitesse max" to (log.maxHorizontalSpeedMetersPerSecond?.let(::formatSpeed) ?: "-"),
        "Drone" to (log.aircraftName ?: log.productType ?: "-"),
    )
    val cardWidth = 168f
    val cardHeight = 54f
    metrics.forEachIndexed { index, metric ->
        val col = index % 3
        val row = index / 3
        drawPdfMetricCard(
            canvas = canvas,
            label = metric.first,
            value = metric.second,
            rect = RectF(34f + col * 179f, 160f + row * 62f, 34f + col * 179f + cardWidth, 160f + row * 62f + cardHeight),
        )
    }
}

private fun drawPdfMetricCard(canvas: android.graphics.Canvas, label: String, value: String, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL_LIGHT)
    canvas.drawText(label, rect.left + 12f, rect.top + 19f, pdfBodyPaint(Pdf.MUTED, 10f, Typeface.BOLD))
    canvas.drawText(value.take(28), rect.left + 12f, rect.top + 40f, pdfBodyPaint(Pdf.TEXT, 14f, Typeface.BOLD))
}

private fun drawPdfExecutiveSummaryPanel(canvas: android.graphics.Canvas, log: LogFileEntity, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL)
    canvas.drawText("Synthèse terrain", rect.left + 16f, rect.top + 28f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    canvas.drawText(
        "Résumé directement exploitable pour contrôle, SAV, retour d'expérience ou transmission interne.",
        rect.left + 16f,
        rect.top + 48f,
        pdfBodyPaint(Pdf.MUTED, 10.3f),
    )
    val maxDistance = buildRealTelemetrySeries(FlightChartKind.DISTANCE, log).maxOrNull()
    val rows = listOf(
        "Créneau médias" to mediaTimeWindowLabel(log),
        "Distance max départ" to (maxDistance?.let(::formatMeters) ?: "-"),
        "Home point" to formatCoordinates(log.homeLatitude, log.homeLongitude),
        "Point départ" to formatCoordinates(log.trajectoryStartLatitude, log.trajectoryStartLongitude),
        "Point arrivée" to formatCoordinates(log.trajectoryEndLatitude, log.trajectoryEndLongitude),
        "Fichier original" to "Conservé, non modifié",
    )
    drawPdfTwoColumnRows(canvas, rows, rect.left + 16f, rect.top + 74f, rect.width() - 32f)
}

private fun drawPdfIdentityPanel(canvas: android.graphics.Canvas, log: LogFileEntity, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL)
    canvas.drawText("Identification", rect.left + 16f, rect.top + 28f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    val rows = listOf(
        "Drone" to listOfNotNull(log.aircraftName, log.productType).joinToString(" / ").ifBlank { "-" },
        "Série drone" to (log.aircraftSerial ?: "-"),
        "Radiocommande" to (log.rcSerial ?: "-"),
        "Batterie" to (log.batterySerial ?: "-"),
        "Caméra" to (log.cameraSerial ?: "-"),
        "Application DJI" to listOfNotNull(log.appPlatform, log.appVersion).joinToString(" ").ifBlank { "-" },
    )
    drawPdfTwoColumnRows(canvas, rows, rect.left + 16f, rect.top + 54f, rect.width() - 32f)
}

private fun drawPdfTwoColumnRows(
    canvas: android.graphics.Canvas,
    rows: List<Pair<String, String>>,
    x: Float,
    y: Float,
    width: Float,
) {
    val colWidth = (width - 16f) / 2f
    rows.forEachIndexed { index, (label, value) ->
        val col = index % 2
        val row = index / 2
        val left = x + col * (colWidth + 16f)
        val top = y + row * 31f
        canvas.drawText(label, left, top, pdfBodyPaint(Pdf.ORANGE, 9.6f, Typeface.BOLD))
        canvas.drawText(value.take(36), left, top + 18f, pdfBodyPaint(Pdf.TEXT, 11.2f, Typeface.BOLD))
    }
}

private fun drawPdfRoutePanel(canvas: android.graphics.Canvas, log: LogFileEntity, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL)
    canvas.drawText("Trajectoire", rect.left + 16f, rect.top + 26f, pdfBodyPaint(Pdf.TEXT, 16f, Typeface.BOLD))
    val points = parseTrajectoryPoints(log.trajectoryPoints)
    if (points.size < 2) {
        canvas.drawText("Trajectoire GPS non disponible", rect.left + 16f, rect.centerY(), pdfBodyPaint(Pdf.MUTED, 13f))
        return
    }

    val map = RectF(rect.left + 18f, rect.top + 44f, rect.right - 18f, rect.bottom - 18f)
    val minLat = points.minOf { it.latitude }
    val maxLat = points.maxOf { it.latitude }
    val minLon = points.minOf { it.longitude }
    val maxLon = points.maxOf { it.longitude }
    val latSpan = (maxLat - minLat).takeIf { it > 0.000001 } ?: 0.000001
    val lonSpan = (maxLon - minLon).takeIf { it > 0.000001 } ?: 0.000001
    fun project(point: FlightGeoPoint): android.graphics.PointF {
        val x = map.left + ((point.longitude - minLon) / lonSpan).toFloat() * map.width()
        val y = map.bottom - ((point.latitude - minLat) / latSpan).toFloat() * map.height()
        return android.graphics.PointF(x, y)
    }

    val gridPaint = pdfStrokePaint(0x33FFFFFF, 1.1f)
    repeat(5) { index ->
        val x = map.left + map.width() * index / 4f
        val y = map.top + map.height() * index / 4f
        canvas.drawLine(x, map.top, x, map.bottom, gridPaint)
        canvas.drawLine(map.left, y, map.right, y, gridPaint)
    }

    val shadow = pdfStrokePaint(0xCC000000.toInt(), 7f)
    val route = pdfStrokePaint(Pdf.ORANGE, 4.5f)
    val path = android.graphics.Path().apply {
        points.forEachIndexed { index, point ->
            val projected = project(point)
            if (index == 0) moveTo(projected.x, projected.y) else lineTo(projected.x, projected.y)
        }
    }
    canvas.drawPath(path, shadow)
    canvas.drawPath(path, route)
    drawPdfRouteMarker(canvas, project(points.first()), Pdf.GREEN, "START")
    drawPdfRouteMarker(canvas, project(points.last()), 0xFFFF4D4D.toInt(), "END")
}

private fun drawPdfRouteMarker(canvas: android.graphics.Canvas, point: android.graphics.PointF, color: Int, label: String) {
    canvas.drawCircle(point.x, point.y, 10f, pdfFillPaint(0xCC000000.toInt()))
    canvas.drawCircle(point.x, point.y, 7f, pdfFillPaint(0xFFFFFFFF.toInt()))
    canvas.drawCircle(point.x, point.y, 4.5f, pdfFillPaint(color))
    canvas.drawText(label, point.x + 12f, point.y + 4f, pdfBodyPaint(Pdf.TEXT, 9f, Typeface.BOLD))
}

private fun drawPdfObservationPanel(canvas: android.graphics.Canvas, log: LogFileEntity, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL)
    canvas.drawText("Qualité du décodage", rect.left + 16f, rect.top + 28f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    canvas.drawText(
        "Points clés calculés depuis le FlightRecord DJI décodé.",
        rect.left + 16f,
        rect.top + 48f,
        pdfBodyPaint(Pdf.MUTED, 10.5f),
    )

    val chips = buildPdfQualityChips(log)
    chips.forEachIndexed { index, chip ->
        val col = index % 3
        val row = index / 3
        drawPdfQualityChip(
            canvas = canvas,
            label = chip.label,
            value = chip.value,
            color = chip.color,
            rect = RectF(
                rect.left + 16f + col * 168f,
                rect.top + 72f + row * 56f,
                rect.left + 160f + col * 168f,
                rect.top + 118f + row * 56f,
            ),
        )
    }
}

private data class PdfQualityChip(
    val label: String,
    val value: String,
    val color: Int,
)

private fun buildPdfQualityChips(log: LogFileEntity): List<PdfQualityChip> {
    val telemetry = parseFlightTelemetryPoints(log.trajectoryPoints)
    val battery = buildRealTelemetrySeries(FlightChartKind.BATTERY, log)
    val satellites = buildRealTelemetrySeries(FlightChartKind.SATELLITES, log)
    val gpsPoints = log.trajectoryPointCount ?: parseTrajectoryPoints(log.trajectoryPoints).size
    return listOf(
        PdfQualityChip("Décodage", if (log.decodedAtMillis != null) "OK" else "À analyser", if (log.decodedAtMillis != null) Pdf.GREEN else Pdf.ORANGE),
        PdfQualityChip("GPS", "$gpsPoints points", if (gpsPoints >= 10) Pdf.GREEN else Pdf.ORANGE),
        PdfQualityChip("Télémétrie", "${telemetry.size} échantillons", if (telemetry.size >= 10) Pdf.GREEN else Pdf.ORANGE),
        PdfQualityChip("Batterie", if (battery.isNotEmpty()) "${battery.first().toInt()}% → ${battery.last().toInt()}%" else "Non disponible", if (battery.isNotEmpty()) Pdf.GREEN else Pdf.ORANGE),
        PdfQualityChip("Satellites", satellites.maxOrNull()?.toInt()?.let { "max $it" } ?: "Non disponible", if (satellites.isNotEmpty()) Pdf.GREEN else Pdf.ORANGE),
        PdfQualityChip("Médias", mediaTimeWindowLabel(log), if (log.flightStartTimeMillis != null && log.flightDurationSeconds != null) Pdf.GREEN else Pdf.ORANGE),
    )
}

private fun drawPdfQualityChip(canvas: android.graphics.Canvas, label: String, value: String, color: Int, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL_LIGHT)
    canvas.drawCircle(rect.left + 16f, rect.centerY(), 6f, pdfFillPaint(color))
    canvas.drawText(label, rect.left + 30f, rect.top + 17f, pdfBodyPaint(Pdf.MUTED, 9.5f, Typeface.BOLD))
    canvas.drawText(value.take(26), rect.left + 30f, rect.top + 36f, pdfBodyPaint(Pdf.TEXT, 12.5f, Typeface.BOLD))
}

private fun buildPdfObservations(log: LogFileEntity): List<String> {
    val observations = mutableListOf<String>()
    val distanceMax = buildRealTelemetrySeries(FlightChartKind.DISTANCE, log).maxOrNull()
    val battery = buildRealTelemetrySeries(FlightChartKind.BATTERY, log)
    val satellites = buildRealTelemetrySeries(FlightChartKind.SATELLITES, log)
    val gpsPoints = log.trajectoryPointCount ?: parseTrajectoryPoints(log.trajectoryPoints).size

    observations += "Créneau de recherche média : ${mediaTimeWindowLabel(log)}."
    if (distanceMax != null) observations += "Distance maximale au point de départ : ${formatMeters(distanceMax)}."
    if (log.totalDistanceMeters != null) observations += "Distance cumulée estimée : ${formatMeters(log.totalDistanceMeters)}."
    if (log.maxHeightMeters != null) observations += "Hauteur maximale enregistrée : ${formatMeters(log.maxHeightMeters)}."
    if (battery.isNotEmpty()) {
        observations += "Batterie observée : ${battery.first().toInt()}% au début, ${battery.last().toInt()}% en fin de log."
    } else {
        observations += "Courbe batterie absente ou inexploitable dans ce FlightRecord."
    }
    if (satellites.isNotEmpty()) {
        observations += "Réception satellite : ${satellites.minOrNull()?.toInt()} à ${satellites.maxOrNull()?.toInt()} satellites."
    }
    observations += if (gpsPoints >= 10) {
        "Trajectoire suffisamment dense pour un contrôle visuel du vol."
    } else {
        "Trajectoire peu dense : réanalyser le log ou vérifier le fichier d'origine."
    }
    return observations.take(8)
}

private fun mediaTimeWindowLabel(log: LogFileEntity): String {
    val start = log.flightStartTimeMillis ?: return "Départ inconnu"
    val durationMs = ((log.flightDurationSeconds ?: return "Fin inconnue") * 1000).toLong().coerceAtLeast(0)
    val end = start + durationMs
    return "${formatShortTime(start)} - ${formatShortTime(end)}"
}

private fun drawPdfTelemetrySection(canvas: android.graphics.Canvas, log: LogFileEntity) {
    drawPdfSectionTitle(canvas, "Courbes synchronisées", 154f)
    val charts = listOf(
        FlightChartKind.DISTANCE to RectF(34f, 180f, 561f, 286f),
        FlightChartKind.SPEED to RectF(34f, 306f, 286f, 420f),
        FlightChartKind.ALTITUDE to RectF(309f, 306f, 561f, 420f),
        FlightChartKind.SATELLITES to RectF(34f, 440f, 286f, 554f),
        FlightChartKind.BATTERY to RectF(309f, 440f, 561f, 554f),
    )
    charts.forEach { (kind, rect) ->
        drawPdfChart(canvas, kind, buildRealTelemetrySeries(kind, log), rect)
    }
    drawPdfTelemetryLegend(canvas, log, RectF(34f, 584f, 561f, 766f))
}

private fun drawPdfChart(canvas: android.graphics.Canvas, kind: FlightChartKind, values: List<Double>, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL)
    canvas.drawText(kind.title, rect.left + 12f, rect.top + 21f, pdfBodyPaint(kind.pdfColor(), 13f, Typeface.BOLD))
    if (values.size < 2) {
        canvas.drawText("Donnée non disponible", rect.left + 12f, rect.centerY(), pdfBodyPaint(Pdf.MUTED, 11f))
        return
    }

    val plot = RectF(rect.left + 32f, rect.top + 34f, rect.right - 12f, rect.bottom - 22f)
    val minValue = if (kind == FlightChartKind.BATTERY) 0.0 else values.minOrNull() ?: 0.0
    val maxValue = (values.maxOrNull() ?: 1.0).coerceAtLeast(minValue + 1.0)
    val gridPaint = pdfStrokePaint(0x2AFFFFFF, 1f)
    repeat(4) { index ->
        val y = plot.top + plot.height() * index / 3f
        canvas.drawLine(plot.left, y, plot.right, y, gridPaint)
    }

    val path = android.graphics.Path()
    var previousY: Float? = null
    values.forEachIndexed { index, value ->
        val x = plot.left + plot.width() * index / values.lastIndex
        val normalized = ((value - minValue) / (maxValue - minValue)).toFloat().coerceIn(0f, 1f)
        val y = plot.bottom - normalized * plot.height()
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            if (kind.stepped) previousY?.let { path.lineTo(x, it) }
            path.lineTo(x, y)
        }
        previousY = y
    }
    canvas.drawPath(path, pdfStrokePaint(kind.pdfColor(), 3.2f))
    canvas.drawText(formatAxisValue(maxValue, kind), rect.left + 8f, plot.top + 4f, pdfBodyPaint(Pdf.MUTED, 8.5f))
    canvas.drawText(formatAxisValue(minValue, kind), rect.left + 8f, plot.bottom + 3f, pdfBodyPaint(Pdf.MUTED, 8.5f))
    canvas.drawText("0:00", plot.left, rect.bottom - 7f, pdfBodyPaint(Pdf.MUTED, 8.5f))
    canvas.drawText("fin", plot.right - 12f, rect.bottom - 7f, pdfBodyPaint(Pdf.MUTED, 8.5f))
}

private fun drawPdfTelemetryLegend(canvas: android.graphics.Canvas, log: LogFileEntity, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL)
    canvas.drawText("Lecture des courbes", rect.left + 16f, rect.top + 26f, pdfBodyPaint(Pdf.TEXT, 15f, Typeface.BOLD))
    var y = rect.top + 54f
    val lines = listOf(
        "Distance : distance instantanée entre le drone et son point de départ, pas la distance cumulée.",
        "Satellites : courbe en escaliers, car le nombre de satellites est une valeur entière.",
        "Batterie : affichée uniquement si les enregistrements batterie DJI sont réellement exploitables.",
        "Les courbes sont exportées depuis les points décodés du FlightRecord, sans simulation dans le PDF.",
        "Durée du vol : ${log.flightDurationSeconds?.let(::formatDuration) ?: "non disponible"}."
    )
    lines.forEach {
        y = drawPdfWrappedText(canvas, "• $it", rect.left + 24f, y, rect.width() - 48f, pdfBodyPaint(Pdf.MUTED, 11f))
        y += 8f
    }
}

private fun buildRealTelemetrySeries(kind: FlightChartKind, log: LogFileEntity): List<Double> {
    val telemetry = parseFlightTelemetryPoints(log.trajectoryPoints)
    if (telemetry.size < 2) return emptyList()
    val start = telemetry.first()
    val values = when (kind) {
        FlightChartKind.DISTANCE -> telemetry.map { point ->
            haversineMeters(start.latitude, start.longitude, point.latitude, point.longitude)
        }
        FlightChartKind.SPEED -> telemetry.map { it.speedMetersPerSecond }
        FlightChartKind.SATELLITES -> telemetry.map { it.satellites.toDouble() }
        FlightChartKind.ALTITUDE -> telemetry.map { it.altitudeMeters }
        FlightChartKind.BATTERY -> buildBatteryPercentSeries(telemetry)
    }.filter { it.isFinite() }
    if (values.none { it > 0.0 } && kind != FlightChartKind.ALTITUDE) return emptyList()
    return sampleChartValues(values)
}

private fun FlightChartKind.pdfColor(): Int =
    when (this) {
        FlightChartKind.DISTANCE -> Pdf.GREEN
        FlightChartKind.SPEED -> Pdf.BLUE
        FlightChartKind.SATELLITES -> Pdf.BLUE
        FlightChartKind.ALTITUDE -> Pdf.ORANGE
        FlightChartKind.BATTERY -> 0xFFFFB000.toInt()
    }

private fun drawPdfSectionTitle(canvas: android.graphics.Canvas, title: String, y: Float) {
    canvas.drawText(title, 34f, y, pdfTitlePaint(20f))
    canvas.drawLine(34f, y + 10f, 561f, y + 10f, pdfStrokePaint(0x33FFFFFF, 1f))
}

private fun drawPdfKeyValue(canvas: android.graphics.Canvas, label: String, value: String, y: Float): Float {
    canvas.drawText(label, 42f, y, pdfBodyPaint(Pdf.ORANGE, 10.5f, Typeface.BOLD))
    return drawPdfWrappedText(canvas, value, 184f, y, 360f, pdfBodyPaint(Pdf.TEXT, 11.5f)) + 10f
}

private fun drawPdfWrappedText(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    maxWidth: Float,
    paint: Paint,
): Float {
    var line = ""
    var currentY = y
    text.split(' ').forEach { word ->
        val candidate = if (line.isBlank()) word else "$line $word"
        if (paint.measureText(candidate) <= maxWidth) {
            line = candidate
        } else {
            canvas.drawText(line, x, currentY, paint)
            currentY += paint.textSize + 5f
            line = word
        }
    }
    if (line.isNotBlank()) canvas.drawText(line, x, currentY, paint)
    return currentY + paint.textSize
}

private fun drawPdfPanel(canvas: android.graphics.Canvas, rect: RectF, color: Int) {
    canvas.drawRoundRect(rect, 12f, 12f, pdfFillPaint(color))
    canvas.drawRoundRect(rect, 12f, 12f, pdfStrokePaint(0x38FFFFFF, 1.2f))
}

private fun drawPdfFooter(canvas: android.graphics.Canvas) {
    canvas.drawText("Orange Drone Compagnon", 34f, 818f, pdfBodyPaint(Pdf.MUTED, 10f, Typeface.BOLD))
    canvas.drawText("Rapport généré v${BuildConfig.VERSION_NAME} · fichier DJI original non modifié", 248f, 818f, pdfBodyPaint(Pdf.MUTED, 9.2f))
}

private fun pdfTitlePaint(size: Float): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Pdf.TEXT
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

private fun pdfBodyPaint(color: Int, size: Float = 12f, style: Int = Typeface.NORMAL): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textSize = size
        typeface = Typeface.create(Typeface.DEFAULT, style)
    }

private fun pdfFillPaint(color: Int): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

private fun pdfStrokePaint(color: Int, width: Float): Paint =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        strokeWidth = width
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

@Composable
private fun PlayLogContent(
    logs: List<LogFileEntity>,
    actionMessage: String,
    onDecodeLog: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val decodedLogs = logs.filter { it.decodedAtMillis != null }
    val latestLog = remember(logs) {
        logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    }
    var selectedLogId by remember { mutableStateOf<Long?>(null) }
    var query by remember { mutableStateOf("") }
    var pdfLog by remember { mutableStateOf<LogFileEntity?>(null) }
    var selectedLogMetric by remember { mutableStateOf<PlayLogTileMetric?>(null) }
    val listState = rememberLazyListState()
    val selectedLog = selectedLogId?.let { id -> logs.firstOrNull { it.id == id } }
    val filteredLogs = remember(logs, query) {
        logs
            .sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }
            .filter {
                query.isBlank() ||
                    it.fileName.contains(query, ignoreCase = true) ||
                    formatDate(it.flightStartTimeMillis ?: it.lastModifiedMillis).contains(query, ignoreCase = true)
            }
    }
    val pdfExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val log = pdfLog
        if (uri != null && log != null) {
            exportLogSummaryPdf(context, uri, log)
        }
        pdfLog = null
    }
    LaunchedEffect(selectedLogId) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (selectedLog != null) {
            item {
                FlightLogDetailCard(
                    log = selectedLog,
                    onBack = { selectedLogId = null },
                    onDecodeLog = onDecodeLog,
                    onMetricClick = { selectedLogMetric = it },
                    onExportPdf = {
                        pdfLog = selectedLog
                        pdfExporter.launch("${selectedLog.fileName.substringBeforeLast('.')}_resume.pdf")
                    },
                )
            }
        } else {
            item {
                PlayLogOverviewTiles(
                    logs = logs,
                    decodedCount = decodedLogs.size,
                    latestLog = latestLog,
                    onMetricClick = { selectedLogMetric = it },
                )
            }
            if (actionMessage.isNotBlank()) {
                item {
                    GlassCard(containerAlpha = 0.30f) {
                        Text(
                            actionMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                    }
                }
            }
            item {
                PlayLogSelectionCard(
                    query = query,
                    onQueryChange = { query = it },
                    totalCount = logs.size,
                    visibleCount = filteredLogs.size,
                )
            }
            if (filteredLogs.isEmpty()) {
                item {
                    GlassCard(containerAlpha = 0.30f) {
                        Text(
                            text = if (logs.isEmpty()) "Aucun log détecté pour le moment. L’app scanne automatiquement l’emplacement DJI FlightRecord ; synchronisez pour actualiser." else "Aucun log ne correspond à la recherche.",
                            modifier = Modifier.padding(12.dp),
                            color = Color.White.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    FlightLogPreviewRow(
                        log = log,
                        onOpenLog = { selectedLogId = log.id },
                        onDecodeLog = onDecodeLog,
                        onMetricClick = { selectedLogMetric = it },
                        onExportPdf = {
                            pdfLog = log
                            pdfExporter.launch("${log.fileName.substringBeforeLast('.')}_resume.pdf")
                        },
                    )
                }
            }
        }
        item {
            CopyrightFooter()
        }
    }
    selectedLogMetric?.let { metric ->
        PlayLogMetricDetailDialog(
            metric = metric,
            onDismiss = { selectedLogMetric = null },
        )
    }
}

private data class PlayLogTileMetric(
    val label: String,
    val value: String,
    val unit: String = "",
    val detail: String,
    val accent: Color,
)

@Composable
private fun PlayLogOverviewTiles(
    logs: List<LogFileEntity>,
    decodedCount: Int,
    latestLog: LogFileEntity?,
    onMetricClick: (PlayLogTileMetric) -> Unit,
) {
    val total = logs.size
    val latestTime = latestLog?.flightStartTimeMillis ?: latestLog?.lastModifiedMillis?.takeIf { it > 0 }
    val metrics = listOf(
        PlayLogTileMetric(
            label = "Dernier vol",
            value = latestTime?.let(::formatShortTime) ?: "--:--",
            detail = latestTime?.let(::formatShortDate) ?: "Aucun vol détecté",
            accent = Orange,
        ),
        PlayLogTileMetric(
            label = "Nombre de vols",
            value = total.toString(),
            detail = if (total > 1) "Logs détectés" else "Log détecté",
            accent = Color(0xFF8BE7FF),
        ),
        PlayLogTileMetric(
            label = "Décodage",
            value = decodedCount.toString(),
            unit = if (total > 0) "/$total" else "",
            detail = if (decodedCount == total && total > 0) "Trajectoires prêtes" else "À analyser si besoin",
            accent = if (decodedCount == total && total > 0) Color(0xFF33D16D) else Color(0xFFFFB23D),
        ),
    )
    PlayLogMetricGrid(metrics = metrics, onMetricClick = onMetricClick)
}

@Composable
private fun PlayLogMetricGrid(
    metrics: List<PlayLogTileMetric>,
    onMetricClick: (PlayLogTileMetric) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val spacing = 8.dp
        val columns = when {
            maxWidth >= 760.dp -> 3
            else -> 2
        }
        val tileWidth = ((maxWidth - spacing * (columns - 1)) / columns.toFloat()).coerceIn(142.dp, 286.dp)
        val tileHeight = (tileWidth * 0.62f).coerceIn(118.dp, 178.dp)
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            metrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    rowMetrics.forEach { metric ->
                        PlayLogMetricCard(
                            metric = metric,
                            onClick = { onMetricClick(metric) },
                            modifier = Modifier
                                .width(tileWidth)
                                .height(tileHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayLogMetricCard(
    metric: PlayLogTileMetric,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        containerAlpha = 0.50f,
        borderAlpha = 0.26f,
        highlightAlpha = 0.08f,
        glowColor = metric.accent.copy(alpha = 0.08f),
        fillContainer = true,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tileWidth = maxWidth
            val tileHeight = maxHeight
            val valueSize = when {
                tileHeight <= 124.dp -> 38.sp
                tileHeight <= 160.dp -> 46.sp
                tileWidth >= 270.dp -> 54.sp
                tileWidth >= 220.dp -> 50.sp
                else -> 44.sp
            }
            val unitSize = when {
                tileHeight <= 124.dp -> 13.sp
                tileHeight <= 160.dp -> 15.sp
                tileWidth >= 270.dp -> 17.sp
                tileWidth >= 220.dp -> 16.sp
                else -> 14.sp
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (tileWidth >= 220.dp) 13.dp else 11.dp, vertical = if (tileHeight <= 124.dp) 9.dp else 11.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = metric.label.uppercase(Locale.FRANCE),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (tileWidth >= 220.dp) 15.sp else 13.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = metric.value,
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = valueSize),
                            color = metric.accent,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                        )
                        if (metric.unit.isNotBlank()) {
                            Text(
                                text = metric.unit,
                                style = MaterialTheme.typography.titleSmall.copy(fontSize = unitSize),
                                color = Color.White.copy(alpha = 0.62f),
                                modifier = Modifier.padding(bottom = 9.dp),
                                maxLines = 1,
                            )
                        }
                    }
                    Text(
                        text = metric.detail,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = if (tileWidth >= 220.dp) 13.sp else 12.sp),
                        color = Color.White.copy(alpha = 0.64f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayLogMetricDetailDialog(
    metric: PlayLogTileMetric,
    onDismiss: () -> Unit,
) {
    val description = when (metric.label) {
        "Dernier vol" -> "Date ou heure du dernier log détecté dans le dossier configuré."
        "Nombre de vols" -> "Total des fichiers de vol visibles après synchronisation ou scan du dossier."
        "Décodage" -> "Nombre de logs déjà analysés avec trajectoire et métriques prêtes."
        "Durée" -> "Temps de vol issu du log DJI décodé."
        "Distance" -> "Distance totale calculée depuis les points de trajectoire disponibles."
        "Hauteur" -> "Hauteur maximale atteinte pendant le vol."
        "Points" -> "Nombre de points de trajectoire exploités pour la lecture du vol."
        else -> metric.detail
    }
    OrangeGlassDialog(
        title = metric.label,
        accent = metric.accent,
        onDismiss = onDismiss,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 56.sp),
                color = metric.accent,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
            if (metric.unit.isNotBlank()) {
                Text(
                    text = metric.unit,
                    modifier = Modifier.padding(bottom = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.70f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
        DialogInfoLine("Lecture", metric.detail, metric.accent)
        DialogInfoLine("Utilisation", description, metric.accent)
        DialogInfoLine("Action", "Ouvrir le vol pour voir le graphique, la trajectoire et les informations détaillées.", metric.accent)
    }
}

@Composable
private fun PlayLogSelectionCard(
    query: String,
    onQueryChange: (String) -> Unit,
    totalCount: Int,
    visibleCount: Int,
) {
    GlassCard(
        containerAlpha = 0.42f,
        borderAlpha = 0.24f,
        highlightAlpha = 0.07f,
        glowColor = Color(0xFF9AA8FF).copy(alpha = 0.07f),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Filtrer et choisir",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "$visibleCount/$totalCount",
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xFF9AA8FF).copy(alpha = 0.16f))
                        .border(1.dp, Color(0xFF9AA8FF).copy(alpha = 0.28f), MaterialTheme.shapes.small)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.86f),
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                )
            }
            GlassTextField(
                value = query,
                onValueChange = onQueryChange,
                label = "Filtrer par nom ou date · vide = derniers vols visibles",
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}

@Composable
private fun FlightLogPreviewRow(
    log: LogFileEntity,
    onOpenLog: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onMetricClick: (PlayLogTileMetric) -> Unit,
    onExportPdf: () -> Unit,
) {
    GlassCard(containerAlpha = 0.34f, borderAlpha = 0.24f, highlightAlpha = 0.08f) {
        Column(
            modifier = Modifier
                .clickable(onClick = onOpenLog)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = log.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(
                            log.flightStartTimeMillis?.let(::formatDate) ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate),
                            log.flightDurationSeconds?.let(::formatDuration),
                            log.aircraftName ?: log.productType,
                        ).joinToString(" · ").ifBlank { "Log DJI non analysé" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.70f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                PlayLogStatusChip(
                    text = displayDecodeStatus(log.decodeStatus),
                    color = if (log.decodedAtMillis != null) Color(0xFF33D16D) else Orange,
                )
            }
            FlightLogPreviewMetricTiles(log = log, onMetricClick = onMetricClick)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryFieldButton("Ouvrir", onOpenLog, Modifier.weight(1f))
                SecondaryFieldButton(if (log.decodedAtMillis == null) "Analyser" else "Réanalyser", { onDecodeLog(log.id) }, Modifier.weight(1f))
                SecondaryFieldButton("Rapport PDF", onExportPdf, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FlightLogPreviewMetricTiles(
    log: LogFileEntity,
    onMetricClick: (PlayLogTileMetric) -> Unit,
) {
    CompactPlayLogMetricGrid(metrics = flightLogDetailMetrics(log), onMetricClick = onMetricClick)
}

@Composable
private fun CompactPlayLogMetricGrid(
    metrics: List<PlayLogTileMetric>,
    onMetricClick: (PlayLogTileMetric) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val spacing = 8.dp
        val columns = when {
            maxWidth >= 720.dp -> 4
            maxWidth >= 360.dp -> 2
            else -> 1
        }
        val tileWidth = ((maxWidth - spacing * (columns - 1)) / columns.toFloat()).coerceAtLeast(132.dp)
        val tileHeight = when {
            columns >= 4 -> 96.dp
            else -> 108.dp
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            metrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    rowMetrics.forEach { metric ->
                        PlayLogMetricCard(
                            metric = metric,
                            onClick = { onMetricClick(metric) },
                            modifier = Modifier
                                .width(tileWidth)
                                .height(tileHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayLogStatusChip(text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.16f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.50f)), MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightLogDetailCard(
    log: LogFileEntity,
    onBack: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onMetricClick: (PlayLogTileMetric) -> Unit,
    onExportPdf: () -> Unit,
) {
    var timeline by remember(log.id) { mutableStateOf(0f) }
    var isPlaying by remember(log.id) { mutableStateOf(false) }
    var playbackSpeed by remember(log.id) { mutableStateOf(1f) }
    var selectedTab by remember(log.id) { mutableStateOf(FlightDetailTab.Resume) }
    LaunchedEffect(isPlaying, log.id, playbackSpeed, log.flightDurationSeconds) {
        val durationSeconds = (log.flightDurationSeconds ?: 0.0).coerceAtLeast(1.0)
        var lastTick = System.currentTimeMillis()
        while (isPlaying) {
            delay(100L)
            val now = System.currentTimeMillis()
            val elapsedSeconds = (now - lastTick).coerceAtLeast(0) / 1000.0
            lastTick = now
            timeline = (timeline + (elapsedSeconds * playbackSpeed / durationSeconds).toFloat()).coerceAtMost(1f)
            if (timeline >= 1f) isPlaying = false
        }
    }
    val playPause = {
        if (!isPlaying && timeline >= 0.999f) timeline = 0f
        isPlaying = !isPlaying
    }
    GlassCard(containerAlpha = 0.50f, borderAlpha = 0.42f, highlightAlpha = 0.10f) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FlightViewerTopBar(
                log = log,
                onBack = onBack,
                onDecodeLog = onDecodeLog,
                onMetricClick = onMetricClick,
                onExportPdf = onExportPdf,
            )
            FlightDetailTabs(selected = selectedTab, onSelected = { selectedTab = it })
            when (selectedTab) {
                FlightDetailTab.Resume -> FlightLogResumePanel(log = log)
                FlightDetailTab.Graphiques -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val landscape = maxWidth > 900.dp
                        FlightTelemetryPanel(
                            log = log,
                            timeline = timeline,
                            isPlaying = isPlaying,
                            playbackSpeed = playbackSpeed,
                            duration = log.flightDurationSeconds,
                            onTimelineChange = { timeline = it },
                            onPlayPause = playPause,
                            onPlaybackSpeedChange = { playbackSpeed = it },
                            modifier = if (landscape) Modifier.fillMaxWidth(0.78f) else Modifier.fillMaxWidth(),
                        )
                    }
                }
                FlightDetailTab.Carte -> FlightMapPanel(
                    log = log,
                    timeline = timeline,
                    isPlaying = isPlaying,
                    playbackSpeed = playbackSpeed,
                    duration = log.flightDurationSeconds,
                    onTimelineChange = { timeline = it },
                    onPlayPause = playPause,
                    onPlaybackSpeedChange = { playbackSpeed = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                FlightDetailTab.Infos -> FlightLogSummaryPanel(log = log)
                }
            }
        }
}

private enum class FlightDetailTab(val label: String) {
    Resume("Résumé"),
    Graphiques("Graphiques"),
    Carte("Carte"),
    Infos("Infos"),
}

@Composable
private fun FlightDetailTabs(
    selected: FlightDetailTab,
    onSelected: (FlightDetailTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlightDetailTab.entries.forEach { tab ->
            ToggleChip(
                label = tab.label,
                selected = selected == tab,
                onClick = { onSelected(tab) },
            )
        }
    }
}

@Composable
private fun FlightLogResumePanel(log: LogFileEntity) {
    val observations = remember(log.id, log.decodedAtMillis, log.decodeError) { buildFlightLogObservations(log) }
    val start = formatCoordinates(log.trajectoryStartLatitude ?: log.homeLatitude, log.trajectoryStartLongitude ?: log.homeLongitude)
    val end = formatCoordinates(log.trajectoryEndLatitude, log.trajectoryEndLongitude)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(containerAlpha = 0.30f, borderAlpha = 0.22f, highlightAlpha = 0.06f) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Lecture rapide",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = flightLogPlainSummary(log),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    PlayLogStatusChip(
                        text = if (log.decodedAtMillis != null) "Lisible" else "À analyser",
                        color = if (log.decodedAtMillis != null) Color(0xFF33D16D) else Orange,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniMetric("Départ", log.flightStartTimeMillis?.let(::formatShortTime) ?: "--:--", Modifier.weight(1f))
                    MiniMetric("Points", (log.trajectoryPointCount ?: parseTrajectoryPoints(log.trajectoryPoints).size).takeIf { it > 0 }?.toString() ?: "--", Modifier.weight(1f))
                    MiniMetric("Vitesse max", log.maxHorizontalSpeedMetersPerSecond?.let(::formatSpeed) ?: "-", Modifier.weight(1f))
                }
                InfoLine("Coordonnées départ", start)
                InfoLine("Coordonnées arrivée", end)
            }
        }
        FlightLogObservationPanel(observations)
        FlightLogSummaryPanel(log = log)
    }
}

private data class FlightLogObservation(
    val label: String,
    val detail: String,
    val accent: Color,
)

@Composable
private fun FlightLogObservationPanel(observations: List<FlightLogObservation>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Points à vérifier",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
        observations.forEach { observation ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(observation.accent.copy(alpha = 0.10f))
                    .border(1.dp, observation.accent.copy(alpha = 0.22f), MaterialTheme.shapes.small)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(observation.accent),
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = observation.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = observation.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.70f),
                    )
                }
            }
        }
    }
}

private fun buildFlightLogObservations(log: LogFileEntity): List<FlightLogObservation> {
    val pointCount = log.trajectoryPointCount ?: parseTrajectoryPoints(log.trajectoryPoints).size
    val observations = mutableListOf<FlightLogObservation>()
    if (log.decodedAtMillis == null) {
        observations += FlightLogObservation(
            "Décodage à lancer",
            "Ouvrez Analyser pour obtenir la trajectoire, les graphiques et les métriques fiables.",
            Orange,
        )
    }
    if (!log.decodeError.isNullOrBlank()) {
        observations += FlightLogObservation(
            "Erreur de lecture",
            log.decodeError.take(140),
            Color(0xFFFF4D4D),
        )
    }
    if (pointCount < 2) {
        observations += FlightLogObservation(
            "Trajectoire limitée",
            "La carte et les graphiques GPS seront incomplets tant que le log ne contient pas assez de points.",
            Color(0xFFFFB23D),
        )
    }
    if (log.maxHeightMeters != null && log.maxHeightMeters > 120.0) {
        observations += FlightLogObservation(
            "Hauteur à contrôler",
            "Hauteur maximale lue : ${formatMeters(log.maxHeightMeters)}. À vérifier avec le contexte opérationnel.",
            Color(0xFFFFB23D),
        )
    }
    if (log.totalDistanceMeters == null || log.flightDurationSeconds == null) {
        observations += FlightLogObservation(
            "Métriques partielles",
            "Durée ou distance indisponible : le résumé PDF restera exploitable mais incomplet.",
            Color(0xFFFFB23D),
        )
    }
    if (observations.isEmpty()) {
        observations += FlightLogObservation(
            "Log exploitable",
            "Les principales données sont lisibles : durée, distance, hauteur, points et trajectoire.",
            Color(0xFF33D16D),
        )
    }
    return observations.take(4)
}

private fun flightLogPlainSummary(log: LogFileEntity): String {
    val drone = log.aircraftName ?: log.productType ?: "Drone DJI"
    val date = log.flightStartTimeMillis?.let(::formatDate)
        ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate)
        ?: "date inconnue"
    val duration = log.flightDurationSeconds?.let(::formatDuration) ?: "durée inconnue"
    val distance = log.totalDistanceMeters?.let(::formatMeters) ?: "distance inconnue"
    return "$drone · $date · $duration · $distance"
}

@Composable
private fun FlightViewerTopBar(
    log: LogFileEntity,
    onBack: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onMetricClick: (PlayLogTileMetric) -> Unit,
    onExportPdf: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SecondaryFieldButton("Retour", onBack, Modifier.width(112.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.aircraftName ?: log.productType ?: "Log DJI",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = log.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PlayLogStatusChip(
                text = displayDecodeStatus(log.decodeStatus),
                color = if (log.decodedAtMillis != null) Color(0xFF33D16D) else Orange,
            )
            SecondaryFieldButton(
                text = if (log.decodedAtMillis == null) "Analyser" else "Réanalyser",
                onClick = { onDecodeLog(log.id) },
                modifier = Modifier.width(132.dp),
            )
            SecondaryFieldButton("Rapport PDF", onExportPdf, Modifier.width(150.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FlightMetricPill("Départ", log.flightStartTimeMillis?.let(::formatDate) ?: "-")
            FlightMetricPill("Fin", flightEndDateLabel(log))
            FlightMetricPill("Batterie", log.batterySerial ?: "-")
        }
        FlightLogDetailMetricTiles(log = log, onMetricClick = onMetricClick)
    }
}

private data class FlightLogDetailMetricParts(
    val value: String,
    val unit: String,
    val detail: String,
)

private fun flightLogDetailMetrics(log: LogFileEntity): List<PlayLogTileMetric> {
    val duration = flightDurationMetricParts(log.flightDurationSeconds)
    val distance = flightDistanceMetricParts(log.totalDistanceMeters, detail = "Distance totale")
    val height = flightDistanceMetricParts(log.maxHeightMeters, detail = "Hauteur max", forceMeters = true)
    val pointCount = log.trajectoryPointCount ?: parseTrajectoryPoints(log.trajectoryPoints).size
    val points = if (pointCount > 0) {
        FlightLogDetailMetricParts(pointCount.toString(), "", "Points trajectoire")
    } else {
        FlightLogDetailMetricParts("--", "", "Points trajectoire")
    }

    return listOf(
        PlayLogTileMetric(
            label = "Durée",
            value = duration.value,
            unit = duration.unit,
            detail = duration.detail,
            accent = Orange,
        ),
        PlayLogTileMetric(
            label = "Distance",
            value = distance.value,
            unit = distance.unit,
            detail = distance.detail,
            accent = Color(0xFF8BE7FF),
        ),
        PlayLogTileMetric(
            label = "Hauteur",
            value = height.value,
            unit = height.unit,
            detail = height.detail,
            accent = Color(0xFFFFC233),
        ),
        PlayLogTileMetric(
            label = "Points",
            value = points.value,
            unit = points.unit,
            detail = points.detail,
            accent = Color(0xFF33D16D),
        ),
    )
}

private fun flightDurationMetricParts(seconds: Double?): FlightLogDetailMetricParts {
    val totalSeconds = seconds?.roundToInt()?.coerceAtLeast(0)
        ?: return FlightLogDetailMetricParts("--", "", "Temps de vol")
    if (totalSeconds < 60) {
        return FlightLogDetailMetricParts(totalSeconds.toString(), "s", "Temps de vol")
    }
    if (totalSeconds < 3600) {
        return FlightLogDetailMetricParts(
            value = (totalSeconds / 60).toString(),
            unit = "min",
            detail = String.format(Locale.FRANCE, "%02d s", totalSeconds % 60),
        )
    }
    return FlightLogDetailMetricParts(
        value = (totalSeconds / 3600).toString(),
        unit = "h",
        detail = String.format(Locale.FRANCE, "%02d min", (totalSeconds % 3600) / 60),
    )
}

private fun flightDistanceMetricParts(
    meters: Double?,
    detail: String,
    forceMeters: Boolean = false,
): FlightLogDetailMetricParts {
    meters ?: return FlightLogDetailMetricParts("--", "", detail)
    if (!forceMeters && meters >= 1000.0) {
        return FlightLogDetailMetricParts(formatFlightMetricNumber(meters / 1000.0), "km", detail)
    }
    return FlightLogDetailMetricParts(formatFlightMetricNumber(meters), "m", detail)
}

private fun formatFlightMetricNumber(value: Double): String {
    return String.format(Locale.FRANCE, "%.1f", value)
        .removeSuffix(",0")
        .removeSuffix(".0")
}

@Composable
private fun FlightLogDetailMetricTiles(
    log: LogFileEntity,
    onMetricClick: (PlayLogTileMetric) -> Unit,
) {
    val metrics = flightLogDetailMetrics(log)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val spacing = 8.dp
        val columns = when {
            maxWidth >= 860.dp -> 4
            maxWidth >= 320.dp -> 2
            else -> 1
        }
        val tileWidth = ((maxWidth - spacing * (columns - 1)) / columns.toFloat()).coerceAtMost(245.dp)
        val tileHeight = (tileWidth * 0.62f).coerceIn(116.dp, 152.dp)
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            metrics.chunked(columns).forEach { rowMetrics ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    rowMetrics.forEach { metric ->
                        PlayLogMetricCard(
                            metric = metric,
                            onClick = { onMetricClick(metric) },
                            modifier = Modifier
                                .width(tileWidth)
                                .height(tileHeight),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightMetricPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.54f), maxLines = 1)
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FlightTelemetryPanel(
    log: LogFileEntity,
    timeline: Float,
    isPlaying: Boolean,
    playbackSpeed: Float,
    duration: Double?,
    onTimelineChange: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedChart by remember(log.id) { mutableStateOf(FlightChartKind.DISTANCE) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FlightChartSelector(
            selected = selectedChart,
            onSelected = { selectedChart = it },
        )
        SingleFlightChartPanel(
            kind = selectedChart,
            log = log,
            timeline = timeline,
            modifier = Modifier.height(360.dp),
        )
        FlightPlaybackBar(
            timeline = timeline,
            isPlaying = isPlaying,
            playbackSpeed = playbackSpeed,
            duration = duration,
            onTimelineChange = onTimelineChange,
            onPlayPause = onPlayPause,
            onPlaybackSpeedChange = onPlaybackSpeedChange,
        )
        FlightSpecsPanel(log)
    }
}

@Composable
private fun FlightLogSummaryPanel(
    log: LogFileEntity,
    modifier: Modifier = Modifier,
) {
    val events = remember(log.id) { emptyList<FlightEventUi>() }
    GlassCard(modifier = modifier, containerAlpha = 0.28f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Détails du vol",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            InfoLine("Fichier", log.fileName)
            InfoLine("Début", log.flightStartTimeMillis?.let(::formatDate) ?: "-")
            InfoLine("Fin", flightEndDateLabel(log))
            InfoLine("Durée", log.flightDurationSeconds?.let(::formatDuration) ?: "-")
            InfoLine("Distance", log.totalDistanceMeters?.let(::formatMeters) ?: "-")
            InfoLine("Hauteur max", log.maxHeightMeters?.let(::formatMeters) ?: "-")
            InfoLine("Drone", log.aircraftName ?: log.productType ?: "-")
            InfoLine("N° drone", log.aircraftSerial ?: "-")
            InfoLine("N° RC", log.rcSerial ?: "-")
            FlightEventsPanel(events)
        }
    }
}

private data class FlightEventUi(
    val timeSeconds: Double,
    val severity: String,
    val source: String,
    val category: String,
    val code: String,
    val label: String,
    val detail: String,
)

@Composable
private fun FlightEventsPanel(events: List<FlightEventUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Événements du vol",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (events.isEmpty()) "Aucun" else events.size.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.66f),
                fontWeight = FontWeight.Bold,
            )
        }
        if (events.isEmpty()) {
            Text(
                text = "Aucun message DJI natif détecté dans ce log.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.58f),
            )
        } else {
            events.take(8).forEach { event ->
                FlightEventRow(event)
            }
            if (events.size > 8) {
                Text(
                    text = "+ ${events.size - 8} événement(s) supplémentaire(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.58f),
                )
            }
        }
    }
}

@Composable
private fun FlightEventRow(event: FlightEventUi) {
    val color = when (event.severity.lowercase(Locale.FRANCE)) {
        "critical" -> Color(0xFFFF4D4D)
        "warning" -> Color(0xFFFFC233)
        else -> Color(0xFF33D16D)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.07f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = formatDuration(event.timeSeconds),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            modifier = Modifier.width(56.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = event.label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${event.source} · ${event.category}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.54f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun parseFlightEvents(raw: String?): List<FlightEventUi> =
    runCatching {
        if (raw.isNullOrBlank()) return@runCatching emptyList()
        val array = JSONArray(raw)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            FlightEventUi(
                timeSeconds = item.optDouble("timeSeconds", 0.0),
                severity = item.optString("severity", "info"),
                source = item.optString("source", "dji_record"),
                category = item.optString("category", "dji_message"),
                code = item.optString("code", "DJI_MESSAGE"),
                label = item.optString("label").ifBlank { item.optString("detail", "Message DJI") },
                detail = item.optString("detail"),
            )
        }.sortedBy { it.timeSeconds }
    }.getOrDefault(emptyList())

private enum class FlightChartKind(
    val label: String,
    val title: String,
    val axisLabel: String,
    val color: Color,
    val descending: Boolean = false,
    val stepped: Boolean = false,
) {
    DISTANCE("Distance", "Distance depuis départ / temps de vol", "Distance départ (m)", Color(0xFF20BDF2)),
    SPEED("Vitesse", "Vitesse / temps de vol", "Vitesse (m/s)", Color(0xFF18D5B5)),
    SATELLITES("Satellites", "Réception satellite / temps de vol", "Satellites", Color(0xFF20BDF2), stepped = true),
    ALTITUDE("Altitude", "Altitude / temps de vol", "Altitude (m)", Color(0xFFFF8A1A)),
    BATTERY("Batterie", "Batterie / temps de vol", "Batterie (%)", Color(0xFFFFC233), descending = true, stepped = true),
}

@Composable
private fun FlightChartSelector(
    selected: FlightChartKind,
    onSelected: (FlightChartKind) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlightChartKind.entries.forEach { kind ->
            ToggleChip(
                label = kind.label,
                selected = selected == kind,
                onClick = { onSelected(kind) },
            )
        }
    }
}

@Composable
private fun SingleFlightChartPanel(
    kind: FlightChartKind,
    log: LogFileEntity,
    timeline: Float,
    modifier: Modifier = Modifier,
) {
    val values = remember(kind, log.id, log.decodedAtMillis) {
        buildTelemetrySeries(kind, log)
    }
    val hasValues = values.size >= 2 && values.any { it.isFinite() && it > 0.0 }
    val maxValue = values.maxOrNull()?.takeIf { it > 0.0 } ?: 1.0
    val minValue = values.minOrNull() ?: 0.0
    val range = (maxValue - minValue).takeIf { it > 0.001 } ?: 1.0
    val yAxisValues = remember(kind, values) {
        List(5) { index ->
            val ratio = index / 4.0
            maxValue - range * ratio
        }
    }
    GlassCard(modifier = modifier, containerAlpha = 0.28f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(kind.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                ChartLegend(kind.color, if (hasValues) currentTelemetryLabel(kind, values, timeline) else kind.axisLabel)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(
                    modifier = Modifier
                        .width(58.dp)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                ) {
                    yAxisValues.forEach { value ->
                        Text(
                            text = formatAxisValue(value, kind),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.82f),
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                }
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    val topPad = 8.dp.toPx()
                    val bottomPad = 8.dp.toPx()
                    val chartWidth = size.width
                    val chartHeight = size.height - topPad - bottomPad
                    val gridColor = Color.White.copy(alpha = 0.14f)
                    val axisColor = Color.White.copy(alpha = 0.34f)
                    repeat(5) { index ->
                        val ratio = index / 4f
                        val y = topPad + chartHeight * ratio
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    }
                    repeat(8) { index ->
                        val x = chartWidth * index / 7f
                        drawLine(gridColor.copy(alpha = 0.55f), Offset(x, topPad), Offset(x, topPad + chartHeight), 1.dp.toPx())
                    }
                    drawLine(axisColor, Offset(0f, topPad), Offset(0f, topPad + chartHeight), 1.4.dp.toPx())
                    drawLine(axisColor, Offset(0f, topPad + chartHeight), Offset(size.width, topPad + chartHeight), 1.4.dp.toPx())
                    val path = Path()
                    values.forEachIndexed { index, value ->
                        val t = index / (values.lastIndex.takeIf { it > 0 } ?: 1).toFloat()
                        val normalized = ((value - minValue) / range).toFloat().coerceIn(0f, 1f)
                        val x = chartWidth * t
                        val y = topPad + chartHeight * (1f - normalized)
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else if (kind.stepped) {
                            val previousT = (index - 1) / (values.lastIndex.takeIf { it > 0 } ?: 1).toFloat()
                            val previousX = chartWidth * previousT
                            val previousValue = values[index - 1]
                            val previousNormalized = ((previousValue - minValue) / range).toFloat().coerceIn(0f, 1f)
                            val previousY = topPad + chartHeight * (1f - previousNormalized)
                            path.lineTo(x, previousY)
                            path.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    if (hasValues) {
                        val markerIndex = (timeline.coerceIn(0f, 1f) * values.lastIndex).toInt().coerceIn(values.indices)
                        val markerX = chartWidth * (markerIndex / values.lastIndex.toFloat())
                        val markerValue = values[markerIndex]
                        val markerNormalized = ((markerValue - minValue) / range).toFloat().coerceIn(0f, 1f)
                        val markerY = topPad + chartHeight * (1f - markerNormalized)
                        val progressPath = Path()
                        values.take(markerIndex + 1).forEachIndexed { index, value ->
                            val t = index / (values.lastIndex.takeIf { it > 0 } ?: 1).toFloat()
                            val normalized = ((value - minValue) / range).toFloat().coerceIn(0f, 1f)
                            val x = chartWidth * t
                            val y = topPad + chartHeight * (1f - normalized)
                            if (index == 0) {
                                progressPath.moveTo(x, y)
                            } else if (kind.stepped) {
                                val previousT = (index - 1) / (values.lastIndex.takeIf { it > 0 } ?: 1).toFloat()
                                val previousX = chartWidth * previousT
                                val previousValue = values[index - 1]
                                val previousNormalized = ((previousValue - minValue) / range).toFloat().coerceIn(0f, 1f)
                                val previousY = topPad + chartHeight * (1f - previousNormalized)
                                progressPath.lineTo(x, previousY)
                                progressPath.lineTo(x, y)
                            } else {
                                progressPath.lineTo(x, y)
                            }
                        }
                        drawPath(path, Color.White.copy(alpha = 0.20f), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(progressPath, Color.Black.copy(alpha = 0.50f), style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(progressPath, kind.color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                        drawLine(Color.White.copy(alpha = 0.72f), Offset(markerX, topPad), Offset(markerX, topPad + chartHeight), 2.dp.toPx())
                        drawCircle(Color.Black.copy(alpha = 0.70f), radius = 10.dp.toPx(), center = Offset(markerX, markerY))
                        drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(markerX, markerY))
                        drawCircle(kind.color, radius = 4.dp.toPx(), center = Offset(markerX, markerY))
                    } else {
                        drawPath(path, Color.White.copy(alpha = 0.18f), style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
            Row {
                Spacer(modifier = Modifier.width(68.dp))
                Text("0:00", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.70f))
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (hasValues) {
                        val currentSeconds = (log.flightDurationSeconds ?: 0.0) * timeline.coerceIn(0f, 1f)
                        "${formatDuration(currentSeconds)} / ${log.flightDurationSeconds?.let(::formatDuration) ?: "temps"}"
                    } else {
                        "Donnée non disponible"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.70f),
                )
            }
        }
    }
}

private fun buildTelemetrySeries(kind: FlightChartKind, log: LogFileEntity): List<Double> {
    val telemetry = parseFlightTelemetryPoints(log.trajectoryPoints)
    if (telemetry.size >= 2) {
        val start = telemetry.first()
        val realValues = when (kind) {
            FlightChartKind.DISTANCE -> telemetry.map { point ->
                haversineMeters(start.latitude, start.longitude, point.latitude, point.longitude)
            }
            FlightChartKind.SPEED -> telemetry.map { it.speedMetersPerSecond }
            FlightChartKind.SATELLITES -> telemetry.map { it.satellites.toDouble() }
            FlightChartKind.ALTITUDE -> telemetry.map { it.altitudeMeters }
            FlightChartKind.BATTERY -> buildBatteryPercentSeries(telemetry)
        }.filter { it.isFinite() }
        if (realValues.any { it > 0.0 }) return sampleChartValues(realValues)
    }
    return emptyList()
}

private fun currentTelemetryLabel(kind: FlightChartKind, values: List<Double>, timeline: Float): String {
    val index = (timeline.coerceIn(0f, 1f) * values.lastIndex).toInt().coerceIn(values.indices)
    val value = values[index]
    return "${kind.axisLabel} : ${formatAxisValue(value, kind)}"
}

private fun buildBatteryPercentSeries(telemetry: List<FlightTelemetryPoint>): List<Double> {
    if (telemetry.size < 2) return emptyList()
    val raw = telemetry.map { point ->
        point.batteryPercent.takeIf { it in 1..100 }
    }
    if (raw.count { it != null } < 2) return emptyList()

    val firstKnown = raw.firstNotNullOfOrNull { it } ?: return emptyList()
    var currentPercent = firstKnown
    return raw.map { value ->
        if (value != null) currentPercent = value
        currentPercent.toDouble()
    }
}

private fun sampleChartValues(values: List<Double>, maxPoints: Int = 180): List<Double> {
    if (values.size <= maxPoints) return values
    val step = values.size.toDouble() / (maxPoints - 1).coerceAtLeast(1)
    return List(maxPoints) { index ->
        values[(index * step).toInt().coerceIn(values.indices)]
    }
}

private fun distanceFromStartMeters(log: LogFileEntity): Double {
    val startLat = log.trajectoryStartLatitude ?: log.homeLatitude
    val startLon = log.trajectoryStartLongitude ?: log.homeLongitude
    val endLat = log.trajectoryEndLatitude
    val endLon = log.trajectoryEndLongitude
    val gpsDistance = if (startLat != null && startLon != null && endLat != null && endLon != null) {
        haversineMeters(startLat, startLon, endLat, endLon)
    } else {
        null
    }
    val estimatedMaxDistance = log.totalDistanceMeters?.times(0.36) ?: 160.0
    return maxOf(gpsDistance ?: 0.0, estimatedMaxDistance).coerceAtLeast(30.0)
}

private fun distanceToHomeProfile(t: Double, wobble: Double): Double {
    val base = when {
        t < 0.06 -> smoothStep(t / 0.06) * 0.18
        t < 0.50 -> 0.18
        t < 0.55 -> 0.18 + smoothStep((t - 0.50) / 0.05) * 0.58
        t < 0.60 -> 0.76 + smoothStep((t - 0.55) / 0.05) * 0.24
        t < 0.78 -> 1.00
        t < 0.82 -> 1.00 - smoothStep((t - 0.78) / 0.04) * 0.55
        t < 0.88 -> 0.45 - smoothStep((t - 0.82) / 0.06) * 0.23
        t < 0.94 -> 0.22
        t < 0.97 -> 0.22 - smoothStep((t - 0.94) / 0.03) * 0.20
        else -> 0.0
    }
    return (base + wobble * 0.012).coerceIn(0.0, 1.0)
}

private fun satelliteCountProfile(t: Double, index: Int): Int =
    when {
        t < 0.03 -> if (index % 2 == 0) 27 else 28
        t < 0.08 -> 28
        t < 0.28 -> 30
        t < 0.74 -> 29
        t < 0.90 -> 28
        t < 0.95 -> if (index % 5 == 0) 27 else 28
        else -> 28
    }

private fun smoothStep(value: Double): Double {
    val x = value.coerceIn(0.0, 1.0)
    return x * x * (3 - 2 * x)
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusMeters = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val rLat1 = Math.toRadians(lat1)
    val rLat2 = Math.toRadians(lat2)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(rLat1) * cos(rLat2) * sin(dLon / 2) * sin(dLon / 2)
    return earthRadiusMeters * 2 * asin(sqrt(a))
}

private fun formatAxisValue(value: Double, kind: FlightChartKind): String =
    when (kind) {
        FlightChartKind.BATTERY -> "${value.toInt()}%"
        FlightChartKind.SATELLITES -> value.toInt().toString()
        else -> if (value >= 100) value.toInt().toString() else String.format(Locale.getDefault(), "%.1f", value)
    }

@Composable
private fun FlightChartPanel(
    title: String,
    leftLabel: String,
    rightLabel: String,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    values: List<Double>,
    modifier: Modifier = Modifier,
    descending: Boolean = false,
) {
    GlassCard(modifier = modifier, containerAlpha = 0.28f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                ChartLegend(primary, "Principal")
                Spacer(modifier = Modifier.width(10.dp))
                ChartLegend(secondary, "Secondaire")
                Spacer(modifier = Modifier.width(10.dp))
                ChartLegend(tertiary, "Variation")
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val gridColor = Color.White.copy(alpha = 0.12f)
                repeat(5) { index ->
                    val y = size.height * index / 4f
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }
                repeat(8) { index ->
                    val x = size.width * index / 7f
                    drawLine(gridColor.copy(alpha = 0.55f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                }
                fun drawSeries(color: Color, seed: Double, offset: Float) {
                    val path = Path()
                    val count = 42
                    for (i in 0 until count) {
                        val t = i / (count - 1f)
                        val base = if (descending) 1f - t * 0.62f else 0.18f + t * 0.52f
                        val pulse = (((i * 7 + seed.toInt()) % 11) / 11f) * 0.18f
                        val normalized = (base + pulse + offset).coerceIn(0.05f, 0.94f)
                        val x = size.width * t
                        val y = size.height * (1f - normalized)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
                drawSeries(primary, values.getOrNull(0) ?: 1.0, 0f)
                drawSeries(secondary, values.getOrNull(1) ?: 2.0, -0.12f)
                drawSeries(tertiary, values.getOrNull(2) ?: 3.0, -0.24f)
            }
            Row {
                Text(leftLabel, style = MaterialTheme.typography.bodySmall, color = primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text(rightLabel, style = MaterialTheme.typography.bodySmall, color = tertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.62f), maxLines = 1)
    }
}

@Composable
private fun FlightSpecsPanel(log: LogFileEntity) {
    GlassCard(containerAlpha = 0.26f, borderAlpha = 0.20f, highlightAlpha = 0.05f) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetric("Hauteur max", log.maxHeightMeters?.let(::formatMeters) ?: "-", Modifier.weight(1f))
                MiniMetric("Vitesse H", log.maxHorizontalSpeedMetersPerSecond?.let(::formatSpeed) ?: "-", Modifier.weight(1f))
                MiniMetric("Points GPS", log.trajectoryPointCount?.toString() ?: "-", Modifier.weight(1f))
            }
            InfoLine("Analyse", displayDecodeStatus(log.decodeStatus))
            log.decodeError?.let { InfoLine("Détail", it) }
            InfoLine("Drone", log.aircraftSerial ?: "-")
            InfoLine("RC", log.rcSerial ?: "-")
            InfoLine("App", listOfNotNull(log.appPlatform, log.appVersion).joinToString(" ").ifBlank { "-" })
        }
    }
}

@Composable
private fun FlightMapPanel(
    log: LogFileEntity,
    timeline: Float,
    isPlaying: Boolean,
    playbackSpeed: Float,
    duration: Double?,
    onTimelineChange: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startLat = log.trajectoryStartLatitude ?: log.homeLatitude
    val startLon = log.trajectoryStartLongitude ?: log.homeLongitude
    val endLat = log.trajectoryEndLatitude
    val endLon = log.trajectoryEndLongitude
    val trajectory = remember(log.trajectoryPoints, startLat, startLon, endLat, endLon) {
        parseTrajectoryPoints(log.trajectoryPoints).ifEmpty {
            listOfNotNull(
                if (startLat != null && startLon != null) FlightGeoPoint(startLat, startLon) else null,
                if (endLat != null && endLon != null) FlightGeoPoint(endLat, endLon) else null,
            )
        }
    }
    var mapZoom by remember(log.id) { mutableStateOf(16) }
    Box(
        modifier = modifier
            .height(520.dp)
            .clip(MaterialTheme.shapes.small)
            .background(Color(0xFF091114)),
    ) {
        NativeSatelliteFlightMap(
            startLat = startLat,
            startLon = startLon,
            endLat = endLat,
            endLon = endLon,
            trajectory = trajectory,
            timeline = timeline,
            zoom = mapZoom,
            modifier = Modifier.fillMaxSize(),
        )
        FlightMapControls(modifier = Modifier.align(Alignment.TopStart).padding(14.dp))
        FlightZoomControls(
            zoom = mapZoom,
            onZoomIn = {
                mapZoom = (mapZoom + 1).coerceAtMost(20)
            },
            onZoomOut = {
                mapZoom = (mapZoom - 1).coerceAtLeast(3)
            },
            onReset = {
                mapZoom = 16
            },
            modifier = Modifier.align(Alignment.TopEnd).padding(14.dp),
        )
        FlightPlaybackBar(
            timeline = timeline,
            isPlaying = isPlaying,
            playbackSpeed = playbackSpeed,
            duration = duration,
            onTimelineChange = onTimelineChange,
            onPlayPause = onPlayPause,
            onPlaybackSpeedChange = onPlaybackSpeedChange,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .fillMaxWidth(0.72f),
        )
        Text(
            text = if (startLat != null && startLon != null) {
                "Satellite Esri | départ ${formatCoordinates(startLat, startLon)} | arrivée ${formatCoordinates(endLat, endLon)}"
            } else {
                "Satellite Esri | GPS détaillé non disponible"
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.62f))
                .padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White,
        )
    }
}

@Composable
private fun NativeSatelliteFlightMap(
    startLat: Double?,
    startLon: Double?,
    endLat: Double?,
    endLon: Double?,
    trajectory: List<FlightGeoPoint>,
    timeline: Float,
    zoom: Int,
    modifier: Modifier = Modifier,
) {
    val fallbackLat = 46.603354
    val fallbackLon = 1.888334
    val sLat = startLat ?: fallbackLat
    val sLon = startLon ?: fallbackLon
    val eLat = endLat ?: startLat
    val eLon = endLon ?: startLon
    val hasLine = startLat != null && startLon != null && endLat != null && endLon != null
    val mapPoints = trajectory.ifEmpty { listOf(FlightGeoPoint(sLat, sLon)) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.background(Color(0xFF071114)),
    ) {
        val widthPx = with(density) { maxWidth.roundToPx() }.coerceIn(640, 1600)
        val heightPx = with(density) { maxHeight.roundToPx() }.coerceIn(420, 1000)
        val viewport = remember(mapPoints, sLat, sLon, eLat, eLon, hasLine, zoom, widthPx, heightPx) {
            buildSatelliteViewport(mapPoints, sLat, sLon, eLat, eLon, hasLine, zoom, widthPx, heightPx)
        }
        @SuppressLint("ProduceStateDoesNotAssignValue")
        val bitmap by produceState<Bitmap?>(initialValue = null, key1 = viewport.url) {
            value = null
            value = withContext(Dispatchers.IO) {
                runCatching {
                    URL(viewport.url).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }.onFailure {
                    Log.w("OrangePlayLogMap", "Satellite image native load failed", it)
                }.getOrNull()
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Vue satellite",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF071114), Color(0xFF103034)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Chargement satellite...",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            fun toScreen(lat: Double, lon: Double): Offset {
                val point = mercatorMeters(lat, lon)
                return Offset(
                    x = (((point.x - viewport.minX) / (viewport.maxX - viewport.minX)) * size.width).toFloat(),
                    y = (((viewport.maxY - point.y) / (viewport.maxY - viewport.minY)) * size.height).toFloat(),
                )
            }
            fun drawMapMarker(position: Offset, color: Color) {
                drawCircle(Color.Black.copy(alpha = 0.64f), radius = 18.dp.toPx(), center = position)
                drawCircle(Color.White, radius = 13.dp.toPx(), center = position)
                drawCircle(color, radius = 8.dp.toPx(), center = position)
            }
            fun drawRoute(points: List<Offset>) {
                if (points.size < 2) return
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(path, Color.Black.copy(alpha = 0.72f), style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round))
                drawPath(path, Color(0xFFFF7900), style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
            }

            val start = toScreen(sLat, sLon)
            val screenPoints = mapPoints.map { toScreen(it.latitude, it.longitude) }
            drawRoute(screenPoints)
            if (hasLine && eLat != null && eLon != null) {
                val end = screenPoints.lastOrNull() ?: toScreen(eLat, eLon)
                drawMapMarker(start, Color(0xFF25D366))
                drawMapMarker(end, Color(0xFFFF3B30))
            } else {
                drawMapMarker(start, Color(0xFF20BDF2))
            }
            screenPoints.pointAtTimeline(timeline)?.let { current ->
                drawCircle(Color.Black.copy(alpha = 0.72f), radius = 24.dp.toPx(), center = current)
                drawCircle(Color.White, radius = 17.dp.toPx(), center = current)
                drawCircle(Color(0xFF20BDF2), radius = 11.dp.toPx(), center = current)
            }
        }
    }
}

private data class SatelliteViewport(
    val url: String,
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double,
)

private data class FlightGeoPoint(val latitude: Double, val longitude: Double)

private data class FlightTelemetryPoint(
    val latitude: Double,
    val longitude: Double,
    val timeSeconds: Double,
    val speedMetersPerSecond: Double,
    val satellites: Int,
    val altitudeMeters: Double,
    val batteryPercent: Int,
)

private data class MercatorPoint(val x: Double, val y: Double)

private fun buildSatelliteViewport(
    trajectory: List<FlightGeoPoint>,
    startLat: Double,
    startLon: Double,
    endLat: Double?,
    endLon: Double?,
    hasLine: Boolean,
    zoom: Int,
    widthPx: Int,
    heightPx: Int,
): SatelliteViewport {
    val allPoints = trajectory.ifEmpty {
        listOfNotNull(
            FlightGeoPoint(startLat, startLon),
            if (hasLine && endLat != null && endLon != null) FlightGeoPoint(endLat, endLon) else null,
        )
    }
    val projected = allPoints.map { mercatorMeters(it.latitude, it.longitude) }
    val center = MercatorPoint(
        x = (projected.minOf { it.x } + projected.maxOf { it.x }) / 2,
        y = (projected.minOf { it.y } + projected.maxOf { it.y }) / 2,
    )
    val metersPerPixel = WEB_MERCATOR_WORLD_METERS / (256 * (1 shl zoom.coerceIn(0, 20)))
    val routeHalfWidth = ((projected.maxOf { it.x } - projected.minOf { it.x }) * 0.62).coerceAtLeast(0.0)
    val routeHalfHeight = ((projected.maxOf { it.y } - projected.minOf { it.y }) * 0.62).coerceAtLeast(0.0)
    val halfWidthMeters = maxOf(widthPx * metersPerPixel / 2, routeHalfWidth)
    val halfHeightMeters = maxOf(heightPx * metersPerPixel / 2, routeHalfHeight)
    val minX = center.x - halfWidthMeters
    val minY = center.y - halfHeightMeters
    val maxX = center.x + halfWidthMeters
    val maxY = center.y + halfHeightMeters
    val url = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/export" +
        "?bbox=$minX,$minY,$maxX,$maxY" +
        "&bboxSR=3857&imageSR=3857&size=$widthPx,$heightPx&format=jpg&f=image"
    return SatelliteViewport(url, minX, minY, maxX, maxY)
}

private fun parseTrajectoryPoints(encoded: String?): List<FlightGeoPoint> =
    encoded
        ?.split(';')
        ?.mapNotNull { token ->
            val parts = token.split(',')
            val lat = parts.getOrNull(0)?.toDoubleOrNull()
            val lon = parts.getOrNull(1)?.toDoubleOrNull()
            if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                FlightGeoPoint(lat, lon)
            } else {
                null
            }
        }
        .orEmpty()

private fun parseFlightTelemetryPoints(encoded: String?): List<FlightTelemetryPoint> =
    encoded
        ?.split(';')
        ?.mapNotNull { token ->
            val parts = token.split(',')
            val lat = parts.getOrNull(0)?.toDoubleOrNull()
            val lon = parts.getOrNull(1)?.toDoubleOrNull()
            val time = parts.getOrNull(2)?.toDoubleOrNull()
            val speed = parts.getOrNull(3)?.toDoubleOrNull()
            val satellites = parts.getOrNull(4)?.toIntOrNull()
            val altitude = parts.getOrNull(5)?.toDoubleOrNull()
            val battery = parts.getOrNull(6)?.toIntOrNull()
            if (
                lat != null &&
                lon != null &&
                time != null &&
                speed != null &&
                satellites != null &&
                altitude != null &&
                battery != null &&
                lat in -90.0..90.0 &&
                lon in -180.0..180.0
            ) {
                FlightTelemetryPoint(
                    latitude = lat,
                    longitude = lon,
                    timeSeconds = time,
                    speedMetersPerSecond = speed,
                    satellites = satellites,
                    altitudeMeters = altitude,
                    batteryPercent = battery.coerceIn(0, 100),
                )
            } else {
                null
            }
        }
        .orEmpty()

private fun List<Offset>.pointAtTimeline(timeline: Float): Offset? {
    if (isEmpty()) return null
    if (size == 1) return first()
    val scaled = timeline.coerceIn(0f, 1f) * (lastIndex)
    val index = scaled.toInt().coerceIn(0, lastIndex - 1)
    val local = scaled - index
    val start = this[index]
    val end = this[index + 1]
    return Offset(
        x = start.x + (end.x - start.x) * local,
        y = start.y + (end.y - start.y) * local,
    )
}

private const val WEB_MERCATOR_WORLD_METERS = 40075016.68557849
private const val WEB_MERCATOR_ORIGIN_SHIFT = WEB_MERCATOR_WORLD_METERS / 2

private fun mercatorMeters(lat: Double, lon: Double): MercatorPoint {
    val safeLat = lat.coerceIn(-85.05112878, 85.05112878)
    val x = lon * WEB_MERCATOR_ORIGIN_SHIFT / 180
    val y = Math.log(Math.tan((90 + safeLat) * PI / 360)) / (PI / 180) * WEB_MERCATOR_ORIGIN_SHIFT / 180
    return MercatorPoint(x, y)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SatelliteFlightMap(
    html: String,
    onWebViewReady: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.blockNetworkImage = false
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webViewClient = WebViewClient()
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d("OrangePlayLogMap", "${it.message()} (${it.lineNumber()})")
                        }
                        return true
                    }
                }
                onWebViewReady(this)
                tag = html
                loadDataWithBaseURL("https://orange-drone-compagnon.local/", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            onWebViewReady(webView)
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL("https://orange-drone-compagnon.local/", html, "text/html", "UTF-8", null)
            }
        },
    )
}

private fun buildSatelliteMapHtml(
    startLat: Double?,
    startLon: Double?,
    endLat: Double?,
    endLon: Double?,
): String {
    val fallbackLat = 46.603354
    val fallbackLon = 1.888334
    val sLat = startLat ?: fallbackLat
    val sLon = startLon ?: fallbackLon
    val eLat = endLat ?: startLat ?: fallbackLat
    val eLon = endLon ?: startLon ?: fallbackLon
    val hasLine = startLat != null && startLon != null && endLat != null && endLon != null
    val zoom = if (hasLine) 16 else 6
    return """
<!doctype html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <style>
    html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #091114; overflow: hidden; }
    #map { position: relative; }
    #satellite { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; filter: saturate(1.10) contrast(1.06) brightness(0.90); z-index: 1; opacity: 1; }
    #route { position: absolute; inset: 0; width: 100%; height: 100%; pointer-events: none; z-index: 2; }
    .label { position: absolute; left: 14px; bottom: 14px; padding: 8px 10px; border-radius: 8px; color: white; background: rgba(0,0,0,.56); font: 600 13px system-ui, sans-serif; z-index: 3; }
    .loading { position: absolute; inset: 0; display: grid; place-items: center; color: rgba(255,255,255,.72); font: 700 16px system-ui, sans-serif; background: linear-gradient(135deg, #071114, #0e2427); z-index: 0; }
  </style>
</head>
<body>
  <div id="map"><div class="loading">Chargement satellite...</div><img id="satellite" alt="Satellite Esri" /><canvas id="route"></canvas><div class="label">Esri World Imagery</div></div>
  <script>
    console.log('Orange Drone Compagnon satellite map boot');
    const mapEl = document.getElementById('map');
    const satelliteEl = document.getElementById('satellite');
    const routeEl = document.getElementById('route');
    const start = { lat: $sLat, lon: $sLon };
    const end = { lat: $eLat, lon: $eLon };
    const hasLine = ${hasLine.toString()};
    const resetZoom = $zoom;
    let zoom = resetZoom;
    let center = {
      lat: hasLine ? (start.lat + end.lat) / 2 : start.lat,
      lon: hasLine ? (start.lon + end.lon) / 2 : start.lon
    };

    const worldMeters = 40075016.68557849;
    const originShift = worldMeters / 2;

    function mercator(lat, lon) {
      const x = lon * originShift / 180;
      const safeLat = Math.max(Math.min(lat, 85.05112878), -85.05112878);
      const y = Math.log(Math.tan((90 + safeLat) * Math.PI / 360)) / (Math.PI / 180) * originShift / 180;
      return { x, y };
    }
    function marker(ctx, x, y, color, label) {
      ctx.beginPath();
      ctx.arc(x, y, 10, 0, Math.PI * 2);
      ctx.fillStyle = color;
      ctx.fill();
      ctx.lineWidth = 4;
      ctx.strokeStyle = 'white';
      ctx.stroke();
      ctx.font = '700 12px system-ui, sans-serif';
      ctx.fillStyle = 'white';
      ctx.textAlign = 'center';
      ctx.fillText(label, x, y + 30);
    }
    function render() {
      const width = mapEl.clientWidth || window.innerWidth;
      const height = mapEl.clientHeight || window.innerHeight;
      const centerMeters = mercator(center.lat, center.lon);
      const metersPerPixel = worldMeters / (256 * Math.pow(2, zoom));
      const halfWidthMeters = width * metersPerPixel / 2;
      const halfHeightMeters = height * metersPerPixel / 2;
      const bbox = {
        minX: centerMeters.x - halfWidthMeters,
        minY: centerMeters.y - halfHeightMeters,
        maxX: centerMeters.x + halfWidthMeters,
        maxY: centerMeters.y + halfHeightMeters
      };
      const imageWidth = Math.min(1600, Math.max(640, Math.round(width)));
      const imageHeight = Math.min(1000, Math.max(420, Math.round(height)));
      const imageUrl = 'https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/export'
        + '?bbox=' + [bbox.minX, bbox.minY, bbox.maxX, bbox.maxY].join(',')
        + '&bboxSR=3857&imageSR=3857&size=' + imageWidth + ',' + imageHeight
        + '&format=jpg&f=image';
      if (satelliteEl.src !== imageUrl) {
        satelliteEl.onload = function() { satelliteEl.style.opacity = '1'; console.log('satellite image loaded'); };
        satelliteEl.onerror = function() { console.log('satellite image error ' + imageUrl); };
        console.log('satellite image url ' + imageUrl);
        satelliteEl.src = imageUrl;
      }
      function toScreen(point) {
        return {
          x: (point.x - bbox.minX) / (bbox.maxX - bbox.minX) * width,
          y: (bbox.maxY - point.y) / (bbox.maxY - bbox.minY) * height
        };
      }
      routeEl.width = width;
      routeEl.height = height;
      const ctx = routeEl.getContext('2d');
      ctx.clearRect(0, 0, width, height);
      const s = toScreen(mercator(start.lat, start.lon));
      const e = toScreen(mercator(end.lat, end.lon));
      if (hasLine) {
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(s.x, s.y);
        ctx.lineTo(e.x, e.y);
        ctx.strokeStyle = 'rgba(0,0,0,.7)';
        ctx.lineWidth = 14;
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(s.x, s.y);
        ctx.lineTo(e.x, e.y);
        ctx.strokeStyle = '#ff7900';
        ctx.lineWidth = 9;
        ctx.stroke();
        marker(ctx, s.x, s.y, '#25d366', 'START');
        marker(ctx, e.x, e.y, '#ff3b30', 'END');
      } else {
        marker(ctx, s.x, s.y, '#20bdf2', 'GPS');
      }
    }
    window.orangeDroneCompagnonZoomIn = function() { zoom = Math.min(20, zoom + 1); render(); };
    window.orangeDroneCompagnonZoomOut = function() { zoom = Math.max(3, zoom - 1); render(); };
    window.orangeDroneCompagnonReset = function() {
      zoom = resetZoom;
      center = { lat: hasLine ? (start.lat + end.lat) / 2 : start.lat, lon: hasLine ? (start.lon + end.lon) / 2 : start.lon };
      render();
    };
    window.addEventListener('resize', render);
    render();
  </script>
</body>
</html>
""".trimIndent()
}

@Composable
private fun FlightZoomControls(
    zoom: Int,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier.width(88.dp), containerAlpha = 0.64f, borderAlpha = 0.26f, highlightAlpha = 0.08f) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapZoomButton("+", onZoomIn)
            MapZoomButton("−", onZoomOut)
            Text(
                text = "z$zoom",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
            Text(
                text = "Reset",
                modifier = Modifier
                    .glassControlSurface(
                        shape = MaterialTheme.shapes.small,
                        containerAlpha = 0.10f,
                        borderAlpha = 0.14f,
                        highlightAlpha = 0.10f,
                    )
                    .clickable(onClick = onReset)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun MapZoomButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .glassControlSurface(
                shape = MaterialTheme.shapes.small,
                containerAlpha = 0.11f,
                borderAlpha = 0.16f,
                highlightAlpha = 0.12f,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FlightMapControls(modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.width(214.dp), containerAlpha = 0.62f, borderAlpha = 0.28f, highlightAlpha = 0.08f) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapToggle("3D Terrain", true)
            MapToggle("Satellite", true)
            MapToggle("Télémétrie", true)
            MapToggle("Drone", true)
            Text("COULEUR", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.52f))
            Text(
                text = "Départ → arrivée",
                modifier = Modifier
                    .glassControlSurface(
                        shape = MaterialTheme.shapes.small,
                        containerAlpha = 0.10f,
                        borderAlpha = 0.14f,
                        highlightAlpha = 0.10f,
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun MapToggle(label: String, enabled: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.78f))
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(22.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (enabled) Color(0xFF20BDF2) else Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(16.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White),
            )
        }
    }
}

@Composable
private fun FlightPlaybackBar(
    timeline: Float,
    isPlaying: Boolean,
    playbackSpeed: Float,
    duration: Double?,
    onTimelineChange: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val speedOptions = listOf(1f, 2f, 5f, 10f)
    GlassCard(modifier = modifier, containerAlpha = 0.62f, borderAlpha = 0.28f, highlightAlpha = 0.08f) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .glassControlSurface(
                        shape = MaterialTheme.shapes.small,
                        active = true,
                        accent = Color(0xFF20BDF2),
                        containerAlpha = 0.16f,
                        borderAlpha = 0.34f,
                        highlightAlpha = 0.14f,
                    )
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (isPlaying) "II" else "▶", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Text(formatDurationProgress(timeline, duration), style = MaterialTheme.typography.titleSmall, color = Color.White)
            Slider(
                value = timeline,
                onValueChange = onTimelineChange,
                modifier = Modifier.weight(1f),
            )
            Text(duration?.let(::formatDuration) ?: "-", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.74f))
            Text(
                text = "${playbackSpeed.toInt()}x",
                modifier = Modifier
                    .glassControlSurface(
                        shape = MaterialTheme.shapes.small,
                        active = playbackSpeed > 1f,
                        accent = Orange,
                        containerAlpha = 0.11f,
                        borderAlpha = if (playbackSpeed > 1f) 0.36f else 0.14f,
                        highlightAlpha = 0.11f,
                    )
                    .clickable {
                        val currentIndex = speedOptions.indexOfFirst { it == playbackSpeed }.takeIf { it >= 0 } ?: 0
                        onPlaybackSpeedChange(speedOptions[(currentIndex + 1) % speedOptions.size])
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CopyrightFooter() {
    Text(
        text = "© Loïc Arnold - loic.arnold@orange.com | Orange Drone Compagnon v${BuildConfig.VERSION_NAME}",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 20.dp),
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.58f),
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.82f),
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun displayLogStatus(status: String): String =
    when (status) {
        LogStatus.SENT -> "Envoyé"
        LogStatus.PENDING -> "En attente"
        LogStatus.ERROR -> "Erreur"
        else -> status
    }

private fun displayDecodeStatus(status: String?): String =
    status
        ?.replace("Metadonnees", "Métadonnées")
        ?.replace("decodees", "décodées")
        ?.replace("cle DJI", "clé DJI")
        ?: "Non analysée"

@Composable
private fun LogRow(
    log: LogFileEntity,
    onEmail: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
) {
    GlassCard(containerAlpha = 0.30f) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(log.fileName, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 2)
            Text("Statut : ${displayLogStatus(log.status)}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.82f))
            Text("Taille : ${log.sizeBytes} octets", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.72f))
            log.sentAtMillis?.let {
                Text("Envoyé le : ${formatDate(it)}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.72f))
            }
            log.lastError?.let {
                Text("Erreur : $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFFFFD3C2))
            }
            if (onEmail != null || onShare != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    onEmail?.let {
                        OrangeButton(
                            onClick = it,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text("Mail")
                        }
                    }
                    onShare?.let {
                        OrangeButton(
                            onClick = it,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text("Partager")
                        }
                    }
                }
            }
        }
    }
}

private fun shareLog(context: android.content.Context, log: LogFileEntity) {
    val logUri = Uri.parse(log.filePath)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "[DJI-LOG] ${log.fileName}")
        putExtra(Intent.EXTRA_TEXT, buildString {
            appendLine("Log DJI : ${log.fileName}")
            appendLine("Statut : ${log.status}")
            appendLine("Taille : ${formatBytes(log.sizeBytes)}")
        })
        putExtra(Intent.EXTRA_STREAM, logUri)
        clipData = ClipData.newUri(context.contentResolver, log.fileName, logUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Partager le log DJI").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

private val OrangeDroneFontFamily = FontFamily(
    Font(R.font.helv_neue_35_w1g, FontWeight.Thin),
    Font(R.font.helv_neue_45_w1g, FontWeight.Light),
    Font(R.font.helv_neue_55_w1g, FontWeight.Normal),
    Font(R.font.helv_neue_65_w1g, FontWeight.Medium),
    Font(R.font.helv_neue_75_w1g, FontWeight.Bold),
)

private val OrangeDroneTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.withOrangeDroneFont(FontWeight.Bold),
        displayMedium = base.displayMedium.withOrangeDroneFont(FontWeight.Bold),
        displaySmall = base.displaySmall.withOrangeDroneFont(FontWeight.Bold),
        headlineLarge = base.headlineLarge.withOrangeDroneFont(FontWeight.Bold),
        headlineMedium = base.headlineMedium.withOrangeDroneFont(FontWeight.Bold),
        headlineSmall = base.headlineSmall.withOrangeDroneFont(FontWeight.Medium),
        titleLarge = base.titleLarge.withOrangeDroneFont(FontWeight.Bold),
        titleMedium = base.titleMedium.withOrangeDroneFont(FontWeight.Medium),
        titleSmall = base.titleSmall.withOrangeDroneFont(FontWeight.Medium),
        bodyLarge = base.bodyLarge.withOrangeDroneFont(FontWeight.Normal),
        bodyMedium = base.bodyMedium.withOrangeDroneFont(FontWeight.Normal),
        bodySmall = base.bodySmall.withOrangeDroneFont(FontWeight.Normal),
        labelLarge = base.labelLarge.withOrangeDroneFont(FontWeight.Bold),
        labelMedium = base.labelMedium.withOrangeDroneFont(FontWeight.Medium),
        labelSmall = base.labelSmall.withOrangeDroneFont(FontWeight.Medium),
    )
}

private fun TextStyle.withOrangeDroneFont(weight: FontWeight): TextStyle =
    copy(
        fontFamily = OrangeDroneFontFamily,
        fontWeight = weight,
        letterSpacing = 0.sp,
    )

@Composable
private fun OrangeDroneCompagnonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = OdsColor.Orange,
            onPrimary = OdsColor.Black,
            surface = OdsColor.White,
            background = OdsColor.Ink,
            error = OdsColor.Error,
        ),
        typography = OrangeDroneTypography,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
        ) {
            content()
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    containerAlpha: Float = 0.50f,
    borderAlpha: Float = 0.40f,
    highlightAlpha: Float = 0.12f,
    shape: Shape = RoundedCornerShape(24.dp),
    glowColor: Color = OdsColor.Orange.copy(alpha = 0.05f),
    fillContainer: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .then(if (fillContainer) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
    ) {
        Card(
            modifier = if (fillContainer) Modifier.matchParentSize() else Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = OdsColor.Black.copy(alpha = containerAlpha)),
            border = BorderStroke(1.dp, OdsColor.Border.copy(alpha = borderAlpha)),
            shape = shape,
        ) {
            Box(
                modifier = Modifier
                    .then(if (fillContainer) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                OdsColor.White.copy(alpha = highlightAlpha + 0.018f),
                                OdsColor.White.copy(alpha = highlightAlpha * 0.20f),
                                OdsColor.Black.copy(alpha = 0.24f),
                                OdsColor.Black.copy(alpha = 0.42f),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset.Infinite,
                        ),
                    )
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                glowColor,
                                Color.Transparent,
                            ),
                            center = Offset(0f, 0f),
                            radius = 920f,
                        ),
                    )
            ) {
                content()
            }
        }
    }
}

private fun Modifier.glassControlSurface(
    shape: Shape,
    active: Boolean = false,
    accent: Color = OdsColor.Orange,
    containerAlpha: Float = 0.15f,
    borderAlpha: Float = 0.28f,
    highlightAlpha: Float = 0.16f,
): Modifier = this
    .clip(shape)
    .background(
        OdsColor.Black.copy(alpha = containerAlpha + 0.30f),
        shape,
    )
    .background(
        accent.copy(alpha = if (active) 0.12f else 0.0f),
        shape,
    )
    .background(
        Brush.verticalGradient(
            colors = listOf(
                OdsColor.White.copy(alpha = highlightAlpha * 0.72f),
                OdsColor.White.copy(alpha = highlightAlpha * 0.18f),
                Color.Transparent,
            ),
        ),
        shape,
    )
    .background(
        Brush.radialGradient(
            colors = listOf(
                accent.copy(alpha = if (active) 0.11f else 0.025f),
                Color.Transparent,
            ),
            center = Offset(0f, 0f),
            radius = if (active) 320f else 220f,
        ),
        shape,
    )
    .border(
        BorderStroke(
            1.dp,
            if (active) accent.copy(alpha = borderAlpha + 0.20f) else OdsColor.Border.copy(alpha = borderAlpha),
        ),
        shape,
    )

private fun Modifier.glassMenuSurface(shape: Shape): Modifier = this
    .clip(shape)
    .background(
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF26363A).copy(alpha = 0.98f),
                Color(0xFF111A1D).copy(alpha = 0.98f),
                Color(0xFF05090B).copy(alpha = 0.98f),
            ),
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        ),
    )
    .border(BorderStroke(1.dp, OdsColor.Border.copy(alpha = 0.24f)), shape)

@Composable
private fun OrangeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.glassControlSurface(
            shape = MaterialTheme.shapes.small,
            active = enabled,
            accent = OdsColor.Orange,
            containerAlpha = if (enabled) 0.17f else 0.06f,
            borderAlpha = if (enabled) 0.44f else 0.14f,
            highlightAlpha = 0.16f,
        ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = OdsColor.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = OdsColor.White.copy(alpha = 0.42f),
        ),
        shape = MaterialTheme.shapes.small,
        contentPadding = contentPadding,
        content = content,
    )
}

@Composable
private fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = OdsColor.White.copy(alpha = 0.76f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = OdsColor.White,
                unfocusedTextColor = OdsColor.White,
                cursorColor = OdsColor.Orange,
                focusedBorderColor = OdsColor.Orange,
                unfocusedBorderColor = OdsColor.Border.copy(alpha = 0.30f),
                focusedContainerColor = OdsColor.Black.copy(alpha = 0.38f),
                unfocusedContainerColor = OdsColor.Black.copy(alpha = 0.30f),
            ),
        )
    }
}

private fun createQrBitmap(value: String, size: Int = 768): Bitmap? =
    runCatching {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)

        for (y in 0 until size) {
            for (x in 0 until size) {
                pixels[y * size + x] =
                    if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }

        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }.getOrNull()

private object OdsColor {
    val Orange = Color(0xFFFF7900)
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val Ink = Color(0xFF050B0E)
    val Surface = Color(0xFF11181B)
    val SurfaceSoft = Color(0xFF1D2528)
    val Border = Color(0xFFD6DBDF)
    val TextMuted = Color(0xFFB8C2C7)
    val Success = Color(0xFF32C832)
    val Warning = Color(0xFFFFCC00)
    val Error = Color(0xFFCD3C14)
    val Info = Color(0xFF4BB4E6)
}

private val Orange = OdsColor.Orange

private fun formatCompactDateTime(millis: Long): String =
    SimpleDateFormat("dd/MM/yy HH:mm", Locale.FRANCE).format(Date(millis))

private fun formatShortDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.SHORT, Locale.FRANCE).format(Date(millis))

private fun formatShortTime(millis: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT, Locale.FRANCE).format(Date(millis))

private fun formatDate(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.FRANCE)
        .format(Date(millis))

private fun formatMeters(value: Double): String =
    String.format(Locale.getDefault(), "%.1f m", value)

private fun formatSpeed(value: Double): String =
    String.format(Locale.getDefault(), "%.1f m/s", value)

private fun formatDuration(seconds: Double): String {
    val totalSeconds = seconds.toLong().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return "%d min %02d s".format(Locale.getDefault(), minutes, remainingSeconds)
}

private fun formatDurationShort(seconds: Long): String {
    val totalSeconds = seconds.coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return if (minutes > 0) {
        "reste ${minutes}m ${remainingSeconds}s"
    } else {
        "reste ${remainingSeconds}s"
    }
}

private fun formatDurationProgress(progress: Float, duration: Double?): String {
    val current = ((duration ?: 0.0) * progress.coerceIn(0f, 1f)).toLong().coerceAtLeast(0)
    val minutes = current / 60
    val seconds = current % 60
    return "%d:%02d".format(Locale.getDefault(), minutes, seconds)
}

private fun formatCoordinates(latitude: Double?, longitude: Double?): String =
    if (latitude == null || longitude == null) {
        "-"
    } else {
        String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
    }

private fun formatBytes(bytes: Long): String =
    when {
        bytes >= 1_048_576 -> String.format(Locale.getDefault(), "%.1f Mo", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format(Locale.getDefault(), "%.1f Ko", bytes / 1_024.0)
        else -> "$bytes octets"
    }

private fun formatStorageBytes(bytes: Long): String =
    when {
        bytes >= 1_073_741_824 -> String.format(Locale.getDefault(), "%.1f Go", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format(Locale.getDefault(), "%.0f Mo", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format(Locale.getDefault(), "%.0f Ko", bytes / 1_024.0)
        else -> "$bytes o"
    }

private fun isUsbDestinationAccessible(context: android.content.Context, folderUri: String): Boolean =
    usbDestinationState(context, folderUri) == UsbDestinationState.Ready

private fun buildDiagnosticReport(
    context: Context,
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
): String {
    val usbState = usbDestinationState(context, settings.usbExportUri)
    val usbInfo = usbDiagnosticInfo(context, settings.usbExportUri)
    val logFolderState = logFolderReadiness(context, settings.folderUri)
    val latestLogs = logs
        .sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }
        .take(12)
    val latestEvents = events
        .sortedByDescending { it.createdAtMillis }
        .take(30)

    return buildString {
        appendLine("Orange Drone Compagnon - diagnostic terrain")
        appendLine("Généré le : ${formatDate(System.currentTimeMillis())}")
        appendLine("Version : ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Application : Orange Drone Compagnon")
        appendLine()
        appendLine("Configuration")
        appendLine("- Radiocommande : ${settings.radioId.ifBlank { "-" }}")
        appendLine("- Dossier logs : ${settings.folderLabel.ifBlank { "-" }}")
        appendLine("- État dossier logs : $logFolderState")
        appendLine("- Mode partage : ${settings.deliveryMode}")
        appendLine("- Destinataire mail : ${settings.recipientEmail.ifBlank { "-" }}")
        appendLine("- Technicien : ${settings.technicianEmail.ifBlank { "-" }}")
        appendLine("- Dossier USB : ${settings.usbExportLabel.ifBlank { "-" }}")
        appendLine("- Source médias : drone connecté")
        appendLine()
        appendLine("USB")
        appendLine("- État : ${usbState.destinationLabel}")
        appendLine("- Volumes Android : ${usbInfo.volumeSummary}")
        appendLine("- Périphériques USB : ${usbInfo.deviceSummary}")
        appendLine("- Droit dossier : ${usbInfo.permissionSummary}")
        appendLine()
        appendLine("DJI SDK")
        appendLine("- Disponible : ${djiSdkState.available}")
        appendLine("- Initialisé : ${djiSdkState.initialized}")
        appendLine("- Enregistré : ${djiSdkState.registered}")
        appendLine("- Drone connecté : ${djiSdkState.hasConnectedDrone()}")
        appendLine("- Produit : ${djiSdkState.productId ?: "-"}")
        appendLine("- Type produit : ${djiSdkState.productType ?: "-"}")
        appendLine("- Firmware drone : ${djiSdkState.aircraftFirmwareVersion ?: "-"}")
        appendLine("- Radiocommande : ${djiSdkState.remoteControllerType ?: "-"}")
        appendLine("- Radiocommande connectée : ${djiSdkState.remoteControllerConnected}")
        appendLine("- Firmware RC : ${djiSdkState.remoteControllerFirmwareVersion ?: "-"}")
        appendLine("- Alerte compatibilité : ${djiSdkState.firmwareWarning ?: "-"}")
        appendLine("- Version MSDK : ${djiSdkState.sdkVersion ?: "-"}")
        appendLine("- Message : ${djiSdkState.message}")
        appendLine("- Dernière erreur : ${djiSdkState.lastError ?: "-"}")
        appendLine()
        appendLine("Sources et API")
        appendLine("- DJI Mobile SDK V5 : connexion drone et médias")
        appendLine("- FlightRecord DJI : lecture locale des fichiers .txt")
        appendLine("- MSurvey Orange : dépôt sécurisé des logs")
        appendLine("- Open-Meteo : météo terrain")
        appendLine("- AviationWeather : METAR et TAF")
        appendLine("- Référentiel Sites Orange : export KML vers DJI Pilot 2")
        appendLine()
        appendLine("Compteurs")
        appendLine("- Logs détectés : ${logs.size}")
        appendLine("- Envoyés/exportés : ${logs.count { it.status == LogStatus.SENT }}")
        appendLine("- En attente : ${logs.count { it.status == LogStatus.PENDING }}")
        appendLine("- Erreurs : ${logs.count { it.status == LogStatus.ERROR }}")
        appendLine("- Événements applicatifs : ${events.size}")
        appendLine()
        appendLine("Derniers logs")
        if (latestLogs.isEmpty()) {
            appendLine("- Aucun log détecté")
        } else {
            latestLogs.forEach { log ->
                appendLine(
                    "- ${log.fileName} | ${log.status} | ${formatBytes(log.sizeBytes)} | " +
                        "détecté ${formatDate(log.detectedAtMillis)} | erreur ${log.lastError ?: "-"}",
                )
            }
        }
        appendLine()
        appendLine("Derniers événements")
        if (latestEvents.isEmpty()) {
            appendLine("- Aucun événement")
        } else {
            latestEvents.forEach { event ->
                appendLine("- ${formatDate(event.createdAtMillis)} | ${event.level} | ${event.message}")
            }
        }
        appendLine()
        appendLine("Copyright")
        appendLine("- Loïc Arnold - loic.arnold@orange.com")
        appendLine("- Orange Drone Compagnon v${BuildConfig.VERSION_NAME}")
    }
}

private fun logFolderReadiness(context: Context, folderUri: String): FolderReadiness =
    runCatching {
        if (folderUri.isBlank()) return@runCatching FolderReadiness.Missing
        val uri = Uri.parse(folderUri)
        if (uri.scheme == "file") {
            val requestedPath = uri.path
            val candidates = (
                listOfNotNull(requestedPath) +
                    DEFAULT_DJI_FLIGHT_RECORD_PATHS.takeIf { folderUri == DEFAULT_DJI_FLIGHT_RECORD_URI }.orEmpty()
                ).distinct()
            val readable = candidates.any { path ->
                val folder = java.io.File(path)
                folder.exists() && folder.isDirectory && folder.canRead()
            }
            return@runCatching if (readable) {
                FolderReadiness.Ready
            } else {
                FolderReadiness.Unavailable
            }
        }
        val document = DocumentFile.fromTreeUri(context, uri)
        val hasPersistedPermission = context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission
        }
        if (document?.exists() == true && document.canRead() && hasPersistedPermission) {
            FolderReadiness.Ready
        } else {
            FolderReadiness.Unavailable
        }
    }.getOrDefault(FolderReadiness.Unavailable)

private fun usbDestinationState(context: android.content.Context, folderUri: String): UsbDestinationState =
    runCatching {
        val removableMounted = hasMountedRemovableStorage(context)
        if (folderUri.isBlank()) {
            return@runCatching if (removableMounted) {
                UsbDestinationState.MountedNotAuthorized
            } else {
                UsbDestinationState.NotConfigured
            }
        }
        val uri = Uri.parse(folderUri)
        val document = DocumentFile.fromTreeUri(context, uri)
        val hasPersistedPermission = context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission && permission.isWritePermission
        }
        val exists = document?.exists() == true
        when {
            exists && hasPersistedPermission -> UsbDestinationState.Ready
            exists && document?.canRead() == true && removableMounted -> UsbDestinationState.MountedNotAuthorized
            removableMounted -> UsbDestinationState.MountedNotAuthorized
            else -> UsbDestinationState.Unavailable
        }
    }.getOrDefault(UsbDestinationState.Unavailable)

private enum class UsbDestinationState {
    NotConfigured,
    MountedNotAuthorized,
    Unavailable,
    Ready,
}

private val UsbDestinationState.destinationLabel: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "Clé USB non connectée"
        UsbDestinationState.MountedNotAuthorized -> "Clé USB connectée, dossier à monter"
        UsbDestinationState.Unavailable -> "Clé USB non connectée"
        UsbDestinationState.Ready -> "Clé USB connectée"
    }

private val UsbDestinationState.shortLabel: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "Non connecté"
        UsbDestinationState.MountedNotAuthorized -> "À monter"
        UsbDestinationState.Unavailable -> "Non connecté"
        UsbDestinationState.Ready -> "Connecté"
    }

private val UsbDestinationState.timelineStatus: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "NON CONNECTÉE"
        UsbDestinationState.MountedNotAuthorized -> "À MONTER"
        UsbDestinationState.Unavailable -> "NON ACCESSIBLE"
        UsbDestinationState.Ready -> "CONNECTÉE"
    }

private val UsbDestinationState.timelineDetail: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "Brancher une clé USB"
        UsbDestinationState.MountedNotAuthorized -> "Monter la clé en choisissant son dossier racine"
        UsbDestinationState.Unavailable -> "Clé USB non accessible par Android"
        UsbDestinationState.Ready -> "Clé montée et accessible"
    }

private val UsbDestinationState.color: Color
    get() = when (this) {
        UsbDestinationState.NotConfigured -> Color(0xFFFF4D4D)
        UsbDestinationState.MountedNotAuthorized -> Color(0xFFFFB020)
        UsbDestinationState.Unavailable -> Color(0xFFFF4D4D)
        UsbDestinationState.Ready -> Color(0xFF33D16D)
    }

@Composable
private fun rememberUsbProbeTick(): Int {
    val context = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                tick += 1
            }
        }
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_REMOVED)
            addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            addAction(Intent.ACTION_MEDIA_CHECKING)
            addDataScheme("file")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
    return tick
}

private fun hasMountedRemovableStorage(context: Context): Boolean =
    runCatching {
        val storageManager = context.getSystemService(StorageManager::class.java)
        storageManager.storageVolumes.any { volume ->
            volume.isRemovable && volume.state == Environment.MEDIA_MOUNTED
        }
    }.getOrDefault(false)

private fun usbDiagnosticInfo(context: Context, folderUri: String): UsbDiagnosticInfo {
    val usbManager = context.getSystemService(UsbManager::class.java)
    val storageManager = context.getSystemService(StorageManager::class.java)
    val volumes = runCatching {
        storageManager.storageVolumes
            .filter { it.isRemovable }
            .map { volume ->
                val label = runCatching { volume.getDescription(context) }.getOrNull().orEmpty()
                val state = volume.state.ifBlank { "état inconnu" }
                listOf(label, state)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { state }
            }
    }.getOrDefault(emptyList())
    val devices = runCatching { usbManager.deviceList.values.toList() }.getOrDefault(emptyList())
    val accessories = runCatching { usbManager.accessoryList?.toList().orEmpty() }.getOrDefault(emptyList())
    val permissionSummary = if (folderUri.isBlank()) {
        "Aucun dossier enregistré"
    } else {
        val uri = Uri.parse(folderUri)
        val permission = context.contentResolver.persistedUriPermissions.firstOrNull { it.uri == uri }
        when {
            permission?.isReadPermission == true && permission.isWritePermission -> "Lecture/écriture OK"
            permission?.isReadPermission == true -> "Lecture seule"
            else -> "Permission absente"
        }
    }
    return UsbDiagnosticInfo(
        volumeSummary = if (volumes.isEmpty()) "Aucun volume amovible" else volumes.joinToString(" | "),
        deviceSummary = buildList {
            add("${devices.size} device")
            add("${accessories.size} accessoire")
        }.joinToString(" · "),
        permissionSummary = permissionSummary,
    )
}

private data class UsbDiagnosticInfo(
    val volumeSummary: String,
    val deviceSummary: String,
    val permissionSummary: String,
)

private fun resolveUsbAvailableBytes(folderUri: String): Long? =
    runCatching {
        val volumeId = removableStorageVolumeId(Uri.parse(folderUri)) ?: return@runCatching null
        val mountPoint = File("/storage/$volumeId")
        if (!mountPoint.exists()) return@runCatching null
        StatFs(mountPoint.absolutePath).availableBytes
    }.getOrNull()

private fun removableStorageVolumeId(uri: Uri): String? =
    runCatching {
        val treeId = DocumentsContract.getTreeDocumentId(uri)
        treeId.substringBefore(':')
            .takeIf { it.isNotBlank() && !it.equals("primary", ignoreCase = true) }
    }.getOrNull()
