package de.hepaza.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    // SpaController leitet /addNewRestaurant, /restaurantDetails und
    //editRestaurant/{id} per forward auf index.html weiter
    //verhindert 404 nach dem Login (Weiterleitung auf /addNewRestaurant)
    @GetMapping({"/addNewRestaurant", "/restaurantDetails", "/editRestaurant/{id}"})
    public String forwardToFrontend() {
        return "forward:/index.html";
    }
}