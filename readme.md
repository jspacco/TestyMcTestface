Testy McTestface
===
Given a classfile of static mystery methods, write simple test cases to infer what each method does

## To build

Be sure you have Java 21 installed and that it is the default version of Java on your path.

Clone this repo from github

On Mac/Linux:
change into the folder with a Terminal

```bash
./gradlew build
./gradlew run
```

On Windows:

```command
.\gradlew.bat build
.\gradlew.bat run
```

You can also run the main method with the run button in VS Code. The `main` method is located in the `Testy` class.

Features
----
* supports parameters of int[], int, String, and String[]
* stores all data as JSON
    * base64 encodes the classfile and stores it in the JSON file, so once the code is loaded once and saved to JSON, you don't need the classfile
* only handles static methods
* 

Requirements
----
* JavaFX
    * Install using [these instructions](https://openjfx.io/openjfx-docs/#gradle)
    * My Mac required the x86 files for some reason, even though I have an M1 chip
* gradle
    
TODO
---
* fxml for css styling
* test String and String[]
* trigger saving to JSON as soon as you upload a classfile
	* autosave after adding every new test case
* tooltips
