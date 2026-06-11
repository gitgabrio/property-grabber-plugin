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
package org.bamoe.utils.property_grabber.beans;

public final class AnnotationClassBean {
    private final String annotationName;

    /**
     * The <code>AnnotationFieldBean</code> representation of the annotation used to define the property name
     */
    private AnnotationFieldBean propertyNameAnnotation;

    /**
     * The <code>AnnotationFieldBean</code> representation of the annotation used to define the parent name, if any (e.g. Quarkus)
     */
    private AnnotationFieldBean parentNameAnnotation;
    /**
     * The <code>AnnotationFieldBean</code> representation of the annotation used to define the default value
     */
    private AnnotationFieldBean defaultValueAnnotation;

    private String prefixAttribute;

    public static Constructor builder(String annotationName) {
        return new Constructor(annotationName);
    }

    private AnnotationClassBean(String annotationName) {
        this.annotationName = annotationName;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public AnnotationFieldBean getPropertyNameAnnotation() {
        return propertyNameAnnotation;
    }

    public AnnotationFieldBean getDefaultValueAnnotation() {
        return defaultValueAnnotation;
    }

    public String getPrefixAttribute() {
        return prefixAttribute;
    }

    public static class Constructor {

        private final AnnotationClassBean toReturn;

        private Constructor(String annotationName) {
            toReturn = new AnnotationClassBean(annotationName);
        }

        public AnnotationClassBean.Constructor withPropertyNameAnnotation(AnnotationFieldBean propertyNameAnnotation) {
            toReturn.propertyNameAnnotation = propertyNameAnnotation;
            return this;
        }

        public AnnotationClassBean.Constructor withParentNameAnnotation(AnnotationFieldBean parentNameAnnotation) {
            toReturn.parentNameAnnotation = parentNameAnnotation;
            return this;
        }

        public AnnotationClassBean.Constructor withDefaultValueAnnotation(AnnotationFieldBean defaultValueAnnotation) {
            toReturn.defaultValueAnnotation = defaultValueAnnotation;
            return this;
        }

        public AnnotationClassBean.Constructor withPrefixAttribute(String prefixAttribute) {
            toReturn.prefixAttribute = prefixAttribute;
            return this;
        }

        public AnnotationClassBean build() {
            return toReturn;
        }
    }

}