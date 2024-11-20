package com.grupo18.twister.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Componente para mostrar una lista de opciones en forma de botones.
 * @param options Lista de opciones a mostrar.
 * @param onOptionSelected Función que se ejecuta al seleccionar una opción.
 */
@Composable
fun OptionsList(
    options: List<String>,
    selected: String?, // Opción seleccionada
    onOptionSelected: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(selected) } // Estado para la opción seleccionada
    println("Opción seleccionada dentro de optionlist: $selected")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.forEach { option ->
            OptionButton(
                optionText = option,
                isSelected = selectedOption.toString() == option, // Compara si esta opción es la seleccionada
                onClick = {

                    selectedOption = option // Establece la opción seleccionada
                    onOptionSelected(option) // Llama la función proporcionada
                }
            )
            println("is selected: ${selectedOption.toString() == option} donde option es $option y selected es $selected")
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * Componente para un botón de opción individual.
 * @param optionText Texto que se mostrará en el botón.
 * @param isSelected Indica si la opción está seleccionada.
 * @param onClick Acción que se ejecuta al hacer clic en el botón.
 */
@Composable
fun OptionButton(
    optionText: String,
    isSelected: Boolean, // Indicador si la opción está seleccionada
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF0066B2) else Color(0xFFF0F0F0) // Azul cuando seleccionado, blanco cuando no
        )
    ) {
        Text(
            text = optionText,
            color = if (isSelected) Color.White else Color.Black, // Blanco cuando seleccionado, negro cuando no
            fontSize = 16.sp
        )
    }
}
