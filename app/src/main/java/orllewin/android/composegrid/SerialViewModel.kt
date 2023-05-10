package orllewin.android.composegrid

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.lifecycle.ViewModel
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.io.InputStream

/*

    Yes, this should all be in a Repository/Datastore, it was thrown together in a couple of hours.
    First carry out a usbScan, if successful call initialise to open the serial port and fetch the
    Lua bytecode from assets/

 */
class SerialViewModel: ViewModel(), SerialInputOutputManager.Listener {

    data class DiscoveredDevice(val device: android.hardware.usb.UsbDevice, val driver: UsbSerialDriver?)

    private var playdate: DiscoveredDevice? = null

    private lateinit var serialIOManager: SerialInputOutputManager
    private lateinit var usbManager: UsbManager
    private var usbSerialPort: UsbSerialPort? = null
    private val bytecode = mutableListOf<ByteArray>()

    fun usbScan(context: Context, onScanComplete: (success: Boolean) -> Unit){
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        for (device in usbManager.deviceList.values) {
            val driver = usbDefaultProber.probeDevice(device)
            when {
                driver != null -> {
                    for (port in driver.ports.indices){
                        if (device.productName?.lowercase() == "playdate"){
                            playdate = DiscoveredDevice(device, driver)
                        }
                    }
                }
            }
        }

        when {
            playdate != null -> {
                println("Playdate found")
                onScanComplete(true)
            }
            else -> {
                println("Playdate not found")
                onScanComplete(false)
            }
        }
    }

    fun initialise(context: Context, onError: (error: String) -> Unit){
        val driver = playdate?.driver

        when {
            driver == null -> {
                onError("No driver available")
                return
            }
            driver.ports.size < 1 -> {
                onError("No port available")
                return
            }
            else -> {
                usbSerialPort = driver.ports[0]

                val usbConnection = usbManager.openDevice(driver.device)
                usbSerialPort?.open(usbConnection)
                usbSerialPort?.dtr = true
                usbSerialPort?.rts = true
                usbSerialPort?.setParameters(
                    9600,
                    UsbSerialPort.DATABITS_8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )

                serialIOManager = SerialInputOutputManager(usbSerialPort, this)
                serialIOManager.start()

                bytecode.add(getBytecode(context, "serial1x1.luac"))
                bytecode.add(getBytecode(context, "serial2x1.luac"))
                bytecode.add(getBytecode(context, "serial3x1.luac"))
                bytecode.add(getBytecode(context, "serial4x1.luac"))
                bytecode.add(getBytecode(context, "serial1x2.luac"))
                bytecode.add(getBytecode(context, "serial2x2.luac"))
                bytecode.add(getBytecode(context, "serial3x2.luac"))
                bytecode.add(getBytecode(context, "serial4x2.luac"))
            }
        }
    }

    private fun getBytecode(context: Context, file: String): ByteArray{
        val inputStream: InputStream = context.assets.open(file)
        val bytes = inputStream.readBytes()
        inputStream.close()
        return bytes
    }

    fun serialSend(index: Int){
        if(playdate == null) return
        val bytes = bytecode[index]
        usbSerialPort?.write("eval ${bytes.size}\n".toByteArray(), 0)
        usbSerialPort?.write(bytes, 100)
    }

    override fun onNewData(data: ByteArray?) {
        if(data == null) return
        val response = String(data)
        println("Playdate onNewData(): $response")
    }

    override fun onRunError(e: Exception?) {
        println("Playdate onRunError(): $e")
    }
}