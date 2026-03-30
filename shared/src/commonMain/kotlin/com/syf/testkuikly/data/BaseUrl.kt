package com.syf.testkuikly.data

/**
 * Platform-specific base URL for API requests.
 * Web/JS uses CORS proxy path, native platforms use direct URL.
 */
expect fun getBaseUrl(): String
