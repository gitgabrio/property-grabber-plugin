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

import java.nio.file.Path;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserHelper {

    private static final Logger logger = LoggerFactory.getLogger(ParserHelper.class);

    private static final String PROPERTY_FORMAT = "%s -> %s";

    static {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
    }

    public static String getProperties(Path javaCode) {
        logger.debug("getProperties {}", javaCode);
        try {
            CompilationUnit compilationUnit = StaticJavaParser.parse(javaCode);
            StringBuilder toPopulate = new StringBuilder();
            compilationUnit.findAll(ClassOrInterfaceDeclaration.class)
                    .forEach(clsOrInt -> populateProperties(clsOrInt, toPopulate));
            return toPopulate.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static void populateProperties(ClassOrInterfaceDeclaration node, StringBuilder toPopulate) {
        logger.debug("populateProperties {} {}", node, toPopulate);
        node.findAll(FieldDeclaration.class).stream()
                .filter(FieldDeclaration::isStatic)
                .filter(FieldDeclaration::isPublic)
                .filter(FieldDeclaration::isFinal)
                .filter(fieldDeclaration -> fieldDeclaration.getVariables().size() == 1 &&
                        fieldDeclaration.getVariable(0).getInitializer().isPresent() &&
                        fieldDeclaration.getVariable(0).getInitializer().get() instanceof StringLiteralExpr)
                .filter(fieldDeclaration -> fieldDeclaration.getComment().isPresent())
                        .forEach(fldDclr -> populateProperties(fldDclr, toPopulate));


    }

    private static void populateProperties(FieldDeclaration field, StringBuilder toPopulate) {
        logger.debug("populateProperties {} {}", field, toPopulate);
        toPopulate.append("\r\n");
        toPopulate.append(String.format(PROPERTY_FORMAT,  field.getVariable(0).getInitializer().get().asStringLiteralExpr().getValue(),
                field.getComment().get().getContent().trim().replace("*", "")));
    }
}   