package ru.job4j.dreamjob.dto;

import java.util.Arrays;

public class FileDto {
    private final String name;
    private final byte[] content;

    public FileDto(String name, byte[] content) {
        this.name = name;
        this.content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    public String getName() {
        return name;
    }

    public String name() {
        return getName();
    }

    public byte[] getContent() {
        return Arrays.copyOf(content, content.length);
    }

    public byte[] content() {
        return getContent();
    }
}
