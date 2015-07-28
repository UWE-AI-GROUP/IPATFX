/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ipat_fx;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author kieran
 */
public class IPAT_FX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String contextPath = System.getProperty("user.dir") + "/web/";
        File logFile = new File(contextPath + "/log/log4j-IPAT.log");
        System.setProperty("rootPath", logFile.getAbsolutePath());

        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.setOnHiding((WindowEvent event) -> {
            Platform.runLater(() -> {
                File src = new File(contextPath + "/Client Data");
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                Date date = new Date();
                File dest = new File(contextPath + "/Saves/Ipat_" + dateFormat.format(date));
                if (!dest.exists()) {
                    dest.mkdirs();
                }
                try {
                    FileUtils.copyDirectory(src, dest);
                } catch (IOException ex) {
                    Logger.getLogger(IPAT_FX.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0);
            });
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
