package com.huai.properties.controller;

import cn.hutool.core.lang.Dict;
import com.huai.properties.properties.ApplicationProperty;
import com.huai.properties.properties.DeveloperProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PropertyController {
    @Autowired
    private final ApplicationProperty applicationProperty;
    @Autowired
    private final DeveloperProperty developerProperty;

    public PropertyController(ApplicationProperty applicationProperty, DeveloperProperty developerProperty) {
        this.applicationProperty = applicationProperty;
        this.developerProperty = developerProperty;
    }

    @GetMapping("/property")
    public Dict index() {
        return Dict.create().set("applicationProperty", applicationProperty).set("developerProperty", developerProperty);
    }
}
