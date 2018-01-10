package com.formulasearchengine.mathmlconverters;

import com.formulasearchengine.mathmlconverters.canonicalize.MathMLCanUtil;
import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLConverter;
import com.formulasearchengine.mathmlconverters.latexml.LaTeXMLServiceResponse;
import com.formulasearchengine.mathmlconverters.mathoid.EnrichedMathMLTransformer;
import com.formulasearchengine.mathmlconverters.mathoid.MathoidConverter;
import com.formulasearchengine.mathmlconverters.util.MathConverterException;
import com.formulasearchengine.mathmltools.mml.CMMLInfo;
import com.formulasearchengine.mathmltools.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import com.formulasearchengine.mathmltools.xmlhelper.XmlNamespaceTranslator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.w3c.dom.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

import java.util.Optional;

/**
 * This Converter is responsible to scan a formula node (TEI format),
 * extract necessary information and transform the node into a well
 * formatted MathML string containing the desired pMML and
 * cMML semantics.
 * <br>
 * This converter is experimental ... kind of.
 *
 * @author Vincent Stange
 */
public class MathMLConverter {

    private static final String DEFAULT_NAMESPACE = "http://www.w3.org/1998/Math/MathML";

    private static Logger logger = Logger.getLogger(MathMLConverter.class);

    private XPath xPath = XMLHelper.namespaceAwareXpath("m", CMMLInfo.NS_MATHML);

    private MathMLConverterConfig config;

    enum Content {
        unknown, // unknown or not recognized
        latex, // latex inline-formula
        pmml, // only presentation MathML
        cmml, // only content MathML
        mathml // well formed MathML (pmml and cmml)
    }

    private String formulaId;

    private String formulaName;

    public MathMLConverter(MathMLConverterConfig config) {
        this();
        this.config = config;
    }

    /**
     * empty local constructor without a configuration
     */
    MathMLConverter() {
    }

    /**
     * This method will scan the formula node, extract necessary information
     * and transform is into a well formatted MathML string containing the
     * desired pMML and cMML semantics.
     *
     * @param formulaNode formula node to be inspected and transformed.
     * @return MathML string
     * @throws Exception              fatal error in the process
     * @throws MathConverterException transformation process failed
     */
    public String transform(Element formulaNode) throws Exception, MathConverterException {
        // 1a. consolidate on the default mathml namespace
        formulaNode = consolidateMathMLNamespace(formulaNode);
        // 1b. scan the inner content format
        Content content = scanFormulaNode(formulaNode);

        String rawMathML;
        if (content == Content.pmml || content == Content.cmml || content == Content.mathml) {
            // 2a. grab the "math" root element
            Element mathEle = grabMathElement(formulaNode);
            formulaId = mathEle.getAttribute("id");
            if (formulaId.equals("")) {
                try {
                    Element applyNode = (Element) XMLHelper.getElementB(formulaNode, xPath.compile("//m:apply"));
                    formulaId = applyNode.getAttribute("id");
                } catch (Exception e) { }
            }
            formulaName = mathEle.getAttribute("name");
            // 2b. try to bring the content into our desired well formatted mathml
            rawMathML = transformMML(mathEle, content);
        } else if (content == Content.latex) {
            // 3a. per default we try to convert it via LaTeXML
            rawMathML = convertLatex(formulaNode.getTextContent());
        } else {
            throw new MathConverterException("formula contains unknown or not recognized content");
        }

        // 4. canonicalize and verify the mathml output
        return verifyMathML(canonicalize(rawMathML));
    }


    /**
     * Just a quick scan over.
     *
     * @param canMathML final mathml to be inspected
     * @return returns input string
     * @throws MathConverterException if it is not a well-structured mathml
     */
    String verifyMathML(String canMathML) throws MathConverterException {
        try {
            Document tempDoc = XMLHelper.string2Doc(canMathML, true);
            Content content = scanFormulaNode((Element) tempDoc.getFirstChild());
            //verify the formula
            if (content == Content.mathml) {
                return canMathML;
            } else {
                throw new MathConverterException("could not verify produced mathml, content was: " + content.name());
            }
        } catch (Exception e) {
            logger.error("could not verify mathml", e);
            throw new MathConverterException("could not verify mathml");
        }
    }

    /**
     * Tries to get the math element which should be next child
     * following the formula node.
     *
     * @param formulaNode formula element
     * @return the subsequent math element
     * @throws XPathExpressionException should not happen or code error
     * @throws MathConverterException   math element not found
     */
    private Element grabMathElement(Element formulaNode) throws XPathExpressionException, MathConverterException {
        Element mathEle = Optional.ofNullable((Element) XMLHelper.getElementB(formulaNode, xPath.compile("./*[1]")))
                .orElseThrow(() -> new MathConverterException("no math element found"));
        // check for the "math" root element
        if (mathEle.getNodeName().toLowerCase().contains("math")) {
            // no math element present
            return mathEle;
        }
        throw new MathConverterException("no math element found");
    }

    /**
     * Should consolidate onto the default MathML namespace.
     *
     * @param mathNode root element of the document
     * @return return the root element of a new document
     * @throws MathConverterException namespace consolidation failed
     */
    Element consolidateMathMLNamespace(Element mathNode) throws MathConverterException {
        try {
            // FIXME ugly node to document transformation since a namespace aware document is required
            Document tempDoc = XMLHelper.string2Doc(XMLHelper.printDocument(mathNode), true);

            // rename namespaces and set a new default namespace
            new XmlNamespaceTranslator()
                    .setDefaultNamespace(DEFAULT_NAMESPACE)
                    .addTranslation("m", "http://www.w3.org/1998/Math/MathML")
                    .addTranslation("mml", "http://www.w3.org/1998/Math/MathML")
                    .translateNamespaces(tempDoc);
            Element root = (Element) tempDoc.getFirstChild();
            removeAttribute(root, "xmlns:mml");
            removeAttribute(root, "xmlns:m");

            // form it into a new document, that is not namespace aware
            Document newDocument = XMLHelper.getNewDocument(true);
            newDocument.appendChild(newDocument.adoptNode(root));
            return (Element) XMLHelper.string2Doc(XMLHelper.printDocument(newDocument), true).getFirstChild();
        } catch (Exception e) {
            logger.error("namespace consolidation failed", e);
            throw new MathConverterException("namespace consolidation failed");
        }
    }

    private void removeAttribute(Node node, String name) {
        try {
            node.getAttributes().removeNamedItem(name);
        } catch (DOMException | NullPointerException e) {
            //Ignore any errors thrown if element does not exist
        }
    }

    /**
     * Tries to scan and interpret a formula node and guess its content format.
     *
     * @param formulaNode formula to be inspected
     * @return sse {@link Content} format
     * @throws Exception parsing error
     */
    Content scanFormulaNode(Element formulaNode) throws Exception {
        // first off, try scanning for mathml nodes directly
        Element annotationNode = (Element) XMLHelper.getElementB(formulaNode, xPath.compile("//m:annotation-xml"));
        //Check if there is an annotationNode and if so check if it's in MathML-Content encoding, which currently indicates LaTeXML Content
        Boolean isLaTeXML = annotationNode != null ? annotationNode.getAttribute("encoding").equals("MathML-Content") : false;
        Element semanticNode = (Element) XMLHelper.getElementB(formulaNode, xPath.compile("//m:semantics"));
        NonWhitespaceNodeList applyNodes = new NonWhitespaceNodeList(XMLHelper.getElementsB(formulaNode, xPath.compile("//m:apply")));
        NonWhitespaceNodeList mrowNodes = new NonWhitespaceNodeList(XMLHelper.getElementsB(formulaNode, xPath.compile("//m:mrow")));
        // both variants are present, if the semantics separator is present everything is fine
        if ((applyNodes.getLength() > 0 && mrowNodes.getLength() > 0) || isLaTeXML) {
            return semanticNode != null ? Content.mathml : Content.unknown;
        }
        // only apply nodes (cmml root element) present?
        if (applyNodes.getLength() > 0) {
            return Content.cmml;
        }
        // or maybe only mrow nodes (pmml root element) present?
        if (mrowNodes.getLength() > 0) {
            return Content.pmml;
        }

        // next, try to identify latex
        Element child = (Element) XMLHelper.getElementB(formulaNode, "./*[1]");
        // if there is no child node, we currently anticipate some form of latex formula
        if (child == null && StringUtils.isNotEmpty(formulaNode.getTextContent())) {
            return Content.latex;
        }

        // found nothing comprehensible
        return Content.unknown;
    }

    /**
     * @param mathEle     math node
     * @param contentForm content format of the math node
     * @return well formatted math ml
     * @throws TransformerException   conversion between formats failed
     * @throws MathConverterException conversion failed, simple info text included
     */
    String transformMML(Element mathEle, Content contentForm) throws TransformerException, MathConverterException {
        switch (contentForm) {
            case mathml:
                return XMLHelper.printDocument(mathEle);
            case cmml:
                // TODO transformation from cmml to pmml
                throw new MathConverterException("cmml transformation not supported");
            case pmml:
                // we try to convert this via Mathoid and LaTeXML
                return convertPmml(mathEle);
            default:
                throw new MathConverterException("should not happen");
        }
    }

    /**
     * Converts from latex to full MathML that contains
     * pMML and cMML semantics. <br/>
     * This method relies on LaTeXMLo.
     *
     * @param latexContent latex formula
     * @return well formatted mathml
     * @throws MathConverterException conversion failed, simple info text included
     */
    String convertLatex(String latexContent) throws MathConverterException {
        LaTeXMLConverter converter = new LaTeXMLConverter(config.getLatexml());
        try {
            LaTeXMLServiceResponse rep = converter.convertLatexml(latexContent);
            if (rep.getStatusCode() == 0) {
                return rep.getResult();
            } else {
                logger.error(String.format("LaTeXMLServiceResponse follows:\n"
                                + "statusCode: %s\nstatus: %s\nlog: %s\nresult: %s",
                        rep.getStatusCode(), rep.getStatus(), rep.getLog(), rep.getResult()));
                throw new MathConverterException("latexml conversion had an error");
            }
        } catch (Exception e) {
            logger.error("latex conversion failed", e);
            throw new MathConverterException("latex conversion failed");
        }
    }

    /**
     * Converts from pmml to enriched Math and then to well formed MathML
     * that contains pMML and cMML semantics. <br/>
     * This method relies on the EnrichedMathMLTransformer
     * which is prone to errors. In the future this should be worked on.
     *
     * @param mathEle math node
     * @return well formatted mathml
     * @throws MathConverterException conversion failed, simple info text included
     * @throws TransformerException   conversion between formats failed
     */
    String convertPmml(Element mathEle) throws MathConverterException, TransformerException {
        String rawMathML = XMLHelper.printDocument(mathEle);
        MathoidConverter converter = new MathoidConverter(config.getMathoid());
        try {
            // pmml > emml > pmml + cmml
            String eMathML = converter.convertMathML(rawMathML);
            EnrichedMathMLTransformer converter2 = new EnrichedMathMLTransformer(eMathML);
            return Optional.ofNullable(converter2.getFullMathML())
                    .orElseThrow(() -> new MathConverterException("enriched mathml conversion failed"));
        } catch (HttpClientErrorException e) {
            logger.error("mathoid conversion failed", e);
            throw new MathConverterException("mathoid conversion failed");
        } catch (ParserConfigurationException | IOException e) {
            logger.error("enriched mathml conversion failed", e);
            throw new MathConverterException("enriched mathml conversion failed");
        } catch (Exception e) {
            logger.error("pmml conversion failed", e);
            throw new MathConverterException("pmml conversion failed");
        }
    }

    /**
     * Calls the {@link MathMLCanUtil}. If an exception occurs,
     * a warning will be logged and the original mathml returned.
     *
     * @param rawMathML raw or original mathml to be normalized
     * @return canonicalize / normalized mathml
     */
    String canonicalize(String rawMathML) {
        try {
            return MathMLCanUtil.canonicalize(rawMathML);
        } catch (Exception e) {
            logger.warn("mathml canonicalization failed", e);
            return rawMathML;
        }
    }

    public String getFormulaId() {
        return formulaId;
    }

    public String getFormulaName() {
        return formulaName;
    }
}
