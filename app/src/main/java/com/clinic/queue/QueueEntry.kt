// FILE: app/src/main/java/com/clinic/queue/QueueEntry.kt
package com.clinic.queue

data class QueueEntry(
    val queueNumber: Int = 0,
    val icNumber: String = "",
    val name: String = ""
)