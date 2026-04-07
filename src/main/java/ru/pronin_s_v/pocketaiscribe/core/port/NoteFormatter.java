package ru.pronin_s_v.pocketaiscribe.core.port;

public interface NoteFormatter {
    // Принимает сырой текст, возвращает красивый Markdown
    String formatToMarkdown(String rawText); 
}