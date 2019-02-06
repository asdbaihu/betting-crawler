# Betting Crawler Application

## Running Betting Crawler locally
Betting Crawler is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/). You can build a jar file and run it from the command line:


```
git clone https://github.com/kavourdiris/betting-crawler.git
cd betting-crawler
./mvnw package
java -jar target/*.jar
```

You can then access the embedded database here: http://localhost:8080/h2-console using the credentials

```
JDBC URL: jdbc:h2~/betting_crawler
Usename:  sa
Password: 
```


Or you can run it from Maven directly using the Spring Boot Maven plugin. If you do this it will pick up changes that you make in the project immediately (changes to Java source files require a compile as well - most people use an IDE for this):

```
./mvnw spring-boot:run
```

## Screenshots
Sample data created by Betting Crawler. Screenshot from H2 database.

![h2-console-inner-join](https://user-images.githubusercontent.com/23057170/52333886-0182d580-2a07-11e9-8ef7-0129139b581c.png)

## Database configuration

In its default configuration, Betting Crawler uses an embedded database (H2) which gets populated when application starts with data crawled from the web. A similar setup is provided for MySql in case a persistent database configuration is needed.
Note that whenever the database type is changed, the app needs to be run with a different profile: `spring.profiles.active=mysql` for MySql.

## Working with Betting Crawler in your IDE

### Prerequisites
The following items should be installed in your system:
* Java 8 or newer.
* git command line tool (https://help.github.com/articles/set-up-git)
* Your prefered IDE 
  * Eclipse with the m2e plugin. Note: when m2e is available, there is an m2 icon in Help -> About dialog. If m2e is not there, just follow the install process here: http://www.eclipse.org/m2e/
  * [Spring Tools Suite](https://spring.io/tools) (STS)
* IntelliJ IDEA

### Steps:

1) On the command line
```
git clone https://github.com/kavourdiris/betting-crawler.git
```
2) Inside Eclipse or STS
```
File -> Import -> Maven -> Existing Maven project
```

Then either build on the command line `./mvnw generate-resources` or using the Eclipse launcher (right click on project and `Run As -> Maven install`) to generate the css. Run the application main method by right clicking on it and choosing `Run As -> Java Application`.

3) Inside IntelliJ IDEA

In the main menu, choose `File -> Open` and select the Betting Crawler [pom.xml](pom.xml). Click on the `Open` button.

A run configuration named `StoiximanApp` should have been created for you if you're using a recent Ultimate
version. Otherwise, run the application by right clicking on the `StoiximanApp` main class and choosing
`Run 'StoiximanApp'`.

4) Navigate to Betting Crawler database

Visit [http://localhost:8080](http://localhost:8080:h2-console) in your browser.


## License

The Spring Betting Crawler application is released under version 2.0 of the [MIT License](https://opensource.org/licenses/MIT).
