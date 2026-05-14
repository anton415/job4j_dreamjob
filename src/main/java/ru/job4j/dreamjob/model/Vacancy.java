package ru.job4j.dreamjob.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Vacancy {

    private int id;

    private String title;

    private String description;

    private LocalDateTime creationDate = LocalDateTime.now();

    private boolean visible;

    private int cityId;

    private int fileId;

    public Vacancy() {
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate) {
        this(id, title, description, creationDate, false, 0, 0);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate, int cityId) {
        this(id, title, description, creationDate, false, cityId, 0);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate,
                   int cityId, int fileId) {
        this(id, title, description, creationDate, false, cityId, fileId);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate, boolean visible) {
        this(id, title, description, creationDate, visible, 0, 0);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate,
                   int cityId, boolean visible) {
        this(id, title, description, creationDate, visible, cityId, 0);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate,
                   boolean visible, int cityId) {
        this(id, title, description, creationDate, visible, cityId, 0);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate,
                   int cityId, boolean visible, int fileId) {
        this(id, title, description, creationDate, visible, cityId, fileId);
    }

    public Vacancy(int id, String title, String description, LocalDateTime creationDate,
                   boolean visible, int cityId, int fileId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creationDate = creationDate;
        this.visible = visible;
        this.cityId = cityId;
        this.fileId = fileId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vacancy vacancy = (Vacancy) o;
        return id == vacancy.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
