package com.chinmay.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.chinmay.smart.dao.ContactRepository;
import com.chinmay.smart.dao.UserRepository;
import com.chinmay.smart.entities.Contact;
import com.chinmay.smart.entities.User;
import com.chinmay.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model , Principal principal)
	{
		String userName = principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER " + user);
		
		
		model.addAttribute("user",user);
	}
	
	//home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model , Principal principal)
	{
		model.addAttribute("title" , "User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	//open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title" , "Add Contact");
		model.addAttribute("contact" , new Contact());  // we will get contact object in view i.e html page
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact ,@RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session)
	{
		//Principal we are using it to fetch the username
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading the image file
		if(file.isEmpty())
		{
			// if the file is empty , can give out message
			System.out.println("File is empty");
			contact.setImage("contact.png");
		}
		else
		{
			// upload the file to the folder and change the file name in the contact object
			
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+ File.separator + file.getOriginalFilename());
			Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
			
			
			
			
		}
		
		
		
		
		
		contact.setUser(user);    // giving contact to user
		user.getContacts().add(contact);  // setting contact for particular user accessing the list of contact
		
		this.userRepository.save(user);
		
		System.out.println(contact);
		System.out.println("Added to database");
		
		session.setAttribute("message", new Message("Your contact is added!" , "success"));
		}catch(Exception e)
		{
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong! Try again" , "danger"));
		}
		return "normal/add_contact_form"; 
	}
	
	//show contact handler
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m , Principal principal)
	{
		m.addAttribute("title" , "Show User Contacts");
		
//		String username  = principal.getName();
//		User user = this.userRepository.getUserByUserName(username);
//		List<Contact> contacts = user.getContacts();
//		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		PageRequest pageable = PageRequest.of(page, 4);   // current page = page , contact per page = 5
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable );
		
		m.addAttribute("contacts" , contacts);
		m.addAttribute("currentPage" , page);
		
		m.addAttribute("totalPages" , contacts.getTotalPages());
		
		return "normal/show_contacts";

	}
	
	
	//handler for showing specific contact details
	
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId , Model model , Principal principal)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact  = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		
		
		return "normal/contact_detail";
	}
	
	
	
	
	//deleting contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId , Model model,  HttpSession session , Principal principal)
	{
		Contact contact = this.contactRepository.findById(cId).get();
		
		
//		contact.setUser(null);
//		this.contactRepository.delete(contact);
//	
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		
		this.userRepository.save(user);
		
		
		session.setAttribute("message", new Message("contact deleted successfully" , "success"));
		return "redirect:/user/show_contacts/0";
		
		
		}
	
	
	
	
	//open update form handler
	
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid ,Model m)
	{
		
		m.addAttribute("title" , "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	
	
	// update contact handler
	
	@RequestMapping(value="/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact , @RequestParam("profileImage") MultipartFile file , 
			Model m , HttpSession session , Principal principal)
	{
		try {
			
			//old contact detail
			
			Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile , oldcontactDetail.getImage());
				file1.delete();
				//update new photo
				
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+ File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}
			
			else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated" , "success"));
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		
		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	
	
	//your profile
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title" , "Profile Page");
		return "normal/profile";
	}
	
	
	
	
	
	
}
