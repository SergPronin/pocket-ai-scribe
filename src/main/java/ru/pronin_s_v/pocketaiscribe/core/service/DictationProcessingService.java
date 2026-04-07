package ru.pronin_s_v.pocketaiscribe.core.service;

import org.springframework.stereotype.Service;
import ru.pronin_s_v.pocketaiscribe.core.port.AudioTranscriber;
import ru.pronin_s_v.pocketaiscribe.core.port.NoteFormatter;
import ru.pronin_s_v.pocketaiscribe.core.port.NotePublisher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DictationProcessingService {

    private final AudioTranscriber transcriber;
    private final NoteFormatter formatter;
    private final NotePublisher publisher;

    // Spring сам подставит нужные реализации через конструктор
    public DictationProcessingService(AudioTranscriber transcriber, 
                                      NoteFormatter formatter, 
                                      NotePublisher publisher) {
        this.transcriber = transcriber;
        this.formatter = formatter;
        this.publisher = publisher;
    }

    public void processAudioRecord(byte[] audioData) {
        // 1. Переводим голос в текст
        String rawText = transcriber.transcribe(audioData);
        
        // 2. Форматируем нейросетью
        String markdownNote = formatter.formatToMarkdown(rawText);
        
        // 3. Генерируем название файла и сохраняем
        String title = "Note_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        publisher.publish(title, markdownNote);
    }
}