package com.example.atmoscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.atmoscope.ui.screens.CityManagementScreen
import com.example.atmoscope.ui.screens.LoginScreen
import com.example.atmoscope.ui.screens.MainWeatherScreen
import com.example.atmoscope.ui.screens.SettingsScreen
import com.example.atmoscope.ui.screens.SplashScreen
import com.example.atmoscope.ui.theme.AtmoscopeTheme
import com.example.atmoscope.viewmodel.WeatherViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.example.atmoscope.data.LoginRequest
import com.example.atmoscope.data.RetrofitClient
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WeatherViewModel = viewModel()
            val isDark by viewModel.isDarkTheme.collectAsState()

            AtmoscopeTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "splash"
                ) {
                    composable("splash") {
                        SplashScreen(onFinish = {
                            // Berubah: Setelah splash selesai, arahkan ke login
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }

                    composable("login") {
                        val coroutineScope = rememberCoroutineScope()
                        val context = LocalContext.current // Untuk menampilkan toast error/sukses

                        LoginScreen(
                            onLoginClick = { email, password ->
                                // Menjalankan fungsi suspend di dalam Coroutine
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.instance.loginUser(LoginRequest(email, password))
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, "Login Sukses!", Toast.LENGTH_SHORT).show()

                                            // Pindah ke halaman utama cuaca jika berhasil
                                            navController.navigate("main") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            // Tampilkan pesan gagal dari PHP (misal: password salah)
                                            val errorMsg = response.body()?.message ?: "Login Gagal"
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        // Menangani kendala koneksi / server mati
                                        Toast.makeText(context, "Tidak dapat terhubung ke server: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onRegisterClick = {
                                // Tempat navigasi register nanti
                            }
                        )
                    }

                    composable("main") {
                        MainWeatherScreen(
                            viewModel = viewModel,
                            onNavigateToCityManagement = {
                                navController.navigate("cities")
                            },
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("cities") {
                        CityManagementScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onCitySelected = { city ->
                                viewModel.fetchWeather(city)
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}