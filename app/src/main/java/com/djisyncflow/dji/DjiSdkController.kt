package com.djisyncflow.dji

import android.content.Context
import android.os.Build
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.KeyManager
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import dji.v5.network.DJINetworkManager
import dji.sdk.keyvalue.key.DJIKey
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.key.ProductKey
import dji.sdk.keyvalue.key.RemoteControllerKey
import dji.sdk.keyvalue.value.product.ProductType
import dji.sdk.keyvalue.value.remotecontroller.RemoteControllerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

data class DjiSdkState(
    val available: Boolean = false,
    val initialized: Boolean = false,
    val registered: Boolean = false,
    val productConnected: Boolean = false,
    val productId: Int? = null,
    val sdkVersion: String? = null,
    val productType: String? = null,
    val aircraftFirmwareVersion: String? = null,
    val remoteControllerConnected: Boolean = false,
    val remoteControllerType: String? = null,
    val remoteControllerFirmwareVersion: String? = null,
    val firmwareWarning: String? = null,
    val message: String = "SDK DJI non démarré",
    val lastError: String? = null,
)

object DjiSdkController {
    private val mutableState = MutableStateFlow(DjiSdkState())
    val state: StateFlow<DjiSdkState> = mutableState.asStateFlow()

    @Volatile
    private var started = false

    @Volatile
    private var appContext: Context? = null

    private val keyManager = KeyManager.getInstance()
    private val keyListenerOwner = Any()
    private val keyListenersBound = AtomicBoolean(false)
    private val productTypeKey = KeyTools.createKey(ProductKey.KeyProductType)
    private val aircraftConnectionKey = KeyTools.createKey(FlightControllerKey.KeyConnection)
    private val aircraftFirmwareVersionKey = KeyTools.createKey(ProductKey.KeyFirmwareVersion)
    private val remoteControllerConnectionKey = KeyTools.createKey(RemoteControllerKey.KeyConnection)
    private val remoteControllerTypeKey = KeyTools.createKey(RemoteControllerKey.KeyRemoteControllerType)
    private val remoteControllerFirmwareVersionKey = KeyTools.createKey(RemoteControllerKey.KeyFirmwareVersion)

    @Synchronized
    fun start(context: Context) {
        appContext = context.applicationContext
        if (started) {
            if (mutableState.value.initialized && !SDKManager.getInstance().isRegistered) {
                registerAppSafely("Nouvelle tentative d'enregistrement DJI...")
            }
            return
        }
        started = true
        if (isAndroidEmulator()) {
            mutableState.value = DjiSdkState(
                available = false,
                message = "SDK DJI indisponible sur émulateur Android",
                lastError = "Émulateur détecté : test SDK à réaliser sur radiocommande DJI",
            )
            return
        }
        runCatching {
            val contextForSdk = appContext ?: context.applicationContext
            mutableState.value = DjiSdkState(
                available = true,
                sdkVersion = sdkVersionOrNull(),
                message = "Initialisation SDK DJI...",
            )
            SDKManager.getInstance().init(contextForSdk, object : SDKManagerCallback {
                override fun onRegisterSuccess() {
                    mutableState.value = mutableState.value.copy(
                        available = true,
                        registered = true,
                        lastError = null,
                        message = if (mutableState.value.productConnected) {
                            "SDK DJI enregistré, drone connecté"
                        } else {
                            "SDK DJI enregistré, en attente du drone"
                        },
                    )
                }

                override fun onRegisterFailure(error: IDJIError) {
                    mutableState.value = mutableState.value.copy(
                        available = true,
                        registered = false,
                        productConnected = false,
                        productId = null,
                        lastError = error.description(),
                        message = "Enregistrement DJI refusé : ${error.description()}",
                    )
                }

                override fun onProductDisconnect(productId: Int) {
                    mutableState.value = mutableState.value.copy(
                        productConnected = false,
                        productId = null,
                        message = "Drone DJI déconnecté",
                    )
                }

                override fun onProductConnect(productId: Int) {
                    val validProduct = isValidProductId(productId)
                    mutableState.value = mutableState.value.copy(
                        productConnected = validProduct,
                        productId = productId.takeIf { validProduct },
                        lastError = null,
                        message = if (validProduct) {
                            mutableState.value.productType?.let { "Drone $it connecté" } ?: "Drone DJI connecté"
                        } else {
                            "SDK DJI enregistré, en attente du drone"
                        },
                    )
                }

                override fun onProductChanged(productId: Int) {
                    val validProduct = isValidProductId(productId)
                    mutableState.value = mutableState.value.copy(
                        productConnected = validProduct,
                        productId = productId.takeIf { validProduct },
                        message = if (validProduct) {
                            mutableState.value.productType?.let { "Drone $it connecté" } ?: "Drone DJI connecté"
                        } else {
                            "SDK DJI prêt, drone non connecté"
                        },
                    )
                }

                override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                    val initialized = event == DJISDKInitEvent.INITIALIZE_COMPLETE
                    mutableState.value = mutableState.value.copy(
                        available = true,
                        initialized = initialized,
                        sdkVersion = sdkVersionOrNull(),
                        message = if (initialized) "SDK DJI initialisé" else "Initialisation DJI $totalProcess%",
                    )
                    if (initialized) {
                        bindConnectionKeys()
                        registerAppSafely("SDK DJI initialisé, enregistrement...")
                    }
                }

                override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                    mutableState.value = mutableState.value.copy(
                        message = if (total > 0) {
                            "Base DJI ${((current * 100) / total).coerceIn(0, 100)}%"
                        } else {
                            "Base DJI en téléchargement"
                        },
                    )
                }
            })
            DJINetworkManager.getInstance().addNetworkStatusListener { isAvailable ->
                if (isAvailable && mutableState.value.initialized && !SDKManager.getInstance().isRegistered) {
                    registerAppSafely("Réseau DJI disponible, nouvel enregistrement...")
                }
            }
        }.onFailure { error ->
            started = false
            val detail = when (error.javaClass.simpleName) {
                "NoClassDefFoundError", "ClassNotFoundException" -> "SDK DJI absent en version de test"
                else -> "SDK DJI indisponible"
            }
            mutableState.value = DjiSdkState(
                available = false,
                lastError = error.message ?: error.javaClass.simpleName,
                message = detail,
            )
        }
    }

    fun retryRegistration(context: Context? = null) {
        context?.applicationContext?.let { appContext = it }
        if (!started) {
            val ctx = appContext ?: context ?: return
            start(ctx)
            return
        }
        if (isAndroidEmulator()) {
            mutableState.value = DjiSdkState(
                available = false,
                message = "SDK DJI indisponible sur émulateur Android",
                lastError = "Émulateur détecté : test SDK à réaliser sur radiocommande DJI",
            )
            return
        }
        if (mutableState.value.initialized) {
            registerAppSafely("Relance manuelle de l'enregistrement DJI...")
        } else {
            started = false
            appContext?.let { start(it) }
        }
    }

    private fun registerAppSafely(message: String) {
        runCatching {
            mutableState.value = mutableState.value.copy(
                available = true,
                message = message,
                lastError = null,
            )
            SDKManager.getInstance().registerApp()
        }.onFailure { error ->
            mutableState.value = mutableState.value.copy(
                available = true,
                registered = false,
                productConnected = false,
                productId = null,
                lastError = error.message ?: error.javaClass.simpleName,
                message = "Relance DJI impossible",
            )
        }
    }

    private fun isValidProductId(productId: Int): Boolean =
        productId > 0

    private fun bindConnectionKeys() {
        if (!keyListenersBound.compareAndSet(false, true)) return

        listen(productTypeKey) { productType ->
            mutableState.value = withFirmwareWarning(mutableState.value.copy(
                productType = productTypeLabel(productType),
                lastError = null,
                message = when {
                    mutableState.value.productConnected -> "Drone ${productTypeLabel(productType)} connecté"
                    mutableState.value.registered -> "SDK DJI enregistré, en attente du drone"
                    else -> mutableState.value.message
                },
            ))
        }
        listen(aircraftConnectionKey) { connected ->
            mutableState.value = mutableState.value.copy(
                productConnected = connected,
                productId = if (connected) mutableState.value.productId else null,
                lastError = if (connected) null else mutableState.value.lastError,
                message = if (connected) {
                    mutableState.value.productType?.let { "Drone $it connecté" } ?: "Drone DJI connecté"
                } else {
                    "Radiocommande détectée, drone non connecté"
                },
            )
        }
        listen(aircraftFirmwareVersionKey) { version ->
            mutableState.value = withFirmwareWarning(
                mutableState.value.copy(aircraftFirmwareVersion = version),
            )
        }
        listen(remoteControllerConnectionKey) { connected ->
            mutableState.value = mutableState.value.copy(
                remoteControllerConnected = connected,
                message = when {
                    mutableState.value.productConnected -> mutableState.value.message
                    connected -> "Radiocommande DJI connectée, en attente du drone"
                    else -> "En attente de la radiocommande DJI"
                },
            )
        }
        listen(remoteControllerTypeKey) { remoteControllerType ->
            mutableState.value = withFirmwareWarning(
                mutableState.value.copy(
                    remoteControllerType = remoteControllerTypeLabel(remoteControllerType),
                ),
            )
        }
        listen(remoteControllerFirmwareVersionKey) { version ->
            mutableState.value = withFirmwareWarning(
                mutableState.value.copy(remoteControllerFirmwareVersion = version),
            )
        }
    }

    private fun <T> listen(key: DJIKey<T>, onValue: (T) -> Unit) {
        runCatching {
            keyManager.listen(key, keyListenerOwner, true) { _, newValue ->
                if (newValue != null) onValue(newValue)
            }
        }.onFailure { error ->
            mutableState.value = mutableState.value.copy(
                lastError = "Lecture des états DJI impossible : ${error.message ?: error.javaClass.simpleName}",
            )
        }
    }

    private fun sdkVersionOrNull(): String? = runCatching {
        SDKManager.getInstance().sdkVersion
    }.getOrNull()

    private fun productTypeLabel(productType: ProductType): String = when (productType) {
        ProductType.DJI_MATRICE_4_SERIES -> "DJI Matrice 4E / 4T"
        ProductType.DJI_MATRICE_4D_SERIES -> "DJI Matrice 4D / 4TD"
        else -> productType.name
    }

    private fun remoteControllerTypeLabel(remoteControllerType: RemoteControllerType): String = when (remoteControllerType) {
        RemoteControllerType.DJI_RC_PLUS_2 -> "DJI RC Plus 2"
        RemoteControllerType.DJI_RC_PLUS -> "DJI RC Plus"
        else -> remoteControllerType.name
    }

    private fun withFirmwareWarning(state: DjiSdkState): DjiSdkState =
        state.copy(firmwareWarning = firmwareCompatibilityWarning(state))

    private fun firmwareCompatibilityWarning(state: DjiSdkState): String? {
        if (state.productType != "DJI Matrice 4E / 4T") return null

        val warnings = buildList {
            state.aircraftFirmwareVersion
                ?.takeIf { it.isNotBlank() && it != MATRICE_4_MSDK_5_18_AIRCRAFT_FIRMWARE }
                ?.let { add("firmware drone $it, attendu $MATRICE_4_MSDK_5_18_AIRCRAFT_FIRMWARE") }
            state.remoteControllerType
                ?.takeIf { it.isNotBlank() && it != "UNKNOWN" && it != "DJI RC Plus 2" }
                ?.let { add("RC détectée $it, attendu DJI RC Plus 2") }
            state.remoteControllerFirmwareVersion
                ?.takeIf { it.isNotBlank() && it != MATRICE_4_MSDK_5_18_RC_FIRMWARE }
                ?.let { add("firmware RC $it, attendu $MATRICE_4_MSDK_5_18_RC_FIRMWARE") }
        }
        return warnings.takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "; ", prefix = "Compatibilité MSDK 5.18 à vérifier : ")
    }

    private fun isAndroidEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        return fingerprint.contains("generic") ||
            fingerprint.contains("emulator") ||
            model.contains("sdk") ||
            model.contains("emulator") ||
            product.contains("sdk") ||
            hardware.contains("ranchu") ||
            hardware.contains("goldfish") ||
            manufacturer.contains("genymotion")
    }

    private const val MATRICE_4_MSDK_5_18_AIRCRAFT_FIRMWARE = "17.01.05.16"
    private const val MATRICE_4_MSDK_5_18_RC_FIRMWARE = "01.64.08.12"
}
