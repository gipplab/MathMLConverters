package com.formulasearchengine.math.latexml;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration for the LaTeXML service.
 *
 * @author Vincent Stange
 */
public class LateXMLConfig {

    private boolean active = true;

    private String url = "";

    private Map<String, String> params = new LinkedHashMap<>();

    public LateXMLConfig() {
        // empty constructor
    }

    public boolean isActive() {
        return active;
    }

    public LateXMLConfig setActive(boolean active) {
        this.active = active;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public LateXMLConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getParams() {
        if (params.isEmpty()) {
            params = getDefaultConfiguration();
        }
        return params;
    }

    public LateXMLConfig setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public static Map<String, String> getDefaultConfiguration() {
        Map<String, String> config = new LinkedHashMap<>();
        config.put("whatsin", "math");
        config.put("whatsout", "math");
        config.put("includestyles", null);
        config.put("format", "xhtml");
        config.put("pmml", null);
        config.put("cmml", null);
        config.put("nodefaultresources", null);
        config.put("linelength", "90");
        config.put("quiet", null);
        config.put("preload", "LaTeX.pool,article.cls,amsmath.sty,amsthm.sty,amstext.sty,amssymb.sty,eucal.sty,DLMFmath.sty,[dvipsnames]xcolor.sty,url.sty,hyperref.sty,[ids]latexml.sty,texvc");
        config.put("stylesheet", "DRMF.xsl");
        return config;
    }
}
