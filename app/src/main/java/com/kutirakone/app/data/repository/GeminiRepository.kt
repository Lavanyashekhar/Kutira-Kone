package com.kutirakone.app.data.repository

import android.graphics.Bitmap
import android.graphics.Matrix
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.kutirakone.app.data.model.DesignIdea
import com.kutirakone.app.data.model.Difficulty
import com.kutirakone.app.data.model.MaterialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

class GeminiRepository(apiKey: String) {

    private val gemini = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey    = apiKey,
        generationConfig = generationConfig {
            temperature     = 0.1f   // very low = more deterministic
            maxOutputTokens = 200
        }
    )

    suspend fun classifyFabric(bitmap: Bitmap): Pair<MaterialType, Float> =
        withContext(Dispatchers.IO) {
            try {
                withTimeout(15_000) {

                    // Resize bitmap for better analysis — too small = wrong result
                    val resized = resizeBitmap(bitmap, 512)

                    val prompt = """
You are a textile expert specializing in Indian fabrics.
Analyze this fabric image very carefully.

CLASSIFICATION GUIDE:
- SILK: Shiny, lustrous, smooth surface. Used in sarees, blouses. Has a natural sheen that reflects light. Often colorful prints.
- COTTON: Matte, dull surface. Soft texture. Plain weave visible. Everyday wear. No shine at all.
- WOOL: Thick, fuzzy, textured. Warm-looking. Knitted or woven with visible texture. Often grey, brown, cream.
- SYNTHETIC: Polyester/nylon. Artificial sheen. Wrinkle-free look. Uniform color.

IMPORTANT RULES:
- Do NOT always say COTTON. Look carefully at the shine level.
- If the fabric has ANY shine or luster, it is likely SILK or SYNTHETIC.
- If it is thick and textured, it is WOOL.
- Only say COTTON if it is clearly matte with no shine.
- Confidence should reflect how certain you are (0.60 to 0.95).

Reply ONLY in this exact JSON format with no other text:
{"material":"SILK","confidence":0.85}
                    """.trimIndent()

                    val response = gemini.generateContent(
                        content {
                            image(resized)
                            text(prompt)
                        }
                    )

                    val raw     = response.text?.trim() ?: ""
                    val cleaned = raw.replace("```json","").replace("```","")
                        .replace("\n","").trim()

                    if (cleaned.isEmpty() || !cleaned.contains("{")) {
                        return@withTimeout Pair(MaterialType.COTTON, 0.5f)
                    }

                    val start = cleaned.indexOf("{")
                    val end   = cleaned.lastIndexOf("}") + 1
                    val json  = JSONObject(cleaned.substring(start, end))

                    val matStr = json.getString("material").uppercase().trim()
                    val mat = when {
                        matStr.contains("SILK")      -> MaterialType.SILK
                        matStr.contains("WOOL")      -> MaterialType.WOOL
                        matStr.contains("SYNTHETIC") ||
                                matStr.contains("POLY")      -> MaterialType.SYNTHETIC
                        else                         -> MaterialType.COTTON
                    }
                    val conf = json.getDouble("confidence").toFloat().coerceIn(0.55f, 0.95f)
                    Pair(mat, conf)
                }
            } catch (e: Exception) {
                Pair(MaterialType.COTTON, 0.5f)
            }
        }

    suspend fun generateDesignIdeas(
        material: MaterialType,
        sizeMeters: Double,
        color: String
    ): List<DesignIdea> = withContext(Dispatchers.IO) {
        try {
            withTimeout(15_000) {
                val prompt = """
Generate 5 creative DIY upcycling ideas for a ${material.name} fabric scrap.
Size: ${sizeMeters}m, Color: $color
For rural Indian artisans with basic tools.
Reply ONLY as JSON array:
[{"projectName":"Silk Hair Scrunchie","difficulty":"EASY","estimatedMinutes":20,"stepsPreview":"Cut strip, fold, stitch ends together","materialsNeeded":["elastic","needle","thread"]}]
                """.trimIndent()

                val response = gemini.generateContent(content { text(prompt) })
                val raw = response.text?.trim() ?: return@withTimeout emptyList()
                val cleaned = raw.replace("```json","").replace("```","").trim()
                val startIdx = cleaned.indexOf("[")
                val endIdx   = cleaned.lastIndexOf("]") + 1
                if (startIdx < 0 || endIdx <= startIdx) return@withTimeout emptyList()

                val arr = JSONArray(cleaned.substring(startIdx, endIdx))
                List(arr.length()) { i ->
                    val obj  = arr.getJSONObject(i)
                    val mats = obj.getJSONArray("materialsNeeded")
                    DesignIdea(
                        scrapMaterial    = material,
                        scrapSizeRange   = "${sizeMeters}m",
                        projectName      = obj.getString("projectName"),
                        difficulty       = try {
                            Difficulty.valueOf(obj.getString("difficulty").uppercase())
                        } catch (e: Exception) { Difficulty.EASY },
                        estimatedMinutes = obj.getInt("estimatedMinutes"),
                        stepsPreview     = obj.getString("stepsPreview"),
                        materialsNeeded  = List(mats.length()) { j -> mats.getString(j) },
                        aiGenerated      = true
                    )
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Resize bitmap so Gemini gets enough detail
    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxSize && h <= maxSize) return bitmap
        val scale = maxSize.toFloat() / maxOf(w, h)
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true)
    }
}