package com.chinmay.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.chinmay.smart.dao.UserRepository;
import com.chinmay.smart.entities.User;
import com.chinmay.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signUp")
	public String signUp(Model model)
	{
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signUp";
	}
	
	//handler for registering user
	@RequestMapping(value="/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user , BindingResult result1,@RequestParam(value="agreement" , defaultValue="false")boolean agreement, 
			Model model, HttpSession session )
	{
		
		try {
			if(!agreement)
			{
				System.out.println("You have not agreed terms and conditions");
				throw new Exception("You have not agreed terms and conditions");
			}
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR" + result1.toString());
				model.addAttribute("user" , user);
				return "signUp";
				
				
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement " + agreement );
			System.out.println("USER" + user);
			
			User result = this.userRepository.save(user);
			
			
			
			model.addAttribute("user",new User());
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Successfully Registered! ","alert-success"));
			return "signUp";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(),"alert-danger"));
			return "signUp";
		}
		
	}

	
	// we use Model model to pass the data or store
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title" , "Login Page");
		return "login";
	}
}
