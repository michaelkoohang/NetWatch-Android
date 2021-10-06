package com.michaelkoohang.netwatch.model.db

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Recording(
    @PrimaryKey var id: String = "",
    var distance: Double = 0.0,
    var duration: Long = 0,
    var start: String = "",
    var end: String = "",
    var carrier: String = "",
    var os: String = "",
    var manufacturer: String = "",
    var features: RealmList<Feature> = RealmList()
) : RealmObject()