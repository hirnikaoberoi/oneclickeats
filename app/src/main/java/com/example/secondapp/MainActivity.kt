package com.example.secondapp

//import com.example.secondapp.ui.theme.SecondAppTheme
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.secondapp.utils.LogoutHelper.logoutUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 Request Notification Permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(POST_NOTIFICATIONS), 101)
            }
        }
        // Get the current user from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainScreen(userId = userId)
            }
        }
    }
}

@Composable
fun MainScreen(userId: String) {
    var currentScreen by remember { mutableStateOf("Home") }
    var selectedRestaurant by remember { mutableStateOf<Canteen?>(null) }
    val context = LocalContext.current
    BackHandler(enabled = true) {
        if (currentScreen != "Home") {
            currentScreen = "Home"
        }
    }
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }
    when (currentScreen) {
        "Home" -> HomePage(
            navigateToRestaurant = { canteen -> // Pass Canteen object instead of String
                selectedRestaurant = canteen
                currentScreen = "Restaurant"
            },
            navigateToOrders = { currentScreen = "Orders" },
            navigateToCart = { currentScreen = "Cart" },
            navigateToFeedbacks = { currentScreen = "Feedbacks" }

        )
        "Orders" -> OrdersScreen(
            userId = userId,
            navigateBack = { currentScreen = "Home" }
        )
        "Cart" -> CartScreen(
            userId = userId, // Replace with actual user ID from authentication
            navigateBack = { currentScreen = "Home" }
        )
        "Restaurant" -> {
            // Ensure the selected restaurant is not null before navigating
            selectedRestaurant?.let { canteen ->
                RestaurantScreen(
                    canteen = canteen, // Pass the Canteen object
                    userId = userId,
                    navigateBack = { currentScreen = "Home" }
                )
            }
        }

        "Feedbacks" -> FeedbackAnalysisScreen(
            userId = userId,
            navigateBack = { currentScreen = "Home" }
        )

    }
}
//enhanced



@Composable
fun HomePage(
    navigateToRestaurant: (Canteen) -> Unit,
    navigateToOrders: () -> Unit,
    navigateToCart: () -> Unit,
    navigateToFeedbacks: () -> Unit
) {
    val canteens = listOf(
        Canteen("Mukteshwari", R.drawable.mukteshwari, "4.5","Res1#A"),
        Canteen("Fun N Frolics", R.drawable.funnfrolics, "4.2", "Res2#B"),
        Canteen("Bella Bites", R.drawable.bellabites, "4.3","Res3#C"),
        Canteen("Annapurna", R.drawable.annapurna, "3.9","Res4#D"),
        Canteen("Shanu's", R.drawable.shanus, "4.2","Res5#E")

    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFBF5),
                        Color(0xFFFFFBF5)
                        //Color( 0xFF8B0000 ),
                        //Color( 0xFF8B0000 )

                    )
                )
            )
    ) {
        LazyColumn( // Single LazyColumn for everything
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp) // Prevent overlap with footer
        ) {
            // Header Section
            item {
                HeaderSection()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Restaurants Title
            item {
                Text(
                    text = "Restaurants",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,

                    color = Color(0xFF27202F),
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }

            items(canteens) { canteen ->
                CanteenCard(
                    canteen = canteen,
                    navigateToRestaurant = { navigateToRestaurant(canteen)}
                )
            }
            item {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Top Picked Items Section
//            item {
//                TopPickedItemsSection()
//            }
        }
        FooterSection(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            navigateToHome = { /* Already in Home */ },
            navigateToOrders = navigateToOrders,
            navigateToCart = navigateToCart,
            navigateToFeedbacks = navigateToFeedbacks
        )

    }
}
@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToOrders: () -> Unit,
    navigateToCart: () -> Unit,
    navigateToFeedbacks: () -> Unit, // <-- add this
) {
    Row(
        modifier = modifier
            .background(Color(0xFFFFFBF5))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FooterIcon(label = "Home", icon = Icons.Default.Home, onClick = navigateToHome)
        FooterIcon(label = "Orders", icon = Icons.Default.List, onClick = navigateToOrders)
        FooterIcon(label = "Cart", icon = Icons.Default.ShoppingCart, onClick = navigateToCart)
        FooterIcon(label = "Review", icon = Icons.Default.BarChart, onClick = navigateToFeedbacks) // 🎯 new
    }
}
@Composable
fun FooterIcon(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(imageVector = icon, contentDescription = label,tint =  Color(0xFF000000))
        Text(text = label, fontSize = 12.sp,color =Color(0xFF000000))

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackAnalysisScreen(
    userId: String,
    navigateBack: () -> Unit
) {
    val feedbacks = remember { mutableStateOf<List<Feedback>>(emptyList()) }
    val filteredFeedbacks = remember { mutableStateOf<List<Feedback>>(emptyList()) }
    val restaurantStats = remember { mutableStateOf<List<RestaurantStats>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val filterExpanded = remember { mutableStateOf(false) }
    val selectedSentiment = remember { mutableStateOf("All") }
    val selectedRestaurant = remember { mutableStateOf("All") }
    val sortOrder = remember { mutableStateOf("Newest First") }
    val availableRestaurants = remember { mutableStateOf<List<String>>(emptyList()) }

    // Cache for order details to avoid repeated queries
    val orderDetailsCache = remember { mutableStateOf<Map<String, Pair<String, String>>>(emptyMap()) }

    // Cache for restaurant and user data
    val restaurantCache = remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val userCache = remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Debug state
    val debugMessage = remember { mutableStateOf("") }

    // Function to categorize sentiment
    fun categorizeSentiment(sentiment: String): String {
        return when (sentiment.lowercase()) {
            "strong positive", "mild positive" -> "positive"
            "strong negative", "mild negative" -> "negative"
            else -> "neutral"
        }
    }

    // Function to apply filters
    fun applyFilters(feedbackList: List<Feedback>) {
        var filtered = feedbackList

        // Filter by sentiment
        if (selectedSentiment.value != "All") {
            filtered = filtered.filter {
                categorizeSentiment(it.sentiment).equals(selectedSentiment.value, ignoreCase = true)
            }
        }

        // Filter by restaurant
        if (selectedRestaurant.value != "All") {
            filtered = filtered.filter { feedback ->
                val (restaurantName, _) = orderDetailsCache.value[feedback.orderId] ?: Pair("", "")
                restaurantName == selectedRestaurant.value
            }
        }

        // Apply sorting
        filtered = when (sortOrder.value) {
            "Newest First" -> filtered.sortedByDescending { it.timestamp.toDate() }
            "Oldest First" -> filtered.sortedBy { it.timestamp.toDate() }
            "Highest Rating" -> filtered.sortedByDescending { it.rating }
            "Lowest Rating" -> filtered.sortedBy { it.rating }
            else -> filtered
        }

        filteredFeedbacks.value = filtered
    }

    // Optimized data fetching function
    suspend fun fetchFeedbackData(): List<Feedback> {
        val db = FirebaseFirestore.getInstance()

        try {
            debugMessage.value = "Fetching feedbacks..."

            // 1. Get all feedbacks first
            val feedbackSnapshot = db.collection("feedbacks")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val tempFeedbacks = feedbackSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Feedback::class.java)?.apply {
                    if (this.feedbackId.isEmpty()) {
                        this.feedbackId = doc.id
                    }
                }
            }

            debugMessage.value = "Found ${tempFeedbacks.size} feedbacks. Fetching order details..."

            // 2. Get unique order IDs to minimize queries
            val orderIds = tempFeedbacks.map { it.orderId }.distinct()

            // 3. Batch fetch orders - Use 'in' query for better performance
            val orderDetailsMap = mutableMapOf<String, Pair<String, String>>()
            val restaurantIds = mutableSetOf<String>()
            val userIds = mutableSetOf<String>()

            // Process orders in batches of 10 (Firestore 'in' query limit)
            orderIds.chunked(10).forEach { orderBatch ->
                val orderQuery = db.collection("orders")
                    .whereIn("orderId", orderBatch)
                    .get()
                    .await()

                orderQuery.documents.forEach { orderDoc ->
                    val orderId = orderDoc.getString("orderId") ?: ""
                    val restaurantId = orderDoc.getString("restId") ?: ""
                    val customerUserId = orderDoc.getString("userId") ?: ""

                    if (orderId.isNotEmpty() && restaurantId.isNotEmpty() && customerUserId.isNotEmpty()) {
                        // Store the IDs for batch fetching
                        restaurantIds.add(restaurantId)
                        userIds.add(customerUserId)

                        // Temporarily store with IDs, we'll replace with names later
                        orderDetailsMap[orderId] = Pair(restaurantId, customerUserId)
                    }
                }
            }

            debugMessage.value = "Fetched ${orderDetailsMap.size} orders. Getting restaurant and user data..."

            // 4. Batch fetch restaurant data
            val restaurantNames = mutableMapOf<String, String>()
            if (restaurantIds.isNotEmpty()) {
                // Use document references for better performance
                val restaurantTasks = restaurantIds.map { restaurantId ->
                    db.collection("Restaurant").document(restaurantId).get()
                }

                val restaurantResults = Tasks.whenAllSuccess<DocumentSnapshot>(restaurantTasks).await()
                restaurantResults.forEach { doc ->
                    if (doc.exists()) {
                        restaurantNames[doc.id] = doc.getString("name") ?: "Unknown Restaurant"
                    }
                }
            }

            // 5. Batch fetch user data
            val userNames = mutableMapOf<String, String>()
            if (userIds.isNotEmpty()) {
                val userTasks = userIds.map { userId ->
                    db.collection("users").document(userId).get()
                }

                val userResults = Tasks.whenAllSuccess<DocumentSnapshot>(userTasks).await()
                userResults.forEach { doc ->
                    if (doc.exists()) {
                        userNames[doc.id] = doc.getString("name") ?: "Anonymous User"
                    }
                }
            }

            debugMessage.value = "Processing final data..."

            // 6. Build final order details map with actual names
            val finalOrderDetailsMap = mutableMapOf<String, Pair<String, String>>()
            val restaurantCountMap = mutableMapOf<String, MutableMap<String, Int>>()
            val restaurantSet = mutableSetOf<String>()

            orderDetailsMap.forEach { (orderId, ids) ->
                val restaurantId = ids.first
                val userId = ids.second

                val restaurantName = restaurantNames[restaurantId] ?: "Unknown Restaurant"
                val userName = userNames[userId] ?: "Anonymous User"

                finalOrderDetailsMap[orderId] = Pair(restaurantName, userName)
                restaurantSet.add(restaurantName)

                // Initialize restaurant stats
                if (!restaurantCountMap.containsKey(restaurantName)) {
                    restaurantCountMap[restaurantName] = mutableMapOf(
                        "positive" to 0,
                        "negative" to 0
                    )
                }
            }

            // 7. Calculate sentiment stats
            tempFeedbacks.forEach { feedback ->
                val (restaurantName, _) = finalOrderDetailsMap[feedback.orderId] ?: return@forEach

                val categorizedSentiment = categorizeSentiment(feedback.sentiment)
                if (categorizedSentiment == "positive" || categorizedSentiment == "negative") {
                    restaurantCountMap[restaurantName]?.let { counts ->
                        counts[categorizedSentiment] = (counts[categorizedSentiment] ?: 0) + 1
                    }
                }
            }

            // 8. Update caches and UI state
            withContext(Dispatchers.Main) {
                orderDetailsCache.value = finalOrderDetailsMap
                restaurantCache.value = restaurantNames
                userCache.value = userNames

                availableRestaurants.value = listOf("All") + restaurantSet.toList().sorted()

                val stats = restaurantCountMap.filter { (_, counts) ->
                    (counts["positive"] ?: 0) > 0 || (counts["negative"] ?: 0) > 0
                }.map { (name, counts) ->
                    RestaurantStats(
                        name = name,
                        positiveCount = counts["positive"] ?: 0,
                        negativeCount = counts["negative"] ?: 0
                    )
                }

                restaurantStats.value = stats
            }

            debugMessage.value = "Data loaded successfully: ${tempFeedbacks.size} feedbacks, ${restaurantSet.size} restaurants"
            return tempFeedbacks

        } catch (e: Exception) {
            debugMessage.value = "Error: ${e.message}"
            throw e
        }
    }

    // Initial data fetch
    LaunchedEffect(Unit) {
        isLoading.value = true
        try {
            val fetchedFeedbacks = fetchFeedbackData()
            feedbacks.value = fetchedFeedbacks
            applyFilters(fetchedFeedbacks)
        } catch (e: Exception) {
            println("Error fetching data: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    // Apply filters when they change
    LaunchedEffect(selectedSentiment.value, selectedRestaurant.value, sortOrder.value) {
        applyFilters(feedbacks.value)
    }

    // Simplified real-time listener - only for feedback changes
    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val feedbackListener = db.collection("feedbacks")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Listen failed: ${e.message}")
                    return@addSnapshotListener
                }

                val updatedFeedbacks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Feedback::class.java)?.apply {
                        if (this.feedbackId.isEmpty()) {
                            this.feedbackId = doc.id
                        }
                    }
                } ?: emptyList()

                // Check if we have new feedbacks that need order details
                val currentOrderIds = orderDetailsCache.value.keys
                val newFeedbacks = updatedFeedbacks.filter { it.orderId !in currentOrderIds }

                if (newFeedbacks.isNotEmpty()) {
                    // Only fetch details for truly new feedbacks
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Use the same optimized fetching approach for new feedbacks
                            val newOrderIds = newFeedbacks.map { it.orderId }.distinct()

                            // Fetch only the new order details
                            val db = FirebaseFirestore.getInstance()
                            val newOrderQuery = db.collection("orders")
                                .whereIn("orderId", newOrderIds.take(10)) // Limit to 10 for 'in' query
                                .get()
                                .await()

                            val newOrderDetailsMap = orderDetailsCache.value.toMutableMap()
                            val newRestaurantIds = mutableSetOf<String>()
                            val newUserIds = mutableSetOf<String>()

                            newOrderQuery.documents.forEach { orderDoc ->
                                val orderId = orderDoc.getString("orderId") ?: ""
                                val restaurantId = orderDoc.getString("restId") ?: ""
                                val customerUserId = orderDoc.getString("userId") ?: ""

                                if (orderId.isNotEmpty() && restaurantId.isNotEmpty() && customerUserId.isNotEmpty()) {
                                    newRestaurantIds.add(restaurantId)
                                    newUserIds.add(customerUserId)
                                    newOrderDetailsMap[orderId] = Pair(restaurantId, customerUserId)
                                }
                            }

                            // Fetch new restaurant and user names if needed
                            val currentRestaurantCache = restaurantCache.value.toMutableMap()
                            val currentUserCache = userCache.value.toMutableMap()

                            val missingRestaurants = newRestaurantIds.filter { it !in currentRestaurantCache }
                            val missingUsers = newUserIds.filter { it !in currentUserCache }

                            // Batch fetch missing data
                            if (missingRestaurants.isNotEmpty()) {
                                val restaurantTasks = missingRestaurants.map { restaurantId ->
                                    db.collection("Restaurant").document(restaurantId).get()
                                }

                                val restaurantResults = Tasks.whenAllSuccess<DocumentSnapshot>(restaurantTasks).await()
                                restaurantResults.forEach { doc ->
                                    if (doc.exists()) {
                                        currentRestaurantCache[doc.id] = doc.getString("name") ?: "Unknown Restaurant"
                                    }
                                }
                            }

                            if (missingUsers.isNotEmpty()) {
                                val userTasks = missingUsers.map { userId ->
                                    db.collection("users").document(userId).get()
                                }

                                val userResults = Tasks.whenAllSuccess<DocumentSnapshot>(userTasks).await()
                                userResults.forEach { doc ->
                                    if (doc.exists()) {
                                        currentUserCache[doc.id] = doc.getString("name") ?: "Anonymous User"
                                    }
                                }
                            }

                            // Update final order details map
                            val finalOrderDetailsMap = newOrderDetailsMap.mapValues { (_, ids) ->
                                val restaurantId = ids.first
                                val userId = ids.second
                                Pair(
                                    currentRestaurantCache[restaurantId] ?: "Unknown Restaurant",
                                    currentUserCache[userId] ?: "Anonymous User"
                                )
                            }

                            withContext(Dispatchers.Main) {
                                orderDetailsCache.value = finalOrderDetailsMap
                                restaurantCache.value = currentRestaurantCache
                                userCache.value = currentUserCache

                                // Update available restaurants
                                availableRestaurants.value = listOf("All") + finalOrderDetailsMap.values
                                    .map { it.first }
                                    .distinct()
                                    .sorted()

                                // Update feedbacks and apply filters
                                feedbacks.value = updatedFeedbacks
                                applyFilters(updatedFeedbacks)
                            }
                        } catch (e: Exception) {
                            println("Error updating new feedbacks: ${e.message}")
                        }
                    }
                } else {
                    // No new feedbacks, just update existing ones
                    feedbacks.value = updatedFeedbacks
                    applyFilters(updatedFeedbacks)
                }
            }

        onDispose {
            feedbackListener.remove()
        }
    }

    // Rest of your UI code remains the same...
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Feedback Analysis") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { filterExpanded.value = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }

                    DropdownMenu(
                        expanded = filterExpanded.value,
                        onDismissRequest = { filterExpanded.value = false }
                    ) {
                        Text(
                            "Filter by Restaurant",
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )

                        availableRestaurants.value.forEach { restaurant ->
                            DropdownMenuItem(
                                text = { Text(restaurant) },
                                onClick = {
                                    selectedRestaurant.value = restaurant
                                    filterExpanded.value = false
                                }
                            )
                        }

                        Divider()

                        Text(
                            "Filter by Sentiment",
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )

                        DropdownMenuItem(
                            text = { Text("All Reviews") },
                            onClick = {
                                selectedSentiment.value = "All"
                                filterExpanded.value = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Positive Reviews") },
                            onClick = {
                                selectedSentiment.value = "positive"
                                filterExpanded.value = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Negative Reviews") },
                            onClick = {
                                selectedSentiment.value = "negative"
                                filterExpanded.value = false
                            }
                        )

                        Divider()

                        Text(
                            "Sort by",
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )

                        DropdownMenuItem(
                            text = { Text("Newest First") },
                            onClick = {
                                sortOrder.value = "Newest First"
                                filterExpanded.value = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Oldest First") },
                            onClick = {
                                sortOrder.value = "Oldest First"
                                filterExpanded.value = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Highest Rating") },
                            onClick = {
                                sortOrder.value = "Highest Rating"
                                filterExpanded.value = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Lowest Rating") },
                            onClick = {
                                sortOrder.value = "Lowest Rating"
                                filterExpanded.value = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading feedback data...")
                    if (debugMessage.value.isNotEmpty()) {
                        Text(
                            debugMessage.value,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Restaurant comparison chart
                item {
                    Text(
                        "Restaurant Sentiment Analysis",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (restaurantStats.value.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No feedback data available yet",
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )

                                if (debugMessage.value.isNotEmpty()) {
                                    Text(
                                        debugMessage.value,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        FeedbackBarChart(restaurantStats.value)
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Current filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Customer Reviews",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            buildString {
                                if (selectedRestaurant.value != "All") {
                                    append(selectedRestaurant.value)
                                    append(" • ")
                                }

                                append(when (selectedSentiment.value) {
                                    "All" -> "All Reviews"
                                    else -> "${selectedSentiment.value.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            Locale.ROOT
                                        ) else it.toString()
                                    }} Reviews"
                                })
                                append(" • ")
                                append(sortOrder.value)
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Review list
                if (filteredFeedbacks.value.isEmpty()) {
                    item {
                        Text(
                            "No reviews match your filters",
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                } else {
                    items(filteredFeedbacks.value) { feedback ->
                        val (restaurantName, userName) = orderDetailsCache.value[feedback.orderId]
                            ?: Pair("Unknown Restaurant", "Anonymous User")

                        FeedbackCard(
                            feedback = feedback,
                            restaurantName = restaurantName,
                            userName = userName
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun FeedbackBarChart(stats: List<RestaurantStats>) {
    // Make sure we have data to display
    if (stats.isEmpty()) {
        Text(
            "No restaurant feedback statistics available",
            modifier = Modifier.padding(vertical = 16.dp)
        )
        return
    }

    // Define custom colors for a more vibrant chart
    val positiveColor = Color(0xFF4CAF50)  // Green
    val negativeColor = Color(0xFFE53935)  // Red

    // Animation values
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = stats) {
        // Reset and start animation when stats change
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        // Draw the custom chart
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height - 50.dp.toPx() // Reserve space for labels
            val maxValue = stats.maxOf { maxOf(it.positiveCount, it.negativeCount) }.toFloat()
            val barMaxHeight = height * 0.8f  // Maximum bar height
            val barWidthTotal = width / stats.size * 0.8f  // Total width for each restaurant
            val barWidth = barWidthTotal / 2 * 0.85f  // Width for each bar (positive or negative)
            val barSpacing = barWidth * 0.2f  // Spacing between bars

            // Draw bars
            stats.forEachIndexed { index, stat ->
                val xCenter = width * (index + 0.5f) / stats.size

                // Calculate total for percentage
                val total = (stat.positiveCount + stat.negativeCount).toFloat().coerceAtLeast(1f)
                val positivePercentage = (stat.positiveCount / total * 100).toInt()
                val negativePercentage = (stat.negativeCount / total * 100).toInt()

                // Apply animation to bar heights
                val animatedFactor = animationProgress.value

                // Positive bar
                val positiveHeight = if (maxValue > 0)
                    (stat.positiveCount / maxValue) * barMaxHeight * animatedFactor else 0f

                drawRoundRect(
                    color = positiveColor,
                    topLeft = Offset(
                        x = xCenter - barWidth - barSpacing/2,
                        y = height - positiveHeight
                    ),
                    size = Size(barWidth, positiveHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw percentage on positive bar
                if (positiveHeight > 30.dp.toPx() && stat.positiveCount > 0) {
                    val text = "$positivePercentage%"
                    val textPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11.sp.toPx()
                        isFakeBoldText = true
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        xCenter - barWidth / 2 - barSpacing / 2,
                        height - positiveHeight + 20.dp.toPx(),
                        textPaint
                    )
                }

                // Negative bar
                val negativeHeight = if (maxValue > 0)
                    (stat.negativeCount / maxValue) * barMaxHeight * animatedFactor else 0f

                drawRoundRect(
                    color = negativeColor,
                    topLeft = Offset(
                        x = xCenter + barSpacing/2,
                        y = height - negativeHeight
                    ),
                    size = Size(barWidth, negativeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw percentage on negative bar
                if (negativeHeight > 30.dp.toPx() && stat.negativeCount > 0) {
                    val text = "$negativePercentage%"
                    val textPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 11.sp.toPx()
                        isFakeBoldText = true
                    }

                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        xCenter + barWidth / 2 + barSpacing / 2,
                        height - negativeHeight + 20.dp.toPx(),
                        textPaint
                    )
                }
            }
        }

        // Restaurant names
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stats.forEach { stat ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = stat.name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Add counts with colored text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${stat.positiveCount}",
                                fontSize = 10.sp,
                                color = positiveColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${stat.negativeCount}",
                                fontSize = 10.sp,
                                color = negativeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackCard(
    feedback: Feedback,
    restaurantName: String,
    userName: String
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    // Categorize sentiment for display
    val displaySentiment = when (feedback.sentiment.lowercase()) {
        "strong positive", "mild positive" -> "Positive"
        "strong negative", "mild negative" -> "Negative"
        else -> feedback.sentiment
    }

    // Determine sentiment color
    val sentimentColor = when (displaySentiment.lowercase()) {
        "positive" -> Color(0xFF4CAF50) // Green
        "negative" -> Color(0xFFE53935) // Red
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        restaurantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        "By $userName",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = sentimentColor
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        displaySentiment,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Star rating
            Row {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= feedback.rating) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(feedback.comment)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                dateFormatter.format(feedback.timestamp.toDate()),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
// Data class for restaurant statistics
data class RestaurantStats(
    val name: String,
    val positiveCount: Int,
    val negativeCount: Int
)
@Composable
fun OrderItemCard(
    order: Order,
    cardColor: Color,
    borderColor: Color,
    statusColor: Color,
    restaurantName: String = "Unknown Restaurant",
    onFeedbackClick: (Order) -> Unit = {} // New parameter for feedback action
) {
    val pickupTimeStr = try {
        order.pickupTime?.toDate()?.let { date ->
            SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(date)
        } ?: "Not Assigned"
    } catch (e: Exception) {
        "Not Assigned"
    }

    val totalAmount = order.items.sumOf { it.price.toDoubleOrNull()?.times(it.quantity.toIntOrNull() ?: 1) ?: 0.0 }

    val statusText = when (order.status) {
        "ready" -> "Ready for Pickup"
        "pending" -> "Preparing"
        "delivered" -> "Completed"
        else -> order.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.orderId}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)

                )
                Spacer(modifier = Modifier.width(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, statusColor),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Restaurant information row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = "Restaurant",
                    tint = Color(0xFFFF9800), // Orange accent color
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = restaurantName,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE53935), // Red primary color
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = "Pickup Time",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = pickupTimeStr,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = borderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Order",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.quantity}x",
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.width(30.dp)
                        )
                        Text(
                            text = item.itemName,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "₹${item.price}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = borderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "₹${"%.2f".format(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Add feedback button for delivered orders
            if (order.status == "delivered") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onFeedbackClick(order) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800) // Orange accent color
                    ),
                    shape = RoundedCornerShape(24.dp),
                    //modifier = Modifier.align(Alignment.End)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Give Feedback",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    //Text("Rate Order", color = Color.White)
                    Text("Rate Order", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

// Now let's create a FeedbackDialog component
@Composable
fun FeedbackDialog(
    order: Order,
    restaurantName: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Rate Your Experience",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = restaurantName,
                    fontSize = 14.sp,
                    color = Color(0xFFE53935)
                )
            }
        },
        text = {
            Column {
                // Rating stars
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFB300) else Color.LightGray,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Comment field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(text = "Your Comments (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Tell us about your experience...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun fetchFeedbackStatus(userId: String): MutableMap<String, Boolean> {
    val feedbackMap = remember { mutableStateMapOf<String, Boolean>() }
    val orders = remember { mutableStateListOf<Order>() }

    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()

        // First get all orders for this user
        db.collection("orders")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { orderDocs ->
                // Extract order IDs
                val orderIds = orderDocs.documents.mapNotNull {
                    it.toObject(Order::class.java)?.orderId
                }

                // If there are no orders, nothing to check
                if (orderIds.isEmpty()) return@addOnSuccessListener

                // Check for feedback for these orders
                db.collection("feedbacks")
                    .whereIn("orderId", orderIds)
                    .get()
                    .addOnSuccessListener { feedbacks ->
                        for (feedback in feedbacks) {
                            val orderId = feedback.getString("orderId") ?: ""
                            if (orderId.isNotEmpty()) {
                                feedbackMap[orderId] = true
                            }
                        }
                    }
            }
    }

    return feedbackMap
}
// Now let's update the OrdersScreen to include the feedback functionality
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(userId: String, navigateBack: () -> Unit) {
    val context = LocalContext.current
    val orders = remember { mutableStateListOf<Order>() }
    val restaurantMap = remember { mutableStateMapOf<String, String>() }

    // Track feedback state
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    // To keep track of orders that have feedback
    val feedbackMap = fetchFeedbackStatus(userId)

    // Theme colors
    val primaryColor = Color(0xFFE53935)       // Red
    val secondaryColor = Color(0xFFFFB300)     // Yellow
    val accentColor = Color(0xFFFF9800)        // Orange
    val backgroundColor = Color(0xFFFFF3E0)    // Light orange/cream background
    val readyOrderColor = Color(0xFF43A047)    // Green for ready orders

    // Load orders and restaurant info
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()

        // First, get all restaurants and create a map for quick lookup
        db.collection("Restaurant")
            .get()
            .addOnSuccessListener { restaurants ->
                for (restaurantDoc in restaurants) {
                    val restaurantId = restaurantDoc.id
                    val restaurantName = restaurantDoc.getString("name") ?: "Unknown Restaurant"
                    restaurantMap[restaurantId] = restaurantName
                }

                // Now get orders
                db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("Firestore", "Error fetching orders", e)
                            return@addSnapshotListener
                        }

                        val updatedOrders = snapshot?.documents?.mapNotNull { it.toObject(Order::class.java) } ?: emptyList()

                        // Send notifications ONLY when status changes to "ready"
                        updatedOrders.forEach { newOrder ->
                            val existingOrder = orders.find { it.orderId == newOrder.orderId }

                            // Notify only if order status changed from something else to "ready"
                            if (existingOrder != null && existingOrder.status != "ready" && newOrder.status == "ready") {
                                sendLocalNotification(
                                    context,
                                    orderId = newOrder.orderId,
                                    orderStatus = newOrder.status
                                )
                            }
                        }
                        orders.clear()
                        orders.addAll(updatedOrders)
                    }
            }
    }
    fun submitFeedback(context: Context, order: Order, rating: Int, comment: String) {
        val db = FirebaseFirestore.getInstance()
        val feedbackId = db.collection("feedbacks").document().id

        if (comment.isNotEmpty()) {
            Toast.makeText(context, "Processing your feedback...", Toast.LENGTH_SHORT).show()

            sendReview(comment) { sentiment ->
                saveFeedbackWithSentiment(context, db, feedbackId, order, rating, comment, sentiment)
            }
        } else {
            saveFeedbackWithSentiment(context, db, feedbackId, order, rating, "", "")
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Orders",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (orders.isEmpty()) {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = "No Orders",
                        tint = accentColor,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No orders yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your order history will appear here",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val readyOrders = orders.filter { it.status == "ready" }
                    val pendingOrders = orders.filter { it.status == "pending" }
                    val pastOrders = orders.filter { it.status == "delivered" }

                    if (readyOrders.isNotEmpty()) {
                        item { SectionHeader(title = "Ready for Pickup", color = readyOrderColor) }
                        items(readyOrders) { order ->
                            OrderItemCard(
                                order = order,
                                cardColor = Color(0xFFEAF6EC),
                                borderColor = readyOrderColor,
                                statusColor = readyOrderColor,
                                restaurantName = restaurantMap[order.restId] ?: "Unknown Restaurant"
                            )
                        }
                    }

                    if (pendingOrders.isNotEmpty()) {
                        item { SectionHeader(title = "Preparing Your Order", color = accentColor) }
                        items(pendingOrders) { order ->
                            OrderItemCard(
                                order = order,
                                cardColor = Color(0xFFFFF8E1),
                                borderColor = secondaryColor,
                                statusColor = accentColor,
                                restaurantName = restaurantMap[order.restId] ?: "Unknown Restaurant"
                            )
                        }
                    }

                    if (pastOrders.isNotEmpty()) {
                        item { SectionHeader(title = "Past Orders", color = Color.DarkGray) }
                        items(pastOrders) { order ->
                            OrderItemCard(
                                order = order,
                                cardColor = Color.White,
                                borderColor = Color.LightGray,
                                statusColor = Color.DarkGray,
                                restaurantName = restaurantMap[order.restId] ?: "Unknown Restaurant",
                                onFeedbackClick = {
                                    // Only show feedback dialog if user hasn't already given feedback
                                    if (feedbackMap[order.orderId] != true) {
                                        selectedOrder = order
                                        showFeedbackDialog = true
                                    } else {
                                        Toast.makeText(context, "You've already rated this order", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Show feedback dialog when needed
            if (showFeedbackDialog && selectedOrder != null) {
                FeedbackDialog(
                    order = selectedOrder!!,
                    restaurantName = restaurantMap[selectedOrder!!.restId] ?: "Unknown Restaurant",
                    onDismiss = {
                        showFeedbackDialog = false
                        selectedOrder = null
                    },
                    onSubmit = { rating, comment ->
                        submitFeedback(context,selectedOrder!!, rating, comment)
                        showFeedbackDialog = false
                        selectedOrder = null
                    }
                )
            }
        }
    }
}
fun sendReview(review: String, onResult: (String) -> Unit) {
    val api = RetrofitClient.api
    val request = SentimentRequest(review)

    api.analyzeReview(request).enqueue(object : Callback<SentimentResponse> {
        override fun onResponse(call: Call<SentimentResponse>, response: Response<SentimentResponse>) {
            if (response.isSuccessful) {
                onResult(response.body()?.sentiment ?: "Unknown")
            } else {
                onResult("Error: Unable to analyze sentiment")
            }
        }

        override fun onFailure(call: Call<SentimentResponse>, t: Throwable) {
            onResult("Error: ${t.message}")
        }
    })
}
fun saveFeedbackWithSentiment(
    context: Context,
    db: FirebaseFirestore,
    feedbackId: String,
    order: Order,
    rating: Int,
    comment: String,
    sentiment: String
) {
    val feedback = Feedback(
        feedbackId = feedbackId,
        orderId = order.orderId,
        rating = rating,
        comment = comment,
        sentiment = sentiment,
        timestamp = Timestamp.now()
    )

    db.collection("feedbacks").document(feedbackId)
        .set(feedback)
        .addOnSuccessListener {
            Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to submit feedback: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(4.dp, 24.dp)
                .background(color = color, shape = RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
    }
}
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Order Notifications"
        val descriptionText = "Notifications for order status updates"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("orders_channel", name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
fun sendLocalNotification(context: Context, orderId: String, orderStatus: String) {
    Log.d("Notification", "Attempting to send notification for Order ID: $orderId, Status: $orderStatus")

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notificationBuilder = NotificationCompat.Builder(context, "orders_channel")
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Order Status Update")
        .setContentText("Your order #$orderId is now $orderStatus. Go grab it.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    val notificationId = orderId.hashCode()

    // ✅ Fix: Check notification permission properly
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(context, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Notification", "Notification permission not granted")
            return
        }
    }

    notificationManager.notify(notificationId, notificationBuilder.build())
}


//orignal
//@Composable
//fun HeaderSection() {
//    var searchQuery by remember { mutableStateOf("") } // State to hold the search query
////    var showMenu by remember { mutableStateOf(false) } // State to show/hide the menu
//    val context = LocalContext.current
//    var expanded by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "One Click Eats",
//                fontWeight = FontWeight.Bold,
//                fontSize = 32.sp,
//                color = Color(0xFF27202F) ,
//                fontFamily = FontFamily.SansSerif,
//                letterSpacing = 1.2.sp
//            )
//            Spacer(modifier = Modifier.weight(1f))
//            // Profile Icon with Dropdown
//            Box {
//                IconButton(onClick = { expanded = true }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.baseline_account_circle_24),
//                        contentDescription = "Profile Icon",
//                        modifier = Modifier.size(32.dp)
//                    )
//                }
//
//                // Dropdown Menu
//                DropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false }
//                ) {
//                    DropdownMenuItem(
//                        text = { Text("Edit Profile") },
//                        onClick = {
//                            expanded = false
//                            val intent = Intent(context, EditProfileActivity::class.java)
//                            context.startActivity(intent)  // Open Edit Profile Screen
//                        }
//                    )
//                    DropdownMenuItem(
//                        text = { Text("Logout") },
//                        onClick = {
//                            expanded = false
//                            logoutUser(context)
//                        }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp)) // Space between title and search bar

// Search Bar with Icon Inside
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(20.dp))
//                .background(Color.White)
//                .border(BorderStroke(2.dp, Color.Gray), RoundedCornerShape(20.dp))
//                .padding(horizontal = 12.dp, vertical = 8.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.baseline_content_paste_search_24), // Search icon
//                    contentDescription = "Search Icon",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(20.dp) // Small icon size
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                BasicTextField(
//                    value = searchQuery,
//                    onValueChange = { searchQuery = it },
//                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
//                    modifier = Modifier.weight(1f),
//                    decorationBox = { innerTextField ->
//                        if (searchQuery.isEmpty()) {
//                            Text("Search here...", color = Color.Gray)
//                        }
//                        innerTextField()
//                    }
//                )
////            }
//        }
//    }
@Composable
fun HeaderSection() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    // Controls when the title should appear
    var titleVisible by remember { mutableStateOf(false) }

    // Trigger the animation on first composition
    LaunchedEffect(Unit) {
        titleVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 2000)) +
                        slideInVertically(
                            initialOffsetY = { -40 },
                            animationSpec = tween(durationMillis = 2000)
                        )
            ) {
                Text(
                    text = "One Click Eats",
                    fontWeight = FontWeight.Bold,
                    fontSize =42.sp,
                    color = Color(0xFF27202F),

                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Profile Icon with Dropdown
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "Profile Icon",
                        modifier = Modifier.size(32.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Profile") },
                        onClick = {
                            expanded = false
                            val intent = Intent(context, EditProfileActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            expanded = false
                            logoutUser(context)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
// Data class to hold restaurant details
data class Canteen(val name: String, val imageRes: Int, val rating: String,val restId: String)
@Composable
fun CanteenCard(canteen: Canteen, navigateToRestaurant: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Ensures full width despite padding outside
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp,Color(0xFF000000).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
// .clickable { navigateToRestaurant(canteen) },
            .clickable { navigateToRestaurant(canteen.restId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor =Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = canteen.imageRes),
                contentDescription = canteen.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Image height
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            // Separator Line
            Divider(
                color = Color(0xFF000000) .copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = canteen.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF000000)
                )
                Text(
                    text = "⭐ ${canteen.rating}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color =Color(0xFFFF9800)    , // Beautiful Green Color for Rating
                    modifier = Modifier
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
@Composable
fun RestaurantScreen(canteen: Canteen, userId: String, navigateBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var categories by remember { mutableStateOf(listOf<Category>()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(canteen.restId) {
        db.collection("Restaurant").document(canteen.restId)
            .collection("categories")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    categories = it.documents.map { doc ->
                        Category(
                            catId = doc.id,
                            cat_name = doc.getString("cat_name") ?: "",
                            image_url = doc.getString("image_url") ?: ""
                        )
                    }
                    if (categories.isNotEmpty()) {
                        selectedCategory = categories.first().catId
                        Log.d("FirestoreDebug", "Selected Category: $selectedCategory")
                    }
                }
            }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF5))
    ) {
        //  Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Back Button & Restaurant Image
            Box {
                Image(
                    painter = painterResource(id = canteen.imageRes), // Fetch image from Canteen data class
                    contentDescription = canteen.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = navigateBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restaurant Name
            Text(
                text = canteen.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Category List
            LazyRow {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        isSelected = selectedCategory == category.catId
                    ) { selectedCategory = category.catId }
                }
            }

            // Food Items List
            selectedCategory?.let { categoryId ->
                FoodItemsList(canteen.restId, categoryId, userId)
            }
        }
    }
}
@Composable
fun CategoryCard(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(category.image_url)
                .crossfade(true)
                .build(),
            contentDescription = category.cat_name,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) Color.Red else Color.Transparent,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = category.cat_name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable
fun FoodItemRow(item: FoodItem, userId: String) {
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.itemName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹${item.price}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black // Green color for price
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // Add to Cart Button
            Button(
                onClick = {
                    val cartRef = db.collection("users").document(userId).collection("cart")
                    val cartItem = hashMapOf(
                        "itemName" to item.itemName,
                        "price" to item.price,
                        "quantity" to "1",
                        "uid" to userId,
                        "restId" to item.restId
                    )

                    cartRef.document(item.itemId).get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            // If item exists, increment quantity (stored as string)
                            val currentQuantity = doc.getString("quantity") ?: "1"
                            val newQuantity = (currentQuantity.toInt() + 1).toString()
                            cartRef.document(item.itemId).update("quantity", newQuantity)
                        } else {
                            // Add new item to cart
                            cartRef.document(item.itemId).set(cartItem)
                        }
                    }
                },
                modifier = Modifier.width(120.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)) // Green color
            ) {
                Text(text = "Add to Cart", color = Color.White)
            }
        }
    }
}

@Composable
fun FoodItemsList(restId: String, categoryId: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    var foodItems by remember { mutableStateOf(listOf<FoodItem>()) }

    LaunchedEffect(categoryId) {
        Log.d("FirestoreDebug", "Fetching food items for Category: $categoryId")
        db.collection("Restaurant").document(restId)
            .collection("categories").document(categoryId)
            .collection("items_table")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    foodItems = it.documents.map { doc ->
                        FoodItem(
                            itemId = doc.id,
                            itemName = doc.getString("itemName") ?: "",
                            price = doc.getString("price") ?: "0.0",
                            restId = restId
                        )
                    }
                    Log.d("FirestoreDebug", "Fetched ${foodItems.size} items")
                }
            }
    }

    LazyColumn {
        items(foodItems) { food ->
            FoodItemRow(food, userId) // Fixed function call
        }
    }
}
fun sendRequestToOwner(
    ownerId: String,
    userId: String,
    cartItems: List<Map<String, Any>>,
    onRequestResult: (String, String?, List<CartItem>, String?, String?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    if (cartItems.isEmpty()) {
        onRequestResult("No items in the cart.", null, emptyList(), null, null)
        return
    }

    val updatedCartItems = cartItems.map { item -> item + ("status" to "available") }

    val requestData = hashMapOf(
        "userId" to userId,
        "items" to updatedCartItems,
        "status" to "pending",
        "timestamp" to FieldValue.serverTimestamp(),
        "estimatedTime" to "30"
    )

    db.collection("owners").document(ownerId).collection("requests").add(requestData)
        .addOnSuccessListener { documentRef ->
            onRequestResult("Request sent successfully!", "pending", emptyList(), documentRef.id, ownerId)
        }
        .addOnFailureListener { e ->
            onRequestResult("Failed to send request: ${e.message}", null, emptyList(), null, null)
        }
}
fun fetchOwnerAndSendRequest(
    restId: String,
    userId: String,
    cartItems: List<Map<String, Any>>,
    onRequestResult: (String, String?, List<CartItem>, String?, String?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("owners")
        .whereEqualTo("restId", restId)
        .get()
        .addOnSuccessListener { ownerDocs ->
            if (ownerDocs.isEmpty) {
                onRequestResult("No owner found for this restaurant.", null, emptyList(), null, null)
                return@addOnSuccessListener
            }

            val ownerDoc = ownerDocs.documents.firstOrNull()
            if (ownerDoc == null) {
                onRequestResult("No owner found.", null, emptyList(), null, null)
                return@addOnSuccessListener
            }

            val ownerId = ownerDoc.id
            sendRequestToOwner(ownerId, userId, cartItems) { message, status, unavailableItems, docId, owner ->
                onRequestResult(message, status, unavailableItems, docId, owner)
            }
        }
        .addOnFailureListener { e ->
            onRequestResult("Failed to fetch restaurant details: ${e.message}", null, emptyList(), null, null)
        }
}

fun checkAvailabilityAndSendRequest(
    userId: String,
    onRequestResult: (String, String?, List<CartItem>, String?, String?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val cartRef = db.collection("users").document(userId).collection("cart")

    cartRef.get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                onRequestResult("Your cart is empty.", null, emptyList(), null, null)
                return@addOnSuccessListener
            }

            val restIds = mutableSetOf<String>()
            val cartItems = mutableListOf<Map<String, Any>>()

            for (document in documents) {
                val restId = document.getString("restId") ?: continue
                val itemName = document.getString("itemName") ?: "Unknown Item"
                val quantity = document.getString("quantity")?.toIntOrNull() ?: 1
                val price = document.getString("price")?.toIntOrNull() ?: 0

                restIds.add(restId)
                cartItems.add(mapOf("itemName" to itemName, "quantity" to quantity, "price" to price))
            }

            when {
                restIds.size > 1 -> onRequestResult("Cannot send request because items are from different restaurants.", null, emptyList(), null, null)
                restIds.isEmpty() -> onRequestResult("Your cart is empty.", null, emptyList(), null, null)
                else -> {
                    val restId = restIds.first()
                    fetchOwnerAndSendRequest(restId, userId, cartItems) { message, status, unavailableItems, docId, ownerId ->
                        onRequestResult(message, status, unavailableItems, docId, ownerId)
                    }
                }
            }
        }
        .addOnFailureListener { e ->
            onRequestResult("Failed to fetch cart items: ${e.message}", null, emptyList(), null, null)
        }
}




private fun deleteRequest(requestDocId: String, userId: String) {
    val db = FirebaseFirestore.getInstance()

    // Get the owner's document based on the request ID
    db.collection("owners").get()
        .addOnSuccessListener { ownerDocs ->
            for (ownerDoc in ownerDocs) {
                val ownerId = ownerDoc.id  // Owner document ID


                val requestRef = db.collection("owners")
                    .document(ownerId)
                    .collection("requests")
                    .document(requestDocId)

                requestRef.delete()
                    .addOnSuccessListener {
                        Log.d("Firestore", "Request deleted successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error deleting request", e)
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching owners", e)
        }
}


fun removeUnavailableItemsFromCart(userId: String, unavailableItems: List<CartItem>, requestDocId: String, ownerId: String, onItemsRemoved: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val cartRef = db.collection("users").document(userId).collection("cart")

    unavailableItems.forEach { item ->
        cartRef.whereEqualTo("itemName", item.itemName)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { it.reference.delete() }
            }
    }

    // ✅ Update request status to indicate user is modifying the order (instead of deleting it immediately)
    db.collection("owners").document(ownerId)
        .collection("requests").document(requestDocId)
        .update("status", "modifying")
        .addOnSuccessListener {
            onItemsRemoved() // Callback when items are removed
        }
}

fun placeOrderAfterPayment(userId: String, ownerId: String, restId: String, requestDocId: String,onOrderPlaced: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val requestRef = db.collection("owners").document(ownerId).collection("requests").document(requestDocId)

    requestRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            Log.e("placeOrder", "Error: Request document not found!")
            return@addOnSuccessListener
        }

        val timestamp = document.getTimestamp("timestamp") ?: Timestamp.now()
        val itemsList = document.get("items") as? List<Map<String, Any>> ?: emptyList()
        val estimatedTimeString = document.getString("estimatedTime") ?: "30" // Default to 30 min if missing

        Log.d("placeOrder", "Fetched items list: $itemsList") // 🔹 Debugging log
        // Convert estimated time to integer
        val estimatedTimeMinutes = estimatedTimeString.toIntOrNull() ?: 30 // Default 30 min if invalid

        // Calculate pickup time
        val pickupTimestamp = Timestamp(Date(timestamp.toDate().time + estimatedTimeMinutes * 60 * 1000))

        val orderItems = itemsList.filter { it["status"] == "available" }.mapNotNull {
            val itemName = it["itemName"] as? String
            val price = it["price"]?.toString()
            val quantity = it["quantity"]?.toString()

            if (itemName != null && price != null && quantity != null) {
                OrderItem(itemName, price, quantity)
            } else {
                Log.e("placeOrder", "Error: Missing fields in item -> $it")
                null
            }
        }

        if (orderItems.isEmpty()) {
            Log.e("placeOrder", "Error: No available items to place order.")
            return@addOnSuccessListener
        }

        val ordersRef = db.collection("orders").document() // Generate unique Order ID
        val newOrder = Order(
            orderId = ordersRef.id,
            userId = userId,
            ownerId = ownerId,
            restId = restId,
            timestamp = timestamp,
            status = "pending",
            pickupTime = pickupTimestamp,
            items = orderItems
        )

        ordersRef.set(newOrder)
            .addOnSuccessListener {
                Log.d("placeOrder", "✅ Order placed successfully!")

                requestRef.delete()
                    .addOnSuccessListener { Log.d("placeOrder", "✅ Request deleted after order placement.") }
                    .addOnFailureListener { e -> Log.e("placeOrder", "Error deleting request: ${e.message}") }
                onOrderPlaced()
            }
            .addOnFailureListener { e ->
                Log.e("placeOrder", "Error placing order: ${e.message}")
            }
    }.addOnFailureListener { e ->
        Log.e("placeOrder", "Error fetching request details: ${e.message}")
    }
}

@Composable
fun CartItemRow(cartItem: CartItem, userId: String) {
    val db = FirebaseFirestore.getInstance()

    // ✅ Convert price & quantity from String to numbers
    val unitPrice = cartItem.price.toDoubleOrNull() ?: 0.0
    val quantity = cartItem.quantity.toIntOrNull() ?: 1
    val totalPrice = unitPrice * quantity

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = cartItem.itemName, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(text = "₹${"%.2f".format(totalPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold) // ✅ Updated price display
        }

        // Quantity Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    val newQuantity = (quantity - 1).coerceAtLeast(0).toString()

                    if (newQuantity == "0") {
                        db.collection("users").document(userId).collection("cart")
                            .document(cartItem.itemId)
                            .delete()
                    } else {
                        db.collection("users").document(userId).collection("cart")
                            .document(cartItem.itemId)
                            .update(mapOf(
                                "quantity" to newQuantity
                            ))
                    }
                }
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease Quantity")
            }

            Text(text = cartItem.quantity, fontSize = 16.sp, fontWeight = FontWeight.Bold)

            IconButton(
                onClick = {
                    val newQuantity = (quantity + 1).toString()
                    db.collection("users").document(userId).collection("cart")
                        .document(cartItem.itemId)
                        .update(mapOf(
                            "quantity" to newQuantity
                        ))
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase Quantity")
            }
        }
    }
}
@Composable
fun CartScreen(userId: String, navigateBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Core cart state - completely independent
    var cartItems by remember { mutableStateOf(listOf<CartItem>()) }
    var isOrderAllowed by remember { mutableStateOf(true) }

    // Recommendation state - separate from core functionality
    var recommendedItems by remember { mutableStateOf(listOf<String>()) }
    var isLoadingRecommendations by remember { mutableStateOf(false) }
    var shouldShowRecommendations by remember { mutableStateOf(false) }

    // Order request state
    var popupMessage by remember { mutableStateOf<String?>(null) }
    var requestStatus by remember { mutableStateOf<String?>(null) }
    var unavailableItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var requestDocId by remember { mutableStateOf<String?>(null) }
    var ownerId by remember { mutableStateOf<String?>(null) }
    val showPopupState = remember { mutableStateOf(false) }
    var orderPlacedMessage by remember { mutableStateOf<String?>(null) }

    // Coroutine scope for recommendation job
    val coroutineScope = rememberCoroutineScope()
    val recommendationJob = remember { mutableStateOf<Job?>(null) }

    // Keep track of Firestore listeners to clean up
    val snapshotListeners = remember { mutableListOf<ListenerRegistration>() }

    // Check if order is allowed based on time - simple and independent
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        isOrderAllowed = currentHour < 19 || (currentHour == 19 && currentMinute < 30)
    }

    // CORE FUNCTIONALITY: Cart items fetching - simple, fast, reliable
    LaunchedEffect(userId) {
        val listener = db.collection("users").document(userId).collection("cart")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CartScreen", "Firestore error: ${error.message}")
                    return@addSnapshotListener
                }

                cartItems = snapshot?.documents?.mapNotNull { doc ->
                    val itemName = doc.getString("itemName")
                    val price = doc.get("price")?.toString()
                    val quantity = doc.get("quantity")?.toString()
                    val uid = doc.getString("uid")
                    val restId = doc.getString("restId")

                    if (itemName != null && price != null && quantity != null && restId != null) {
                        CartItem(
                            itemId = doc.id,
                            itemName = itemName,
                            price = price,
                            quantity = quantity,
                            uid = uid ?: userId,
                            restId = restId
                        )
                    } else null
                } ?: emptyList()

                // Only enable recommendations if cart has items
                shouldShowRecommendations = cartItems.isNotEmpty()
            }

        // Add to list for cleanup
        snapshotListeners.add(listener)
    }
    // UPDATED: LaunchedEffect for recommendations without the timeout
    LaunchedEffect(shouldShowRecommendations, cartItems) {
        // Cancel any previous recommendation job
        recommendationJob.value?.cancel()
        recommendationJob.value = null

        if (shouldShowRecommendations && cartItems.isNotEmpty()) {
            isLoadingRecommendations = true
            recommendedItems = emptyList()  // Clear previous recommendations

            // Start a new job for recommendations
            recommendationJob.value = coroutineScope.launch {
                try {
                    val cartItemNames = cartItems.map { it.itemName }

                    // Use suspendCancellableCoroutine without timeout
                    val results = suspendCancellableCoroutine<List<String>> { continuation ->
                        // Set up cancellation handler
                        continuation.invokeOnCancellation {
                            Log.d("CartScreen", "Recommendation fetch cancelled")
                        }

                        // Call your actual recommendation function
                        fetchRecommendations(context, cartItemNames) { recommendations ->
                            if (continuation.isActive) {
                                Log.d("CartScreen", "Received recommendations in continuation: ${recommendations.size}")
                                continuation.resume(recommendations)
                            }
                        }
                    }

                    // Only update if still active
                    if (isActive) {
                        Log.d("CartScreen", "Setting recommendedItems with ${results.size} items")
                        recommendedItems = results
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> Log.d("CartScreen", "Recommendation fetch cancelled")
                        else -> Log.e("CartScreen", "Error loading recommendations: ${e.message}")
                    }
                } finally {
                    // Ensure loading state is cleared if still active
                    if (isActive) {
                        isLoadingRecommendations = false
                    }
                }
            }
        } else {
            // Reset recommendation state when cart is empty
            isLoadingRecommendations = false
            recommendedItems = emptyList()
        }
    }
    // CORE FUNCTIONALITY: Request status - independent from recommendations
    LaunchedEffect(userId) {
        db.collection("owners").get()
            .addOnSuccessListener { ownerDocs ->
                ownerDocs.documents.forEach { ownerDoc ->
                    val listener = ownerDoc.reference.collection("requests")
                        .whereEqualTo("userId", userId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e("CartScreen", "Error listening to requests: ${error.message}")
                                return@addSnapshotListener
                            }

                            snapshot?.documents?.forEach { doc ->
                                val status = doc.getString("status")
                                val items = doc.get("items") as? List<Map<String, Any>>

                                if (status != null) {
                                    requestStatus = status
                                    requestDocId = doc.id
                                    ownerId = ownerDoc.id

                                    when (status) {
                                        "accepted" -> {
                                            popupMessage = "Your order is accepted! Proceed with payment?"
                                            showPopupState.value = true
                                        }
                                        "rejected" -> {
                                            popupMessage = "Order is not available."
                                            showPopupState.value = true
                                        }
                                        "partially accepted" -> {
                                            unavailableItems = items?.filter { it["status"] == "unavailable" }?.mapNotNull { item ->
                                                val itemName = item["itemName"] as? String
                                                val price = item["price"].toString()
                                                val quantity = item["quantity"].toString()

                                                if (itemName != null && price.isNotEmpty() && quantity.isNotEmpty()) {
                                                    CartItem(
                                                        itemId = "",
                                                        itemName = itemName,
                                                        price = price,
                                                        quantity = quantity,
                                                        uid = userId,
                                                        restId = ""
                                                    )
                                                } else null
                                            } ?: emptyList()
                                            popupMessage = "Some items are unavailable. Please remove them."
                                            showPopupState.value = true
                                        }
                                    }
                                }
                            }
                        }
                    snapshotListeners.add(listener)
                }
            }
    }

    // Proper cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // Cancel recommendation job
            recommendationJob.value?.cancel()
            recommendationJob.value = null
            isLoadingRecommendations = false

            // Clean up all Firestore listeners
            snapshotListeners.forEach { it.remove() }
            snapshotListeners.clear()

            Log.d("CartScreen", "Cleaned up resources")
        }
    }

    // Main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // CORE UI: Top bar - always responsive
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        // Cancel any ongoing recommendation work when leaving
                        recommendationJob.value?.cancel()
                        recommendationJob.value = null
                        isLoadingRecommendations = false
                        navigateBack()
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Your Cart",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // CORE UI: Cart items - always works immediately
        if (cartItems.isEmpty()) {
            Text(
                text = "Your cart is empty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(cartItems) { cartItem ->
                    // Use key parameter to improve stability during recomposition
                    key(cartItem.itemId) {
                        CartItemRow(cartItem, userId)
                    }
                }
            }

            val totalCartPrice = cartItems.sumOf {
                (it.price.toDoubleOrNull() ?: 0.0) * (it.quantity.toIntOrNull() ?: 1)
            }

            Text(
                text = "Total: ₹${"%.2f".format(totalCartPrice)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )

            Button(
                onClick = {
                    checkAvailabilityAndSendRequest(userId) { message, status, unavailableItemsList, docId, owner ->
                        popupMessage = message
                        requestStatus = status
                        unavailableItems = unavailableItemsList
                        requestDocId = docId
                        ownerId = owner
                        showPopupState.value = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isOrderAllowed) Color(0xFFFF9800) else Color.Gray),
                enabled = isOrderAllowed
            ) {
                Text(
                    text = if (isOrderAllowed) "Check Availability" else "Ordering Closed",
                    color = Color.White
                )
            }
        }

        // OPTIONAL UI: Recommendations - only if we have cart items
        if (shouldShowRecommendations) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recommended for You",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Show loading status
                if (isLoadingRecommendations) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Recommendation content - handle empty state gracefully
            if (recommendedItems.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(recommendedItems.filter { it.isNotBlank() }) { item ->
                        // Use key parameter to improve stability
                        key(item) {
                            RecommendedItemCard(item)
                        }
                    }
                }
            } else if (isLoadingRecommendations) {
                // Lightweight loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Use a more lightweight loading indicator that won't cause layout shift
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }
        }
    }

    // CORE UI: Dialogs
    if (showPopupState.value) {
        AlertDialog(
            onDismissRequest = {
                if (requestStatus == "rejected" || requestStatus == "partially accepted" || requestStatus == "accepted") {
                    if (requestDocId != null && ownerId != null) {
                        Log.d("Firestore", "Popup dismissed, deleting request: requestDocId=$requestDocId, ownerId=$ownerId")
                        deleteRequest(requestDocId!!, ownerId!!)
                    }
                }
                showPopupState.value = false
            },
            confirmButton = {
                Column {
                    when (requestStatus) {
                        "rejected" -> {
                            Button(onClick = {
                                if (requestDocId != null && ownerId != null) {
                                    deleteRequest(requestDocId!!, ownerId!!)
                                }
                                showPopupState.value = false
                            }) {
                                Text("OK")
                            }
                        }
                        "partially accepted" -> {
                            LazyColumn(
                                modifier = Modifier.height(100.dp)
                            ) {
                                items(unavailableItems) { item ->
                                    Text("${item.itemName} is unavailable")
                                }
                            }
                            Button(onClick = {
                                if (requestDocId != null && ownerId != null) {
                                    removeUnavailableItemsFromCart(userId, unavailableItems, requestDocId!!, ownerId!!) {
                                        popupMessage = "Unavailable items removed. You can now proceed with payment."
                                        requestStatus = "ready_for_payment"
                                    }
                                }
                            }) {
                                Text("Remove Unavailable Items")
                            }
                        }
                    }

                    if (requestStatus == "ready_for_payment" || requestStatus == "accepted") {
                        Button(onClick = {
                            showPopupState.value = false
                            if (ownerId != null && requestDocId != null) {
                                placeOrderAfterPayment(userId, ownerId!!, cartItems.firstOrNull()?.restId ?: "", requestDocId!!) {
                                    orderPlacedMessage = "Your order has been placed successfully! You can check the order status in the Orders Screen."
                                }
                            }
                        }) {
                            Text("Proceed to Payment")
                        }
                    }
                }
            },
            title = { Text("Order Update") },
            text = { Text(popupMessage ?: "") }
        )
    }

    if (orderPlacedMessage != null) {
        AlertDialog(
            onDismissRequest = { orderPlacedMessage = null },
            confirmButton = {
                Button(onClick = { orderPlacedMessage = null }) {
                    Text("OK")
                }
            },
            title = { Text("Order Placed") },
            text = { Text(orderPlacedMessage!!) }
        )
    }
}
fun fetchRecommendations(context: Context, cartItemNames: List<String>, onResult: (List<String>) -> Unit) {
    Thread {
        try {
            Log.d("CartScreen", "Starting recommendation fetch for items: ${cartItemNames.joinToString()}")

            val inputStream = context.resources.openRawResource(R.raw.recommapp)
            val csvReader = inputStream.bufferedReader()
            val csvData = csvReader.readText()
            csvReader.close()

            Log.d("CartScreen", "CSV loaded, processing ${csvData.lines().size} lines")

            val recommendations = mutableSetOf<String>()
            val lines = csvData.lines().filter { it.isNotBlank() }

            // Skip header line if it exists
            val dataLines = if (lines.firstOrNull()?.lowercase()?.contains("antecedents") == true) {
                lines.drop(1)
            } else {
                lines
            }

            for (line in dataLines) {
                try {
                    // More flexible parsing approach
                    val columns = parseCsvLine(line)
                    if (columns.size < 3) continue

                    // Get antecedents and consequents from columns
                    val antecedents = parseItems(columns[1])
                    val consequents = parseItems(columns[2])

                    // Check if any cart item matches any antecedent
                    val matchFound = cartItemNames.any { cartItem ->
                        antecedents.any { antecedent ->
                            antecedent.equals(cartItem, ignoreCase = true)
                        }
                    }

                    if (matchFound) {
                        recommendations.addAll(consequents)
                    }
                } catch (e: Exception) {
                    Log.e("CartScreen", "Error parsing line: $line", e)
                    // Continue to the next line
                }
            }

            // Filter out items already in cart
            val filteredRecommendations = recommendations
                .filter { it.isNotBlank() }
                .filter { rec -> cartItemNames.none { it.equals(rec, ignoreCase = true) } }
                .distinct()
                .shuffled() // Randomize for variety
                .take(10)

            Log.d("CartScreen", "Found ${filteredRecommendations.size} recommendations")

            // Use fallback recommendations if none found
            val finalRecommendations = if (filteredRecommendations.isEmpty() && cartItemNames.isNotEmpty()) {
                Log.d("CartScreen", "Using fallback recommendations")
                listOf("French Fries", "Garlic Bread", "Coke", "Ice Cream", "Brownie")
                    .filter { fallback -> cartItemNames.none { it.equals(fallback, ignoreCase = true) } }
            } else {
                filteredRecommendations
            }

            // Return on main thread
            Handler(Looper.getMainLooper()).post {
                onResult(finalRecommendations)
            }

        } catch (e: Exception) {
            Log.e("CartScreen", "Error processing recommendations", e)
            Handler(Looper.getMainLooper()).post {
                onResult(emptyList())
            }
        }
    }.apply {
        isDaemon = true
        start()
    }
}
fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var insideQuotes = false

    for (char in line) {
        when {
            char == '"' -> insideQuotes = !insideQuotes
            char == ',' && !insideQuotes -> {
                result.add(current.toString().trim().removeSurrounding("\""))
                current = StringBuilder()
            }

            else -> current.append(char)
        }
    }

    // Add the last field
    if (current.isNotEmpty()) {
        result.add(current.toString().trim().removeSurrounding("\""))
    }

    return result
}

fun parseItems(itemsStr: String): List<String> {
    // If already looks like a list representation, parse it
    if (itemsStr.startsWith("[") && itemsStr.endsWith("]")) {
        return itemsStr.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"", "'") }
            .filter { it.isNotBlank() }
    }

    // Otherwise split by comma
    return itemsStr.split(",")
        .map { it.trim().removeSurrounding("\"", "'") }
        .filter { it.isNotBlank() }
}

@Composable
fun RecommendedItemCard(itemName: String) {
    if (itemName.isBlank()) {
        return // Skip rendering empty items
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .width(140.dp)
            .height(120.dp)
            .clickable {
                // Add item to cart logic could go here
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor =Color(0xFFFFC107))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = itemName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

