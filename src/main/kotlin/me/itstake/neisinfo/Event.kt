package me.itstake.neisinfo

import java.net.URL

data class Event(val name: String, val code: String, val date: String, var info: EventInfo? = null) {

    /**
     * This will update event's info with details about it. But It needs request to web.
     * @param school
     * @return JSON Object with indexes targetGrades and details.
     */
    fun updateEventInfo(school: School): EventInfo? {
        this.info =
            NeisParser.parseEventInfo(URL("https://stu.${school.region.url}/sts_sci_sf01_002.do?schulCode=${school.code}&schulCrseScCode=${school.type.type}&schulKndScCode=0${school.type.type}&eventCode=$code&eventDate=$date").readText())
        return this.info
    }
}