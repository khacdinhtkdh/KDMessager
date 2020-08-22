package com.example.kdmessager.ModelClasses

class Chat (
    val sender: String,
    val receiver: String,
    val message: String,
    var seen: Boolean,
    val url: String,
    val messageId: String) {
    constructor() : this ("", "", "", false, "", "")
}