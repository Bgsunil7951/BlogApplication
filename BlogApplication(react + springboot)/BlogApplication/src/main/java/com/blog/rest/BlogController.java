package com.blog.rest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blog.dto.BlogDTO;
import com.blog.entity.Blog;
import com.blog.entity.User;
import com.blog.exception.BlogException;
import com.blog.request.CreateBlogRequest;
import com.blog.service.CustomUserDetails;
import com.blog.service.IBlogService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

	@Autowired
	private IBlogService blogService;

	@PostMapping("/secure/create")
	public ResponseEntity<?> createBlog(@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@Valid @RequestBody CreateBlogRequest request, BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			throw new BlogException("Invalid Input data", HttpStatus.BAD_REQUEST);
		}

		User user = customUserDetails.getUser();

		Blog blog = new Blog();
		blog.setAuthor(user);
		blog.setContent(request.getContent());

		
		blog.setHashTags(request.getHashTags());
		blog.setImg(request.getImg());
		blog.setLikes(0l);
		blog.setTitle(request.getTitle());
		blog.setCreatedAt(LocalDateTime.now());

		blog = blogService.create(blog);

		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Blog craeted Successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@PutMapping("/secure/update/{id}")
	public ResponseEntity<?> upadetBlog(@PathVariable Integer id, 
			@Valid @RequestBody CreateBlogRequest request,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			throw new BlogException("Invalid Input data", HttpStatus.BAD_REQUEST);
		}

		Blog blog = blogService.findById(id);

		if (blog == null) {
			throw new BlogException("Blog Not Found", HttpStatus.NOT_FOUND);
		}

		blog.setContent(request.getContent());
		
		blog.setHashTags(request.getHashTags());
		blog.setImg(request.getImg());
		blog.setLikes(blog.getLikes());
		blog.setTitle(request.getTitle());
		
		blog = blogService.update(blog);

		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "Blog updated Successfully");
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@DeleteMapping("/secure/delete/{id}")
	public ResponseEntity<?> deleteBlog(@PathVariable Integer id)
	{

		Blog blog   = blogService.findById(id);
		
		if(blog ==null)
		{
			throw new BlogException("Blog Not Found", HttpStatus.NOT_FOUND);
		}
		
		blogService.deleteById(id);
		
		Map<String, Object> response = new HashMap<>();
		response.put("status","success");
		response.put("message","Blog deleted Successfully");
		return new ResponseEntity<>(response , HttpStatus.OK);
		
	}

	@GetMapping("/public/")
	public ResponseEntity<?> getBlogs(
			@RequestParam(required = false ,defaultValue = "") String q ,
			@RequestParam(required = false ,defaultValue = "1") Integer  page ,
			@RequestParam(required = false ,defaultValue = "10") Integer limit)
	{
		
		
		Pageable pageable =  PageRequest.of(page, limit,Sort.by("id").descending());
		Page<Blog>  blogs = blogService.findAll(q, pageable);
		Page<BlogDTO> blogsResponse = blogs.map(blog->{
			BlogDTO blogDTO =  new BlogDTO();
			User user = blog.getAuthor();
			user.setPassword(null);
			blogDTO.setAuthor(user);
			blogDTO.setContent(blog.getContent());
			blogDTO.setId(blog.getId());
			blogDTO.setLikes(blog.getLikes());
			blogDTO.setTitle(blog.getTitle());
			blogDTO.setImg(blog.getImg());
			blogDTO.setHashTags(blog.getHashTags());
			blogDTO.setCreatedAt(blog.getCreatedAt());
			return blogDTO;
		});
		Map<String, Object> response = new HashMap<>();
		response.put("status","success");
		response.put("blogs",blogsResponse);
		return new ResponseEntity<>(response , HttpStatus.OK);
		
	}
	
	@GetMapping("/public/user/{id}")
	public ResponseEntity<?> getBlogsUserId(
			@PathVariable Integer id ,
			@RequestParam(required = false ,defaultValue = "0") Integer  page ,
			@RequestParam(required = false ,defaultValue = "10") Integer limit)
	{
		
	
		Pageable pageable =  PageRequest.of(page, limit,Sort.by("id").descending());
		Page<Blog>  blogs = blogService.findByUserId(id, pageable);
		Page<BlogDTO> blogsResponse = blogs.map(blog->{
			BlogDTO blogDTO =  new BlogDTO();
			User user = blog.getAuthor();
			user.setPassword(null);
			blogDTO.setAuthor(user);
			blogDTO.setContent(blog.getContent());
			blogDTO.setId(blog.getId());
			blogDTO.setLikes(blog.getLikes());
			blogDTO.setTitle(blog.getTitle());
			blogDTO.setImg(blog.getImg());
			blogDTO.setHashTags(blog.getHashTags());
			return blogDTO;
		});
		
		Map<String, Object> response = new HashMap<>();
		response.put("status","success");
		response.put("blogs",blogsResponse);
		return new ResponseEntity<>(response , HttpStatus.OK);
		
	}
	
	
	
}
