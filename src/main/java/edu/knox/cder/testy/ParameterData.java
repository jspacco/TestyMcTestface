package edu.knox.cder.testy;

import java.util.HashMap;
import java.util.Map;

public class ParameterData {
    private String type;
    private String name;

    public ParameterData(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("type", type);
        paramMap.put("name", name);
        return paramMap;
    }
    
    public static ParameterData fromMap(Map<String, Object> map) {
        String type = (String) map.get("type");
        String name = (String) map.get("name");
        return new ParameterData(type, name);
    }
    
    public String toString() 
    {
    	return String.format("%s %s", type, name);
    }
    
    public String getType()
    {
    	return type;
    }
}

