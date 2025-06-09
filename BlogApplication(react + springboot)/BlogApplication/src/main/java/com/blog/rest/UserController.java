package com.blog.rest;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blog.entity.User;
import com.blog.exception.UserException;
import com.blog.request.UserSignInRequest;
import com.blog.request.UserSignupRequest;
import com.blog.service.CustomUserDetails;
import com.blog.service.IUserService;
import com.blog.utils.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private IUserService userService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/public/signup")
	ResponseEntity<?> userSignUp(@Valid @RequestBody UserSignupRequest request, BindingResult bindingResult) {

		System.out.println(request);
		
		if (bindingResult.hasErrors()) {
			throw new UserException("Invalid User Input", HttpStatus.BAD_REQUEST);
		}

		User findUser = userService.findByEmail(request.getEmail());

		if (findUser != null) {
			throw new UserException("Email ALready Exists", HttpStatus.CONFLICT);
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setName(request.getName());
		
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		user = userService.create(user);

		Map<String, Object> claims = new HashMap<>();
		claims.put("email", user.getEmail());
		claims.put("role", "user");
		claims.put("userId", user.getId());
		String token = jwtUtil.generateToken(user.getEmail(), claims);
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Account created Successfully");
		response.put("token", token);
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PostMapping("/public/signin")
	ResponseEntity<?> userSignIn(@Valid @RequestBody UserSignInRequest request, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			throw new UserException("Invalid User Input", HttpStatus.BAD_REQUEST);
		}

		User user = userService.findByEmail(request.getEmail());

		if (user == null) {
			throw new UserException("Email Not Found", HttpStatus.NOT_FOUND);
		}
		
		if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
		{
			throw new UserException("Invalid Password", HttpStatus.UNAUTHORIZED);
		}
		
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", user.getEmail());
		claims.put("role", "user");
		claims.put("userId", user.getId());
		String token = jwtUtil.generateToken(user.getEmail(), claims);
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Logged In Successfully");
		response.put("token", token);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/secure/profile")
	ResponseEntity<?> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		user.setPassword("");
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("user", user);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
