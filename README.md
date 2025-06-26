# Thirukkural
Project to translate and create Thirukkural epub book in multiple languages

### Required Software and Tools
* Java Version: 21 (Execute **_java -version_** in command line after installation)
* Integrated Development Environment: Any version IntelliJ Idea or Eclipse or VS Code

### Commands

#### Run Maven build to download dependencies and generate artefacts

    ./mvnw clean install

#### Transform raw json to structured json

    ./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--task=transform_raw"
