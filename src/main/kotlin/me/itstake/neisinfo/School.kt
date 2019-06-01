package me.itstake.neisinfo

import org.json.JSONObject
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

    var name = ""

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

    //INIT WITH NAME
    constructor(type: SchoolType, region: SchoolRegion, code: String, name: String) : this(type, region, code) {
        this.name = name
    }

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
            val results = JSONObject(data).getJSONObject("resultSVO").getJSONObject("data").getJSONArray("orgDVOList")
            val resultMap = ArrayList<School>()
            for (i in 0 until results.length()) {
                val si = results.getJSONObject(i)
                resultMap.add(
                    School(
                        SchoolType.getByType(si["schulCrseScCode"].toString().toInt()),
                        region,
                        si["orgCode"] as String,
                        si["kraOrgNm"] as String
                    )
                )
            }
            return resultMap
        }
    }

    /**
     * Get Meal data of Selected Month from NEIS Server.
     * @param year
     * @param month
     * @return Meal data. key is day of month, value is meal information object.
     */
    fun getMealMonthly(year: Int, month: Int): Map<Int, Meal> =
        NeisParser.parseSchoolMeals(
            URL("https://stu.${region.url}/sts_sci_md00_001.do?schulCode=$code&schulCrseScCode=${type.type}&schulKndScCode=0${type.type}&schYm=$year${String.format("%02d", month)}&")
                .readText())

    /**
     * Get Schedule data of Selected Month from NEIS Server.
     * @param year
     * @param month
     * @param deep Perform a deep find. this will make a lot of web requests, but you'll get details(which you can get with updateEventInfo() function in Event object) for event.
     * @return Schedule data. key is day of month, value is event information object.
     */
    fun getSchedule(year: Int, month: Int, deep: Boolean): Map<Int, Event> =
        NeisParser.parseSchoolSchedule(
            URL("https://stu.${region.url}/sts_sci_sf01_001.do?schulCode=$code&schulCrseScCode=${type.type}&schulKndScCode=0${type.type}&ay=$year&mm=${String.format("%02d", month)}&")
                .readText(), deep, this
        )

    /**
     * Get basic school information from NEIS Server.
     * @return Get School infomation based on JSON
     */
    fun getSchoolInfo(): Info =
        NeisParser.parseSchoolInfo(
            URL("https://stu.${region.url}/sts_sci_si00_001.do?schulCode=$code&schulCrseScCode=${type.type}&schulKndScCode=0${type.type}&")
                .readText())
}