package ru.bvn13.jircbot.web.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.bvn13.jircbot.documentation.DocumentationProvider;

/**
 * Created by bvn13 on 28.10.2018.
 */
@Controller
@RequestMapping("/docs")
public class DocumentationController {

    @Autowired
    private DocumentationProvider documentationProvider;

    @GetMapping
    public String get(Model model) {

        model.addAttribute("modules", documentationProvider.getModuleNames());
        model.addAttribute("descriptions", documentationProvider.getDescriptors());

        return "documentation";
    }

}
