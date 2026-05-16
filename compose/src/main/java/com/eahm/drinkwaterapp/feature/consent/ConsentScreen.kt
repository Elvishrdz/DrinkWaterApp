package com.eahm.drinkwaterapp.feature.consent

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwPrimaryButton
import com.eahm.drinkwaterapp.core.designsystem.component.button.DwTertiaryButton
import com.eahm.drinkwaterapp.core.designsystem.theme.DrinkWaterAppTheme

internal class ConsentViewModel : ViewModel() {

}

internal sealed interface ConsentState {

}

internal sealed interface ConsentAction {

}

internal sealed interface ConsentEvent {

}

@Composable
fun ConsentRoot(modifier: Modifier = Modifier) {

}

@Composable
fun ConsentScreen(modifier: Modifier = Modifier) {

    // display privacy policy first step
    // then tracking screen

    // if use close the app after privacy policy, we will start with track screen next time, in this case we will not show the steps (because tracking screen will be now step 1 and only)

    // only after deciding about the tracking (yes or no) we will not display the consent screen anymore on app start up


    // store user accepted and date time of acceptance
    PrivacyPolicyScreen()

    // set tracking on (by default is off on first installation)
    TrackRequestScreen()
}

@Composable
fun PrivacyPolicyScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) {
        Column(modifier = Modifier.padding(it)) {
            // icon placeholder here
            Text("step 1 of 2")
            Text("Privacy Policy & Terms of use")
            Text("Take a moment to read our terms of use and privacy policy to use DrinkWater app.")

            // we hard code this text on every release if the document changes
            Text("Last updated * May 12, 2026")
            // Scrollable container with a hardcoded text (for now) with the privacy policy
            // use this for the text, make it a safe text and do not read any html commands or scripts. Html.fromHtml(getResources().getString(R.string.privacy_policy_local_content), Html.FROM_HTML_MODE_LEGACY)

            // clickable text: redirects to terms of use and privacy policy page, now redirect to google.com for now
            Row {
                // arrow icon pointing top right indicating navigation to external page.
                Text("Read the latest version on drinkwater.app")
            }

            // toggle button
            Text("I've read and agree to the terms of use and privacy policy")

            DwTertiaryButton(
                // button disabled until toggle is active
                text = "Accept terms of use and privacy policy and continue",
                onClick = {
                    // save this in prefs storage with current date and time of acceptance. and continue to step 2
                }
            )
        }
    }
}

@Composable
fun TrackRequestScreen(modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier) {
        Column(modifier = Modifier.padding(it)) {
            // icon placeholder here
            Text("step 2 of 2")
            Text("Help shape the app")
            Text("Share anonymous usage so we can see which features people love and which need work. Metrics only, no name, no email from you.")

            Text("You can find more information in our privacy policy")

            Row {
                // Reuse any icon here
                Column {
                    Text("Feature usage")
                    Text("We want to know what works and what not from a general view.")
                }
            }

            Row {
                // Reuse any icon here
                Column {
                    Text("Never linked to you")
                    Text("We use a random generated id that is NOT linked to any of your information.")
                }
            }

            Row {
                // Reuse any icon here
                Column {
                    Text("Off any time")
                    Text("Disable it anytime under Settings → Privacy → Usage data")
                }
            }

            // if tracking is OFF we show:
            Column {
                DwTertiaryButton(
                    text = "Not now",
                    onClick = {
                        // use tracking service and set track disabled
                    }
                )

                DwPrimaryButton(
                    text = "Allow sharing usage data",
                    onClick = {
                        // use tracking service to set to track
                    },
                )
            }

            // if tracking is ON we show:
            Column {
                Text("Tracking is active, you can disable it in app settings")
                DwTertiaryButton(
                    text = "Continue",
                    onClick = {
                        // we continue and finalize consent screens process
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ConsentScreenPreview() {
    DrinkWaterAppTheme {
        ConsentScreen()
    }
}
