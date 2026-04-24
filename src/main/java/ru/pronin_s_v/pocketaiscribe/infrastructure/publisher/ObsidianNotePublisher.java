package ru.pronin_s_v.pocketaiscribe.infrastructure.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.pronin_s_v.pocketaiscribe.core.port.NotePublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ObsidianNotePublisher implements NotePublisher {

    private final String inboxPath;

    public ObsidianNotePublisher(@Value("${obsidian.inbox.path}") String inboxPath) {
        this.inboxPath = inboxPath;
    }

    @Override
    public void publish(String title, String markdownContent) {
        try {
            Path directory = Paths.get(inboxPath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path filePath = directory.resolve(title + ".md");

            Files.writeString(filePath, markdownContent);
            System.out.println("✅ Заметка успешно сохранена в Obsidian: " + filePath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("❌ Ошибка при сохранении файла в Obsidian: " + e.getMessage());
            e.printStackTrace();
        }
    }
}