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
// Fake import to test the parser
import java.lang.String;

public class BasicJavaClassWithFields {

    /**
     * (boolean) generate java classes to support strongly typed input (default false)
     */
    public static String STRONGLY_TYPED_CONFIGURATION_KEY = "kogito.decisions.stronglytyped";

    /**
     * model validation strategy; possible values: ENABLED, DISABLED, IGNORE; (default ENABLED)
     */
    public static String VALIDATION_CONFIGURATION_KEY = "kogito.decisions.validation";

    /**
     * (string) kafka bootstrap server address
     */
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    public static final String KOGITO_ADDON_TRACING_DECISION_KAFKA_BOOTSTRAPADDRESS = "kogito.addon.tracing.decision.kafka.bootstrapAddress";

    /**
     * (string) name of the decision topic; default to kogito-tracing-decision
     * @type String
     * @default Value of `kogito-tracing-decision`
     */
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    public static final String KOGITO_ADDON_TRACING_DECISION_KAFKA_TOPIC_NAME = "kogito.addon.tracing.decision.kafka.topic.name";

    /**
     * (integer) number of decision topic partitions; default to 1
     */
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    public static final String KOGITO_ADDON_TRACING_DECISION_KAFKA_TOPIC_PARTITIONS = "kogito.addon.tracing.decision.kafka.topic.partitions";

    /**
     * (integer) number of decision topic replication factor; default to 1
     */
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    public static final String KOGITO_ADDON_TRACING_DECISION_KAFKA_TOPIC_REPLICATION_FACTOR = "kogito.addon.tracing.decision.kafka.topic.replicationFactor";

    /**
     * (boolean) enable/disable asynchronous collection of decision events; default to true
     */
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    @KieProperty()
    public static final String KOGITO_ADDON_TRACING_DECISION_ASYNC_ENABLED = "kogito.addon.tracing.decision.asyncEnabled";

    public static final String WITHOUT_JAVA_DOC = "kogito.addon.tracing.decision.asyncEnabled";

    /**
     * This is not an application property
     */
    public static final String WITHOUT_INITIALIZER;

    static {
        WITHOUT_INITIALIZER = "initializer";
    }

    @IfBuildProperty(name = "quarkus.kogito.data-index.graphql.ui.always-include", stringValue = "true")
    public String ifBuildProperty() {
        return "anything";
    }

    @IfBuildProperty(name = Constants.MONITORING_RULE_USE_DEFAULT, stringValue = "true", enableIfMissing = true)
    public String ifBuildPropertyFromConstant() {
        return "anything";
    }

    @UnlessBuildProperty(name = "kogito.data-index.blocking", stringValue = "true")
    public String unlessBuildProperty() {
        return "anything";
    }

    @ConfigProperty(name = "kogito.jobs-service.url")
    public String configProperty() {
        return "anything";
    }
}
