package ru.pronin_s_v.pocketaiscribe.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.pronin_s_v.pocketaiscribe.core.service.DictationProcessingService;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/audio")
public class AudioController {

    private final DictationProcessingService processingService;

    public AudioController(DictationProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Ошибка: Файл не должен быть пустым");
        }

        try {
            byte[] audioBytes = file.getBytes();
            
            processingService.processAudioRecord(audioBytes);
            
            return ResponseEntity.ok("Аудиофайл успешно принят в обработку. Размер: " + audioBytes.length + " байт");
            
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Ошибка при чтении аудиофайла: " + e.getMessage());
        }
    }
}