package edu.knox.cder.testy;

import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;

public class ExpandableTitledPane extends TitledPane
{
    public ExpandableTitledPane(String headerString, GridPane content) 
    {
        super(headerString, content);
        // Remove default click behavior
        setExpanded(false); // Start collapsed (optional)
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) 
            {
                if (isExpanded()) 
                {
                    setExpanded(false);
                }
                else
                {
                    setExpanded(true);
                }
            }
        });
    }
}
