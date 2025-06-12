package com.example.foodiemock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodiemock.ui.theme.FoodieMockTheme

// Mock Data Model definitions

data class Restaurant(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val description: String,
    val menu: List<MenuItem>
)

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageRes: Int
)

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

// The actual MainActivity uses Jetpack Compose for modern UI design

class MainActivity : ComponentActivity() {
    // PUBLIC_INTERFACE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the Compose content & provide mock data to the main app view
        setContent {
            FoodieMockTheme(
                primaryColor = Color(0xFFFF5722),
                secondaryColor = Color(0xFFFFFFFF),
                accentColor = Color(0xFF4CAF50)
            ) {
                FoodieMockApp()
            }
        }
    }
}

@Composable
fun FoodieMockApp() {
    // Navigation stack: "list", "menu", "cart", "confirm"
    var screen by remember { mutableStateOf("list") }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var cart by remember { mutableStateOf(listOf<CartItem>()) }

    val restaurants = remember { getMockRestaurants() }

    // Root scaffold for theming and cart navigation
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FoodieMock", color = Color.White) },
                backgroundColor = Color(0xFFFF5722),
                actions = {
                    IconButton(onClick = { screen = "cart" }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                        if (cart.isNotEmpty()) {
                            Text(
                                text = "${cart.sumOf { it.quantity }}",
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xFF4CAF50), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier
            .padding(it)
            .background(Color.White)
            .fillMaxSize()
        ) {
            when (screen) {
                "list" -> RestaurantList(
                    restaurants = restaurants,
                    onRestaurantClick = { restaurant ->
                        selectedRestaurant = restaurant
                        screen = "menu"
                    }
                )
                "menu" -> {
                    selectedRestaurant?.let { restaurant ->
                        MenuScreen(
                            restaurant = restaurant,
                            cart = cart,
                            onAddToCart = { menuItem ->
                                cart = addMenuItemToCart(cart, menuItem)
                            },
                            onBack = { screen = "list" }
                        )
                    }
                }
                "cart" -> {
                    CartScreen(
                        cart = cart,
                        onIncrease = { menuItem -> cart = addMenuItemToCart(cart, menuItem) },
                        onDecrease = { menuItem -> cart = removeMenuItemFromCart(cart, menuItem) },
                        onOrder = { screen = "confirm" },
                        onBack = {
                            if (selectedRestaurant != null) screen = "menu" else screen = "list"
                        }
                    )
                }
                "confirm" -> {
                    OrderSummaryScreen(
                        cart = cart,
                        onConfirm = {
                            // For mock order, just clear the cart and go back home
                            cart = listOf()
                            selectedRestaurant = null
                            screen = "list"
                        }
                    )
                }
            }
        }
    }
}

fun addMenuItemToCart(cart: List<CartItem>, menuItem: MenuItem): List<CartItem> {
    val mutableCart = cart.toMutableList()
    val index = mutableCart.indexOfFirst { it.menuItem.id == menuItem.id }
    if (index != -1) {
        val item = mutableCart[index]
        mutableCart[index] = item.copy(quantity = item.quantity + 1)
    } else {
        mutableCart.add(CartItem(menuItem, 1))
    }
    return mutableCart
}

fun removeMenuItemFromCart(cart: List<CartItem>, menuItem: MenuItem): List<CartItem> {
    val mutableCart = cart.toMutableList()
    val index = mutableCart.indexOfFirst { it.menuItem.id == menuItem.id }
    if (index != -1) {
        val item = mutableCart[index]
        if (item.quantity > 1) {
            mutableCart[index] = item.copy(quantity = item.quantity - 1)
        } else {
            mutableCart.removeAt(index)
        }
    }
    return mutableCart
}

// PUBLIC_INTERFACE
@Composable
fun RestaurantList(
    restaurants: List<Restaurant>,
    onRestaurantClick: (Restaurant) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .background(Color.White)
            .padding(8.dp)
    ) {
        items(restaurants.size) { idx ->
            RestaurantCard(
                restaurant = restaurants[idx],
                onClick = { onRestaurantClick(restaurants[idx]) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(12.dp)
        ) {
            Image(
                painter = painterResource(id = restaurant.imageRes),
                contentDescription = "${restaurant.name} image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFFF5722))
                Text(restaurant.description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

// PUBLIC_INTERFACE
@Composable
fun MenuScreen(
    restaurant: Restaurant,
    cart: List<CartItem>,
    onAddToCart: (MenuItem) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier
        .background(Color.White)
        .fillMaxSize()
    ) {
        Text(
            text = restaurant.name + " Menu",
            color = Color(0xFFFF5722),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(restaurant.menu.size) { idx ->
                MenuItemCard(menuItem = restaurant.menu[idx], onAddToCart = onAddToCart)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(12.dp)
                .fillMaxWidth(0.6f)
        ) {
            Text("Back to Restaurants", color = Color.White)
        }
    }
}

@Composable
fun MenuItemCard(menuItem: MenuItem, onAddToCart: (MenuItem) -> Unit) {
    Card(
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(10.dp)
        ) {
            Image(
                painter = painterResource(id = menuItem.imageRes),
                contentDescription = "${menuItem.name} image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(menuItem.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFFF5722))
                Text(menuItem.description, fontSize = 13.sp, color = Color.Gray)
                Text("$${String.format("%.2f", menuItem.price)}", fontSize = 15.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onAddToCart(menuItem) },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text("Add", color = Color.White)
            }
        }
    }
}

// PUBLIC_INTERFACE
@Composable
fun CartScreen(
    cart: List<CartItem>,
    onIncrease: (MenuItem) -> Unit,
    onDecrease: (MenuItem) -> Unit,
    onOrder: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Text(
            "Your Cart",
            color = Color(0xFFFF5722),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(12.dp)
        )
        if (cart.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty.", color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart.size) { idx ->
                    CartItemRow(
                        cartItem = cart[idx],
                        onIncrease = { onIncrease(cart[idx].menuItem) },
                        onDecrease = { onDecrease(cart[idx].menuItem) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            val total = cart.sumOf { it.menuItem.price * it.quantity }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total: $${String.format("%.2f", total)}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 18.sp)
                Button(
                    onClick = onOrder,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF5722)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Order", color = Color.White)
                }
            }
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
                    .fillMaxWidth(0.5f)
            ) {
                Text("Continue Shopping", color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun CartItemRow(cartItem: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 6.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = cartItem.menuItem.imageRes),
            contentDescription = "",
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(cartItem.menuItem.name, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFFFF5722))
            Text("$${String.format("%.2f", cartItem.menuItem.price)}", color = Color(0xFF4CAF50), fontSize = 13.sp)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDecrease, enabled = cartItem.quantity > 0) {
                Text("-", fontSize = 18.sp, color = Color.Red)
            }
            Text("${cartItem.quantity}", modifier = Modifier.width(22.dp), fontSize = 16.sp, color = Color.Black)
            IconButton(onClick = onIncrease) {
                Text("+", fontSize = 18.sp, color = Color(0xFF4CAF50))
            }
        }
    }
}

// PUBLIC_INTERFACE
@Composable
fun OrderSummaryScreen(
    cart: List<CartItem>,
    onConfirm: () -> Unit
) {
    val total = cart.sumOf { it.menuItem.price * it.quantity }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Order Summary",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = Color(0xFFFF5722),
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            items(cart.size) { idx ->
                val item = cart[idx]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.menuItem.name} x${item.quantity}", color = Color.Gray, fontSize = 16.sp)
                    Text("$${String.format("%.2f", item.menuItem.price * item.quantity)}",
                        color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        }
        Text("Total: $${String.format("%.2f", total)}",
            fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 20.sp, modifier = Modifier.padding(10.dp))
        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Text("Confirm Order!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Text("Thank you for using FoodieMock!", modifier = Modifier.padding(bottom = 22.dp), color = Color.Gray)
    }
}

// ------------------- MOCK DATA SECTION ----------------------

fun getMockRestaurants(): List<Restaurant> {
    // Using built-in drawable resources for illustration; replace with your assets as needed.
    // All menu images also reuse the same images for mockup.
    val demoBurger = MenuItem(
        id = 1, name = "Classic Burger",
        description = "Juicy beef patty, cheese, lettuce, and tomato on a toasted bun.",
        price = 7.99,
        imageRes = android.R.drawable.ic_menu_gallery
    )
    val demoPizza = MenuItem(
        id = 2, name = "Pepperoni Pizza",
        description = "12-inch pizza, loaded with pepperoni and mozzarella.",
        price = 12.49,
        imageRes = android.R.drawable.ic_menu_gallery
    )
    val demoFries = MenuItem(
        id = 3, name = "Fries",
        description = "Crispy golden fries with ketchup.",
        price = 2.99,
        imageRes = android.R.drawable.ic_menu_gallery
    )
    val demoNoodles = MenuItem(
        id = 4, name = "Stir-fried Noodles",
        description = "Egg noodles with veggies and chicken in special sauce.",
        price = 8.50,
        imageRes = android.R.drawable.ic_menu_gallery
    )
    val demoSushi = MenuItem(
        id = 5, name = "Salmon Sushi Roll",
        description = "Fresh salmon rolls with rice, seaweed, and wasabi.",
        price = 10.00,
        imageRes = android.R.drawable.ic_menu_gallery
    )

    return listOf(
        Restaurant(
            id = 100,
            name = "Burger Bistro",
            imageRes = android.R.drawable.ic_menu_camera,
            description = "Burgers, fries & shakes. Quick bites and great taste.",
            menu = listOf(demoBurger, demoFries)
        ),
        Restaurant(
            id = 200,
            name = "Pizza Plaza",
            imageRes = android.R.drawable.ic_menu_report_image,
            description = "Wood-fired pizzas, pasta and salads.",
            menu = listOf(demoPizza, demoFries)
        ),
        Restaurant(
            id = 300,
            name = "Asian Delights",
            imageRes = android.R.drawable.ic_menu_slideshow,
            description = "Noodles, rice bowls and fresh sushi rolls.",
            menu = listOf(demoSushi, demoNoodles)
        )
    )
}
