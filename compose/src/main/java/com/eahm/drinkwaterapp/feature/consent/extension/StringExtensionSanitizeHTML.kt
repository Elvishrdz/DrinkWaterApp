package com.eahm.drinkwaterapp.feature.consent.extension

import androidx.compose.ui.text.AnnotatedString

/**
 * Strips potentially dangerous HTML before rendering with [AnnotatedString.Companion.fromHtml].
 *
 * What is removed:
 * - `<script>` blocks (code execution)
 * - `<style>` blocks (CSS injection)
 * - `<a>` tags entirely — link text is preserved, but no navigable href is emitted.
 *   The UI provides a dedicated "View online" button for the full policy.
 * - `on*` event attributes (onclick, onload, etc.)
 * - Dangerous href schemes: javascript:, data:, intent://, vbscript:
 *
 * What is kept:
 * - All visual markup: `<b>`, `<i>`, `<u>`, `<h1>`–`<h6>`, `<p>`, `<ul>`, `<li>`, `<strong>`, `<em>`, `<br>`
 * - HTML entities (&amp;, &nbsp;, &mdash;, etc.)
 */
internal fun String.sanitizeHtml(): String = this
    // Remove <script> blocks entirely (including their contents)
    .replace(Regex("""<script\b[^>]*>[\s\S]*?</script>""", RegexOption.IGNORE_CASE), "")
    // Remove <style> blocks entirely
    .replace(Regex("""<style\b[^>]*>[\s\S]*?</style>""", RegexOption.IGNORE_CASE), "")
    // Strip <a href="..."> opening tags (keep inner text)
    .replace(Regex("""<a\b[^>]*>""", RegexOption.IGNORE_CASE), "")
    // Strip </a> closing tags
    .replace(Regex("""</a\s*>""", RegexOption.IGNORE_CASE), "")
    // Strip on* event attributes (onclick="...", onload='...', etc.)
    .replace(Regex("""\s+on\w+\s*=\s*"[^"]*"""", RegexOption.IGNORE_CASE), "")
    .replace(Regex("""\s+on\w+\s*=\s*'[^']*'""", RegexOption.IGNORE_CASE), "")
    // Strip any remaining dangerous href schemes as a last-resort guard
    .replace(
        Regex(
            """href\s*=\s*["'](javascript:|data:|intent://|vbscript:)[^"']*["']""",
            RegexOption.IGNORE_CASE
        ),
        "",
    )
