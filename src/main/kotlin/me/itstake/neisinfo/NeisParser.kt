package me.itstake.neisinfo

import java.io.StringWriter
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.forEach
import kotlin.collections.hashMapOf
import kotlin.collections.set


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
                for (i in 18 downTo 1) {
                    val index = t.indexOf("$i.")
                    if (index > -1) allergies.add(MealMenu.AllergyInfo.getByKey(i)); name =
                        StringUtils.unescapeHtml3(name).replace("$i.", "")
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

        object StringUtils {

            private val ESCAPES = arrayOf(
                arrayOf("\"", "quot"), // " - double-quote
                arrayOf("&", "amp"), // & - ampersand
                arrayOf("<", "lt"), // < - less-than
                arrayOf(">", "gt"), // > - greater-than

                // Mapping to escape ISO-8859-1 characters to their named HTML 3.x equivalents.
                arrayOf("\u00A0", "nbsp"), // non-breaking space
                arrayOf("\u00A1", "iexcl"), // inverted exclamation mark
                arrayOf("\u00A2", "cent"), // cent sign
                arrayOf("\u00A3", "pound"), // pound sign
                arrayOf("\u00A4", "curren"), // currency sign
                arrayOf("\u00A5", "yen"), // yen sign = yuan sign
                arrayOf("\u00A6", "brvbar"), // broken bar = broken vertical bar
                arrayOf("\u00A7", "sect"), // section sign
                arrayOf("\u00A8", "uml"), // diaeresis = spacing diaeresis
                arrayOf("\u00A9", "copy"), // © - copyright sign
                arrayOf("\u00AA", "ordf"), // feminine ordinal indicator
                arrayOf("\u00AB", "laquo"), // left-pointing double angle quotation mark = left pointing guillemet
                arrayOf("\u00AC", "not"), // not sign
                arrayOf("\u00AD", "shy"), // soft hyphen = discretionary hyphen
                arrayOf("\u00AE", "reg"), // ® - registered trademark sign
                arrayOf("\u00AF", "macr"), // macron = spacing macron = overline = APL overbar
                arrayOf("\u00B0", "deg"), // degree sign
                arrayOf("\u00B1", "plusmn"), // plus-minus sign = plus-or-minus sign
                arrayOf("\u00B2", "sup2"), // superscript two = superscript digit two = squared
                arrayOf("\u00B3", "sup3"), // superscript three = superscript digit three = cubed
                arrayOf("\u00B4", "acute"), // acute accent = spacing acute
                arrayOf("\u00B5", "micro"), // micro sign
                arrayOf("\u00B6", "para"), // pilcrow sign = paragraph sign
                arrayOf("\u00B7", "middot"), // middle dot = Georgian comma = Greek middle dot
                arrayOf("\u00B8", "cedil"), // cedilla = spacing cedilla
                arrayOf("\u00B9", "sup1"), // superscript one = superscript digit one
                arrayOf("\u00BA", "ordm"), // masculine ordinal indicator
                arrayOf("\u00BB", "raquo"), // right-pointing double angle quotation mark = right pointing guillemet
                arrayOf("\u00BC", "frac14"), // vulgar fraction one quarter = fraction one quarter
                arrayOf("\u00BD", "frac12"), // vulgar fraction one half = fraction one half
                arrayOf("\u00BE", "frac34"), // vulgar fraction three quarters = fraction three quarters
                arrayOf("\u00BF", "iquest"), // inverted question mark = turned question mark
                arrayOf("\u00C0", "Agrave"), // А - uppercase A, grave accent
                arrayOf("\u00C1", "Aacute"), // Б - uppercase A, acute accent
                arrayOf("\u00C2", "Acirc"), // В - uppercase A, circumflex accent
                arrayOf("\u00C3", "Atilde"), // Г - uppercase A, tilde
                arrayOf("\u00C4", "Auml"), // Д - uppercase A, umlaut
                arrayOf("\u00C5", "Aring"), // Е - uppercase A, ring
                arrayOf("\u00C6", "AElig"), // Ж - uppercase AE
                arrayOf("\u00C7", "Ccedil"), // З - uppercase C, cedilla
                arrayOf("\u00C8", "Egrave"), // И - uppercase E, grave accent
                arrayOf("\u00C9", "Eacute"), // Й - uppercase E, acute accent
                arrayOf("\u00CA", "Ecirc"), // К - uppercase E, circumflex accent
                arrayOf("\u00CB", "Euml"), // Л - uppercase E, umlaut
                arrayOf("\u00CC", "Igrave"), // М - uppercase I, grave accent
                arrayOf("\u00CD", "Iacute"), // Н - uppercase I, acute accent
                arrayOf("\u00CE", "Icirc"), // О - uppercase I, circumflex accent
                arrayOf("\u00CF", "Iuml"), // П - uppercase I, umlaut
                arrayOf("\u00D0", "ETH"), // Р - uppercase Eth, Icelandic
                arrayOf("\u00D1", "Ntilde"), // С - uppercase N, tilde
                arrayOf("\u00D2", "Ograve"), // Т - uppercase O, grave accent
                arrayOf("\u00D3", "Oacute"), // У - uppercase O, acute accent
                arrayOf("\u00D4", "Ocirc"), // Ф - uppercase O, circumflex accent
                arrayOf("\u00D5", "Otilde"), // Х - uppercase O, tilde
                arrayOf("\u00D6", "Ouml"), // Ц - uppercase O, umlaut
                arrayOf("\u00D7", "times"), // multiplication sign
                arrayOf("\u00D8", "Oslash"), // Ш - uppercase O, slash
                arrayOf("\u00D9", "Ugrave"), // Щ - uppercase U, grave accent
                arrayOf("\u00DA", "Uacute"), // Ъ - uppercase U, acute accent
                arrayOf("\u00DB", "Ucirc"), // Ы - uppercase U, circumflex accent
                arrayOf("\u00DC", "Uuml"), // Ь - uppercase U, umlaut
                arrayOf("\u00DD", "Yacute"), // Э - uppercase Y, acute accent
                arrayOf("\u00DE", "THORN"), // Ю - uppercase THORN, Icelandic
                arrayOf("\u00DF", "szlig"), // Я - lowercase sharps, German
                arrayOf("\u00E0", "agrave"), // а - lowercase a, grave accent
                arrayOf("\u00E1", "aacute"), // б - lowercase a, acute accent
                arrayOf("\u00E2", "acirc"), // в - lowercase a, circumflex accent
                arrayOf("\u00E3", "atilde"), // г - lowercase a, tilde
                arrayOf("\u00E4", "auml"), // д - lowercase a, umlaut
                arrayOf("\u00E5", "aring"), // е - lowercase a, ring
                arrayOf("\u00E6", "aelig"), // ж - lowercase ae
                arrayOf("\u00E7", "ccedil"), // з - lowercase c, cedilla
                arrayOf("\u00E8", "egrave"), // и - lowercase e, grave accent
                arrayOf("\u00E9", "eacute"), // й - lowercase e, acute accent
                arrayOf("\u00EA", "ecirc"), // к - lowercase e, circumflex accent
                arrayOf("\u00EB", "euml"), // л - lowercase e, umlaut
                arrayOf("\u00EC", "igrave"), // м - lowercase i, grave accent
                arrayOf("\u00ED", "iacute"), // н - lowercase i, acute accent
                arrayOf("\u00EE", "icirc"), // о - lowercase i, circumflex accent
                arrayOf("\u00EF", "iuml"), // п - lowercase i, umlaut
                arrayOf("\u00F0", "eth"), // р - lowercase eth, Icelandic
                arrayOf("\u00F1", "ntilde"), // с - lowercase n, tilde
                arrayOf("\u00F2", "ograve"), // т - lowercase o, grave accent
                arrayOf("\u00F3", "oacute"), // у - lowercase o, acute accent
                arrayOf("\u00F4", "ocirc"), // ф - lowercase o, circumflex accent
                arrayOf("\u00F5", "otilde"), // х - lowercase o, tilde
                arrayOf("\u00F6", "ouml"), // ц - lowercase o, umlaut
                arrayOf("\u00F7", "divide"), // division sign
                arrayOf("\u00F8", "oslash"), // ш - lowercase o, slash
                arrayOf("\u00F9", "ugrave"), // щ - lowercase u, grave accent
                arrayOf("\u00FA", "uacute"), // ъ - lowercase u, acute accent
                arrayOf("\u00FB", "ucirc"), // ы - lowercase u, circumflex accent
                arrayOf("\u00FC", "uuml"), // ь - lowercase u, umlaut
                arrayOf("\u00FD", "yacute"), // э - lowercase y, acute accent
                arrayOf("\u00FE", "thorn"), // ю - lowercase thorn, Icelandic
                arrayOf("\u00FF", "yuml")
            )// я - lowercase y, umlaut

            private const val MIN_ESCAPE = 2
            private const val MAX_ESCAPE = 6

            private val lookupMap: HashMap<String, CharSequence> = HashMap()

            fun unescapeHtml3(input: String): String {
                var writer: StringWriter? = null
                val len = input.length
                var i = 1
                var st = 0
                while (true) {
                    // look for '&'
                    while (i < len && input[i - 1] != '&')
                        i++
                    if (i >= len)
                        break

                    // found '&', look for ';'
                    var j = i
                    while (j < len && j < i + MAX_ESCAPE + 1 && input[j] != ';')
                        j++
                    if (j == len || j < i + MIN_ESCAPE || j == i + MAX_ESCAPE + 1) {
                        i++
                        continue
                    }

                    // found escape
                    if (input[i] == '#') {
                        // numeric escape
                        var k = i + 1
                        var radix = 10

                        val firstChar = input[k]
                        if (firstChar == 'x' || firstChar == 'X') {
                            k++
                            radix = 16
                        }

                        try {
                            val entityValue = Integer.parseInt(input.substring(k, j), radix)

                            if (writer == null)
                                writer = StringWriter(input.length)
                            writer.append(input.substring(st, i - 1))

                            if (entityValue > 0xFFFF) {
                                val chrs = Character.toChars(entityValue)
                                writer.write(chrs[0].toInt())
                                writer.write(chrs[1].toInt())
                            } else {
                                writer.write(entityValue)
                            }

                        } catch (ex: NumberFormatException) {
                            i++
                            continue
                        }

                    } else {
                        // named escape
                        val value = lookupMap[input.substring(i, j)]
                        if (value == null) {
                            i++
                            continue
                        }

                        if (writer == null)
                            writer = StringWriter(input.length)
                        writer.append(input.substring(st, i - 1))

                        writer.append(value)
                    }

                    // skip escape
                    st = j + 1
                    i = st
                }

                if (writer != null) {
                    writer.append(input.substring(st, len))
                    return writer.toString()
                }
                return input
            }

            init {
                for (seq in ESCAPES)
                    lookupMap[seq[1]] = seq[0]
            }

        }
    }
}