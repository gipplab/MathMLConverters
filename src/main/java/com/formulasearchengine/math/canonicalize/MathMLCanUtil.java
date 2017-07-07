package com.formulasearchengine.math.canonicalize;

import cz.muni.fi.mir.mathmlcanonicalization.ConfigException;
import cz.muni.fi.mir.mathmlcanonicalization.MathMLCanonicalizer;
import cz.muni.fi.mir.mathmlcanonicalization.modules.ModuleException;
import org.apache.commons.io.IOUtils;
import org.jdom2.JDOMException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Prepares the {@link MathMLCanonicalizer} based on our custom configuration.
 * This purely a utility class and therefore has no public constructur.
 *
 * @author Vincent Stange
 */
public final class MathMLCanUtil {

    private static final MathMLCanonicalizer CANONICALIZER;

    static {
        // load our custom configuration and the CANONICALIZER itself
        try (InputStream configIS = MathMLCanUtil.class.getClassLoader()
                .getResourceAsStream("com/formulasearchengine/math/canonicalize/canonicalizer-config.xml")) {
            CANONICALIZER = new MathMLCanonicalizer(configIS);
        } catch (final IOException e) {
            throw new RuntimeException("Could not find config for CANONICALIZER, exiting", e);
        } catch (final ConfigException e) {
            throw new RuntimeException("Unable to configure CANONICALIZER, exiting", e);
        }
    }

    private MathMLCanUtil() {
        // not visible, utility class only
    }

    /**
     * Canonicalize an input MathML string.
     * Line separators are system dependant.
     *
     * @param mathml MathML string
     * @return Canonicalized MathML string
     * @throws JDOMException      problem with DOM
     * @throws IOException        problem with streams
     * @throws ModuleException    some module cannot canonicalize the input
     * @throws XMLStreamException an error with XML processing occurs
     */
    public static String canonicalize(String mathml) throws IOException, JDOMException, XMLStreamException, ModuleException {
        InputStream input = IOUtils.toInputStream(mathml, StandardCharsets.UTF_8.toString());
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        CANONICALIZER.canonicalize(input, output);
        String result = output.toString(StandardCharsets.UTF_8.toString());
        // xml header will not be used
        if (result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n")) {
            result = result.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n".length());
        }
        return result.replaceAll("\\r\\n", System.getProperty("line.separator"));
    }
}
