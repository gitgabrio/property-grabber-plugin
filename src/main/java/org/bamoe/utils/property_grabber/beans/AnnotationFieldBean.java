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

public final class AnnotationFieldBean {
    private final String annotationName;
    private String propertyNameAttribute;
    private String propertyTypeAttribute;
    private String defaultValue;
    private String defaultValueAttribute;
    private String allowedValuesAttribute;
    private String activationAttribute;
    private String deactivationAttribute;
    private String prefixAttribute;

    public static Constructor builder(String annotationName) {
        return new Constructor(annotationName);
    }

    private AnnotationFieldBean(String annotationName) {
        this.annotationName = annotationName;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public String getPropertyNameAttribute() {
        return propertyNameAttribute;
    }

    public String getPropertyTypeAttribute() {
        return propertyTypeAttribute;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueAttribute() {
        return defaultValueAttribute;
    }

    public String getAllowedValuesAttribute() {
        return allowedValuesAttribute;
    }

    public String getActivationAttribute() {
        return activationAttribute;
    }

    public String getDeactivationAttribute() {
        return deactivationAttribute;
    }

    public String getPrefixAttribute() {
        return prefixAttribute;
    }

    public static class Constructor {

        private final AnnotationFieldBean toReturn;

        private Constructor(String annotationName) {
            toReturn = new AnnotationFieldBean(annotationName);
        }

        /**
         * The attribute pointing to the property name, if any
         * @param propertyNameAttribute
         * @return
         */
        public Constructor withPropertyNameAttribute(String propertyNameAttribute) {
            toReturn.propertyNameAttribute = propertyNameAttribute;
            return this;
        }

        /**
         * The attribute pointing to the property type, if any
         * @param propertyTypeAttribute
         * @return
         */
        public Constructor withPropertyTypeAttribute(String propertyTypeAttribute) {
            toReturn.propertyTypeAttribute = propertyTypeAttribute;
            return this;
        }

        /**
         * The "hardcoded" default value, depending on tha annotation itself (e.g. for IfBuildProperty, UnlessBuildProperty)
         * @param defaultValue
         * @return
         */
        public Constructor withDefaultValue(String defaultValue) {
            toReturn.defaultValue = defaultValue;
            return this;
        }

        /**
         * The attribute pointing to the default value
         * @param defaultValueAttribute
         * @return
         */
        public Constructor withDefaultValueAttribute(String defaultValueAttribute) {
            toReturn.defaultValueAttribute = defaultValueAttribute;
            return this;
        }

        /**
         * The attribute pointing to the allowed values, if any
         * @param allowedValues
         * @return
         */
        public Constructor withAllowedValues(String allowedValues) {
            toReturn.allowedValuesAttribute = allowedValues;
            return this;
        }

        /**
         * The attribute pointing to the activation attribute, if any
         * @param activationAttribute
         * @return
         */
        public Constructor withActivationAttribute(String activationAttribute) {
            toReturn.activationAttribute = activationAttribute;
            return this;
        }

        /**
         * The attribute pointing to the deactivation attribute, if any
         * @param deactivationAttribute
         * @return
         */
        public Constructor withDeactivationAttribute(String deactivationAttribute) {
            toReturn.deactivationAttribute = deactivationAttribute;
            return this;
        }

        /**
         * The attribute pointing to the property name prefixe, if any
         * @param prefixAttribute
         * @return
         */
        public Constructor withPrefixAttribute(String prefixAttribute) {
            toReturn.prefixAttribute = prefixAttribute;
            return this;
        }

        public AnnotationFieldBean build() {
            return toReturn;
        }
    }
}   