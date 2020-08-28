package app.carebnb.smartthing

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_setup.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.text.NumberFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.concurrent.thread

class SetupActivity: AppCompatActivity() {

    companion object {
        const val PARAM_DEVICE = "device"
        const val PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 12345
        const val TAG = "SetupActivity"
    }

    inner class WifiScanReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            val wifiScanList: List<ScanResult> = wifiManager.scanResults
            val set: MutableSet<String> = mutableSetOf()
            wifiScanList.forEach { item ->
                if(!item.SSID.isNullOrEmpty()) {
                    set.add(item.SSID)
                }
            }
            val spinnerArrayAdapter = ArrayAdapter(this@SetupActivity, android.R.layout.simple_spinner_item, set.toList())
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            (wifi_name_holder.editText as? AutoCompleteTextView)?.setAdapter(spinnerArrayAdapter)
            writeOutput("Scanning completed.")
            refresh_wifi_button.isEnabled = true
            refresh_wifi_button.isClickable = true
        }
    }

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiReciever: WifiScanReceiver
    private lateinit var device: BluetoothDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        device = intent.extras!!.get(PARAM_DEVICE) as BluetoothDevice
        title_txt.text = device.name

        refresh_wifi_button.setOnClickListener{ refreshWifi() }
        save_settings_button.setOnClickListener{
            val ssid: String = wifi_name_holder.editText?.text.toString()
            val psk: String = wifi_password_input.text.toString()
            thread {
                workerThread(ssid, psk)
            }
        }

        wifiReciever = WifiScanReceiver()
        registerReceiver(wifiReciever, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        refreshWifi()

        wifi_password_input.addTextChangedListener { checkParams() }
        wifi_name_input.addTextChangedListener { checkParams() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            wifiScan()
        }
    }

    private fun refreshWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION)
        } else {
            wifiScan()
        }
    }

    private fun wifiScan(){
        refresh_wifi_button.isEnabled = false
        refresh_wifi_button.isClickable = false
        writeOutput("Scanning for wifi networks...")
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.startScan()
    }

    private fun writeOutput(text: String) {
//        runOnUiThread {
//            messages_text.text = ("${messages_text.text}\n${text}")
//        }
        Log.d(TAG, text)
    }

    private fun workerThread(ssid: String, psk: String) {
        writeOutput("Starting config update.")
        writeOutput("Network: $ssid")
        writeOutput("Device: " + device.name + " - " + device.address)
        try {
            val uuid = UUID.fromString(getString(app.carebnb.smartthing.R.string.bt_uuid))
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            if (!socket.isConnected) {
                socket.connect()
                Thread.sleep(1000)
            }
            writeOutput("Connected.")
            val outputStream: OutputStream = socket.outputStream
            writeOutput("Output stream OK.")
            val inputStream: InputStream = socket.inputStream
            writeOutput("Input stream OK.")
            waitForResponse(inputStream, -1)
            writeOutput("Sending SSID.")
            outputStream.write(ssid.toByteArray())
            outputStream.flush()
            waitForResponse(inputStream, -1)
            writeOutput("Sending PSK.")
            outputStream.write(psk.toByteArray())
            outputStream.flush()

            runOnUiThread {
                initCountdown(15)
            }
            val response = waitForResponse(inputStream, -1)

            // Extract IP address
            val zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])"
            val ipPatter = "$zeroTo255\\.$zeroTo255\\.$zeroTo255\\.$zeroTo255"
            val pattern = Pattern.compile(ipPatter)
            val matcher: Matcher = pattern.matcher(response)
            if (matcher.find()) {
                val ip = matcher.group()
                writeOutput(ip)
                runOnUiThread {
                    Toast.makeText(this, "${device.name} has been set up with success!", Toast.LENGTH_LONG).show()
                }
            }
            else{
                runOnUiThread {
                    Toast.makeText(this, "Error. Check WiFi password.", Toast.LENGTH_LONG).show()
                }
            }

            // Reset views
            runOnUiThread {
                save_settings_button.isEnabled = true
                save_settings_button.isClickable = true
                progress_bar.visibility = View.GONE
                refresh_wifi_button.visibility = View.VISIBLE
            }

            socket.close()
            writeOutput("Success.")
        } catch (e: Exception) {
            e.printStackTrace()
            save_settings_button.isEnabled = true
            save_settings_button.isClickable = true
            writeOutput("Failed.")
            runOnUiThread {
                Toast.makeText(this, "It looks like this device is not a Carebnb device.", Toast.LENGTH_LONG).show()
            }
        }
        writeOutput("Done.")
    }

    private fun initCountdown(seconds: Long) {
        var countdown = 0
        progress_bar.visibility = View.VISIBLE
        refresh_wifi_button.visibility = View.GONE
        save_settings_button.isEnabled = false
        save_settings_button.isClickable = false
        object : CountDownTimer(seconds * 1000, 50) {
            override fun onTick(millisUntilFinished: Long) {
                countdown++
                progress_bar.progress = (countdown * 100 / (seconds * 1000 / 50)).toInt()
            }
            override fun onFinish() {
                countdown++
                progress_bar.progress = 100
            }
        }.start()
    }

    private fun checkParams(){
        save_settings_button.isEnabled = (wifi_password_input.text.toString().isNullOrEmpty().not() && wifi_name_holder.editText?.text.isNullOrEmpty().not())
    }

    /*
     * TODO actually use the timeout
     */
    @Throws(IOException::class)
    private fun waitForResponse(mmInputStream: InputStream, timeout: Long): String {
        val delimiter: Byte = 33
        var readBufferPosition = 0
        var bytesAvailable: Int
        while (true) {
            bytesAvailable = mmInputStream.available()
            if (bytesAvailable > 0) {
                val packetBytes = ByteArray(bytesAvailable)
                val readBuffer = ByteArray(1024)
                mmInputStream.read(packetBytes)
                for (i in 0 until bytesAvailable) {
                    val b = packetBytes[i]
                    if (b == delimiter) {
                        val encodedBytes = ByteArray(readBufferPosition)
                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.size)
                        val data = String(encodedBytes, Charset.forName("US-ASCII"))
                        writeOutput("Received:$data")
                        return data
                    } else {
                        readBuffer[readBufferPosition++] = b
                    }
                }
            }
        }
    }
}