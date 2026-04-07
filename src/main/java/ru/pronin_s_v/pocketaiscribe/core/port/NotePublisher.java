package ru.pronin_s_v.pocketaiscribe.core.port;

public interface NotePublisher {
    // Принимает готовый Markdown и сохраняет его (на диск, в GitHub и т.д.)
    void publish(String title, String markdownContent); 
}