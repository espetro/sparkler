Sparkler development requires

- Java JDK 1.8 or newer
- Gradle
- A Spark compiled distribution

The project is currently placed inside Spark folder. As it oriented towards non-technical users, it runs the scripts from relative paths; thus, two configurations are used meanwhile to get the relative `/spark/bin` path

- On production, it gets `File.getAbsolutePath()`
- On development, it gets `File(System.getProperty("user.dir")).getParent()`



*Eclipse IDE*

```java

```



As the project only interfaces Spark, it should give the least overhead possible. Suggestions and Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.