package com.normie69K.v_guard.data.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val linkedEspId: String = "",
    val emergencyContacts: List<String> = emptyList()
)