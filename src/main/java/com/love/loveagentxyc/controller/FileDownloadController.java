package com.love.loveagentxyc.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将 Agent 写入 tmp 目录的文件通过 HTTP 提供给浏览器下载
 */
@RestController
@RequestMapping("/files")
@CrossOrigin(originPatterns = "*")
public class FileDownloadController {

    private static final String TMP = "tmp";

    @GetMapping("/download/{category}/{filename:.+}")
    public ResponseEntity<Resource> download(
            @PathVariable String category,
            @PathVariable String filename) {
        if (!isAllowedCategory(category)) {
            return ResponseEntity.notFound().build();
        }
        if (!isSafeFileName(filename)) {
            return ResponseEntity.badRequest().build();
        }

        Path base = Paths.get(System.getProperty("user.dir"), TMP, category).normalize();
        Path file = base.resolve(filename).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(file.getFileName().toString(), StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = filename.toLowerCase().endsWith(".pdf")
                ? MediaType.APPLICATION_PDF
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    private static boolean isAllowedCategory(String category) {
        return "pdf".equals(category) || "file".equals(category) || "download".equals(category);
    }


    static boolean isSafeFileName(String name) {
        if (name == null || name.isEmpty() || name.length() > 255) {
            return false;
        }
        return !name.contains("..") && !name.contains("/") && !name.contains("\\");
    }
}
