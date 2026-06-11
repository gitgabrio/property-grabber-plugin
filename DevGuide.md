# property-grabber-plugin

The two mojos (PropertyAnnotationMojo and PropertyGrabberMojo) extends AbstractPropertyMojo, from which inherits the common methods.
Then, they act as simple "gateways", where most of the behavior is implemented inside ParserHelper.

ANNOTATION_NAME_MAP and ANNOTATION_TYPE_MAP are used to identify and parse CDI-annotated properties
The not-CDI-annotated properties are identified by the following criteria:

1. inside a concrete class
2. public, final, static property declaration
3. property initialized
4. javadoc present

See ParserHelper.getApplicationPropertyFields(ClassOrInterfaceDeclaration) 


