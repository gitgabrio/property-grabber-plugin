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
package org.bamoe.utils.property_grabber.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.javadoc.JavadocBlockTag;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bamoe.utils.property_grabber.beans.AnnotationClassBean;
import org.bamoe.utils.property_grabber.beans.AnnotationFieldBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bamoe.utils.property_grabber.utils.CommonHelper.KIE_PROPERTY_ANNOTATION;

public class GrabberHelper {

    private static final Logger logger = LoggerFactory.getLogger(GrabberHelper.class);

    private static final String TXT_PROPERTY_FORMAT = "Config Name: %s | Description: %s | Type: %s | Default: %s";
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

    /**
     * Map of the annotation name and its "name" attribute.
     */
    static final Map<String, AnnotationFieldBean> ANNOTATION_NAME_MAP = Map.of(
            "IfBuildProperty", AnnotationFieldBean.builder("IfBuildProperty").withPropertyNameAttribute("name").withDefaultValue("false").withActivationAttribute("stringValue").build(),
            "UnlessBuildProperty", AnnotationFieldBean.builder("UnlessBuildProperty").withPropertyNameAttribute("name").withDefaultValue("false").withDeactivationAttribute("stringValue").build(),
            "ConfigProperty", AnnotationFieldBean.builder("ConfigProperty").withPropertyNameAttribute("name").build(),
            "ConditionalOnProperty", AnnotationFieldBean.builder("ConditionalOnProperty").withPropertyNameAttribute("value").withDefaultValue("true").withActivationAttribute("havingValue").build(),
            "Value", AnnotationFieldBean.builder("Value").build());

    /**
     * Map of the configuration class name and its "type" attribute.
     */
    private static final Map<String, AnnotationClassBean> ANNOTATION_TYPE_MAP = Map.of(
            "ConfigurationProperties", AnnotationClassBean.builder("ConfigurationProperties")
                    .withPropertyNameAnnotation(AnnotationFieldBean.builder("Name").withPropertyNameAttribute("value").build())
                    .withPrefixAttribute("prefix").build(),
            "ConfigMapping", AnnotationClassBean.builder("ConfigMapping")
                    .withPropertyNameAnnotation(AnnotationFieldBean.builder("WithName").build())
                    .withParentNameAnnotation(AnnotationFieldBean.builder("WithParentName").build())
                    .withDefaultValueAnnotation(AnnotationFieldBean.builder("WithDefault").build())
                    .withPrefixAttribute("prefix").build());

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

    /*
        Default access modifier for testing purpose
     */
    static Collection<FieldDeclaration> getNotKieAnnotatedApplicationPropertyFields(ClassOrInterfaceDeclaration node) {
        logger.debug("getNotKieAnnotatedApplicationPropertyFields {}", node.getName());
        return node.findAll(FieldDeclaration.class).stream()
                .filter(GrabberHelper::isValidPropertyField)
                .filter(field -> !isKieAnnotated(field))
                .toList();
    }

    /*
        Default access modifier for testing purpose
    */
    static Collection<FieldDeclaration> getKieAnnotatedApplicationPropertyFields(ClassOrInterfaceDeclaration node) {
        logger.debug("getKieAnnotatedApplicationPropertyFields {}", node.getName());
        List<FieldDeclaration> toReturn = node.findAll(FieldDeclaration.class)
                .stream()
                .filter(fieldDeclaration -> getFilteredAnnotation(fieldDeclaration, KIE_PROPERTY_ANNOTATION).isPresent())
                .toList();
        for (FieldDeclaration fieldDeclaration : toReturn) {
            if (!isValidPropertyField(fieldDeclaration)) {
                throw new IllegalArgumentException(
                        "All fields annotated with @" + KIE_PROPERTY_ANNOTATION + " should be static, public, final, have a single variable with an initializer of type StringLiteralExpr and a JavaDoc. Offending field: " + fieldDeclaration);

            }
        }
        return toReturn;
    }

    /*
        Default access modifier for testing purpose
    */
    static boolean isValidPropertyField(FieldDeclaration fieldDeclaration) {
        return fieldDeclaration.isStatic() &&
                fieldDeclaration.isPublic() &&
                fieldDeclaration.isFinal() &&
                fieldDeclaration.getVariables().size() == 1 &&
                fieldDeclaration.getVariable(0).getInitializer().isPresent() &&
                fieldDeclaration.getVariable(0).getInitializer().get() instanceof StringLiteralExpr &&
                fieldDeclaration.getJavadoc().isPresent();
    }

    /*
        Default access modifier for testing purpose
    */
    static Optional<AnnotationExpr> getConfigClassAnnotation(ClassOrInterfaceDeclaration node) {
        return node.getAnnotations()
                .stream()
                .filter(annotationExpr -> ANNOTATION_TYPE_MAP.containsKey(annotationExpr.getNameAsString()))
                .findFirst();
    }

    /*
        Default access modifier for testing purpose
    */
    static List<MethodDeclaration> getApplicationPropertyFieldsFromConfigClass(ClassOrInterfaceDeclaration node) {
        return node.getMethods()
                .stream()
                .filter(methodDeclaration -> !Objects.equals(methodDeclaration.getType(), new VoidType()) && (methodDeclaration.isPublic() || node.isInterface()))
                .toList();
    }

    /*
      Default access modifier for testing purpose
    */
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

    /*
        Default access modifier for testing purpose
    */
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

    /*
        Default access modifier for testing purpose
    */
    static Optional<String> getAnnotatedValue(MethodDeclaration methodDeclaration, String annotationName, String annotationProperty) {
        if (annotationName == null) {
            return Optional.empty();
        }
        Optional<AnnotationExpr> annotation = getFilteredAnnotation(methodDeclaration, annotationName);
        return annotation.map(annt -> getAnnotatedValue(annt, annotationProperty));
    }

    /*
        Default access modifier for testing purpose
    */
    static String getAnnotatedValue(AnnotationExpr annotationExpr, String annotationProperty) {
        if (annotationExpr.isNormalAnnotationExpr()) {
            return getAnnotatedValue(annotationExpr.asNormalAnnotationExpr(), annotationProperty);
        } else if (annotationExpr.isSingleMemberAnnotationExpr()) {
            return getAnnotatedValue(annotationExpr.asSingleMemberAnnotationExpr(), annotationProperty);
        } else {
            throw new IllegalArgumentException("Unsupported annotation type: " + annotationExpr);
        }
    }

    private static boolean isKieAnnotated(FieldDeclaration fieldDeclaration) {
        return fieldDeclaration.isAnnotationPresent(KIE_PROPERTY_ANNOTATION);
    }

    private static Optional<AnnotationExpr> getFilteredAnnotation(BodyDeclaration<?> bodyDeclaration, String annotationName) {
        return bodyDeclaration.getAnnotations()
                .stream()
                .filter(annotationExpr -> annotationExpr.getNameAsString().equals(annotationName))
                .findFirst();
    }

    private static String getAnnotatedValue(NormalAnnotationExpr annotationExpr, String annotationProperty) {
        if (annotationProperty == null) {
            throw new IllegalArgumentException("annotationProperty cannot be null");
        }
        if (annotationProperty.isBlank()) {
            return null;
        }
        MemberValuePair memberValuePair = annotationExpr.getPairs().stream()
                .filter(pair -> pair.getNameAsString().equals(annotationProperty))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No matching pair: " + annotationExpr + " for annotationProperty: " + annotationProperty));

        var value = memberValuePair.getValue();
        var toReturn = "";
        if (value instanceof StringLiteralExpr) {
            toReturn = value.asStringLiteralExpr().asString();
        } else if (value instanceof FieldAccessExpr) {
            toReturn = String.format("(%s)", ((FieldAccessExpr) value).toString());
        }
        return toReturn;
    }

    private static String getAnnotatedValue(SingleMemberAnnotationExpr annotationExpr, String annotationProperty) {
        if (annotationProperty != null && !annotationProperty.isBlank()) {
            throw new IllegalArgumentException("annotationProperty must be null");
        }
        return annotationExpr.getMemberValue().asStringLiteralExpr().asString();
    }

    private static Map<AnnotationExpr, Node> getApplicationPropertyAnnotations(ClassOrInterfaceDeclaration node) {
        logger.debug("getApplicationPropertyAnnotations {}", node.getName());
        Map<AnnotationExpr, Node> toReturn = new java.util.HashMap<>();
        toReturn.putAll(getApplicationPropertyAnnotationsFromMethods(node));
        toReturn.putAll(getApplicationPropertyAnnotationsFromClass(node));
        return toReturn;
    }

    private static String getProperties(Path javaCode, String propertyPattern) {
        logger.debug("getProperties {}", javaCode);
        CompilationUnit compilationUnit = CommonHelper.getCompilationUnit(javaCode);
        StringBuilder toPopulate = new StringBuilder();
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                .forEach(clsOrInt -> populateProperties(clsOrInt, toPopulate, propertyPattern));
        return toPopulate.toString();
    }

    private static void populateProperties(ClassOrInterfaceDeclaration node, StringBuilder toPopulate, String propertyPattern) {
        logger.debug("populateProperties {} {}", node.getName(), toPopulate);
        getNotKieAnnotatedApplicationPropertyFields(node)
                .forEach(fldDclr -> populatePropertiesFromRawClass(fldDclr, toPopulate, propertyPattern));
        getKieAnnotatedApplicationPropertyFields(node)
                .forEach(fldDclr -> populatePropertiesFromRawClass(fldDclr, toPopulate, propertyPattern));
        Optional<AnnotationExpr> configClassAnnotation = getConfigClassAnnotation(node);
        if (configClassAnnotation.isPresent()) {
            AnnotationExpr annotationExpr = configClassAnnotation.get();
            AnnotationClassBean annotationClassBean = ANNOTATION_TYPE_MAP.get(annotationExpr.getNameAsString());
            String prefix = getAnnotatedValue(configClassAnnotation.get(), annotationClassBean.getPrefixAttribute());
            List<MethodDeclaration> configClassMethods = getApplicationPropertyFieldsFromConfigClass(node);
            configClassMethods.forEach(methodDeclaration ->
                    populatePropertiesFromConfigurationClass(methodDeclaration, toPopulate, annotationClassBean, prefix, propertyPattern));
        }
        getApplicationPropertyAnnotations(node)
                .forEach((annotation, mappedNode) -> populateProperties(annotation, mappedNode, toPopulate, propertyPattern));
    }

    private static void populatePropertiesFromRawClass(FieldDeclaration field, StringBuilder toPopulate, String propertyPattern) {
        logger.trace("populatePropertiesFromRawClass {} {}", field, toPopulate);

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

    private static void populatePropertiesFromConfigurationClass(MethodDeclaration methodDeclaration, StringBuilder toPopulate, AnnotationClassBean annotationClassBean, String prefix,
            String propertyPattern) {
        logger.trace("populatePropertiesFromConfigurationClass {} {}", methodDeclaration, toPopulate);
        Optional<String> annotatedName = Optional.empty();
        AnnotationFieldBean nameAnnotationFieldsBean = annotationClassBean.getPropertyNameAnnotation();
        if (nameAnnotationFieldsBean != null) {
            annotatedName = getAnnotatedValue(methodDeclaration, nameAnnotationFieldsBean.getAnnotationName(), nameAnnotationFieldsBean.getPropertyNameAttribute());
        }
        var name = annotatedName.orElseGet(methodDeclaration::getNameAsString);
        var prefixedName = prefix != null ? prefix + "." + name : name;
        var type = "";

        Optional<String> annotatedDefaultValue = Optional.empty();
        AnnotationFieldBean defaultValueAnnotationFieldsBean = annotationClassBean.getDefaultValueAnnotation();
        if (defaultValueAnnotationFieldsBean != null) {
            annotatedDefaultValue = getAnnotatedValue(methodDeclaration, defaultValueAnnotationFieldsBean.getAnnotationName(), defaultValueAnnotationFieldsBean.getDefaultValueAttribute());
        }
        var defaultValue = annotatedDefaultValue.orElse("");
        // We need to get the information from JavaDoc, if there isn't any Java, we'll add the best we can
        var javadoc = methodDeclaration.getJavadocComment();
        var desc = javadoc.isPresent() ? javadoc.get().parse().getDescription().toText() : "";
        toPopulate.append(String.format(propertyPattern, prefixedName, desc, type, defaultValue)).append(System.lineSeparator());
    }

    private static void populateProperties(AnnotationExpr annotation, Node node, StringBuilder toPopulate, String propertyPattern) {
        logger.trace("populateProperties {} {}", annotation, toPopulate);
        if (!ANNOTATION_NAME_MAP.containsKey(annotation.getNameAsString())) {
            logger.trace("Ignored annotation: {}", annotation);
            return;
        }
        AnnotationFieldBean annotationFieldsBean = ANNOTATION_NAME_MAP.get(annotation.getNameAsString());
        var name = getAnnotatedValue(annotation, annotationFieldsBean.getPropertyNameAttribute());
        String defaultValue = null;
        if (name.contains("{") && name.contains("}")) {
            name = name.substring(name.indexOf("{") + 1, name.indexOf("}"));
            if (name.contains(":")) {
                defaultValue = name.substring(name.indexOf(":") + 1);
                name = name.substring(0, name.indexOf(":"));
            }
        }
        var type = "";
        if ((defaultValue == null || defaultValue.isBlank()) && annotationFieldsBean.getDefaultValueAttribute() != null) {
            defaultValue = getAnnotatedValue(annotation, annotationFieldsBean.getDefaultValueAttribute());
        }
        if (defaultValue == null || defaultValue.isBlank()) {
            defaultValue = annotationFieldsBean.getDefaultValue() != null ? annotationFieldsBean.getDefaultValue() : "";
        }
        var desc = getDescription(annotationFieldsBean, node);

        toPopulate.append(String.format(propertyPattern, name, desc, type, defaultValue)).append(System.lineSeparator());
    }

    private static String getDescription(AnnotationFieldBean annotationFieldsBean, Node node) {
        String toReturn = "";
        Optional<String> propertyActivationValue = Optional.empty();
        Optional<String> propertyDeactivationValue = Optional.empty();
        if (node instanceof MethodDeclaration methodDeclaration) {
            toReturn = "Property used to instantiate " + methodDeclaration.getType();
            if (annotationFieldsBean.getActivationAttribute() != null) {
                propertyActivationValue = getAnnotatedValue(methodDeclaration, annotationFieldsBean.getAnnotationName(), annotationFieldsBean.getActivationAttribute());
            }
            if (annotationFieldsBean.getDeactivationAttribute() != null) {
                propertyDeactivationValue = getAnnotatedValue(methodDeclaration, annotationFieldsBean.getAnnotationName(), annotationFieldsBean.getDeactivationAttribute());
            }
        } else if (node instanceof ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
            toReturn = "Property used to instantiate " + classOrInterfaceDeclaration.getName();
        }

        if (propertyActivationValue.isPresent() && !propertyActivationValue.get().isBlank()) {
            toReturn += " (only active when \"" + propertyActivationValue.get() + "\")";
        }
        if (propertyDeactivationValue.isPresent() && !propertyDeactivationValue.get().isBlank()) {
            toReturn += " (only active when not \"" + propertyDeactivationValue.get() + "\")";
        }
        return toReturn;
    }

}