@file:OptIn(ExperimentalWasmJsInterop::class)
package org.com.campus.presentation

@JsFun("""(enabled) => {
    const id = 'campnav-monetag-ipp-12066520';
    const existing = document.getElementById(id);

    if (!enabled) {
        if (existing && existing.parentNode) {
            existing.parentNode.removeChild(existing);
        }
        return;
    }

    if (existing) return;

    const script = document.createElement('script');
    script.id = id;
    script.setAttribute('data-zone', '12066520');
    script.src = 'https://nap5k.com/tag.min.js';
    script.async = true;
    (document.body || document.documentElement).appendChild(script);
}""")
private external fun updateMonetagIpp(enabled: Boolean)

actual fun setHomeScreenAdEnabled(enabled: Boolean) {
    updateMonetagIpp(enabled)
}
