package edu.knox.cder.testy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;

import com.google.gson.Gson;

public class StaticMethodExtractor
{
	
	private static byte[] readBytes(String classFilePath) throws IOException
	{
		File file = new File(classFilePath);
        FileInputStream fileInputStream = new FileInputStream(file);

        // Read the class file bytes
        int len = (int)file.length();
        byte[] classFileBytes = new byte[len];
        int read = 0;
        while (read < len)
        {
        	read += fileInputStream.read(classFileBytes, read, len - read);
        }
        fileInputStream.close();
        return classFileBytes;
	}
	
	public static TestClassData readFromClassFile(String classFilePath) throws IOException, ClassNotFoundException 
	{
		// FIXME inefficient, we have to keep reading the bytes
		byte[] classFileBytes = readBytes(classFilePath);
		String className = getClassName(classFileBytes);
		
		// convert java.lang.reflection methods into my simplified JSON format 
		List<Method> methods = getStaticMethods(className, classFileBytes);
		
		
		List<MethodData> methodDataList = new ArrayList<>();
		for (Method m : methods)
		{
			String methodName = getMethodName(m);
			String returnType = m.getReturnType().getSimpleName();
			String header = toHeaderString(m);
			
			List<ParameterData> params = new ArrayList<>();
			for (int j=0; j < m.getParameterCount(); j++)
			{
				Parameter p = m.getParameters()[j];
				String type = p.getType().getSimpleName();
				String name = p.getName();
				params.add(new ParameterData(type, name));
			}
			methodDataList.add(new MethodData(methodName, header, returnType, params));
		}
		
		return new TestClassData(className, methodDataList, classFileBytes);
	}
	
	public static List<Method> getStaticMethods(String className, byte[] classFileBytes) throws IOException, ClassNotFoundException 
	{
		ByteArrayClassLoader loader = new ByteArrayClassLoader(classFileBytes);
		Class<?> clazz = loader.loadClass(className);
		// Get all declared methods
        Method[] methods = clazz.getDeclaredMethods();
        
        // remove synthetic methods and instance methods
        // then sort by the method name
        // Ugh, Java map/filter and lambdas are not as nice as C#
        return Arrays.asList(Arrays.stream(methods).
        		filter(m -> !m.isSynthetic() && Modifier.isStatic(m.getModifiers())).
        		sorted((m1, m2) -> {
        			return m1.getName().compareTo(m2.getName());
        		}).
        		toArray(Method[]::new));
	}

    public static List<Method> getStaticMethods(String classFilePath) throws IOException, ClassNotFoundException 
    {
        byte[] classFileBytes = readBytes(classFilePath);
        String className = getClassName(classFileBytes);
        
        return getStaticMethods(className, classFileBytes);
    }

    private static String getClassName(byte[] classFileBytes) throws IOException, ClassNotFoundException {
        // Create a ClassReader to analyze the class file
    	ClassReader reader = new ClassReader(classFileBytes);
    	return reader.getClassName();
    }
    
    private static String getMethodName(Method method)
    {
    	String fullName = method.getName();
        int lastIndex = fullName.lastIndexOf('.');
        return fullName.substring(lastIndex+1);
    }
	
    public static String toHeaderString(Method method)
    {
    	StringBuilder buf = new StringBuilder();
        
        // modifiers
        buf.append(Modifier.toString(method.getModifiers()) + " ");
        // return value
        buf.append(method.getReturnType().getSimpleName() + " ");
        // method name
        String className = getMethodName(method);
        buf.append(className);
        // parameters
        buf.append("(");
        if (method.getParameterCount() > 0)
        {
	        var params =  Arrays.stream(method.getParameters()).map(p -> p.getType().getSimpleName() + " " + p.getName()).toArray(String[]::new);
	        String paramString = String.join(", ", params);
	        buf.append(paramString);
        }
        buf.append(")");
        // exceptions
        if (method.getExceptionTypes().length > 0)
        {
        	var exceptions = Arrays.stream(method.getExceptionTypes()).map(e -> e.getSimpleName()).toArray(String[]::new);
        	String exceptionString = " throws " + String.join(", ", exceptions);
        	buf.append(exceptionString);
        }
        return buf.toString();
    }

    
    
    

    public static void writeJson(TestClassData testClassData, String filePath) throws IOException 
	{
        Map<String, Object> jsonMap = testClassData.toMap();
        

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(new Gson().toJson(jsonMap));
        }
    }
    
    public static void main2(String[] args) throws Exception {
    	String classFilePath = "bin/Check.class";
    	List<Method> staticMethods = getStaticMethods(classFilePath);
    	
    	Method m = staticMethods.get(0);
    	
    	TestClassData td = TestClassData.readJson("check_orig.json");
    	
    	// generate a test case from the test data
    	MethodData md = td.getMethods().get(0);
    	
    	System.out.printf("reflection name: %s; json name: %s\n", m.getName(), md.getName());
    	
    	// need to dynamically create invocations of the method with the given arrays
    	TestCaseData test = md.getTests().get(0);
    	
    	Object[] params = test.getParameterArray(new String[] {"int[]", "int[]"});
    	System.out.println(String.join(", ", Arrays.stream(params).map(o -> o.toString()).toArray(String[]::new)));
    	Object o = m.invoke(null, params);
    	System.out.println(o);
    	
    }

    public static void main(String[] args) throws Exception {
    	String classFilePath = "bin/Check.class";
    	List<Method> staticMethods = getStaticMethods(classFilePath);

        for (Method method : staticMethods) {
            System.out.println("Static method: " + method.getName());
        }
        
        TestClassData td = readFromClassFile(classFilePath);
        writeJson(td, "tmp.json");
        
        TestClassData td2 = TestClassData.readJson("check_orig2.json");
        
        
        System.out.format("%s has %d method, first method has %d tests\n", td2.getClassName(), td2.getMethodCount(), td2.getMethods().get(0).getTests().size());
    }

	static int[] readIntArray(String text)
	{
		Gson gson = new Gson();
		return gson.fromJson(text, int[].class);
	}

	static ArrayList<Integer> readIntegerList(String text)
	{
		return Arrays.stream(readIntArray(text)).boxed().collect(Collectors.toCollection(ArrayList::new));
	}

	static ArrayList<String> readStringList(String text)
	{
		// convert to arraylist
		return Arrays.stream(readStringArray(text)).collect(Collectors.toCollection(ArrayList::new));
	}

	static String readString(String text)
	{
		Gson gson = new Gson();
		return gson.fromJson(text, String.class);
	}

	static double readDouble(String text)
	{
		Gson gson = new Gson();
		return gson.fromJson(text, Double.class);
	}

	static String[] readStringArray(String text)
	{
		Gson gson = new Gson();
		return gson.fromJson(text, String[].class);
	}
	
	static boolean validate(String text, String type)
	{
		if (text == null || text.isEmpty())
			return false;
		try {
			if (type.equals("int[]"))
			{
				readIntArray(text);
				return true;
			}
			if (type.equals("ArrayList<Integer>"))
			{
				//FIXME: because of type erasure, we can't validate this
				//TODO: use GenericTypes annotation to specify the types
				readIntegerList(text);
				return true;
			}
			if (type.equals("ArrayList<String>"))
			{
				//FIXME: type erasure means we don't know the actual type
				readStringList(text);
				return true;
			}
			if (type.equals("int"))
			{
				Integer.parseInt(text);
				return true;
			}
			if (type.equals("String"))
			{
				readString(text);
				return true;
			}
			if (type.equals("String[]"))
			{
				readStringArray(text);
				return true;
			}
			throw new RuntimeException("Unknown type: "+type);
		} catch (Exception e) {
			// what exceptions work here?
			System.err.println(e);
			return false;
		}
		
	}
	

}
