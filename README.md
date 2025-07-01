# Thirukkural
Project to translate and create Thirukkural epub book in multiple languages

### Required Software and Tools
* Java Version: 21 (Execute **_java -version_** in command line after installation)
* Integrated Development Environment: Any version IntelliJ Idea or Eclipse or VS Code

### Commands

#### Run Maven build to download dependencies and generate artefacts

    ./mvnw clean install

#### Transform raw json to structured json

    ./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--task=transform_raw --input.json=./data/thirukkural_raw.json --output.json=./data/thirukkural_structured.json"

#### Transform structured json to another language

    ./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--task=transform_language --input.json=./data/thirukkural_structured.json --output.json=./data/thirukkural_english.json --source.language=Tamil --target.language=English"
