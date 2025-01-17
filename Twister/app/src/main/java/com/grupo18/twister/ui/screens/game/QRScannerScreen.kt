import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions


@Composable
fun QRScannerScreen(
    paddingValues: PaddingValues,
    onQRCodeScanned: (String) -> Unit // Callback para manejar el resultado
) {
    val pinSala: MutableState<String> = remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            val pin = result.contents ?: ""
            if (pin.isNotEmpty()) {
                pinSala.value = pin // Asignar el PIN de la sala escaneado
                onQRCodeScanned(pin) // Llamar al callback con el resultado
            }
        }
    )

    Surface(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        val scanOptions = ScanOptions().apply {
                            setBeepEnabled(true)
                            setCaptureActivity(CaptureActivity::class.java)
                            setOrientationLocked(false)
                        }
                        scanLauncher.launch(scanOptions)
                    }
                ) {
                    Text(text = "Scan QR Code")
                }
            }

            if (pinSala.value.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Room PIN: ${pinSala.value}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "Scan a QR code to join a room")
                }
            }
        }
    }
}