package edu.knox.cder.testy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCaseData 
{
    private List<String> actualParameters;
    private long added;
    private String result;

    public List<String> getActualParameters() {
		return actualParameters;
	}

	public long getAdded() {
		return added;
	}

	public TestCaseData(List<String> actualParmeters, long added, String result)
	{
		this.actualParameters = actualParmeters;
		this.added = added;
		this.result = result;
	}
    
    public TestCaseData(List<String> actualParameters)
    {
    	this.actualParameters = actualParameters;
    	this.added = System.currentTimeMillis();
    }
    
    public String getResult()
    {
    	return result;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("actualParameters", actualParameters);
        testMap.put("added", added);
        testMap.put("result", result);
        return testMap;
    }
    
    public static TestCaseData fromMap(Map<String, Object> map) {
        @SuppressWarnings("unchecked")
        List<String> actualParameters = (List<String>) map.get("actualParameters");
        Double added = (Double) map.get("added");
        long added2 = added.longValue();
        String result = (String)map.get("result");
        return new TestCaseData(actualParameters, added2, result);
    }
    
    public String toString() 
    {
    	return String.format("(%s)", String.join(", ", actualParameters));
    }
    
    public Object[] getParameterArray(String[] types) 
	{
		
		Object[] result = new Object[actualParameters.size()];
		for (int i=0; i < actualParameters.size(); i++)
		{			
			String p = actualParameters.get(i);
			String type = types[i];
			//System.err.println(p);
			// TODO: validate p against its type

			
			if (type.equals("int[]"))
			{
				result[i] = StaticMethodExtractor.readIntArray(p);
			} 
			else if (type.equals("ArrayList<Integer>"))
			{
				result[i] = StaticMethodExtractor.readIntegerList(p);
			}
			else if (type.equals("int"))
			{
				result[i] = Integer.parseInt(p);
			} 
			else if (type.equals("String"))
			{
				result[i] = StaticMethodExtractor.readString(p);
			} 
			else if (type.equals("String[]"))
			{
				result[i] = StaticMethodExtractor.readStringArray(p);
			} 
			else if (type.equals("ArrayList<String>"))
			{
				result[i] = StaticMethodExtractor.readStringList(p);
			}
			else
			{
				throw new RuntimeException(String.format("Unknown type: %s", type));
			}
		}
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}
}
