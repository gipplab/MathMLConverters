# MathML Converters

[![Build Status](https://travis-ci.org/ag-gipp/MathMLConverters.svg?branch=master)](https://travis-ci.org/ag-gipp/MathMLConverters)

This library is a collection of utilities and service calls to convert from various input formats to MathML.
The desired MathML output is always a well-formed MathML containing the presentation and content semantic. 

  * LaTeXML Service Call: LaTeXML to MathML
  * Mathoid Service Call: LaTeXML to Enriched PMML
  * Mathoid Service Call: PMML to Enriched PMML
  * EnrichedMathMLTransformer: Enriched PMML to MathML

### Dependencies ###

Note-worthy dependencies for this library.

**MathML Tools**: Library with various tools for processing MathML using Java. (https://github.com/physikerwelt/MathMLTools)

    <dependency>
        <groupId>com.formulasearchengine</groupId>
        <artifactId>mathmltools</artifactId>
        <version>...</version>
    </dependency>

**MathML-Canonicalizer**: Canonicalizer for MathML formulas. (https://github.com/michal-ruzicka/MathMLCan)

    <!--  -->
    <dependency>
        <groupId>cz.muni.fi.mir</groupId>
        <artifactId>mathml-canonicalizer</artifactId>
        <version>...</version>
    </dependency>


