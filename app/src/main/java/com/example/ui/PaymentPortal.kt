package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.data.Course
import com.example.data.Payment
import com.example.data.UserAccount
import java.text.SimpleDateFormat
import java.util.*

// Payment-specific Style Constants (other main theme colors are inherited from MainScreen.kt)
val SoftGreen = Color(230, 245, 235)
val SoftRed = Color(253, 237, 237)
val AlertRed = Color(211, 47, 47)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentPortalComponent(
    viewModel: LmsViewModel,
    currentUser: UserAccount?,
    courses: List<Course>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val dynamicPrimaryHex by viewModel.primaryColor.collectAsStateWithLifecycle()
    val dynamicGoldHex by viewModel.brandGoldColor.collectAsStateWithLifecycle()
    
    val dynamicPrimary = parseHexColor(dynamicPrimaryHex, PrimaryGreen)
    val dynamicGold = parseHexColor(dynamicGoldHex, Color(200, 155, 60))

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Pay Now / Purchase, 2: History & Receipts

    // Selected items for checkout
    var selectedInvoiceForPayment by remember { mutableStateOf<Payment?>(null) }
    var selectedCourseForPurchase by remember { mutableStateOf<Course?>(null) }
    
    // Receipt or Invoice viewer state
    var viewInvoiceDetails by remember { mutableStateOf<Payment?>(null) }
    var viewReceiptDetails by remember { mutableStateOf<Payment?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandCream),
            color = BrandCream
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.lms_launcher_fg),
                                    contentDescription = "Rooted & Ready Official Logo",
                                    modifier = Modifier
                                        .width(88.dp)
                                        .height(30.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, dynamicGold.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SECURE TUITION & BILLING PORTAL", fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 1.sp)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, "Close Portal")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BrandWhite)
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = BrandWhite,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = activeTab == 0 && selectedInvoiceForPayment == null && selectedCourseForPurchase == null,
                            onClick = { 
                                activeTab = 0
                                selectedInvoiceForPayment = null
                                selectedCourseForPurchase = null
                            },
                            icon = { Icon(Icons.Default.Dashboard, "My Ledger") },
                            label = { Text("My Ledger", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = activeTab == 1 || selectedInvoiceForPayment != null || selectedCourseForPurchase != null,
                            onClick = { activeTab = 1 },
                            icon = { Icon(Icons.Default.AddCard, "Pay Tuition") },
                            label = { Text("Pay Tuition", fontSize = 11.sp) }
                        )
                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { 
                                activeTab = 2
                                selectedInvoiceForPayment = null
                                selectedCourseForPurchase = null
                            },
                            icon = { Icon(Icons.Outlined.ReceiptLong, "Invoices & Receipts") },
                            label = { Text("Tax Receipts", fontSize = 11.sp) }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    if (selectedInvoiceForPayment != null) {
                        PaymentCheckoutScreen(
                            viewModel = viewModel,
                            payment = selectedInvoiceForPayment!!,
                            course = courses.find { it.id == selectedInvoiceForPayment!!.courseId },
                            currentUser = currentUser,
                            dynamicPrimary = dynamicPrimary,
                            dynamicGold = dynamicGold,
                            onPaymentSuccess = {
                                selectedInvoiceForPayment = null
                                activeTab = 0
                            },
                            onCancel = { selectedInvoiceForPayment = null }
                        )
                    } else if (selectedCourseForPurchase != null) {
                        // Generate a temporary invoice for course purchase
                        val courseFee = 4500.00 // Flat tuition fee per course
                        val tempPayment = Payment(
                            clientName = currentUser?.name ?: "Guest Learner",
                            amount = courseFee,
                            status = "Pending",
                            paymentType = "Card",
                            reference = "INV-PUR-${System.currentTimeMillis() % 10000}",
                            userEmail = currentUser?.email ?: "",
                            courseId = selectedCourseForPurchase!!.id,
                            dueDate = System.currentTimeMillis() + 14 * 24 * 3600 * 1000L
                        )
                        PaymentCheckoutScreen(
                            viewModel = viewModel,
                            payment = tempPayment,
                            course = selectedCourseForPurchase,
                            currentUser = currentUser,
                            dynamicPrimary = dynamicPrimary,
                            dynamicGold = dynamicGold,
                            onPaymentSuccess = {
                                selectedCourseForPurchase = null
                                activeTab = 0
                            },
                            onCancel = { selectedCourseForPurchase = null }
                        )
                    } else {
                        when (activeTab) {
                            0 -> LedgerDashboardTab(
                                viewModel = viewModel,
                                payments = payments,
                                currentUser = currentUser,
                                courses = courses,
                                dynamicPrimary = dynamicPrimary,
                                dynamicGold = dynamicGold,
                                onSelectPayment = { selectedInvoiceForPayment = it },
                                onViewInvoice = { viewInvoiceDetails = it },
                                onViewReceipt = { viewReceiptDetails = it }
                            )
                            1 -> TuitionPurchaseTab(
                                courses = courses,
                                payments = payments,
                                currentUser = currentUser,
                                dynamicPrimary = dynamicPrimary,
                                dynamicGold = dynamicGold,
                                onPurchaseCourse = { selectedCourseForPurchase = it }
                            )
                            2 -> ReceiptsAndInvoicesTab(
                                payments = payments,
                                currentUser = currentUser,
                                dynamicPrimary = dynamicPrimary,
                                dynamicGold = dynamicGold,
                                onViewInvoice = { viewInvoiceDetails = it },
                                onViewReceipt = { viewReceiptDetails = it }
                            )
                        }
                    }

                    // Invoice Details Dialog
                    if (viewInvoiceDetails != null) {
                        InvoiceViewerDialog(
                            payment = viewInvoiceDetails!!,
                            course = courses.find { it.id == viewInvoiceDetails!!.courseId },
                            dynamicPrimary = dynamicPrimary,
                            dynamicGold = dynamicGold,
                            onDismiss = { viewInvoiceDetails = null }
                        )
                    }

                    // Receipt Details Dialog
                    if (viewReceiptDetails != null) {
                        ReceiptViewerDialog(
                            payment = viewReceiptDetails!!,
                            course = courses.find { it.id == viewReceiptDetails!!.courseId },
                            dynamicPrimary = dynamicPrimary,
                            dynamicGold = dynamicGold,
                            onDismiss = { viewReceiptDetails = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerDashboardTab(
    viewModel: LmsViewModel,
    payments: List<Payment>,
    currentUser: UserAccount?,
    courses: List<Course>,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onSelectPayment: (Payment) -> Unit,
    onViewInvoice: (Payment) -> Unit,
    onViewReceipt: (Payment) -> Unit
) {
    val context = LocalContext.current
    val email = currentUser?.email ?: ""
    val isCorporate = currentUser?.role == "Corporate Client"
    
    // Filter payments belonging to current user or their company
    val filteredPayments = payments.filter {
        if (isCorporate) {
            it.clientName.lowercase().trim() == currentUser?.company?.lowercase()?.trim() && 
            it.paymentType == "Corporate Invoice"
        } else {
            it.userEmail.lowercase().trim() == email.lowercase().trim()
        }
    }

    val outstandingInvoices = filteredPayments.filter { it.status != "Paid" }
    val paidPayments = filteredPayments.filter { it.status == "Paid" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Status Banner
        item {
            val isSuspended = currentUser?.status == "Suspended"
            val totalOutstanding = outstandingInvoices.sumOf { it.amount }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuspended) SoftRed else SoftGreen
                ),
                border = BorderStroke(
                    1.dp, 
                    if (isSuspended) AlertRed.copy(alpha = 0.5f) else PrimaryGreen.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSuspended) AlertRed.copy(alpha = 0.1f) else PrimaryGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSuspended) Icons.Default.Lock else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isSuspended) AlertRed else PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSuspended) "Account Access Suspended" else "Account in Good Standing",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = if (isSuspended) AlertRed else PrimaryGreen
                        )
                        Text(
                            text = if (isSuspended) {
                                "Access locked due to outstanding overdue balances of R${"%,.2f".format(totalOutstanding)}."
                            } else if (totalOutstanding > 0) {
                                "You have R${"%,.2f".format(totalOutstanding)} in pending tuition invoices."
                            } else {
                                "All payments verified. Keep rocking your learning journey!"
                            },
                            fontSize = 11.sp,
                            color = DarkCharcoal
                        )
                    }
                }
            }
        }

        // Summary Statistics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL PAID", fontSize = 10.sp, color = MutedCharcoal, fontWeight = FontWeight.Bold)
                        Text("R${"%,.2f".format(paidPayments.sumOf { it.amount })}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = PrimaryGreen)
                    }
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("OUTSTANDING", fontSize = 10.sp, color = MutedCharcoal, fontWeight = FontWeight.Bold)
                        Text("R${"%,.2f".format(outstandingInvoices.sumOf { it.amount })}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AlertRed)
                    }
                }
            }
        }

        // Section: Outstanding Invoices
        item {
            Text("Outstanding Tuition & Invoices", fontWeight = FontWeight.Black, fontSize = 14.sp, color = DarkCharcoal)
        }

        if (outstandingInvoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, "No pending invoices", tint = PrimaryGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No outstanding invoices found", fontWeight = FontWeight.Bold, color = MutedCharcoal, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(outstandingInvoices) { pay ->
                InvoiceItemCard(
                    payment = pay,
                    course = courses.find { it.id == pay.courseId },
                    dynamicPrimary = dynamicPrimary,
                    dynamicGold = dynamicGold,
                    onPayNow = { onSelectPayment(pay) },
                    onViewInvoice = { onViewInvoice(pay) }
                )
            }
        }

        // Section: Past History
        item {
            Text("Transaction History", fontWeight = FontWeight.Black, fontSize = 14.sp, color = DarkCharcoal)
        }

        if (paidPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No payment records found", fontWeight = FontWeight.Bold, color = MutedCharcoal, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(paidPayments) { pay ->
                PaidItemCard(
                    payment = pay,
                    course = courses.find { it.id == pay.courseId },
                    onViewReceipt = { onViewReceipt(pay) }
                )
            }
        }
    }
}

@Composable
fun TuitionPurchaseTab(
    courses: List<Course>,
    payments: List<Payment>,
    currentUser: UserAccount?,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onPurchaseCourse: (Course) -> Unit
) {
    // List courses that the user hasn't paid for yet
    val email = currentUser?.email ?: ""
    val userPaidCourseIds = payments
        .filter { it.userEmail.lowercase().trim() == email.lowercase().trim() && it.status == "Paid" }
        .map { it.courseId }
        .toSet()

    // Filter out corporate academy courses if user is a standard student (or only show appropriate ones)
    val availableCourses = courses.filter { 
        it.category != "Corporate Academy" && !userPaidCourseIds.contains(it.id)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Available Courses & Curriculum Library", fontWeight = FontWeight.Black, fontSize = 15.sp, color = DarkCharcoal)
            Text("Select a course to enroll, configure tuition structures, and unlock study sessions.", fontSize = 11.sp, color = MutedCharcoal)
        }

        if (availableCourses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You are fully paid and enrolled in all syllabus modules!", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(availableCourses) { course ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite),
                    border = BorderStroke(1.dp, parseHexColor(course.colorHex).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(parseHexColor(course.colorHex)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${course.code}: ${course.name}", fontWeight = FontWeight.Black, color = parseHexColor(course.colorHex))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(course.description, fontSize = 11.sp, color = MutedCharcoal)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TUITION PRICE", fontSize = 9.sp, color = MutedCharcoal, fontWeight = FontWeight.Bold)
                                Text("R4,500.00", fontWeight = FontWeight.Black, fontSize = 15.sp, color = DarkCharcoal)
                                Text("Or R375.00/mo instalments", fontSize = 10.sp, color = dynamicGold, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onPurchaseCourse(course) },
                                colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Enroll & Pay", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptsAndInvoicesTab(
    payments: List<Payment>,
    currentUser: UserAccount?,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onViewInvoice: (Payment) -> Unit,
    onViewReceipt: (Payment) -> Unit
) {
    val email = currentUser?.email ?: ""
    val isCorporate = currentUser?.role == "Corporate Client"
    
    val userPayments = payments.filter {
        if (isCorporate) {
            it.clientName.lowercase().trim() == currentUser?.company?.lowercase()?.trim() && 
            it.paymentType == "Corporate Invoice"
        } else {
            it.userEmail.lowercase().trim() == email.lowercase().trim()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Tax Invoices & Receipts Ledger", fontWeight = FontWeight.Black, fontSize = 15.sp, color = DarkCharcoal)
            Text("Access or view automatically generated tax documents for accounting and corporate compliance audits.", fontSize = 11.sp, color = MutedCharcoal)
        }

        if (userPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No billing documents available.", fontWeight = FontWeight.Bold, color = MutedCharcoal, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(userPayments) { pay ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandWhite)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (pay.status == "Paid") Icons.Default.Description else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (pay.status == "Paid") PrimaryGreen else dynamicGold,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(pay.reference.ifEmpty { "INV-${pay.id + 2026}" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Date: ${formatPaymentDate(pay.date)} • R${"%,.2f".format(pay.amount)}", fontSize = 11.sp, color = MutedCharcoal)
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (pay.hasInvoice) {
                                OutlinedButton(
                                    onClick = { onViewInvoice(pay) },
                                    border = BorderStroke(1.dp, dynamicPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("Invoice", fontSize = 11.sp, color = dynamicPrimary)
                                }
                            }
                            if (pay.hasReceipt && pay.status == "Paid") {
                                Button(
                                    onClick = { onViewReceipt(pay) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text("Receipt", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceItemCard(
    payment: Payment,
    course: Course?,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onPayNow: () -> Unit,
    onViewInvoice: () -> Unit
) {
    val isOverdue = payment.status == "Overdue" || (payment.dueDate < System.currentTimeMillis() && payment.status != "Paid")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrandWhite),
        border = BorderStroke(1.dp, if (isOverdue) AlertRed.copy(alpha = 0.3f) else Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.reference.ifEmpty { "INV-2026-${payment.id}" },
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = DarkCharcoal
                    )
                    Text(
                        text = if (course != null) "Course: ${course.name}" else "General Registration Tuition",
                        fontSize = 11.sp,
                        color = MutedCharcoal
                    )
                }
                Badge(
                    containerColor = if (isOverdue) AlertRed else dynamicGold,
                    contentColor = Color.White
                ) {
                    Text(if (isOverdue) "Overdue" else payment.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("AMOUNT DUE", fontSize = 9.sp, color = MutedCharcoal, fontWeight = FontWeight.Bold)
                    Text("R${"%,.2f".format(payment.amount)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = DarkCharcoal)
                    Text("Due Date: ${formatPaymentDate(payment.dueDate)}", fontSize = 10.sp, color = if (isOverdue) AlertRed else MutedCharcoal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewInvoice,
                        border = BorderStroke(1.dp, dynamicPrimary)
                    ) {
                        Text("View Invoice", fontSize = 11.sp, color = dynamicPrimary)
                    }
                    if (payment.status != "Pending Verification") {
                        Button(
                            onClick = onPayNow,
                            colors = ButtonDefaults.buttonColors(containerColor = dynamicGold)
                        ) {
                            Text("Settle Now", fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                        ) {
                            Text("Processing...", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaidItemCard(
    payment: Payment,
    course: Course?,
    onViewReceipt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrandWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SoftGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = PrimaryGreen)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(payment.reference.ifEmpty { "INV-2026-${payment.id}" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        text = if (course != null) "Unlocked Course: ${course.name}" else "General Tuition Fees",
                        fontSize = 11.sp,
                        color = MutedCharcoal
                    )
                    Text("Paid: R${"%,.2f".format(payment.amount)} via ${payment.paymentType}", fontSize = 10.sp, color = MutedCharcoal)
                }
            }
            Button(
                onClick = onViewReceipt,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Description, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Receipt", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PaymentCheckoutScreen(
    viewModel: LmsViewModel,
    payment: Payment,
    course: Course?,
    currentUser: UserAccount?,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onPaymentSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var paymentMethod by remember { mutableStateOf("Card") } // "Card", "Manual EFT", "Monthly Instalment"

    // Card Input States
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf(currentUser?.name ?: "") }
    
    // EFT Input States
    var eftBankName by remember { mutableStateOf("") }
    var eftRefNumber by remember { mutableStateOf("") }
    
    // Monthly Instalment states
    var selectedPlanMonths by remember { mutableIntStateOf(3) } // 3, 6, 12 months

    var isProcessing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tuition Checkout Portal", fontWeight = FontWeight.Black, fontSize = 18.sp, color = DarkCharcoal)
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "Cancel")
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandWhite),
                border = BorderStroke(1.dp, dynamicGold.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TRANSACTION SUMMARY", fontSize = 10.sp, color = MutedCharcoal, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (course != null) "Course Enrollment: ${course.name}" else "Tuition Balance Settlement",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = DarkCharcoal
                    )
                    Text("Ref Code: ${payment.reference}", fontSize = 11.sp, color = MutedCharcoal)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Tuition Fees:", fontWeight = FontWeight.Bold)
                        Text("R${"%,.2f".format(payment.amount)}", fontWeight = FontWeight.Black, color = dynamicPrimary)
                    }
                }
            }
        }

        // Select Payment Type Chip row
        item {
            Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Card", "Manual EFT", "Monthly Instalment").forEach { method ->
                    ElevatedFilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method) },
                        modifier = Modifier.testTag("chip_method_$method")
                    )
                }
            }
        }

        // Display Inputs Based on Payment Type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandWhite)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (paymentMethod == "Card") {
                        Text("Secure Card Transaction", fontWeight = FontWeight.Bold, color = dynamicPrimary)
                        Text("Instant verification. VISA, MasterCard, and AMEX supported.", fontSize = 11.sp, color = MutedCharcoal)
                        
                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("Cardholder Full Name") },
                            modifier = Modifier.fillMaxWidth().testTag("holder_name_input")
                        )
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it.take(19) },
                            label = { Text("Card Number (16-digits)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("card_number_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = { cardExpiry = it.take(5) },
                                label = { Text("Expiry (MM/YY)") },
                                modifier = Modifier.weight(1f).testTag("expiry_input")
                            )
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = { cardCvv = it.take(3) },
                                label = { Text("CVV") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("cvv_input")
                            )
                        }
                    } else if (paymentMethod == "Manual EFT") {
                        Text("Manual EFT / Bank Wire", fontWeight = FontWeight.Bold, color = dynamicPrimary)
                        Text("Upload payment details for administrative validation. Courses will unlock once confirmed.", fontSize = 11.sp, color = MutedCharcoal)
                        
                        Card(colors = CardDefaults.cardColors(containerColor = BrandCream), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("BENEFICIARY BANK DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                                Text("Bank: Rooted Ready Academy Trust Bank", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Account: 9876543210 (Cheque / Current)", fontSize = 11.sp)
                                Text("Branch Code: 250655", fontSize = 11.sp)
                                Text("Reference: ${payment.reference}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = dynamicPrimary)
                            }
                        }

                        OutlinedTextField(
                            value = eftBankName,
                            onValueChange = { eftBankName = it },
                            label = { Text("Your Issuing Bank Name") },
                            modifier = Modifier.fillMaxWidth().testTag("eft_bank_input")
                        )
                        OutlinedTextField(
                            value = eftRefNumber,
                            onValueChange = { eftRefNumber = it },
                            label = { Text("EFT Proof reference number / UUID") },
                            modifier = Modifier.fillMaxWidth().testTag("eft_ref_input")
                        )
                    } else {
                        // Monthly Instalments Plan Selection
                        Text("Tuition Instalment Splitting Plan", fontWeight = FontWeight.Bold, color = dynamicPrimary)
                        Text("Choose an instalment plan to spread your course fees across monthly blocks. Suspensions trigger if instalments expire.", fontSize = 11.sp, color = MutedCharcoal)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(3, 6, 12).forEach { months ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                        .clickable { selectedPlanMonths = months },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedPlanMonths == months) SoftGreen else BrandCream
                                    ),
                                    border = BorderStroke(1.dp, if (selectedPlanMonths == months) PrimaryGreen else Color.LightGray)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$months Months", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("R${"%,.2f".format(payment.amount / months)}/mo", fontSize = 10.sp, color = PrimaryGreen, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("You will pay R${"%,.2f".format(payment.amount / selectedPlanMonths)} today to authorize your study syllabus access.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dynamicGold)
                    }
                }
            }
        }

        // Action button to Process Transaction
        item {
            if (isProcessing) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = dynamicPrimary)
                }
            } else {
                Button(
                    onClick = {
                        isProcessing = true
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(1200) // Simulate secure gateway ping
                            
                            val notes = when (paymentMethod) {
                                "Card" -> "Paid securely with card ending *${cardNumber.takeLast(4).ifEmpty { "1234" }}"
                                "Manual EFT" -> "EFT Ref: $eftRefNumber from Bank: $eftBankName"
                                else -> "Instalments initialized: R${"%,.2f".format(payment.amount / selectedPlanMonths)}/month over $selectedPlanMonths months"
                            }

                            if (payment.id > 0) {
                                // This is an existing invoice we are settling
                                val finalStatus = if (paymentMethod == "Manual EFT") "Pending Verification" else "Paid"
                                viewModel.updatePaymentStatus(payment, finalStatus)
                            } else {
                                // This is a new course registration purchase
                                viewModel.registerPurchase(
                                    userEmail = currentUser?.email ?: "",
                                    userName = currentUser?.name ?: "Guest Learner",
                                    courseId = payment.courseId,
                                    courseName = course?.name ?: "",
                                    amount = if (paymentMethod == "Monthly Instalment") (payment.amount / selectedPlanMonths) else payment.amount,
                                    paymentType = paymentMethod,
                                    reference = payment.reference,
                                    notes = notes
                                )
                            }
                            
                            isProcessing = false
                            Toast.makeText(
                                context, 
                                if (paymentMethod == "Manual EFT") "EFT proof submitted. Pending admin review! 🏦" else "Tuition Fees Authenticated Successfully! 🎓", 
                                Toast.LENGTH_LONG
                            ).show()
                            onPaymentSuccess()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_complete_checkout")
                ) {
                    Text(
                        text = when (paymentMethod) {
                            "Card" -> "Authorize Secure Payment (R${"%,.2f".format(payment.amount)})"
                            "Manual EFT" -> "Submit Proof of EFT"
                            else -> "Confirm & Pay Instalment 1 (R${"%,.2f".format(payment.amount / selectedPlanMonths)})"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceViewerDialog(
    payment: Payment,
    course: Course?,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandWhite),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, dynamicGold.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TAX INVOICE", fontWeight = FontWeight.Black, fontSize = 16.sp, color = dynamicPrimary)
                        Text(payment.reference.ifEmpty { "INV-2026-${payment.id}" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MutedCharcoal)
                    }
                    Icon(Icons.Default.School, null, tint = dynamicGold, modifier = Modifier.size(36.dp))
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Details
                Text("BILL TO:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedCharcoal)
                Text(payment.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (payment.userEmail.isNotEmpty()) {
                    Text(payment.userEmail, fontSize = 11.sp, color = MutedCharcoal)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Product Details
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Total Price", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = if (course != null) "Course Tuition: ${course.name}" else "Syllabus Academy Tuition Fee",
                        fontSize = 11.sp,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text("R${"%,.2f".format(payment.amount)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.3f), textAlign = TextAlign.End)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Totals
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal:", fontSize = 11.sp)
                    Text("R${"%,.2f".format(payment.amount * 0.85)}", fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("VAT (15%):", fontSize = 11.sp)
                    Text("R${"%,.2f".format(payment.amount * 0.15)}", fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount Due:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("R${"%,.2f".format(payment.amount)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = dynamicPrimary)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Document")
                }
            }
        }
    }
}

@Composable
fun ReceiptViewerDialog(
    payment: Payment,
    course: Course?,
    dynamicPrimary: Color,
    dynamicGold: Color,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandWhite),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, PrimaryGreen.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header with PAID Stamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PAYMENT RECEIPT", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryGreen)
                        Text("REF: REC-${payment.id + 1000}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MutedCharcoal)
                    }
                    
                    // Paid stamp style
                    Box(
                        modifier = Modifier
                            .border(2.dp, PrimaryGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("PAID", color = PrimaryGreen, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Text("RECEIVED FROM:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedCharcoal)
                Text(payment.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (payment.userEmail.isNotEmpty()) {
                    Text(payment.userEmail, fontSize = 11.sp, color = MutedCharcoal)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(colors = CardDefaults.cardColors(containerColor = SoftGreen), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PAYMENT DETAILS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text("Transaction Date: ${formatPaymentDate(payment.date)}", fontSize = 11.sp)
                        Text("Payment Method: ${payment.paymentType}", fontSize = 11.sp)
                        Text("Amount Cleared: R${"%,.2f".format(payment.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = PrimaryGreen)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("ACADEMIC ITEM:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MutedCharcoal)
                Text(
                    text = if (course != null) "Course Enrollment: ${course.name}" else "Full Syllabus Masterclass Tuition",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "RECEIPT DOWNLOADED: PDF generated successfully! 📂", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, dynamicPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun formatPaymentDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(date)
}


