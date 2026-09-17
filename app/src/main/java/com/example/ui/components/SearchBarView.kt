package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationInfo
import com.example.ui.theme.AtmosphericBlue
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NightSky
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark

@Composable
fun SearchBarView(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    searchResults: List<LocationInfo>,
    onLocationSelected: (LocationInfo) -> Unit,
    onUseMyLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            onUseMyLocation()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Outlined futuristic search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onQueryChange(it)
                    if (!isSearching) onSearchActiveChange(true)
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("location_search_input"),
                placeholder = {
                    Text(
                        "Search city, state, country, or ZIP...",
                        color = AtmosphericBlue.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ElectricCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onQueryChange("")
                                onSearchActiveChange(false)
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = AtmosphericBlue
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NightSky.copy(alpha = 0.85f),
                    unfocusedContainerColor = NightSky.copy(alpha = 0.65f),
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceBorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = ElectricCyan
                )
            )

            // "Use My Location" quick button
            IconButton(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NightSky.copy(alpha = 0.85f))
                    .testTag("use_my_location_button")
                    .minimumInteractiveComponentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Use My Location",
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Search Results Dropdown List
        AnimatedVisibility(
            visible = isSearching && (searchResults.isNotEmpty() || searchQuery.isNotEmpty()),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceCardDark.copy(alpha = 0.95f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderDark)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Suggested Global Hubs" else "Matching Locations",
                        style = MaterialTheme.typography.labelSmall,
                        color = AtmosphericBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        modifier = Modifier.height(
                            minOf((searchResults.size * 54).dp, 220.dp)
                        )
                    ) {
                        items(searchResults) { loc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onLocationSelected(loc)
                                        onSearchActiveChange(false)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("search_result_${loc.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = loc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (loc.state.isNotEmpty()) "${loc.state}, ${loc.country} • ZIP ${loc.zipCode}"
                                               else "${loc.country} • ZIP ${loc.zipCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AtmosphericBlue.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
