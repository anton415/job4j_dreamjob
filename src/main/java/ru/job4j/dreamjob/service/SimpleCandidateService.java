package ru.job4j.dreamjob.service;

import net.jcip.annotations.ThreadSafe;

import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.File;
import ru.job4j.dreamjob.repository.CandidateRepository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@ThreadSafe
@Service
public class SimpleCandidateService implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final FileService fileService;

    @Autowired
    public SimpleCandidateService(@Qualifier("sql2oCandidateRepository") CandidateRepository sql2oCandidateRepository,
                                  FileService fileService) {
        this.candidateRepository = sql2oCandidateRepository;
        this.fileService = fileService;
    }

    public SimpleCandidateService(CandidateRepository candidateRepository) {
        this(candidateRepository, null);
    }

    @Override
    public Candidate save(Candidate candidate) {
        return candidateRepository.save(candidate);
    }

    @Override
    public Candidate save(Candidate candidate, FileDto fileDto) {
        saveFile(fileDto).ifPresent(file -> candidate.setFileId(file.getId()));
        return candidateRepository.save(candidate);
    }

    @Override
    public boolean deleteById(int id) {
        var candidateOptional = candidateRepository.findById(id);
        if (candidateOptional.isEmpty()) {
            return false;
        }
        var isDeleted = candidateRepository.deleteById(id);
        if (isDeleted) {
            deleteFile(candidateOptional.get().getFileId());
        }
        return isDeleted;
    }

    @Override
    public boolean update(Candidate candidate) {
        return candidateRepository.update(candidate);
    }

    @Override
    public boolean update(Candidate candidate, FileDto fileDto) {
        var oldCandidateOptional = candidateRepository.findById(candidate.getId());
        if (oldCandidateOptional.isEmpty()) {
            return false;
        }
        var oldCandidate = oldCandidateOptional.get();
        var savedFileOptional = saveFile(fileDto);
        savedFileOptional.ifPresent(file -> candidate.setFileId(file.getId()));
        if (savedFileOptional.isEmpty()) {
            candidate.setFileId(oldCandidate.getFileId());
        }
        var isUpdated = candidateRepository.update(candidate);
        if (isUpdated) {
            savedFileOptional.ifPresent(file -> deleteFile(oldCandidate.getFileId()));
        } else {
            savedFileOptional.ifPresent(file -> deleteFile(file.getId()));
        }
        return isUpdated;
    }

    @Override
    public Optional<Candidate> findById(int id) {
        return candidateRepository.findById(id);
    }

    @Override
    public Collection<Candidate> findAll() {
        return candidateRepository.findAll();
    }

    private Optional<File> saveFile(FileDto fileDto) {
        if (!hasContent(fileDto) || fileService == null) {
            return Optional.empty();
        }
        return Optional.of(fileService.save(fileDto));
    }

    private boolean hasContent(FileDto fileDto) {
        return fileDto != null && fileDto.getContent().length > 0;
    }

    private void deleteFile(int fileId) {
        if (fileId > 0 && fileService != null) {
            fileService.deleteById(fileId);
        }
    }
}
