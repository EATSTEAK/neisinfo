package me.itstake.neisinfo

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.URL
import java.net.URLEncoder

/**
 * Basic object for get Information about Schools.
 *
 * @property type School type of selected school.
 * @property region School region of selected school.
 * @property code School code of selected school.
 * @constructor Creates an school object within basic information.
 */
class School(val type:SchoolType, val region:SchoolRegion, val code:String) {
    // ENUMS
    enum class SchoolType(val type:Int) {
        KINDERGARDEN(1),
        ELEMENTARY(2),
        MIDDLE(3),
        HIGH(4);

        companion object {
            private val values = values()
            fun getByType(type: Int) = values.first {
                it.type == type
            }
        }
    }
    enum class SchoolRegion(val url:String) {
        SEOUL("sen.go.kr"),
        INCHEON("ice.go.kr"),
        BUSAN("pen.go.kr"),
        GWANGJU("gen.go.kr"),
        DAEJEON("dje.go.kr"),
        DAEGU("dge.go.kr"),
        SEJONG("sje.go.kr"),
        ULSAN("use.go.kr"),
        GYEONGGI("goe.go.kr"),
        KANGWON("kwe.go.kr"),
        CHUNGBUK("cbe.go.kr"),
        CHUNGNAM("cne.go.kr"),
        GYEONGBUK("gbe.go.kr"),
        GYEONGNAM("gne.go.kr"),
        JEONBUK("jbe.go.kr"),
        JEONNAM("jne.go.kr"),
        JEJU("jje.go.kr")
    }

    //URL FOR REQUEST

    // STATIC METHODS FOR FIND SCHOOL
    companion object {

        /**
         * Use this function to lookup school by name and region.
         *
         * @param region Region of School that you find.
         * @param name Name(or part of name) of School that you find.
         * @return Array List of search results of School.
         */
        fun findSchool(region: SchoolRegion, name: String):ArrayList<School> {
            val data = URL("https://par.${region.url}/spr_ccm_cm01_100.do?kraOrgNm=${URLEncoder.encode(name, "utf-8")}&").readText(Charsets.UTF_8)
            val results = ((JSONParser().parse(data) as JSONObject).get("resultSVO") as? JSONObject)?.get("orgDVOList") as? JSONArray
                ?:
                return arrayListOf()
            val resultMap = ArrayList<School>()
            results.forEach { u ->
                val si = u as JSONObject
                resultMap.add(School(SchoolType.getByType(si["schulCrseScCode"].toString().toInt()), region, si["orgCode"] as String))
            }
            return resultMap
        }
    }

    /**
     * Get Meal data of Selected Month from NEIS Server.
     * @param year
     * @param month
     * @return Meal data based on JSON
     */
    fun getMealMonthly(year: Int, month: Int): JSONObject =
        NeisParser.parseSchoolMeals(
            URL("https://stu.${region.url}/sts_sci_md00_001.do?schulCode=$code&schulCrseScCode=${type.type}&schulKndScCode=0${type.type}&schYm=$year${String.format("%02d", month)}&")
                .readText())

    /**
     * Get Schedule data of Selected Month from NEIS Server.
     * @param year
     * @param month
     * @return Schedule data based on JSON
     */
    fun getSchedule(year: Int, month: Int): JSONObject =
        NeisParser.parseSchoolSchedule(
            URL("https://stu.${region.url}/sts_sci_sf01_001.do?schulCode=$code&schulCrseScCode=${type.type}&schulKndScCode=0${type.type}&ay=$year&mm=${String.format("%02d", month)}&")
                .readText())

    /**
     * Get basic school information from NEIS Server.
     * @return Get School infomation based on JSON
     */
    fun getSchoolInfo(): SchoolInfo =
        NeisParser.parseSchoolInfo(
            URL("https://stu.${region.url}/sts_sci_si00_001.do?schulCode=$code&schulCrseScCode=${type.type}&schulKndScCode=0${type.type}&")
                .readText())
}