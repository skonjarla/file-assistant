package net.konjarla.aifilemonitor.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Slf4j
@Component
public class FileUtils {

    /**
     * Updates file metadata in the provided metadata map
     *
     * @param file     the file to get metadata from
     * @param metadata map to store the metadata
     */
    public void populateFileMetadata(File file, Map<String, Object> metadata) {
        if (file == null || metadata == null) return;

        metadata.put("fileName", file.getName());
        metadata.put("fileSize", file.length());
        metadata.put("lastModified", LocalDateTime.ofInstant(
                Instant.ofEpochMilli(file.lastModified()),
                ZoneId.systemDefault()
        ));
        try {
            BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            metadata.put("creationTime", LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(attributes.creationTime().toMillis()),
                    ZoneId.systemDefault()
            ));
        } catch (IOException e) {
            metadata.put("creationTime", null);
            log.error("Could not retrieve file attributes for {}", file.getAbsolutePath(), e);
        }

        metadata.put("isHidden", file.isHidden());
        metadata.put("isReadOnly", !file.canWrite());

        // Set file extension
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');
        metadata.put("fileExtension", lastDotIndex > 0 ?
                fileName.substring(lastDotIndex + 1).toLowerCase() : "");

        // Calculate and add checksum
        metadata.put("checksum", calculateFileChecksum(file));

        // Add POSIX attributes if available
        try {
            Path path = file.toPath();
            metadata.put("owner", Files.getOwner(path).getName());

            if (Files.getFileStore(path).supportsFileAttributeView("posix")) {
                metadata.put("groupName", Files.readAttributes(
                        path, PosixFileAttributes.class).group().getName());
                metadata.put("permissions", PosixFilePermissions.toString(
                        Files.getPosixFilePermissions(path)));
            }
        } catch (Exception e) {
            log.warn("Could not retrieve file attributes for {}", file.getAbsolutePath(), e);
        }
    }

    /**
     * Calculates SHA-256 checksum of a file
     *
     * @param file the file to calculate checksum for
     * @return SHA-256 checksum as a hex string, or null if an error occurs
     */
    public String calculateFileChecksum(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            return null;
        }

        try (InputStream fis = Files.newInputStream(file.toPath())) {
            byte[] byteArray = new byte[8192];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            byte[] bytes = digest.digest();
            BigInteger number = new BigInteger(1, bytes);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (IOException e) {
            log.error("Error calculating checksum for file: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    /**
     * Converts file size in bytes to human-readable format
     *
     * @param size file size in bytes
     * @return human-readable string representation
     */
    public String toHumanReadableSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", size / Math.pow(1024, exp), pre);
    }

    public static Instant convertDateToInstant(String dateStr) throws DateTimeParseException {
        LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        //return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
}
