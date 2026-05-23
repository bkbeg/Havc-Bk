package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DuctCalculation
import com.example.data.DuctCalculationRepository
import com.example.data.DuctType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sqrt

class DuctViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DuctCalculationRepository

    val history: StateFlow<List<DuctCalculation>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DuctCalculationRepository(db.ductCalculationDao())
        history = repository.allCalculations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Interactive input states initialized to default sizes requested (600mm x 700mm x 900mm)
    private val _selectedDuctType = MutableStateFlow(DuctType.STRAIGHT)
    val selectedDuctType: StateFlow<DuctType> = _selectedDuctType.asStateFlow()

    private val _width = MutableStateFlow(600.0)
    val width: StateFlow<Double> = _width.asStateFlow()

    private val _height = MutableStateFlow(700.0)
    val height: StateFlow<Double> = _height.asStateFlow()

    private val _length = MutableStateFlow(900.0)
    val length: StateFlow<Double> = _length.asStateFlow()

    private val _extra1 = MutableStateFlow(50.0) // Maps to Straight's default Seam allowance
    val extra1: StateFlow<Double> = _extra1.asStateFlow()

    private val _extra2 = MutableStateFlow(0.0)
    val extra2: StateFlow<Double> = _extra2.asStateFlow()

    private val _selectedGauge = MutableStateFlow("24G (0.70mm)")
    val selectedGauge: StateFlow<String> = _selectedGauge.asStateFlow()

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity.asStateFlow()

    private val _calcName = MutableStateFlow("")
    val calcName: StateFlow<String> = _calcName.asStateFlow()

    fun updateDuctType(type: DuctType) {
        _selectedDuctType.value = type
        // Apply default extras mapping
        _extra1.value = type.defaultExtra1Value
        _extra2.value = type.defaultExtra2Value
        _calcName.value = "${type.displayName} Session"
    }

    fun updateWidth(v: Double) { _width.value = v }
    fun updateHeight(v: Double) { _height.value = v }
    fun updateLength(v: Double) { _length.value = v }
    fun updateExtra1(v: Double) { _extra1.value = v }
    fun updateExtra2(v: Double) { _extra2.value = v }
    fun updateGauge(v: String) { _selectedGauge.value = v }
    fun updateQuantity(v: Int) { _quantity.value = v }
    fun updateName(v: String) { _calcName.value = v }

    // Quick presets to easily match user's custom layout requests
    fun applyPresetDimensions(w: Double, h: Double, l: Double, r: Double = 150.0, taper: Double = 250.0, offset: Double = 300.0) {
        _width.value = w
        _height.value = h
        _length.value = l
        when (_selectedDuctType.value) {
            DuctType.ELBOW_90 -> { _extra1.value = r }
            DuctType.TAPER, DuctType.HIGH_SIDE_TAPER -> {
                _extra1.value = taper
                _extra2.value = 500.0 // preset second height
            }
            DuctType.OFFSET -> { _extra1.value = offset }
            else -> {}
        }
    }

    fun calculateAreaAndWeight(
        type: DuctType,
        w: Double,
        h: Double,
        l: Double,
        e1: Double,
        e2: Double,
        gauge: String,
        qty: Int
    ): Pair<Double, Double> {
        val areaSqMeter = when (type) {
            DuctType.STRAIGHT -> {
                val perimeter = 2 * (w + h)
                (perimeter * l) / 1000000.0
            }
            DuctType.ELBOW_90 -> {
                val radius = e1
                val cheekArea = 2.0 * (PI / 4.0 * ((radius + w) * (radius + w) - radius * radius))
                val throatArea = h * (PI / 2.0 * radius)
                val heelArea = h * (PI / 2.0 * (radius + w))
                (cheekArea + throatArea + heelArea) / 1000000.0
            }
            DuctType.TAPER -> {
                val w2 = e1
                val h2 = e2
                val slantL1 = sqrt(l * l + ((h - h2) / 2.0) * ((h - h2) / 2.0))
                val slantL2 = sqrt(l * l + ((w - w2) / 2.0) * ((w - w2) / 2.0))
                val topBottom = 2.0 * ((w + w2) / 2.0) * slantL1
                val leftRight = 2.0 * ((h + h2) / 2.0) * slantL2
                (topBottom + leftRight) / 1000000.0
            }
            DuctType.OFFSET -> {
                val shift = e1
                val slantL = sqrt(l * l + shift * shift)
                val perimeter = 2 * (w + h)
                (perimeter * slantL) / 1000000.0
            }
            DuctType.HIGH_SIDE_TAPER -> {
                val w2 = e1
                val h2 = e2
                val slantL1 = sqrt(l * l + (h - h2) * (h - h2))
                val slantL2 = sqrt(l * l + (w - w2) * (w - w2))
                val topBottom = 2.0 * ((w + w2) / 2.0) * slantL1
                val leftRight = 2.0 * ((h + h2) / 2.0) * slantL2
                (topBottom + leftRight) / 1000000.0
            }
            DuctType.PLENUM -> {
                val topBottom = 2.0 * w * h
                val horizontalSides = 2.0 * w * l
                val verticalSides = 2.0 * h * l
                (topBottom + horizontalSides + verticalSides) / 1000000.0
            }
            DuctType.DUMMY_SPACER -> {
                val perimeter = 2 * (w + h)
                (perimeter * l) / 1000000.0
            }
            DuctType.SHOE_COLLAR -> {
                val perimeter = 2 * (w + h)
                val bodyArea = perimeter * l
                val flangeArea = 2.0 * (w + 50) * e1
                (bodyArea + flangeArea) / 1000000.0
            }
            DuctType.TEE_PIECE -> {
                val mainArea = 2 * (w + h) * l
                val branchW = e1
                val branchL = e2
                val branchArea = 2 * (branchW + h) * branchL
                val holeArea = branchW * h
                (mainArea + branchArea - holeArea) / 1000000.0
            }
            DuctType.YEE_PIECE -> {
                val branchW = e1
                val pantsArea = 2.0 * (w * l)
                val sideWrapsArea = 2.0 * (2.0 * sqrt(l * l + (w / 2.0) * (w / 2.0))) * h
                (pantsArea + sideWrapsArea) * 1.55 / 1000000.0
            }
        }

        val multiplier = when (gauge) {
            "26G (0.55mm)" -> 4.35
            "24G (0.70mm)" -> 5.54
            "22G (0.85mm)" -> 6.72
            "20G (1.00mm)" -> 7.85
            "18G (1.20mm)" -> 9.42
            else -> 5.54
        }

        return Pair(areaSqMeter * qty, areaSqMeter * qty * multiplier)
    }

    fun saveCalculation() {
        viewModelScope.launch {
            val type = _selectedDuctType.value
            val w = _width.value
            val h = _height.value
            val l = _length.value
            val e1 = _extra1.value
            val e2 = _extra2.value
            val gauge = _selectedGauge.value
            val qty = _quantity.value
            val (area, weight) = calculateAreaAndWeight(type, w, h, l, e1, e2, gauge, qty)

            val nameStr = _calcName.value.ifBlank { "Unlabelled ${type.displayName}" }

            val calculation = DuctCalculation(
                name = nameStr,
                ductType = type.id,
                width = w,
                height = h,
                length = l,
                extra1 = e1,
                extra2 = e2,
                gaugeThickness = gauge,
                quantity = qty,
                calculatedArea = area,
                calculatedWeight = weight
            )
            repository.insert(calculation)
        }
    }

    fun deleteCalculation(calc: DuctCalculation) {
        viewModelScope.launch {
            repository.delete(calc)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun loadCalculation(calc: DuctCalculation) {
        val type = DuctType.fromId(calc.ductType)
        _selectedDuctType.value = type
        _width.value = calc.width
        _height.value = calc.height
        _length.value = calc.length
        _extra1.value = calc.extra1
        _extra2.value = calc.extra2
        _selectedGauge.value = calc.gaugeThickness
        _quantity.value = calc.quantity
        _calcName.value = calc.name
    }
}
