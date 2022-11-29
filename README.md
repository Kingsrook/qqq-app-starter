# QQQ App Starter
This project is meant to serve as an example, or a starter, for how to build
a QQQ application.

## Quick class overview
* `com.kingsrook.qqq.starterapp.StarterAppMetaDataProvider`
   * This class's job is to define the QQQ MetaData used by the application.
   * e.g., the backend(s) used by the application; what authentication type
   is used; what table(s) exist; how tables are arranged as apps, etc.

* `com.kingsrook.qqq.starterapp.StarterAppJavalinServer`
   * This class starts a QQQ Javalin (e.g., lightweight HTTP) server.
   * It uses the StarterAppMetaDataProvider to define the QInstance, then
   starts the javalin server.

* `com.kingsrook.qqq.starterapp.StarterAppCli`
   * This class allows you to run a QQQ command-line interface (CLI).
   * It uses the StarterAppMetaDataProvider to define the QInstance, then
   interprets your command line to execute actions against that instance.
   * *Note that this version of a QQQ CLI makes direct connections to the
   backends defined in the QInstance - e.g., jdbc network connections for
   RDBMS backends - this may have networking and/or security implications...*

## material-dashboard
* The qqq-frontend-material-dashboard module is inclued as a git submodule
  under this project, located at `src/main/ui`.
* The `pom.xml` uses the `frontend-maven-plugin` plugin to compile that module
  under the `target` path, via either `mvn generate-sources
   -Dfrontend.phase=generate-sources` or `mvn package`.

