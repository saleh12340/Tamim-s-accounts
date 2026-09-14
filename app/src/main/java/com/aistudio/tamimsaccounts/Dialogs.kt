package com.aistudio.tamimsaccounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerDialog(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (Customer) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var initialBalanceStr by remember { mutableStateOf(if (customer != null) customer.balance.toString() else "0") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }
    var isDebt by remember { mutableStateOf(customer?.let { it.balance >= 0 } ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (customer == null) "إضافة عميل جديد" else "تعديل بيانات العميل",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل *") },
                    placeholder = { Text("مثال: محمد عبدالله الصالح") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف (واتساب)") },
                    placeholder = { Text("770000000") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (customer == null) {
                    Text("الرصيد الافتتاحي (إن وجد):", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isDebt,
                            onClick = { isDebt = true },
                            label = { Text("عليه دين سابق") },
                            leadingIcon = { if (isDebt) Icon(Icons.Default.Check, contentDescription = null) else null }
                        )
                        FilterChip(
                            selected = !isDebt,
                            onClick = { isDebt = false },
                            label = { Text("له رصيد مسبق") },
                            leadingIcon = { if (!isDebt) Icon(Icons.Default.Check, contentDescription = null) else null }
                        )
                    }

                    OutlinedTextField(
                        value = initialBalanceStr,
                        onValueChange = { initialBalanceStr = it },
                        label = { Text("المبلغ الافتتاحي") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    placeholder = { Text("العنوان، نوع الدفع، أو أي تفاصيل...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val initAmount = initialBalanceStr.toDoubleOrNull() ?: 0.0
                        val finalBalance = if (customer != null) {
                            customer.balance
                        } else {
                            if (isDebt) Math.abs(initAmount) else -Math.abs(initAmount)
                        }

                        onSave(
                            Customer(
                                id = customer?.id ?: 0,
                                name = name.trim(),
                                phone = phone.trim(),
                                balance = finalBalance,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("حفظ البيانات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSave: (Tx) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDebit by remember { mutableStateOf(true) } // true = عليه (دين), false = له (سداد)

    val quickNotes = if (isDebit) {
        listOf("مشتريات بقالة", "حساب شهري", "أغراض منزلية", "رصيد شحن", "بضاعة آجلة")
    } else {
        listOf("سداد نقدي كاش", "دفعة من الحساب", "تحويل بنكي / كريمي", "سداد كامل الحساب", "خصم تسوية")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("تسجيل عملية جديدة", fontWeight = FontWeight.Bold)
                Text("للعميل: ${customer.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isDebit = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDebit) DebtRedLight else Color(0xFFF1F5F9),
                        border = if (isDebit) androidx.compose.foundation.BorderStroke(2.dp, DebtRed) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("عليه (أخذ بضاعة / دين)", fontWeight = FontWeight.Bold, color = if (isDebit) DebtRed else Color.Gray)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isDebit = false },
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isDebit) CreditGreenLight else Color(0xFFF1F5F9),
                        border = if (!isDebit) androidx.compose.foundation.BorderStroke(2.dp, CreditGreen) else null
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("له (دفع سداد / كاش)", fontWeight = FontWeight.Bold, color = if (!isDebit) CreditGreen else Color.Gray)
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ (ريال) *") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Amount Adders
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val adders = listOf(500, 1000, 2000, 5000, 10000)
                    items(adders) { addVal ->
                        SuggestionChip(
                            onClick = {
                                val cur = amountStr.toDoubleOrNull() ?: 0.0
                                amountStr = (cur + addVal).toInt().toString()
                            },
                            label = { Text("+$addVal") }
                        )
                    }
                }

                // Note Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("البيان / التفاصيل") },
                    placeholder = { Text("تفاصيل المشتريات أو طريقة الدفع...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Note Chips
                Text("اقتراحات سريعة للبيان:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickNotes) { qNote ->
                        SuggestionChip(
                            onClick = { note = qNote },
                            label = { Text(qNote) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                        val date = sdf.format(Date())

                        onSave(
                            Tx(
                                customerId = customer.id,
                                type = if (isDebit) TransactionType.DEBIT else TransactionType.CREDIT,
                                amount = amount,
                                currency = "ريال",
                                date = date,
                                note = note.ifBlank { if (isDebit) "أخذ بضاعة" else "سداد نقدي" }
                            )
                        )
                    }
                },
                enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDebit) DebtRed else CreditGreen
                )
            ) {
                Text(if (isDebit) "تسجيل الدين" else "تسجيل السداد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var buyPriceStr by remember { mutableStateOf(product?.buyPrice?.toString() ?: "") }
    var sellPriceStr by remember { mutableStateOf(product?.sellPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "10") }
    var minStockStr by remember { mutableStateOf(product?.minStock?.toString() ?: "5") }
    var notes by remember { mutableStateOf(product?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (product == null) "إضافة صنف للمخزون" else "تعديل الصنف",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف / المنتج *") },
                    placeholder = { Text("مثال: أرز بسمتي 10 كجم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("الباركود / الرمز") },
                    placeholder = { Text("أدخل أو امسح الباركود") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it },
                        label = { Text("سعر الشراء") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { sellPriceStr = it },
                        label = { Text("سعر البيع *") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("الكمية المتوفرة") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("حد التنبيه (نقص)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    placeholder = { Text("الشركة الموردة أو موقع الصنف...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val buyPrice = buyPriceStr.toDoubleOrNull() ?: 0.0
                        val sellPrice = sellPriceStr.toDoubleOrNull() ?: 0.0
                        val stock = stockStr.toIntOrNull() ?: 0
                        val minStock = minStockStr.toIntOrNull() ?: 5

                        onSave(
                            Product(
                                id = product?.id ?: 0,
                                name = name.trim(),
                                barcode = barcode.trim(),
                                buyPrice = buyPrice,
                                sellPrice = sellPrice,
                                stock = stock,
                                minStock = minStock,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("حفظ الصنف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ExpenseDialog(
    expense: Expense?,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var title by remember { mutableStateOf(expense?.title ?: "") }
    var amountStr by remember { mutableStateOf(expense?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(expense?.note ?: "") }

    val categories = listOf("كهرباء ومياه", "إيجار المحل", "أجور ونقل", "شراء أكياس ومطبوعات", "صيانة وإصلاحات", "نثريات وضيافة", "رواتب عمال")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DebtRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (expense == null) "تسجيل مصروف جديد" else "تعديل المصروف", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بند المصروف *") },
                    placeholder = { Text("مثال: فاتورة الكهرباء") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("تصنيفات شائعة:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        SuggestionChip(
                            onClick = { title = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ (ريال) *") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("تفاصيل أو ملاحظات") },
                    placeholder = { Text("ملاحظات إضافية...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val date = expense?.date ?: sdf.format(Date())

                        onSave(
                            Expense(
                                id = expense?.id ?: 0,
                                title = title.trim(),
                                amount = amount,
                                currency = "ريال",
                                date = date,
                                note = note.trim()
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = DebtRed)
            ) {
                Text("تسجيل المصروف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("نعم، احذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
