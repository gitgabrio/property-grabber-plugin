# property-grabber-plugin

The two mojos (PropertyAnnotatorMojo and PropertyGrabberMojo) extends AbstractPropertyMojo, from which inherits the common methods.
Then, they act as simple "gateways", where most of the behavior is implemented inside AnnotatorHelper and GrabberHelper, respectively.

Inside GrabberHelper, ANNOTATION_NAME_MAP and ANNOTATION_TYPE_MAP are used to identify and parse CDI-annotated properties
The not-CDI-annotated properties are identified by the following criteria:

1. inside a concrete class
2. public, final, static property declaration
3. property initialized
4. javadoc present

See GrabberHelper.getApplicationPropertyFields(ClassOrInterfaceDeclaration) 


