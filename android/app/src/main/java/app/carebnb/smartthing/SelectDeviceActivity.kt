package app.carebnb.smartthing

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_select_device.*
import kotlinx.android.synthetic.main.activity_select_device.devices_spinner
import kotlinx.android.synthetic.main.activity_select_device.refresh_devices_button
import java.util.ArrayList

class SelectDeviceActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)
        refresh_devices_button.setOnClickListener { refresh() }
        next_button.setOnClickListener { next() }
        refresh()
    }

    private fun refresh() {
        val adapterDevices = SpinnerTextAdapter(this, R.layout.spinner_text, ArrayList())
        devices_spinner.adapter = adapterDevices
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled) {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetooth, 0)
        }
        val pairedDevices = mBluetoothAdapter!!.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                for (uuid in device.uuids) {
                    if (uuid.toString().equals(this.getString(R.string.bt_uuid))){
                        adapterDevices.add(device)
                        break
                    }
                }

            }
        }
        if(adapterDevices.devices.size == 0){
            Toast.makeText(this, "No device found. Please sync with bluetooth before continuing.", Toast.LENGTH_LONG).show()
        }
        else{
            next_button.isEnabled = true
        }
    }

    private fun next(){
        val intent = Intent(this, SetupActivity::class.java)
        val device = devices_spinner.selectedItem as BluetoothDevice
        intent.putExtra(SetupActivity.PARAM_DEVICE, device)
        startActivity(intent)
    }
}