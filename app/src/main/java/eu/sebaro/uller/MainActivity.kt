package eu.sebaro.uller

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.navigation.compose.composable
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import eu.sebaro.uller.data.getMockProductList
import eu.sebaro.uller.network.DataManager
import eu.sebaro.uller.network.getSubscriptionHandler
import kotlinx.coroutines.CoroutineExceptionHandler

class MainActivity : ComponentActivity() {

    private lateinit var dataManager: DataManager
    private lateinit var url: String
    private lateinit var token: String
    private var scrapedItems: ScrapItems by mutableStateOf(getMockProductList())
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        getFirebaseToken()
        dataManager = DataManager()
        updateProductList()

        setContent {
            val navController = rememberNavController()
            UllerTheme {
                Surface(
                    color = MaterialTheme.colors.surface,
                    contentColor = contentColorFor(MaterialTheme.colors.background)
                ) {
                    NavigationComponent(navController)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
    }

    @Composable
    fun NavigationComponent(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("details/{productName}") {
                val id = it.arguments?.getString("productName")
                if (id != null) {
                    DetailScreen(id, navController)
                }
            }
            composable("webview/") {
                if (url != null) {
                    WebViewScreen(url = url)
                }
            }
        }
    }

    @Composable
    fun HomeScreen(navController: NavController) {
        Scaffold(
            topBar = {
                HomeAppBar(
                    "Uller",
                    openSearch = { notYetDone() },
                    searchVisible = true,
                    startReload = { updateProductList() },
                    reloadVisible = true,
                    goBack = { onBackPressed() })
            },
            content = {
                ProductList(navController)
            }
        )
    }

    @Composable
    fun DetailScreen(id: String, navController: NavController) {
        Scaffold(modifier = Modifier.background(MaterialTheme.colors.background),
            topBar = {
                HomeAppBar(
                    "Uller",
                    openSearch = { notYetDone() },
                    startReload = { updateProductList() },
                    reloadVisible = true,
                    goBack = { onBackPressed() },
                    backButtonVisible = true
                )
            },
            content = {
                scrapedItems.products.products.forEach { product ->
                    if (product.name == id) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                                .background(MaterialTheme.colors.background),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        )
                        {
                            item {
                                Image(
                                    painter = painterResource(getCategoryImage(product)),
                                    contentDescription = "Image",
                                    modifier = Modifier
                                        .fillMaxWidth().padding(8.dp).height(250.dp)
                                )
                            }
                            product.variant.forEach {
                                it.shopList.forEach { shop ->
                                    item {
                                        Row(
                                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                                                .height(80.dp)
                                                .clickable {
                                                    url = shop.url
                                                    navController.navigate("webview/")
                                                }
                                                .background(MaterialTheme.colors.primary),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            val favorite = dataManager.isSubscribed(it.name)
                                            Text(
                                                text = shop.name,
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .fillMaxWidth()
                                                    .align(Alignment.CenterVertically)
                                                    .weight(0.4f)
                                                    .clickable {
                                                        url = shop.url
                                                        navController.navigate("webview/")
                                                    }
                                            )
                                            Text(
                                                text = (if (shop.available) "Verfügbar" else "nicht verfügbar"),
                                                maxLines = 2,
                                                modifier = Modifier
                                                    //.background(Color.Cyan)
                                                    .weight(0.2f)
                                                    .align(Alignment.CenterVertically)
                                                    .clickable {
                                                        notYetDone(shop.url)
                                                    }
                                            )
                                            Image(
                                                painter = painterResource(if (shop.available) R.drawable.ic_available else R.drawable.ic_not_available),
                                                colorFilter = ColorFilter.tint((if (shop.available) Color.Green else Color.Red)),
                                                contentDescription = "Image",
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .weight(0.2f)
                                            )
                                        }

                                    }

                                }

                            }
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun ProductList(navController: NavController) {
        LazyColumn {
            items(scrapedItems.products.products) { product ->
                Row {
                    Card(
                        elevation = 4.dp,
                        backgroundColor = MaterialTheme.colors.secondary,
                        modifier = Modifier.padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .height(80.dp)
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("details/${product.name}")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp).fillMaxWidth()
                                .background(MaterialTheme.colors.primary)
                                .align(alignment = Alignment.CenterVertically),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Image(
                                painterResource(id = getCategoryImage(product)),
                                "Image",
                                modifier = Modifier.padding(8.dp).weight(0.3f)
                            )
                            val favorite = dataManager.isSubscribed(product.name)
                            Text(
                                product.name,
                                modifier = Modifier.align(alignment = Alignment.CenterVertically)
                                    .weight(0.5f),

                                )
                            Image(
                                painter = painterResource(if (favorite) R.drawable.ic_bookmark_added else R.drawable.ic_bookmark_add),
                                colorFilter = ColorFilter.tint((if (favorite) Color.Green else Color.LightGray)),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .weight(0.2f)
                                    .clickable {
                                        if (favorite) {
                                            dataManager.unSubscribe(product.name)
                                            lifecycleScope.launch(getSubscriptionHandler()) {
                                                dataManager.handleSubscription(
                                                    product.variant[0].productId.toString(),
                                                    token,
                                                    false
                                                )
                                            }
                                        } else {
                                            dataManager.subscribe(
                                                product.name
                                            )
                                            lifecycleScope.launch(getSubscriptionHandler()) {
                                                dataManager.handleSubscription(
                                                    product.variant[0].productId.toString(),
                                                    token,
                                                    true
                                                )
                                            }
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HomeAppBar(
        title: String,
        openSearch: () -> Unit,
        startReload: () -> Unit,
        goBack: () -> Unit,
        reloadVisible: Boolean? = false,
        searchVisible: Boolean? = false,
        backButtonVisible: Boolean? = false
    ) {
        TopAppBar(
            backgroundColor = MaterialTheme.colors.background,
            contentColor = contentColorFor(MaterialTheme.colors.onBackground),
            title = { Text(text = title, color = MaterialTheme.colors.onBackground) },
            actions = {
                if (searchVisible == true) {
                    IconButton(onClick = openSearch) {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                    }
                }

                if (reloadVisible == true) {
                    IconButton(onClick = startReload) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Filter")
                    }
                }
                if (backButtonVisible == true) {
                    IconButton(onClick = goBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            }
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewScreen(url: String) {
        Scaffold(modifier = Modifier.background(MaterialTheme.colors.background),
            topBar = {
                HomeAppBar(
                    "Uller",
                    openSearch = { notYetDone() },
                    startReload = { updateProductList() },
                    goBack = { onBackPressed() },
                    backButtonVisible = true
                )
            },
            content = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(MaterialTheme.colors.primary)
                    ) {
                        Text(
                            text = "WebView Page",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        AndroidView(
                            factory = {
                                WebView(it).apply {
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            return false
                                        }
                                    }
                                }
                            }, update = {
                                it.settings.javaScriptEnabled = true
                                it.loadUrl(url)
                            }
                        )
                    }
                }
            })
    }

    fun notYetDone(text: String? = "not yet done") {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }


    private fun updateProductList() {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Server is Offline => MockData loaded $exception")
            scrapedItems = getMockProductList()
        }
        lifecycleScope.launch(handler) {
            scrapedItems = dataManager.requestProductList()
        }
    }

    private fun getFirebaseToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            token = task.result
            Log.d(TAG, token)
            Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
        })
    }
}


