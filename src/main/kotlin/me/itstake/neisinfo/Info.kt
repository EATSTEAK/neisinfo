package me.itstake.neisinfo

import kotlin.ClassCastException as ClassCastException1

data class Info(
    val name: String,
    val zipCode: Int,
    val address: String,
    val callNum: String,
    val faxNum: String,
    val homepage: String,
    val stuNum: Int,
    val stuNumMen: Int,
    val stuNumWomen: Int,
    val classNumByGrade: List<Int>,
    val teacherNum: Int
) {


    companion object {
        fun fromMap(map: Map<String, Any>): Info {
            val name: String by map
            val zipCode: Int by map
            val address: String by map
            val callNum: String by map
            val faxNum: String by map
            val homepage: String by map
            val stuNum: Int by map
            val stuNumMen: Int by map
            val stuNumWomen: Int by map
            val classNumByGrade: List<Int> by map
            val teacherNum: Int by map
            return Info(
                name,
                zipCode,
                address,
                callNum,
                faxNum,
                homepage,
                stuNum,
                stuNumMen,
                stuNumWomen,
                classNumByGrade,
                teacherNum
            )

        }

    }
}