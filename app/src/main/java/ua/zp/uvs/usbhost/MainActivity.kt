package ua.zp.uvs.usbhost

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ua.zp.uvs.usbhost.databinding.ActivityMainBinding

lateinit var mUsbManager: UsbManager
var mDevice: UsbDevice? = null
var mAccessoryConnection: UsbDeviceConnection? = null
var mControlEndpoint: UsbEndpoint? = null
var mInEndpoint: UsbEndpoint? = null
var mOutEndpoint: UsbEndpoint? = null

var mProtocolVersion = 0
//var mDevice: UsbDevice? = null
var mAccessoryInterface: UsbInterface? = null
//var mAccessoryConnection: UsbDeviceConnection? = null
//var mControlEndpoint: UsbEndpoint? = null
var mTransport: UsbAccessoryBulkTransport? = null

lateinit var binding : ActivityMainBinding
lateinit var buttonCheck: Button
//private var bytes: ByteArray() = ByteArray(16384)
var bytes = ByteArray(16384)
private val TIMEOUT = 100
private val forceClaim = true
const val ACTION_USB_DEVICE_PERMISSION = "ua.zp.uvs.usbhost.USB_PERMISSION"
private const val MANUFACTURER = "Nokia"
private const val MODEL = "Nokia 3.4"
private const val DESCRIPTION = "Accessory Display Sink Test Application"
private const val VERSION = "1.0"
private const val URI = "http://www.android.com/"
private const val SERIAL = "0000000012345678"
private var mConnected = false

class MainActivity : AppCompatActivity() {

    var sendTaskUsb: SendTaskUsb? = null

//    val context = applicationContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        buttonCheck = binding.buttonCheck

        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_DEVICE_PERMISSION)
//        mReceiver = DeviceReceiver()
        registerReceiver(mReceiver, filter)

        val intent = intent
        if (intent.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            device?.let { onDeviceAttached(it) }
        } else {
            val devices: Map<String, UsbDevice> = mUsbManager.getDeviceList()
            if (devices != null) {
                for (device in devices.values) {
                    onDeviceAttached(device)
                }
            }
        }

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
        unregisterReceiver(mReceiver)
        sendTaskUsb?.onCancelled()
    }

    private fun terminat() {
        sendTaskUsb?.onCancelled()
        if (mAccessoryConnection!=null) {
            Toast.makeText(this, mAccessoryConnection.toString() +" .close", Toast.LENGTH_LONG)
                .show()
            mAccessoryConnection!!.close()
        }
    }

    private fun communicate() {
        sendTaskUsb = SendTaskUsb()
        sendTaskUsb?.execute(mOutEndpoint)
//        mDevice?.getInterface(0)?.also { intf ->
//            intf.getEndpoint(0)?.also { endpoint ->
//                mControlEndpoint = endpoint
//                mUsbManager.openDevice(mDevice)?.apply {
//                    mAccessoryConnection = this
//                    Toast.makeText(applicationContext, mAccessoryConnection.toString(), Toast.LENGTH_LONG)
//                        .show()
//                    claimInterface(intf, forceClaim)
////                    bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT) //do in another thread
//                    sendTaskUsb = SendTaskUsb()
//                    sendTaskUsb?.execute(mControlEndpoint)
//                }
//            }
//        }
    }

    private fun checkInfo() {
        var i = ""
//        manager = getSystemService(Context.USB_SERVICE) as UsbManager

        val deviceList: HashMap<String, UsbDevice> = mUsbManager.deviceList
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
            mDevice = deviceThis
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
            var bytesWritten = mAccessoryConnection?.bulkTransfer(endpoint, bytes, bytes.size, TIMEOUT)
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




    private fun onDeviceAttached(device: UsbDevice) {
        if (!mConnected) {
            connect(device)
        }
    }

    private fun connect(device: UsbDevice) {
        if (mConnected) {
            disconnect()
        }

        // Check whether we have permission to access the device.
        if (!mUsbManager.hasPermission(device)) {
//            mLogger.log("Prompting the user for access to the device.")
            val intent =
                Intent(ACTION_USB_DEVICE_PERMISSION)
//            Intent.setPackage(getPackageName())
            val pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, FLAG_MUTABLE
            )
            mUsbManager.requestPermission(device, pendingIntent)
            return
        }

        // Claim the device.
        val conn: UsbDeviceConnection = mUsbManager.openDevice(device)
        if (conn == null) {
//            mLogger.logError("Could not obtain device connection.")
            return
        }
        val iface = device.getInterface(0)
        val controlEndpoint = iface.getEndpoint(0)
        if (!conn.claimInterface(iface, true)) {
//            mLogger.logError("Could not claim interface.")
            return
        }
        try {
            // If already in accessory mode, then connect to the device.
            if (isAccessory(device)) {
//                mLogger.log("Connecting to accessory...")
                val protocolVersion: Int = getProtocol(conn)
                if (protocolVersion < 1) {
//                    mLogger.logError("Device does not support accessory protocol.")
                    return
                }
//                mLogger.log("Protocol version: $protocolVersion")

                // Setup bulk endpoints.
                var bulkIn: UsbEndpoint? = null
                var bulkOut: UsbEndpoint? = null
                for (i in 0 until iface.endpointCount) {
                    val ep = iface.getEndpoint(i)
                    if (ep.direction == UsbConstants.USB_DIR_IN) {
                        if (bulkIn == null) {
//                            mLogger.log(String.format("Bulk IN endpoint: %d", i))
                            bulkIn = ep
                        }
                    } else {
                        if (bulkOut == null) {
//                            mLogger.log(String.format("Bulk OUT endpoint: %d", i))
                            bulkOut = ep
                        }
                    }
                }
                if (bulkIn == null || bulkOut == null) {
//                    mLogger.logError("Unable to find bulk endpoints")
                    return
                }
//                mLogger.log("Connected")
                mConnected = true
                mDevice = device
                mProtocolVersion = protocolVersion
                mAccessoryInterface = iface
                mAccessoryConnection = conn
                mControlEndpoint = controlEndpoint
                mInEndpoint = bulkIn
                mOutEndpoint = bulkOut
//                mTransport = UsbAccessoryBulkTransport(conn, bulkIn, bulkOut)
                if (mProtocolVersion >= 2) {
//                    registerHid()
                }
//                startServices()
//                mTransport.startReading()
                return
            }

            // Do accessory negotiation.
//            mLogger.log("Attempting to switch device to accessory mode...")

            // Send get protocol.
            val protocolVersion: Int = getProtocol(conn)
            if (protocolVersion < 1) {
//                mLogger.logError("Device does not support accessory protocol.")
                return
            }
//            mLogger.log("Protocol version: $protocolVersion")

            // Send identifying strings.
            sendString(
                conn,
                UsbAccessoryConstants.ACCESSORY_STRING_MANUFACTURER,
                MANUFACTURER
            )
            sendString(
                conn,
                UsbAccessoryConstants.ACCESSORY_STRING_MODEL,
                MODEL
            )
            sendString(
                conn,
                UsbAccessoryConstants.ACCESSORY_STRING_DESCRIPTION,
                DESCRIPTION
            )
            sendString(
                conn,
                UsbAccessoryConstants.ACCESSORY_STRING_VERSION,
                VERSION
            )
            sendString(
                conn,
                UsbAccessoryConstants.ACCESSORY_STRING_URI,
                URI
            )
            sendString(
                conn,
                UsbAccessoryConstants.ACCESSORY_STRING_SERIAL,
                SERIAL
            )

            // Send start.
            // The device should re-enumerate as an accessory.
//            mLogger.log("Sending accessory start request.")
            val len = conn.controlTransfer(
                UsbConstants.USB_DIR_OUT or UsbConstants.USB_TYPE_VENDOR,
                UsbAccessoryConstants.ACCESSORY_START, 0, 0, null, 0, 10000
            )
            if (len != 0) {
//                mLogger.logError("Device refused to switch to accessory mode.")
            } else {
//                mLogger.log("Waiting for device to re-enumerate...")
            }
        } finally {
            if (!mConnected) {
                conn.releaseInterface(iface)
            }
        }
    }

    private fun sendString(conn: UsbDeviceConnection, index: Int, string: String) {
        val buffer = (string + "\u0000").toByteArray()
        val len = conn.controlTransfer(
            UsbConstants.USB_DIR_OUT or UsbConstants.USB_TYPE_VENDOR,
            UsbAccessoryConstants.ACCESSORY_SEND_STRING, 0, index,
            buffer, buffer.size, 10000
        )
        if (len != buffer.size) {
//            mLogger.logError("Failed to send string $index: \"$string\"")
        } else {
//            mLogger.log("Sent string $index: \"$string\"")
        }
    }

    private fun getProtocol(conn: UsbDeviceConnection): Int {
        val buffer = ByteArray(2)
        val len = conn.controlTransfer(
            UsbConstants.USB_DIR_IN or UsbConstants.USB_TYPE_VENDOR,
            UsbAccessoryConstants.ACCESSORY_GET_PROTOCOL, 0, 0, buffer, 2, 10000
        )
        return if (len != 2) {
            -1
        } else buffer[0].toInt()
    }

    private fun isAccessory(device: UsbDevice): Boolean {
        val vid = device.vendorId
        val pid = device.productId
        return (vid == UsbAccessoryConstants.USB_ACCESSORY_VENDOR_ID
                && (pid == UsbAccessoryConstants.USB_ACCESSORY_PRODUCT_ID
                || pid == UsbAccessoryConstants.USB_ACCESSORY_ADB_PRODUCT_ID))
    }

    private fun onDeviceDetached(device: UsbDevice) {
        if (mConnected && device == mDevice) {
            disconnect()
        }

    }

    private fun disconnect() {
//        stopServices()
//        unregisterHid()
//
//        mLogger.log("Disconnected.")
        mConnected = false
        mDevice = null
        mAccessoryConnection = null
        mAccessoryInterface = null
        mControlEndpoint = null
        if (mTransport != null) {
            mTransport!!.ioClose()
            mTransport = null
        }
    }

    private val mReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            if (device != null) {
                val action = intent.action
                if (action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                    onDeviceAttached(device)
                } else if (action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                    onDeviceDetached(device)
                } else if (ACTION_USB_DEVICE_PERMISSION == action) {
                    synchronized(this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.apply {
                                //call method to set up device communication
                                onDeviceAttached(device)
                            }
                        } else {
//                        Log.d(TAG, "permission denied for device $device")
                        }
                    }
                }
            }
        }
    }

}