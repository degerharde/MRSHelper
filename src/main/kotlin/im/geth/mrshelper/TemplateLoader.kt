package im.geth.mrshelper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object TemplateLoader {
    private val gson = Gson()

    data class TemplateInfo(val file: String, val name: String, val description: String)
    data class TemplateGroup(val label: String, val templates: List<TemplateInfo>)

    fun loadMetadata(): Map<String, TemplateGroup> {
        val stream = javaClass.classLoader.getResourceAsStream("templates/metadata.json")
            ?: throw IllegalArgumentException("metadata.json not found")
        val reader = InputStreamReader(stream)

        val type = object : TypeToken<Map<String, TemplateGroup>>() {}.type
        return gson.fromJson(reader, type)
    }

    fun loadTemplate(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream("templates/$path")
        return stream?.reader()?.readText() ?: ""
    }
}