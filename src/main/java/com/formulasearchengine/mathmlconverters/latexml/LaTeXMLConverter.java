package com.formulasearchengine.mathmlconverters.latexml;

import com.formulasearchengine.mathmlconverters.util.CommandExecutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Main approach for conversion from a latex formula to
 * a MathML representation via LaTeXML.
 *
 * @author Vincent Stange
 */
public class LaTeXMLConverter {

    private static Logger logger = Logger.getLogger(LaTeXMLConverter.class);

    private final LateXMLConfig lateXMLConfig;

    public LaTeXMLConverter(LateXMLConfig lateXMLConfig) {
        this.lateXMLConfig = lateXMLConfig;
    }

    /**
     * Converts a latex formula string into mathml and includes
     * pmml, cmml and tex semantics. If no url in the config is given,
     * the local installation is used.
     *
     * @param latex Latex Formula
     * @return MathML output in the result of LaTeXMLServiceResponse
     * @throws Exception Execution of latexmlc failed.
     */
    public LaTeXMLServiceResponse convertLatexml(String latex) throws Exception {
        // no url = use the local installation of latexml, otherwise use: url = online service
        if (StringUtils.isEmpty(lateXMLConfig.getUrl())) {
            logger.debug("local latex conversion");
            return runLatexmlc(latex);
        } else {
            logger.debug("service latex conversion");
            return convertLatexmlService(latex);
        }
    }

    /**
     * This methods needs a LaTeXML installation. It converts a latex formula
     * string into mathml and includes pmml, cmml and tex semantics.
     * Conversion is executed by "latexmlc".
     *
     * @param latex Latex Formula
     * @return MathML output in the result of LaTeXMLServiceResponse
     * @throws Exception Execution of latexmlc failed.
     */
    public LaTeXMLServiceResponse runLatexmlc(String latex) throws Exception {
        CommandExecutor latexmlmath = new CommandExecutor("latexmlc",
                "--includestyles",
                "--format=xhtml",
                "--whatsin=math",
                "--whatsout=math",
                "--pmml",
                "--cmml",
                "--nodefaultresources",
                "--linelength=90",
                "--quiet",
                "--preload", "LaTeX.pool",
                "--preload", "article.cls",
                "--preload", "amsmath.sty",
                "--preload", "amsthm.sty",
                "--preload", "amstext.sty",
                "--preload", "amssymb.sty",
                "--preload", "eucal.sty",
                "--preload", "[dvipsnames]xcolor.sty",
                "--preload", "url.sty",
                "--preload", "hyperref.sty",
                "--preload", "[ids]latexml.sty",
                "--preload", "texvc",
                "literal:" + latex);
        return new LaTeXMLServiceResponse(latexmlmath.exec(2000L), "Conversion via local installation of latexmlc");
    }

    /**
     * Call a LaTeXML service.
     *
     * @param latex LaTeX formula
     * @return MathML String
     */
    public LaTeXMLServiceResponse convertLatexmlService(String latex) {
        String payload = "format=xhtml" + configToUrlString(lateXMLConfig.getParams()) + "&tex=literal:" + latex;

        RestTemplate restTemplate = new RestTemplate();
        try {
            LaTeXMLServiceResponse rep = restTemplate.postForObject(lateXMLConfig.getUrl(), payload, LaTeXMLServiceResponse.class);
            logger.debug(String.format("LaTeXMLServiceResponse:\n"
                            + "statusCode: %s\nstatus: %s\nlog: %s\nresult: %s",
                    rep.getStatusCode(), rep.getStatus(), rep.getLog(), rep.getResult()));
            return rep;
        } catch (HttpClientErrorException e) {
            logger.error(e.getResponseBodyAsString());
            throw e;
        }
    }

    /**
     * Create a URL Path consisting of the mapped values.
     *
     * @param values map of properties converted into a URL path.
     * @return String representation of the URL path.
     */
    String configToUrlString(Map<String, Object> values) {
        StringBuilder sb = new StringBuilder();
        values.forEach((k, v) -> {
            if (v instanceof List) {
                for (Object ele : (List) v) {
                    appendParameterToUrlString(sb, k, ele);
                }
            } else {
                appendParameterToUrlString(sb, k, v);
            }
        });
        return sb.toString();
    }

    private void appendParameterToUrlString(StringBuilder sb, String key, Object rawValue) {
        String value = String.valueOf(rawValue);
        sb.append("&").append(key);
        if (!"".equals(value)) {
            sb.append("=").append(value);
        }
    }
}