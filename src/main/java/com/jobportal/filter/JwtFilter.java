package com.jobportal.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.jobportal.service.JwtService;

import java.io.IOException;

@Component
@Order(1)
public class JwtFilter implements Filter {
	
	private final JwtService jwtService;

	public JwtFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}


    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();
        String method = request.getMethod();

        // Don't filter login (or any other public endpoints)
//        if (path.startsWith("/api/users") ) {
//            filterChain.doFilter(request, response);
//            return;
//        }

     // 1) Allow preflight CORS requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 2) Public endpoints (NO token required)
        if (("POST".equalsIgnoreCase(method) && "/api/users".equals(path)) || // register user/admin/jobseeker
            ("POST".equalsIgnoreCase(method) && "/api/users/login".equals(path)) // login
            ) {
        	filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7); 

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);
        request.setAttribute("userRole", role);

        // If you want to use the email inside controller:
        request.setAttribute("userEmail", email);

        filterChain.doFilter(request, response);
    }
}
