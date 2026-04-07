package ru.pronin_s_v.pocketaiscribe.core.port;

public interface AudioTranscriber {
    // Принимает байты аудиофайла, возвращает сырой текст
    String transcribe(byte[] audioData);
}