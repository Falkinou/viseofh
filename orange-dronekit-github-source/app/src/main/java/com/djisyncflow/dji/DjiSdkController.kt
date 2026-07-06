package com.djisyncflow.dji

import android.content.Context
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import dji.v5.network.DJINetworkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DjiSdkState(
    val available: Boolean = false,
    val initialized: Boolean = false,
    val registered: Boolean = false,
    val productConnected: Boolean = false,
    val productId: Int? = null,
    val message: String = "SDK DJI non démarré",
    val lastError: String? = null,
)

object DjiSdkController {
    private val mutableState = MutableStateFlow(DjiSdkState())
    val state: StateFlow<DjiSdkState> = mutableState.asStateFlow()

    @Volatile
    private var started = false

    fun start(context: Context) {
        if (started) return
        started = true
        runCatching {
            mutableState.value = DjiSdkState(available = true, message = "Initialisation SDK DJI...")
            SDKManager.getInstance().init(context.applicationContext, object : SDKManagerCallback {
                override fun onRegisterSuccess() {
                    mutableState.value = mutableState.value.copy(
                        available = true,
                        registered = true,
                        lastError = null,
                        message = "SDK DJI enregistré, en attente du drone",
                    )
                }

                override fun onRegisterFailure(error: IDJIError) {
                    mutableState.value = mutableState.value.copy(
                        available = true,
                        registered = false,
                        lastError = error.description(),
                        message = "Enregistrement DJI refusé : ${error.description()}",
                    )
                }

                override fun onProductDisconnect(productId: Int) {
                    mutableState.value = mutableState.value.copy(
                        productConnected = false,
                        productId = productId,
                        message = "Drone DJI déconnecté",
                    )
                }

                override fun onProductConnect(productId: Int) {
                    mutableState.value = mutableState.value.copy(
                        productConnected = true,
                        productId = productId,
                        lastError = null,
                        message = "Drone DJI connecté",
                    )
                }

                override fun onProductChanged(productId: Int) {
                    mutableState.value = mutableState.value.copy(
                        productId = productId,
                        message = "Produit DJI détecté",
                    )
                }

                override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                    val initialized = event == DJISDKInitEvent.INITIALIZE_COMPLETE
                    mutableState.value = mutableState.value.copy(
                        available = true,
                        initialized = initialized,
                        message = if (initialized) "SDK DJI initialisé" else "Initialisation DJI $totalProcess%",
                    )
                    if (initialized) {
                        SDKManager.getInstance().registerApp()
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
                    mutableState.value = mutableState.value.copy(
                        message = "Réseau DJI disponible, nouvel enregistrement...",
                    )
                    SDKManager.getInstance().registerApp()
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
}
