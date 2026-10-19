package org.com.campus.presentation

import kotlinx.browser.document
import org.w3c.dom.HTMLScriptElement

actual fun setHomeScreenAdEnabled(enabled: Boolean) {
    val id = "campnav-monetag-ipp-12073426"
    val existing = document.getElementById(id)

    if (!enabled) {
        existing?.parentNode?.removeChild(existing)
        return
    }

    if (existing != null) return

    val script = document.createElement("script") as HTMLScriptElement
    script.id = id
    script.setAttribute("data-zone", "12073426")
    script.src = "https://nap5k.com/tag.min.js"
    script.async = true
    document.body?.appendChild(script)
}
