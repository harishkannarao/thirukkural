# Thirukkural
Project to translate and create Thirukkural epub book in multiple languages

### Required Software and Tools
* Java Version: 21 (Execute **_java -version_** in command line after installation)
* Integrated Development Environment: Any version IntelliJ Idea or Eclipse or VS Code

### Commands

#### Run Maven build to download dependencies and generate artefacts

    ./mvnw clean install

#### Transform raw json to base json

    ./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--task=transform_raw --input.json=./data/thirukkural_raw.json --output.json=./data/thirukkural_Base.json"

#### Transform base json to another language using Local Ollama AI

    ollama pull llama3.2:3b

    ./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--task=transform_language --input.json=./data/thirukkural_Base.json --output.json=./data/thirukkural_English.json --target.language=English --app.ai.chat.provider=ollama"

#### Transform structured json to another language using OpenAI

    export OPEN_AI_KEY=<<Open AI Key>>

    ./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--task=transform_language --input.json=./data/thirukkural_Base.json --output.json=./data/thirukkural_English.json --target.language=English --app.ai.chat.provider=openai"

#### Performing a dry run

Use the `--dry.run=true` to test the transformation to a limited number of volumes, chapters and couplets. By default `--dry.run=false` is set.