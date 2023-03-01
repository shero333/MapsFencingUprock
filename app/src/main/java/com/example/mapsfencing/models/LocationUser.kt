package com.example.mapsfencing.models

class LocationUser {

    var latitude: Double? = null
    var longitude: Double? = null

    constructor(latitude: Double?, longitude: Double?) {
        this.latitude = latitude
        this.longitude = longitude
    }

    constructor()

    override fun toString(): String {
        return "SimpleUser{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}'
    }
}