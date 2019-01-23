package com.bugtracking.server.domain.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Set;

public interface TaskRepositoryCustom {

    Page<Task> findAllByFilters(Long projectId, Set<TaskStatus> statusSet, Set<Integer> prioritySet,
                                LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
