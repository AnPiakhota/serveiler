package com.itsm.worker;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.omg.CORBA.portable.Streamable;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Source: http://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
 * https://gist.github.com/jewelsea/4947946
 *
 * The class provides service to load either last modified test file
 * or the file which path is set via setLoadableFilePath method.
 */
public class LoadService extends Service<ObservableList<String>> {

    @Autowired
    private FileWorker fileWorker;

    private ObservableList<String> stringObservableList;
    private Path providedFilePath;
    private Path loadableFilePath;

    public void setProvidedFilePath(Path providedFilePath) {
        this.providedFilePath = providedFilePath;
    }

    protected Task<ObservableList<String>> createTask() {

        return new Task<ObservableList<String>>() {

            @Override
            protected ObservableList<String> call() throws Exception {

                if (stringObservableList == null) {
                    stringObservableList = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
                }

                if (providedFilePath != null) {
                    loadableFilePath = providedFilePath;
                } else {
                    Optional<Path> lastTestPathOptional = fileWorker.getLastTestPath();
                    if (lastTestPathOptional.isPresent()) loadableFilePath = lastTestPathOptional.get();
                    else return stringObservableList;
                }

                if (stringObservableList.isEmpty()) {

                    try (Stream<String> stream = Files.lines(loadableFilePath)) {
                        stream.map(line -> line.trim()).forEachOrdered(stringObservableList::add);
                    }

                } else { // list is not empty

                    try (Stream<String> stream = Files.lines(loadableFilePath)) {
                        stream.skip(stringObservableList.size())
                                .map(line -> line.trim()).forEachOrdered(stringObservableList::add);
                    }

                }

                return stringObservableList;

            }
        };
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

    @Override
    protected void running() {
        super.running();
    }

    public void clear() {
        stringObservableList.clear();
    }

}


