package com.gymtracker.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymtracker.app.R

private val PLATE_SIZES_KG = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
private val PLATE_SIZES_LBS = listOf(45.0, 35.0, 25.0, 10.0, 5.0, 2.5)
private const val BAR_WEIGHT_KG = 20.0
private const val BAR_WEIGHT_LBS = 45.0

@Composable
fun PlatesCalculatorDialog(useKg: Boolean, onDismiss: () -> Unit) {
    var targetWeight by remember { mutableStateOf("") }
    val barWeight = if (useKg) BAR_WEIGHT_KG else BAR_WEIGHT_LBS
    val plateSizes = if (useKg) PLATE_SIZES_KG else PLATE_SIZES_LBS
    val unit = if (useKg) "kg" else "lbs"

    val plates = remember(targetWeight) {
        val total = targetWeight.toDoubleOrNull() ?: 0.0
        calculatePlates(total, barWeight, plateSizes)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.plates_calculator)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetWeight,
                    onValueChange = { targetWeight = it },
                    label = { Text(stringResource(R.string.target_weight, unit)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (plates.isNotEmpty()) {
                    Text(
                        text = "${stringResource(R.string.bar)}: ${barWeight} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.plates_per_side),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    plates.forEach { (plate, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$plate $unit")
                            Text("× $count")
                        }
                    }
                } else if (targetWeight.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.weight_too_low),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

private fun calculatePlates(
    total: Double,
    barWeight: Double,
    plateSizes: List<Double>
): List<Pair<Double, Int>> {
    if (total <= barWeight) return emptyList()
    var remaining = (total - barWeight) / 2
    val result = mutableListOf<Pair<Double, Int>>()
    for (plate in plateSizes) {
        val count = (remaining / plate).toInt()
        if (count > 0) {
            result.add(plate to count)
            remaining -= plate * count
        }
    }
    return result
}
