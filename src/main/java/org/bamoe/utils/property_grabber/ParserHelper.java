/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.bamoe.utils.property_grabber;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.javadoc.JavadocBlockTag;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserHelper {

    private static final Logger logger = LoggerFactory.getLogger(ParserHelper.class);

    private static final String TXT_PROPERTY_FORMAT = "Config Name: %s | Description: %s | Type: %s | Default: %s";

    /**
     * Map of the annotation name and its "name" attribute.
     */
    static final Map<String, String> ANNOTATION_NAME_MAP = Map.of("IfBuildProperty", "name",
            "UnlessBuildProperty", "name",
            "ConfigProperty", "name");

/*    *//**
     * Map of the annotation name and its "type" attribute.
     *//*
    private static final Map<String, String> ANNOTATION_TYPE_MAP = Map.of("IfBuildProperty", "name",
            "UnlessBuildProperty", "name");*/

    /**
     * Asciidoc format for a single configuration row in a table.
     * a| [name]
     * [.description]
     * --
     * [description]
     * --
     * | [type]
     * | [default]
     */
    private static final String ADOC_PROPERTY_FORMAT = """
            a| `%s`
            [.description]
            --
            %s
            --
            | %s
            | %s""";

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }

    public static String getProperties(Path javaCode) {
        return getProperties(javaCode, TXT_PROPERTY_FORMAT);
    }

    /**
     * Get the information about a configuration option, but formatted for an Asciidoc table.
     *
     * @param javaCode Path to a Java source file.
     * @return All configuration properties within the specified Java source file
     */
    public static String getPropertiesAsAdoc(Path javaCode) {
        return getProperties(javaCode, ADOC_PROPERTY_FORMAT);
    }

    private static String getProperties(Path javaCode, String propertyPattern) {
        logger.debug("getProperties {}", javaCode);
        CompilationUnit compilationUnit = getCompilationUnit(javaCode);
        StringBuilder toPopulate = new StringBuilder();
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(clsOrInt -> populateProperties(clsOrInt, toPopulate, propertyPattern));
        return toPopulate.toString();
    }

    /*
        Default access modifier for testing purpose
    */
    static CompilationUnit getCompilationUnit(Path javaCode) {
        logger.debug("getCompilationUnit {}", javaCode);
        try {
            return StaticJavaParser.parse(javaCode);
        } catch (Exception e) {
            logger.error("Error while parsing file: {}", javaCode);
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /*
        Default access modifier for testing purpose
     */
    static Collection<FieldDeclaration> getApplicationPropertyFields(ClassOrInterfaceDeclaration node) {
        logger.debug("getApplicationPropertyFields {}", node);
        return node.findAll(FieldDeclaration.class).stream()
                .filter(FieldDeclaration::isStatic)
                .filter(FieldDeclaration::isPublic)
                .filter(FieldDeclaration::isFinal)
                .filter(fieldDeclaration -> fieldDeclaration.getVariables().size() == 1 &&
                        fieldDeclaration.getVariable(0).getInitializer().isPresent() &&
                        fieldDeclaration.getVariable(0).getInitializer().get() instanceof StringLiteralExpr)
                .filter(fieldDeclaration -> fieldDeclaration.getJavadoc().isPresent())
                .toList();
    }

    /*
    Default access modifier for testing purpose
 */
    static Map<AnnotationExpr, Node> getApplicationPropertyAnnotations(ClassOrInterfaceDeclaration node) {
        logger.debug("getApplicationPropertyAnnotations {}", node);
        Map<AnnotationExpr, Node> toReturn = new java.util.HashMap<>();
        toReturn.putAll(getApplicationPropertyAnnotationsFromMethods(node));
        toReturn.putAll(getApplicationPropertyAnnotationsFromClass(node));
        return toReturn;
    }

    static Map<AnnotationExpr, Node> getApplicationPropertyAnnotationsFromMethods(Node node) {
        return node.findAll(MethodDeclaration.class).stream()
                .filter(methodDeclaration -> methodDeclaration.getAnnotations().stream()
                        .anyMatch(annotationExpr -> ANNOTATION_NAME_MAP.containsKey(annotationExpr.getNameAsString())))
                .collect(Collectors.toMap(
                        methodDeclaration -> methodDeclaration.getAnnotations().stream()
                                .filter(annotationExpr -> ANNOTATION_NAME_MAP.containsKey(annotationExpr.getNameAsString()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No matching annotation: " + methodDeclaration)),
                        methodDeclaration -> methodDeclaration
                ));
    }

    static Map<AnnotationExpr, Node> getApplicationPropertyAnnotationsFromClass(Node node) {
        return node.findAll(ClassOrInterfaceDeclaration.class).stream()
                .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getAnnotations().stream()
                        .anyMatch(annotationExpr -> ANNOTATION_NAME_MAP.containsKey(annotationExpr.getNameAsString())))
                .collect(Collectors.toMap(
                        classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getAnnotations().stream()
                                .filter(annotationExpr -> ANNOTATION_NAME_MAP.containsKey(annotationExpr.getNameAsString()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("No matching annotation: " + classOrInterfaceDeclaration)),
                        classOrInterfaceDeclaration -> classOrInterfaceDeclaration
                ));
    }

    private static void populateProperties(ClassOrInterfaceDeclaration node, StringBuilder toPopulate, String propertyPattern) {
        logger.debug("populateProperties {} {}", node, toPopulate);
        getApplicationPropertyFields(node)
                .forEach(fldDclr -> populateProperties(fldDclr, toPopulate, propertyPattern));
        getApplicationPropertyAnnotations(node)
                .forEach((annotation, mappedNode) -> populateProperties(annotation, mappedNode, toPopulate, propertyPattern));
    }

    private static void populateProperties(FieldDeclaration field, StringBuilder toPopulate, String propertyPattern) {
        logger.debug("populateProperties {} {}", field, toPopulate);

        var name = field.getVariable(0).getInitializer().orElseThrow(() -> new IllegalArgumentException("No Initializer: " + field)).asStringLiteralExpr();
        var type = "";
        var defaultValue = "";

        // We need to get the information from JavaDoc, if there isn't any Java, we'll add the best we can
        var javadoc = field.getJavadocComment().orElseThrow(() -> new IllegalArgumentException("No JavadocComment: " + field)).parse();
        var desc = javadoc.getDescription().toText();

        // Start the really brittle parsing of a JavaDoc comment like:
        // (integer) number of decision topic partitions; default to 1
        var pattern = Pattern.compile("^\\((?<type>\\w+)\\)\\s+(?<desc>[\\w\\s/]+)(?<defaultValue>.*)?$");
        var matcher = pattern.matcher(desc);

        if (matcher.matches()) {
            type = matcher.group("type");
            desc = matcher.group("desc");

            // Take the default value, and strip out the first part we don't need
            defaultValue = matcher.group("defaultValue").replace("; default to ", "");
        }

        // If there are JavaDoc tags, use them instead of the regex
        if (!javadoc.getBlockTags().isEmpty()) {
            for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                if (tag.getTagName().equals("type")) {
                    type = tag.getContent().getElements().get(0).toText(); // Should only be one element
                }
                if (tag.getTagName().equals("default")) {
                    defaultValue = tag.getContent().getElements().get(0).toText(); // Should only be one element
                }
            }
        }

        toPopulate.append(String.format(propertyPattern, name.asString(), desc, type, defaultValue)).append(System.lineSeparator());
    }

    private static void populateProperties(AnnotationExpr annotation, Node node, StringBuilder toPopulate, String propertyPattern) {
        logger.debug("populateProperties {} {}", annotation, toPopulate);

        MemberValuePair memberValuePair = ((NormalAnnotationExpr) annotation)
                .getPairs().stream().filter(Objects::nonNull)
                .filter(pair -> ANNOTATION_NAME_MAP.get(annotation.getNameAsString()).equals(pair.getNameAsString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching pair: " + annotation));

        var value = memberValuePair.getValue();
        var name = "";
        if (value instanceof StringLiteralExpr) {
            name = value.asStringLiteralExpr().asString();
        } else if (value instanceof FieldAccessExpr) {
            name = String.format("(%s)",((FieldAccessExpr) value).toString());
        }
        var type = "";
        var defaultValue = "";
        var desc = getDescription(node);

//        // We need to get the information from JavaDoc, if there isn't any Java, we'll add the best we can
//        var javadoc = annotation.getJavadocComment().orElseThrow(() -> new IllegalArgumentException("No JavadocComment: " + annotation)).parse();
//        var desc = javadoc.getDescription().toText();

//        var name = annotation.getVariable(0).getInitializer().orElseThrow(() -> new IllegalArgumentException("No Initializer: " + annotation)).asStringLiteralExpr();
//        var type = "";
//        var defaultValue = "";
//
//        // We need to get the information from JavaDoc, if there isn't any Java, we'll add the best we can
//        var javadoc = annotation.getJavadocComment().orElseThrow(() -> new IllegalArgumentException("No JavadocComment: " + annotation)).parse();
//        var desc = javadoc.getDescription().toText();
//
//        // Start the really brittle parsing of a JavaDoc comment like:
//        // (integer) number of decision topic partitions; default to 1
//        var pattern = Pattern.compile("^\\((?<type>\\w+)\\)\\s+(?<desc>[\\w\\s/]+)(?<defaultValue>.*)?$");
//        var matcher = pattern.matcher(desc);
//
//        if (matcher.matches()) {
//            type = matcher.group("type");
//            desc = matcher.group("desc");
//
//            // Take the default value, and strip out the first part we don't need
//            defaultValue = matcher.group("defaultValue").replace("; default to ", "");
//        }
//
//        // If there are JavaDoc tags, use them instead of the regex
//        if (!javadoc.getBlockTags().isEmpty()) {
//            for (JavadocBlockTag tag : javadoc.getBlockTags()) {
//                if (tag.getTagName().equals("type")) {
//                    type = tag.getContent().getElements().get(0).toText(); // Should only be one element
//                }
//                if (tag.getTagName().equals("default")) {
//                    defaultValue = tag.getContent().getElements().get(0).toText(); // Should only be one element
//                }
//            }
//        }
//
        toPopulate.append(String.format(propertyPattern, name, desc, type, defaultValue)).append(System.lineSeparator());
    }

    private static String getDescription(Node node) {
        String toReturn = "";
        if (node instanceof MethodDeclaration methodDeclaration) {
            toReturn = "Property used to instantiate " + methodDeclaration.getType();
        } else if (node instanceof ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
            toReturn = "Property used to instantiate " + classOrInterfaceDeclaration.getName();
        }
        return toReturn;
    }
}   