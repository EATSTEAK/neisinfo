package me.itstake.neisinfo


class NeisParser {
    companion object {

        //FOR SCHOOL INFO
        fun parseSchoolInfo(data: String): Info {
            val convertKey = hashMapOf("학교명" to "name", "우편번호" to "zipCode", "주소" to "address", "전화번호" to "callNum", "팩스번호" to "faxNum", "홈페이지주소" to "homepage", "학생수" to "stuNum", "남" to "stuNumMen", "여" to "stuNumWomen", "학년별 학급수" to "classNumByGrade", "교원정보" to "teacherNum")
            val table1starts = data.indexOf("<table cellspacing=\"0\" summary=\"이 표는")
            val table1ends = data.indexOf("</table>") + 8
            val table2starts = data.indexOf("<table cellspacing=\"0\" summary=\"이 표는", table1ends)
            val table2ends = data.indexOf("</table>", table1ends)
            val table1 = infoTableToMap(data.substring(table1starts, table1ends)) as HashMap
            table1.putAll(infoTableToMap(data.substring(table2starts, table2ends)))
            val ret = HashMap<String, Any>()
            table1.forEach { t, u ->
                val ck = convertKey[t]
                if (convertKey.containsKey(t) && ck != null) ret.put(ck, convertInfoValue(t, u))
            }
            return Info.fromMap(ret)
        }

        private fun convertInfoValue(t:String, u: String): Any {
            return when(t) {
                "우편번호" -> u.toLong()
                "학생수", "남", "여" -> u.replace("명", "").trim().toLong()
                "학년별 학급수" -> {
                    val array = ArrayList<Int>()
                    u.split(",").forEach { v ->
                        array.add(v.split("(")[1].replace("학급)", "").toInt())
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

        private fun infoTableToMap(table: String): Map<String, String> {
            var lastSearchedIndex = 0
            var searchedIndex:Int
            val ret = HashMap<String, String>()
            while(lastSearchedIndex > -1) {
                val thIndex = table.indexOf("<th", lastSearchedIndex)
                if(thIndex <= -1) break
                val thEnd = table.indexOf(">", thIndex) + 1
                val tdIndex = table.indexOf("<td", lastSearchedIndex)
                val tdEnd = table.indexOf(">", tdIndex) + 1
                searchedIndex = table.indexOf(">", tdIndex)
                lastSearchedIndex = if(searchedIndex > -1) searchedIndex + 1 else searchedIndex
                ret[table.substring(thEnd, table.indexOf("</th>", thIndex)).trim()] =
                    table.substring(tdEnd, table.indexOf("</td>", tdIndex)).trim()
            }
            return ret
        }

        //FOR SCHOOL MEALS
        fun parseSchoolMeals(data: String): Map<Int, Meal> {
            val table = data.substring(data.indexOf("<table cellspacing=\"0\" summary=\"이 표는"), data.indexOf("</table>") + 8)
            var lastSearchedIndex = 0
            var searchedIndex:Int
            val ret = HashMap<Int, Meal>()
            while(lastSearchedIndex > -1) {
                val tdIndex = table.indexOf("<td", lastSearchedIndex)
                if(tdIndex <= -1) break
                val tdEnd = table.indexOf(">", tdIndex) + 1
                searchedIndex = table.indexOf("</td>", tdIndex)
                lastSearchedIndex = if(searchedIndex > -1) searchedIndex + 1 else searchedIndex
                val td = table.substring(tdEnd, searchedIndex).replace("<div>", "").replace("</div>", "").trim()
                    .split("<br />")
                if(td.size > 1) {
                    val day = td[0].toInt()
                    val breakfastIndex = td.indexOf("[조식]")
                    val lunchIndex = td.indexOf("[중식]")
                    val dinnerIndex = td.indexOf("[석식]")
                    ret[day] = Meal(
                        if (breakfastIndex > -1) toMealArray(
                            td.subList(
                                breakfastIndex + 1,
                                if (lunchIndex > -1) lunchIndex - 1 else td.size - 1
                            )
                        ) else null,
                        if (lunchIndex > -1) toMealArray(
                            td.subList(
                                lunchIndex + 1,
                                if (dinnerIndex > -1) dinnerIndex - 1 else td.size - 1
                            )
                        ) else null,
                        if (dinnerIndex > -1) toMealArray(td.subList(dinnerIndex + 1, td.size - 1)) else null
                    )
                }
            }
            return ret
        }

        private fun toMealArray(menus: List<String>): List<MealMenu> {
            val ret = ArrayList<MealMenu>()
            menus.forEach { t ->
                val allergies = ArrayList<MealMenu.AllergyInfo>()
                var name = t
                for(i in 1..18) {
                    val index = t.indexOf("$i.")
                    if (index > -1) allergies.add(MealMenu.AllergyInfo.getByKey(i)); name = name.replace("$i.", "")
                }
                ret.add(MealMenu(name.trim(), allergies))
            }
            return ret
        }

        //FOR SCHOOL SCHEDULE
        fun parseSchoolSchedule(data: String, deep: Boolean, school: School?): Map<Int, Event> {
            val table =
                data.substring(data.indexOf("<table cellspacing=\"0\" summary=\"월간학사"), data.indexOf("</table>") + 8)
            var lastSearchedIndex = 0
            var searchedIndex: Int
            val ret = HashMap<Int, Event>()
            while (lastSearchedIndex > -1) {
                val tdIndex = table.indexOf("<td", lastSearchedIndex)
                if (tdIndex <= -1) break
                val tdEnd = table.indexOf(">", tdIndex) + 1
                searchedIndex = table.indexOf("</td>", tdIndex)
                lastSearchedIndex = if (searchedIndex > -1) searchedIndex + 1 else searchedIndex
                val td = table.substring(tdEnd, searchedIndex).replace("<div>", "").replace("</div>", "").trim()
                val emIndex = td.indexOf("<em")
                if (emIndex > -1) {
                    val strongIndex = td.indexOf("<strong", emIndex)
                    if (strongIndex > -1) {
                        val emEnd = td.indexOf(">", emIndex) + 1
                        val strongEnd = td.indexOf(">", strongIndex) + 1
                        val eventCodeEnd = td.indexOf("eventCode=", emIndex) + 10
                        val eventDateEnd = td.indexOf("eventDate=", emIndex) + 10
                        val event = Event(
                            name = td.substring(strongEnd, td.indexOf("</strong>")),
                            code = td.substring(eventCodeEnd, eventCodeEnd + 4),
                            date = td.substring(eventDateEnd, eventDateEnd + 8)
                        )
                        if (deep && school != null) event.updateEventInfo(school)
                        ret[td.substring(emEnd, td.indexOf("</em>")).toInt()] = event
                    }
                }
            }
            return ret
        }

        fun parseEventInfo(data: String): EventInfo {
            val tdStart = data.indexOf("<td class=\"textL\">", data.indexOf("해당학년</th>"))
            val grades = ArrayList<Int>()
            data.substring(tdStart + 18, data.indexOf("</td>", tdStart)).split("\n").forEach { t ->
                if (t.contains("학년")) grades.add(t.replace("학년", "").trim().toInt())
            }
            val textareaStart = data.indexOf("readonly\">") + 10
            val textarea = data.substring(textareaStart, data.indexOf("</textarea>") + 11)
            return EventInfo(targetGrades = grades, details = textarea)
        }
    }
}