Testy McTestface
===
Given a classfile of static mystery methods, write simple test cases to infer what each method does

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
    * Ugh, this is such a mess to use in Eclipse; I feel like I'm learning LaTeX again for the first time and constantly copying the one document I know works

TODO
---
* fxml for css styling
* test String and String[]
* trigger saving to JSON as soon as you upload a classfile
	* autosave after adding every new test case
* add a textbox for what each method does that students can enter
* tooltips
* syntax highlighting for the method header