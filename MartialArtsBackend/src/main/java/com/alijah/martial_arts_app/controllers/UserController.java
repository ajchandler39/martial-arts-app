package com.alijah.martial_arts_app.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alijah.martial_arts_app.models.Technique;
import com.alijah.martial_arts_app.models.User;
import com.alijah.martial_arts_app.repositories.TechniqueRepository;
import com.alijah.martial_arts_app.repositories.UserRepository;

//This class handles the User models on the /api/user end-point, returning JSON.
@RestController
@RequestMapping(path="/api/user")
public class UserController {
	
	Logger logger = LoggerFactory.getLogger(UserController.class);

	//The below allows me to use the queries defined in the UserRepository interface.
	@Autowired
	private UserRepository userRepo;
	
	//The below allows me to use the queries defined in the TechniqueRepository interface.
	//Techniques are needed in this controller as-well, as users have many techniques and many favorites.
	@Autowired
	private TechniqueRepository techRepo;

	@PostMapping
	public ResponseEntity<User> addUser(@RequestBody User user)
	{
		logger.info("User created.");
		PasswordEncoder passEncoder = new BCryptPasswordEncoder();
		String encodedPass = passEncoder.encode(user.getPassword());
		user.setPassword(encodedPass);
		System.out.println(encodedPass);
		return ResponseEntity.ok(userRepo.save(user));
	}
	
	//The below adds a technique to a users favorite. This can be done when searching for a foreign user, adding a top technique from the forum page, or adding a new technique from the forum page.
	@PostMapping(path="/fav/{username}/{id}")
	public ResponseEntity<User> addFavorite(@PathVariable String username, @PathVariable Integer id)
	{
		logger.info("User technique created.");
		Technique foundTech = techRepo.findById(id).orElse(null);
		User foundUser = userRepo.findById(username).orElse(null);
		foundUser.getFavorites().add(foundTech);
		return ResponseEntity.ok(userRepo.save(foundUser));
	}
	
	//This gets a users favorites, to be displayed in their library.
	@GetMapping(path="/fav/{username}")
	public HashMap<String, List<Technique>> getUserFavorites(@PathVariable(value="username") String user) {
		
		List<Technique> userTechs = userRepo.findById(user).get().getFavorites();
		HashMap<String, List<Technique>> result = new HashMap<String, List<Technique>>();
		for(Technique t : userTechs)
		{
			if(!result.containsKey(t.getType())) result.put(t.getType(), new ArrayList<Technique>());
			result.get(t.getType()).add(t);
		}
		return result;
	}
	
	//Authenticates a user. Credentials are sent in the POST body (not the URL) so the password
	//never lands in request logs, browser history, or proxy caches. BCrypt verifies the raw
	//password against the stored hash.
	@PostMapping(path="/login")
	public ResponseEntity<User> login(@RequestBody User credentials)
	{
		PasswordEncoder passEncoder = new BCryptPasswordEncoder();
		User user = userRepo.findById(credentials.getUsername()).orElse(null);
		if(user != null && passEncoder.matches(credentials.getPassword(), user.getPassword()))
		{
			return ResponseEntity.ok(user);
		}
		return ResponseEntity.status(401).build();
	}

	//The below allows a user to delete one of their technique favorites, from their library page.
	@DeleteMapping(path="/fav/{username}/{id}")
	public ResponseEntity<User> deleteFavorite(@PathVariable String username, @PathVariable Integer id)
	{
		logger.info("User favorite deleted.");
		User target = userRepo.findById(username).get();
		List<Technique> favs = target.getFavorites();
		for(int i = 0; i < favs.size(); i++) if(favs.get(i).getId().equals(id)) favs.remove(i);
		return ResponseEntity.ok(userRepo.save(target));
	}
}