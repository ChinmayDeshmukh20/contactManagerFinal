package com.chinmay.smart.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.chinmay.smart.dao.ContactRepository;
import com.chinmay.smart.dao.UserRepository;
import com.chinmay.smart.entities.Contact;
import com.chinmay.smart.entities.User;

@RestController
public class SearchController {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;

	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query  ,Principal principal)
	{
		User user = this.userRepository.getUserByUserName(principal.getName());
		//principal.getName() will get name of logged in user
		
		List<Contact>contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		
		return ResponseEntity.ok(contacts);
	}
	
	
	
	
}
