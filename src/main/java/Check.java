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
	
	public int instanceMethodBAD(int[] nums)
	{
		return nums[0];
	}
}
