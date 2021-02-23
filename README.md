# PARTICLE TRIEUR #

## Building

- Install Amazon Coretto 8
- Open this repository in IntelliJ IDEA
- Set compiler to Amazon Correto 8
- Install the fixed tensorflow jar to your local maven repository with: (ctrl-ctrl in IntelliJ then paste and run below)

    mvn install:install-file -Dfile=lib/libtensorflow-1.15.0.jar -DgroupId=org.tensorflow -DartifactId=libtensorflow-fixed -Dversion=1.15.0 -Dpackaging=jar

- Build project
- Run project (if no configuration, add an 'Application' target with the main class set to particletrieur.App)
