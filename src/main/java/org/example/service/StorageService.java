package org.example.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;

public interface StorageService {
    void save(MultipartFile file, String keyPrefix, String filename) throws IOException;

    Resource loadAsResource(String keyPrefix, String filename) throws MalformedURLException;

    void delete(String keyPrefix, String filename) throws IOException;
}