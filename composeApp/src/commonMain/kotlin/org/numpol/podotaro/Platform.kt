package org.numpol.podotaro

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform