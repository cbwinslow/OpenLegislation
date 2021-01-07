package gov.nysenate.openleg.common.util;

import gov.nysenate.openleg.config.annotation.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static gov.nysenate.openleg.common.util.RomanNumerals.*;
import static org.junit.Assert.assertEquals;

@Category(UnitTest.class)
public class RomanNumeralsTest {
    @Test
    public void numeralTest() {
        test("I", "ONE", 1);
        test("IV", "FOUR", 4);
        test("XII", "TWELVE", 12);
        test("XXIX", "TWENTY-NINE", 29);
        test("XXXV", "THIRTY-FIVE", 35);
        test("XL", "FORTY", 40);
        test("XCIX", "NINETY-NINE", 99);
        test("CLXIX", "ONE HUNDRED SIXTY-NINE", 169);
    }

    private static void test(String numeral, String word, int num) {
        assertEquals(numeralToInt(numeral), num);
        assertEquals(numeral, intToNumeral(num));
        assertEquals(numberToWord(num), word);
        assertEquals("(" + num + "|" + numeral + "|" + word + ")", allOptions(Integer.toString(num)));
    }
}
