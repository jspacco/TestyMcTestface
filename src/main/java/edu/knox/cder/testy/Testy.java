package edu.knox.cder.testy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javafx.application.Application;
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
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Testy extends Application 
{
	private File jsonFile;
	private boolean dirty = false;
	//private boolean editAnswer = true;
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

		System.out.printf("before method %d %d\n", content.getRowCount(), content.getChildren().size());

		// get the test label and the results label
		Label testLabel = new Label(testCaseData.toString());
		Label resultLabel = new Label(testCaseData.getResult());

		// remove the last 2 rows, which are the new test form and the answer form
		// we remove them by removing the children from the gridpane
		// I can't figure out how to remove a whole row and there may
		// not be a way to do that in JavaFX
		Node saveButton = content.getChildren().remove(content.getChildren().size()-1);
		Node saveInput = content.getChildren().remove(content.getChildren().size()-1);

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
		content.addRow(content.getRowCount(), saveInput, saveButton);
		
		// clear the text fields used for the new test case
		HBox hBox = (HBox) newTestInput;
		((TextField)hBox.getChildren().get(0)).clear();
		((TextField)hBox.getChildren().get(1)).clear();
		//System.out.printf("hbox contains %s\n", hbox.getChildren().get(0).getClass());
		//System.out.printf("hbox contains %s\n", hbox.getChildren().get(1).getClass());
		//System.out.printf("new test input %s\n", newTestInput.getClass());

		methodPane.requestLayout();
	}
	

    private void update2()
    {
    	List<ExpandableTitledPane> methodPanes = createMethodPanes();
    	
    	methodPanel.getChildren().clear();
    	methodPanel.getChildren().addAll(methodPanes);
		methodPanel.setPrefHeight(600);
    }
    
    private List<ExpandableTitledPane> createMethodPanes()
    {
    	methodPanes = new LinkedList<>();

    	for (int index=0; index < testClassData.getMethodCount(); index++)
    	{
    		MethodData methodData = testClassData.getMethods().get(index);
    		Method method = methods.get(index);
    		
    		// using GridPane instead of VBox to get 2 columns
    		GridPane content = new GridPane();
    		ColumnConstraints parameterCol = new ColumnConstraints();
    		parameterCol.setPercentWidth(80);
    		ColumnConstraints resultCol = new ColumnConstraints();
    		resultCol.setPercentWidth(20);
    		content.getColumnConstraints().addAll(parameterCol, resultCol);
    		
			// put each test into its own row
			for (int i=0; i < methodData.getTestCount(); i++)
			{
				final int testNum = i;
				TestCaseData testCase = methodData.getTests().get(testNum);
				// add to column 0, row i
				content.add(new Label(testCase.toString()), 0, i);
				// add to column 1, row i
				content.add(new Label(testCase.getResult()), 1, i);
				
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
						alert.setHeaderText(String.format("%s should be of type %s", value, type));
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
					System.out.printf("invoked %s, got back %s\n", arrayToString(params), res.toString());
					
					testCaseData.setResult(res.toString());
					methodData.addTest(testCaseData);
					// set dirty so that we save to the json file
					dirty = true;

					update(index2, testCaseData);
				} catch (InvocationTargetException ex) {
					Throwable cause = ex.getCause();
					System.out.println(cause.getMessage());
					alert(methodData.getName(), cause.toString());
				} catch (Exception ex) {
					// some other unexpected exception here
					alert(methodData.getName(), ex.toString());
				}
			});
			
			// next row
			int answerRow = newTestRow+1;
			// create answer textfield
			TextField answer = new TextField();
			answer.setPromptText("What does this method do?");
			String title = "Save Answer";
			if (methodData.getAnswer() != null)
			{
				answer.setText(methodData.getAnswer());
				title = "Edit Answer";
			}

			content.addRow(answerRow, answer);
			// create a button to add the answer
			Button saveAnswerButton = new Button(title);
			content.addRow(answerRow, saveAnswerButton);
			saveAnswerButton.setOnAction(event -> {
				String text = answer.getText();
				methodData.setAnswer(text);
				System.out.println("setting answer to " + text);
				//update();
			});
			
			
			// add the title and the mouse click event for expanding/contracting each method
			String headerString = methodData.getHeader();
	        ExpandableTitledPane titlePane = new ExpandableTitledPane(headerString, content);
			if (methodData.getAnswer() != null)
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
	        		System.out.printf("PRIMARY, is expanded? %s\n", titlePane.isExpanded());
	        	}
	        	titlePane.setExpanded(!titlePane.isExpanded());
	        });
			methodPanes.add(titlePane);
    	}
    	
    	return methodPanes;
    }

	private void alert(String methodName, String message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Test Case Error");
		alert.setHeaderText(String.format("Error running method %s", methodName));
		alert.setContentText(message);
		alert.showAndWait();
	}
    
    private boolean saveAsPrompt(Stage stage)
    {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Save As");
    	
    	
    	if (jsonFile != null)
    		fileChooser.setInitialFileName(jsonFile.getName());
    	else
    		fileChooser.setInitialDirectory(new File("."));
    	jsonFile = fileChooser.showSaveDialog(stage);
    	
    	return saveToJsonFile();
    	
    }
    
    private static Alert setDefaultButton ( Alert alert, ButtonType defBtn ) {
    	   DialogPane pane = alert.getDialogPane();
    	   for ( ButtonType t : alert.getButtonTypes() )
    	      ( (Button) pane.lookupButton(t) ).setDefaultButton( t == defBtn );
    	   return alert;
    	}
    
    private boolean unsavedQuitPrompt(Stage stage)
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
    		saveToJsonFile();
    		return true;
    	} 
    	else if (result == quitButtonType)
    	{
    		return true;
    	}
    	else if (result == saveAsButtonType)
    	{
    		saveAsPrompt(stage);
    		saveToJsonFile();
    		return true;
    	}
    	// this means they either clicked cancel, or closed the window
    	return false;
    }
    
    private boolean saveToJsonFile()
    {
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
	//long lastRefreshTime = 0;
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

		
		//this goes after you've defined your scene, 
		// but before you display your stage
		// scene.addPreLayoutPulseListener(() -> {
    	// 	long refreshTime = System.nanoTime();
    	// 	System.out.println(refreshTime - lastRefreshTime);
    	// 	lastRefreshTime = refreshTime;
		// });

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
			System.out.println("Unsaved changes, please save first!");
			return;
		}
		this.jsonFile = file;
		testClassData = TestClassData.readJson(jsonFile.getPath());
		methods = StaticMethodExtractor.getStaticMethods(testClassData.getClassName(), testClassData.getBytecode());
		dirty = false;

		// update the root panel to display the newly loaded data
		update2();
	}
	
	private void loadClassFile(String classFile) throws ClassNotFoundException, IOException, ParseException
	{
		if (dirty) {
			// TODO: make into Alert
			//JOptionPane.showMessageDialog(null, "Unsaved changes, please save first!");
			return;
		}
		testClassData = StaticMethodExtractor.readFromClassFile(classFile);
		methods = StaticMethodExtractor.getStaticMethods(testClassData.getClassName(), testClassData.getBytecode());
		dirty = false;
		System.out.printf("loaded %d methods from %s (%d now in testClassData)\n", methods.size(), testClassData.getClassName(), testClassData.getMethodCount());
		
		update2();
	}
    
    private MenuBar createMenuBar(Stage stage)
    {
    	MenuBar menuBar = new MenuBar();
    	menuBar.getStyleClass().add("menubar");

    	Menu fileMenu = new Menu("File");
    	MenuItem loadJson = new MenuItem("Load json");
    	loadJson.setOnAction(event -> {
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
    	fileMenu.getItems().add(loadJson);
    	
    	MenuItem loadClassfile = new MenuItem("Load classfile");
    	loadClassfile.setOnAction(event -> {
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
    	fileMenu.getItems().add(loadClassfile);
    	
    	MenuItem save = new MenuItem("Save");
    	save.setOnAction(event -> {
    		// TODO save
    		if (jsonFile == null)
    		{
    			saveAsPrompt(stage);
    		} else
    		{
    			boolean success = saveToJsonFile();
    			System.out.printf("Did we successfully save? %s\n", success);
    		}
    	});
    	fileMenu.getItems().add(save);
    	
    	MenuItem saveAs = new MenuItem("Save As");
    	saveAs.setOnAction(event -> {
    		saveAsPrompt(stage);
    	});
    	fileMenu.getItems().add(saveAs);

		MenuItem extra = new MenuItem("Extra");
		extra.setOnAction(event -> {
			ExpandableTitledPane pane = methodPanes.get(0);
			GridPane content = (GridPane)pane.getContent();
			System.out.println(content.getRowCount());
			System.out.println(content.getChildren().size());
			content.getChildren().stream().forEach(System.out::println);

		});
		fileMenu.getItems().add(extra);
    	
    	menuBar.getMenus().add(fileMenu);

    	return menuBar;
    }
    
    private static String arrayToString(Object[] array) {
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
    
    public static void main(String[] args) 
    {
        launch(args);
    }
}

