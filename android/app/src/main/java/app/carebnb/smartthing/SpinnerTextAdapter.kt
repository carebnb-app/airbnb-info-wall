package app.carebnb.smartthing

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SpinnerTextAdapter(context: Context, resource: Int, val devices: MutableList<BluetoothDevice>) : ArrayAdapter<BluetoothDevice>(context, resource, devices) {
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = inflater.inflate(R.layout.spinner_text_padded, parent, false)
        val device = devices[position]
        val name = row.findViewById<View>(R.id.name_label) as TextView
        name.text = device.name
        return row
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = inflater.inflate(R.layout.spinner_text, parent, false)
        val device = devices[position]
        val name = row.findViewById<View>(R.id.name_label) as TextView
        name.text = device.name
        return row
    }
}