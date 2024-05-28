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
* trigger saving to JSON as soon as you upload a classfile
	* autosave after adding every new test case
* tooltips
* show the empty string better
* " and ' both work for strings since we are treating inputs as JSON; either allow this, or catch it in the GUI and disallow it
* allow the expandable method panes to resize up to the size of the main window; right now there is a fixed size somewhere, probably set in the scene
* highlight rows when we click them
* allow copy/paste of test cases
* check the UI of the test cases and make sure it makes sense; should we put in method name? should we try other columns to keep it more organized?
