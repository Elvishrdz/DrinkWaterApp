package com.eahm.drinkwaterapp.feature.adaptivelayout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eahm.drinkwaterapp.core.presentation.LayoutType
import com.eahm.drinkwaterapp.core.presentation.getLayoutType

@Composable
fun AdaptiveScreen() {
    val layoutType = getLayoutType()

    when (layoutType) {
        LayoutType.TABLE_TOP -> {
            TabletopLayout()
        }

        LayoutType.LANDSCAPE -> {
            LandscapeLayout()
        }

        LayoutType.FOLDABLE_EXPANDED -> {
            ExpandedLayout()
        }

        LayoutType.PORTRAIT -> {
            PortraitLayout()
        }
    }
}

@Composable
fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Portrait")
    }
}

@Composable
fun ExpandedLayout() {
    Row(modifier = Modifier.fillMaxSize()) {
        Text("Expanded")
    }
}

@Composable
fun TabletopLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Tabletop")
    }
}

@Composable
fun LandscapeLayout() {
    Row(modifier = Modifier.fillMaxSize()) {
        Text("Landscape")
    }
}

@Preview
@Composable
private fun AdaptiveScreenPreview() {
    AdaptiveScreen()
}
