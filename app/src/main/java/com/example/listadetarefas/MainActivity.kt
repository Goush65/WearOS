package com.example.listadetarefas

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa o AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // ETAPA 2: Instancia e testa a verificação de saída de áudio
        val audioHelper = AudioHelper(this)
        val isSpeakerAvailable = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val isBluetoothHeadsetConnected = audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)

        // ETAPA 3: Registro dinâmico do Callback para fone Bluetooth
        audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesAdded(addedDevices)
                if (audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    // Fone de ouvido Bluetooth conectado
                }
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                super.onAudioDevicesRemoved(removedDevices)
                if (!audioHelper.audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)) {
                    // Fone de ouvido Bluetooth desconectado
                }
            }
        }, null)

        // ETAPA 4: Se não houver fone Bluetooth conectado, direciona para as configurações
        if (!isBluetoothHeadsetConnected) {
            abrirConfiguracoesBluetooth()
        }
    }

    // ETAPA 4: Função para abrir as configurações do Bluetooth
    private fun abrirConfiguracoesBluetooth() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("EXTRA_CONNECTION_ONLY", true)
            putExtra("EXTRA_CLOSE_ON_CONNECT", true)
            putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
        }
        startActivity(intent)
    }
}

// ETAPA 2: Classe auxiliar para verificação de saídas de áudio
class AudioHelper(private val context: Context) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun audioOutputAvailable(type: Int): Boolean {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return false
        }
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { it.type == type }
    }
}