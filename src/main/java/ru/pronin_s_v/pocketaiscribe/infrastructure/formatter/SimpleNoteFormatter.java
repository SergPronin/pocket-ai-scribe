package ru.pronin_s_v.pocketaiscribe.infrastructure.formatter;

import org.springframework.stereotype.Component;
import ru.pronin_s_v.pocketaiscribe.core.port.NoteFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SimpleNoteFormatter implements NoteFormatter {

    @Override
    public String formatToMarkdown(String rawText) {
        String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        return """
                ---
                tags: [voice_note, pocket_scribe]
                date: %s
                ---
                
                # Голосовая заметка
                
                %s
                """.formatted(dateString, rawText);
    }
}