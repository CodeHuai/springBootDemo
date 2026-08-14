package com.huai.properties.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class ApplicationProperty {
    //    注意：这里不能选择lombok中的value，而要选择bean中的value注解
    @Value("${application.name}")
    private String name;
    @Value("${application.version}")
    private String version;
}
