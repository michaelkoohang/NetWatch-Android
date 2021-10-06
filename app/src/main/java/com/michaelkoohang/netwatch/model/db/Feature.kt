package com.michaelkoohang.netwatch.model.db

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Feature(
    @PrimaryKey var id: String = "",
    var timestamp: String = "",
    var battery: Int = 0,
    var network: String = "",
    var service: Boolean = false,
    var connected: Boolean = false,
    var http: Boolean = false,
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var accuracy: Double = 0.0,
    var speed: Double = 0.0
) : RealmObject()