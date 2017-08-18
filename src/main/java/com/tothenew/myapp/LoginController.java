package com.tothenew.myapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.tothenew.myapp.beans.UserModel;

@PropertySource("classpath:configuration.properties")
@Controller 
public class LoginController 
{

	@Autowired
	private Environment env;

	@RequestMapping(value = "/login")
	public ModelAndView demoLogin() {
		ModelAndView loginViewModel = new ModelAndView("login");
		loginViewModel.addObject("errorMessage", "");
	    return loginViewModel;
	}
	
	@RequestMapping(value = "/auth", method = RequestMethod.POST)
	public ModelAndView authenticateUser(@ModelAttribute("SpringWeb")UserModel model) {
		if(model.getUsername().equals(env.getProperty("username")) && model.getPassword().equals(env.getProperty("password"))) {
			return  new ModelAndView("redirect:/excel");
		}
		else {
			ModelAndView loginViewModel = new ModelAndView("redirect:/login");
			loginViewModel.addObject("errorMessage", "Invalid credentials");
		    return loginViewModel;
		}
	}
}
