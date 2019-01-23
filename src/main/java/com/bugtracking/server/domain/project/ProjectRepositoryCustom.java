package com.bugtracking.server.domain.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {

    Page<Project> findAllByFilters(Pageable pageable);
}
