package org.example.dto;

import org.springframework.core.io.Resource;

public class FileWithContentType {
    private final Resource resource;
    private final String contentType;

    public FileWithContentType(Resource resource, String contentType) {
        this.resource = resource;
        this.contentType = contentType;
    }

    public Resource getResource() {
        return resource;
    }

    public String getContentType() {
        return contentType;
    }
}
