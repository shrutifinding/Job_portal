package com.jobportal.service;

import com.jobportal.model.Admin;
import com.jobportal.model.CompanyProfile;
import com.jobportal.model.Employer;
import com.jobportal.model.enums.ApprovalStatus;
import com.jobportal.model.enums.UserStatus;
import com.jobportal.repository.AdminRepository;
import com.jobportal.repository.EmployerRepository;
import com.jobportal.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository repository;
    private final UserRepository userRepository ;
    private final EmployerRepository employerRepository ;

	public AdminService(AdminRepository repository, UserRepository userRepository,
			EmployerRepository employerRepository) {
		super();
		this.repository = repository;
		this.userRepository = userRepository;
		this.employerRepository = employerRepository;
	}

	public Admin create(Admin admin) {
        return repository.save(admin);
    }

    public Admin findById(int adminId) {
        return repository.findById(adminId);
    }

    public List<Admin> findAll() {
        return repository.findAll();
    }

    public List<Employer> listPendingEmployers() {
        return repository.findAllPendingEmployers();
    }

    public void approveEmployer(int employerId) {
        repository.updateEmployerStatus(employerId, ApprovalStatus.APPROVED);
    }

    public void rejectEmployer(int employerId) {
        repository.updateEmployerStatus(employerId, ApprovalStatus.REJECTED);
    }
    
    public CompanyProfile viewCompanyProfile(int employerId) {
        return repository.findCompanyProfileByEmployer(employerId);
    }
    
    

	public Employer approveOrRejectEmployer(int id, ApprovalStatus status) {
		// 1. Load employer to know which user it belongs to
	    Employer employer = employerRepository.findById(id); // method name can be findById / findEmployerById
	    if (employer == null) {
	        throw new IllegalArgumentException("Employer not found with id: " + id);
	    }

	    // 2. Update employer approval_status
	    int updated = repository.updateEmployerStatus(id, status);
	    if (updated == 0) {
	        throw new IllegalStateException("Failed to update employer status for id: " + id);
	    }

	    // 3. Update user.status based on approval
	      
	    UserStatus newUserStatus =
	            (status == ApprovalStatus.APPROVED)
	                    ? UserStatus.ACTIVE
	                    : UserStatus.PENDING; // or INACTIVE / SUSPENDED as you prefer

	    userRepository.updateStatus(employer.getUserId(), newUserStatus);
	    employer.setApprovalStatus(status);
	    return employer;
	}
}
