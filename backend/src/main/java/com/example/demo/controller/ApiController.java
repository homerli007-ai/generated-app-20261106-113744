package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./allowed_root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody DeleteRequest req) {
        List<Map<String, String>> results = new ArrayList<>();
        Path root;
        try {
            root = Paths.get(allowedRoot).toRealPath();
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "allowed_root not accessible: " + allowedRoot));
        }
        if (req == null || req.paths == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "paths is required"));
        }
        for (String p : req.paths) {
            Map<String, String> r = new LinkedHashMap<>();
            r.put("path", String.valueOf(p));
            try {
                if (p == null || p.isBlank()) {
                    r.put("status", "refused");
                    r.put("reason", "empty path");
                    results.add(r);
                    continue;
                }
                Path target = root.resolve(p).normalize();
                if (!target.startsWith(root)) {
                    r.put("status", "refused");
                    r.put("reason", "outside allowed_root");
                    results.add(r);
                    continue;
                }
                if (Files.isSymbolicLink(target)) {
                    r.put("status", "refused");
                    r.put("reason", "symlink");
                    results.add(r);
                    continue;
                }
                if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    r.put("status", "refused");
                    r.put("reason", "not found");
                    results.add(r);
                    continue;
                }
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                    r.put("status", "refused");
                    r.put("reason", "not a regular file");
                    results.add(r);
                    continue;
                }
                Files.delete(target);
                r.put("status", "deleted");
            } catch (Exception e) {
                r.put("status", "error");
                r.put("reason", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            results.add(r);
        }
        return ResponseEntity.ok(Map.of("results", results));
    }

    public static class DeleteRequest {
        public List<String> paths;
    }
}
