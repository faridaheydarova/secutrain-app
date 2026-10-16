package com.securityawareness.secutrainapp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Component
@Slf4j
public class FileUploadUtil {

    @Value("${file.upload.path:${user.home}/secutrain/uploads}")
    private String uploadBasePath;

    @Value("${file.max-size:524288000}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS =
            new HashSet<>(Arrays.asList("mp4", "avi", "mov", "mkv", "webm", "flv", "wmv", "m4v"));

    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS =
            new HashSet<>(Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"));

    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("avi", "video/x-msvideo");
        MIME_TYPES.put("mov", "video/quicktime");
        MIME_TYPES.put("mkv", "video/x-matroska");
        MIME_TYPES.put("webm", "video/webm");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    public void validateVideoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %s bytes", maxFileSize)
            );
        }
        String filename = file.getOriginalFilename();
        if (!isValidVideoFile(filename)) {
            throw new IllegalArgumentException(
                    "Invalid video file type. Allowed types: " + String.join(", ", ALLOWED_VIDEO_EXTENSIONS)
            );
        }
        log.info("Video file validation passed: {}", filename);
    }

    public void validateDocument(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        String filename = file.getOriginalFilename();
        if (!isValidDocumentFile(filename)) {
            throw new IllegalArgumentException("Invalid document file type");
        }
    }

    public boolean isValidVideoFile(String filename) {
        if (filename == null) return false;
        return ALLOWED_VIDEO_EXTENSIONS.contains(getFileExtension(filename).toLowerCase());
    }

    public boolean isValidDocumentFile(String filename) {
        if (filename == null) return false;
        return ALLOWED_DOCUMENT_EXTENSIONS.contains(getFileExtension(filename).toLowerCase());
    }

    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public String getMimeType(String extension) {
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    public String saveVideoFile(MultipartFile file) throws IOException {
        validateVideoFile(file);
        return saveFile(file, "videos");
    }

    public String saveDocumentFile(MultipartFile file) throws IOException {
        validateDocument(file);
        return saveFile(file, "documents");
    }

    public String saveThumbnail(MultipartFile file) throws IOException {
        return saveFile(file, "thumbnails");
    }

    private String saveFile(MultipartFile file, String subdirectory) throws IOException {

        Path uploadDir = Paths.get(uploadBasePath, subdirectory).toAbsolutePath().normalize();

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            log.info("Created upload directory: {}", uploadDir);
        }

        String filename = generateUniqueFilename(file.getOriginalFilename());
        Path filePath = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File saved: {}", filePath);

        return filePath.toString();
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("File deleted: {}", filePath);
        } else {
            log.warn("File not found for deletion: {}", filePath);
        }
    }

    public long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.size(path);
    }

    public String getFileSizeFormatted(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public long getTotalStorageUsed() throws IOException {
        Path uploadDir = Paths.get(uploadBasePath);
        if (!Files.exists(uploadDir)) return 0;
        return Files.walk(uploadDir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try { return Files.size(path); }
                    catch (IOException e) { return 0; }
                })
                .sum();
    }

    public long getDirectorySize(String directory) throws IOException {
        Path dir = Paths.get(uploadBasePath, directory);
        if (!Files.exists(dir)) return 0;
        return Files.walk(dir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try { return Files.size(path); }
                    catch (IOException e) { return 0; }
                })
                .sum();
    }

    public void cleanupOldFiles(long maxAgeMillis) throws IOException {
        Path uploadDir = Paths.get(uploadBasePath);
        if (!Files.exists(uploadDir)) return;
        long now = System.currentTimeMillis();
        Files.walk(uploadDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        long lastModified = Files.getLastModifiedTime(path).toMillis();
                        if (now - lastModified > maxAgeMillis) {
                            Files.delete(path);
                            log.info("Deleted old file: {}", path);
                        }
                    } catch (IOException e) {
                        log.error("Error deleting file: {}", path, e);
                    }
                });
    }
}