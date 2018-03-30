package com.itsm.controller;

import com.itsm.controller.data.dto.response.Response;
import com.itsm.worker.LoadService;
import com.itsm.worker.LoadTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.net.URL;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Created by anpiakhota on 7.12.16.
 * https://docs.oracle.com/javase/8/javafx/user-interface-tutorial/line-chart.htm#CIHGBCFI
 */
public class LineChartController implements Initializable {

    @FXML
    private GridPane chartGridPane;

    private Slider sliderScale;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LoadService loadService;


    private XYChart.Series<Number, Number> currentSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> voltageSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> temperatureSeries = new XYChart.Series<>();

    private ObservableList<XYChart.Data<Number, Number>> currentSeriesData = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> voltageSeriesData = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> temperatureSeriesData = FXCollections.observableArrayList();
    private Rectangle2D primaryScreenBounds;
    private int tickUnit;
    private int tickUnitMax;

    {
        currentSeries.setData(currentSeriesData);
        currentSeries.setName("Current");


        voltageSeries.setData(voltageSeriesData);
        voltageSeries.setName("Voltage");

        temperatureSeries.setData(temperatureSeriesData);
        temperatureSeries.setName("Temperature");
    }

    private LineChart<Number, Number> compositeChart;
    private LineChart<Number, Number> currentChart;
    private LineChart<Number, Number> voltageChart;
    private LineChart<Number, Number> temperatureChart;

    private ScrollPane compositeScrollPane;
    private ScrollPane currentScrollPane;
    private ScrollPane voltageScrollPane;
    private ScrollPane temperatureScrollPane;

    private double yMaxCurrentValue, yMaxVoltageValue, yMaxTemperatureValue;
    private int xBufferRequestNumber;
    private long xReadTime;

    private ChartLayout chartLayout = ChartLayout.COMPOSITE;

    public void initialize(URL location, ResourceBundle resources) {

        display(ChartLayout.COMPOSITE);

//        http://fxexperience.com/2011/07/worker-threading-in-javafx-2-0/

        primaryScreenBounds = Screen.getPrimary().getVisualBounds();

//        chartGridPane.getScene().getStylesheets().add(
//                getClass().getClassLoader().getResource("serveiler-theme.css").toExternalForm());
//
//        currentSeries.nodeProperty().get().getStyleClass().add("tooltip-hover-current");

    }

    public void setSliders(Slider range /*, Slider position */) {

        this.sliderScale = range;

        sliderScale.valueChangingProperty().addListener((observableValue, wasChanging, changing) -> {
            if (changing) {
                // TODO nothing
            } else {

//                compositeChart.setAnimated(false);

//                final long toTime = (long) sliderScale.getValue() * checkInterval;

//                http://stackoverflow.com/questions/12093556/javafx-2-x-how-to-remove-xy-line-chart-once-plotted

//                filteredCurrentSeriesData.setPredicate(xy -> xy.getXValue().longValue() <= toTime);
//                filteredVoltageSeriesData.setPredicate(xy -> xy.getXValue().longValue() <= toTime);
//                filteredTemperatureSeriesData.setPredicate(xy -> xy.getXValue().longValue() <= toTime);

                int scale = (int) sliderScale.getValue();
                int width = scale * temperatureSeriesData.size();

                chartGridPane.getChildren().clear();

                switch (chartLayout) {
                    case COMPOSITE:
                        if (width > primaryScreenBounds.getWidth() && scale != 0) {
                            compositeScrollPane.setFitToWidth(false);
                            compositeChart.setPrefWidth(width);
                        } else {
                            compositeScrollPane.setFitToWidth(true);
                        }
                        chartGridPane.getChildren().add(compositeScrollPane);
                        break;
                    default:
                        if (width > primaryScreenBounds.getWidth() && scale != 0) {
                            currentScrollPane.setFitToWidth(false);
                            voltageScrollPane.setFitToWidth(false);
                            temperatureScrollPane.setFitToWidth(false);
                            currentChart.setPrefWidth(width);
                            voltageChart.setPrefWidth(width);
                            temperatureChart.setPrefWidth(width);
                        } else {
                            currentScrollPane.setFitToWidth(true);
                            voltageScrollPane.setFitToWidth(true);
                            temperatureScrollPane.setFitToWidth(true);
                        }

                        chartGridPane.getChildren().add(currentScrollPane);
                        chartGridPane.getChildren().add(voltageScrollPane);
                        chartGridPane.getChildren().add(temperatureScrollPane);

                }

            }

        });

//        sliderScale.valueProperty().addListener((ov, oldValue, newValue) -> {
//
//            switch (chartLayout) {
//                case COMPOSITE:
//
//                    int scale = temperatureSeriesData.size() * newValue.intValue();
//
//                    if (scale == temperatureSeriesData.size()) {
//                        compositeScrollPane.setFitToWidth(true);
//                    } else {
//                        compositeScrollPane.setFitToWidth(false);
//                        compositeChart.setPrefWidth(scale);
//                    }
//
//                    chartGridPane.getChildren().clear();
//                    chartGridPane.getChildren().add(compositeScrollPane);
//                    break;
//                default:
//                    chartGridPane.getChildren().clear();
//                    chartGridPane.getChildren().add(currentChart);
//                    chartGridPane.getChildren().add(voltageChart);
//                    chartGridPane.getChildren().add(temperatureChart);
//            }
//
//        });

    }

    public void displayDebug(ChartLayout layout) {

/*
        Zooming
        https://gist.github.com/james-d/7252698
*/

        chartGridPane.getChildren().clear();

        switch (layout) {
            case COMPOSITE: { // All
                final CategoryAxis xAxis = new CategoryAxis();
                final NumberAxis yAxis = new NumberAxis();
                xAxis.setLabel("Time");
                yAxis.setLabel("Voltage");

                final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
                chart.setTitle("Monitoring All");
                chart.setCreateSymbols(true);
                chart.getData().addAll(generateSeries(20), generateSeries(20), generateSeries(20));

                GridPane.setRowIndex(chart, 0);
                GridPane.setColumnIndex(chart, 0);
                GridPane.setHgrow(chart, Priority.ALWAYS);
                GridPane.setVgrow(chart, Priority.ALWAYS);

                chartGridPane.getChildren().add(chart);

                break;
            }
            case VERTICAL:
            case QUATERNARY:
                { // Current
                    final CategoryAxis xAxis = new CategoryAxis();
                    final NumberAxis yAxis = new NumberAxis();
                    xAxis.setLabel("Time");
                    yAxis.setLabel("Amperage");

                    final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
                    chart.setTitle("Monitoring Amperage");
                    chart.getData().addAll(generateSeries(20));

                    GridPane.setRowIndex(chart, 0);
                    GridPane.setColumnIndex(chart, 0);
                    GridPane.setHgrow(chart, Priority.ALWAYS);
                    GridPane.setVgrow(chart, Priority.ALWAYS);

                    chartGridPane.getChildren().add(chart);
                }
                { // Voltage
                    final CategoryAxis xAxis = new CategoryAxis();
                    final NumberAxis yAxis = new NumberAxis();
                    xAxis.setLabel("Time");
                    yAxis.setLabel("Voltage");

                    final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
                    chart.setTitle("Monitoring Voltage");
                    chart.getData().addAll(generateSeries(20));

                    if (layout == ChartLayout.VERTICAL) {
                        GridPane.setRowIndex(chart, 1);
                        GridPane.setColumnIndex(chart, 0);
                    }

                    if (layout == ChartLayout.QUATERNARY) {
                        GridPane.setRowIndex(chart, 0);
                        GridPane.setColumnIndex(chart, 1);
                    }

                    GridPane.setHgrow(chart, Priority.ALWAYS);
                    GridPane.setVgrow(chart, Priority.ALWAYS);

                    chartGridPane.getChildren().add(chart);
                }
                { // Temperature
                    final CategoryAxis xAxis = new CategoryAxis();
                    final NumberAxis yAxis = new NumberAxis();
                    xAxis.setLabel("Time");
                    yAxis.setLabel("Temperature");

                    final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
                    chart.setTitle("Monitoring Temperature");
                    chart.getData().addAll(generateSeries(20));

                    if (layout == ChartLayout.VERTICAL) {
                        GridPane.setRowIndex(chart, 2);
                        GridPane.setColumnIndex(chart, 0);
                    }

                    if (layout == ChartLayout.QUATERNARY) {
                        GridPane.setRowIndex(chart, 1);
                        GridPane.setColumnIndex(chart, 0);
                    }

                    GridPane.setHgrow(chart, Priority.ALWAYS);
                    GridPane.setVgrow(chart, Priority.ALWAYS);

                    chartGridPane.getChildren().add(chart);

                }

                break;

        }

    }


    /**
     * https://gist.github.com/zinch84/9802986
     * @param layout
     */
    public void display(ChartLayout layout) {

        chartLayout = layout;

        chartGridPane.getChildren().clear();

        switch (layout) {
            case COMPOSITE: { // All

                compositeScrollPane = new ScrollPane();
                compositeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                compositeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                compositeScrollPane.setFitToHeight(true);
                compositeScrollPane.setFitToWidth(true);
                compositeScrollPane.setStyle("-fx-box-border: transparent;");

                final NumberAxis xAxis = new NumberAxis();
                final NumberAxis yAxis = new NumberAxis();

                yAxis.setLabel("Current | Voltage | Temperature");
                xAxis.setLabel("Time");

                yAxis.setAutoRanging(false);
                xAxis.setAutoRanging(false);

                yAxis.setLowerBound(0);
                yAxis.setUpperBound(10);
                yAxis.setAnimated(true);

                xAxis.setLowerBound(0);
                xAxis.setUpperBound(10);
                xAxis.setAnimated(true);

                xAxis.setMinorTickVisible(false);
                yAxis.setMinorTickVisible(false);

                xAxis.setTickUnit(1);
                yAxis.setTickUnit(1);

                if (temperatureSeriesData.size() != 0) {

                    double max = Math.max(Math.max(yMaxCurrentValue, yMaxVoltageValue), yMaxTemperatureValue);

                    yAxis.setUpperBound(max);
                    yAxis.setTickUnit(max / 10);
                    xAxis.setLowerBound(xLowerBound);
                    xAxis.setUpperBound(xUpperBound);
                    xAxis.setTickUnit(tickUnit);

                }

                compositeChart = new LineChart<>(xAxis, yAxis);
                compositeChart.setAnimated(true);

                compositeChart.setLegendVisible(true);
//                compositeChart.setTitle("All");
                compositeChart.getData().addAll(currentSeries, voltageSeries, temperatureSeries);
                compositeChart.setCreateSymbols(true);
                compositeChart.setAlternativeRowFillVisible(true);
                compositeChart.setAlternativeColumnFillVisible(true);
                compositeChart.setVerticalGridLinesVisible(true);
                compositeChart.setHorizontalGridLinesVisible(true);

                compositeScrollPane.setContent(compositeChart);

                GridPane.setRowIndex(compositeScrollPane, 0);
                GridPane.setColumnIndex(compositeScrollPane, 0);
                GridPane.setHgrow(compositeScrollPane, Priority.ALWAYS);
                GridPane.setVgrow(compositeScrollPane, Priority.ALWAYS);

                chartGridPane.getChildren().add(compositeScrollPane);

                break;
            }
            case VERTICAL:
            case QUATERNARY:
                { // Current

                    currentScrollPane = new ScrollPane();
                    currentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    currentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    currentScrollPane.setFitToHeight(true);
                    currentScrollPane.setFitToWidth(true);
                    currentScrollPane.setStyle("-fx-box-border: transparent;");

                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();

                    xAxis.setLabel("Time");
                    yAxis.setLabel("Current");

                    yAxis.setAutoRanging(false);
                    xAxis.setAutoRanging(false);

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(10);
                    yAxis.setAnimated(true);

                    xAxis.setLowerBound(0);
                    xAxis.setUpperBound(10);
                    xAxis.setAnimated(true);

                    xAxis.setMinorTickVisible(false);
                    yAxis.setMinorTickVisible(false);

                    xAxis.setTickUnit(1);
                    yAxis.setTickUnit(1);

                    if (currentSeriesData.size() != 0) {

                        yAxis.setUpperBound(yMaxCurrentValue);
                        yAxis.setTickUnit(yMaxCurrentValue / 10);
                        xAxis.setLowerBound(xLowerBound);
                        xAxis.setUpperBound(xUpperBound);
                        xAxis.setTickUnit(tickUnit);

                    }

                    currentChart = new LineChart<>(xAxis, yAxis);
                    currentChart.setLegendVisible(false);
//                    currentChart.setTitle("Current");
                    currentChart.getData().addAll(currentSeries);

                    currentScrollPane.setContent(currentChart);

                    GridPane.setRowIndex(currentScrollPane, 0);
                    GridPane.setColumnIndex(currentScrollPane, 0);
                    GridPane.setHgrow(currentScrollPane, Priority.ALWAYS);
                    GridPane.setVgrow(currentScrollPane, Priority.ALWAYS);

                    chartGridPane.getChildren().add(currentScrollPane);
                }
                { // Voltage

                    voltageScrollPane = new ScrollPane();
                    voltageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    voltageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    voltageScrollPane.setFitToHeight(true);
                    voltageScrollPane.setFitToWidth(true);
                    voltageScrollPane.setStyle("-fx-box-border: transparent;");

                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();

                    xAxis.setLabel("Time");
                    yAxis.setLabel("Voltage");

                    yAxis.setAutoRanging(false);
                    xAxis.setAutoRanging(false);

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(10);
                    yAxis.setAnimated(true);

                    xAxis.setLowerBound(0);
                    xAxis.setUpperBound(10);
                    xAxis.setAnimated(true);

                    xAxis.setMinorTickVisible(false);
                    yAxis.setMinorTickVisible(false);

                    xAxis.setTickUnit(1);
                    yAxis.setTickUnit(1);

                    if (voltageSeriesData.size() != 0) {

                        yAxis.setUpperBound(yMaxVoltageValue);
                        yAxis.setTickUnit(yMaxVoltageValue / 10);
                        xAxis.setLowerBound(xLowerBound);
                        xAxis.setUpperBound(xUpperBound);
                        xAxis.setTickUnit(tickUnit);

                    }

                    voltageChart = new LineChart<>(xAxis, yAxis);
                    voltageChart.setLegendVisible(false);
//                    voltageChart.setTitle("Voltage");
                    voltageChart.getData().addAll(voltageSeries);

                    voltageScrollPane.setContent(voltageChart);

                    if (layout == ChartLayout.VERTICAL) {
                        GridPane.setRowIndex(voltageScrollPane, 1);
                        GridPane.setColumnIndex(voltageScrollPane, 0);
                    }

                    if (layout == ChartLayout.QUATERNARY) {
                        GridPane.setRowIndex(voltageScrollPane, 0);
                        GridPane.setColumnIndex(voltageScrollPane, 1);
                    }

                    GridPane.setHgrow(voltageScrollPane, Priority.ALWAYS);
                    GridPane.setVgrow(voltageScrollPane, Priority.ALWAYS);

                    chartGridPane.getChildren().add(voltageScrollPane);
                }
                { // Temperature

                    temperatureScrollPane = new ScrollPane();
                    temperatureScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    temperatureScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    temperatureScrollPane.setFitToHeight(true);
                    temperatureScrollPane.setFitToWidth(true);
                    temperatureScrollPane.setStyle("-fx-box-border: transparent;");

                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();

                    xAxis.setLabel("Time");
                    yAxis.setLabel("Temperature");

                    yAxis.setAutoRanging(false);
                    xAxis.setAutoRanging(false);

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(10);
                    yAxis.setAnimated(true);

                    xAxis.setLowerBound(0);
                    xAxis.setUpperBound(10);
                    xAxis.setAnimated(true);

                    xAxis.setMinorTickVisible(false);
                    yAxis.setMinorTickVisible(false);

                    xAxis.setTickUnit(1);
                    yAxis.setTickUnit(1);

                    if (temperatureSeriesData.size() != 0) {

                        yAxis.setUpperBound(yMaxTemperatureValue);
                        yAxis.setTickUnit(yMaxTemperatureValue / 10);
                        xAxis.setLowerBound(xLowerBound);
                        xAxis.setUpperBound(xUpperBound);
                        xAxis.setTickUnit(tickUnit);

                    }

                    temperatureChart = new LineChart<>(xAxis, yAxis);
                    temperatureChart.setLegendVisible(false);
//                    temperatureChart.setTitle("Temperature");
                    temperatureChart.getData().addAll(temperatureSeries);

                    temperatureScrollPane.setContent(temperatureChart);

                    if (layout == ChartLayout.VERTICAL) {
                        GridPane.setRowIndex(temperatureScrollPane, 2);
                        GridPane.setColumnIndex(temperatureScrollPane, 0);
                    }

                    if (layout == ChartLayout.QUATERNARY) {
                        GridPane.setRowIndex(temperatureScrollPane, 1);
                        GridPane.setColumnIndex(temperatureScrollPane, 0);
                    }

                    GridPane.setHgrow(temperatureScrollPane, Priority.ALWAYS);
                    GridPane.setVgrow(temperatureScrollPane, Priority.ALWAYS);

                    chartGridPane.getChildren().add(temperatureScrollPane);

                }

                break;

        }

    }


    /**
     * https://gist.github.com/zinch84/9802986
     * @param layout
     */
    public void display2(ChartLayout layout) {

        chartLayout = layout;

        chartGridPane.getChildren().clear();

        switch (layout) {
            case COMPOSITE: { // All

                compositeScrollPane = new ScrollPane();
                compositeScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                compositeScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                compositeScrollPane.setFitToHeight(true);
                compositeScrollPane.setFitToWidth(true);
                compositeScrollPane.setStyle("-fx-box-border: transparent;");

                final NumberAxis xAxis = new NumberAxis();
                final NumberAxis yAxis = new NumberAxis();

                yAxis.setLabel("Current | Voltage | Temperature");
                xAxis.setLabel("Time");

                yAxis.setAutoRanging(false);
                xAxis.setAutoRanging(false);

                yAxis.setLowerBound(0);
                yAxis.setUpperBound(10);
                yAxis.setAnimated(true);

                xAxis.setLowerBound(0);
                xAxis.setUpperBound(10);
                xAxis.setAnimated(true);

                xAxis.setMinorTickVisible(false);
                yAxis.setMinorTickVisible(false);

                xAxis.setTickUnit(1);
                yAxis.setTickUnit(1);

                if (temperatureSeriesData.size() != 0) {

                    double max = Math.max(Math.max(yMaxCurrentValue, yMaxVoltageValue), yMaxTemperatureValue);

                    yAxis.setUpperBound(max);
                    xAxis.setUpperBound(xReadTime);
                    yAxis.setTickUnit(max / 10);
                    xAxis.setTickUnit(tickUnit);
                }

                compositeChart = new LineChart<>(xAxis, yAxis);
                compositeChart.setAnimated(true);

                compositeChart.setLegendVisible(true);
//                compositeChart.setTitle("All");
                compositeChart.getData().addAll(currentSeries, voltageSeries, temperatureSeries);
                compositeChart.setCreateSymbols(true);
                compositeChart.setAlternativeRowFillVisible(true);
                compositeChart.setAlternativeColumnFillVisible(true);
                compositeChart.setVerticalGridLinesVisible(true);
                compositeChart.setHorizontalGridLinesVisible(true);

                compositeScrollPane.setContent(compositeChart);

                GridPane.setRowIndex(compositeScrollPane, 0);
                GridPane.setColumnIndex(compositeScrollPane, 0);
                GridPane.setHgrow(compositeScrollPane, Priority.ALWAYS);
                GridPane.setVgrow(compositeScrollPane, Priority.ALWAYS);

                chartGridPane.getChildren().add(compositeScrollPane);

                break;
            }
            case VERTICAL:
            case QUATERNARY:
                { // Current

                    currentScrollPane = new ScrollPane();
                    currentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    currentScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    currentScrollPane.setFitToHeight(true);
                    currentScrollPane.setFitToWidth(true);
                    currentScrollPane.setStyle("-fx-box-border: transparent;");

                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();

                    xAxis.setLabel("Time");
                    yAxis.setLabel("Current");

                    yAxis.setAutoRanging(false);
                    xAxis.setAutoRanging(false);

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(10);
                    yAxis.setAnimated(true);

                    xAxis.setLowerBound(0);
                    xAxis.setUpperBound(10);
                    xAxis.setAnimated(true);

                    xAxis.setMinorTickVisible(false);
                    yAxis.setMinorTickVisible(false);

                    xAxis.setTickUnit(1);
                    yAxis.setTickUnit(1);

                    if (currentSeriesData.size() != 0) {

                        yAxis.setUpperBound(yMaxCurrentValue);
                        xAxis.setUpperBound(xReadTime);
                        yAxis.setTickUnit(yMaxCurrentValue / 10);
                        xAxis.setTickUnit(tickUnit);

                    }

                    currentChart = new LineChart<>(xAxis, yAxis);
                    currentChart.setLegendVisible(false);
//                    currentChart.setTitle("Current");
                    currentChart.getData().addAll(currentSeries);

                    currentScrollPane.setContent(currentChart);

                    GridPane.setRowIndex(currentScrollPane, 0);
                    GridPane.setColumnIndex(currentScrollPane, 0);
                    GridPane.setHgrow(currentScrollPane, Priority.ALWAYS);
                    GridPane.setVgrow(currentScrollPane, Priority.ALWAYS);

                    chartGridPane.getChildren().add(currentScrollPane);
                }
                { // Voltage

                    voltageScrollPane = new ScrollPane();
                    voltageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    voltageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    voltageScrollPane.setFitToHeight(true);
                    voltageScrollPane.setFitToWidth(true);
                    voltageScrollPane.setStyle("-fx-box-border: transparent;");

                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();

                    xAxis.setLabel("Time");
                    yAxis.setLabel("Voltage");

                    yAxis.setAutoRanging(false);
                    xAxis.setAutoRanging(false);

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(10);
                    yAxis.setAnimated(true);

                    xAxis.setLowerBound(0);
                    xAxis.setUpperBound(10);
                    xAxis.setAnimated(true);

                    xAxis.setMinorTickVisible(false);
                    yAxis.setMinorTickVisible(false);

                    xAxis.setTickUnit(1);
                    yAxis.setTickUnit(1);

                    if (voltageSeriesData.size() != 0) {

                        yAxis.setUpperBound(yMaxVoltageValue);
                        xAxis.setUpperBound(xReadTime);
                        yAxis.setTickUnit(yMaxVoltageValue / 10);
                        xAxis.setTickUnit(tickUnit);

                    }

                    voltageChart = new LineChart<>(xAxis, yAxis);
                    voltageChart.setLegendVisible(false);
//                    voltageChart.setTitle("Voltage");
                    voltageChart.getData().addAll(voltageSeries);

                    voltageScrollPane.setContent(voltageChart);

                    if (layout == ChartLayout.VERTICAL) {
                        GridPane.setRowIndex(voltageScrollPane, 1);
                        GridPane.setColumnIndex(voltageScrollPane, 0);
                    }

                    if (layout == ChartLayout.QUATERNARY) {
                        GridPane.setRowIndex(voltageScrollPane, 0);
                        GridPane.setColumnIndex(voltageScrollPane, 1);
                    }

                    GridPane.setHgrow(voltageScrollPane, Priority.ALWAYS);
                    GridPane.setVgrow(voltageScrollPane, Priority.ALWAYS);

                    chartGridPane.getChildren().add(voltageScrollPane);
                }
                { // Temperature

                    temperatureScrollPane = new ScrollPane();
                    temperatureScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                    temperatureScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    temperatureScrollPane.setFitToHeight(true);
                    temperatureScrollPane.setFitToWidth(true);
                    temperatureScrollPane.setStyle("-fx-box-border: transparent;");

                    final NumberAxis xAxis = new NumberAxis();
                    final NumberAxis yAxis = new NumberAxis();

                    xAxis.setLabel("Time");
                    yAxis.setLabel("Temperature");

                    yAxis.setAutoRanging(false);
                    xAxis.setAutoRanging(false);

                    yAxis.setLowerBound(0);
                    yAxis.setUpperBound(10);
                    yAxis.setAnimated(true);

                    xAxis.setLowerBound(0);
                    xAxis.setUpperBound(10);
                    xAxis.setAnimated(true);

                    xAxis.setMinorTickVisible(false);
                    yAxis.setMinorTickVisible(false);

                    xAxis.setTickUnit(1);
                    yAxis.setTickUnit(1);

                    if (temperatureSeriesData.size() != 0) {

                        yAxis.setUpperBound(yMaxTemperatureValue);
                        xAxis.setUpperBound(xReadTime);
                        yAxis.setTickUnit(yMaxTemperatureValue / 10);
                        xAxis.setTickUnit(tickUnit);

                    }

                    temperatureChart = new LineChart<>(xAxis, yAxis);
                    temperatureChart.setLegendVisible(false);
//                    temperatureChart.setTitle("Temperature");
                    temperatureChart.getData().addAll(temperatureSeries);

                    temperatureScrollPane.setContent(temperatureChart);

                    if (layout == ChartLayout.VERTICAL) {
                        GridPane.setRowIndex(temperatureScrollPane, 2);
                        GridPane.setColumnIndex(temperatureScrollPane, 0);
                    }

                    if (layout == ChartLayout.QUATERNARY) {
                        GridPane.setRowIndex(temperatureScrollPane, 1);
                        GridPane.setColumnIndex(temperatureScrollPane, 0);
                    }

                    GridPane.setHgrow(temperatureScrollPane, Priority.ALWAYS);
                    GridPane.setVgrow(temperatureScrollPane, Priority.ALWAYS);

                    chartGridPane.getChildren().add(temperatureScrollPane);

                }

                break;

        }

    }

    public void refresh() {

        currentSeriesData.clear();
        voltageSeriesData.clear();
        temperatureSeriesData.clear();

        sliderScale.setValue(0);
        tickUnit = tickUnitMax = 0;

        display(ChartLayout.COMPOSITE);

        lastUpdatedIndex = 0;

        xUpperBound = 0;
        xLowerBound = 0;
        xSizePrior = 0;
        xSizeDelta = 0;

    }


    /** a node which displays a value on hover, but is otherwise empty */
    class HoverNode extends StackPane {

        HoverNode(String value, int type) {

            setPrefSize(15, 15);

            final Label label = createHoverLabel(value, type);

            setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    getChildren().clear();
                    setCursor(Cursor.CROSSHAIR);
                }
            });
        }
    }

    private Label createHoverLabel(String value, int type) {
        final Label label = new Label(value);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        label.setTranslateY(-25);

        if (type == 2) {
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setTextFill(Color.BLACK);
        }

        if (type == 3) {
            label.getStyleClass().addAll("default-color1", "chart-line-symbol", "chart-series-line");
            label.setTextFill(Color.BLACK);
        }

        if (type == 4) {
            label.getStyleClass().addAll("default-color2", "chart-line-symbol", "chart-series-line");
            label.setTextFill(Color.BLACK);
        }

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }

    /**
     * Used for debug purposes
     * @param quantity
     * @return
     */
    private XYChart.Series<String, Number> generateSeries(int quantity) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Random");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm s");

        for (int i = 0; i < quantity; i++) {

            LocalTime now = LocalTime.now().plusSeconds(i);
            series.getData().add(new XYChart.Data(now.format(formatter), Math.ceil(Math.random() * 2000)));
        }

        return series;

    }

    public enum ChartLayout {
        COMPOSITE, VERTICAL, QUATERNARY
    }

    private void updateSeries(ObservableList<String> observableList) {

        observableList.stream()
            .skip(temperatureSeriesData.size())
            .map(line -> line.split(",")) // Stream<String[]>
            .forEach(array -> {

                xBufferRequestNumber = Integer.parseInt(array[0]);
                xReadTime = Long.parseLong(array[1]);

                double current = Double.parseDouble(array[2]);
                currentSeriesData.add(new XYChart.Data<>(Long.parseLong(array[1]), current));
                if (yMaxCurrentValue < current) yMaxCurrentValue = current;
                Tooltip.install(currentSeriesData.get(currentSeriesData.size() - 1).getNode(), new Tooltip("Current: "
                        + current + "\nTime: " + array[1]));

                double voltage = Double.parseDouble(array[3]);
                voltageSeriesData.add(new XYChart.Data<>(Long.parseLong(array[1]), voltage));
                if (yMaxVoltageValue < voltage) yMaxVoltageValue = voltage;
                Tooltip.install(voltageSeriesData.get(voltageSeriesData.size() - 1).getNode(), new Tooltip("Voltage: "
                        + voltage + "\nTime: " + array[1]));

                double temperature = Double.parseDouble(array[4]);
                temperatureSeriesData.add(new XYChart.Data<>(Long.parseLong(array[1]), temperature));
                if (yMaxTemperatureValue < temperature) yMaxTemperatureValue = temperature;
                Tooltip.install(temperatureSeriesData.get(temperatureSeriesData.size() - 1).getNode(), new Tooltip("Temperature: "
                        + temperature + "\nTime: " + array[1]));

                /*  https://gist.github.com/jewelsea/4681797
                    final XYChart.Data<Number, Number> currentData = new XYChart.Data<>(Long.parseLong(array[0]),
                            Double.parseDouble(array[2]));
                    currentData.setNode(new HoverNode(array[2], 2));
                    currentSeriesData.add(currentData);

                    final XYChart.Data<Number, Number> voltageData = new XYChart.Data<>(Long.parseLong(array[0]),
                            Double.parseDouble(array[3]));
                    voltageData.setNode(new HoverNode(array[3], 3));
                    voltageSeriesData.add(voltageData);

                    final XYChart.Data<Number, Number> temperatureData = new XYChart.Data<>(Long.parseLong(array[0]),
                            Double.parseDouble(array[4]));
                    temperatureData.setNode(new HoverNode(array[4], 4));
                    temperatureSeriesData.add(temperatureData);
                */

            });


        tickUnit = tickUnitMax = (int) (xReadTime / xBufferRequestNumber);

        switch (chartLayout) {
            case COMPOSITE:
                chartGridPane.getChildren().clear();

                double max = Math.max(Math.max(yMaxCurrentValue, yMaxVoltageValue), yMaxTemperatureValue);

                ((NumberAxis) compositeChart.getYAxis()).setUpperBound(max);
                ((NumberAxis) compositeChart.getXAxis()).setUpperBound(xReadTime);
                ((NumberAxis) compositeChart.getYAxis()).setTickUnit(max / 10);
                ((NumberAxis) compositeChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(compositeScrollPane);
                break;
            default:
                chartGridPane.getChildren().clear();

                ((NumberAxis) currentChart.getYAxis()).setUpperBound(yMaxCurrentValue);
                ((NumberAxis) currentChart.getXAxis()).setUpperBound(xReadTime);
                ((NumberAxis) currentChart.getYAxis()).setTickUnit(yMaxCurrentValue / 10);
                ((NumberAxis) currentChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(currentScrollPane);

                ((NumberAxis) voltageChart.getYAxis()).setUpperBound(yMaxVoltageValue);
                ((NumberAxis) voltageChart.getXAxis()).setUpperBound(xReadTime);
                ((NumberAxis) voltageChart.getYAxis()).setTickUnit(yMaxVoltageValue / 10);
                ((NumberAxis) voltageChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(voltageScrollPane);

                ((NumberAxis) temperatureChart.getYAxis()).setUpperBound(yMaxTemperatureValue);
                ((NumberAxis) temperatureChart.getXAxis()).setUpperBound(xReadTime);
                ((NumberAxis) temperatureChart.getYAxis()).setTickUnit(yMaxTemperatureValue / 10);
                ((NumberAxis) temperatureChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(temperatureScrollPane);

        }

    }

    private int lastUpdatedIndex = 0;
    private long xUpperBound = 0;
    private long xLowerBound = 0;
    private int xSizePrior = 0;
    private int xSizeDelta = 0;

    public void updateSeries(Response response, int xSize, short checkInterval) {

        if (xSizePrior == 0) {
            xSizePrior = xSize;
        } else {
            if (xSizePrior != xSize) {
                xSizeDelta = xSize - xSizePrior; // xSizeDelta is positive on increasing chart display range and negative on decreasing
            }
        }

        Stream.of(response.getData().blockArray)
            .forEach(r -> {

                lastUpdatedIndex++;

                xUpperBound = lastUpdatedIndex * checkInterval;


                if (xSizeDelta > 0) { // not change xLowerBound until xSizeDelta is 0

                    xSizePrior++;
                    xSizeDelta--;

                } else if (xSizeDelta < 0) { // set xLowerBound regarding delta

                    xSizePrior--;

                    xSizeDelta = +xSizeDelta;
                    xSizeDelta++; // increase delta by one because one additional point will be added

                    currentSeriesData.remove(0, xSizeDelta);
                    voltageSeriesData.remove(0, xSizeDelta);
                    temperatureSeriesData.remove(0, xSizeDelta);

                    xSizeDelta--;

                    xLowerBound = (lastUpdatedIndex - (xSize - xSizeDelta - 1)) * checkInterval;
                    xSizeDelta = 0;

                } else {

                    xSizePrior = xSize;

                    if (lastUpdatedIndex >   xSize) {

                        currentSeriesData.remove(0);
                        voltageSeriesData.remove(0);
                        temperatureSeriesData.remove(0);

                        xLowerBound = (lastUpdatedIndex - (xSize - 1)) * checkInterval;

                    }

                }

                double current = r[0];
                currentSeriesData.add(new XYChart.Data<>(lastUpdatedIndex * checkInterval, current));
                if (yMaxCurrentValue < current) yMaxCurrentValue = current;
                Tooltip.install(currentSeriesData.get(currentSeriesData.size() - 1).getNode(), new Tooltip("Current: "
                        + current + "\nTime: " + lastUpdatedIndex * checkInterval));

                double voltage = r[1];
                voltageSeriesData.add(new XYChart.Data<>(lastUpdatedIndex * checkInterval, voltage));
                if (yMaxVoltageValue < voltage) yMaxVoltageValue = voltage;
                Tooltip.install(voltageSeriesData.get(voltageSeriesData.size() - 1).getNode(), new Tooltip("Voltage: "
                        + voltage + "\nTime: " + lastUpdatedIndex * checkInterval));

                double temperature = response.getData().readTemperature;
                temperatureSeriesData.add(new XYChart.Data<>(lastUpdatedIndex * checkInterval, temperature));
                if (yMaxTemperatureValue < temperature) yMaxTemperatureValue = temperature;
                Tooltip.install(temperatureSeriesData.get(temperatureSeriesData.size() - 1).getNode(), new Tooltip("Temperature: "
                        + temperature + "\nTime: " + lastUpdatedIndex * checkInterval));

            });

        if (lastUpdatedIndex < xSize) {
            if (lastUpdatedIndex > 1) {
                tickUnit = tickUnitMax = (int) (xUpperBound - xLowerBound) / (lastUpdatedIndex - 1);
            } else {
                tickUnit = tickUnitMax =  (int) (xUpperBound - xLowerBound) / 2;
            }
        } else {
            tickUnit = tickUnitMax = (int) (xUpperBound - xLowerBound) / (xSize - 1);
        }

        switch (chartLayout) {
            case COMPOSITE:
                chartGridPane.getChildren().clear();

                double max = Math.max(Math.max(yMaxCurrentValue, yMaxVoltageValue), yMaxTemperatureValue);

                ((NumberAxis) compositeChart.getYAxis()).setUpperBound(max);
                ((NumberAxis) compositeChart.getYAxis()).setTickUnit(max / 10);
                ((NumberAxis) compositeChart.getXAxis()).setLowerBound(xLowerBound);
                ((NumberAxis) compositeChart.getXAxis()).setUpperBound(xUpperBound);
                ((NumberAxis) compositeChart.getXAxis()).setTickUnit(tickUnitMax);

                chartGridPane.getChildren().add(compositeScrollPane);
                break;
            default:
                chartGridPane.getChildren().clear();

                ((NumberAxis) currentChart.getYAxis()).setUpperBound(yMaxCurrentValue);
                ((NumberAxis) currentChart.getYAxis()).setTickUnit(yMaxCurrentValue / 10);
                ((NumberAxis) currentChart.getXAxis()).setLowerBound(xLowerBound);
                ((NumberAxis) currentChart.getXAxis()).setUpperBound(xUpperBound);
                ((NumberAxis) currentChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(currentScrollPane);

                ((NumberAxis) voltageChart.getYAxis()).setUpperBound(yMaxVoltageValue);
                ((NumberAxis) voltageChart.getYAxis()).setTickUnit(yMaxVoltageValue / 10);
                ((NumberAxis) voltageChart.getXAxis()).setLowerBound(xLowerBound);
                ((NumberAxis) voltageChart.getXAxis()).setUpperBound(xUpperBound);
                ((NumberAxis) voltageChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(voltageScrollPane);

                ((NumberAxis) temperatureChart.getYAxis()).setUpperBound(yMaxTemperatureValue);
                ((NumberAxis) temperatureChart.getYAxis()).setTickUnit(yMaxTemperatureValue / 10);
                ((NumberAxis) temperatureChart.getXAxis()).setLowerBound(xLowerBound);
                ((NumberAxis) temperatureChart.getXAxis()).setUpperBound(xUpperBound);
                ((NumberAxis) temperatureChart.getXAxis()).setTickUnit(tickUnitMax);
                chartGridPane.getChildren().add(temperatureScrollPane);

        }

    }

    public void tickLess() {
        if (tickUnit != 0) {
            tickUnit <<= 1;
            adjustTickUnit();
        }
    }

    public void tickMore() {
        if (tickUnit != 0 && tickUnit > tickUnitMax) {
            tickUnit >>= 1;
            adjustTickUnit();
        }
    }

    public void adjustTickUnit() {

        chartGridPane.getChildren().clear();

        switch (chartLayout) {
            case COMPOSITE:
                ((NumberAxis) compositeChart.getXAxis()).setTickUnit(tickUnit);
                chartGridPane.getChildren().add(compositeScrollPane);
                break;
            default:

                ((NumberAxis) currentChart.getXAxis()).setTickUnit(tickUnit);
                ((NumberAxis) voltageChart.getXAxis()).setTickUnit(tickUnit);
                ((NumberAxis) temperatureChart.getXAxis()).setTickUnit(tickUnit);

                chartGridPane.getChildren().add(currentScrollPane);
                chartGridPane.getChildren().add(voltageScrollPane);
                chartGridPane.getChildren().add(temperatureScrollPane);

        }

    }



    /**
     * Source: http://stackoverflow.com/questions/11703568/how-to-use-the-return-value-of-call-method-of-task-class-in-javafx
     */
    public void executeLoadTask(Path path) {

        ExecutorService exService = Executors.newSingleThreadExecutor();
        LoadTask task = applicationContext.getBean(LoadTask.class);

        task.setLoadableFilePath(path);
        exService.submit(task);

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> updateSeries(task.getValue()));

        /* Variants
        loadTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                ObservableList<String> observableList = loadTask.getValue();
            }
        });


        loadTask.valueProperty().addListener(new ChangeListener<Task>() {
            @Override
            public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {

            }

        });
        */

    }

    /**
     * http://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm
     */
    public void startLoadService() {

//        loadService.setExecutor(sequentialServiceExecutor);

        loadService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                event -> updateSeries(loadService.getValue()));

        /*
        loadService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        ObservableList<String> observableList = loadService.getValue();
                    }
                });

        loadService.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("done:" + t.getSource().getValue());
            }
        });

        loadService.setOnRunning(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("done:" + t.getSource().getValue());
            }
        });

       loadService.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("done:" + t.getSource().getValue());
            }
        });
        */

//        loadService.start();

    }

    public void restartLoadService() {

        if (!loadService.getState().equals(Worker.State.RUNNING)) {
            loadService.restart();
        }

    }

    public void stopLoadService() {
        loadService.cancel();
        loadService.clear();
    }

}
