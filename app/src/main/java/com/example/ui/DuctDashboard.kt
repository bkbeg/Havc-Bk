package com.example.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DuctCalculation
import com.example.data.DuctType
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuctDashboard(
    viewModel: DuctViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Duct Fabricator, 1: Saved History, 2: Formula Sheet

    // Live state bindings from the dynamic technical view model
    val selectedType by viewModel.selectedDuctType.collectAsState()
    val width by viewModel.width.collectAsState()
    val height by viewModel.height.collectAsState()
    val length by viewModel.length.collectAsState()
    val extra1 by viewModel.extra1.collectAsState()
    val extra2 by viewModel.extra2.collectAsState()
    val gauge by viewModel.selectedGauge.collectAsState()
    val quantity by viewModel.quantity.collectAsState()
    val calcName by viewModel.calcName.collectAsState()
    val savedHistory by viewModel.history.collectAsState()

    // Calculate Area ($m^2$) and Weight (kg) based on industrial sheet constants
    val (liveArea, liveWeight) = viewModel.calculateAreaAndWeight(
        selectedType, width, height, length, extra1, extra2, gauge, quantity
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BentoCardPurpleLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Duct Tool Icon",
                                tint = BentoCardPurpleOnContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Duct Master Draw",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BentoTextPrimary
                            )
                            Text(
                                text = "Fabricator Sheet Metal Guide",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.applyPresetDimensions(600.0, 700.0, 900.0)
                        },
                        modifier = Modifier.testTag("reset_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Standard Preset",
                            tint = BentoCardPurpleDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoSurface
                ),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = BentoBorder,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BentoSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .drawBehind {
                        drawLine(
                            color = BentoBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Calculator Icon") },
                    label = { Text("Fabricator", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = BentoCardPurpleDark,
                        indicatorColor = BentoCardPurpleDark,
                        unselectedIconColor = BentoTextSecondary,
                        unselectedTextColor = BentoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_calculator")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (savedHistory.isNotEmpty()) {
                                Badge(containerColor = BentoCardPink) {
                                    Text(
                                        savedHistory.size.toString(),
                                        color = Color(0xFF31111D),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }) {
                            Icon(Icons.Default.List, contentDescription = "Save History Icon")
                        }
                    },
                    label = { Text("Logs", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = BentoCardPurpleDark,
                        indicatorColor = BentoCardPurpleDark,
                        unselectedIconColor = BentoTextSecondary,
                        unselectedTextColor = BentoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_history")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Formulas Icon") },
                    label = { Text("Formulas", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = BentoCardPurpleDark,
                        indicatorColor = BentoCardPurpleDark,
                        unselectedIconColor = BentoTextSecondary,
                        unselectedTextColor = BentoTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_formulas")
                )
            }
        },
        containerColor = BentoBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val isWide = maxWidth >= 800.dp

            AnimatedVisibility(
                visible = activeTab == 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (isWide) {
                    // Split screen side-by-side for tablets / desktop viewports
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: Type selector & Inputs
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDuctTypeSelector(
                                selected = selectedType,
                                onTypeSelect = { viewModel.updateDuctType(it) }
                            )

                            InputsCard(
                                type = selectedType,
                                width = width,
                                height = height,
                                length = length,
                                extra1 = extra1,
                                extra2 = extra2,
                                quantity = quantity,
                                calcName = calcName,
                                onWidthChange = { viewModel.updateWidth(it) },
                                onHeightChange = { viewModel.updateHeight(it) },
                                onLengthChange = { viewModel.updateLength(it) },
                                onExtra1Change = { viewModel.updateExtra1(it) },
                                onExtra2Change = { viewModel.updateExtra2(it) },
                                onQtyChange = { viewModel.updateQuantity(it) },
                                onNameChange = { viewModel.updateName(it) },
                                selectedGauge = gauge,
                                onGaugeChange = { viewModel.updateGauge(it) },
                                onSave = { viewModel.saveCalculation() }
                            )
                        }

                        // Right Column: Canvas drawing and results preview
                        Column(
                            modifier = Modifier
                                .weight(1.3f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ResultsHeaderCard(
                                liveArea = liveArea,
                                liveWeight = liveWeight,
                                gauge = gauge,
                                qty = quantity,
                                type = selectedType
                            )

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                                border = BorderStroke(1.dp, BlueprintCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "MASTER DRAW - Blueprints & Folds",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = BlueprintPrimary,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                            .border(1.dp, BlueprintGrid.copy(alpha = 0.5f))
                                    ) {
                                        BlueprintRenderer(
                                            type = selectedType,
                                            w = width,
                                            h = height,
                                            l = length,
                                            e1 = extra1,
                                            e2 = extra2
                                        )
                                    }
                                }
                            }

                            TrickCard(type = selectedType)
                        }
                    }
                } else {
                    // Mobile-first scrolling stack
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            HorizontalDuctTypeSelector(
                                selected = selectedType,
                                onTypeSelect = { viewModel.updateDuctType(it) }
                            )
                        }

                        item {
                            ResultsHeaderCard(
                                liveArea = liveArea,
                                liveWeight = liveWeight,
                                gauge = gauge,
                                qty = quantity,
                                type = selectedType
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                                border = BorderStroke(1.dp, BlueprintCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "CAD DRAWING PLATFORM",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = BlueprintPrimary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                    ) {
                                        BlueprintRenderer(
                                            type = selectedType,
                                            w = width,
                                            h = height,
                                            l = length,
                                            e1 = extra1,
                                            e2 = extra2
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            InputsCard(
                                type = selectedType,
                                width = width,
                                height = height,
                                length = length,
                                extra1 = extra1,
                                extra2 = extra2,
                                quantity = quantity,
                                calcName = calcName,
                                onWidthChange = { viewModel.updateWidth(it) },
                                onHeightChange = { viewModel.updateHeight(it) },
                                onLengthChange = { viewModel.updateLength(it) },
                                onExtra1Change = { viewModel.updateExtra1(it) },
                                onExtra2Change = { viewModel.updateExtra2(it) },
                                onQtyChange = { viewModel.updateQuantity(it) },
                                onNameChange = { viewModel.updateName(it) },
                                selectedGauge = gauge,
                                onGaugeChange = { viewModel.updateGauge(it) },
                                onSave = { viewModel.saveCalculation() }
                            )
                        }

                        item {
                            TrickCard(type = selectedType)
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = activeTab == 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HistoryPanel(
                    savedList = savedHistory,
                    onLoad = {
                        viewModel.loadCalculation(it)
                        activeTab = 0 // return to fabricator
                    },
                    onDelete = { viewModel.deleteCalculation(it) },
                    onClearAll = { viewModel.clearAll() }
                )
            }

            AnimatedVisibility(
                visible = activeTab == 2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FormulasManualPanel()
            }
        }
    }
}

@Composable
fun HorizontalDuctTypeSelector(
    selected: DuctType,
    onTypeSelect: (DuctType) -> Unit
) {
    Column {
        Text(
            text = "DUCT TYPE LIBRARY",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = BlueprintPrimary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DuctType.values()) { item ->
                val isSelected = item == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BlueprintPrimary else BlueprintSurface)
                        .border(
                            1.dp,
                            if (isSelected) BlueprintPrimary else BlueprintCardBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onTypeSelect(item) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("duct_type_select_${item.id}")
                ) {
                    Text(
                        text = item.displayName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.Black else BlueprintTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun ResultsHeaderCard(
    liveArea: Double,
    liveWeight: Double,
    gauge: String,
    qty: Int,
    type: DuctType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
        border = BorderStroke(1.dp, BlueprintCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(BlueprintStatusOk)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE SHEET CALCULATION",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = BlueprintStatusOk,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${type.displayName} (Qty: $qty)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintTextPrimary
                )
                Text(
                    text = "Gauge: $gauge",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = BlueprintTextSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Area Glow Card
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlueprintCardBorder.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TOTAL AREA",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = BlueprintTextSecondary
                    )
                    Text(
                        text = String.format("%.3f m²", liveArea),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BlueprintPrimary
                    )
                }

                // Weight Glow Card
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BlueprintCardBorder.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "TOTAL WEIGHT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = BlueprintTextSecondary
                    )
                    Text(
                        text = String.format("%.2f kg", liveWeight),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = BlueprintAccent
                    )
                }
            }
        }
    }
}

@Composable
fun InputsCard(
    type: DuctType,
    width: Double,
    height: Double,
    length: Double,
    extra1: Double,
    extra2: Double,
    quantity: Int,
    calcName: String,
    onWidthChange: (Double) -> Unit,
    onHeightChange: (Double) -> Unit,
    onLengthChange: (Double) -> Unit,
    onExtra1Change: (Double) -> Unit,
    onExtra2Change: (Double) -> Unit,
    onQtyChange: (Int) -> Unit,
    onNameChange: (String) -> Unit,
    selectedGauge: String,
    onGaugeChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
        border = BorderStroke(1.dp, BlueprintCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LAYOUT TECHNICAL PARAMETERS (mm)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BlueprintPrimary
            )

            // Dynamic Form Field Grid
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Width & Height Side-by-Side
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = if (width == 0.0) "" else width.roundToInt().toString(),
                        onValueChange = { onWidthChange(it.toDoubleOrNull() ?: 0.0) },
                        label = { Text("Width A (mm)", color = BlueprintTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueprintPrimary,
                            unfocusedBorderColor = BlueprintCardBorder,
                            focusedTextColor = BlueprintTextPrimary,
                            unfocusedTextColor = BlueprintTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_width")
                    )

                    OutlinedTextField(
                        value = if (height == 0.0) "" else height.roundToInt().toString(),
                        onValueChange = { onHeightChange(it.toDoubleOrNull() ?: 0.0) },
                        label = { Text("Height B (mm)", color = BlueprintTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueprintPrimary,
                            unfocusedBorderColor = BlueprintCardBorder,
                            focusedTextColor = BlueprintTextPrimary,
                            unfocusedTextColor = BlueprintTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_height")
                    )
                }

                // Length Field & Quantity
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = if (length == 0.0) "" else length.roundToInt().toString(),
                        onValueChange = { onLengthChange(it.toDoubleOrNull() ?: 0.0) },
                        label = { Text("Length L (mm)", color = BlueprintTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueprintPrimary,
                            unfocusedBorderColor = BlueprintCardBorder,
                            focusedTextColor = BlueprintTextPrimary,
                            unfocusedTextColor = BlueprintTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_length")
                    )

                    OutlinedTextField(
                        value = quantity.toString(),
                        onValueChange = { onQtyChange(it.toIntOrNull() ?: 1) },
                        label = { Text("Quantity (Qty)", color = BlueprintTextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueprintPrimary,
                            unfocusedBorderColor = BlueprintCardBorder,
                            focusedTextColor = BlueprintTextPrimary,
                            unfocusedTextColor = BlueprintTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_qty")
                    )
                }

                // Conditional Duct Parameters
                if (type.defaultExtra1Label != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = if (extra1 == 0.0) "" else extra1.roundToInt().toString(),
                            onValueChange = { onExtra1Change(it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(type.defaultExtra1Label, color = BlueprintTextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueprintPrimary,
                                unfocusedBorderColor = BlueprintCardBorder,
                                focusedTextColor = BlueprintTextPrimary,
                                unfocusedTextColor = BlueprintTextPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_extra1")
                        )

                        if (type.defaultExtra2Label != null) {
                            OutlinedTextField(
                                value = if (extra2 == 0.0) "" else extra2.roundToInt().toString(),
                                onValueChange = { onExtra2Change(it.toDoubleOrNull() ?: 0.0) },
                                label = { Text(type.defaultExtra2Label, color = BlueprintTextSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BlueprintPrimary,
                                    unfocusedBorderColor = BlueprintCardBorder,
                                    focusedTextColor = BlueprintTextPrimary,
                                    unfocusedTextColor = BlueprintTextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_extra2")
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f)) // spacer
                        }
                    }
                }

                Divider(color = BlueprintCardBorder, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Steel Sheet Quality Selection & Name
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val gauges = listOf(
                        "26G (0.55mm)",
                        "24G (0.70mm)",
                        "22G (0.85mm)",
                        "20G (1.00mm)",
                        "18G (1.20mm)"
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedGauge,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Sheet Gauge", color = BlueprintTextSecondary) },
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Gauge", tint = BlueprintPrimary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueprintCardBorder,
                                unfocusedBorderColor = BlueprintCardBorder,
                                focusedTextColor = BlueprintTextPrimary,
                                unfocusedTextColor = BlueprintTextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(BlueprintSurface)
                        ) {
                            gauges.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g, color = BlueprintTextPrimary, fontFamily = FontFamily.Monospace) },
                                    onClick = {
                                        onGaugeChange(g)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = calcName,
                        onValueChange = onNameChange,
                        label = { Text("Section Label", color = BlueprintTextSecondary) },
                        placeholder = { Text("e.g. Lobby Return Joint") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BlueprintPrimary,
                            unfocusedBorderColor = BlueprintCardBorder,
                            focusedTextColor = BlueprintTextPrimary,
                            unfocusedTextColor = BlueprintTextPrimary
                        ),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("input_label")
                    )
                }
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = BlueprintPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_calculation_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save Layout",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE CALCULATION TO LOG",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun TrickCard(type: DuctType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
        border = BorderStroke(1.dp, BlueprintCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tips & Tricks",
                    tint = BlueprintAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fabricator Pro Marking Trick:",
                    style = MaterialTheme.typography.titleSmall,
                    color = BlueprintAccent,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = type.trickTitle,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = BlueprintTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = type.trickContent,
                style = MaterialTheme.typography.bodySmall,
                color = BlueprintTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun BlueprintRenderer(
    type: DuctType,
    w: Double,
    h: Double,
    l: Double,
    e1: Double,
    e2: Double
) {
    val w = w.coerceAtLeast(0.0)
    val h = h.coerceAtLeast(0.0)
    val l = l.coerceAtLeast(0.0)
    val e1 = e1.coerceAtLeast(0.0)
    val e2 = e2.coerceAtLeast(0.0)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val widthCanvas = size.width
        val heightCanvas = size.height

        // Draw blueprint grid lines in background
        val gridStep = 40f
        for (x in 0..(widthCanvas / gridStep).toInt()) {
            drawLine(
                color = BlueprintGrid,
                start = Offset(x * gridStep, 0f),
                end = Offset(x * gridStep, heightCanvas),
                strokeWidth = 1f
            )
        }
        for (y in 0..(heightCanvas / gridStep).toInt()) {
            drawLine(
                color = BlueprintGrid,
                start = Offset(0f, y * gridStep),
                end = Offset(widthCanvas, y * gridStep),
                strokeWidth = 1f
            )
        }

        // Draw corner crosshairs to make it look like a real draft document
        drawCornerCrosshairs(widthCanvas, heightCanvas)

        val center = Offset(widthCanvas / 2f, heightCanvas / 2f)

        // Draw duct type dependent graphics
        when (type) {
            DuctType.STRAIGHT -> {
                // Draw 4-Plate Wrap Layout sheet
                val scale = (widthCanvas * 0.7f) / (w * 2 + h * 2).toFloat().coerceAtLeast(1f)
                val totalWidth = (w * 2 + h * 2).toFloat() * scale
                val sheetL = l.toFloat() * scale

                val startX = center.x - totalWidth / 2f
                val startY = center.y - sheetL / 2f

                // Outer cutout boundary (solid cyan line)
                drawRect(
                    color = BlueprintLine,
                    topLeft = Offset(startX, startY),
                    size = Size(totalWidth, sheetL),
                    style = Stroke(width = 3f)
                )

                // Fold lines (dashed amber lines)
                val wFold = w.toFloat() * scale
                val hFold = h.toFloat() * scale

                val valFoldLocations = listOf(wFold, wFold + hFold, wFold * 2 + hFold)
                valFoldLocations.forEach { offset ->
                    drawDashedVerticalLine(
                        color = BlueprintAccent,
                        x = startX + offset,
                        startY = startY,
                        endY = startY + sheetL
                    )
                }

                // Annotation text
                drawTextOnBlueprint(
                    "4-SIDE WRAP SHEET (A + B + A + B)",
                    Offset(center.x, startY - 12f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "Width folds",
                    Offset(startX + wFold/2, startY + 20f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintTextSecondary
                )
                drawTextOnBlueprint(
                    "Height folds",
                    Offset(startX + wFold + hFold/2, startY + 20f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintTextSecondary
                )
            }

            DuctType.ELBOW_90 -> {
                // Draw 90 Degree Cheek Plate layout
                val innerR = e1.toFloat()
                val ductW = w.toFloat()
                val radiusOut = innerR + ductW
                val scale = (widthCanvas * 0.7f) / radiusOut.coerceAtLeast(1f)

                val scaledInner = innerR * scale
                val scaledOuter = radiusOut * scale

                val origin = Offset(center.x - scaledOuter / 2f, center.y + scaledOuter / 2f)

                // Draw solid backing cheek boundaries
                drawArc(
                    color = BlueprintLine,
                    startAngle = -90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(origin.x - scaledOuter, origin.y - scaledOuter),
                    size = Size(scaledOuter * 2, scaledOuter * 2),
                    style = Stroke(width = 4f)
                )

                drawArc(
                    color = BlueprintLine,
                    startAngle = -90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(origin.x - scaledInner, origin.y - scaledInner),
                    size = Size(scaledInner * 2, scaledInner * 2),
                    style = Stroke(width = 4f)
                )

                // Draw connector lines
                drawLine(
                    color = BlueprintLine,
                    start = Offset(origin.x, origin.y - scaledInner),
                    end = Offset(origin.x, origin.y - scaledOuter),
                    strokeWidth = 4f
                )
                drawLine(
                    color = BlueprintLine,
                    start = Offset(origin.x + scaledInner, origin.y),
                    end = Offset(origin.x + scaledOuter, origin.y),
                    strokeWidth = 4f
                )

                // Marking layout squares (dashed yellow)
                drawDashedRect(
                    color = BlueprintAccent.copy(alpha = 0.6f),
                    topLeft = origin + Offset(0f, -scaledOuter),
                    size = Size(scaledOuter, scaledOuter)
                )

                drawTextOnBlueprint(
                    "CHEEK PLATE LAYOUT",
                    Offset(center.x, origin.y - scaledOuter - 15f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "R=$innerR mm",
                    Offset(origin.x + scaledInner / 2f, origin.y - scaledInner / 2f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintAccent
                )
                drawTextOnBlueprint(
                    "Duct Width W=$ductW mm",
                    Offset(origin.x + scaledOuter - scaledInner / 2f, origin.y + 16f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintPrimary
                )
            }

            DuctType.TAPER -> {
                // Draw Taper Side Cheek trapezoid
                val w2 = e1.toFloat()
                val maxDim = w.coerceAtLeast(w2.toDouble()).coerceAtLeast(l).toFloat().coerceAtLeast(1f)
                val scale = (widthCanvas * 0.65f) / maxDim

                val scW1 = w.toFloat() * scale
                val scW2 = w2 * scale
                val scL = l.toFloat() * scale

                val startY = center.y - scL / 2f
                val endY = center.y + scL / 2f

                // Flat Pattern outline
                val p = Path().apply {
                    moveTo(center.x - scW2 / 2f, startY)
                    lineTo(center.x + scW2 / 2f, startY)
                    lineTo(center.x + scW1 / 2f, endY)
                    lineTo(center.x - scW1 / 2f, endY)
                    close()
                }
                drawPath(path = p, color = BlueprintLine, style = Stroke(width = 3.5f))

                // Centerline
                drawDashedVerticalLine(
                    color = BlueprintAccent,
                    x = center.x,
                    startY = startY - 10f,
                    endY = endY + 10f
                )

                drawTextOnBlueprint(
                    "TAPER FLANGES",
                    Offset(center.x, startY - 12f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "W2 = ${w2.toInt()} mm",
                    Offset(center.x, startY - 4f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintPrimary
                )
                drawTextOnBlueprint(
                    "W1 = ${w.toInt()} mm",
                    Offset(center.x, endY + 16f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintPrimary
                )
            }

            DuctType.OFFSET -> {
                // Draw Offset Parallel Slanted Transition
                val shift = e1.toFloat()
                val maxDim = l.coerceAtLeast(shift.toDouble()).toFloat().coerceAtLeast(1f)
                val scale = (widthCanvas * 0.6f) / maxDim

                val scL = l.toFloat() * scale
                val scShift = shift * scale
                val scW = w.toFloat() * scale / 2f // scaled width profile

                val startX = center.x - scL / 2f
                val startY = center.y + scShift / 2f // begin lower

                val endX = center.x + scL / 2f
                val endY = center.y - scShift / 2f

                // Draw Offset outline
                val pathOffset = Path().apply {
                    moveTo(startX, startY - scW)
                    lineTo(endX, endY - scW)
                    lineTo(endX, endY + scW)
                    lineTo(startX, startY + scW)
                    close()
                }
                drawPath(path = pathOffset, color = BlueprintLine, style = Stroke(width = 3.5f))

                // Shift marker line
                drawDashedVerticalLine(
                    color = BlueprintAccent,
                    x = endX,
                    startY = endY + scW,
                    endY = startY + scW
                )

                drawTextOnBlueprint(
                    "OFFSET TRANSITION",
                    Offset(center.x, endY - scW - 12f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "Offset = ${e1.toInt()} mm",
                    Offset(endX + 15f, center.y),
                    align = android.graphics.Paint.Align.LEFT,
                    color = BlueprintAccent
                )
                drawTextOnBlueprint(
                    "Length = ${l.toInt()} mm",
                    Offset(center.x, startY + scW + 16f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintPrimary
                )
            }

            DuctType.HIGH_SIDE_TAPER -> {
                // Flat on one side, slanted on the other
                val w2 = e1.toFloat()
                val maxDim = w.coerceAtLeast(w2.toDouble()).coerceAtLeast(l).toFloat().coerceAtLeast(1f)
                val scale = (widthCanvas * 0.65f) / maxDim

                val scW1 = w.toFloat() * scale
                val scW2 = w2 * scale
                val scL = l.toFloat() * scale

                val startY = center.y - scL / 2f
                val endY = center.y + scL / 2f
                val leftX = center.x - scW1 / 2f

                // Reducer path (flush straight left side, slanted right side)
                val pathHTaper = Path().apply {
                    moveTo(leftX, startY)
                    lineTo(leftX + scW2, startY)
                    lineTo(leftX + scW1, endY)
                    lineTo(leftX, endY)
                    close()
                }
                drawPath(path = pathHTaper, color = BlueprintLine, style = Stroke(width = 3.5f))

                // Highlight Left straight wall
                drawLine(
                    color = BlueprintAccent,
                    start = Offset(leftX, startY),
                    end = Offset(leftX, endY),
                    strokeWidth = 6f
                )

                drawTextOnBlueprint(
                    "FLUSH STRAIGHT WALL (LEFT)",
                    Offset(leftX - 10f, center.y),
                    align = android.graphics.Paint.Align.RIGHT,
                    color = BlueprintAccent
                )
                drawTextOnBlueprint(
                    "Taper Width W2 = ${w2.toInt()} mm",
                    Offset(center.x, startY - 8f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintPrimary
                )
            }

            DuctType.PLENUM -> {
                // Plenum Box 3D perspective or wrapper unfold
                val boxW = w.toFloat()
                val boxH = h.toFloat()
                val scale = (widthCanvas * 0.5f) / boxW.coerceAtLeast(boxH).coerceAtLeast(200f)

                val scW = boxW * scale
                val scH = boxH * scale

                // Unfolded 5-sided pattern
                val startX = center.x - scW / 2f
                val startY = center.y - scH / 2f

                drawRect(
                    color = BlueprintLine,
                    topLeft = Offset(startX, startY),
                    size = Size(scW, scH),
                    style = Stroke(width = 4f)
                )

                // Cut open outlets inside representation
                drawCircle(
                    color = BlueprintAccent,
                    radius = scW * 0.15f,
                    center = Offset(center.x - scW * 0.2f, center.y),
                    style = Stroke(width = 2.5f)
                )
                drawRect(
                    color = BlueprintAccent,
                    topLeft = Offset(center.x + scW * 0.05f, center.y - scH * 0.15f),
                    size = Size(scW * 0.3f, scH * 0.3f),
                    style = Stroke(width = 2.5f)
                )

                drawTextOnBlueprint(
                    "PLENUM TERMINAL DISTRIBUTION",
                    Offset(center.x, startY - 12f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "Collar cutouts allowed",
                    Offset(center.x, startY + scH + 16f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintAccent
                )
            }

            DuctType.DUMMY_SPACER -> {
                // Short straight joint adapter
                val scale = (widthCanvas * 0.65f) / w.toFloat().coerceAtLeast(1f)
                val scW = w.toFloat() * scale
                val scL = l.toFloat() * scale // typically short

                val startX = center.x - scW / 2f
                val startY = center.y - scL / 2f

                drawRect(
                    color = BlueprintLine,
                    topLeft = Offset(startX, startY),
                    size = Size(scW, scL),
                    style = Stroke(width = 3.5f)
                )

                // S-Cleat end flanges drawn on both ends
                drawRect(
                    color = BlueprintAccent,
                    topLeft = Offset(startX, startY - 6f),
                    size = Size(scW, 6f)
                )
                drawRect(
                    color = BlueprintAccent,
                    topLeft = Offset(startX, startY + scL),
                    size = Size(scW, 6f)
                )

                drawTextOnBlueprint(
                    "DUMMY SPACER ADAPTER",
                    Offset(center.x, startY - 15f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "Fit length L = ${l.toInt()} mm",
                    Offset(center.x, center.y + 4f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintPrimary
                )
            }

            DuctType.SHOE_COLLAR -> {
                // Drawn shape of an angled collar shoe
                val scale = (widthCanvas * 0.65f) / w.toFloat().coerceAtLeast(1f)
                val scW = w.toFloat() * scale
                val scH = l.toFloat() * scale

                val startX = center.x - scW / 2f
                val startY = center.y - scH / 2f

                // Draw standard body
                drawRect(
                    color = BlueprintLine,
                    topLeft = Offset(startX, startY),
                    size = Size(scW, scH),
                    style = Stroke(width = 3.5f)
                )

                // Angled bottom shoe flange
                val pathShoe = Path().apply {
                    moveTo(startX, startY + scH)
                    lineTo(startX - 20f, startY + scH + 20f)
                    lineTo(startX, startY + scH + 20f)
                    lineTo(startX + scW, startY + scH + 20f)
                    lineTo(startX + scW + 20f, startY + scH + 20f)
                    lineTo(startX + scW, startY + scH)
                    close()
                }
                drawPath(path = pathShoe, color = BlueprintAccent, style = Stroke(width = 3f))

                drawTextOnBlueprint(
                    "SHOE COLLAR HOLE SNAP-ON",
                    Offset(center.x, startY - 12f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "Tab notches are flared",
                    Offset(center.x, startY + scH + 34f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintAccent
                )
            }

            DuctType.TEE_PIECE -> {
                // T-Intersection Main run and branch split
                val scale = (widthCanvas * 0.5f) / w.toFloat().coerceAtLeast(l.toFloat()).coerceAtLeast(100f)
                val mLength = l.toFloat() * scale
                val mWidth = w.toFloat() * scale

                val bWidth = e1.toFloat() * scale
                val bLength = e2.toFloat() * scale

                val mainStartX = center.x - mLength / 2f
                val mainStartY = center.y - mWidth / 2f

                // 1. Draw main run rect
                drawRect(
                    color = BlueprintLine,
                    topLeft = Offset(mainStartX, mainStartY),
                    size = Size(mLength, mWidth),
                    style = Stroke(width = 3.5f)
                )

                // 2. Draw Branch splitting off perpendicular
                val branchStartX = center.x - bWidth / 2f
                val branchStartY = mainStartY - bLength

                drawRect(
                    color = BlueprintLine,
                    topLeft = Offset(branchStartX, branchStartY),
                    size = Size(bWidth, bLength),
                    style = Stroke(width = 3.5f)
                )

                // Seam connector line
                drawLine(
                    color = BlueprintAccent,
                    start = Offset(branchStartX, mainStartY),
                    end = Offset(branchStartX + bWidth, mainStartY),
                    strokeWidth = 5f
                )

                drawTextOnBlueprint(
                    "3-WAY TEE JUNCTION",
                    Offset(center.x, branchStartY - 10f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "Branch Split W=${e1.toInt()} mm",
                    Offset(center.x, branchStartY + bLength / 2f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintAccent
                )
            }

            DuctType.YEE_PIECE -> {
                // Two branches split symmetry "Pant Wye"
                val scale = (widthCanvas * 0.45f) / w.toFloat().coerceAtLeast(l.toFloat()).coerceAtLeast(100f)
                val inletW = w.toFloat() * scale
                val branchW = e1.toFloat() * scale
                val shapeL = l.toFloat() * scale

                val bottomY = center.y + shapeL / 2f
                val topY = center.y - shapeL / 2f
                val midX = center.x

                // Draw splitting pants cheek
                val pathYee = Path().apply {
                    // Inlet base
                    moveTo(midX - inletW / 2f, bottomY)
                    lineTo(midX + inletW / 2f, bottomY)
                    // Right split outlet
                    lineTo(midX + inletW / 2f + branchW * 0.5f, topY)
                    lineTo(midX + inletW / 2f - branchW * 0.5f, topY)
                    // Inner crotch splitter
                    lineTo(midX, center.y + 10f)
                    // Left split outlet
                    lineTo(midX - inletW / 2f + branchW * 0.5f, topY)
                    lineTo(midX - inletW / 2f - branchW * 0.5f, topY)
                    close()
                }
                drawPath(path = pathYee, color = BlueprintLine, style = Stroke(width = 4f))

                // Highlight inner crotch V-Point (red/amber)
                drawCircle(
                    color = BlueprintAccent,
                    radius = 8f,
                    center = Offset(midX, center.y + 10f)
                )

                drawTextOnBlueprint(
                    "PANT-WYE (YEE SPLIT) CHEEK",
                    Offset(center.x, topY - 12f),
                    align = android.graphics.Paint.Align.CENTER
                )
                drawTextOnBlueprint(
                    "AIR DEV V-POINT SEAM",
                    Offset(center.x, center.y + 25f),
                    align = android.graphics.Paint.Align.CENTER,
                    color = BlueprintAccent
                )
            }
        }
    }
}

// Custom blueprint canvas text & drawing helpers
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerCrosshairs(w: Float, h: Float) {
    val sizeLine = 20f
    // Top Left crosshair
    drawLine(color = BlueprintGrid, start = Offset(10f, 10f), end = Offset(10f + sizeLine, 10f), strokeWidth = 2f)
    drawLine(color = BlueprintGrid, start = Offset(10f, 10f), end = Offset(10f, 10f + sizeLine), strokeWidth = 2f)
    // Top Right crosshair
    drawLine(color = BlueprintGrid, start = Offset(w - 10f, 10f), end = Offset(w - 10f - sizeLine, 10f), strokeWidth = 2f)
    drawLine(color = BlueprintGrid, start = Offset(w - 10f, 10f), end = Offset(w - 10f, 10f + sizeLine), strokeWidth = 2f)
    // Bottom Left crosshair
    drawLine(color = BlueprintGrid, start = Offset(10f, h - 10f), end = Offset(10f + sizeLine, h - 10f), strokeWidth = 2f)
    drawLine(color = BlueprintGrid, start = Offset(10f, h - 10f), end = Offset(10f, h - 10f - sizeLine), strokeWidth = 2f)
    // Bottom Right crosshair
    drawLine(color = BlueprintGrid, start = Offset(w - 10f, h - 10f), end = Offset(w - 10f - sizeLine, h - 10f), strokeWidth = 2f)
    drawLine(color = BlueprintGrid, start = Offset(w - 10f, h - 10f), end = Offset(w - 10f, h - 10f - sizeLine), strokeWidth = 2f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDashedVerticalLine(
    color: Color,
    x: Float,
    startY: Float,
    endY: Float
) {
    val chunk = 12f
    val gap = 8f
    var currentY = startY
    while (currentY < endY) {
        val nextEnd = (currentY + chunk).coerceAtMost(endY)
        drawLine(
            color = color,
            start = Offset(x, currentY),
            end = Offset(x, nextEnd),
            strokeWidth = 3f
        )
        currentY = nextEnd + gap
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDashedRect(
    color: Color,
    topLeft: Offset,
    size: Size
) {
    val stepsX = (size.width / 12f).toInt()
    val stepsY = (size.height / 12f).toInt()

    // Draw top & bottom dashed lines
    for (i in 0 until stepsX step 2) {
        val currX = topLeft.x + i * 12f
        val nextX = (currX + 12f).coerceAtMost(topLeft.x + size.width)
        drawLine(color, Offset(currX, topLeft.y), Offset(nextX, topLeft.y), strokeWidth = 2f)
        drawLine(color, Offset(currX, topLeft.y + size.height), Offset(nextX, topLeft.y + size.height), strokeWidth = 2f)
    }

    // Draw left & right dashed lines
    for (i in 0 until stepsY step 2) {
        val currY = topLeft.y + i * 12f
        val nextY = (currY + 12f).coerceAtMost(topLeft.y + size.height)
        drawLine(color, Offset(topLeft.x, currY), Offset(topLeft.x, nextY), strokeWidth = 2f)
        drawLine(color, Offset(topLeft.x + size.width, currY), Offset(topLeft.x + size.width, nextY), strokeWidth = 2f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTextOnBlueprint(
    text: String,
    position: Offset,
    align: android.graphics.Paint.Align = android.graphics.Paint.Align.LEFT,
    color: Color = BlueprintPrimary
) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        position.x,
        position.y,
        android.graphics.Paint().apply {
            this.color = color.toArgb()
            this.textSize = 28f
            this.textAlign = align
            this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            this.isAntiAlias = true
        }
    )
}

@Composable
fun HistoryPanel(
    savedList: List<DuctCalculation>,
    onLoad: (DuctCalculation) -> Unit,
    onDelete: (DuctCalculation) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SAVED DUCTING SESSIONS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = BlueprintPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Database log for instant field recalls and template setups.",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintTextSecondary
                )
            }

            if (savedList.isNotEmpty()) {
                TextButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("clear_all_history")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CLEAR ALL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (savedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, BlueprintCardBorder, RoundedCornerShape(8.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No Saved Logs",
                        tint = BlueprintTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Warehouse Calculations Database Empty",
                        color = BlueprintTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Set up your straight, elbow, or taper fittings in the layout workspace and click 'SAVE TO DATABASE'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlueprintTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 350.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedList) { calc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLoad(calc) }
                            .testTag("history_item_${calc.id}"),
                        colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                        border = BorderStroke(1.dp, BlueprintCardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = calc.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BlueprintPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${DuctType.fromId(calc.ductType).displayName} | Size: ${calc.width.toInt()} × ${calc.height.toInt()} | L: ${calc.length.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BlueprintTextPrimary
                                )
                                Text(
                                    text = "Thickness: ${calc.gaugeThickness} | Qty: ${calc.quantity}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BlueprintTextSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = String.format("%.3f m²", calc.calculatedArea),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueprintPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = String.format("%.2f kg", calc.calculatedWeight),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueprintAccent,
                                        fontSize = 13.sp
                                    )
                                }

                                IconButton(
                                    onClick = { onDelete(calc) },
                                    modifier = Modifier.testTag("delete_history_item_${calc.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Single Item",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
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

@Composable
fun FormulasManualPanel() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "DUCT AREA FORMULAS & CONSTANTS",
                    fontWeight = FontWeight.Bold,
                    color = BlueprintPrimary,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Engineering reference guidelines for workshop billing and raw sheet estimations.",
                    style = MaterialTheme.typography.labelSmall,
                    color = BlueprintTextSecondary
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                border = BorderStroke(1.dp, BlueprintCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Rectangular Straight Section",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Formula: Area = 2 × (Width + Height) × Length\n\n- Perimeter (mm) = 2 × (W + H)\n- Net Square Meters = (Perimeter × L) ÷ 1,000,000\n- Flat Seam Pattern Addition: Add +50mm to total width for Pittsburgh seam coupling wrap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlueprintTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                border = BorderStroke(1.dp, BlueprintCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Rectangular 90° Elbow",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cheeks Area = 2 × [ (π / 4) × ( (Radius + Width)² - Radius² ) ]\nThroat Area = Height × [ (π / 2) × Radius ]\nHeel Area = Height × [ (π / 2) × (Radius + Width) ]\n\nTotal Area = sum of Cheeks + Throat + Heel.\n- Shortcut: Throat arc length = Radius × 1.57. Heel arc length = (Radius + Width) × 1.57.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlueprintTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                border = BorderStroke(1.dp, BlueprintCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Reducers / Taper Sections",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Slant L (height) = √[ Length² + ((Height1 - Height2)/2)² ]\nSlant L (width) = √[ Length² + ((Width1 - Width2)/2)² ]\n\nCheek Areas (Top/Bottom) = 2 × [ (W1 + W2) / 2 ] × Slant_Height_L1\nCheek Areas (Left/Right) = 2 × [ (H1 + H2) / 2 ] × Slant_Height_L2",
                        style = MaterialTheme.typography.bodySmall,
                        color = BlueprintTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlueprintSurface),
                border = BorderStroke(1.dp, BlueprintCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Galvanized Steel Weight Standard Chart",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BlueprintAccent,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TableGaugeWeightRow("26 Gauge (0.55mm)", "4.35 kg/m²")
                    TableGaugeWeightRow("24 Gauge (0.70mm)", "5.54 kg/m²")
                    TableGaugeWeightRow("22 Gauge (0.85mm)", "6.72 kg/m²")
                    TableGaugeWeightRow("20 Gauge (1.00mm)", "7.85 kg/m²")
                    TableGaugeWeightRow("18 Gauge (1.20mm)", "9.42 kg/m²")
                }
            }
        }
    }
}

@Composable
fun TableGaugeWeightRow(gauge: String, weight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = gauge, color = BlueprintTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Text(text = weight, color = BlueprintAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
