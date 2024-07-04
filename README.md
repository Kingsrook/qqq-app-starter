# QQQ App Starter
This project is meant to serve as an example, or a starter, for how to build
a QQQ application.

## Prerequisites
* Required software:
    * Java 17+
    * Maven (tested with 3.8.6)
    * Git
* Maven settings:
  * You will need a github token in your `~/.m2/settings.xml` file, to get 
  the QQQ jars through maven. 
  There are 2 options for generating such a token in github:
    1. github > your photo > Settings > Developer Settings > Personal access 
    tokens > Tokens (classic) > Generate new token > Generate new token 
    (classic) > give it a note, expiration date, and critically, choose the 
    read:packages checkbox.
    2. github > your photo > Settings > Developer Settings > Personal access
    tokens > Fine-grained Tokens > Generate new token > give it a name, 
    expiration date, and critically, choose the "All repositories" option 
    under "Repository access"
  * After you have your token, then make sure you have a `servers` section 
  that looks like the one in following example in your `~/.m2/settings.xml` 
  file (if you don't have that file at all yet, you can create the whole 
  file as shown):

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" 
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
   <servers>
      <server>
         <id>github-qqq-maven-registry</id>
         <username>YOUR-GITHUB-USERNAME</username>
         <password>YOUR-GITHUB-TOKEN</password>
      </server>
   </servers>
</settings>
```

* Database:
    * This starter app is going to try to connect to a MySQL database.  You will specify
      the database's name, hostname, port, etc, in a `.env` file.  The only assumption
      or requirement for the database, is that it have a table named `sample`, with
      an `id INTEGER AUTO_INCREMENT PRIMARY KEY` column, and a `name VARCHAR(100)` column.
        * If you don't have (or want to have) such a table, you can edit the method
        `defineSampleTable` in `StarterAppMetaDataProvider.java`, to specify a different
        sample table name and/or columns.

## First Steps
1. Clone this repo / expand this archive into a new directory where you want to set up your project.
   * For example:
   ```sh
   ~:] cd ~/git/
   ~git:] mkdir my-first-qqq-project
   ~git:] cd my-first-qqq-project
   ~git/my-first-qqq-project:] unzip qqq-app-starter.zip
   ~git/my-first-qqq-project:] ## or:
   ~git/my-first-qqq-project:] git clone git@github.com:Kingsrook/qqq-app-starter.git
   ```
2. Write .env file with settings for a MySQL that you want to connect to:
   ```properties
   RDBMS_VENDOR=mysql
   RDBMS_HOSTNAME=<your rdbms hostname>
   RDBMS_PORT=3306
   RDBMS_DATABASE_NAME=<your rdbms database name>
   RDBMS_USERNAME=<your rdbms username>
   RDBMS_PASSWORD=<your rdbms password>
   
   QQQ_ENV_MATERIAL_UI_LICENSE_KEY=<Optionally, a license key for MaterialUI.  Without this data grids will show a watermark.>
   ```

## In IntelliJ
To open your new project in IntelliJ, as if you actually wanted to work on it:
1. Create a new project from existing sources, opening this project's `pom.xml` file.
2. Open `StarterAppJavalinServer.java`, and Run or Debug it.  
3. Access the dashboard at: http://localhost:8000/

## Project Structure
### Java Class overview
* `com.kingsrook.qqq.starterapp.StarterAppMetaDataProvider`
   * This class's job is to define the QQQ MetaData used by the application.
   * e.g., the backend(s) used by the application; what authentication type
     is used; what table(s) and processes exist; how tables and processes are 
     arranged as apps, etc.
   * You'll generally do lots of work here (or in classes used by this class,
     as you choose to organize your project) to define more tables, processes,
     apps, etc.

* `com.kingsrook.qqq.starterapp.StarterAppJavalinServer`
   * This class starts a QQQ Javalin (e.g., lightweight HTTP) server.
   * It uses the StarterAppMetaDataProvider to define the QInstance, then
     starts the javalin server.
   * You won't generally need to touch or change much of anything here (and
     in fact, the qqq team has an open task to make this class even smaller,
     more automatic/light-weight/meta-data driven).

* `com.kingsrook.qqq.starterapp.StarterAppCli`
   * *This class is not functional in the initial published version of qqq-app-starter*
   * This class allows you to run a QQQ command-line interface (CLI).
   * It uses the StarterAppMetaDataProvider to define the QInstance, then
     interprets your command line to execute actions against that instance.
   * *Note that this version of a QQQ CLI makes direct connections to the
     backends defined in the QInstance - e.g., jdbc network connections for
     RDBMS backends - this may have networking and/or security implications...*

## Other (older) Notes
*These notes need review, and come from an earlier build of qqq-app-starter...*

### How to run bare-bones
To get a server up and running ASAP, e.g., from a command line, without an IDE,
here are the basic steps:

1. Build a jar
   ```sh
   mvn package
   ```
2. Run the jar
   ```sh
   java -jar target/qqq-app-starter-0.1-SNAPSHOT.jar
   ```
3. Access the dashboard at: http://localhost:8000/

### How to run via Docker
1. Write a `Dockerfile` such as:
   ```dockerfile
   FROM openjdk:17-alpine
   EXPOSE 8000
   ADD target/qqq-app-starter-0.1-SNAPSHOT.jar app.jar
   ENTRYPOINT ["java", "-Xms1G", "-Xmx1G", "-jar", "app.jar"]
   ```
2. Build the image, ala:
   ```sh
   docker build -t qqq-app-starter .
   ```
3. Run a container from the image, ala:
   ```sh
   docker run qqq-app-starter:latest
   ```
3. Access the dashboard at: http://localhost:8000/

