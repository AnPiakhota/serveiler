package com.itsm.worker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Source: https://docs.oracle.com/javafx/2/api/javafx/concurrent/Task.html
 * The class provides task to load whole file content
 * from the file which path is set via setLoadableFilePath method.
 */
public class LoadTask extends Task<ObservableList<String>> {

    private Path loadableFilePath;

    public void setLoadableFilePath(Path loadableFilePath) {
        this.loadableFilePath = loadableFilePath;
    }

    @Override
    protected ObservableList<String> call() throws Exception {

        if (Files.notExists(loadableFilePath)) return null;

        ObservableList<String> stringObservableList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

        try (Stream<String> stream = Files.lines(loadableFilePath)) {

            /*

            Source: http://stackoverflow.com/questions/22753755/how-to-add-elements-of-a-java8-stream-into-an-existing-list

            stream.forEach(System.out::println);
            stream.map(line -> line.trim()).sequential()
                .collect(Collectors.toCollection(() -> stringObservableList));

            */

            stream.map(line -> line.trim()).forEachOrdered(stringObservableList::add);

        }

        return stringObservableList;

    }

    @Override
    protected void running() {
        super.running();
    }

    @Override protected void succeeded() {
        super.succeeded();
    }

    @Override protected void cancelled() {
        super.cancelled();
    }

    @Override protected void failed() {
        super.failed();
    }

}


