package me.itstake.neisinfo

import org.json.simple.JSONObject
import java.net.URL

class SchoolEvent(val name: String, val code: String, val date: String) : JSONObject() {

    init {
        this["name"] = name
        this["code"] = code
        this["date"] = date
        this["info"] = JSONObject()
    }

    constructor(name: String, code: String, date: String, info: JSONObject) : this(name, code, date) {
        this["info"] = info
    }

    /**
     * This will update event's info with details about it. But It needs request to web.
     * @param school
     * @return JSON Object with indexes targetGrades and details.
     */
    fun updateEventInfo(school: School): JSONObject {
        this["info"] =
            NeisParser.parseEventInfo(URL("https://stu.${school.region.url}/sts_sci_sf01_002.do?schulCode=${school.code}&schulCrseScCode=${school.type.type}&schulKndScCode=0${school.type.type}&eventCode=$code&eventDate=$date").readText())
        return (this["info"] as JSONObject)
    }
}