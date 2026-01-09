package org.thebois.utils;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileHandler {
    private String filePath;
    
    public FileHandler(String filePath){
        this.filePath = filePath;
    }

    public List<Map<String, Object>> read() throws Exception {
        File file = new File(filePath);
        
        // If file doesn't exist, return empty list
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        // Read JSON from file
        try (Jsonb jsonb = JsonbBuilder.create();
             FileReader reader = new FileReader(file)) {
            
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            return jsonb.fromJson(content, new ArrayList<Map<String, Object>>() {}.getClass().getGenericSuperclass());
        }
    }

    public void write(List<Map<String, Object>> data) throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create();
             FileWriter writer = new FileWriter(filePath)) {
            
            String json = jsonb.toJson(data);
            writer.write(json);
        }
    }
}
