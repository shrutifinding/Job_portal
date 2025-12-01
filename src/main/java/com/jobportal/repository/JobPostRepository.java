package com.jobportal.repository;

import com.jobportal.model.JobPost;
import java.util.List;

public interface JobPostRepository {

    int create(JobPost jobPost);

    JobPost findById(int id);

    List<JobPost> findAll();

    int update(JobPost jobPost);

    int softDelete(int id);

    List<JobPost> findAllActive();
    
    List<JobPost> findByEmployerId(int employerId);

}
