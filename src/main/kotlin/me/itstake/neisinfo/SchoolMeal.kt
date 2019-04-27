package me.itstake.neisinfo

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class SchoolMeal(val time:MealTime, val menus: Array<SchoolMealMenu>): JSONObject() {

    enum class MealTime {
        BREAKFAST,
        LUNCH,
        DINNER
    }

    class SchoolMealMenu(val name: String, val allergies:Array<AllergyInfo>): JSONObject() {
        enum class AllergyInfo(val key:Int, val korname:String) {
            EGGS(1, "난류"),
            MILK(2, "우유"),
            BUCKWHEAT(3, "메밀"),
            PEANUT(4, "땅콩"),
            SOYBEAN(5, "대두"),
            WHEAT(6, "밀"),
            MACKEREL(7, "고등어"),
            CRAB(8, "게"),
            SHRIMP(9, "새우"),
            PORK(10, "돼지고기"),
            PEACH(11, "복숭아"),
            TOMATO(12, "토마토"),
            SULFUROUS_ACIDS(13, "아황산류"),
            WALNUT(14, "호두"),
            CHICKEN(15, "닭고기"),
            BEEF(16, "쇠고기"),
            SQUID(17, "오징어"),
            CLAM(18, "조개류(굴,전복,홍합 포함");

            companion object {
                private val values = values()
                fun getByKey(key: Int) = values.first {
                    it.key == key
                }
            }
        }
        init {
            this["name"] = name
            val allergyList = JSONArray()
            allergies.iterator().forEach { t ->
                allergyList.add(t)
            }
            this["allergies"] = allergyList
        }
    }

    init {
        this["time"] = time
        this["menus"] = JSONArray()
        menus.iterator().forEach { t ->
            (this["menus"] as JSONArray).add(t)
        }
    }
}