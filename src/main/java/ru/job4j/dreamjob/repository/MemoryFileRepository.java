package ru.job4j.dreamjob.repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.springframework.stereotype.Repository;

import ru.job4j.dreamjob.model.File;

@ThreadSafe
@Repository
public class MemoryFileRepository implements FileRepository {
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final ConcurrentMap<Integer, File> files = new ConcurrentHashMap<>();

    @Override
    public File save(File file) {
        file.setId(nextId.getAndIncrement());
        var savedFile = copy(file);
        files.put(savedFile.getId(), savedFile);
        return copy(savedFile);
    }

    @Override
    public Optional<File> findById(int id) {
        return Optional.ofNullable(files.get(id)).map(this::copy);
    }

    @Override
    public void deleteById(int id) {
        files.remove(id);
    }

    private File copy(File file) {
        return new File(file.getId(), file.getName(), file.getPath());
    }
}
