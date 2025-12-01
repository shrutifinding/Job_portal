 package com.jobportal.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.jobportal.dto.LoginResponse;
import com.jobportal.dto.UserDTO;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.model.Employer;
import com.jobportal.model.JobSeeker;
import com.jobportal.model.User;
import com.jobportal.model.enums.UserStatus;
import com.jobportal.repository.EmployerRepository;
import com.jobportal.repository.JobSeekerRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.utils.PasswordUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import com.jobportal.model.enums.ApprovalStatus;
import com.jobportal.model.enums.UserType;
@Service
public class UserService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;
    private final EmployerRepository employerRepository;
    private final JobSeekerRepository jobSeekerRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private FileStorageService fileStorageService;
  
    private PasswordUtil passwordUtils;
   
    
    		public UserService(UserRepository repository, EmployerRepository employerRepository, JobSeekerRepository jobSeekerRepository) {
    	        this.repository = repository;
    	        this.employerRepository = employerRepository;
    	        this.jobSeekerRepository =jobSeekerRepository;
    	    }

    	    public User create(User user) {
    	    	if(repository.emailExists(user.getEmail())) {
    	    		throw new BadRequestException("User with this email already exists, please login");
    	    	}
    	    	user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
    	        //User createdUser = repository.create(user);    
    	        //return findUserByIdOrThrow(createdUser.getUserId());
    	     // 1. set initial user status
    	        // employers => PENDING, others (ADMIN, JOB_SEEKER, etc.) => ACTIVE
    	    	
    	       if(user.getStatus() == null) {
    	    	   
    	    	  if (user.getUserType() == UserType.EMPLOYER) {
    	            user.setStatus(UserStatus.PENDING);
    	        } else {
    	            user.setStatus(UserStatus.ACTIVE);
    	        }
    	       }

    	        // 2. save user
    	        User saved = repository.create(user);

    	        // 3. if EMPLOYER -> create row in employer table with PENDING approval_status
    	        if (saved.getUserType() == UserType.EMPLOYER) {
    	            Employer emp = new Employer();
    	            emp.setUserId(saved.getUserId());
    	            emp.setApprovalStatus(ApprovalStatus.PENDING);
    	            employerRepository.create(emp);
    	        }

    	        // 4. if JOB_SEEKER -> create row in job_seeker table (if you have this table)
    	        if (saved.getUserType() == UserType.JOB_SEEKER) {
    	            JobSeeker js = new JobSeeker();
    	            js.setUserId(saved.getUserId());
    	            jobSeekerRepository.create(js);
    	        }

    	        return saved;
    	        
    	    }

    	    public User update(User user) {
    	        repository.update(user);
    	        return findUserByIdOrThrow(user.getUserId());
    	    }

    	    public User findById(int id) {
    	        return findUserByIdOrThrow(id);
    	    }

    	    public List<User> findAll() {
    	        return repository.findAll();
    	    }

    	    public void delete(int id) {
    	        repository.delete(id);
    	    }
    	    private User findUserByIdOrThrow(int id) {
    	        return repository.findById(id)
    	                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    	    }
    	    
    	    /**
    	     * Stores the profile image and updates the user's profile_image column.
    	     * Returns updated User (fetched from DB).
    	     */
    	    public User updateProfileImage(int userId, MultipartFile file) {
    	        if (file == null || file.isEmpty()) {
    	            throw new BadRequestException("Profile image is empty");
    	        }

    	        // ensure user exists
    	        User user = repository.findById(userId)
    	                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    	        // prefix e.g. "user_12"
    	        String prefix = "user_" + userId;

    	        // save inside uploads/users/...
    	        String relativePath = fileStorageService.store(file, "users", prefix);

    	        // update DB
    	        repository.updateProfileImage(userId, relativePath);

    	        // fetch updated user and return
    	        return repository.findById(userId)
    	                .orElseThrow(() -> new ResourceNotFoundException("User not found after update: " + userId));
    	    }
    	    public void activateUser(int userId) {
    	    	repository.updateStatus(userId, UserStatus.ACTIVE);
    	    }
    
    public LoginResponse login(String email, String password) {
        User user;
       
        try {
            user = repository.findByEmail(email);
            if(user.getStatus() == UserStatus.PENDING) {
            	user.setStatus(UserStatus.ACTIVE);
            }
            repository.update(user);
          
        } catch (Exception e) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!PasswordUtil.checkPassword(password,user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new BadRequestException("User is not active");
        }
        
        String token = jwtService.generateToken(user.getEmail(), user.getUserType().name());
        UserDTO userDTO = objectMapper.convertValue(user, UserDTO.class);
        return new LoginResponse(token, userDTO);
    }
}
