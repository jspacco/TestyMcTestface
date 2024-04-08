import java.util.Arrays;

public class Check
{
	public static int mystery0(int[] nums1, int[] nums2)
	{
		int x = 0;
		for (int n : nums1) if (n > 0) x++;
		for (int n : nums2) if (n > 0) x++;
		return x;
	}
	
	public static boolean mystery1(int[] nums1, int[] nums2)
	{
		return Arrays.equals(Arrays.stream(nums1).map(x -> x+1).toArray(), nums2);
	}
	
	public static int mystery2(int[] nums)
	{
		int x = 0;
		for (int n : nums) if (n > 0) x++;
		return x;
	}
	
	public static int mystery3(int[] nums)
	{
		int x = 0;
		for (int n : nums) if (n < 0) x++;
		return x;
	}
	
	public static void mystery4(int[] nums, int x)
	{
		nums[x] = x;
	}

	public static int mystery5(String[] words)
	{
		int x = 0;
		for (String w : words) if (w != null && w.length() > 0) x++;
		return x;
	}

	public static int mystery6(int[] nums, int target) {
		int count = 0;
		for (int n : nums) {
		  if (n == target) { // Check if element equals the target value
			count++;
		  }
		}
		return count;
	}

	public static boolean mystery7(int[] nums) {
		return nums.length == 0; // Check if array length is 0 (empty)
	}

	public static String mystery8(String str) {
		return str.toUpperCase(); // Use String method to uppercase all characters
	}

	public static int mystery9(int[] nums, int target) {
		for (int i = 0; i < nums.length; i++) {
		  if (nums[i] == target) {
			return i; // Return index if target found
		  }
		}
		return -1; // Return -1 if target not found
	}

	public static int mystery10(int[] nums) {
		int sum = 0;
		for (int n : nums) {
		  sum += n; // Add each element to sum
		}
		return sum;
	}

	public static int mystery11(int[] nums) {
		int sum = 0;
		for (int n : nums) {
		  if (n % 2 == 0) {
			sum += n; // Add even numbers to sum
		  }
		}
		return sum;
	}

	public static boolean mystery12(String str) {
		StringBuilder reversed = new StringBuilder(str).reverse(); // Reverse the string
		return str.equals(reversed.toString()); // Compare original and reversed string
	}

	public static int mystery13(String[] words) {
		int count = 0;
		for (String w : words) {
		  if (w.length() > 0) {
			count++; // Increment count if word length is greater than 0
		  }
		}
		return count;
	}

	public static int mystery14(int[] nums) {
		int sum = 0;
		for (int n : nums) {
		  if (n > 0) {
			sum += n; // Add positive numbers to sum
		  }
		}
		return sum;
	}

	public static int mystery15(int[] nums) {
		int sum = 0;
		for (int n : nums) {
		  if (n < 0) {
			sum += n; // Add negative numbers to sum
		  }
		}
		return sum;
	}

	public static int mystery16(int[] nums, int num1, int num2) {
		// count all the values that are between num1 and num2
		int count = 0;
		for (int n : nums) {
		  if (n > num1 && n < num2) {
			count++; // Increment count if value is between num1 and num2
		  }
		}
		return count;
	}

	public static int mystery17(int[] nums, int num1, int num2) {
		// count all the values divisible by num1 but not by num2
		int count = 0;
		for (int n : nums) {
		  if (n % num1 == 0 && n % num2 != 0) {
			count++; // Increment count if value is divisible by num1 but not by num2
		  }
		}
		return count;
	}

	public static int mystery18(int[] nums, int num1, int num2)
	{
		// count all the values that are between num1 and num2, or between num2 and num1
		int count = 0;
		for (int n : nums) {
		  if ((n > num1 && n < num2) || (n > num2 && n < num1)) {
			count++; // Increment count if value is between num1 and num2, or between num2 and num1
		  }
		}
		return count;
	}



	  
	
	public int instanceMethodBAD(int[] nums)
	{
		return nums[0];
	}
}
