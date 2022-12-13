package ua.zp.uvs.usbhost

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ua.zp.uvs.usbhost.databinding.ActivityMainBinding

lateinit var manager: UsbManager
var device: UsbDevice? = null
var usbDeviseConnection: UsbDeviceConnection? = null
var endpointM: UsbEndpoint? = null
lateinit var binding : ActivityMainBinding
lateinit var buttonCheck: Button
private lateinit var bytes: ByteArray
private val TIMEOUT = 100
private val forceClaim = true


class MainActivity : AppCompatActivity() {
    var sendTaskUsb: SendTaskUsb? = null
//    val context = applicationContext

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

        binding.buttonCommunicate.setOnClickListener(View.OnClickListener {
            communicate()
        }
        )

        binding.buttonTerminat.setOnClickListener(View.OnClickListener {
            terminat()
        }
        )


    }

    override fun onDestroy() {
        super.onDestroy()
        sendTaskUsb?.onCancelled()
    }

    private fun terminat() {
        sendTaskUsb?.onCancelled()
        if (usbDeviseConnection!=null) {
            Toast.makeText(this, usbDeviseConnection.toString() +" .close", Toast.LENGTH_LONG)
                .show()
            usbDeviseConnection!!.close()
        }
    }

    private fun communicate() {
        device?.getInterface(0)?.also { intf ->
            intf.getEndpoint(0)?.also { endpoint ->
                endpointM = endpoint
                manager.openDevice(device)?.apply {
                    usbDeviseConnection = this
                    Toast.makeText(applicationContext, usbDeviseConnection.toString(), Toast.LENGTH_LONG)
                        .show()
                    claimInterface(intf, forceClaim)
//                    bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread
                    sendTaskUsb = SendTaskUsb()
                    sendTaskUsb?.execute(endpointM)
                }
            }
        }
    }

    private fun checkInfo() {
        var i = ""
        manager = getSystemService(Context.USB_SERVICE) as UsbManager

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
        deviceList.values.forEach { deviceThis ->
            //your code
            i += "\n" +
                    "DeviceID: " + deviceThis.getDeviceId() + "\n" +
                    "DeviceName: " + deviceThis.getDeviceName() + "\n" +
                    "DeviceClass: " + deviceThis.getDeviceClass() + "\n" +
                    "DeviceSubClass: " + deviceThis.getDeviceSubclass() + "\n" +
                    "VendorID: " + deviceThis.getVendorId() + "\n" +
                    "ProductID: " + deviceThis.getProductId() + "\n";
            device = deviceThis
        }
        binding.textInfo.text = i
    }

    class SendTaskUsb :
        AsyncTask<UsbEndpoint?, String?, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
//            progressBar2.setVisibility(ProgressBar.VISIBLE)
        }

        protected override fun doInBackground(vararg endpoints: UsbEndpoint?): Void? {
            try {
//                sendName("name" + ua.zp.uvs.wifisender.ServerFileActivity.fileInform.getChosenFile())
//                Thread.sleep(5000)
//                sendFile(
//                    ua.zp.uvs.wifisender.ServerFileActivity.fileInform.getPath(),
//                    ua.zp.uvs.wifisender.ServerFileActivity.fileInform.getChosenFile()
//                )
//                Thread.sleep(5000)
//                sendName(endpoints[0], "Start")
                publishProgress(sendName(endpoints[0], "Start"))
            } catch (e: Exception) {
                publishProgress(e.toString())
            }
            return null
        }

        private fun sendName(endpoint: UsbEndpoint?, chosenFile: String): String? {

            // Check that there's actually something to send
            if (chosenFile.length > 0) {
                // Get the message bytes and tell the BluetoothChatService to write
                val send = chosenFile.toByteArray()
                bytes = send
            }
            var bytesWritten = usbDeviseConnection?.bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT)
            return bytesWritten.toString()
        }

        protected override fun onProgressUpdate(vararg nextLine: String?) {
            super.onProgressUpdate(*nextLine)

//            showMessageToast(nextLine[0])
            binding.textInfo.text = nextLine[0]

        }


        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
//            progressBar2.setVisibility(ProgressBar.INVISIBLE)
            onCancelled()
        }

        public override fun onCancelled() {
            super.onCancelled()
            //            showMessageToast("onCancelledClient");
        }
    }

    fun showMessageToast(s: String?) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG)
            .show()
    }

}