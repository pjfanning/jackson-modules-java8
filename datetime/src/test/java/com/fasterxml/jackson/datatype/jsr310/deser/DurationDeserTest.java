package com.fasterxml.jackson.datatype.jsr310.deser;

import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.TemporalAmount;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.MockObjectConfiguration;
import com.fasterxml.jackson.datatype.jsr310.ModuleTestBase;

public class DurationDeserTest extends ModuleTestBase
{
    private final ObjectReader READER = newMapper().readerFor(Duration.class);

    @Test
    public void testDeserializationAsFloat01() throws Exception
    {
        Duration value = READER.with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("60.0");

        assertNotNull("The value should not be null.", value);
        Duration exp = Duration.ofSeconds(60L, 0);
        assertEquals("The value is not correct.", exp,  value);
    }

    @Test
    public void testDeserializationAsFloat02() throws Exception
    {
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("60.0");

        assertNotNull("The value should not be null.", value);
        Duration exp = Duration.ofSeconds(60L, 0);
        assertEquals("The value is not correct.", exp, value);
    }

    @Test
    public void testDeserializationAsFloat03() throws Exception
    {
        Duration value = READER.with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("13498.000008374");
        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", Duration.ofSeconds(13498L, 8374), value);
    }

    @Test
    public void testDeserializationAsFloat04() throws Exception
    {
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("13498.000008374");
        assertNotNull("The value should not be null.", value);
        assertEquals("The value is not correct.", Duration.ofSeconds(13498L, 8374), value);
    }

    /**
     * Test the upper-bound of Duration.
     */
    @Test
    public void testDeserializationAsFloatEdgeCase01() throws Exception
    {
        String input = Long.MAX_VALUE + ".999999999";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(Long.MAX_VALUE, value.getSeconds());
        assertEquals(999999999, value.getNano());
    }

    /**
     * Test the lower-bound of Duration.
     */
    @Test
    public void testDeserializationAsFloatEdgeCase02() throws Exception
    {
        String input = Long.MIN_VALUE + ".0";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(Long.MIN_VALUE, value.getSeconds());
        assertEquals(0, value.getNano());
    }

    @Test(expected = ArithmeticException.class)
    public void testDeserializationAsFloatEdgeCase03() throws Exception
    {
        // Duration can't go this low
        READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(Long.MIN_VALUE + ".1");
    }

    /*
     * DurationDeserializer currently uses BigDecimal.longValue() which has surprising behavior
     * for numbers outside the range of Long.  Numbers less than 1e64 will result in the lower 64 bits.
     * Numbers at or above 1e64 will always result in zero.
     */

    @Test
    public void testDeserializationAsFloatEdgeCase04() throws Exception
    {
        // Just beyond the upper-bound of Duration.
        String input = new BigInteger(Long.toString(Long.MAX_VALUE)).add(BigInteger.ONE) + ".0";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(Long.MIN_VALUE, value.getSeconds());  // We've turned a positive number into negative duration!
    }

    @Test
    public void testDeserializationAsFloatEdgeCase05() throws Exception
    {
        // Just beyond the lower-bound of Duration.
        String input = new BigInteger(Long.toString(Long.MIN_VALUE)).subtract(BigInteger.ONE) + ".0";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(Long.MAX_VALUE, value.getSeconds());  // We've turned a negative number into positive duration!
    }

    @Test
    public void testDeserializationAsFloatEdgeCase06() throws Exception
    {
        // Into the positive zone where everything becomes zero.
        String input = "1e64";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(0, value.getSeconds());
    }

    @Test
    public void testDeserializationAsFloatEdgeCase07() throws Exception
    {
        // Into the negative zone where everything becomes zero.
        String input = "-1e64";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(0, value.getSeconds());
    }

    /**
     * Numbers with very large exponents can take a long time, but still result in zero.
     * https://github.com/FasterXML/jackson-databind/issues/2141
     */
    @Test(timeout = 100)
    public void testDeserializationAsFloatEdgeCase08() throws Exception
    {
        String input = "1e10000000";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(0, value.getSeconds());
    }

    @Test(timeout = 100)
    public void testDeserializationAsFloatEdgeCase09() throws Exception
    {
        String input = "-1e10000000";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(0, value.getSeconds());
    }

    /**
     * Same for large negative exponents.
     */
    @Test(timeout = 100)
    public void testDeserializationAsFloatEdgeCase10() throws Exception
    {
        String input = "1e-10000000";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(0, value.getSeconds());
    }

    @Test(timeout = 100)
    public void testDeserializationAsFloatEdgeCase11() throws Exception
    {
        String input = "-1e-10000000";
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                                 .readValue(input);
        assertEquals(0, value.getSeconds());
    }


    @Test
    public void testDeserializationAsInt01() throws Exception
    {
        Duration value = READER.with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("60");
        assertEquals("The value is not correct.", Duration.ofSeconds(60L, 0),  value);
    }

    @Test
    public void testDeserializationAsInt02() throws Exception
    {
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("60000");
        assertEquals("The value is not correct.", Duration.ofSeconds(60L, 0),  value);
    }

    @Test
    public void testDeserializationAsInt03() throws Exception
    {
        Duration value = READER.with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("13498");
        assertEquals("The value is not correct.", Duration.ofSeconds(13498L, 0),  value);
    }

    @Test
    public void testDeserializationAsInt04() throws Exception
    {
        Duration value = READER.without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue("13498000");
        assertEquals("The value is not correct.", Duration.ofSeconds(13498L, 0),  value);
    }

    @Test
    public void testDeserializationAsString01() throws Exception
    {
        Duration exp = Duration.ofSeconds(60L, 0);
        Duration value = READER.readValue('"' + exp.toString() + '"');
        assertEquals("The value is not correct.", exp,  value);
    }

    @Test
    public void testDeserializationAsString02() throws Exception
    {
        Duration exp = Duration.ofSeconds(13498L, 8374);
        Duration value = READER.readValue('"' + exp.toString() + '"');
        assertEquals("The value is not correct.", exp,  value);
    }

    @Test
    public void testDeserializationAsString03() throws Exception
    {
        assertNull("The value should be null.", READER.readValue("\"   \""));
    }

    @Test
    public void testDeserializationWithTypeInfo01() throws Exception
    {
        Duration duration = Duration.ofSeconds(13498L, 8374);

        String prefix = "[\"" + Duration.class.getName() + "\",";

        ObjectMapper mapper = newMapperBuilder()
                .addMixIn(TemporalAmount.class, MockObjectConfiguration.class)
                .build();
        TemporalAmount value = mapper.readerFor(TemporalAmount.class)
                .without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue(prefix + "13498.000008374]");

        assertTrue("The value should be a Duration.", value instanceof Duration);
        assertEquals("The value is not correct.", duration, value);
    }

    @Test
    public void testDeserializationWithTypeInfo02() throws Exception
    {
        String prefix = "[\"" + Duration.class.getName() + "\",";

        ObjectMapper mapper = newMapperBuilder()
                .addMixIn(TemporalAmount.class, MockObjectConfiguration.class)
                .build();
        TemporalAmount value = mapper.readerFor(TemporalAmount.class)
                .with(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue(prefix + "13498]");
        assertTrue("The value should be a Duration.", value instanceof Duration);
        assertEquals("The value is not correct.", Duration.ofSeconds(13498L), value);
    }

    @Test
    public void testDeserializationWithTypeInfo03() throws Exception
    {
        String prefix = "[\"" + Duration.class.getName() + "\",";

        ObjectMapper mapper = newMapperBuilder()
                .addMixIn(TemporalAmount.class, MockObjectConfiguration.class)
                .build();
        TemporalAmount value = mapper
                .readerFor(TemporalAmount.class)
                .without(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .readValue(prefix + "13498837]");
        assertTrue("The value should be a Duration.", value instanceof Duration);
        assertEquals("The value is not correct.", Duration.ofSeconds(13498L, 837000000), value);
    }

    @Test
    public void testDeserializationWithTypeInfo04() throws Exception
    {
        Duration duration = Duration.ofSeconds(13498L, 8374);
        String prefix = "[\"" + Duration.class.getName() + "\",";
        ObjectMapper mapper = newMapperBuilder()
            .addMixIn(TemporalAmount.class, MockObjectConfiguration.class)
            .build();
        TemporalAmount value = mapper.readerFor(TemporalAmount.class)
                .readValue(prefix + '"' + duration.toString() + "\"]");
        assertTrue("The value should be a Duration.", value instanceof Duration);
        assertEquals("The value is not correct.", duration, value);
    }
    
    @Test
    public void testDeserializationAsArrayDisabled() throws Exception {
    	Duration exp = Duration.ofSeconds(13498L, 8374);
    	try {
	        READER.readValue("[\"" + exp.toString() + "\"]");
	        fail("expected JsonMappingException");
        } catch (JsonMappingException e) {
           // OK
        }
    }
    
    
    @Test
    public void testDeserializationAsEmptyArrayDisabled() throws Throwable
    {
        try {
            READER.readValue("[]");
            fail("expected MismatchedInputException");
        } catch (MismatchedInputException e) {
            // 17-Aug-2019, tatu: Message differs between 2.10 and 3.0...
            verifyException(e, "Cannot deserialize value of type `java.time.Duration` out of START_ARRAY");
        }
        try {
            READER.with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
        	        .readValue(aposToQuotes("[]"));
            fail("expected MismatchedInputException");
        } catch (MismatchedInputException e) {
            verifyException(e, "Unexpected token (END_ARRAY), expected one of");
        }
    }

    @Test
    public void testDeserializationAsArrayEnabled() throws Exception {
        Duration exp = Duration.ofSeconds(13498L, 8374);
        Duration value = newMapper().readerFor(Duration.class)
                .with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .readValue("[\"" + exp.toString() + "\"]");
        assertEquals("The value is not correct.", exp,  value);
    }
   
    @Test
    public void testDeserializationAsEmptyArrayEnabled() throws Throwable
    {
        Duration value= newMapper().readerFor(Duration.class)
                .with(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS,
                        DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .readValue("[]");
        assertNull(value);
    }
}
