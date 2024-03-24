package edu.knox.cder.testy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MethodData {
    private String name;
    private String header;
    private List<ParameterData> parameters;
    private String returnType;
    private List<TestCaseData> tests;
    private String answer;

    public MethodData(String name, String header, String returnType, List<ParameterData> parameters)
    {
    	this(name, header, returnType, parameters, new ArrayList<TestCaseData>(), null);
    }
    
    public MethodData(String name, String header, String returnType, List<ParameterData> parameters, List<TestCaseData> tests, String answer)
    {
        this.name = name;
        this.header = header;
        this.parameters = parameters;
        this.returnType = returnType;
        this.tests = tests;
        this.answer = answer;
    }
    
    public void addTest(TestCaseData test)
    {
    	this.tests.add(test);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> methodMap = new HashMap<>();
        methodMap.put("name", name);
        methodMap.put("header", header);
        methodMap.put("return", returnType);
        
        // convert the parameters into json
        List<Map<String, Object>> parameterList = new ArrayList<>();
        parameters.forEach(p -> parameterList.add(p.toMap()));
        methodMap.put("parameters", parameterList);
       
        // convert the test cases into json
        List<Map<String, Object>> testList = new ArrayList<>();
        tests.forEach(t -> testList.add(t.toMap()));
        methodMap.put("tests", testList);

        // add the answer
        methodMap.put("answer", answer);

        return methodMap;
    }
    
    public static MethodData fromMap(Map<String, Object> map) {
        String name = (String) map.get("name");
        String header = (String) map.get("header");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parameterList = (List<Map<String, Object>>) map.get("parameters");
        List<ParameterData> parameters = parameterList.stream()
                .map(p -> ParameterData.fromMap(p))
                .collect(Collectors.toList());

        String returnType = (String) map.get("return");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> testList = (List<Map<String, Object>>) map.get("tests");
        List<TestCaseData> tests = testList.stream()
                .map(t -> TestCaseData.fromMap(t))
                .collect(Collectors.toList());

        String answer = (String) map.get("answer");

        return new MethodData(name, header, returnType, parameters, tests, answer);
    }

	public List<TestCaseData> getTests() {
		return tests;
	}

	public String getName() {
		return name;
	}

	public String getHeader() {
		return header;
	}

	public List<ParameterData> getParameters() {
		return parameters;
	}
	
	public int getParameterCount() {
		return parameters.size();
	}
	
	public String[] getParameterTypes()
	{
		return parameters.stream().map(p -> p.getType()).toArray(String[]::new);
	}

	public String getReturnType() {
		return returnType;
	}

	public int getTestCount() {
		return tests.size();
	}

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }
    
}


