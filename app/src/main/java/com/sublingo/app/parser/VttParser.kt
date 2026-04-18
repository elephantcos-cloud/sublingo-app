package com.sublingo.app.parser

import com.sublingo.app.data.model.SubtitleLine

object VttParser {

    fun parse(content: String): List<SubtitleLine> {
        val lines = mutableListOf<SubtitleLine>()
        val rawLines = content.lines()
        var index = 1
        var i = 0

        // Skip WEBVTT header
        while (i < rawLines.size && !rawLines[i].contains("-->")) i++

        while (i < rawLines.size) {
            val line = rawLines[i].trim()
            if (line.contains("-->")) {
                val timeParts = line.split("-->")
                if (timeParts.size == 2) {
                    val startTime = parseTime(timeParts[0].trim().split(" ")[0])
                    val endTime = parseTime(timeParts[1].trim().split(" ")[0])
                    val textLines = mutableListOf<String>()
                    i++
                    while (i < rawLines.size && rawLines[i].isNotBlank()) {
                        textLines.add(rawLines[i].replace(Regex("<[^>]*>"), "").trim())
                        i++
                    }
                    val text = textLines.joinToString(" ").trim()
                    if (text.isNotEmpty()) {
                        lines.add(SubtitleLine(index++, startTime, endTime, text))
                    }
                }
            }
            i++
        }
        return lines
    }

    fun generate(lines: List<SubtitleLine>): String {
        val sb = StringBuilder("WEBVTT\n\n")
        lines.forEach { line ->
            sb.append(formatTime(line.startTime))
            sb.append(" --> ")
            sb.append(formatTime(line.endTime))
            sb.append("\n")
            sb.append(line.translatedText ?: line.originalText)
            sb.append("\n\n")
        }
        return sb.toString().trimEnd()
    }

    private fun parseTime(time: String): Long {
        return try {
            val normalized = time.replace(",", ".")
            val parts = normalized.split(":")
            when (parts.size) {
                3 -> {
                    val h = parts[0].toLong()
                    val m = parts[1].toLong()
                    val secParts = parts[2].split(".")
                    val s = secParts[0].toLong()
                    val mil = secParts.getOrNull(1)?.padEnd(3, '0')?.substring(0, 3)?.toLong() ?: 0L
                    h * 3_600_000L + m * 60_000L + s * 1_000L + mil
                }
                2 -> {
                    val m = parts[0].toLong()
                    val secParts = parts[1].split(".")
                    val s = secParts[0].toLong()
                    val mil = secParts.getOrNull(1)?.padEnd(3, '0')?.substring(0, 3)?.toLong() ?: 0L
                    m * 60_000L + s * 1_000L + mil
                }
                else -> 0L
            }
        } catch (e: Exception) { 0L }
    }

    private fun formatTime(ms: Long): String {
        val h = ms / 3_600_000L
        val m = (ms % 3_600_000L) / 60_000L
        val s = (ms % 60_000L) / 1_000L
        val mil = ms % 1_000L
        return "%02d:%02d:%02d.%03d".format(h, m, s, mil)
    }
}
