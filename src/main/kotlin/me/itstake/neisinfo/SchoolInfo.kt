package me.itstake.neisinfo

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import kotlin.ClassCastException as ClassCastException1

class SchoolInfo(json:JSONObject) : JSONObject() {
    val indexes = hashMapOf("name" to String::class, "zipCode" to Long::class, "address" to String::class, "callNum" to String::class, "faxNum" to String::class, "homepage" to String::class, "stuNum" to Long::class, "stuNumMen" to Long::class, "stuNumWomen" to Long::class, "classNumByGrade" to JSONArray::class, "teacherNum" to Long::class)

    init {
        json.forEach { t, u ->
            if(t is String && indexes.containsKey(t) && u::class == indexes[t]) {
                this[t] = u
            } else {
                throw ClassCastException("Data is incompatible.")
            }
        }
    }
}