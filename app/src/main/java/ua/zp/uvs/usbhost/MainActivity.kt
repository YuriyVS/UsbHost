package ua.zp.uvs.usbhost

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ua.zp.uvs.usbhost.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding
    lateinit var buttonCheck: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        buttonCheck = binding.buttonCheck

        buttonCheck.setOnClickListener(View.OnClickListener {
            checkInfo()
        }
        )




    }

    private fun checkInfo() {
        var i = ""
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager

        val deviceList: HashMap<String, UsbDevice> = manager.deviceList
        if (deviceList.size == 0){
            i += "\n" +
            "Devices not find " + "\n" +
                    "DeviceID: " + "\n" +
                    "DeviceName: "  + "\n" +
                    "DeviceClass: "  + "\n" +
                    "DeviceSubClass: "  + "\n" +
                    "VendorID: "  + "\n" +
                    "ProductID: "  + "\n";
        }
        deviceList.values.forEach { device ->
            //your code
            i += "\n" +
                    "DeviceID: " + device.getDeviceId() + "\n" +
                    "DeviceName: " + device.getDeviceName() + "\n" +
                    "DeviceClass: " + device.getDeviceClass() + "\n" +
                    "DeviceSubClass: " + device.getDeviceSubclass() + "\n" +
                    "VendorID: " + device.getVendorId() + "\n" +
                    "ProductID: " + device.getProductId() + "\n";
        }
        binding.textInfo.text = i
    }
}