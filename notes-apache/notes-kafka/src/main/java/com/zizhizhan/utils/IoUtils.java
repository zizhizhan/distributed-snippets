package com.zizhizhan.utils;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

@Slf4j
public class IoUtils {

    public static File saveContentAsTempFile(String content, String prefix, String suffix) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(prefix, suffix);
            Files.asCharSink(tempFile, StandardCharsets.UTF_8).write(content);
            return tempFile;
        } catch (IOException e) {
            log.warn("Can't create temp file {}.", tempFile, e);
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                tempFile.deleteOnExit();
            }
        }
    }

    public static File copyResourceAsTempFile(Class<?> relatedClass, String resourceName, String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            try (InputStream in = relatedClass.getResourceAsStream(resourceName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Resource is {}.", relatedClass.getResource(resourceName));
                }
                try (OutputStream out = new FileOutputStream(tempFile)) {
                    if (in != null) {
                        ByteStreams.copy(in, out);
                        return tempFile;
                    } else {
                        throw new IllegalArgumentException(resourceName + " is not exist!");
                    }
                }
            } finally {
                tempFile.deleteOnExit();
            }
        } catch (IOException e) {
            log.error("Unexpected io error where process {}.", resourceName, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static File copyResourceAsTempFile(String resourcePath, String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
                if (log.isInfoEnabled()) {
                    log.info("Resource is {}.", Thread.currentThread().getContextClassLoader().getResource(resourcePath));
                }
                try (OutputStream out = new FileOutputStream(tempFile)) {
                    if (in != null) {
                        ByteStreams.copy(in, out);
                        return tempFile;
                    } else {
                        throw new IllegalArgumentException(resourcePath + " is not exist!");
                    }
                }
            } finally {
                tempFile.deleteOnExit();
            }
        } catch (IOException e) {
            log.error("Unexpected io error where process {}.", resourcePath, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static File saveContentToPWD(String content, String targetName) {
        String pwd = System.getProperty("user.dir");
        try {
            File targetFile = Paths.get(pwd, targetName).toFile();
            Files.asCharSink(targetFile, StandardCharsets.UTF_8).write(content);
            return targetFile;
        } catch (IOException e) {
            log.warn("Can't create file {} with content {}.", targetName, content, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String copyResourceToPWD(String resourcePath, String targetName) {
        String pwd = System.getProperty("user.dir");
        try {
            try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
                if (log.isInfoEnabled()) {
                    log.info("Resource is {}.", Thread.currentThread().getContextClassLoader().getResource(resourcePath));
                }
                String targetPath = Paths.get(pwd, targetName).toString();
                try (OutputStream out = new FileOutputStream(targetPath)) {
                    if (in != null) {
                        ByteStreams.copy(in, out);
                        return targetPath;
                    } else {
                        throw new IllegalArgumentException(resourcePath + " is not exist!");
                    }
                }
            }
        } catch (IOException e) {
            log.error("Unexpected io error where process {}.", resourcePath, e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
