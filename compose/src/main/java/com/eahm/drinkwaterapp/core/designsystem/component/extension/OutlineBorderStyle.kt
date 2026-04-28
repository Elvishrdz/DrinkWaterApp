package com.eahm.drinkwaterapp.core.designsystem.component.extension

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eahm.drinkwaterapp.core.designsystem.theme.DwInk100
import com.eahm.drinkwaterapp.core.designsystem.theme.DwPaper

private val shape = RoundedCornerShape(14.dp)

fun Modifier.outlineBorderStyle() = this.then(
    Modifier
        .clip(shape)
        .background(DwPaper)
        .border(1.dp, DwInk100, shape)
)
