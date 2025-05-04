package com.example.kharcha

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.kharcha.ui.theme.KharchaTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var transactions = loadTransactionsFromFile(this)
        setContent {
            var transactionState by remember { mutableStateOf(TransactionState(transactions)) }
                KharchaTheme {
                MyApp(
                    transactionState = transactionState,
                    onTransactionStateChange = { updatedState ->
                        transactionState = updatedState
                        saveTransactionsToFile(this, updatedState.transactions) }
                )
            }
        }
    }
}

@Composable
fun MyApp(
    transactionState: TransactionState,
    onTransactionStateChange: (TransactionState) -> Unit
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "todays_transaction",
            Modifier.padding(innerPadding)
        ) {
            composable("todays_transaction") { TodaysTransactionScreen(navController, transactionState.transactions,onDeleteTransaction = { transaction ->
                onTransactionStateChange(transactionState.deleteTransaction(transaction))
            }) }
            composable("transaction_history") { TransactionHistoryScreen(transactionState.transactions) }
            composable("add_transaction") { AddTransaction(
                onAddTransaction = { details, amount, date ->
                    val newTransaction = TransactionsItem(
                        details = details,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = date
                    )
                    val updatedTransactions = transactionState.copy(
                        transactions = transactionState.transactions + newTransaction
                    )
                    onTransactionStateChange( updatedTransactions)
                    navController.navigateUp()
                }
            ) }
            composable("view_all_transactions"){AllTransactions(transactionState.transactions)}
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val items = listOf(
            "todays_transaction" to R.drawable.home_svgrepo_com,
            "transaction_history" to R.drawable.search_svgrepo_com,
            "view_all_transactions" to R.drawable.history_svgrepo_com
        )
        val currentDestination = navController.currentBackStackEntry?.destination
        items.forEach { (route,iconRes) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = route,
                        modifier = Modifier.size(30.dp), // Adjust size here
                        tint = Color.Black // Set color to black
                    )
                }, // No icon
                label = {Text("")},
                selected = currentDestination?.route == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun TodaysTransactionScreen(navController: NavController, transactions: List<TransactionsItem>, onDeleteTransaction: (TransactionsItem) -> Unit) {
    val todaysDate = getTodaysDate()
    Log.d("MyApp", "$todaysDate")
    val filteredTransactions = transactions.filter { transaction ->
        transaction.date == todaysDate
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier
            .fillMaxSize().padding(bottom = 60.dp)) {
            Text("Hello Nita Dahal",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp))
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Your Transactions Today",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp))
            Spacer(modifier = Modifier.padding(14.dp))
            Transactions(filteredTransactions)
        }
        if(filteredTransactions.isEmpty()){
            Text("No transactions today")
        }
        else{
            SubTotal(filteredTransactions)
        }

        // FAB that overlays the content
        FloatingActionButton(
            onClick = {
                navController.navigate("add_transaction")
            },
            modifier = Modifier
                .padding(30.dp)
                .padding(bottom = 30.dp)
                .align(Alignment.BottomEnd),
            containerColor = Color.Black
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White
            )
        }
    }
}
@Composable
fun SubTotal(transactions: List<TransactionsItem>){
    var totalAmount = transactions.sumOf { it.amount }
    Row(horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxSize().padding(10.dp)
    ){
        Text("SubTotal",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp)
        Text("₹$totalAmount",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp)
    }


}
@Composable
fun Transactions(transactions: List<TransactionsItem>) {
    LazyColumn(modifier = Modifier.fillMaxSize()
        .padding(8.dp)) {
        items(transactions) { transaction ->
            TransactionItem(transaction)
        }
    }
}

@Composable
fun TransactionItem(transactionsItem: TransactionsItem,
                    modifier: Modifier = Modifier){
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Transaction Icon or Category indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFFF5F5F5), // Light gray background
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transactionsItem.details.first().toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction details and date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transactionsItem.details,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Black
                    )
                )
                
            }

            // Amount
            Text(
                text = "₹${transactionsItem.amount}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

}

@Composable
fun TransactionHistoryScreen(transactions: List<TransactionsItem>) {
    var day by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var filteredTransactions by remember { mutableStateOf<List<TransactionsItem>>(emptyList()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Filter Transactions by Date",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Input fields for day, month, and year
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = day,
                onValueChange = { day = it },
                label = { Text("Day") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = month,
                onValueChange = { month = it },
                label = { Text("Month") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year") },
                modifier = Modifier.weight(1f)
            )
        }

        // Filter Button
        Button(
            onClick = {
                keyboardController?.hide()
                filteredTransactions = transactions.filter { transaction ->
                    transaction.date == "$day$month$year"
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Filter")
        }


        // Display Filtered Transactions
        if (filteredTransactions.isEmpty()) {
            Text(
                text = "No transactions found for the selected date.",
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 14.sp
            )
        } else {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp)
                    Text("Amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp)
                }
                LazyColumn(modifier = Modifier.padding(2.dp)) {
                    items(filteredTransactions) { transaction ->
                        TransactionItem(transactionsItem = transaction)
                    }
                }

            }
            SubTotal(filteredTransactions)
        }
    }

}

@Composable
fun AddTransaction(onAddTransaction: (String, String, String) -> Unit){
    var details by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Add Transaction",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Transaction Details") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = hasError && details.isBlank()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount in Rs") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            isError = hasError && amount.isBlank()
        )

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (DD/MM/YYYY)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            isError = hasError && date.isBlank()
        )

        Button(
            onClick = {
                hasError = details.isBlank() || amount.isBlank() || date.isBlank()
                if (!hasError) {
                    onAddTransaction(details, amount, date)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Add Transaction")
        }

        if (hasError) {
            Text(
                "Please fill in all fields",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

}

@Composable
fun AllTransactions(transactions: List<TransactionsItem>){
    Column(modifier = Modifier.fillMaxSize().padding(bottom = 60.dp) ) {
        Text("All Transactions",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(15.dp))
        Spacer(modifier = Modifier.height(30.dp))
        TransactionsWithDate(transactions)
    }
    SubTotal(transactions)


}

@Composable
fun TransactionsItemWithDate(
    transactionsItem: TransactionsItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Transaction Icon or Category indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color(0xFFF5F5F5), // Light gray background
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transactionsItem.details.first().toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction details and date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transactionsItem.details,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Black
                    )
                )
                Text(
                    text = formatTransactionDate(transactionsItem.date),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray
                    )
                )
            }

            // Amount
            Text(
                text = "₹${transactionsItem.amount}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun TransactionsWithDate(transactions: List<TransactionsItem>){
    LazyColumn(modifier = Modifier.fillMaxSize()
        .padding(8.dp)) {
        items(transactions) { transaction ->
            TransactionsItemWithDate(transaction)
        }
    }
}


