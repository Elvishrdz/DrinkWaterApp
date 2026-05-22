package com.eahm.drinkwaterapp.core.presentation.layouttype

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.eahm.drinkwaterapp.core.presentation.LayoutType

class LayoutTypesPreviewProvider : PreviewParameterProvider<LayoutType> {
    override val values = LayoutType.entries.asSequence()
}
