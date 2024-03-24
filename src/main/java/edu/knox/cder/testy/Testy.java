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
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
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
import javafx.scene.control.TitledPane;
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
	private TestClassData testClassData;
	private List<Method> methods;
	private VBox root;
	private VBox methodPanel;

    private void update()
    {
    	List<TitledPane> methodPanes = createMethodPanes();
    	
    	methodPanel.getChildren().clear();
    	methodPanel.getChildren().addAll(methodPanes);
    	
    	//Accordion accordion = new Accordion();
    	//accordion.getPanes().addAll(methodPanes);
    	//if (methodPanes.size() > 0) accordion.setExpandedPane(methodPanes.get(0));
    	//methodPanel.getChildren().add(accordion);
    }
    
    /*
     * methodbox: VBox
     */
    
    private List<TitledPane> createMethodPanes()
    {
    	List<TitledPane> methodPanes = new LinkedList<>();
    	
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
			int lastRow = content.getRowCount();
			content.addRow(lastRow, newTest);

			// OK "check" button for triggering us to add a new test case
			Button check = new Button(" " + ((char)0x2713) +" ");
			content.addRow(lastRow, check);
			
			// clicking the "check" button adds a new test case
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
						// TODO highlight box with bad data
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("syntax error a test case");
						alert.setHeaderText(String.format("%s should be of type %s", value, type));
						alert.setContentText(String.format("error in input %d", i));
						alert.showAndWait();
						return;
					}
				}
				
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
					
					update();
				} catch (InvocationTargetException ex) {
					// TODO ugly duplicate code
					Throwable cause = ex.getCause();
					testCaseData.setResult("unknown runtime exception");
					if (cause != null)
						testCaseData.setResult(cause.getMessage());
					
					methodData.addTest(testCaseData);
					
					// set dirty so that we save to the json file
					dirty = true;
					
					update();
				} catch (Exception ex) {
					// some other unexpected exception here
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");
					alert.setHeaderText(String.format("Error running method %s", methodData.getName()));
					alert.setContentText(ex.toString());
					alert.showAndWait();
					throw new RuntimeException(ex);
				}
			});
			
			
			
			// add the title and the mouse click event for expanding/contracting each method
			String headerString = methodData.getHeader();
	        TitledPane titlePane = new TitledPane(headerString, content);
	        titlePane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
	        	System.out.printf("%s %s %s %s\n", event.getButton(), 
	        			titlePane.isExpanded(), 
	        			titlePane.expandedProperty().get(),
	        			event.getSource().getClass());

	        	if (event.getButton() == MouseButton.PRIMARY) {
	        		System.out.printf("PRIMARY, is expanded? \n");
	        	}
	        	titlePane.setExpanded(!titlePane.isExpanded());	
	        });
			methodPanes.add(titlePane);
    	}
    	
    	return methodPanes;
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

    @Override
    public void start(Stage primaryStage) {
    	
        root = new VBox();
        //root.getStyleClass().add("root");

        // TODO legitimate css styling
        //root.setStyle("-fx-font-size: 16pt;");
        //root.setStyle("-fx-font-family: \"Courier New\";");
        //root.getStylesheets().add(getClass().getResource("root.css").toExternalForm());
        root.getChildren().add(createMenuBar(primaryStage));
        
        methodPanel = new VBox();
        root.getChildren().add(new ScrollPane(methodPanel));
        
        Scene scene = new Scene(root, 800, 600);
		URL styleURL = getClass().getResource("/style1.css");
		String stylesheet = styleURL.toExternalForm();
		scene.getStylesheets().add(stylesheet);
        primaryStage.setTitle("TESTify");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(event -> {
        	System.out.println("oncloserequest");
        	// if the test cases are not dirty, then just quit
        	if (!dirty) return;
        	boolean quit = unsavedQuitPrompt(primaryStage);
        	if (!quit) event.consume();
        });
    }

    
	private void loadJsonFile(File file) throws ClassNotFoundException, IOException
	{
		if (dirty) {
			//TODO make this an alert
			System.out.println("Unsaved changes, please save first!");
			return;
		}
		this.jsonFile = file;
		testClassData = TestClassData.readJson(jsonFile.getPath());
		methods = StaticMethodExtractor.getStaticMethods(testClassData.getClassName(), testClassData.getBytecode());
		dirty = false;

		// update the root panel to display the newly loaded data
		update();
	}
	
	private void loadClassFile(String classFile) throws ClassNotFoundException, IOException, ParseException
	{
		if (dirty) {
			// TODO make into Alert
			//JOptionPane.showMessageDialog(null, "Unsaved changes, please save first!");
			return;
		}
		testClassData = StaticMethodExtractor.readFromClassFile(classFile);
		methods = StaticMethodExtractor.getStaticMethods(testClassData.getClassName(), testClassData.getBytecode());
		dirty = false;
		System.out.printf("loaded %d methods from %s (%d now in testClassData)\n", methods.size(), testClassData.getClassName(), testClassData.getMethodCount());
		
		update();
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
			//this.pack();
			//this.repaint();
			
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
			} else 
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

