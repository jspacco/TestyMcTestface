module TestyMcTestface {
  requires transitive javafx.graphics;
  requires transitive javafx.fxml;
  requires transitive javafx.controls;
  requires transitive javafx.base;
  requires com.google.gson;
  requires org.objectweb.asm;

  //main-class = edu.knox.cder.testy.Testy;

  exports edu.knox.cder.testy;
  //exports check;
}