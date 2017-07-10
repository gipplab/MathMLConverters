package com.formulasearchengine.math;

import com.formulasearchengine.math.latexml.LaTeXMLConverterTest;
import com.formulasearchengine.math.latexml.LateXMLConfig;
import com.formulasearchengine.math.mathoid.MathoidConfig;
import com.formulasearchengine.math.mathoid.MathoidConverterTest;
import com.formulasearchengine.math.util.MathConverterException;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace;
import static org.junit.Assert.assertThat;

/**
 * @author Vincent Stange
 */
public class MathMLConverterTest {

    @Test
    @Ignore("need external services")
    public void convertPmml() throws Exception, MathConverterException {
        // prepare configuration and objects
        Document mathNode = XMLHelper.string2Doc(getResourceContent("mathml_pmml.txt"), false);
        MathMLConverterConfig mathConfig = new MathMLConverterConfig().setMathoid(new MathoidConfig().setActive(true).setUrl(MathoidConverterTest.HTTP_MATHOID_TEXT));
        MathMLConverter mathMLConverter = new MathMLConverter(mathConfig);
        // test
        String result = mathMLConverter.convertPmml((Element) mathNode.getFirstChild());
        assertThat(result, is(getResourceContent("mathml_pmml_expected.txt")));
    }

    @Test
    @Ignore("need latexml service")
    public void transform_latex_1() throws Exception, MathConverterException {
        // prepare configuration and objects
        Document formulaNode = XMLHelper.string2Doc(getResourceContent("mathml_latex_1.txt"), false);
        MathMLConverterConfig mathConfig = new MathMLConverterConfig().setLatexml(new LateXMLConfig().setActive(true).setUrl(LaTeXMLConverterTest.HTTP_LATEXML_TEST));
        MathMLConverter mathMLConverter = new MathMLConverter(mathConfig);
        // test
        String result = mathMLConverter.transform((Element) formulaNode.getFirstChild());
        assertThat(result, equalToIgnoringWhiteSpace(getResourceContent("mathml_latex_1_expected.txt")));
    }

    @Test(expected = MathConverterException.class)
//    @Ignore("need latexml service")
    public void transform_latex_2() throws Exception, MathConverterException {
        // prepare configuration and objects
        Document formulaNode = XMLHelper.string2Doc(getResourceContent("mathml_latex_2.txt"), false);
        MathMLConverterConfig mathConfig = new MathMLConverterConfig().setLatexml(new LateXMLConfig().setActive(true).setUrl(LaTeXMLConverterTest.HTTP_LATEXML_TEST));
        MathMLConverter mathMLConverter = new MathMLConverter(mathConfig);
        // test
        String result = mathMLConverter.transform((Element) formulaNode.getFirstChild());
        assertThat(result, is(getResourceContent("mathml_latex_2_expected.txt")));
    }

    @Test()
    public void transform_mathml_1() throws Exception, MathConverterException {
        Document formulaNode = XMLHelper.string2Doc(getResourceContent("mathml_perfect_1.txt"), false);
        MathMLConverter mathMLConverter = new MathMLConverter();
        String result = mathMLConverter.transform((Element) formulaNode.getFirstChild());
        assertThat(result, equalToIgnoringWhiteSpace(getResourceContent("mathml_perfect_1_expected.txt")));
    }


    @Test
    public void consolidateNamespace_m() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc("<m:math xmlns:m=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "<m:mrow>Test</m:mrow>\n" +
                "</m:math>", false);
        Element result = new MathMLConverter().consolidateMathMLNamespace((Element) mathNode.getFirstChild());
        String actual = XMLHelper.printDocument(result);
        assertThat(actual, is("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n   <mrow>Test</mrow>\n</math>"));
    }

    @Test
    public void consolidateNamespace_mml() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc("<mml:math xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "<mml:mrow><mml:mi>%</mml:mi></mml:mrow>\n" +
                "</mml:math>", false);
        Element result = new MathMLConverter().consolidateMathMLNamespace((Element) mathNode.getFirstChild());
        String actual = XMLHelper.printDocument(result);
        assertThat(actual, is("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "   <mrow>\n" +
                "      <mi>%</mi>\n" +
                "   </mrow>\n" +
                "</math>"));
    }


    @Test
    public void scanFormulaNode_pmml() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "<mrow><mi>%</mi></mrow></math>", false);
        MathMLConverter.Content result = new MathMLConverter().scanFormulaNode((Element) mathNode.getFirstChild());
        assertThat(result, is(MathMLConverter.Content.pmml));
    }

    @Test
    public void scanFormulaNode_pmml_2() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc("<formula xmlns=\"http://www.w3.org/1998/Math/MathML\" rend=\"display\">\n" +
                "   <math xmlns:mml=\"http://www.w3.org/1998/Math/MathML\"\n" +
                "         xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "         id=\"m1\">\n" +
                "      <mrow>\n" +
                "         <mi mathvariant=\"italic\">Circularity</mi>\n" +
                "         <mo>=</mo>\n" +
                "         <mn>4</mn>\n" +
                "         <mo>π</mo>\n" +
                "         <mo>×</mo>\n" +
                "         <mfrac>\n" +
                "            <mrow>\n" +
                "               <mi mathvariant=\"italic\">Area</mi>\n" +
                "            </mrow>\n" +
                "            <mrow>\n" +
                "               <msup>\n" +
                "                  <mi mathvariant=\"italic\">Perimeter</mi>\n" +
                "                  <mn>2</mn>\n" +
                "               </msup>\n" +
                "            </mrow>\n" +
                "         </mfrac>\n" +
                "      </mrow>\n" +
                "   </math>\n" +
                "</formula>", false);
        MathMLConverter.Content result = new MathMLConverter().scanFormulaNode((Element) mathNode.getFirstChild());
        assertThat(result, is(MathMLConverter.Content.pmml));
    }

    @Test
    public void scanFormulaNode_cmml() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc("<math xmlns=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "<apply><times/><ci>x</ci><ci>y</ci></apply></math>", false);
        MathMLConverter.Content result = new MathMLConverter().scanFormulaNode((Element) mathNode.getFirstChild());
        assertThat(result, is(MathMLConverter.Content.cmml));
    }

    @Test
    public void scanFormulaNode_mathml() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc(getResourceContent("mathml_perfect_1.txt"), false);
        MathMLConverter.Content result = new MathMLConverter().scanFormulaNode((Element) mathNode.getFirstChild());
        assertThat(result, is(MathMLConverter.Content.mathml));
    }

    @Test
    public void scanFormulaNode_latex() throws Exception, MathConverterException {
        Document mathNode = XMLHelper.string2Doc("<mml:math xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n" +
                "a+b\n" +
                "</mml:math>", false);
        MathMLConverter.Content result = new MathMLConverter().scanFormulaNode((Element) mathNode.getFirstChild());
        assertThat(result, is(MathMLConverter.Content.latex));
    }


    @Test
    public void canonilize() throws Exception, MathConverterException {
        // prepare and execute
        String actualMathML = new MathMLConverter().canonicalize(getResourceContent("mathml_canonilize_1.xml"));
        String expected = getResourceContent("mathml_canonilize_expected.xml");
        // they should be equal - if not, the UnaryOperatorRemover is active and this should not be!
        assertThat(actualMathML, is(expected));
    }


    @Test
    public void verifyMathML() throws Exception, MathConverterException {
        String shouldbeOkay = new MathMLConverter().verifyMathML(getResourceContent("mathml_canonilize_expected.xml"));
        assertThat(shouldbeOkay, notNullValue());
    }

    @Test(expected = MathConverterException.class)
    public void verifyMathML_exception() throws Exception, MathConverterException {
        // empty document
        new MathMLConverter().verifyMathML("<math />");
    }


    private String getResourceContent(String resourceFilename) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resourceFilename), "UTF-8");
    }

}