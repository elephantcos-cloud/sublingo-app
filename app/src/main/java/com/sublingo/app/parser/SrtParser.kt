package com.sublingo.app.parser

import com.sublingo.app.data.model.SubtitleLine

object SrtParser {

    fun parse(content: String): List<SubtitleLine> {
        val lines = mutableListOf<SubtitleLine>()
        val blocks = content.trim().split(Regex("\r?\n\r?\n"))

        for (block in blocks) {
            val blockLines = block.trim().lines()
            if (blockLines.size < 3) continue

            val index = blockLines[0].trim().toIntOrNull() ?: continue
            val timeLine = blockLines[1]
            val timeParts = timeLine.split("-->")
            if (timeParts.size != 2) continue

            val startTime = parseTime(timeParts[0].trim())
            val endTime = parseTime(timeParts[1].trim())
            val text = blockLines.drop(2)
                .joinToString(" ")
                .replace(Regex("<[^>]*>"), "")  // strip HTML tags
                .trim()

            if (text.isNotEmpty()) {
                lines.add(SubtitleLine(index, startTime, endTime, text))
            }
        }
        return lines
    }

    fun generate(lines: List<SubtitleLine>): String {
        val sb = StringBuilder()
        lines.forEachIndexed { i, line ->
            sb.append(i + 1)
            sb.append("\n")
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
            val hours = parts[0].trim().toLong()
            val minutes = parts[1].trim().toLong()
            val secParts = parts[2].trim().split(".")
            val seconds = secParts[0].toLong()
            val millis = secParts.getOrNull(1)?.padEnd(3, '0')?.substring(0, 3)?.toLong() ?: 0L
            hours * 3_600_000L + minutes * 60_000L + seconds * 1_000L + millis
        } catch (e: Exception) { 0L }
    }

    private fun formatTime(ms: Long): String {
        val h = ms / 3_600_000L
        val m = (ms % 3_600_000L) / 60_000L
        val s = (ms % 60_000L) / 1_000L
        val mil = ms % 1_000L
        return "%02d:%02d:%02d,%03d".format(h, m, s, mil)
    }
}
