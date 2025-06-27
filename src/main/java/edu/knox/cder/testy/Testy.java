package edu.knox.cder.testy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

 public class Testy extends Application 
{
	private File jsonFile;
	private boolean dirty = false;
	private TestClassData testClassData;
	private List<Method> methods;
	private List<ExpandableTitledPane> methodPanes;
	private VBox root;
	private VBox methodPanel;

	private void update(int methodIndex, TestCaseData testCaseData)
	{
		
		// get the method pane and add a new test result to it
		ExpandableTitledPane methodPane = methodPanes.get(methodIndex);
		GridPane content = (GridPane) methodPane.getContent();

		// get the test label and the results label
		Label testLabel = new Label(testCaseData.toString());
		Label resultLabel = new Label(testCaseData.getResult());
		//TODO: add a method to check if a test case data is an exception
		if (testCaseData.getResult().startsWith("EXCEPTION"))
		{
			// set the font red for the result label
			resultLabel.getStyleClass().add("exception-label");
		}

		// remove the last 2 rows, which are the new test form and the answer form
		// the answer form is a single VBox containing both parts
		// we remove them by removing the children from the gridpane
		// I can't figure out how to remove a whole row and there may
		// not be a way to do that in JavaFX
		Node editOrSaveButtonAndInput = content.getChildren().remove(content.getChildren().size()-1);

		// also get the check button and the new test input
		Node checkButton = content.getChildren().remove(content.getChildren().size()-1);
		Node newTestInput = content.getChildren().remove(content.getChildren().size()-1);

		// this reduces the number of rows in the gridpane,
		// and also reduces the number of children in the gridpane
		// adding more children does not, however, increase the number of rows
		// instead, we are going to add 3 new rows to the gridpane
		// add the new test case to the grid
		content.addRow(content.getRowCount(), testLabel, resultLabel);
		// add back in the new test input row
		content.addRow(content.getRowCount(), newTestInput, checkButton);
		// add back in the answer row
		content.addRow(content.getRowCount(), editOrSaveButtonAndInput);
		
		// clear the text fields used for the new test case
		HBox hBox = (HBox) newTestInput;
		hBox.getChildren().stream().forEach(n -> ((TextField)n).clear());
		
		methodPane.requestLayout();
	}

	private void saveAnswer(String answer, int index)
	{
		ExpandableTitledPane methodPane = methodPanes.get(index);
		if (answer != null)
		{
			methodPane.getStyleClass().remove("titled-pane-grey");
			methodPane.getStyleClass().add("titled-pane-green");

			methodPane.setExpanded(false);
		}
		else
		{
			methodPane.getStyleClass().remove("titled-pane-green");
			methodPane.getStyleClass().add("titled-pane-grey");
		}
	}
	

    private void reloadMethodPanes()
    {
    	List<ExpandableTitledPane> methodPanes = createMethodPanes();
    	
    	methodPanel.getChildren().clear();
    	methodPanel.getChildren().addAll(methodPanes);
    }
    
    private List<ExpandableTitledPane> createMethodPanes()
    {
    	methodPanes = new LinkedList<>();

		if (testClassData == null) return methodPanes;

    	for (int index=0; index < testClassData.getMethodCount(); index++)
    	{
    		MethodData methodData = testClassData.getMethods().get(index);
    		Method method = methods.get(index);
    		
    		// using GridPane instead of VBox to get 2 columns
    		GridPane content = new GridPane();
			content.getStyleClass().add("gridpane");
			
			

			Label parameterLabel = new Label("Parameters");
			parameterLabel.getStyleClass().add("parameter-label");
			Label returnValueLabel = new Label("Return values");
			returnValueLabel.getStyleClass().add("parameter-label");
			content.add(parameterLabel, 0, 0);
			content.add(returnValueLabel, 1, 0);
    		
			// put each test into its own row
			for (int i=0; i < methodData.getTestCount(); i++)
			{
				final int testNum = i;
				TestCaseData testCase = methodData.getTests().get(testNum);
				// add to column 0, row i+1 (adding 1 because of the header row)
				final Label label1 = new Label(testCase.toString());
				content.add(label1, 0, i + 1);
				// add to column 1, row i+1 (adding 1 because of the header row)
				final Label label2 = new Label(testCase.getResult());
				if (testCase.getResult().startsWith("EXCEPTION"))
				{
					label2.getStyleClass().add("exception-label");
				}
				content.add(label2, 1, i + 1);
				//TODO: add highlighting of the row when clicked
			}
			
			HBox newTest = new HBox();
			TextField[] inputs = new TextField[methodData.getParameterCount()];
			for (int j=0; j < methodData.getParameterCount(); j++)
			{
				ParameterData p = methodData.getParameters().get(j);
				TextField textField = new TextField();
				textField.setPromptText(p.getType());
				inputs[j] = textField;
				newTest.getChildren().add(textField);
			}
			// add the form for adding a new test to the last row
			int newTestRow = content.getRowCount();
			content.addRow(newTestRow, newTest);

			// OK "check" button for triggering us to add a new test case
			Button check = new Button(" " + ((char)0x2713) +" ");
			content.addRow(newTestRow, check);
			
			// clicking the "check" button adds a new test case
			int index2 = index;
			check.setOnAction(event -> {
				List<ParameterData> parameters = methodData.getParameters();
				
				List<String> actualParameters = new LinkedList<>();
				// pull actual paramters from the text fields in the GUI
				for (int i=0; i<inputs.length; i++) {
					String type = parameters.get(i).getType();
					String value = inputs[i].getText();
					if (StaticMethodExtractor.validate(value, type))
					{
						actualParameters.add(value);
					}
					else
					{
						// TODO: highlight box with bad data
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("syntax error a test case");
						alert.setHeaderText(
							String.format(
								"parameter number %d should be of type %s; '%s' is not a valid input", i+1, type, value));
						alert.setContentText(String.format("error in input %d", i));
						alert.showAndWait();
						return;
					}
				}
				
				// new test case data
				TestCaseData testCaseData = new TestCaseData(actualParameters);
				
				Object[] params = testCaseData.getParameterArray(methodData.getParameterTypes());
				
				try {
					System.out.printf("invoking method %s with %s\n", method.getName(), arrayToString(params));
					Object res = method.invoke(null, params);
					System.out.printf("invoked %s, got back %s\n", arrayToString(params), customToString(res));
					
					// if res is an array, convert it to a string
					// we want [1,2,3] and not [I@1a2b3c4d
					if (res instanceof int[])
					{
						res = Arrays.toString((int[])res);
					}
					else if (res instanceof String[])
					{
						res = Arrays.toString((String[])res);
					}
					testCaseData.setResult(res.toString());
					methodData.addTest(testCaseData);
					// set dirty so that we save to the json file
					dirty = true;

					update(index2, testCaseData);
				} catch (InvocationTargetException ex) {
					// an exception happened when calling the method
					// this can happen if there are test inputs that legitimately
					// cause an exception. For example, if the method 
					// gets at a given index and we give an index that is out of bounds
					// we should get an exception

					Throwable cause = ex.getCause();
					testCaseData.setResult("EXCEPTION: " + cause.getMessage());
					methodData.addTest(testCaseData);
					// set dirty so that we save to the json file
					dirty = true;

					update(index2, testCaseData);
				} catch (Exception ex) {
					// some other unexpected exception here
					// we should give an alert to the user
					// this usually means that the inputs for
					// the test case were not valid, i.e. we 
					// needed int[] and int and we gave something
					// like: [1,2,3] [4], so the types don't match.
					ex.printStackTrace();
					alert(methodData.getName(), ex.toString());
				}
			});
			
			// next row
			int answerRow = newTestRow+1;
			// create answer textarea
			TextArea answer = new TextArea();
			answer.setPromptText("What does this method do?");
			answer.setWrapText(true);
			answer.setPrefRowCount(2);
			//answer.setPrefColumnCount(25);
			answer.getStyleClass().add("answer-textarea");

			// vertical box for the answer that can grow
			VBox answerBox = new VBox();
			answerBox.setPadding(new Insets(10));
        	answerBox.setSpacing(10);
			VBox.setVgrow(answer, Priority.ALWAYS);
			answerBox.getChildren().add(answer);

			// One button, two different possible meanings:
			// "edit answer": unlocks the textarea for editing
			// "save answer": saves the answer, locks the textarea for editing
			boolean hasAnswer = methodData.getAnswer() != null;
			if (methodData.getAnswer() != null)
			{
				answer.setText(methodData.getAnswer());
				answer.getStyleClass().add("answer-textarea-uneditable");
				answer.setEditable(false);
			}

			// save or edit button
			Button saveOrEditButton = new Button(hasAnswer ? "Edit Answer" : "Save Answer");

			saveOrEditButton.setOnAction(event -> {
				//Button b = (Button)event.getSource();
				if (saveOrEditButton.getText().equals("Edit Answer"))
				{
					// we now want to edit the answer
					answer.getStyleClass().remove("answer-textarea-uneditable");
					answer.setEditable(true);
					saveOrEditButton.setText("Save Answer");
				}
				else
				{
					// disallow saving empty answers
					// these are most likely mistakes
					String text = answer.getText();
					if (text == null || text.isEmpty()) {
						alert(methodData.getName(), "Please provide an answer");
						return;
					}
					answer.getStyleClass().add("answer-textarea-uneditable");
					answer.setEditable(false);
					methodData.setAnswer(text);
					dirty = true;
					// need to get the save thing
					saveAnswer(text, index2);
					// once we've saved the answer, we can edit it
					saveOrEditButton.setText("Edit Answer");
				}
			});

			// vbox for the answer and the button together
			VBox answerContainer = new VBox();
			answerContainer.setPadding(new Insets(10));
			answerContainer.setSpacing(10); // Add spacing between the text area and button

			// Add the answer box and button inside the VBox
			answerContainer.getChildren().addAll(answerBox, saveOrEditButton);

			// Add the answerContainer to the GridPane
			content.addRow(answerRow+1, answerContainer);

			// Ensure the answerBox takes up two columns
			GridPane.setColumnSpan(answerContainer, 2);


			
			// // Don't add this yet
			// content.addRow(answerRow, answerBox);
			// // span two columns
			// GridPane.setColumnSpan(answerBox, 2);

			

			// // add the edit/save button to the gridpane
			// //TODO: put the button BELOW the answer box, ugh
			// //content.addRow(answerRow+1, saveOrEditButton);
			// content.addRow(answerRow+2, saveOrEditButton);
			
			
			// add the title and the mouse click event for expanding/contracting each method
			String headerString = methodData.getHeader();
	        ExpandableTitledPane titlePane = new ExpandableTitledPane(headerString, content);
			if (hasAnswer)
				titlePane.getStyleClass().add("titled-pane-green");
			else
				titlePane.getStyleClass().add("titled-pane-grey");
	        titlePane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
				// TODO: is this mouse handler even necessary?
	        	System.out.printf("%s %s %s %s\n", event.getButton(), 
	        			titlePane.isExpanded(), 
	        			titlePane.expandedProperty().get(),
	        			event.getSource().getClass());

	        	if (event.getButton() == MouseButton.PRIMARY) {
	        		//System.out.printf("PRIMARY, is expanded? %s\n", titlePane.isExpanded());
	        	}
	        	titlePane.setExpanded(!titlePane.isExpanded());
	        });
			methodPanes.add(titlePane);
    	}
    	
    	return methodPanes;
    }

	private void alert(AlertType type, String title, String header, String message)
	{
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void alert(String methodName, String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(String.format("Error in method %s", methodName));
		alert.setContentText(message);
		alert.showAndWait();
	}
    
    private boolean saveAsPrompt(Window window)
    {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Save As");
    	
    	
    	if (jsonFile != null)
    		fileChooser.setInitialFileName(jsonFile.getName());
    	else
    		fileChooser.setInitialDirectory(new File("."));
    	jsonFile = fileChooser.showSaveDialog(window);
    	
    	return saveToJsonFile();
    	
    }
    
    private static Alert setDefaultButton ( Alert alert, ButtonType defBtn ) {
    	   DialogPane pane = alert.getDialogPane();
    	   for ( ButtonType t : alert.getButtonTypes() )
    	      ( (Button) pane.lookupButton(t) ).setDefaultButton( t == defBtn );
    	   return alert;
    	}
    
    private boolean unsavedQuitPrompt(Window window)
    {
    	ButtonType saveButtonType = new ButtonType("Save");
    	ButtonType cancelButtonType = new ButtonType("Cancel");
    	ButtonType quitButtonType = new ButtonType("Quit without saving");
    	ButtonType saveAsButtonType = new ButtonType("Save As...");
    	
		

    	Alert alert = new Alert(AlertType.CONFIRMATION);
    	alert.setTitle("Please choose an option:");
    	alert.setHeaderText("Unsaved changes detected!");

    	alert.getButtonTypes().clear();
    	alert.getButtonTypes().addAll(
    			cancelButtonType, 
    			quitButtonType, 
    			saveButtonType, 
    			saveAsButtonType
    	);
    	// Set the default button type to Save
    	setDefaultButton(alert, saveButtonType);

    	ButtonType result = alert.showAndWait().get();
    	
    	if (result == saveButtonType)
    	{
			if (jsonFile == null) {
				saveAsPrompt(window);
			}
    		return saveToJsonFile();
    	} 
    	else if (result == quitButtonType)
    	{
    		return true;
    	}
    	else if (result == saveAsButtonType)
    	{
    		saveAsPrompt(window);
    		saveToJsonFile();
    		return true;
    	}
    	// this means they either clicked cancel, or closed the window
    	return false;
    }
    
    private boolean saveToJsonFile()
    {
		if (jsonFile == null) {
			return false;
		}

    	if (jsonFile != null) {
    	    try {
				testClassData.writeJson(jsonFile);
				dirty = false;
				return true;
			} catch (IOException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle(String.format("unable to save to %s", jsonFile.getAbsolutePath()));
				alert.setHeaderText("unable to save file " +e.getMessage());
				alert.setContentText(e.toString());
				alert.showAndWait();
			}
    	}
    	return false;
    }

    @Override
    public void start(Stage primaryStage)
	{
        root = new VBox();

        root.getChildren().add(createMenuBar(primaryStage));
        
        methodPanel = new VBox();
		methodPanel.setFillWidth(true);
		ScrollPane scrollPane = new ScrollPane(methodPanel);
        root.getChildren().add(scrollPane);
        
        Scene scene = new Scene(root, 800, 600);

		URL styleURL = getClass().getResource("/style1.css");
		String stylesheet = styleURL.toExternalForm();
		scene.getStylesheets().add(stylesheet);
        primaryStage.setTitle("Testy McTestface");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(event -> {
        	//System.out.println("oncloserequest");
        	// if the test cases are not dirty, then just quit
        	if (!dirty) return;
        	boolean quit = unsavedQuitPrompt(primaryStage);
        	if (!quit) event.consume();
        });
    }

    
	private void loadJsonFile(File file) throws ClassNotFoundException, IOException
	{
		if (dirty) {
			//TODO: make this an alert
			alert(AlertType.WARNING, "Unsaved changes", "Unsaved changes detected", "Please save first!");
			System.out.println("Unsaved changes, please save first!");
			return;
		}
		try {
			testClassData = TestClassData.readJson(file.getPath());
			this.jsonFile = file;
		} catch (Exception e) {
			alert(AlertType.ERROR, "Error reading json file", "Error reading json file", "Are you sure this is a JSON file?");
			throw new IOException(e);
		}
		methods = StaticMethodExtractor.getStaticMethods(testClassData.getClassName(), testClassData.getBytecode());
		dirty = false;

		// load the root panel to display the newly loaded data
		reloadMethodPanes();
	}
	
	private void loadClassFile(String classFile) throws ClassNotFoundException, IOException, ParseException
	{
		if (dirty) {
			// TODO: make into Alert
			//JOptionPane.showMessageDialog(null, "Unsaved changes, please save first!");
			return;
		}
		
		try {
			testClassData = StaticMethodExtractor.readFromClassFile(classFile);
		} catch (Exception e) {
			alert(AlertType.ERROR, "Error reading class file", "Error reading class file", "Are you sure this is a class file?");
			throw new IOException(e);
		}
		methods = StaticMethodExtractor.getStaticMethods(testClassData.getClassName(), testClassData.getBytecode());
		dirty = false;
		System.out.printf("loaded %d methods from %s (%d now in testClassData)\n", 
		methods.size(), testClassData.getClassName(), testClassData.getMethodCount());
		
		// reload the root panel to display the newly loaded data
		reloadMethodPanes();
	}
    
    private MenuBar createMenuBar(Stage stage)
    {
    	MenuBar menuBar = new MenuBar();
    	menuBar.getStyleClass().add("menubar");

    	Menu fileMenu = new Menu("File");
		createMenuItem(fileMenu, "HTMLify", () -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File("."));
			jsonFile = fileChooser.showOpenDialog(stage);

			if (jsonFile != null)
			{
				StringBuffer sb = new StringBuffer();
				try {
					testClassData = TestClassData.readJson(jsonFile.getPath());
					
					sb.append("<html><head><title>Testy McTestface</title></head><body><table border='1'>\n");
					sb.append("<tr><th>Method</th><th>Header</th><th>Answer</th></tr>\n");

					testClassData.getMethods().forEach(m -> {
						String name = m.getName();
						String header = m.getHeader();
						String answer = m.getAnswer();
						
						sb.append(String.format("<tr>\n\t%s\n\t%s\n\t%s\n</tr>\n", td(b(name)), td(b(header)), td(b(answer))));
					
						m.getTests().forEach(t -> {
							String params = String.join(", ", t.getActualParameters());
							String result = t.getResult();
							sb.append(String.format("<tr>\n\t%s\n\t%s\n\t<td></td>\n</tr>\n", td(params), td(result)));
						});
						sb.append("<tr><td colspan='3'>&nbsp;</td></tr>\n");
					});
					
					sb.append("</table></body></html>");
				} catch (Exception e) {
					alert(AlertType.ERROR, "Error reading json file", "Error reading json file", "Are you sure this is a JSON file?");
				}
				String html = sb.toString();
				String filePath = null;
				try {
					// Choose a new location to save the file
					FileChooser fileChooser2 = new FileChooser();
					fileChooser2.setInitialDirectory(new File("."));
					File htmlFile = fileChooser2.showSaveDialog(stage);
					// so that we have this string for exceptions
					filePath = htmlFile.getAbsolutePath();
					if (htmlFile != null)
					{
						BufferedWriter bw = new BufferedWriter(new FileWriter(htmlFile));
						bw.write(html);
						bw.flush();
						bw.close();
					}
					alert(AlertType.INFORMATION, "HTML file written", "HTML file written", "HTML file written to " + htmlFile.getAbsolutePath());
				} catch (Exception e) {
					alert(AlertType.ERROR, "Error writing to HTML file", "Error writing to HTML file", "Cannot write to " + filePath);
				}
			}
		});
		createMenuItem(fileMenu, "Expand All", () -> {
			methodPanes.forEach(p -> p.setExpanded(true));
		});
		createMenuItem(fileMenu, "Load json", () -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File("."));
			jsonFile = fileChooser.showOpenDialog(stage);

			if (jsonFile != null)
			{
				try {
					loadJsonFile(jsonFile);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
    	});
    	
    	createMenuItem(fileMenu, "Load classfile", () -> {
    		FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialDirectory(new File("."));
			File classFile = fileChooser.showOpenDialog(stage);

			if (classFile != null)
			{
				try {
					// TODO load the classfile
					loadClassFile(classFile.getAbsolutePath());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
    	});
    	
		createMenuItem(fileMenu, "Save", () -> {
    		// TODO save
    		if (jsonFile == null)
    		{
    			saveAsPrompt(stage);
    		} 
			else
    		{
    			boolean success = saveToJsonFile();
    			System.out.printf("Did we successfully save? %s\n", success);
    		}
    	});
    	
		createMenuItem(fileMenu, "Save As", () -> {
    		saveAsPrompt(stage);
    	});

		createMenuItem(fileMenu, "Clear", () -> {
			
			if (dirty)
			{
				boolean quit = unsavedQuitPrompt(this.root.getScene().getWindow());
				//TODO: add alert
				if (!quit) 
				{
					System.out.println("Unable to save file, quitting");
					return;
				}
			}
			testClassData = null;
			methods = null;
			reloadMethodPanes();
		});

		createMenuItem(fileMenu, "Quit", () -> {
			if (dirty)
			{
        		boolean quit = unsavedQuitPrompt(this.root.getScene().getWindow());
			}
			System.exit(0);
		});
    	
    	menuBar.getMenus().add(fileMenu);

    	return menuBar;
    }

	private void createMenuItem(Menu menu, String name, Runnable action)
	{
		MenuItem menuItem = new MenuItem(name);
		menuItem.setOnAction(event -> action.run());
		menu.getItems().add(menuItem);
	}
    
	public static String arrayToString(Object[] array) {
		if (array == null || array.length == 0) {
			return "[]";
		}

		StringBuilder sb = new StringBuilder("[");
		for (Object element : array)
		{
			if (element instanceof Object[])
			{
				sb.append(arrayToString((Object[])element));
			}
			else if (element instanceof int[])
			{
				sb.append(Arrays.toString((int[]) element));
			}
			else if (element instanceof String[])
			{
				//TODO: test String[]
				sb.append(Arrays.toString((String[]) element));
			}
			else 
			{
				sb.append(element); // handle other types as needed
			}
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1); // remove trailing comma
		sb.append("]");
		return sb.toString();
	}

	private static String td(String s)
	{
		return "<td>" + s + "</td>";
	}

	private static String b(String s)
	{
		return "<b>" + s + "</b>";
	}

	
    private static String customToString(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof int[]) {
			return Arrays.toString((int[]) obj);
		} else if (obj instanceof String[]) {
			return Arrays.toString((String[]) obj);
		} else if (obj instanceof Object[]) {
			return arrayToString((Object[]) obj);
        } else {
            return obj.toString();
        }
    }
    
    public static void start(String[] args) 
    {
        launch(Testy.class, args);
    }
}

