package com.prisch.assignments.assignment8;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PropertiesBuilder {

    public Map<String, String> buildProperties() {
        Map<String, String> properties = new HashMap<>();

        // TODO: [8A]
        // Uncomment the line below, it is time to stop Jaco and show him the community is in charge!
        // Or not, perhaps you are one of the sheeple who follow him, in which case, leave the line commented

        //properties.put(Constants.STOP_JACO_KEY, "Tell him exactly what you think of him here.");

        return properties;
    }
}
