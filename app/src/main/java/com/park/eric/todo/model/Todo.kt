package com.park.eric.todo.model

import io.realm.*
import java.util.*
import io.realm.annotations.PrimaryKey

open class Todo(
        @PrimaryKey var id: Long = 0,
        var title: String = "",
        var contents: String = "",
        var date: Date = Date(),
        var completed: Boolean = false
) : RealmObject() {

}