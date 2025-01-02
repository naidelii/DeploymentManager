package com.naidelii.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author lanwei
 */
@Slf4j
public final class ScriptUtils {
    private ScriptUtils() {
    }

    public static Process buildProcess(String scriptPath, String jarName, String jarSavePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", scriptPath, jarName, jarSavePath);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    public static String readScriptOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString();
        }
    }
}
