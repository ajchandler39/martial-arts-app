package com.alijah.martial_arts_app.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alijah.martial_arts_app.models.Technique;
import com.alijah.martial_arts_app.models.TechniqueResponse;
import com.alijah.martial_arts_app.models.User;
import com.alijah.martial_arts_app.repositories.TechniqueRepository;
import com.alijah.martial_arts_app.repositories.UserRepository;

//This class handles the Technique models on the /api/technique end-point, returning JSON.
@RestController
@RequestMapping(path="/api/technique")
public class TechniqueController 
{
	private static final Logger logger = LoggerFactory.getLogger(TechniqueController.class);
	//The below allows me to use the queries defined in the TechniqueRepository interface.
	@Autowired
	private TechniqueRepository techRepo;
	
	//The below allows me to use the queries defined in the UserRepository interface.
	//Users are needed in this controller as-well, as techniques need to be added to users and their favorites.
	@Autowired
	private UserRepository userRepo;
	
	@PostMapping
	ResponseEntity<List<Technique>> addTechnique(
			@RequestParam String creator,
			@RequestParam String name, 
			@RequestParam String type, 
			@RequestParam String description, 
			@RequestParam MultipartFile video)
	{
		logger.info("Technique created.");
		byte[] convertedVid = null;
		try {
			convertedVid = video.getBytes();
		} catch (IOException e) {
			logger.error("Failed to read video bytes for technique creation", e);
		}
		
		User user = userRepo.findById(creator).orElse(null);
		user.getTechs().add(new Technique(creator, name, type, description, convertedVid));
		return ResponseEntity.ok(userRepo.save(user).getTechs());
	}
	
	//The below gets all the techniques created by a user.
	//Java logic is included to format it in a way that's easy for the front-end to display.
	@GetMapping(path="/{username}")
	HashMap<String, List<Technique>> getUserTechniques(@PathVariable(value="username") String user) 
	{
		List<Technique> userTechs = userRepo.findById(user).get().getTechs();
		HashMap<String, List<Technique>> result = new HashMap<String, List<Technique>>();
		for(Technique t : userTechs)
		{
			if(!result.containsKey(t.getType())) result.put(t.getType(), new ArrayList<Technique>());
			result.get(t.getType()).add(t);
		}
		return result;
	}
	
	//The following gets the techniques by the specified user, used when a signed in user is searching for the techniques of others.
	@GetMapping(path="/foreign/{firstName}/{lastName}")
	HashMap<String, List<Technique>> getUserTechniques(@PathVariable String firstName, @PathVariable String lastName)
	{
		List<Technique> userTechs = userRepo.findByFirstNameAndLastName(firstName, lastName).getTechs();
		HashMap<String, List<Technique>> result = new HashMap<String, List<Technique>>();
		for(Technique t : userTechs)
		{
			if(!result.containsKey(t.getType())) result.put(t.getType(), new ArrayList<Technique>());
			result.get(t.getType()).add(t);
		}
		return result;
	}
	
	//The following applies a more complex query and algorithm to find the most highly rated techniques, only returning the top of each kind of technique, i.e. the highest rated armbar.
	@GetMapping(path="/popular")
	HashMap<String, List<Technique>> getTop()
	{
		List<TechniqueResponse> full = userRepo.findPopular();
		Set<String> popular = new HashSet<String>();
		HashMap<String, List<Technique>> result = new HashMap<String, List<Technique>>();
		for(TechniqueResponse t : full) if(popular.add(t.getName()) && t.getN() > 1)
			{
			Technique tech = new Technique(t.getId(), t.getCreator(), t.getName(), t.getType(), t.getDescription(), t.getVideo());
			
			if(!result.containsKey(t.getType())) result.put(t.getType(), new ArrayList<Technique>());
			result.get(t.getType()).add(tech);
			}
		return result;
		
	}
	
	//The following returns the most recent 10 posted techniques to a public page, so users can find and favorite them.
	@GetMapping(path="/latest")
	List<Technique> getLatest() { return techRepo.findLatest(); }
	
	//The following is an end-point that returns a video. This makes linking the html video tag with the file easy.
	@GetMapping(path="/video/{id}", produces = "video/mp4")
	byte[] getVideo(@PathVariable Integer id)
	{
		Technique foundTech = techRepo.findById(id).orElse(null);
		try {
			return foundTech.getVideo();
		} catch (Exception e) {
			logger.error("Failed to retrieve video for technique id: {}", id, e);
		}
		return null;
	}
	
	//This allows a user to update one of their techniques. Only changed fields from the front-end will be updated.
	@PutMapping(path="/{id}")
	ResponseEntity<Technique> putTechnique(
			@PathVariable(value="id") Integer id,
			@RequestParam(required = false) String name, 
			@RequestParam(required = false) String type, 
			@RequestParam(required = false) String description, 
			@RequestParam(required = false) MultipartFile video)
	{
		logger.info("Technique updated.");
		byte[] convertedVid = null;
		//for some reason video.getBytes() will stop the program if the exception of "can't call .getBytes()" is called.
		if (video != null) {
			try {
				convertedVid = video.getBytes();
			} catch (IOException e) {
				logger.warn("Failed to read video bytes for technique update, keeping existing video", e);
			}
		}
		
		Technique found = techRepo.findById(id).orElse(null);
		
		//should test for empty strings on all but video. vid wont return empty string, all others wont return null.
		if(name != null && !name.isBlank()) found.setName(name);
		if(type != null && !type.isBlank()) found.setType(type);
		if(description != null && !description.isBlank()) found.setDescription(description);
		if(video != null && !video.isEmpty()) found.setVideo(convertedVid);
		return ResponseEntity.ok(techRepo.save(found));
	}
	
	//This deletes a users technique, to be used when they want to delete one from their library.
	@DeleteMapping(path="/{id}")
	ResponseEntity<Technique> deleteTechnique(@PathVariable(value="id") Integer id)
	{		
		logger.info("Technique deleted.");
		Technique deleted = techRepo.findById(id).orElse(null);
		techRepo.deleteById(id);
		return ResponseEntity.ok(deleted);
	}
}


/*
@GetMapping(path="/all")
List<Technique> getAllTechniques() { return techRepo.findAll(); }
*/
/*
@GetMapping(path="/name/{name}")
List<Technique> getTechniqueByName(@PathVariable(value="name") String name) { return techRepo.findByName(name); }
*/
/*
@GetMapping(path="/type/{type}")
List<Technique> getTechniqueByType(@PathVariable(value="type") String type)
{ return techRepo.findByType(type); }
*/
/*
@GetMapping(path="/types")
List<String> getTechniqueTypes() { return techRepo.findDistinctTypes(); }
*/