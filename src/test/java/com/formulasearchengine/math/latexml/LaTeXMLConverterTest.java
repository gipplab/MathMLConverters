package com.formulasearchengine.math.latexml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author Vincent Stange
 */
public class LaTeXMLConverterTest {

    /**
     * This test needs a local LaTeXML installation. If you don't have
     * one, just @Ignore this test.
     */
    @Test
    @Ignore("installation is not always available")
    public void runLatexmlc() throws Exception {
        // local configuration for the test in json
        String config = "{\n" +
                "  \"url\" : \"\",\n" +
                "  \"params\" : {\n" +
                "    \"whatsin\" : \"math\",\n" +
                "    \"whatsout\" : \"math\",\n" +
                "    \"includestyles\" : \"\",\n" +
                "    \"format\" : \"xhtml\",\n" +
                "    \"pmml\" :\"\",\n" +
                "    \"cmml\" : \"\",\n" +
                "    \"nodefaultresources\" : \"\",\n" +
                "    \"linelength\" : \"90\",\n" +
                "    \"quiet\" : \"\",\n" +
                "    \"preload\" : \"LaTeX.pool,article.cls,amsmath.sty,amsthm.sty,amstext.sty,amssymb.sty,eucal.sty,DLMFmath.sty,[dvipsnames]xcolor.sty,url.sty,hyperref.sty,[ids]latexml.sty,texvc\",\n" +
                "   \"stylesheet\":\"DRMF.xsl\"\n" +
                "  }\n" +
                "}";
        LateXMLConfig lateXMLConfig = new ObjectMapper().readValue(config, LateXMLConfig.class);
        // prepare the converter with a custom configuration
        LaTeXMLConverter converter = new LaTeXMLConverter(lateXMLConfig);

        // test local installation
        String latex = "\\sqrt{3}+\\frac{a+1}{b-2}";
        LaTeXMLServiceResponse serviceResponse = converter.runLatexmlc(latex);

        // validate
        String expected = getResourceContent("latexmlc_result1_expected.txt");
        assertThat(serviceResponse.getStatusCode(), equalTo(0));
        assertThat(serviceResponse.getResult(), equalTo(expected));
    }

    /**
     * Test works with http://gw125.iu.xsede.org:8888
     */
    @Test
    @Ignore("external service needs to be running or be available")
    public void convertLatexmlService() throws Exception {
        // local configuration for the test in json (with DRMF stylesheet)
        String config = "{\n" +
                "  \"url\" : \"http://gw125.iu.xsede.org:8888\",\n" +
                "  \"params\" : {\n" +
                "    \"whatsin\" : \"math\",\n" +
                "    \"whatsout\" : \"math\",\n" +
                "    \"includestyles\" : \"\",\n" +
                "    \"format\" : \"xhtml\",\n" +
                "    \"pmml\" :\"\",\n" +
                "    \"cmml\" : \"\",\n" +
                "    \"nodefaultresources\" : \"\",\n" +
                "    \"linelength\" : \"90\",\n" +
                "    \"quiet\" : \"\",\n" +
                "    \"preload\" : \"LaTeX.pool,article.cls,amsmath.sty,amsthm.sty,amstext.sty,amssymb.sty,eucal.sty,DLMFmath.sty,[dvipsnames]xcolor.sty,url.sty,hyperref.sty,[ids]latexml.sty,texvc\",\n" +
                "   \"stylesheet\":\"DRMF.xsl\"\n" +
                "  }\n" +
                "}";
        LateXMLConfig lateXMLConfig = new ObjectMapper().readValue(config, LateXMLConfig.class);
        LaTeXMLConverter converter = new LaTeXMLConverter(lateXMLConfig);

        // test online service
        String latex = "{\\displaystyle\\RiemannZeta@{s}=\\frac{1}{(1-2^{1-s})\\EulerGamma@{s+1}}\\Int{0}{\\infty}@{x}{\\frac{e^{x}x^{s}}{(e^{x}+1)^{2}}}}";
        LaTeXMLServiceResponse serviceResponse = converter.convertLatexmlService(latex);

        // validate
        String expected = getResourceContent("latexml_service_expected.txt");
        assertThat(serviceResponse.getStatusCode(), equalTo(0));
        assertThat(serviceResponse.getResult(), equalTo(expected));
    }

    @Test
    public void configToUrlString() throws Exception {
        // prepare
        Map<String, String> map = new LinkedHashMap<>();
        map.put("A", "1");
        map.put("B", "");
        map.put("C", "2,3,4,5");

        // test it
        LaTeXMLConverter laTeXMLConverter = new LaTeXMLConverter(null);
        String result = laTeXMLConverter.configToUrlString(map);

        // verify
        assertThat(result, equalTo("&A=1&B&C=2&C=3&C=4&C=5"));
    }

    private String getResourceContent(String resourceFilename) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resourceFilename), "UTF-8");
    }

}