package com.scrapps.qucontroller

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform