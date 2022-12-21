package com.example.weatherapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.service.dto.CurrentWeather
import com.example.weatherapp.service.dto.FullWeather
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.ui.theme.cloudyBlue
import com.example.weatherapp.ui.theme.rainyGrey
import com.example.weatherapp.ui.theme.sunnyGreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


@AndroidEntryPoint
@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                val permission = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

                PermissionRequired(
                    permissionState = permission,
                    permissionNotGrantedContent = { LocationPermissionDetails(onContinueClick = permission::launchPermissionRequest) },
                    permissionNotAvailableContent = { LocationPermissionNotAvailable(onContinueClick = permission::launchPermissionRequest) }
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val current by viewModel.current.collectAsState(null)
    val forecast by viewModel.forecast.collectAsState(emptyList())

    current?.let {
        rememberSystemUiController().setStatusBarColor(it.backgroundColour())
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(current?.backgroundColour() ?: Color.White)
    ) {
        current?.let {
            WeatherSummary(weather = it)
            TemperatureSummary(weather = it)
            Divider(color = Color.White)
            Spacer(modifier = Modifier.height(5.dp))
        }
        FiveDayForecast(forecast)
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun WeatherSummary(weather: CurrentWeather) {
    Box {
        Image(
            painter = painterResource(id = weather.background()),
            contentDescription = "Background",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
        Column(
            Modifier
                .padding(top = 48.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = formatTemperature(weather.main.temp), fontSize = 48.sp, color = Color.White)
            Text(text = weather.weather.first().main, fontSize = 28.sp, color = Color.White)
            Text(text = weather.name, fontSize = 18.sp, color = Color.White)
        }
    }
}

@Composable
fun TemperatureSummary(weather: CurrentWeather) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTemperature(weather.main.tempMin),
                fontSize = 18.sp,
                color = Color.White
            )
            Text(text = stringResource(R.string.min_temperature), color = Color.White)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTemperature(weather.main.temp),
                fontSize = 18.sp,
                color = Color.White
            )
            Text(text = stringResource(R.string.now_temperature), color = Color.White)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTemperature(weather.main.tempMax),
                fontSize = 18.sp,
                color = Color.White
            )
            Text(text = stringResource(R.string.max_temperature), color = Color.White)
        }
    }
}

@Composable
fun FiveDayForecast(forecast: List<FullWeather.Daily>) {
    LazyColumn {
        items(forecast) { dayForecast ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            ) {
                Text(
                    text = SimpleDateFormat("EEEE").format(Date(dayForecast.dt * 1_000)),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Image(
                    painter = painterResource(dayForecast.forecastIcon()),
                    contentDescription = "Forecast icon",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center)
                )
                Text(
                    text = formatTemperature(dayForecast.temp.day),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun formatTemperature(temperature: Double): String {
    return stringResource(R.string.temperature_degrees, temperature.roundToInt())
}

@DrawableRes
private fun CurrentWeather.background(): Int {
    val conditions = weather.first().main
    return when {
        conditions.contains("cloud", ignoreCase = true) -> R.drawable.forest_cloudy
        conditions.contains("rain", ignoreCase = true) -> R.drawable.forest_rainy
        else -> R.drawable.forest_sunny
    }
}

private fun CurrentWeather.backgroundColour(): Color {
    val conditions = weather.first().main
    return when {
        conditions.contains("cloud", ignoreCase = true) -> cloudyBlue
        conditions.contains("rain", ignoreCase = true) -> rainyGrey
        else -> sunnyGreen
    }
}

private fun FullWeather.Daily.forecastIcon(): Int {
    val conditions = weather.first().main
    return when {
        conditions.contains("cloud", ignoreCase = true) -> R.drawable.partly
        conditions.contains("rain", ignoreCase = true) -> R.drawable.rain
        else -> R.drawable.clear_sky
    }
}