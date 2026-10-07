package com.example.ticketapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale


enum class StatusPesanan {
    AWAL,          // Silakan pesan tiket
    MEMPROSES,     // Memproses pesanan... (5 detik)
    BERHASIL,      // Tiket telah dipesan
    NAMA_KOSONG    // Nama masih kosong
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TiketScreen()
            }
        }
    }
}

@Composable
fun TiketScreen() {
    var hargaTiket by remember { mutableIntStateOf(15_000) }
    var jumlahTiket by remember { mutableIntStateOf(1) }
    var namaPembeli by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(StatusPesanan.AWAL) }

    LaunchedEffect(status) {
        if (status == StatusPesanan.MEMPROSES) {
            delay(5_000)
            status = StatusPesanan.BERHASIL
        }
    }

    TiketContent(
        hargaTiket = hargaTiket,
        jumlahTiket = jumlahTiket,
        namaPembeli = namaPembeli,
        status = status,
        onNamaChange = {
            namaPembeli = it
            // kalau sebelumnya error, kembalikan ke awal saat user mengetik
            if (status == StatusPesanan.NAMA_KOSONG) status = StatusPesanan.AWAL
        },
        onTambah = { jumlahTiket++ },
        onKurang = { if (jumlahTiket > 1) jumlahTiket-- },
        onPesan = {
            status = if (namaPembeli.isBlank()) {
                StatusPesanan.NAMA_KOSONG
            } else {
                StatusPesanan.MEMPROSES
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiketContent(
    hargaTiket: Int,
    jumlahTiket: Int,
    namaPembeli: String,
    status: StatusPesanan,
    onNamaChange: (String) -> Unit,
    onTambah: () -> Unit,
    onKurang: () -> Unit,
    onPesan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val biru = Color(0xFF1E5BC6)
    val sedangMemproses = status == StatusPesanan.MEMPROSES

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Pemesanan Tiket", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = biru)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Harga Tiket", fontWeight = FontWeight.SemiBold)
            Text(
                text = formatRupiah(hargaTiket) + " / tiket",
                fontSize = 18.sp,
                color = biru,
                fontWeight = FontWeight.Bold
            )

            Text("Nama", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = namaPembeli,
                onValueChange = onNamaChange,
                placeholder = { Text("Masukkan nama Anda") },
                singleLine = true,
                enabled = !sedangMemproses,
                isError = status == StatusPesanan.NAMA_KOSONG,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Jumlah Tiket", fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalButton(onClick = onKurang, enabled = !sedangMemproses) { Text("-") }
                Text(
                    text = jumlahTiket.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(onClick = onTambah, enabled = !sedangMemproses) { Text("+") }
            }

            Text(
                text = "Total: " + formatRupiah(hargaTiket * jumlahTiket),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Button(
                onClick = onPesan,
                enabled = !sedangMemproses,
                colors = ButtonDefaults.buttonColors(containerColor = biru),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pesan Tiket")
            }

            StatusBox(status)
        }
    }
}

@Composable
fun StatusBox(status: StatusPesanan) {
    val (bg, fg, teks) = when (status) {
        StatusPesanan.AWAL ->
            Triple(Color(0xFFF1F5F9), Color(0xFF475569), "Silakan pesan tiket")
        StatusPesanan.MEMPROSES ->
            Triple(Color(0xFFE3F0FF), Color(0xFF1E5BC6), "Memproses pesanan.........")
        StatusPesanan.BERHASIL ->
            Triple(Color(0xFFE6F6EA), Color(0xFF1E8E3E), "Tiket telah dipesan")
        StatusPesanan.NAMA_KOSONG ->
            Triple(Color(0xFFFDE8E8), Color(0xFFD32F2F), "Nama masih kosong")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (status) {
            StatusPesanan.MEMPROSES -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = fg
            )
            StatusPesanan.BERHASIL -> Icon(
                Icons.Default.CheckCircle, contentDescription = null,
                tint = fg, modifier = Modifier.size(18.dp)
            )
            StatusPesanan.NAMA_KOSONG -> Icon(
                Icons.Default.Warning, contentDescription = null,
                tint = fg, modifier = Modifier.size(18.dp)
            )
            else -> {}
        }
        if (status != StatusPesanan.AWAL) Spacer(Modifier.width(8.dp))
        Text("Status: $teks", color = fg, fontSize = 13.sp)
    }
}

private fun formatRupiah(nilai: Int): String =
    "Rp " + NumberFormat.getNumberInstance(Locale("id", "ID")).format(nilai)

@Preview(showBackground = true)
@Composable
fun TiketScreenPreview() {
    MaterialTheme { TiketScreen() }
}