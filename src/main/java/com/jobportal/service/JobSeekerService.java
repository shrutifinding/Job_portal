package com.jobportal.service;

import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.JobSeeker;
import com.jobportal.model.User;
import com.jobportal.model.enums.UserType;
import com.jobportal.repository.JobSeekerRepository;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class JobSeekerService {

	private final JobSeekerRepository repository;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private FileStorageService fileStorageService;

	public JobSeekerService(JobSeekerRepository repository) {
		this.repository = repository;
	}

	public JobSeeker create(JobSeeker jobSeeker) {
		User user = userService.findById(jobSeeker.getUserId());
		if(!UserType.JOB_SEEKER.equals(user.getUserType())) {
			throw new BadRequestException("Required role is missing");
		}
		return repository.create(jobSeeker);
	}

	public JobSeeker findById(int id) {
		return repository.findById(id);
	}
	
	public JobSeeker IsUserId(int id) {
		return repository.IsUserId(id);
	}

	public List<JobSeeker> findAll() {
		return repository.findAll();
	}

	public JobSeeker update(JobSeeker jobSeeker) {
		JobSeeker saved = repository.update(jobSeeker);
		userService.activateUser(saved.getUserId());
		return saved;
		
	}

	public int softDelete(int id) {
		return repository.softDelete(id);
	}
	
	 public JobSeeker updateResume(int id, MultipartFile file) {
	        if (file == null || file.isEmpty()) {
	            throw new BadRequestException("Profile image is empty");
	        }

	        // ensure user exists
	        JobSeeker seeker = repository.findById(id);
	               if(seeker==null) { throw new ResourceNotFoundException("Jobseeker not found with id: " + id);}

	        // prefix e.g. "user_12"
	        String prefix = "resume" + id;

	        // save inside uploads/users/...
	        String relativePath = fileStorageService.store(file, "resumes", prefix);

	        // update DB
	        repository.updateResume(id, relativePath);

	        // fetch updated user and return
	        JobSeeker updated= repository.findById(id);
	        		 if(seeker==null) { throw new ResourceNotFoundException("Jobseeker not found with id: " + id);}
	        return updated;
	    }

}
