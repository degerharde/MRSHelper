package im.geth.mrshelper

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange

class TemplateAction {
    companion object {
        const val PLACEHOLDER = "{{PLACEHOLDER}}"
    }
    fun insertTemplate(project: Project, templatePath: String) {
        val editor = EditorFactory.getInstance().allEditors.find { it.project == project } ?: return
        val document = editor.document
        val selection = editor.selectionModel

        var content = TemplateLoader.loadTemplate(templatePath).replace("\r\n", "\n")

        val hasSelection = selection.hasSelection()
        val selectedText = selection.selectedText
        val caretOffset = if (hasSelection) selection.selectionStart else editor.caretModel.offset
        val baseShift = getCurrentLineShift(document, caretOffset)

        val cleanedOffset = removeSurroundingBraces(document, caretOffset)

        content = if (hasSelection && !selectedText.isNullOrBlank()) {
            val adjustedSelection = adjustShift(selectedText, baseShift)

            content.replace(PLACEHOLDER, adjustedSelection)
        } else {
            content.replace(PLACEHOLDER, "{ }")
        }

        val shiftedResult = shiftAllLines(content, baseShift)

        WriteCommandAction.runWriteCommandAction(project) {
            if (hasSelection) {
                document.deleteString(selection.selectionStart, selection.selectionEnd)
            }
            document.insertString(cleanedOffset, shiftedResult)
        }
    }

    private fun adjustShift(selectedText: String, baseShift: String): String {
        val lines = selectedText.trimEnd().lines()
        if (lines.size == 1) return selectedText
        val shift = lines.mapIndexed { i, line ->
            if (i == 0) line else "\t" + line.replace(baseShift, "")
        }
        return shift.joinToString("\n")
    }

    private fun getCurrentLineShift(document: Document, offset: Int): String {
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, offset))
        return lineText.takeWhile { it == ' ' || it == '\t' }
    }

    private fun shiftAllLines(text: String, shift: String): String {
        return text.lines().mapIndexed { index, line ->
            if (index == 0 || line.isBlank()) line else shift + line
        }.joinToString("\n").trimEnd()
    }

    private fun removeSurroundingBraces( document: Document, offset: Int): Int {
        val text = document.charsSequence
        val startIndex = (offset - 1 downTo 0)
            .dropWhile { text[it].isWhitespace() }
            .firstOrNull()
        val start = startIndex?.let { text[it] } ?: return offset
        if (start != '{') return offset

        val endIndex = (offset until  text.length)
            .dropWhile { text[it].isWhitespace() }
            .firstOrNull()
        val end = endIndex?.let { text[it] } ?: return offset
        if (end != '}') return offset
            WriteCommandAction.runWriteCommandAction(null) {
                document.deleteString(startIndex, endIndex + 1)
            }
            return startIndex
    }
}
