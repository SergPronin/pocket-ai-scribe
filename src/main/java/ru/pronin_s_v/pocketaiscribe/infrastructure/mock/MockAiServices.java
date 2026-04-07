package ru.pronin_s_v.pocketaiscribe.infrastructure.mock;

import org.springframework.stereotype.Service;
import ru.pronin_s_v.pocketaiscribe.core.port.AudioTranscriber;
import ru.pronin_s_v.pocketaiscribe.core.port.NoteFormatter;
import ru.pronin_s_v.pocketaiscribe.core.port.NotePublisher;

import java.util.logging.Logger;

@Service
public class MockAiServices implements AudioTranscriber, NoteFormatter, NotePublisher {

    private static final Logger log = Logger.getLogger(MockAiServices.class.getName());

    @Override
    public String transcribe(byte[] audioData) {
        log.info("MOCK: Имитация распознавания аудио... Принято " + audioData.length + " байт");
        return "Это тестовый текст, который якобы распознала нейросеть из голоса.";
    }

    @Override
    public String formatToMarkdown(String rawText) {
        log.info("MOCK: Имитация форматирования текста в Markdown...");
        return "# Тестовая заметка\n\n" + rawText + "\n\n- [ ] Задача 1\n- [ ] Задача 2";
    }

    @Override
    public void publish(String title, String markdownContent) {
        log.info("MOCK: Имитация сохранения файла в Obsidian...");
        log.info("Название файла: " + title + ".md\nСодержимое:\n" + markdownContent);
    }
}