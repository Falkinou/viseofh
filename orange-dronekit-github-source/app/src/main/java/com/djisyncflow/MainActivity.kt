package com.djisyncflow

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.documentfile.provider.DocumentFile
import com.djisyncflow.data.AppSettings
import com.djisyncflow.data.ActivityEventEntity
import com.djisyncflow.data.DeliveryMode
import com.djisyncflow.data.LogFileEntity
import com.djisyncflow.data.LogStatus
import com.djisyncflow.data.SmtpSecurity
import com.djisyncflow.data.isReadyForSync
import com.djisyncflow.dji.DjiSdkState
import com.djisyncflow.mail.EmbeddedMailConfig
import com.djisyncflow.ui.MainViewModel
import com.djisyncflow.ui.UsbKitProgress
import com.djisyncflow.ui.UsbKitStage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

private const val CHECKMAP_URL = "https://john1340a.github.io/drone/"
private const val MSURVEY_DRONELOG_URL = "https://msurvey.orange.com/dronelog"
private const val CHECKMAP_LOG_TAG = "OrangeCheckmap"
private const val OFFICIAL_DOC_BASE_URL = "https://dronekit.orange.intra/docs"
private const val CHECKMAP_VIEWPORT_FIX_SCRIPT = """
    (function () {
      var map = document.getElementById('map');
      if (!map) return;
      document.documentElement.style.height = '100%';
      document.documentElement.style.margin = '0';
      document.body.style.height = '100%';
      document.body.style.margin = '0';
      document.body.style.overflow = 'hidden';
      map.style.position = 'absolute';
      map.style.top = '0';
      map.style.right = '0';
      map.style.bottom = '0';
      map.style.left = '0';
      map.style.width = '100%';
      var targetHeight = Math.max(
        window.innerHeight || 0,
        document.documentElement.clientHeight || 0,
        document.body.clientHeight || 0,
        screen.height || 0,
        600
      );
      map.style.height = targetHeight + 'px';
      map.style.minHeight = targetHeight + 'px';
      function resizeMap() {
        try {
          var controller = window.app && window.app.getMapController && window.app.getMapController();
          var mapInstance = controller && controller.mapService && controller.mapService.getMap && controller.mapService.getMap();
          if (mapInstance && mapInstance.resize) mapInstance.resize();
        } catch (error) {
          console.error('Checkmap resize failed', error);
        }
      }
      resizeMap();
      setTimeout(resizeMap, 250);
      setTimeout(resizeMap, 1000);
      setTimeout(resizeMap, 2500);
    })();
"""

private data class DroneKitTheme(
    val id: String,
    val label: String,
    val description: String,
    val colors: List<Color>,
)

private val DroneKitThemes = listOf(
    DroneKitTheme(
        id = "dronekit",
        label = "DroneKit",
        description = "Bleu pétrole, orange doux",
        colors = listOf(Color(0xFF052E36), Color(0xFF061B22), Color(0xFF02080B)),
    ),
    DroneKitTheme(
        id = "graphite",
        label = "Graphite",
        description = "Noir lisible, reflets froids",
        colors = listOf(Color(0xFF1D2528), Color(0xFF0E1518), Color(0xFF030607)),
    ),
    DroneKitTheme(
        id = "orange_night",
        label = "Orange nuit",
        description = "Ambiance terrain sombre",
        colors = listOf(Color(0xFF2E1B0D), Color(0xFF07171B), Color(0xFF020608)),
    ),
    DroneKitTheme(
        id = "high_readability",
        label = "Contraste",
        description = "Lecture prioritaire en extérieur",
        colors = listOf(Color(0xFF173740), Color(0xFF0A2028), Color(0xFF050A0D)),
    ),
)
private val ModuleIconSteel = Color(0xFFDDE7EB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SyncflowTheme {
                val viewModel: MainViewModel = viewModel()
                SyncflowScreen(viewModel)
            }
        }
    }
}

private enum class AppModule(val label: String, val tileDescription: String) {
    HOME("Accueil", "Boîte à outils terrain pour RC DJI"),
    SYNC_LOG("SyncLog", "Vérifier les logs et préparer un envoi ciblé"),
    PLAY_LOG("PlayLog", "Analyser un log, la data et la trajectoire"),
    USB_KIT("FlightExportUSB", "Exporter automatiquement logs et médias sur clé USB"),
    SCREEN("Screen", "Créer un fond d’écran RC avec photo"),
    CHECKMAP("Checkmap", "Consulter la carte des restrictions drone"),
    INFO("Info", "Afficher un QR Code vers la documentation interne"),
    PINPOINT("PinPoint", "Exporter les sites Orange ANFR vers DJI Pilot 2"),
    SETTINGS("Réglages", "Réglages dossiers, USB, radio et application"),
}

private enum class ResponsiveClass {
    Compact,
    Medium,
    Expanded,
}

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

private enum class SyncLogTab(val label: String) {
    Mail("Mail"),
    Msurvey("MSURVEY"),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncflowScreen(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val djiSdkState by viewModel.djiSdkState.collectAsStateWithLifecycle()
    val usbKitProgress by viewModel.usbKitProgress.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    var draft by remember { mutableStateOf(settings) }
    var selectedModule by remember { mutableStateOf(AppModule.HOME) }
    var showAboutInfo by remember { mutableStateOf(false) }
    var infoStartPanel by remember { mutableStateOf(InfoStartPanel.Documents) }
    var sunlightMode by remember { mutableStateOf(false) }
    var lastUsbAutoPromptTick by remember { mutableStateOf(-1) }
    var lastUsbAutoExportKey by remember { mutableStateOf("") }
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()

    LaunchedEffect(settings) {
        draft = settings
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
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val runtimePermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    LaunchedEffect(Unit) {
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
        AppBackground(themeId = settings.appTheme)

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
                            onOpenSettings = {
                                showAboutInfo = false
                                selectedModule = AppModule.SETTINGS
                            },
                            onOpenInfo = { showAboutInfo = true },
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
                            onBackHome = { showAboutInfo = false },
                            onOpenSettings = {
                                showAboutInfo = false
                                selectedModule = AppModule.SETTINGS
                            },
                        )
                    } else if (selectedModule != AppModule.HOME) {
                        ModuleReturnBar(
                            module = selectedModule,
                            onBackHome = { selectedModule = AppModule.HOME },
                            onOpenSettings = {
                                showAboutInfo = false
                                selectedModule = AppModule.SETTINGS
                            },
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (showAboutInfo) {
                            AboutInfoContent(
                                settings = settings,
                                logs = logs,
                                djiSdkState = djiSdkState,
                                onOpenFullDiagnostic = {
                                    showAboutInfo = false
                                    infoStartPanel = InfoStartPanel.AdminLogs
                                    selectedModule = AppModule.INFO
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else when (selectedModule) {
                            AppModule.HOME -> {
                                HomeContent(
                                    settings = settings,
                                    logs = logs,
                                    djiSdkState = djiSdkState,
                                    sunlightMode = sunlightMode,
                                    onToggleSunlightMode = { sunlightMode = !sunlightMode },
                                    onRefreshLogs = viewModel::refreshLogFolder,
                                    onSelectModule = {
                                        showAboutInfo = false
                                        selectedModule = it
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.PLAY_LOG -> {
                                PlayLogContent(
                                    logs = logs,
                                    events = events,
                                    actionMessage = actionMessage,
                                    onDecodeLog = viewModel::decodeLog,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.SCREEN -> {
                                OrangeScreenContent(
                                    settings = settings,
                                    onPickScreenExportFolder = { screenExportFolderPicker.launch(null) },
                                    onSaveScreenProject = viewModel::saveScreenProject,
                                    onAddScreenExportHistory = viewModel::addScreenExportHistory,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.CHECKMAP -> {
                                CheckmapContent(modifier = Modifier.fillMaxSize())
                            }
                            AppModule.INFO -> {
                                InfoContent(
                                    settings = settings,
                                    logs = logs,
                                    events = events,
                                    djiSdkState = djiSdkState,
                                    initialPanel = infoStartPanel,
                                    onInitialPanelConsumed = { infoStartPanel = InfoStartPanel.Documents },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.PINPOINT -> {
                                ComingSoonToolContent(
                                    module = selectedModule,
                                    title = "PinPoint",
                                    body = "Recuperation des sites Orange ANFR France et export PinPoint pour DJI Pilot 2.",
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.SYNC_LOG -> {
                                SyncLogOverviewContent(
                                    settings = settings,
                                    logs = logs,
                                    events = events,
                                    actionMessage = actionMessage,
                                    onSyncNow = { viewModel.syncNow(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                    onSaveMailSettings = viewModel::saveSyncLogMailSettings,
                                    onSendLatestByMail = { recipient, technician ->
                                        viewModel.sendLatestLogByMail(recipient, technician)
                                    },
                                    onSendLogByMail = viewModel::sendLogByMail,
                                    onRetryErrors = { viewModel.retryErrors(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                    onCheckUpdate = viewModel::checkForUpdateNow,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.USB_KIT -> {
                                UsbKitContent(
                                    settings = settings,
                                    logs = logs,
                                    events = events,
                                    djiSdkState = djiSdkState,
                                    usbKitProgress = usbKitProgress,
                                    actionMessage = actionMessage,
                                    onSyncNow = { viewModel.syncNow(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                    onExportLog = { logId -> viewModel.exportLatestUsb(draft.copy(deliveryMode = DeliveryMode.USB), logId) },
                                    onRecoverDroneMedia = { logId -> viewModel.recoverDroneMedia(logId) },
                                    onRetryErrors = { viewModel.retryErrors(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                    onCheckUpdate = viewModel::checkForUpdateNow,
                                    onOpenSettings = {
                                        showAboutInfo = false
                                        selectedModule = AppModule.SETTINGS
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            AppModule.SETTINGS -> {
                                SettingsContent(
                                    draft = draft,
                                    settings = settings,
                                    logs = logs,
                                    djiSdkState = djiSdkState,
                                    actionMessage = actionMessage,
                                    onDraftChange = { draft = it },
                                    onPickFolder = { folderPicker.launch(null) },
                                    onPickUsbFolder = { usbFolderPicker.launch(null) },
                                    onPickMediaFolder = { mediaFolderPicker.launch(null) },
                                    onPickScreenExportFolder = { screenExportFolderPicker.launch(null) },
                                    onThemeChange = viewModel::saveAppTheme,
                                    onOpenModule = {
                                        showAboutInfo = false
                                        selectedModule = it
                                    },
                                    onRefreshLogs = viewModel::refreshLogFolder,
                                    onSave = { viewModel.saveSettings(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                    onTest = { viewModel.testDestination(draft.copy(deliveryMode = DeliveryMode.USB)) },
                                    modifier = Modifier.fillMaxSize(),
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
private fun AppBackground(themeId: String) {
    val theme = DroneKitThemes.firstOrNull { it.id == themeId } ?: DroneKitThemes.first()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(theme.colors)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Orange.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                        center = Offset(0.50f, 1.04f),
                        radius = 920f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.32f),
                        ),
                    ),
                ),
        )
    }
}

@Composable
private fun HeaderBar(
    selectedModule: AppModule,
    onOpenSettings: () -> Unit,
    onOpenInfo: () -> Unit,
    onModuleChange: (AppModule) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val screenClass = currentResponsiveClass(maxWidth)
        val shortLandscape = isShortLandscapeScreen()
        val compact = screenClass == ResponsiveClass.Compact
        val medium = screenClass == ResponsiveClass.Medium
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
                    text = " DroneKit",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = titleSize),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SettingsHeaderButton(onClick = onOpenSettings, controlSize = buttonSize, iconSize = iconSize)
            InfoHeaderButton(onClick = onOpenInfo, controlSize = buttonSize, iconSize = iconSize)
            ModuleDropdown(
                selectedModule = selectedModule,
                onModuleChange = onModuleChange,
                controlSize = buttonSize,
                iconSize = if (compact) 23.dp else 28.dp,
            )
        }
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
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(iconSize)) {
            val color = Color.White
            val strokeWidth = 2.7.dp.toPx()
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(color, radius = size.width * 0.19f, center = Offset(cx, cy), style = Stroke(strokeWidth))
            drawCircle(color, radius = size.width * 0.37f, center = Offset(cx, cy), style = Stroke(strokeWidth))
            repeat(8) { index ->
                val angle = Math.toRadians((index * 45).toDouble())
                val inner = size.width * 0.35f
                val outer = size.width * 0.48f
                drawLine(
                    color,
                    Offset(cx + kotlin.math.cos(angle).toFloat() * inner, cy + kotlin.math.sin(angle).toFloat() * inner),
                    Offset(cx + kotlin.math.cos(angle).toFloat() * outer, cy + kotlin.math.sin(angle).toFloat() * outer),
                    strokeWidth,
                    StrokeCap.Round,
                )
            }
        }
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
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.16f))
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
private fun InfoHeaderButton(
    onClick: () -> Unit,
    controlSize: Dp = 52.dp,
    iconSize: Dp = 31.dp,
) {
    Box(
        modifier = Modifier
            .size(controlSize)
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(iconSize)) {
            drawCircle(Color.White, radius = size.minDimension / 2.35f, style = Stroke(3.dp.toPx()))
        }
        Text(
            text = "i",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = if (controlSize < 48.dp) 22.sp else 26.sp),
            color = Color.White,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun ModuleDropdown(
    selectedModule: AppModule,
    onModuleChange: (AppModule) -> Unit,
    controlSize: Dp = 52.dp,
    iconSize: Dp = 28.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .size(controlSize)
                .clip(MaterialTheme.shapes.small)
                .background(Color.White.copy(alpha = 0.16f))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(iconSize)) {
                val lineStroke = 3.2.dp.toPx()
                drawLine(Color.White, Offset(size.width * 0.12f, size.height * 0.25f), Offset(size.width * 0.88f, size.height * 0.25f), lineStroke, StrokeCap.Round)
                drawLine(Color.White, Offset(size.width * 0.12f, size.height * 0.50f), Offset(size.width * 0.88f, size.height * 0.50f), lineStroke, StrokeCap.Round)
                drawLine(Color.White, Offset(size.width * 0.12f, size.height * 0.75f), Offset(size.width * 0.88f, size.height * 0.75f), lineStroke, StrokeCap.Round)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF11181B)),
        ) {
            AppModule.entries.forEach { module ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = module.label,
                            color = if (module == selectedModule) Orange else Color.White,
                            fontWeight = if (module == selectedModule) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        expanded = false
                        onModuleChange(module)
                    },
                )
            }
        }
    }
}

private fun moduleSubtitle(module: AppModule): String =
    when (module) {
        AppModule.HOME -> "Boîte à outils terrain pour RC DJI"
        AppModule.SYNC_LOG -> "Suivi et partage cible des logs DJI"
        AppModule.PLAY_LOG -> "Lecture et contrôle des logs de vol"
        AppModule.USB_KIT -> "Export terrain vers clé USB"
        AppModule.SCREEN -> "Création de fonds d’écran RC"
        AppModule.CHECKMAP -> "Carte des restrictions drone"
        AppModule.INFO -> "Documentation interne Orange"
        AppModule.PINPOINT -> "Export sites ANFR vers DJI Pilot 2"
        AppModule.SETTINGS -> "Réglages centralisés"
    }

@Composable
private fun ModuleReturnBar(
    module: AppModule,
    onBackHome: () -> Unit,
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
            BackHomeHeaderButton(onClick = onBackHome, controlSize = controlSize, iconSize = iconSize)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.label,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = if (compact) 19.sp else 23.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
    onBackHome: () -> Unit,
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
            BackHomeHeaderButton(onClick = onBackHome, controlSize = controlSize, iconSize = iconSize)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "À propos",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = if (compact) 19.sp else 23.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
    onToggleSunlightMode: () -> Unit,
    onRefreshLogs: () -> Unit,
    onSelectModule: (AppModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(settings.folderUri) {
        if (settings.folderUri.isNotBlank()) {
            onRefreshLogs()
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            HomeDashboardPanel(
                settings = settings,
                logs = logs,
                djiSdkState = djiSdkState,
                sunlightMode = sunlightMode,
                onToggleSunlightMode = onToggleSunlightMode,
                onRefreshLogs = onRefreshLogs,
                onSelectModule = onSelectModule,
            )
        }
    }
}

@Composable
private fun HomeDashboardPanel(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    djiSdkState: DjiSdkState,
    sunlightMode: Boolean,
    onToggleSunlightMode: () -> Unit,
    onRefreshLogs: () -> Unit,
    onSelectModule: (AppModule) -> Unit,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val pending = logs.count { it.status == LogStatus.PENDING || it.status == LogStatus.ERROR }
    val latestFlight = logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    val latestFlightMillis = latestFlight?.flightStartTimeMillis ?: latestFlight?.lastModifiedMillis
    val latestFlightDateTime = latestFlightMillis?.let { formatCompactDateTime(it) } ?: "--/-- --:--"
    val usbState = remember(settings.usbExportUri, usbProbeTick) { usbDestinationState(context, settings.usbExportUri) }
    val usbAccessible = usbState == UsbDestinationState.Ready
    val usbAvailableBytes = remember(settings.usbExportUri, usbAccessible, usbProbeTick) {
        if (usbAccessible) resolveUsbAvailableBytes(settings.usbExportUri) else null
    }
    val droneDetail = when {
        djiSdkState.productConnected -> djiSdkState.productId?.let { "Modèle DJI #$it" } ?: "Modèle détecté"
        djiSdkState.registered -> "Connecter le drone"
        djiSdkState.initialized -> "Enregistrement DJI..."
        djiSdkState.available -> djiSdkState.message
        else -> "SDK DJI indisponible"
    }
    val statusCards = listOf(
        HomeStatus(
            label = "Checkmap",
            value = "Ouvrir",
            detail = "Carte restrictions drone",
            iconModule = AppModule.CHECKMAP,
            state = HomeStatusState.Navigate,
            action = HomeStatusAction.OpenModule(AppModule.CHECKMAP),
        ),
        HomeStatus(
            label = "Dernier vol",
            value = latestFlightDateTime,
            detail = latestFlight?.let {
                latestFlight.flightDurationSeconds?.let { duration -> "Durée ${formatDuration(duration)}" } ?: "Durée inconnue"
            } ?: "Actualiser les logs",
            iconModule = AppModule.PLAY_LOG,
            state = if (latestFlight != null) HomeStatusState.Navigate else HomeStatusState.Error,
            action = HomeStatusAction.OpenModule(AppModule.PLAY_LOG),
        ),
        HomeStatus(
            label = "Drone connecté",
            value = if (djiSdkState.productConnected) "Connecté" else "Non connecté",
            detail = droneDetail,
            iconModule = AppModule.SYNC_LOG,
            state = if (djiSdkState.productConnected) HomeStatusState.Ok else HomeStatusState.Error,
            action = HomeStatusAction.OpenModule(AppModule.USB_KIT),
        ),
        HomeStatus(
            label = "Clé USB",
            value = if (usbAccessible) "Connectée" else "Non connectée",
            detail = when {
                usbAccessible && usbAvailableBytes != null -> "${formatStorageBytes(usbAvailableBytes)} disponibles"
                usbAccessible -> "Espace non lisible"
                else -> usbState.destinationLabel
            },
            iconModule = AppModule.USB_KIT,
            state = if (usbAccessible) HomeStatusState.Ok else HomeStatusState.Error,
            action = HomeStatusAction.OpenModule(if (usbAccessible) AppModule.USB_KIT else AppModule.SETTINGS),
        ),
        HomeStatus(
            label = "À exporter",
            value = pending.toString(),
            detail = if (pending == 0) "Tout est sauvegardé" else "Log(s) en attente",
            iconModule = AppModule.USB_KIT,
            state = if (pending == 0) HomeStatusState.Ok else HomeStatusState.Warning,
            action = HomeStatusAction.OpenModule(AppModule.USB_KIT),
        ),
    )
    val tools = listOf(
        AppModule.SYNC_LOG,
        AppModule.PLAY_LOG,
        AppModule.USB_KIT,
        AppModule.SCREEN,
        AppModule.INFO,
        AppModule.PINPOINT,
    )
    var detailsExpanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color(0xFF7FA6AD).copy(alpha = if (sunlightMode) 0.74f else 0.54f)),
        shape = MaterialTheme.shapes.small,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            val screenClass = currentResponsiveClass(maxWidth, maxHeight)
            val compact = screenClass == ResponsiveClass.Compact
            val landscape = maxWidth > 780.dp || configuration.screenWidthDp > configuration.screenHeightDp
            val shortLandscape = compact && configuration.screenWidthDp > configuration.screenHeightDp
            val veryShortLandscape = shortLandscape && maxWidth < 1120.dp
            val columns = if (landscape) 3 else 2
            val primaryStatusCards = listOfNotNull(
                statusCards.firstOrNull { it.label == "Dernier vol" },
                statusCards.firstOrNull { it.label == "Drone connecté" },
                statusCards.firstOrNull { it.label == "Clé USB" },
            )
            val secondaryStatusCards = statusCards.filterNot { it in primaryStatusCards }
            val shownStatusCards = if (detailsExpanded || !landscape) statusCards else primaryStatusCards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (compact) 8.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            ) {
                if (shortLandscape) {
                    if (!veryShortLandscape) {
                        HomeDashboardCompactBar(
                            detailsExpanded = detailsExpanded,
                            hiddenCount = if (detailsExpanded) 0 else secondaryStatusCards.size,
                            onToggleDetails = { detailsExpanded = !detailsExpanded },
                        )
                    }
                    if (detailsExpanded) {
                        HomeStatusMiniStrip(
                            cards = statusCards,
                            sunlightMode = sunlightMode,
                            onSelectModule = onSelectModule,
                            onRefreshLogs = onRefreshLogs,
                        )
                    } else {
                        HomeStatusMiniStrip(
                            cards = primaryStatusCards,
                            sunlightMode = sunlightMode,
                            onSelectModule = onSelectModule,
                            onRefreshLogs = onRefreshLogs,
                        )
                    }
                    HomeToolRows(
                        tools = tools,
                        columns = 3,
                        sunlightMode = sunlightMode,
                        onSelectModule = onSelectModule,
                        compact = true,
                        dense = true,
                    )
                } else {
                    HomeDashboardSummaryBar(
                        detailsExpanded = detailsExpanded,
                        hiddenCount = if (detailsExpanded || !landscape) 0 else secondaryStatusCards.size,
                        compact = compact,
                        onToggleDetails = { detailsExpanded = !detailsExpanded },
                    )
                    if (landscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp),
                    ) {
                        HomeStatusRows(
                            cards = shownStatusCards,
                            sunlightMode = sunlightMode,
                            onSelectModule = onSelectModule,
                            onRefreshLogs = onRefreshLogs,
                            compact = compact,
                            modifier = Modifier.weight(1.02f),
                        )
                        HomeToolRows(
                            tools = tools,
                            columns = columns,
                            sunlightMode = sunlightMode,
                            onSelectModule = onSelectModule,
                            compact = true,
                            modifier = Modifier.weight(1.36f),
                        )
                    }
                    } else {
                        HomeStatusRows(statusCards, sunlightMode, onSelectModule, onRefreshLogs, compact = compact)
                        HomeToolRows(tools, 2, sunlightMode, onSelectModule, compact = compact)
                    }
                    HomeDashboardFooter(sunlightMode = sunlightMode, onToggleSunlightMode = onToggleSunlightMode)
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
            DroneKitIcon(
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 10.dp),
    ) {
        cards.forEach { card ->
            HomeStatusCard(
                status = card,
                sunlightMode = sunlightMode,
                compact = compact,
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshCard = status.action == HomeStatusAction.RefreshLogs
    GlassCard(
        modifier = modifier
            .height(if (compact) 72.dp else 84.dp)
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
        DroneKitIcon(
            module = module,
            modifier = Modifier.size(if (compact) 34.dp else 44.dp),
            color = statusIconColor(module),
            thin = true,
        )
    }
}

private fun statusIconColor(module: AppModule): Color =
    when (module) {
        AppModule.SYNC_LOG -> Color(0xFF2DD4BF)
        AppModule.PLAY_LOG -> Color(0xFF38BDF8)
        AppModule.USB_KIT -> Color(0xFFFBBF24)
        AppModule.SCREEN -> Color(0xFF8B5CF6)
        AppModule.CHECKMAP -> Color(0xFF22C55E)
        AppModule.PINPOINT -> Color(0xFFEF4444)
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
    columns: Int,
    sunlightMode: Boolean,
    onSelectModule: (AppModule) -> Unit,
    compact: Boolean = false,
    dense: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (dense) 4.dp else if (compact) 8.dp else 12.dp),
    ) {
        tools.chunked(columns).forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (dense) 4.dp else if (compact) 8.dp else 12.dp),
            ) {
                rowTools.forEach { module ->
                    HomeToolTile(
                        module = module,
                        sunlightMode = sunlightMode,
                        compact = compact,
                        dense = dense,
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
    sunlightMode: Boolean,
    compact: Boolean = false,
    dense: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = module == AppModule.USB_KIT
    GlassCard(
        modifier = modifier
            .aspectRatio(if (dense) 4.15f else if (compact) 2.15f else 1.16f)
            .clickable(onClick = onClick),
        containerAlpha = when {
            sunlightMode && primary -> 0.76f
            sunlightMode -> 0.66f
            primary -> 0.62f
            else -> 0.56f
        },
        borderAlpha = if (primary) 0.72f else if (sunlightMode) 0.52f else 0.36f,
        highlightAlpha = if (primary) 0.18f else if (sunlightMode) 0.10f else 0.08f,
    ) {
        if (dense) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                DroneKitIcon(
                    module = module,
                    modifier = Modifier.size(27.dp),
                    color = ModuleIconSteel,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = module.label,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (compact) 8.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                DroneKitIcon(
                    module = module,
                    modifier = Modifier.size(if (compact) 38.dp else if (primary) 74.dp else 68.dp),
                    color = ModuleIconSteel,
                )
                Spacer(Modifier.height(if (compact) 4.dp else 12.dp))
                Text(
                    text = module.label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (compact) 17.sp else if (primary) 28.sp else 26.sp,
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardFooter(
    sunlightMode: Boolean,
    onToggleSunlightMode: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (sunlightMode) "Plein soleil ON" else "Plein soleil",
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(if (sunlightMode) Orange else Color.White.copy(alpha = 0.16f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onToggleSunlightMode),
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DroneKitIcon(
    module: AppModule,
    modifier: Modifier = Modifier,
    color: Color = Orange,
    thin: Boolean = false,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = if (thin) 3.5.dp.toPx() else 5.dp.toPx()
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        val w = size.width
        val h = size.height

        when (module) {
            AppModule.SYNC_LOG -> {
                val cloud = Path().apply {
                    moveTo(w * 0.28f, h * 0.68f)
                    cubicTo(w * 0.18f, h * 0.68f, w * 0.12f, h * 0.60f, w * 0.12f, h * 0.52f)
                    cubicTo(w * 0.12f, h * 0.42f, w * 0.20f, h * 0.35f, w * 0.31f, h * 0.36f)
                    cubicTo(w * 0.36f, h * 0.24f, w * 0.48f, h * 0.18f, w * 0.60f, h * 0.24f)
                    cubicTo(w * 0.68f, h * 0.27f, w * 0.73f, h * 0.34f, w * 0.75f, h * 0.42f)
                    cubicTo(w * 0.83f, h * 0.43f, w * 0.89f, h * 0.50f, w * 0.89f, h * 0.58f)
                    cubicTo(w * 0.89f, h * 0.64f, w * 0.84f, h * 0.68f, w * 0.76f, h * 0.68f)
                    close()
                }
                drawPath(cloud, color)
                drawLine(Color.Black, Offset(w * 0.50f, h * 0.74f), Offset(w * 0.50f, h * 0.45f), strokeWidth * 0.82f, StrokeCap.Round)
                drawLine(Color.Black, Offset(w * 0.38f, h * 0.56f), Offset(w * 0.50f, h * 0.44f), strokeWidth * 0.82f, StrokeCap.Round)
                drawLine(Color.Black, Offset(w * 0.62f, h * 0.56f), Offset(w * 0.50f, h * 0.44f), strokeWidth * 0.82f, StrokeCap.Round)
            }
            AppModule.PLAY_LOG -> {
                drawLine(color, Offset(w * 0.18f, h * 0.82f), Offset(w * 0.18f, h * 0.18f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(w * 0.18f, h * 0.82f), Offset(w * 0.82f, h * 0.82f), strokeWidth, StrokeCap.Round)
                val path = Path().apply {
                    moveTo(w * 0.28f, h * 0.66f)
                    lineTo(w * 0.42f, h * 0.50f)
                    lineTo(w * 0.55f, h * 0.58f)
                    lineTo(w * 0.74f, h * 0.34f)
                }
                drawPath(path, color, style = stroke)
            }
            AppModule.USB_KIT -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.27f, h * 0.30f),
                    size = Size(w * 0.58f, h * 0.40f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f, w * 0.08f),
                )
                drawRect(
                    color = color,
                    topLeft = Offset(w * 0.10f, h * 0.39f),
                    size = Size(w * 0.20f, h * 0.22f),
                )
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(w * 0.34f, h * 0.36f),
                    size = Size(w * 0.03f, h * 0.28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.015f, w * 0.015f),
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.82f),
                    topLeft = Offset(w * 0.13f, h * 0.43f),
                    size = Size(w * 0.045f, h * 0.045f),
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.82f),
                    topLeft = Offset(w * 0.22f, h * 0.43f),
                    size = Size(w * 0.045f, h * 0.045f),
                )
                drawCircle(
                    color = Color.Black.copy(alpha = 0.78f),
                    radius = w * 0.045f,
                    center = Offset(w * 0.72f, h * 0.50f),
                )
                drawLine(
                    color = Color.Black.copy(alpha = 0.34f),
                    start = Offset(w * 0.45f, h * 0.42f),
                    end = Offset(w * 0.62f, h * 0.42f),
                    strokeWidth = strokeWidth * 0.50f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color.Black.copy(alpha = 0.34f),
                    start = Offset(w * 0.45f, h * 0.58f),
                    end = Offset(w * 0.62f, h * 0.58f),
                    strokeWidth = strokeWidth * 0.50f,
                    cap = StrokeCap.Round,
                )
            }
            AppModule.SCREEN -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.16f, h * 0.24f),
                    size = Size(w * 0.68f, h * 0.52f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f, w * 0.03f),
                    style = stroke,
                )
                val mountains = Path().apply {
                    moveTo(w * 0.24f, h * 0.68f)
                    lineTo(w * 0.40f, h * 0.52f)
                    lineTo(w * 0.52f, h * 0.64f)
                    lineTo(w * 0.62f, h * 0.54f)
                    lineTo(w * 0.78f, h * 0.70f)
                }
                drawPath(mountains, color, style = stroke)
                drawCircle(color, radius = w * 0.045f, center = Offset(w * 0.70f, h * 0.36f))
            }
            AppModule.CHECKMAP -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.16f, h * 0.18f),
                    size = Size(w * 0.68f, h * 0.64f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, w * 0.04f),
                    style = stroke,
                )
                drawLine(color, Offset(w * 0.38f, h * 0.18f), Offset(w * 0.38f, h * 0.82f), strokeWidth * 0.72f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.62f, h * 0.18f), Offset(w * 0.62f, h * 0.82f), strokeWidth * 0.72f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.16f, h * 0.40f), Offset(w * 0.84f, h * 0.40f), strokeWidth * 0.72f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.16f, h * 0.62f), Offset(w * 0.84f, h * 0.62f), strokeWidth * 0.72f, StrokeCap.Round)
                drawCircle(color, radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.50f))
                drawLine(Color.Black.copy(alpha = 0.72f), Offset(w * 0.50f, h * 0.37f), Offset(w * 0.50f, h * 0.63f), strokeWidth * 0.54f, StrokeCap.Round)
                drawLine(Color.Black.copy(alpha = 0.72f), Offset(w * 0.37f, h * 0.50f), Offset(w * 0.63f, h * 0.50f), strokeWidth * 0.54f, StrokeCap.Round)
            }
            AppModule.INFO -> {
                drawCircle(color, radius = w * 0.33f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawCircle(color, radius = strokeWidth * 0.62f, center = Offset(w * 0.50f, h * 0.34f))
                drawLine(color, Offset(w * 0.50f, h * 0.48f), Offset(w * 0.50f, h * 0.68f), strokeWidth, StrokeCap.Round)
            }
            AppModule.PINPOINT -> {
                drawCircle(color, radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawCircle(color, radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(color, Offset(w * 0.50f, h * 0.10f), Offset(w * 0.50f, h * 0.26f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(w * 0.50f, h * 0.74f), Offset(w * 0.50f, h * 0.90f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(w * 0.10f, h * 0.50f), Offset(w * 0.26f, h * 0.50f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(w * 0.74f, h * 0.50f), Offset(w * 0.90f, h * 0.50f), strokeWidth, StrokeCap.Round)
            }
            AppModule.HOME -> {
                drawCircle(color, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                drawLine(color, Offset(w * 0.28f, h * 0.50f), Offset(w * 0.72f, h * 0.50f), strokeWidth, StrokeCap.Round)
                drawLine(color, Offset(w * 0.50f, h * 0.28f), Offset(w * 0.50f, h * 0.72f), strokeWidth, StrokeCap.Round)
            }
            AppModule.SETTINGS -> {
                drawCircle(color, radius = w * 0.24f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                repeat(8) { index ->
                    val angle = Math.toRadians((index * 45).toDouble())
                    val inner = w * 0.34f
                    val outer = w * 0.46f
                    drawLine(
                        color,
                        Offset(w * 0.50f + kotlin.math.cos(angle).toFloat() * inner, h * 0.50f + kotlin.math.sin(angle).toFloat() * inner),
                        Offset(w * 0.50f + kotlin.math.cos(angle).toFloat() * outer, h * 0.50f + kotlin.math.sin(angle).toFloat() * outer),
                        strokeWidth,
                        StrokeCap.Round,
                    )
                }
            }
        }
    }
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
        AppModule.PLAY_LOG -> "PL"
        AppModule.USB_KIT -> "USB"
        AppModule.SCREEN -> "SC"
        AppModule.CHECKMAP -> "CM"
        AppModule.INFO -> "QR"
        AppModule.PINPOINT -> "PP"
        AppModule.HOME -> "DK"
        AppModule.SETTINGS -> "SE"
    }

@Composable
private fun SyncLogOverviewContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    actionMessage: String,
    onSyncNow: () -> Unit,
    onSaveMailSettings: (String, String) -> Unit,
    onSendLatestByMail: (String, String) -> Unit,
    onSendLogByMail: (Long) -> Unit,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val latestLog = logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    var selectedTab by remember { mutableStateOf(SyncLogTab.Msurvey) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GlassCard(containerAlpha = 0.46f, borderAlpha = 0.24f, highlightAlpha = 0.07f) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SyncLogTab.entries.forEach { tab ->
                    ToggleChip(
                        label = tab.label,
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (selectedTab == SyncLogTab.Msurvey) "Dépôt via formulaire Orange" else "Envoi direct par mail",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                    modifier = Modifier.align(Alignment.CenterVertically),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (selectedTab == SyncLogTab.Msurvey) {
            MsurveySyncLogContent(
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SyncLogSimpleCard(
                        settings = settings,
                        logs = logs,
                        actionMessage = actionMessage,
                        onSyncNow = onSyncNow,
                        onSaveMailSettings = onSaveMailSettings,
                        onSendLatestByMail = onSendLatestByMail,
                        canSendLatestByMail = latestLog != null,
                        onRetryErrors = onRetryErrors,
                        onCheckUpdate = onCheckUpdate,
                    )
                }
                item {
                    HistoryTitle()
                }
                items(logs, key = { it.id }) { log ->
                    LogRow(
                        log = log,
                        onEmail = { onSendLogByMail(log.id) },
                        onShare = { shareLog(context, log) },
                    )
                }
                item {
                    CopyrightFooter()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MsurveySyncLogContent(
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

@Composable
private fun SyncLogSimpleCard(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    actionMessage: String,
    onSyncNow: () -> Unit,
    onSaveMailSettings: (String, String) -> Unit,
    onSendLatestByMail: (String, String) -> Unit,
    canSendLatestByMail: Boolean,
    onRetryErrors: () -> Unit,
    onCheckUpdate: () -> Unit,
) {
    val latestLog = logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    val pending = logs.count { it.status == LogStatus.PENDING || it.status == LogStatus.ERROR }
    val sent = logs.count { it.status == LogStatus.SENT }
    val errors = logs.count { it.status == LogStatus.ERROR }
    var recipientDraft by remember(settings.recipientEmail) { mutableStateOf(settings.recipientEmail) }
    var technicianDraft by remember(settings.technicianEmail) { mutableStateOf(settings.technicianEmail) }
    val mailReady = recipientDraft.isNotBlank() && canSendLatestByMail

    GlassCard(containerAlpha = 0.58f, borderAlpha = 0.38f, highlightAlpha = 0.10f) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("SyncLog", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Black)
            Text(
                text = "Envoyer rapidement le dernier log de vol par mail.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MiniMetric("Détectés", logs.size.toString(), Modifier.weight(1f))
                MiniMetric("Envoyés", sent.toString(), Modifier.weight(1f))
                MiniMetric("À traiter", pending.toString(), Modifier.weight(1f))
                MiniMetric("Erreurs", errors.toString(), Modifier.weight(1f))
            }
            InfoLine("Dossier logs", settings.folderLabel.ifBlank { "Non configuré" })
            InfoLine(
                "Dernier vol",
                latestLog?.flightStartTimeMillis?.let(::formatDate)
                    ?: latestLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatDate)
                    ?: "Aucun log",
            )
            InfoLine("Expéditeur", EmbeddedMailConfig.senderEmail)
            latestLog?.fileName?.let { InfoLine("Fichier", it) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GlassTextField(
                    value = recipientDraft,
                    onValueChange = { recipientDraft = it },
                    label = "Adresse destinataire",
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                GlassTextField(
                    value = technicianDraft,
                    onValueChange = { technicianDraft = it },
                    label = "Mail technicien",
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
            }
            if (actionMessage.isNotBlank()) {
                Text(
                    text = actionMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
            OrangeButton(
                onClick = { onSendLatestByMail(recipientDraft, technicianDraft) },
                enabled = mailReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
            ) {
                Text("Envoyer le dernier log", fontWeight = FontWeight.Black)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SecondaryFieldButton(
                    text = "Scanner",
                    onClick = onSyncNow,
                    modifier = Modifier.weight(1f),
                )
                SecondaryFieldButton(
                    text = "Enregistrer",
                    onClick = { onSaveMailSettings(recipientDraft, technicianDraft) },
                    modifier = Modifier.weight(1f),
                )
                SecondaryFieldButton(
                    text = "Réessayer",
                    onClick = onRetryErrors,
                    modifier = Modifier.weight(1f),
                    enabled = errors > 0,
                )
            }
        }
    }
}

@Composable
private fun MiniMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.07f))
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
        modifier = modifier.height(48.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.11f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.34f),
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Text(text, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CheckmapContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
                WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.evaluateJavascript(CHECKMAP_VIEWPORT_FIX_SCRIPT, null)
                            view?.postDelayed({
                                view.evaluateJavascript(CHECKMAP_VIEWPORT_FIX_SCRIPT, null)
                            }, 3_000L)
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?,
                        ) {
                            callback?.invoke(origin, true, false)
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.setGeolocationEnabled(true)
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = false
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    loadUrl(CHECKMAP_URL)
                }
            },
            update = { webView ->
                if (webView.url != CHECKMAP_URL) {
                    webView.loadUrl(CHECKMAP_URL)
                }
            },
        )
    }
}

private data class InfoDocument(
    val title: String,
    val description: String,
    val badge: String,
    val url: String,
)

private val OfficialInfoDocuments = listOf(
    InfoDocument(
        title = "Check-list prévol",
        description = "Contrôles RC, batterie, zone, météo, autorisations et points sécurité avant décollage.",
        badge = "PDF",
        url = "$OFFICIAL_DOC_BASE_URL/checklist-prevol",
    ),
    InfoDocument(
        title = "Guide Checkmap",
        description = "Lecture des restrictions de vol et préparation rapide de la mission terrain.",
        badge = "DOC",
        url = "$OFFICIAL_DOC_BASE_URL/checkmap",
    ),
    InfoDocument(
        title = "Export FlightExportUSB",
        description = "Procédure d'export des logs et médias drone vers clé USB après intervention.",
        badge = "PROC",
        url = "$OFFICIAL_DOC_BASE_URL/export-usb",
    ),
    InfoDocument(
        title = "Récupération médias DJI",
        description = "Associer les médias drone au dernier vol et contrôler les preuves de mission.",
        badge = "GUIDE",
        url = "$OFFICIAL_DOC_BASE_URL/medias-dji",
    ),
    InfoDocument(
        title = "Support terrain",
        description = "Contacts, escalade et conduite à tenir en cas d'anomalie sur site.",
        badge = "SUP",
        url = "$OFFICIAL_DOC_BASE_URL/support",
    ),
)

@Composable
private fun InfoContent(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    initialPanel: InfoStartPanel = InfoStartPanel.Documents,
    onInitialPanelConsumed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedDocument by remember { mutableStateOf(OfficialInfoDocuments.first()) }
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
                        )
                    } else if (testSelected) {
                        FieldTestGuidePanel(
                            settings = settings,
                            logs = logs,
                            djiSdkState = djiSdkState,
                        )
                    } else {
                        InfoQrPanel(document = selectedDocument)
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
                    modifier = Modifier.weight(0.44f),
                )
                if (adminSelected) {
                    AdminLogsPanel(
                        settings = settings,
                        logs = logs,
                        events = events,
                        djiSdkState = djiSdkState,
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
                    InfoQrPanel(
                        document = selectedDocument,
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
                    text = "Admin logs",
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
                        diagnosticExporter.launch("orange-dronekit-diagnostic-$stamp.txt")
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Exporter diagnostic .txt")
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
                        FolderReadiness.Missing -> "À choisir"
                        FolderReadiness.Unavailable -> "À réautoriser"
                    },
                    detail = settings.folderLabel.ifBlank { "FlightRecord non choisi" },
                    ok = logFolderState == FolderReadiness.Ready,
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "Dernier log",
                    value = latestLog?.flightStartTimeMillis?.let(::formatDate)
                        ?: latestLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatDate)
                        ?: "Aucun",
                    detail = latestLog?.fileName ?: "Lancer une synchronisation si le dossier vient d'être choisi",
                    ok = latestLog != null,
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "E-mail SyncLog",
                    value = if (settings.recipientEmail.isBlank()) "À renseigner" else "Prêt",
                    detail = settings.recipientEmail.ifBlank { "Adresse destinataire libre côté technicien" },
                    ok = settings.recipientEmail.isNotBlank(),
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "FlightExportUSB",
                    value = usbState.shortLabel,
                    detail = usbState.destinationLabel,
                    ok = usbState == UsbDestinationState.Ready,
                )
            }
            item {
                ReadinessSummaryRow(
                    title = "Drone DJI",
                    value = if (djiSdkState.productConnected) "Connecté" else "Non connecté",
                    detail = djiSdkState.lastError ?: djiSdkState.message,
                    ok = djiSdkState.productConnected,
                )
            }
            item {
                FieldTestStep("1", "Ouvrir Réglages", "Vérifier le dossier logs, l'e-mail destinataire et l'état USB.")
                FieldTestStep("2", "Ouvrir SyncLog", "Envoyer un log par e-mail et confirmer sa réception.")
                FieldTestStep("3", "Ouvrir FlightExportUSB", "Brancher la clé, sélectionner le dernier log, puis exporter.")
                FieldTestStep("4", "Si problème", "Exporter le diagnostic depuis Admin logs et prendre une photo de l'écran DJI Pilot 2.")
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
    selectedDocument: InfoDocument,
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
                text = "Documentation officielle",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Sélectionner un document, puis scanner le QR code avec le téléphone connecté au VPN Orange.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.74f),
            )
            OfficialInfoDocuments.forEach { document ->
                    InfoDocumentRow(
                        document = document,
                        selected = !adminSelected && !testSelected && document == selectedDocument,
                        onClick = { onSelectDocument(document) },
                    )
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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(if (selected) Orange.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.07f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(54.dp)
                .height(38.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (selected) Orange else Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = document.badge,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
                maxLines = 1,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
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
            color = if (selected) Orange else Color.White.copy(alpha = 0.52f),
            fontWeight = FontWeight.Black,
        )
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(containerAlpha = 0.56f, borderAlpha = 0.36f, highlightAlpha = 0.12f) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.size(74.dp)) {
                            drawCircle(Color.White.copy(alpha = 0.90f), radius = size.minDimension / 2.2f, style = Stroke(4.dp.toPx()))
                        }
                        Text(
                            text = "i",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 46.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Text(
                        text = "Orange DroneKit",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "Boîte à outils terrain pour radiocommandes DJI Enterprise. L’application centralise la lecture des logs, l’export USB, la préparation de fonds d’écran RC et les accès utiles aux outils drone Orange.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.84f),
                    )
                    Text(
                        text = "Objectif : simplifier la fin de mission, sécuriser les traces de vol et limiter les manipulations techniques pour les télépilotes.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.84f),
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White.copy(alpha = 0.07f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Diagnostic rapide",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                        )
                        InfoLine(
                            "Dossier logs",
                            when (logFolderState) {
                                FolderReadiness.Ready -> "OK"
                                FolderReadiness.Missing -> "À choisir"
                                FolderReadiness.Unavailable -> "À réautoriser"
                            },
                        )
                        InfoLine("Dernier log", latestLog?.fileName ?: "Aucun log détecté")
                        InfoLine("Destinataire mail", settings.recipientEmail.ifBlank { "Non renseigné" })
                        InfoLine("Clé USB", usbState.destinationLabel)
                        InfoLine("SDK DJI", djiSdkState.message)
                        InfoLine("Drone", if (djiSdkState.productConnected) "Connecté" else "Non connecté")
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.White.copy(alpha = 0.07f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        InfoLine("Version", BuildConfig.VERSION_NAME)
                        InfoLine("Build", BuildConfig.VERSION_CODE.toString())
                        InfoLine("Application", BuildConfig.APPLICATION_ID)
                    }
                    OrangeButton(
                        onClick = onOpenFullDiagnostic,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Ouvrir Info et diagnostic complet")
                    }
                    Text(
                        text = "© Loïc Arnold - loic.arnold@orange.com",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.76f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
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
                        text = "Module prevu dans Orange DroneKit.",
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
    onRefreshLogs: () -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
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
        item {
            EquipmentDiagnosticCard(
                settings = settings,
                djiSdkState = djiSdkState,
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
                    Text("Screen", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
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
                    InfoLine("Open API FlightRecord", if (settings.djiApiKey.isBlank()) "Clé OrangePlayLog intégrée" else "Clé personnalisée enregistrée")
                    InfoLine("Mode partage", "USB prioritaire")
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
            InfoLine("SDK DJI", djiSdkState.message)
            InfoLine("Drone", if (djiSdkState.productConnected) "Connecté" else "Non connecté")
            djiSdkState.lastError?.let { InfoLine("Erreur DJI", it) }
            Text(
                text = "Si la clé est branchée mais non autorisée, appuyez sur « Choisir dossier sur clé USB » et sélectionnez la racine de la clé. Si le drone reste non connecté, fermez DJI Pilot 2 puis relancez Orange DroneKit avec la radiocommande connectée au drone.",
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
            title = "Dossier logs",
            detail = when {
                settings.folderUri.isBlank() -> "Choisir le dossier FlightRecord avant le test"
                logFolderState == FolderReadiness.Ready -> settings.folderLabel.ifBlank { "Dossier autorisé" }
                else -> "Dossier non accessible, autorisation à refaire"
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
            title = "Envoi mail",
            detail = if (settings.recipientEmail.isBlank()) {
                "Adresse destinataire à renseigner dans SyncLog"
            } else {
                "Prêt vers ${settings.recipientEmail}"
            },
            state = if (settings.recipientEmail.isBlank()) FieldTestState.Warning else FieldTestState.Ok,
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
            detail = if (djiSdkState.productConnected) {
                djiSdkState.productId?.let { "Connecté · modèle #$it" } ?: "Connecté"
            } else {
                djiSdkState.message
            },
            state = if (djiSdkState.productConnected) FieldTestState.Ok else FieldTestState.Warning,
        ),
        FieldTestItem(
            title = "Médias drone",
            detail = if (settings.mediaFolderUri.isNotBlank()) {
                settings.mediaFolderLabel.ifBlank { "Dossier média local autorisé" }
            } else {
                "Optionnel : les médias directs drone passeront par le SDK"
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
                            SecondaryFieldButton("SyncLog", { onOpenModule(AppModule.SYNC_LOG) }, Modifier.weight(1f))
                            SecondaryFieldButton("FlightExportUSB", { onOpenModule(AppModule.USB_KIT) }, Modifier.weight(1f))
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
                            SecondaryFieldButton("Ouvrir SyncLog", { onOpenModule(AppModule.SYNC_LOG) }, Modifier.weight(1f))
                            SecondaryFieldButton("Ouvrir FlightExportUSB", { onOpenModule(AppModule.USB_KIT) }, Modifier.weight(1f))
                        }
                    }
                }
            }
            Text(
                text = "Parcours conseillé demain : ouvrir l’app, vérifier cette carte, brancher la clé, autoriser la racine de la clé si Android le demande, lancer SyncLog mail puis FlightExportUSB.",
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
            DroneKitThemes.chunked(2).forEach { rowThemes ->
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
    theme: DroneKitTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = if (selected) 0.14f else 0.07f)),
        border = BorderStroke(1.dp, if (selected) Orange.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.22f)),
        shape = MaterialTheme.shapes.small,
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
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestLog = logs.maxByOrNull { it.flightStartTimeMillis ?: it.lastModifiedMillis }
    var selectedLogId by remember { mutableStateOf<Long?>(null) }
    var journalExpanded by remember { mutableStateOf(false) }
    val selectedLog = logs.firstOrNull { it.id == selectedLogId } ?: latestLog
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FlightExportUsbCockpit(
                settings = settings,
                logs = logs,
                selectedLog = selectedLog,
                events = events,
                djiSdkState = djiSdkState,
                progress = usbKitProgress,
                actionMessage = actionMessage,
                journalExpanded = journalExpanded,
                onToggleJournal = { journalExpanded = !journalExpanded },
                onLogSelected = { selectedLogId = it },
                onExportSelected = { onExportLog(selectedLog?.id) },
                onRecoverDroneMedia = { onRecoverDroneMedia(selectedLog?.id) },
                onSyncNow = onSyncNow,
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
    events: List<ActivityEventEntity>,
    djiSdkState: DjiSdkState,
    progress: UsbKitProgress,
    actionMessage: String,
    journalExpanded: Boolean,
    onToggleJournal: () -> Unit,
    onLogSelected: (Long) -> Unit,
    onExportSelected: () -> Unit,
    onRecoverDroneMedia: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) { usbDestinationState(context, settings.usbExportUri) }
    val usbReady = usbState == UsbDestinationState.Ready
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
    ) {
            val compact = maxWidth < 980.dp
            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    UsbKitProgressCard(
                        settings = settings,
                        logs = logs,
                        djiSdkState = djiSdkState,
                        progress = progress,
                        selectedLog = selectedLog,
                        onLogSelected = onLogSelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FlightExportUsbActionsPanel(
                        actionMessage = actionMessage,
                        eventCount = events.size,
                        journalExpanded = journalExpanded,
                        progress = progress,
                        selectedLog = selectedLog,
                        onToggleJournal = onToggleJournal,
                        onRecoverDroneMedia = onRecoverDroneMedia,
                        onExportSelected = onExportSelected,
                        onSyncNow = onSyncNow,
                        onOpenSettings = onOpenSettings,
                        usbReady = usbReady,
                        usbState = usbState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    UsbKitProgressCard(
                        settings = settings,
                        logs = logs,
                        djiSdkState = djiSdkState,
                        progress = progress,
                        selectedLog = selectedLog,
                        onLogSelected = onLogSelected,
                        modifier = Modifier
                            .weight(0.58f)
                            .heightIn(min = 430.dp),
                    )
                    FlightExportUsbActionsPanel(
                        actionMessage = actionMessage,
                        eventCount = events.size,
                        journalExpanded = journalExpanded,
                        progress = progress,
                        selectedLog = selectedLog,
                        onToggleJournal = onToggleJournal,
                        onRecoverDroneMedia = onRecoverDroneMedia,
                        onExportSelected = onExportSelected,
                        onSyncNow = onSyncNow,
                        onOpenSettings = onOpenSettings,
                        usbReady = usbReady,
                        usbState = usbState,
                        modifier = Modifier
                            .weight(0.42f)
                            .heightIn(min = 430.dp),
                    )
                }
            }
    }
}

@Composable
private fun FlightExportMissionPanel(
    settings: AppSettings,
    logs: List<LogFileEntity>,
    selectedLog: LogFileEntity?,
    djiSdkState: DjiSdkState,
    onLogSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val usbProbeTick = rememberUsbProbeTick()
    val usbState = remember(settings.usbExportUri, usbProbeTick) { usbDestinationState(context, settings.usbExportUri) }
        var logMenuExpanded by remember { mutableStateOf(false) }
    val selectableLogs = remember(logs) {
        logs.sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }.take(16)
    }
    GlassCard(modifier = modifier, containerAlpha = 0.42f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Mission",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Dernier vol sélectionné",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
            )
            Text(
                text = selectedLog?.flightStartTimeMillis?.let(::formatShortTime)
                    ?: selectedLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatShortTime)
                    ?: "--:--",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = selectedLog?.flightStartTimeMillis?.let(::formatShortDate)
                    ?: selectedLog?.lastModifiedMillis?.takeIf { it > 0 }?.let(::formatShortDate)
                    ?: "Aucun log",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            MissionInfoPill(
                label = "Log",
                value = selectedLog?.fileName ?: "Aucun log détecté",
                modifier = Modifier.fillMaxWidth(),
            )
            MissionInfoPill(
                label = "Destination",
                value = usbState.destinationLabel,
                modifier = Modifier.fillMaxWidth(),
                accentColor = usbState.color,
            )
            if (selectedLog?.flightDurationSeconds != null) {
                MissionInfoPill(
                    label = "Durée",
                    value = formatDuration(selectedLog.flightDurationSeconds),
                    modifier = Modifier.fillMaxWidth(),
                    accentColor = Color.White.copy(alpha = 0.92f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlightExportStatusMiniChip(
                    label = "Log",
                    value = if (selectedLog != null) "OK" else "Absent",
                    color = if (selectedLog != null) Color(0xFF33D16D) else Color(0xFFFFB020),
                    modifier = Modifier.weight(1f),
                )
                FlightExportStatusMiniChip(
                    label = "USB",
                    value = usbState.shortLabel,
                    color = usbState.color,
                    modifier = Modifier.weight(1f),
                )
                FlightExportStatusMiniChip(
                    label = "Drone",
                    value = if (djiSdkState.productConnected) "OK" else "Non",
                    color = if (djiSdkState.productConnected) Color(0xFF33D16D) else Color(0xFFFF4D4D),
                    modifier = Modifier.weight(1f),
                )
            }

            Box {
                Button(
                    onClick = { logMenuExpanded = true },
                    enabled = selectableLogs.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.13f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.06f),
                        disabledContentColor = Color.White.copy(alpha = 0.42f),
                    ),
                ) {
                    Text(
                        text = "Changer de vol",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                DropdownMenu(
                    expanded = logMenuExpanded,
                    onDismissRequest = { logMenuExpanded = false },
                ) {
                    selectableLogs.forEach { log ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(log.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = log.flightStartTimeMillis?.let(::formatDate)
                                            ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate)
                                            ?: "Date inconnue",
                                        style = MaterialTheme.typography.bodySmall,
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
    eventCount: Int,
    journalExpanded: Boolean,
    progress: UsbKitProgress,
    selectedLog: LogFileEntity?,
    onToggleJournal: () -> Unit,
    onRecoverDroneMedia: () -> Unit,
    onExportSelected: () -> Unit,
    onSyncNow: () -> Unit,
    onOpenSettings: () -> Unit,
    usbReady: Boolean,
    usbState: UsbDestinationState,
    modifier: Modifier = Modifier,
) {
    val hasLog = selectedLog != null
    val busy = progress.stage == UsbKitStage.DETECTING_MEDIA || progress.stage == UsbKitStage.TRANSFERRING
    val primaryText = when {
        !hasLog -> "Scanner les logs"
        usbState == UsbDestinationState.NotConfigured -> "Configurer USB"
        usbState == UsbDestinationState.MountedNotAuthorized -> "Autoriser USB"
        usbState == UsbDestinationState.Unavailable -> "Reconnecter USB"
        progress.stage == UsbKitStage.DETECTING_MEDIA -> "Scan en cours"
        progress.stage == UsbKitStage.TRANSFERRING -> "Transfert ${progress.transferPercent}%"
        progress.stage == UsbKitStage.DONE -> "Exporter à nouveau"
        else -> "Exporter le vol"
    }
    val primaryAction = when {
        !hasLog -> onSyncNow
        !usbReady -> onOpenSettings
        else -> onRecoverDroneMedia
    }
    val guidance = when {
        !hasLog -> "Aucun log sélectionné : scanner le dossier log de vol."
        usbState == UsbDestinationState.NotConfigured -> "Clé USB non configurée : choisir la racine de la clé dans Réglages."
        usbState == UsbDestinationState.MountedNotAuthorized -> "Clé détectée : autoriser son dossier racine pour permettre l’écriture."
        usbState == UsbDestinationState.Unavailable -> "Clé absente ou démontée : rebrancher la clé puis attendre quelques secondes."
        progress.stage == UsbKitStage.TRANSFERRING -> "Transfert en cours : garder la radiocommande et le drone allumés."
        progress.stage == UsbKitStage.DONE -> "Export terminé : vérifier le dossier OrangeDroneKit sur la clé."
        else -> "Prêt : exporter le log et les médias du vol sélectionné."
    }
    GlassCard(modifier = modifier, containerAlpha = 0.42f, borderAlpha = 0.20f, highlightAlpha = 0.06f) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Actions rapides",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = "Dernière action disponible selon l’état du module.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                }
                FlightExportJournalChip(
                    text = if (journalExpanded) "Journal ouvert" else "Journal replié",
                    count = eventCount,
                    onClick = onToggleJournal,
                )
            }

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
                                text = "Log seul",
                                onClick = onExportSelected,
                                modifier = Modifier.weight(1f),
                                enabled = hasLog && usbReady && !busy,
                            )
                            SecondaryFieldButton(
                                text = "Scanner",
                                onClick = onSyncNow,
                                modifier = Modifier.weight(1f),
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
                            text = "Log seul",
                            onClick = onExportSelected,
                            modifier = Modifier.weight(1f),
                            enabled = hasLog && usbReady && !busy,
                        )
                        SecondaryFieldButton(
                            text = "Scanner",
                            onClick = onSyncNow,
                            modifier = Modifier.weight(1f),
                            enabled = !busy,
                        )
                    }
                }
            }
        }
    }
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
        modifier = modifier.height(52.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Orange,
            contentColor = Color.White,
            disabledContainerColor = Orange.copy(alpha = 0.42f),
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
        modifier = modifier.height(52.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.12f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
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
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.12f))
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
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.12f))
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
        logs.sortedByDescending { it.flightStartTimeMillis ?: it.lastModifiedMillis }.take(16)
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
            label = "Log",
            detail = when {
                progress.selectedLogName.isNotBlank() -> progress.selectedLogName
                selectedLog != null -> selectedLog.fileName
                else -> "Aucun log détecté"
            },
            extraDetail = logMeta.ifBlank { "Scanner ou choisir un dossier de logs" },
            status = when {
                logTime != null -> logTime
                progress.selectedLogName.isNotBlank() || selectedLog != null -> "PRÊT"
                else -> "À FAIRE"
            },
            state = if (progress.selectedLogName.isNotBlank() || selectedLog != null) FlightExportStepState.Done else FlightExportStepState.Warning,
            iconModule = AppModule.PLAY_LOG,
            actionLabel = if (selectableLogs.isNotEmpty()) "Changer" else null,
        ),
        FlightExportStep(
            label = "USB",
            detail = usbState.timelineDetail,
            extraDetail = settings.usbExportLabel.ifBlank { usbState.destinationLabel },
            status = usbState.timelineStatus,
            state = if (usbReady) FlightExportStepState.Done else FlightExportStepState.Warning,
            iconModule = AppModule.USB_KIT,
        ),
        FlightExportStep(
            label = "Médias",
            detail = when {
                progress.stage == UsbKitStage.DETECTING_MEDIA -> "Détection en cours..."
                mediaDetected -> "${progress.detectedMediaCount} média(s) détecté(s)"
                progress.stage == UsbKitStage.NO_MEDIA -> "0 média dans le timing"
                djiSdkState.productConnected -> "Drone connecté, prêt à scanner"
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
            iconModule = AppModule.SYNC_LOG,
        ),
        FlightExportStep(
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
                    text = "Progression FlightExportUSB",
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
                selectableLogs = selectableLogs,
                onLogSelected = onLogSelected,
            )
            Text(
                text = progress.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FlightExportTimeline(
    steps: List<FlightExportStep>,
    progressRatio: Float,
    transferring: Boolean,
    selectableLogs: List<LogFileEntity>,
    onLogSelected: (Long) -> Unit,
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
                selectableLogs = if (index == 0) selectableLogs else emptyList(),
                onLogSelected = onLogSelected,
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
    selectableLogs: List<LogFileEntity>,
    onLogSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = step.state.color
    var menuExpanded by remember { mutableStateOf(false) }
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
                    .clickable(enabled = selectableLogs.isNotEmpty()) { menuExpanded = true },
                contentAlignment = Alignment.Center,
            ) {
                if (step.state == FlightExportStepState.Active) {
                    Canvas(modifier = Modifier.size(72.dp)) {
                        drawCircle(color.copy(alpha = 0.16f), radius = size.minDimension / 2f)
                        drawCircle(color.copy(alpha = 0.10f), radius = size.minDimension / 2.8f)
                    }
                }
                Canvas(modifier = Modifier.size(64.dp)) {
                    drawCircle(Color(0xFF111A1C), radius = size.minDimension / 2f)
                    drawCircle(
                        color = color,
                        radius = size.minDimension / 2.2f,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                DroneKitIcon(step.iconModule, Modifier.size(36.dp), thin = true, color = color)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                selectableLogs.forEach { log ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(log.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = log.flightStartTimeMillis?.let(::formatDate)
                                        ?: log.lastModifiedMillis.takeIf { it > 0 }?.let(::formatDate)
                                        ?: "Date inconnue",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        },
                        onClick = {
                            onLogSelected(log.id)
                            menuExpanded = false
                        },
                    )
                }
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
    val label: String,
    val detail: String,
    val extraDetail: String = "",
    val status: String,
    val state: FlightExportStepState,
    val iconModule: AppModule,
    val actionLabel: String? = null,
)

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
            StatusCard(settings, logs, actionMessage, onSyncNow, onExportLatest, onRetryErrors, onCheckUpdate)
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
                StatusCard(settings, logs, actionMessage, onSyncNow, onExportLatest, onRetryErrors, onCheckUpdate)
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
            SetupLine("Dossier log de vol", draft.folderUri.isNotBlank())
            SetupLine("Identifiant radiocommande", draft.radioId.isNotBlank())
            SetupLine("Destination USB", draft.usbExportUri.isNotBlank())
            SetupLine("Premier scan", logs.isNotEmpty())
            Text(
                text = if (draft.isReadyForSync()) {
                    "Prêt pour le mode automatique. Testez la connexion pour fermer cet assistant."
                } else {
                    "Complétez les champs puis testez la connexion. Ensuite l'app pourra travailler seule."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.78f),
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
                text = if (draft.folderLabel.isBlank()) "Aucun dossier choisi" else draft.folderLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            SecondaryFieldButton(text = "Choisir dossier log de vol", onClick = onPickFolder, modifier = Modifier.fillMaxWidth())

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
                onPickMediaFolder = onPickMediaFolder,
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
    onPickMediaFolder: () -> Unit,
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
        text = if (draft.mediaFolderLabel.isBlank()) {
            "Dossier photos/vidéos DJI non configuré"
        } else {
            draft.mediaFolderLabel
        },
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.78f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )

    SecondaryFieldButton(text = "Choisir dossier photos/vidéos DJI", onClick = onPickMediaFolder, modifier = Modifier.fillMaxWidth())

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
private fun FtpFields(
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassTextField(
            value = draft.ftpHost,
            onValueChange = { onDraftChange(draft.copy(ftpHost = it)) },
            label = "Serveur FTP",
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        GlassTextField(
            value = draft.ftpPort,
            onValueChange = { onDraftChange(draft.copy(ftpPort = it.filter { char -> char.isDigit() })) },
            label = "Port",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(96.dp),
            singleLine = true,
        )
    }

    GlassTextField(
        value = draft.ftpRemoteDir,
        onValueChange = { onDraftChange(draft.copy(ftpRemoteDir = it)) },
        label = "Dossier distant FTP",
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    GlassTextField(
        value = draft.ftpUsername,
        onValueChange = { onDraftChange(draft.copy(ftpUsername = it)) },
        label = "Utilisateur FTP",
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    GlassTextField(
        value = draft.ftpPassword,
        onValueChange = { onDraftChange(draft.copy(ftpPassword = it)) },
        label = "Mot de passe FTP",
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleChip(
            label = "FTP",
            selected = !draft.ftpUseFtps,
            onClick = { onDraftChange(draft.copy(ftpUseFtps = false, ftpPort = "21")) },
        )
        ToggleChip(
            label = "FTPS",
            selected = draft.ftpUseFtps,
            onClick = { onDraftChange(draft.copy(ftpUseFtps = true, ftpPort = "21")) },
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ToggleChip(
            label = "Dossier par date",
            selected = draft.ftpUseDateFolders,
            onClick = { onDraftChange(draft.copy(ftpUseDateFolders = !draft.ftpUseDateFolders)) },
        )
        ToggleChip(
            label = "Ne pas ecraser",
            selected = draft.skipExistingRemoteFiles,
            onClick = { onDraftChange(draft.copy(skipExistingRemoteFiles = !draft.skipExistingRemoteFiles)) },
        )
    }
}

@Composable
private fun SmtpFields(
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
) {
    GlassTextField(
        value = draft.recipientEmail,
        onValueChange = { onDraftChange(draft.copy(recipientEmail = it)) },
        label = "Adresse mail destinataire",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassTextField(
            value = draft.smtpHost,
            onValueChange = { onDraftChange(draft.copy(smtpHost = it)) },
            label = "Serveur SMTP",
            modifier = Modifier.weight(1f),
            singleLine = true,
        )
        GlassTextField(
            value = draft.smtpPort,
            onValueChange = { onDraftChange(draft.copy(smtpPort = it.filter { char -> char.isDigit() })) },
            label = "Port",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(96.dp),
            singleLine = true,
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SecurityChip("STARTTLS", SmtpSecurity.STARTTLS, draft, onDraftChange)
        SecurityChip("SSL/TLS", SmtpSecurity.SSL_TLS, draft, onDraftChange)
        SecurityChip("Aucun", SmtpSecurity.NONE, draft, onDraftChange)
    }

    GlassTextField(
        value = draft.senderEmail,
        onValueChange = { onDraftChange(draft.copy(senderEmail = it)) },
        label = "Adresse expediteur",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    GlassTextField(
        value = draft.smtpPassword,
        onValueChange = { onDraftChange(draft.copy(smtpPassword = it)) },
        label = "Mot de passe ou app password",
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun DeliveryModeChip(
    label: String,
    value: DeliveryMode,
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
) {
    ToggleChip(
        label = label,
        selected = draft.deliveryMode == value,
        onClick = { onDraftChange(draft.copy(deliveryMode = value)) },
    )
}

@Composable
private fun SecurityChip(
    label: String,
    value: SmtpSecurity,
    draft: AppSettings,
    onDraftChange: (AppSettings) -> Unit,
) {
    FilterChip(
        selected = draft.smtpSecurity == value,
        onClick = { onDraftChange(draft.copy(smtpSecurity = value)) },
        label = { Text(label) },
        colors = chipColors(draft.smtpSecurity == value),
        border = chipBorder(draft.smtpSecurity == value),
    )
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
        containerColor = Color.Black.copy(alpha = 0.34f),
        labelColor = Color.White,
        selectedContainerColor = Color.White.copy(alpha = 0.12f),
        selectedLabelColor = Color.White,
    )

@Composable
private fun chipBorder(selected: Boolean) =
    FilterChipDefaults.filterChipBorder(
        enabled = true,
        selected = selected,
        borderColor = Color.White.copy(alpha = 0.36f),
        selectedBorderColor = Orange.copy(alpha = 0.95f),
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
    var previewOnly by remember { mutableStateOf(false) }
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
            showQrCode = true
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
                "Fond d'ecran JPG exporte."
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
            showQrCode = true,
            overlayText = "",
            textColor = textColor,
            textScale = textScale,
        )
    val startExport = {
        val name = "orange-dronekit-${selectedResolution.fileSuffix}-${System.currentTimeMillis()}.jpg"
        val request = currentRequest()
        if (settings.screenExportUri.isNotBlank()) {
            exportMessage = if (exportScreenJpgToFolder(context, Uri.parse(settings.screenExportUri), name, request)) {
                onAddScreenExportHistory("${formatDate(System.currentTimeMillis())} - $name")
                "Fond d'ecran exporte dans ${settings.screenExportLabel.ifBlank { "le dossier choisi" }}."
            } else {
                "Export direct impossible. Choisissez un dossier valide."
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
                InfoLine("Export", settings.screenExportLabel.ifBlank { "Sélecteur Android" })
                OrangeButton(onClick = startExport, modifier = Modifier.fillMaxWidth()) {
                    Text("Exporter en .jpg")
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
            onClick: () -> Unit,
            modifier: Modifier = Modifier,
        ) {
            OrangeButton(
                onClick = onClick,
                modifier = modifier.height(if (vertical) 36.dp else 50.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Black,
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
                ActionButton("RC2", { selectedResolution = ScreenResolution.Rc2 }, Modifier.fillMaxWidth())
                ActionButton("RCPLUS2", { selectedResolution = ScreenResolution.RcPlus2 }, Modifier.fillMaxWidth())
                ActionButton("EXPORT", startExport, Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionButton("RC2", { selectedResolution = ScreenResolution.Rc2 }, Modifier.weight(1f))
                ActionButton("RCPLUS2", { selectedResolution = ScreenResolution.RcPlus2 }, Modifier.weight(1f))
                ActionButton("EXPORT", startExport, Modifier.weight(1f))
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
                            showQrCode = true,
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
                            showQrCode = true,
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
                if (!previewOnly) {
                    if (!compactActions) {
                        item { FormatPhotoCard(showResolution = true, compact = narrowPanel) }
                        item { QrCard(compact = narrowPanel) }
                    }
                    item { HistoryCard() }
                }
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
                        qrLayer = qrLayer,
                        onQrLayerChange = { qrLayer = it },
                        maxPreviewHeight = previewMaxHeight,
                    )
                    Text(
                        text = "Aperçu réduit : déplacez le QR code avec un doigt, pincez pour ajuster sa taille.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.66f),
                        modifier = Modifier.padding(horizontal = 4.dp),
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
                    ScreenWallpaperEditor(
                        resolution = selectedResolution,
                        background = selectedBackground,
                        customBitmap = customBitmap,
                        qrLayer = qrLayer,
                        onQrLayerChange = { qrLayer = it },
                        maxPreviewHeight = previewMaxHeight,
                    )
                }
                item { ExportCard() }
                if (!previewOnly) {
                    item { FormatPhotoCard() }
                    item { QrCard() }
                    item { HistoryCard() }
                }
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
    qrLayer: ScreenLayerTransform,
    onQrLayerChange: (ScreenLayerTransform) -> Unit,
    maxPreviewHeight: Dp = 360.dp,
) {
    GlassCard(containerAlpha = 0.42f) {
        Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .heightIn(max = maxPreviewHeight)
                .aspectRatio(resolution.width.toFloat() / resolution.height.toFloat())
                .clip(MaterialTheme.shapes.small)
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
            Text(
                text = "${resolution.label} - ${resolution.width} x ${resolution.height}",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.52f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
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
        BitmapFactory.decodeResource(context.resources, R.drawable.screen_qr_code)?.let {
            drawExportBitmap(canvas, it, output.width, output.height, request.qrLayer, 0.19f)
        }
        context.contentResolver.openOutputStream(outputUri)?.use { stream ->
            output.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        } ?: return false
        true
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
        exportScreenJpg(context, target.uri, request)
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
        drawFlightReportDetailsPage(detailsPage.canvas, log)
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
    drawPdfRoutePanel(canvas, log, RectF(34f, 214f, 561f, 438f))
    drawPdfObservationPanel(canvas, log, RectF(34f, 472f, 561f, 788f))
    drawPdfFooter(canvas)
}

private fun drawFlightReportTelemetryPage(context: android.content.Context, canvas: android.graphics.Canvas, log: LogFileEntity) {
    drawPdfBackground(canvas)
    drawPdfHeader(context, canvas, "Télémétrie détaillée", log.fileName, 2)
    drawPdfTelemetrySection(canvas, log)
    drawPdfFooter(canvas)
}

private fun drawFlightReportDetailsPage(canvas: android.graphics.Canvas, log: LogFileEntity) {
    drawPdfBackground(canvas)
    canvas.drawText("Page 3", 510f, 48f, pdfBodyPaint(Pdf.MUTED, 11f, Typeface.BOLD))
    drawPdfSectionTitle(canvas, "Détails techniques", 62f)

    var y = 96f
    val rows = listOf(
        "Fichier" to log.fileName,
        "Statut analyse" to displayDecodeStatus(log.decodeStatus),
        "Message analyse" to (log.decodeError ?: "-"),
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
    rows.forEach { (label, value) ->
        y = drawPdfKeyValue(canvas, label, value, y)
    }

    drawPdfSectionTitle(canvas, "Exploitation recommandée", y + 18f)
    y += 48f
    listOf(
        "Conserver ce PDF avec le FlightRecord DJI original.",
        "Utiliser FlightExportUSB pour exporter le log et les médias du même créneau horaire.",
        "Réanalyser le log après mise à jour de l'application pour enrichir les courbes.",
    ).forEach {
        y = drawPdfWrappedText(canvas, "• $it", 54f, y, 500f, pdfBodyPaint(Pdf.MUTED))
        y += 8f
    }
    drawPdfFooter(canvas)
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
    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.orange_dronekit_wordmark)
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
            rect = RectF(34f + col * 179f, 152f + row * 62f, 34f + col * 179f + cardWidth, 152f + row * 62f + cardHeight),
        )
    }
}

private fun drawPdfMetricCard(canvas: android.graphics.Canvas, label: String, value: String, rect: RectF) {
    drawPdfPanel(canvas, rect, Pdf.PANEL_LIGHT)
    canvas.drawText(label, rect.left + 12f, rect.top + 19f, pdfBodyPaint(Pdf.MUTED, 10f, Typeface.BOLD))
    canvas.drawText(value.take(28), rect.left + 12f, rect.top + 40f, pdfBodyPaint(Pdf.TEXT, 14f, Typeface.BOLD))
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
    canvas.drawText("Synthèse automatique", rect.left + 16f, rect.top + 28f, pdfBodyPaint(Pdf.TEXT, 17f, Typeface.BOLD))
    canvas.drawText(
        "Points clés calculés depuis le FlightRecord DJI décodé.",
        rect.left + 16f,
        rect.top + 48f,
        pdfBodyPaint(Pdf.MUTED, 10.5f),
    )

    val chips = buildPdfQualityChips(log)
    chips.forEachIndexed { index, chip ->
        val col = index % 2
        val row = index / 2
        drawPdfQualityChip(
            canvas = canvas,
            label = chip.label,
            value = chip.value,
            color = chip.color,
            rect = RectF(
                rect.left + 16f + col * 252f,
                rect.top + 72f + row * 56f,
                rect.left + 244f + col * 252f,
                rect.top + 118f + row * 56f,
            ),
        )
    }

    var y = rect.top + 214f
    buildPdfObservations(log).forEach {
        y = drawPdfWrappedText(canvas, "• $it", rect.left + 24f, y, rect.width() - 48f, pdfBodyPaint(Pdf.TEXT, 11.2f))
        y += 10f
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
        FlightChartKind.BATTERY -> telemetry
            .map { it.batteryPercent.toDouble() }
            .takeIf(::isUsableBatterySeries)
            ?: emptyList()
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
    canvas.drawText("Orange DroneKit", 34f, 818f, pdfBodyPaint(Pdf.MUTED, 10f, Typeface.BOLD))
    canvas.drawText("Loïc Arnold - loic.arnold@orange.com", 312f, 818f, pdfBodyPaint(Pdf.MUTED, 10f))
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
    events: List<ActivityEventEntity>,
    actionMessage: String,
    onDecodeLog: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val decodedLogs = logs.filter { it.decodedAtMillis != null }
    val lastDecoded = decodedLogs.firstOrNull()
    var selectedLogId by remember { mutableStateOf<Long?>(null) }
    var query by remember { mutableStateOf("") }
    var pdfLog by remember { mutableStateOf<LogFileEntity?>(null) }
    val selectedLog = selectedLogId?.let { id -> logs.firstOrNull { it.id == id } }
    val filteredLogs = remember(logs, query) {
        logs.filter {
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

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (selectedLog != null) {
            item {
                FlightLogDetailCard(
                    log = selectedLog,
                    onBack = { selectedLogId = null },
                    onDecodeLog = onDecodeLog,
                    onExportPdf = {
                        pdfLog = selectedLog
                        pdfExporter.launch("${selectedLog.fileName.substringBeforeLast('.')}_resume.pdf")
                    },
                )
            }
        } else {
            item {
                GlassCard(containerAlpha = 0.58f) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Lecteur de logs de vol",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "PlayLog décode les métadonnées DJI locales sans modifier les fichiers originaux. Sélectionnez un log pour ouvrir une fiche dédiée et plus lisible.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.82f),
                        )
                        InfoLine("Logs disponibles", logs.size.toString())
                        InfoLine("Logs analysés", decodedLogs.size.toString())
                        InfoLine("Dernier log", lastDecoded?.fileName ?: logs.firstOrNull()?.fileName ?: "Aucun log détecté")
                        InfoLine("Dernier modèle", lastDecoded?.aircraftName ?: lastDecoded?.productType ?: "-")
                        InfoLine("Dernier départ", lastDecoded?.flightStartTimeMillis?.let(::formatDate) ?: "-")
                        if (actionMessage.isNotBlank()) {
                            Text(
                                actionMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.82f),
                            )
                        }
                        InfoLine("Open API FlightRecord", "Clé OrangePlayLog intégrée")
                        GlassTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = "Rechercher un log par nom ou date",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }
            }
            item {
                Text("Logs de vol", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            if (filteredLogs.isEmpty()) {
                item {
                    GlassCard(containerAlpha = 0.30f) {
                        Text(
                            text = if (logs.isEmpty()) "Aucun log détecté pour le moment. Lancez une synchronisation depuis SyncLog." else "Aucun log ne correspond à la recherche.",
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
                        onExportPdf = {
                            pdfLog = log
                            pdfExporter.launch("${log.fileName.substringBeforeLast('.')}_resume.pdf")
                        },
                    )
                }
            }
            item {
                Text("Activité récente", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            items(events.take(12), key = { it.id }) { event ->
                ActivityRow(event)
            }
        }
        item {
            CopyrightFooter()
        }
    }
}

@Composable
private fun FlightLogPreviewRow(
    log: LogFileEntity,
    onOpenLog: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onExportPdf: () -> Unit,
) {
    GlassCard(containerAlpha = 0.30f) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = log.fileName,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OrangeButton(
                    onClick = onOpenLog,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Ouvrir le log")
                }
                OrangeButton(
                    onClick = { onDecodeLog(log.id) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (log.decodedAtMillis == null) "Analyser ce log" else "Reanalyser ce log")
                }
                OrangeButton(
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("PDF")
                }
            }
            InfoLine("Statut", displayLogStatus(log.status))
            InfoLine("Analyse", displayDecodeStatus(log.decodeStatus))
            InfoLine("Résumé", "${log.flightDurationSeconds?.let(::formatDuration) ?: "-"} | ${log.totalDistanceMeters?.let(::formatMeters) ?: "-"} | ${log.maxHeightMeters?.let(::formatMeters) ?: "-"}")
        }
    }
}

@Composable
private fun FlightLogDetailCard(
    log: LogFileEntity,
    onBack: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onExportPdf: () -> Unit,
) {
    var timeline by remember(log.id) { mutableStateOf(0f) }
    var isPlaying by remember(log.id) { mutableStateOf(false) }
    var playbackSpeed by remember(log.id) { mutableStateOf(1f) }
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
                onExportPdf = onExportPdf,
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val landscape = maxWidth > 900.dp
                if (landscape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FlightTelemetryPanel(
                            log = log,
                            timeline = timeline,
                            modifier = Modifier.weight(0.92f),
                        )
                        FlightMapPanel(
                            log = log,
                            timeline = timeline,
                            isPlaying = isPlaying,
                            playbackSpeed = playbackSpeed,
                            duration = log.flightDurationSeconds,
                            onTimelineChange = { timeline = it },
                            onPlayPause = playPause,
                            onPlaybackSpeedChange = { playbackSpeed = it },
                            modifier = Modifier.weight(1.08f),
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlightMapPanel(
                            log = log,
                            timeline = timeline,
                            isPlaying = isPlaying,
                            playbackSpeed = playbackSpeed,
                            duration = log.flightDurationSeconds,
                            onTimelineChange = { timeline = it },
                            onPlayPause = playPause,
                            onPlaybackSpeedChange = { playbackSpeed = it },
                        )
                        FlightTelemetryPanel(log = log, timeline = timeline)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightViewerTopBar(
    log: LogFileEntity,
    onBack: () -> Unit,
    onDecodeLog: (Long) -> Unit,
    onExportPdf: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrangeButton(onClick = onBack) {
            Text("Retour")
        }
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
        FlightMetricPill("Départ", log.flightStartTimeMillis?.let(::formatDate) ?: "-")
        FlightMetricPill("Durée", log.flightDurationSeconds?.let(::formatDuration) ?: "-")
        FlightMetricPill("Distance", log.totalDistanceMeters?.let(::formatMeters) ?: "-")
        FlightMetricPill("Batterie", log.batterySerial ?: "-")
        OrangeButton(onClick = { onDecodeLog(log.id) }) {
            Text(if (log.decodedAtMillis == null) "Analyser" else "Réanalyser")
        }
        OrangeButton(onClick = onExportPdf) {
            Text("Export PDF")
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
        FlightSpecsPanel(log)
    }
}

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
            FlightChartKind.BATTERY -> telemetry
                .map { it.batteryPercent.toDouble() }
                .takeIf(::isUsableBatterySeries)
                ?: emptyList()
        }.filter { it.isFinite() }
        if (realValues.any { it > 0.0 }) return sampleChartValues(realValues)
    }

    if (kind == FlightChartKind.BATTERY) return emptyList()

    val count = 48
    val maxDistanceFromStart = distanceFromStartMeters(log)
    val maxSpeed = log.maxHorizontalSpeedMetersPerSecond ?: 13.8
    val maxAltitude = log.maxHeightMeters ?: 35.0
    return List(count) { index ->
        val t = index / (count - 1.0)
        val wobble = (((index * 7) % 9) - 4) / 28.0
        when (kind) {
            FlightChartKind.DISTANCE -> maxDistanceFromStart * distanceToHomeProfile(t, wobble)
            FlightChartKind.SPEED -> (maxSpeed * (0.25 + sin(t * PI * 3.1).coerceAtLeast(0.0) * 0.62 + wobble)).coerceAtLeast(0.0)
            FlightChartKind.SATELLITES -> satelliteCountProfile(t, index).toDouble()
            FlightChartKind.ALTITUDE -> (maxAltitude * (0.18 + sin(t * PI).coerceAtLeast(0.0) * 0.70 + wobble * 0.45)).coerceAtLeast(0.0)
            FlightChartKind.BATTERY -> (65.0 - 28.0 * t + wobble * 2.2).coerceIn(0.0, 100.0)
        }
    }
}

private fun currentTelemetryLabel(kind: FlightChartKind, values: List<Double>, timeline: Float): String {
    val index = (timeline.coerceIn(0f, 1f) * values.lastIndex).toInt().coerceIn(values.indices)
    val value = values[index]
    return "${kind.axisLabel} : ${formatAxisValue(value, kind)}"
}

private fun isUsableBatterySeries(values: List<Double>): Boolean {
    if (values.size < 3) return false
    val clean = values.filter { it in 1.0..100.0 }
    if (clean.size < values.size * 0.80) return false
    val distinct = clean.map { it.toInt() }.distinct().size
    if (distinct < 2) return false
    val jumpCount = clean.zipWithNext().count { (a, b) -> kotlin.math.abs(a - b) > 8.0 }
    return jumpCount <= clean.size / 8
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
                loadDataWithBaseURL("https://orange-dronekit.local/", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            onWebViewReady(webView)
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL("https://orange-dronekit.local/", html, "text/html", "UTF-8", null)
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
  <div id="map"><div class="loading">Chargement satellite...</div><img id="satellite" alt="Satellite Esri" /><svg id="route"></svg><div class="label">Esri World Imagery</div></div>
  <script>
    console.log('Orange DroneKit satellite map boot');
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
    function marker(x, y, color, label) {
      return '<circle cx="' + x + '" cy="' + y + '" r="10" fill="' + color + '" stroke="white" stroke-width="4"></circle>' +
        '<text x="' + x + '" y="' + (y + 28) + '" text-anchor="middle" fill="white" font-size="12" font-weight="700">' + label + '</text>';
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
      const s = toScreen(mercator(start.lat, start.lon));
      const e = toScreen(mercator(end.lat, end.lon));
      routeEl.setAttribute('viewBox', '0 0 ' + width + ' ' + height);
      routeEl.innerHTML = hasLine
        ? '<line x1="' + s.x + '" y1="' + s.y + '" x2="' + e.x + '" y2="' + e.y + '" stroke="rgba(0,0,0,.7)" stroke-width="14" stroke-linecap="round"/>' +
          '<line x1="' + s.x + '" y1="' + s.y + '" x2="' + e.x + '" y2="' + e.y + '" stroke="#ff7900" stroke-width="9" stroke-linecap="round"/>' +
          marker(s.x, s.y, '#25d366', 'START') + marker(e.x, e.y, '#ff3b30', 'END')
        : marker(s.x, s.y, '#20bdf2', 'GPS');
    }
    window.orangeDroneKitZoomIn = function() { zoom = Math.min(20, zoom + 1); render(); };
    window.orangeDroneKitZoomOut = function() { zoom = Math.max(3, zoom - 1); render(); };
    window.orangeDroneKitReset = function() {
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
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White.copy(alpha = 0.08f))
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
            .clip(MaterialTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.10f))
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
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.White.copy(alpha = 0.08f))
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
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(0xFF0F766E))
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
                    .clip(MaterialTheme.shapes.small)
                    .background(if (playbackSpeed > 1f) Orange.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f))
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
        text = "© Loïc Arnold - loic.arnold@orange.com | Orange DroneKit v${BuildConfig.VERSION_NAME}",
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

private fun sendLogByMail(
    context: android.content.Context,
    radioId: String,
    recipientEmail: String,
    technicianEmail: String,
    log: LogFileEntity,
) {
    if (recipientEmail.isBlank()) {
        Toast.makeText(context, "Adresse destinataire manquante.", Toast.LENGTH_LONG).show()
        return
    }
    val logUri = Uri.parse(log.filePath)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
        putExtra(Intent.EXTRA_SUBJECT, EmbeddedMailConfig.subject(radioId, log))
        putExtra(Intent.EXTRA_TEXT, EmbeddedMailConfig.body(radioId, technicianEmail, log))
        putExtra(Intent.EXTRA_STREAM, logUri)
        clipData = ClipData.newUri(context.contentResolver, log.fileName, logUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Envoyer le log DJI par mail")
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(chooser)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "Aucune application mail disponible.", Toast.LENGTH_LONG).show()
    }
}

@Composable
private fun SyncflowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Orange,
            onPrimary = Color.White,
            surface = Color.White,
            background = Color(0xFF050B0E),
        ),
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
    containerAlpha: Float = 0.52f,
    borderAlpha: Float = 0.34f,
    highlightAlpha: Float = 0.10f,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = containerAlpha)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha)),
        shape = MaterialTheme.shapes.small,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = highlightAlpha),
                            Color.Black.copy(alpha = 0.22f),
                        ),
                    ),
                ),
        ) {
            content()
        }
    }
}

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
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.42f),
        ),
        border = BorderStroke(1.dp, Orange.copy(alpha = 0.64f)),
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
            color = Color.White.copy(alpha = 0.76f),
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
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Orange,
                focusedBorderColor = Orange,
                unfocusedBorderColor = Color.White.copy(alpha = 0.30f),
                focusedContainerColor = Color.Black.copy(alpha = 0.38f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.30f),
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

private val Orange = Color(0xFFFF7900)

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
        appendLine("Orange DroneKit - diagnostic terrain")
        appendLine("Généré le : ${formatDate(System.currentTimeMillis())}")
        appendLine("Version : ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        appendLine("Package : ${context.packageName}")
        appendLine()
        appendLine("Configuration")
        appendLine("- Radiocommande : ${settings.radioId.ifBlank { "-" }}")
        appendLine("- Dossier logs : ${settings.folderLabel.ifBlank { "-" }}")
        appendLine("- État dossier logs : $logFolderState")
        appendLine("- Mode partage : ${settings.deliveryMode}")
        appendLine("- Destinataire mail : ${settings.recipientEmail.ifBlank { "-" }}")
        appendLine("- Technicien : ${settings.technicianEmail.ifBlank { "-" }}")
        appendLine("- Dossier USB : ${settings.usbExportLabel.ifBlank { "-" }}")
        appendLine("- Médias locaux : ${settings.mediaFolderLabel.ifBlank { "-" }}")
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
        appendLine("- Drone connecté : ${djiSdkState.productConnected}")
        appendLine("- Produit : ${djiSdkState.productId ?: "-"}")
        appendLine("- Message : ${djiSdkState.message}")
        appendLine("- Dernière erreur : ${djiSdkState.lastError ?: "-"}")
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
    }
}

private fun logFolderReadiness(context: Context, folderUri: String): FolderReadiness =
    runCatching {
        if (folderUri.isBlank()) return@runCatching FolderReadiness.Missing
        val uri = Uri.parse(folderUri)
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
        UsbDestinationState.NotConfigured -> "Clé non branchée"
        UsbDestinationState.MountedNotAuthorized -> "Clé branchée, dossier à autoriser"
        UsbDestinationState.Unavailable -> "Clé absente ou démontée"
        UsbDestinationState.Ready -> "USB prête"
    }

private val UsbDestinationState.shortLabel: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "Absente"
        UsbDestinationState.MountedNotAuthorized -> "À autoriser"
        UsbDestinationState.Unavailable -> "Non accessible"
        UsbDestinationState.Ready -> "Prête"
    }

private val UsbDestinationState.timelineStatus: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "ABSENTE"
        UsbDestinationState.MountedNotAuthorized -> "À AUTORISER"
        UsbDestinationState.Unavailable -> "ABSENTE"
        UsbDestinationState.Ready -> "PRÊTE"
    }

private val UsbDestinationState.timelineDetail: String
    get() = when (this) {
        UsbDestinationState.NotConfigured -> "Brancher une clé USB"
        UsbDestinationState.MountedNotAuthorized -> "Choisir le dossier de la clé"
        UsbDestinationState.Unavailable -> "Clé absente ou démontée"
        UsbDestinationState.Ready -> "Clé accessible"
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
