# property-grabber-plugin
Plugin used to grab and manage properties inside "kie" codebase.

The plugin offers two mojos:

1. property-grabber-plugin:grab
2. property-grabber-plugin:annotate

The "grab" goal will grab all properties from the codebase and dump them in a file, whose format could be specified with the "outputType" property.
The "grab" goal detects:
1. the CDI-annotated properties (e.g. `@IfBuildProperty`, `@Value`  etc.)
2. the properties defined inside CDI-annotated configuration classes (e.g. inside `@ConfigurationProperties`)
3. the no-CDI-annotated properties (detected as public final String properties with a value assigned and a javadoc)

The "annotate" goal will annotate all the no-CDI-annotated properties (at above point 3.) with `@KieProperty`.






