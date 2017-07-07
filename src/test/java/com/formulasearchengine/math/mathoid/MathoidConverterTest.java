package com.formulasearchengine.math.mathoid;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Vincent Stange
 */
public class MathoidConverterTest {

    @Test
    @Ignore("external service needs to be running or be available")
    public void convertMathML() throws Exception {
        MathoidConverter converter = new MathoidConverter(createTestConfig());
        String actual = converter.convertMathML(getResourceContent("mathoid_1_test.txt"));
        String expected = getResourceContent("mathoid_1_expected.txt");
        assertThat(actual, is(expected));
    }

    @Test
    @Ignore("external service needs to be running or be available")
    public void convertLatex() throws Exception {
        MathoidConverter converter = new MathoidConverter(createTestConfig());
        String actual = converter.convertLatex("\\sqrt{3}+\\frac{a+1}{b-2}");
        String expected = getResourceContent("mathoid_2_expected.txt");
        assertThat(actual, is(expected));
    }

    private MathoidConfig createTestConfig() {
        return new MathoidConfig().setActive(true).setUrl("http://localhost:10044/mml");
    }

    private String getResourceContent(String resourceFilename) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resourceFilename), "UTF-8");
    }

}