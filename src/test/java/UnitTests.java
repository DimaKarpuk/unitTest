import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

@EnabledOnJre(JRE.JAVA_22)
@EnabledOnOs(OS.WINDOWS)
public class UnitTests {
    private final int[] arr = {1, 2, 3, 4, 5};
    private final int[] arr1 = new int[0];

    @Test
    public void findMaxTest() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(5, findMax(arr)),
                () -> Assertions.assertThrows(IllegalArgumentException.class, () -> findMax(arr1))
        );
    }

    @Test
    public void isPalindromeTrueTest() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(isPalindrome("заказ"),"слово заказ не является палиндромом"),
                () -> Assertions.assertFalse(isPalindrome("крышаa"),"не прошел 1"),
                () -> Assertions.assertFalse(isPalindrome("крышаa"),"не прошел 1"),
                () -> Assertions.assertFalse(isPalindrome("")),
                () -> Assertions.assertFalse(isPalindrome(null)),
                () -> Assertions.assertTrue(isPalindrome("12321"), "не прошел 2"),
                () -> Assertions.assertFalse(isPalindrome("123211")));
    }

    @Test
    public void isLeapYearTest() {
        Assertions.assertAll(
                () -> Assertions.assertTrue(isLeapYear(2012)),
                () -> Assertions.assertTrue(isLeapYear(2016)),
                () -> Assertions.assertFalse(isLeapYear(2017)),
                () -> Assertions.assertFalse(isLeapYear(111)),
                () -> Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> isLeapYear(-100)));
    }

    @Test
    public void factorialTest() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(120, factorial(5)),
                () -> Assertions.assertNotEquals(130, factorial(5)),
                () -> Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> factorial(-1)));
    }

    @Test
    public void sumArrayTest() {
        Assertions.assertAll(
                () -> Assertions.assertEquals(15, sumArray(arr)),
                () -> Assertions.assertNotEquals(12, sumArray(arr))
        );
    }


    public int findMax(int[] numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("Array must not be null or empty");
        }
        int max = numbers[0];
        for (int num : numbers) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    public boolean isPalindrome(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        str = str.replaceAll("[^a-zA-Z0-9а-яА-Я]", "").toLowerCase();
        int left = 0;
        int right = str.length() - 1;

        while (left < right) {
            if (str.charAt(left) != str.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    public boolean isLeapYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("Year must be greater than 0");
        }
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    public int factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }
        int result = 1;
        for (int i = 1; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    public static int sumArray(int[] numbers) {
        if (numbers == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        return sum;
    }
}
