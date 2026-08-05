package com.leets7th.job_is_be.domain.job.controller;

import com.leets7th.job_is_be.domain.job.entity.Job;
import com.leets7th.job_is_be.domain.job.repository.JobRepository;
import com.leets7th.job_is_be.domain.job.service.EditorNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/editor")
public class EditorNoteTestController {

    private final JobRepository jobRepository;
    private final EditorNoteService editorNoteService;


    @GetMapping("/{jobId}")
    public String test(
            @PathVariable Long jobId
    ) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow();

        return editorNoteService.generateEditorNote(job);
    }
}