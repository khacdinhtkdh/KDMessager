package com.example.kdmessager.ModelClasses

class Data(var sender: String,var icon: Int, var body: String, var title: String, var receiver: String) {
    constructor() : this("", 0, "", "", "")
}