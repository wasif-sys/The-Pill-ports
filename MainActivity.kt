package com.example.medicinepos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private data class Medicine(
    val id: Int,
    val name: String,
    val brand: String,
    val price: Double,
    val stock: Int,
    val batch: String,
    val expiry: String
)

private data class CartLine(val medicine: Medicine, val quantity: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MedicinePosApp() }
    }
}

@Composable
fun MedicinePosApp() {
    val medicines = remember {
        mutableStateListOf(
            Medicine(1, "Paracetamol 500 mg", "Generic", 18.00, 120, "PCM2408", "08/2027"),
            Medicine(2, "Cetirizine 10 mg", "Generic", 22.50, 65, "CTZ2411", "11/2027"),
            Medicine(3, "ORS Sachet", "Generic", 21.00, 80, "ORS2501", "01/2028"),
            Medicine(4, "Vitamin C 500 mg", "HealthPlus", 45.00, 40, "VTC2409", "09/2027"),
            Medicine(5, "Antacid Tablets", "CareMed", 32.00, 8, "ANT2405", "05/2027"),
            Medicine(6, "Cough Syrup 100 ml", "CareMed", 95.00, 14, "CS2502", "02/2028")
        )
    }
    val cart = remember { mutableStateListOf<CartLine>() }
    var search by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf("Sale") }
    var showCheckout by remember { mutableStateOf(false) }
    var receipt by remember { mutableStateOf<String?>(null) }
    val money: (Double) -> String = { value ->
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
    }
    val filtered = medicines.filter {
        it.name.contains(search, true) || it.brand.contains(search, true) ||
            it.batch.contains(search, true)
    }
    val subtotal = cart.sumOf { it.medicine.price * it.quantity }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF087F8C),
            secondary = Color(0xFF3A7D44),
            background = Color(0xFFF4F7F8)
        )
    ) {
        Scaffold(
            topBar = {
                Column(
                    Modifier.fillMaxWidth().background(Color(0xFF087F8C)).padding(18.dp)
                ) {
                    Text("MEDICINE POS", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Pharmacy billing & stock", color = Color.White.copy(alpha = .85f), fontSize = 13.sp)
                }
            },
            bottomBar = {
                NavigationBar {
                    listOf("Sale", "Inventory", "Reports").forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Text(when (item) { "Sale" -> "🛒"; "Inventory" -> "📦"; else -> "📊" }) },
                            label = { Text(item) }
                        )
                    }
                }
            },
            containerColor = Color(0xFFF4F7F8)
        ) { padding ->
            when (tab) {
                "Sale" -> SaleScreen(
                    modifier = Modifier.padding(padding),
                    search = search,
                    onSearch = { search = it },
                    medicines = filtered,
                    cart = cart,
                    money = money,
                    subtotal = subtotal,
                    onAdd = { medicine ->
                        val current = cart.find { it.medicine.id == medicine.id }
                        if (current == null && medicine.stock > 0) cart.add(CartLine(medicine, 1))
                        else if (current != null && current.quantity < medicine.stock) {
                            val index = cart.indexOf(current)
                            cart[index] = current.copy(quantity = current.quantity + 1)
                        }
                    },
                    onQuantity = { line, delta ->
                        val index = cart.indexOf(line)
                        if (index >= 0) {
                            val newQty = line.quantity + delta
                            if (newQty <= 0) cart.removeAt(index)
                            else if (newQty <= line.medicine.stock) cart[index] = line.copy(quantity = newQty)
                        }
                    },
                    onCheckout = { if (cart.isNotEmpty()) showCheckout = true }
                )
                "Inventory" -> InventoryScreen(Modifier.padding(padding), medicines, money)
                else -> ReportsScreen(Modifier.padding(padding), money)
            }
        }

        if (showCheckout) {
            AlertDialog(
                onDismissRequest = { showCheckout = false },
                title = { Text("Confirm sale") },
                text = {
                    Column {
                        Text("${cart.sumOf { it.quantity }} item(s)")
                        Spacer(Modifier.height(8.dp))
                        Text("Total: ${money(subtotal)}", fontWeight = FontWeight.Bold)
                        Text("This demo does not yet save sales or reduce stock permanently.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        receipt = "Sale completed • Total ${money(subtotal)}"
                        cart.clear()
                        showCheckout = false
                    }) { Text("Complete sale") }
                },
                dismissButton = { TextButton(onClick = { showCheckout = false }) { Text("Cancel") } }
            )
        }
        receipt?.let { message ->
            AlertDialog(
                onDismissRequest = { receipt = null },
                title = { Text("Sale complete") },
                text = { Text(message) },
                confirmButton = { TextButton(onClick = { receipt = null }) { Text("OK") } }
            )
        }
    }
}

@Composable
private fun SaleScreen(
    modifier: Modifier,
    search: String,
    onSearch: (String) -> Unit,
    medicines: List<Medicine>,
    cart: List<CartLine>,
    money: (Double) -> String,
    subtotal: Double,
    onAdd: (Medicine) -> Unit,
    onQuantity: (CartLine, Int) -> Unit,
    onCheckout: () -> Unit
) {
    Column(modifier.fillMaxSize().padding(14.dp)) {
        OutlinedTextField(
            value = search, onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search medicine, brand or batch") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("MEDICINES", fontWeight = FontWeight.Bold, color = Color(0xFF52636A), fontSize = 12.sp)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(medicines) { medicine ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(medicine.name, fontWeight = FontWeight.SemiBold)
                            Text("${medicine.brand} • Batch ${medicine.batch}", fontSize = 11.sp, color = Color.Gray)
                            Text("Expiry ${medicine.expiry} • Stock ${medicine.stock}", fontSize = 11.sp, color = if (medicine.stock < 10) Color(0xFFB54708) else Color.Gray)
                            Text(money(medicine.price), color = Color(0xFF087F8C), fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { onAdd(medicine) }, enabled = medicine.stock > 0) { Text("Add") }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Current bill (${cart.sumOf { it.quantity }} units)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(money(subtotal), fontWeight = FontWeight.Bold, color = Color(0xFF087F8C), fontSize = 18.sp)
                }
                if (cart.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    cart.take(3).forEach { line ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(line.medicine.name, modifier = Modifier.weight(1f), fontSize = 12.sp)
                            TextButton(onClick = { onQuantity(line, -1) }) { Text("−") }
                            Text("${line.quantity}")
                            TextButton(onClick = { onQuantity(line, 1) }) { Text("+") }
                        }
                    }
                    if (cart.size > 3) Text("+ ${cart.size - 3} more products", fontSize = 11.sp, color = Color.Gray)
                } else {
                    Text("Add medicines to start a bill.", color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onCheckout, enabled = cart.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text("CHECKOUT • ${money(subtotal)}")
                }
            }
        }
    }
}

@Composable
private fun InventoryScreen(modifier: Modifier, medicines: List<Medicine>, money: (Double) -> String) {
    Column(modifier.fillMaxSize().padding(14.dp)) {
        Text("Inventory overview", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Sample stock data • read-only demo", color = Color.Gray)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryCard("Products", "${medicines.size}", Modifier.weight(1f))
            SummaryCard("Low stock", "${medicines.count { it.stock < 10 }}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(medicines) { med ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(med.name, fontWeight = FontWeight.SemiBold)
                            Text("Batch ${med.batch} • Exp: ${med.expiry}", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Stock ${med.stock}", color = if (med.stock < 10) Color(0xFFB54708) else Color.DarkGray)
                            Text(money(med.price), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF087F8C))
        }
    }
}

@Composable
private fun ReportsScreen(modifier: Modifier, money: (Double) -> String) {
    Column(modifier.fillMaxSize().padding(18.dp)) {
        Text("Reports", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Today's sales", color = Color.Gray)
                Text(money(0.0), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("No saved transactions in this starter demo.")
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Planned: daily/monthly sales, profit, GST summary, returns and cashier-wise reports.", color = Color.Gray)
    }
}
