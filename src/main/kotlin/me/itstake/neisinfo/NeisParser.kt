package me.itstake.neisinfo

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class NeisParser {
    companion object {

        //FOR SCHOOL INFO
        fun parseSchoolInfo(data: String): SchoolInfo {
            val convertKey = hashMapOf("학교명" to "name", "우편번호" to "zipCode", "주소" to "address", "전화번호" to "callNum", "팩스번호" to "faxNum", "홈페이지주소" to "homepage", "학생수" to "stuNum", "남" to "stuNumMen", "여" to "stuNumWomen", "학년별 학급수" to "classNumByGrade", "교원정보" to "teacherNum")
            val table1starts = data.indexOf("<table cellspacing=\"0\" summary=\"이 표는")
            val table1ends = data.indexOf("</table>") + 8
            val table2starts = data.indexOf("<table cellspacing=\"0\" summary=\"이 표는", table1ends)
            val table2ends = data.indexOf("</table>", table1ends)
            val table1 = infoTableToMap(data.substring(table1starts, table1ends))
            table1.putAll(infoTableToMap(data.substring(table2starts, table2ends)))
            val ret = JSONObject()
            table1.forEach { t, u -> if(convertKey.containsKey(t)) ret[convertKey[t]] = convertInfoValue(t as String, u as String) }
            return SchoolInfo(ret)
        }

        private fun convertInfoValue(t:String, u: String): Any {
            return when(t) {
                "우편번호" -> u.toLong()
                "학생수", "남", "여" -> u.replace("명", "").trim().toLong()
                "학년별 학급수" -> {
                    val array = JSONArray()
                    u.split(",").forEach { t ->
                        array.add(t.split("(")[1].replace("학급)", "").toLong())
                    }
                    array
                }
                "홈페이지주소" -> {
                    u.substringAfter("이동\">").substringBefore("</a>").trim()
                }
                "교원정보" -> u.replace(" 명 재직(파견인력 포함)", "").trim().toLong()
                else -> u
            }
        }

        private fun infoTableToMap(table: String): JSONObject {
            var lastSearchedIndex = 0
            var searchedIndex:Int
            val ret = JSONObject()
            while(lastSearchedIndex > -1) {
                val thIndex = table.indexOf("<th", lastSearchedIndex)
                if(thIndex <= -1) break
                val thSize = table.indexOf(">", thIndex) - thIndex + 1
                val tdIndex = table.indexOf("<td", lastSearchedIndex)
                val tdSize = table.indexOf(">", tdIndex) - tdIndex + 1
                searchedIndex = table.indexOf(">", tdIndex)
                lastSearchedIndex = if(searchedIndex > -1) searchedIndex + 1 else searchedIndex
                val th = table.substring(thIndex + thSize, table.indexOf("</th>", thIndex)).trim()
                val td = table.substring(tdIndex + tdSize, table.indexOf("</td>", tdIndex)).trim()
                ret[th] = td
            }
            return ret
        }

        //FOR SCHOOL MEALS
        fun parseSchoolMeals(data:String): JSONObject {
            val table = data.substring(data.indexOf("<table cellspacing=\"0\" summary=\"이 표는"), data.indexOf("</table>") + 8)
            var lastSearchedIndex = 0
            var searchedIndex:Int
            val ret = JSONObject()
            while(lastSearchedIndex > -1) {
                val tdIndex = table.indexOf("<td", lastSearchedIndex)
                val tdSize = table.indexOf(">", tdIndex) - tdIndex + 1
                if(tdIndex <= -1) break
                searchedIndex = table.indexOf("</td>", tdIndex)
                lastSearchedIndex = if(searchedIndex > -1) searchedIndex + 1 else searchedIndex
                val td = table.substring(tdIndex + tdSize, searchedIndex).replace("<div>", "").replace("</div>", "").trim().split("<br />")
                if(td.size > 1) {
                    val day = td[0].toInt()
                    val breakfastIndex = td.indexOf("[조식]")
                    val lunchIndex = td.indexOf("[중식]")
                    val dinnerIndex = td.indexOf("[석식]")
                    val dayMeals = JSONObject()
                    if(breakfastIndex > -1) dayMeals[SchoolMeal.MealTime.BREAKFAST.name] = toMealArray(td.subList(breakfastIndex + 1, if(lunchIndex > -1) lunchIndex -1 else td.size - 1))
                    if(lunchIndex > -1) dayMeals[SchoolMeal.MealTime.LUNCH.name] = toMealArray(td.subList(lunchIndex + 1, if(dinnerIndex > -1) dinnerIndex -1 else td.size - 1))
                    if(dinnerIndex > -1) dayMeals[SchoolMeal.MealTime.DINNER.name] = toMealArray(td.subList(dinnerIndex + 1, td.size - 1))
                    ret[day] = dayMeals
                }
            }
            return ret
        }

        private fun toMealArray(menus: List<String>): JSONArray {
            val ret = JSONArray()
            menus.forEach { t ->
                val allergies = ArrayList<SchoolMeal.AllergyInfo>()
                var name = t
                for(i in 1..18) {
                    val index = t.indexOf("$i.")
                    if(index > -1) allergies.add(SchoolMeal.AllergyInfo.getByKey(i)); name = name.replace("$i.", "")
                }
                ret.add(SchoolMeal(name.trim(), allergies))
            }
            return ret
        }

        //FOR SCHOOL SCHEDULE
        fun parseSchoolSchedule(data:String):JSONObject {
            val ret = JSONObject()
            return ret
        }
    }
}