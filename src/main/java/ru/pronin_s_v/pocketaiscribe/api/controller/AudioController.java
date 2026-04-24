package ru.pronin_s_v.pocketaiscribe.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.pronin_s_v.pocketaiscribe.core.port.AudioTranscriber;
import ru.pronin_s_v.pocketaiscribe.core.service.DictationProcessingService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/audio")
public class AudioController {

    private final DictationProcessingService processingService;
    private final AudioTranscriber transcriber;

    public AudioController(DictationProcessingService processingService, AudioTranscriber transcriber) {
        this.processingService = processingService;
        this.transcriber = transcriber;
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

    @GetMapping(value = "/test-receive", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> testReceivePing() {
        return ResponseEntity.ok(
            "OK — сервер доступен.\nДля приёма аудио используйте POST с телом application/octet-stream.");
    }

    @PostMapping(value = "/test-receive", consumes = "application/octet-stream")
    public ResponseEntity<String> testReceiveAudio(
        @RequestBody byte[] audioBytes,
        @RequestParam(defaultValue = "false") boolean autoFormat) { // <-- НАШ ФЛАЖОК

        System.out.println(">>> Получено байт от ESP32: " + audioBytes.length);

        if (audioBytes == null || audioBytes.length == 0) {
            return ResponseEntity.badRequest().body("Пустой массив байтов!");
        }

        try {
            if (autoFormat) {
                System.out.println(">>> Включен режим AutoFormat! Отправляем в Сбер и Obsidian...");
                processingService.processAudioRecord(audioBytes);
                return ResponseEntity.ok("🎤 Заметка успешно создана в Obsidian!");
            }
            else {
                String fileName = "test_audio_" + System.currentTimeMillis() + ".wav";
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName)) {
                    fos.write(createWavHeader(audioBytes.length));
                    fos.write(audioBytes);
                }
                System.out.println(">>> Файл сохранен как WAV: " + fileName);
                return ResponseEntity.ok("Файл сохранен: " + fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping(value = "/test-sber", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> testSaluteSpeech(@RequestParam String filename) {
        try {
            System.out.println(">>> Пытаемся прочитать файл: " + filename);
            Path path = Paths.get(filename);

            if (!Files.exists(path)) {
                return ResponseEntity.badRequest().body("Файл не найден в корне проекта: " + filename);
            }

            byte[] audioBytes = Files.readAllBytes(path);
            System.out.println(">>> Файл прочитан. Размер: " + audioBytes.length + " байт. Запускаем полный пайплайн...");

            processingService.processAudioRecord(audioBytes);

            return ResponseEntity.ok("🎤 УСПЕХ! Полный цикл завершен. Проверь папку " + filename + " в Obsidian!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Ошибка: " + e.getMessage());
        }
    }

    private byte[] createWavHeader(int dataLength) {
        byte[] header = new byte[44];
        long totalDataLen = dataLength + 36;
        long sampleRate = 16000;
        long byteRate = sampleRate * 2;

        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff); header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff); header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0;
        header[20] = 1; header[21] = 0;
        header[22] = 1; header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff); header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff); header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff); header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff); header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = 2; header[33] = 0;
        header[34] = 16; header[35] = 0;
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        header[40] = (byte) (dataLength & 0xff); header[41] = (byte) ((dataLength >> 8) & 0xff);
        header[42] = (byte) ((dataLength >> 16) & 0xff); header[43] = (byte) ((dataLength >> 24) & 0xff);

        return header;
    }
}