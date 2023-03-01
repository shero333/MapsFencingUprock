package com.example.mapsfencing.models

class User {
    var latitude: Double? = null
    var longitude: Double? = null
    var username: String? = null
    var name: String? = null
    var contact: String? = null
    var password: String? = null
    var id: String? = null

    constructor(username: String?,name: String?,contact: String?, password: String?,id: String?) {
        this.username = username
        this.name = name
        this.contact = contact
        this.password = password
        this.id = id
    }

    constructor(latitude: Double?, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    constructor()

    override fun toString(): String {
        return "User(" +
                "latitude=$latitude, " +
                "longitude=$longitude, " +
                "username=$username, " +
                "name=$name, " +
                "contact=$contact, " +
                "password=$password), " +
                "password=$id)"
    }


}