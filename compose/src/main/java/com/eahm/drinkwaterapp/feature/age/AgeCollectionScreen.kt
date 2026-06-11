package com.eahm.drinkwaterapp.feature.age

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType
import com.eahm.drinkwaterapp.core.presentation.layouttype.LayoutTypesPreviewProvider
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AgeCollectionRoot(
    onComplete: () -> Unit,
    viewModel: AgeCollectionViewModel = koinViewModel(),
) {
    val completed by viewModel.completed.collectAsStateWithLifecycle()

    LaunchedEffect(completed) {
        if (completed) onComplete()
    }

    AgeCollectionScreen(onContinue = viewModel::submitSelection)
}

@Suppress("Indentation")
@Composable
internal fun AgeCollectionScreen(
    onContinue: (AgeBracketOption) -> Unit,
    layoutType: LayoutType = getLayoutType(),
) {
    var selected by rememberSaveable { mutableStateOf<AgeBracketOption?>(null) }

    val onSelect: (AgeBracketOption) -> Unit = { selected = it }
    val onContinueClick: () -> Unit = { selected?.let(onContinue) }

    when (layoutType) {
        LayoutType.PORTRAIT_EXPANDED,
        LayoutType.PORTRAIT,
        LayoutType.TABLE_TOP,
            -> AgePortrait(
            selected = selected,
            onSelect = onSelect,
            onContinue = onContinueClick,
        )

        LayoutType.LANDSCAPE,
            -> AgeLandscape(
            weight = 0.7f,
            selected = selected,
            onSelect = onSelect,
            onContinue = onContinueClick,
        )

        LayoutType.FOLDABLE_BOOK,
        LayoutType.FOLDABLE_EXPANDED,
            -> AgeLandscape(
            weight = 1f,
            selected = selected,
            onSelect = onSelect,
            onContinue = onContinueClick,
        )
    }
}

@Preview
@Composable
private fun AgeCollectionScreenPreview(
    @PreviewParameter(LayoutTypesPreviewProvider::class) layoutType: LayoutType,
) {
    DrinkWaterAppTheme {
        AgeCollectionScreen(
            onContinue = {},
            layoutType = layoutType,
        )
    }
}
