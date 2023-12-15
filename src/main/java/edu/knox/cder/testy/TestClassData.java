package edu.knox.cder.testy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

public class TestClassData
{
	private String className;
	private List<MethodData> methods;
	private byte[] bytecode;
	
	public TestClassData(String className, List<MethodData> methods, byte[] bytecode)
	{
		this.className = className;
		this.methods = methods;
		this.bytecode = bytecode;
	}
	
	public void addMethodData(MethodData method)
	{
		this.methods.add(method);
	}
	
	public Map<String, Object> toMap()
	{
		Map<String, Object> methodMap = new HashMap<>();
		methodMap.put("className", className);
		// base64 encoded the bytecode before we write it to JSON
		methodMap.put("bytecode", Base64.getEncoder().encodeToString(bytecode));
		
		List<Map<String, Object>> methodsList = new LinkedList<>();
        methods.forEach(m -> methodsList.add(m.toMap()));
        
        methodMap.put("methods", methodsList);
		
		
		return methodMap;
	}
	
	public void writeJson(String filePath) throws IOException
	{
		writeJson(new File(filePath));
	}
	
	public void writeJson(File file) throws IOException
	{
		Map<String, Object> jsonMap = toMap();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(new Gson().toJson(jsonMap));
            writer.flush();
            writer.close();
        }
	}
	
	public static TestClassData readJson(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Gson gson = new Gson();
            Map<String, Object> jsonMap = gson.fromJson(reader, Map.class);

            String className = (String) jsonMap.get("className");
            String text = (String)jsonMap.get("bytecode");
            byte[] decodedBytes = Base64.getDecoder().decode(text);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> methodsList = (List<Map<String, Object>>) jsonMap.get("methods");
            List<MethodData> methods = methodsList.stream()
                    .map(m -> MethodData.fromMap(m))
                    .sorted((m1, m2) -> {
                    	return m1.getName().compareTo(m2.getName());
                    })
                    .collect(Collectors.toList());

            return new TestClassData(className, methods, decodedBytes);
        }
    }
	
	public List<MethodData> getMethods() {
		return methods;
	}
	
	public int getMethodCount() {
		return methods.size();
	}

	public String getClassName() {
		return className;
	}

	public void setBytecode(byte[] bytecode) {
		this.bytecode = bytecode;
	}
	public byte[] getBytecode()
	{
		return bytecode;
	}

}
