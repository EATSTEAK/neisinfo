package me.itstake.neisinfo

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class NeisInfoTest {

    val type = School.SchoolType.HIGH
    val region = School.SchoolRegion.BUSAN
    val name = "광명고등학교"
    val code = "C100000357"
    val year = 2019
    val month = 3

    // FUNCTIONAL TESTS
    @Test
    fun searchTest() {
        val codes = School.findSchool(School.SchoolRegion.BUSAN, name)
        assertEquals(1, codes.size, "Returned Map's size is wrong.")
        assertEquals(type, codes[0].type, "Returned Map's target school type is wrong.")
        assertEquals(region, codes[0].region, "Returned Map's target school region is wrong.")
        assertEquals(code, codes[0].code, "Returned Map's target school code is wrong.")
        //assertEquals(name, codes[0].getSchoolInfo()["name"], "Returned Map not contains target school name.")
        println("TEST SUCCESS")
    }

    @Test
    fun schoolInfoTest() {
        val school = School(type, region, code)
        /*val oriInfo = JSONParser().parse("{\"name\":\"광명고등학교\",\"zipCode\":49094,\"address\":\"부산광역시 영도구 와치로 131(동삼동, 광명고등학교)\",\"callNum\":\"051-405-6302\",\"faxNum\":\"051-405-6289\",\"homepage\":\"http://www.km.hs.kr\",\"stuNum\":370,\"stuNumMen\":370,\"stuNumWomen\":0,\"classNumByGrade\":[6,6,7],\"teacherNum\":43}") as JSONObject
        val info = school.getSchoolInfo()
        oriInfo.forEach { t, u ->
            assertTrue(info.containsKey(t), "Returned Info hasn't key $t.")
            assertEquals(oriInfo[t], info[t], "Value mismatched in key $t, Expected value is $u, but ${info[t]} returned.")
        }
        println("TEST SUCCESS")
        println("RETURNED JSON STRING: ${info.toJSONString()}")*/
    }

    @Test
    fun schoolMealTest() {
        val school = School(type, region, code)
        val meals = school.getMealMonthly(year, month)
        assertEquals("오곡밥 ★", meals[4]?.lunch?.get(0)?.name, "Returned Meal Schedule is incorrect.")
        println("TEST SUCCESS")
    }

    @Test
    fun schoolScheduleTest() {
        val school = School(type, region, code)
        val schedule = school.getSchedule(year, month, false)
        assertEquals("3・1절", (schedule[1] as Event).name, "Returned School Schedule's data test 1 failed.")
        assertFalse(schedule.containsKey(3), "Returned School Schedule's data test 2 failed.")
        println("TEST SUCCESS")
    }

    @Test
    fun schoolScheduleDeepTest() {
        val school = School(type, region, code)
        val schedule = school.getSchedule(year, month, true)
        assertEquals("3・1절", schedule[1]?.name, "Returned School Schedule's data test 1 failed.")
        assertFalse(schedule.containsKey(3), "Returned School Schedule's data test 2 failed.")
        println("TEST SUCCESS")
    }

    //UNIT TESTS

    @Test
    fun schoolInfoParseTest() {
        val html = File("src/test/resources/neisschoolinfo.html").readText()
        val startTime = System.nanoTime()
        val info = NeisParser.parseSchoolInfo(html)
        val endTime = System.nanoTime()
        println("PARSE COMPLETE")
        println("Time taken:${TimeUnit.NANOSECONDS.toMillis(endTime - startTime)} ms")
        /*val oriInfo = JSONParser().parse("{\"name\":\"광명고등학교\",\"zipCode\":49094,\"address\":\"부산광역시 영도구 와치로 131(동삼동, 광명고등학교)\",\"callNum\":\"051-405-6302\",\"faxNum\":\"051-405-6289\",\"homepage\":\"http://www.km.hs.kr\",\"stuNum\":370,\"stuNumMen\":370,\"stuNumWomen\":0,\"classNumByGrade\":[6,6,7],\"teacherNum\":43}") as JSONObject
        oriInfo.forEach { t, u ->
            assertTrue(info.containsKey(t), "Returned Info hasn't key $t.")
            assertEquals(oriInfo[t], info[t], "Value mismatched in key $t, Expected value is $u, but ${info[t]} returned.")
        }*/
        println("TEST SUCCESS")
    }

    @Test
    fun schoolMealParseTest() {
        val html = File("src/test/resources/neisschoolmeal.html").readText()
        val startTime = System.nanoTime()
        val meals = NeisParser.parseSchoolMeals(html)
        val endTime = System.nanoTime()
        println("PARSE COMPLETE")
        println("Time taken:${TimeUnit.NANOSECONDS.toMillis(endTime - startTime)} ms")
        assertEquals("오곡밥 ★", meals[4]?.lunch?.get(0)?.name, "Returned Meal Schedule is incorrect.")
        println("TEST SUCCESS")
    }

    @Test
    fun schoolScheduleParseTest() {
        val html = File("src/test/resources/neisschoolschedule.html").readText()
        val startTime = System.nanoTime()
        val schedule = NeisParser.parseSchoolSchedule(html, false, null)
        val endTime = System.nanoTime()
        println("PARSE COMPLETE")
        println("Time taken:${TimeUnit.NANOSECONDS.toMillis(endTime - startTime)} ms")
        assertEquals("3・1절", (schedule[1] as Event).name, "Returned School Schedule's data test 1 failed.")
        assertFalse(schedule.containsKey(3), "Returned School Schedule's data test 2 failed.")
        println("TEST SUCCESS")
    }
}