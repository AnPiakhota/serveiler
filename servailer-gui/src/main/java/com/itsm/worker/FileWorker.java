package com.itsm.worker;

import com.itsm.util.OSInfo;
import javafx.fxml.FXML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by anpiakhota on 23.12.16.
 */
@PropertySource("classpath:config/config.properties")
public class FileWorker {

    public static final String PROP_SERVEILER_COM_PORT = "serveiler.com.port";

    public static final String PROP_MODERATION_CALIBRATION_CURRENT_COEF1 = "moderation.calibration.current.coef1";
    public static final String PROP_MODERATION_CALIBRATION_CURRENT_COEF2 = "moderation.calibration.current.coef2";
    public static final String PROP_MODERATION_CALIBRATION_VOLTAGE_COEF1 = "moderation.calibration.voltage.coef1";
    public static final String PROP_MODERATION_CALIBRATION_VOLTAGE_COEF2 = "moderation.calibration.voltage.coef2";
    public static final String PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF1 = "moderation.calibration.temperature.coef1";
    public static final String PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF2 = "moderation.calibration.temperature.coef2";
    public static final String PROP_MODERATION_OUTPUT_VOLTAGE_CONST1 = "moderation.output.voltage.const1";
    public static final String PROP_MODERATION_OUTPUT_VOLTAGE_CONST2 = "moderation.output.voltage.const2";
    public static final String PROP_MODERATION_OUTPUT_VOLTAGE_COEF1 = "moderation.output.voltage.coef1";
    public static final String PROP_MODERATION_OUTPUT_VOLTAGE_COEF2 = "moderation.output.voltage.coef2";
    public static final String PROP_MODERATION_GAIN_FACTOR_CURRENT = "moderation.gain.factor.current";
    public static final String PROP_MODERATION_GAIN_FACTOR_VOLTAGE = "moderation.gain.factor.voltage";

    public static final String PROP_TEST_CHART_DISPLAY_RANGE = "test.chart.range.display";

    /**
     * Calibration check interval is the value to put into START command
     * when calibrating current, voltage, and temperature.
     * The interval is the time span in milliseconds from one check to the next.
     * Minimum interval shouldn't be less than 5 milliseconds.
     * Maximum interval is the max value of two bytes sent to controller in START command.
     *
     */
    public static final String PROP_CALIBRATION_CHECK_INTERVAL = "calibration.check.interval";
    public static final String PROP_SERVEILER_CHECK_INTERVAL = "serveiler.check.interval";

    @Value("${serveiler.workspace}")
    private String workspace;

    private Path workspacePath;
    private Path currentFilePath;
    private long currentCheckStamp;
    private int currentReadNumber;
    private Path propertiesFilePath;
    private Properties properties;
    private List<String> lines;

    @PostConstruct
    public void initialize() {

        try {
            this.workspacePath = Files.createDirectories(Paths.get(workspace));
            this.propertiesFilePath = workspacePath.resolve("serveiler.properties");

            properties = new Properties();

            if (Files.notExists(propertiesFilePath)) {

                try (OutputStream os = Files.newOutputStream(propertiesFilePath)) {

                    properties.setProperty("serveiler.controller", "CP2102");
                    properties.setProperty("serveiler.api", "SerialPundit");

                    switch (OSInfo.getOs()) {
                        case WINDOWS:
                            properties.setProperty(PROP_SERVEILER_COM_PORT, "COM3");
                            break;
                        case UNIX:
                            properties.setProperty(PROP_SERVEILER_COM_PORT, "/dev/ttyUSB0");
                            break;
                        case POSIX_UNIX:
                            properties.setProperty(PROP_SERVEILER_COM_PORT, "/dev/ttyUSB0");
                            break;
                        case MAC:
                        case OTHER:
                            properties.setProperty(PROP_SERVEILER_COM_PORT, "/dev/ttyUSB0");
                            break;
                    }

                    /* Calibration */
                    properties.setProperty(PROP_CALIBRATION_CHECK_INTERVAL, "20");

                    properties.setProperty(PROP_MODERATION_CALIBRATION_CURRENT_COEF1, "");
                    properties.setProperty(PROP_MODERATION_CALIBRATION_CURRENT_COEF2, "");
                    properties.setProperty(PROP_MODERATION_CALIBRATION_VOLTAGE_COEF1, "");
                    properties.setProperty(PROP_MODERATION_CALIBRATION_VOLTAGE_COEF2, "");
                    properties.setProperty(PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF1, "");
                    properties.setProperty(PROP_MODERATION_CALIBRATION_TEMPERATURE_COEF2, "");

                    properties.setProperty(PROP_MODERATION_OUTPUT_VOLTAGE_CONST1, "1000");
                    properties.setProperty(PROP_MODERATION_OUTPUT_VOLTAGE_CONST2, "40000");
                    properties.setProperty(PROP_MODERATION_OUTPUT_VOLTAGE_COEF1, "");
                    properties.setProperty(PROP_MODERATION_OUTPUT_VOLTAGE_COEF2, "");

                    properties.setProperty(PROP_MODERATION_GAIN_FACTOR_CURRENT, "");
                    properties.setProperty(PROP_MODERATION_GAIN_FACTOR_VOLTAGE, "");

                    properties.setProperty(PROP_TEST_CHART_DISPLAY_RANGE, "50");

                    properties.setProperty(PROP_SERVEILER_CHECK_INTERVAL, "");

                    properties.setProperty("info.user.dir", System.getProperty("user.dir"));
                    properties.setProperty("info.user.home", System.getProperty("user.home"));

                    properties.store(os, "Serveiler properties");

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * The method creates worker if it doesn't exist.
     * If the worker does exist, it is got updated.
     */
    public void fileBuffer(String task, short checkInterval, double temperature, int[][] blockArray) throws Exception {

        if (workspacePath == null) workspacePath = Paths.get(".");

        currentFilePath = workspacePath.resolve("Buffer (" + task + ") at "
                + DateTimeFormatter.ofPattern("dd-HH_mm_ss").format(LocalDateTime.now()) + ".csv");
        Files.createFile(currentFilePath);
        currentCheckStamp = 0;
        currentReadNumber = 0;

        try (BufferedWriter writer = Files.newBufferedWriter(currentFilePath,
                StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {

            currentReadNumber++;

            Stream.of(blockArray)
                    .forEach(r -> {
                        try {
                            writer.append(currentReadNumber + "," + currentCheckStamp
                                    + "," + r[0] + "," + r[1] + "," + temperature);
                            currentCheckStamp += checkInterval;
                            writer.newLine();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    });

        }

    }

    /**
     * The method creates worker if it doesn't exist.
     * If the worker does exist, it is got updated.
     */
   @Deprecated
   public void updateFile(short checkInterval, double temperature, int[][] blockArray) throws Exception {

       if (workspacePath == null) workspacePath = Paths.get(".");

       if (currentFilePath == null || Files.notExists(currentFilePath)) {
           currentFilePath = workspacePath.resolve("Test at "
                   + DateTimeFormatter.ofPattern("dd-HH_mm_ss").format(LocalDateTime.now()) + ".csv");
           Files.createFile(currentFilePath);
           currentCheckStamp = 0;
           currentReadNumber = 0;
       }

       try (BufferedWriter writer = Files.newBufferedWriter(currentFilePath,
               StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {

           currentReadNumber++;

           Stream.of(blockArray)
               .forEach(r -> {
                   try {
                       writer.append(currentReadNumber + "," + currentCheckStamp
                               + "," + r[0] + "," + r[1] + "," + temperature);
                       currentCheckStamp += checkInterval;
                       writer.newLine();
                   } catch (IOException e1) {
                       e1.printStackTrace();
                   }
               });

       }

   }

    /**
     * The method creates worker if it doesn't exist.
     * If the worker does exist, it is got updated.
     */
   public void updateFile(final int currentReadNumber, short checkInterval,
                                  double temperature, int[][] blockArray) throws Exception {

       if (workspacePath == null) workspacePath = Paths.get(".");

       if (currentFilePath == null || Files.notExists(currentFilePath)) {
           currentFilePath = workspacePath.resolve("Test at "
                   + DateTimeFormatter.ofPattern("dd-HH_mm_ss").format(LocalDateTime.now()) + ".csv");
           Files.createFile(currentFilePath);
           currentCheckStamp = 0;
       }

       try (BufferedWriter writer = Files.newBufferedWriter(currentFilePath,
               StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {

           Stream.of(blockArray)
               .forEach(r -> {
                   try {

                       String line = currentReadNumber + "," + currentCheckStamp
                               + "," + r[0] + "," + r[1] + "," + temperature;

                       writer.append(line);
                       currentCheckStamp += checkInterval;
                       writer.newLine();

                   } catch (IOException e1) {
                       e1.printStackTrace();
                   }
               });

       }

    }

    public void reset() {
        currentFilePath = null;
        currentCheckStamp = 0;
        currentReadNumber = 0;
    }

    public Optional<Path> getLastTestPath() throws Exception {

      if (workspacePath == null) workspacePath = Paths.get(".");

      final PathMatcher matcher = workspacePath.getFileSystem().getPathMatcher("glob:**Test at*.csv");

      /*
      Files.list(workspacePath)
              .filter(Files::isRegularFile)
              .filter(matcher::matches)
              .forEach(System.out::println);
      */

      /*
      Files.newDirectoryStream(Paths.get("."),
              path -> path.toString().endsWith(".csv"))
              .forEach(System.out::println);
      */

      Optional<Path> lastTestPathOptional = Files.list(workspacePath)
              .filter(Files::isRegularFile)
              .filter(f -> Files.isDirectory(f) == false)
              .filter(matcher::matches)
              .max((f1, f2) -> (int)(f1.toFile().lastModified() - f2.toFile().lastModified()));

     return lastTestPathOptional;

  }

    /**
     * Source: http://www.programcreek.com/java-api-examples/index.php?class=java.nio.file.Files&method=newInputStream
     * http://www.java2s.com/Code/Java/Development-Class/Readasetofpropertiesfromthereceivedinputstreamstripoffanyexcesswhitespacethatexistsinthosepropertyvalues.htm
     *
     * @return
     */
  public Properties getConfigProperties() {

      if (workspacePath == null) workspacePath = Paths.get(".");

      try (InputStream is = Files.newInputStream(propertiesFilePath, StandardOpenOption.READ)) {
          properties.load(is);
          properties.entrySet().stream().forEach(p -> ((String) p.getValue()).trim());
      } catch (IOException e) {
          e.printStackTrace();
          return null;
      }
      return properties;
  }

  public boolean storeConfigProperties(Properties properties) {

      try (OutputStream os = Files.newOutputStream(propertiesFilePath)) {
          properties.store(os, "Serveiler properties");
          return true;
      } catch (IOException e) {
          return false;
      }

  }

    public boolean isModerated() {
        boolean b = properties.entrySet().stream()
                .filter(p -> ((String) p.getKey()).startsWith("moderation"))
                .anyMatch(p -> p.getValue().equals("") || p.getValue().equals("\\s*") );
        return !b;
    }

    /**
     * The method indicates whether check interval has been set or not.
     * @return boolean
     */
    public boolean isRated() {
        return properties.getProperty(PROP_SERVEILER_CHECK_INTERVAL, "")
                .chars().allMatch(Character::isDigit);
    }


   /*
   try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("writePath", true)))) {
        out.println("the text");
   } catch (IOException e) {
        System.err.println(e);
   }


   try {
        Files.write(Paths.get("myfile.txt"), "the text".getBytes(), StandardOpenOption.APPEND);
    }catch (IOException e) {
        //exception handling left as an exercise for the reader
    }


    Set<Record> amazonRecords;
    Comparator<Record> byCreatedTime = Comparator.comparingLong(Record::getCreatedTime);

    List<String> csvContent = amazonRecords.stream()
            .sorted(byCreatedTime)
            .map(o -> o.toString())
            .collect(Collectors.toList());

        Files.write(amazonS3ResultDirPath.resolve("amazon-result-register.csv"), csvContent);


    List<String> csvContent = uspsRecords.stream()
            .map(o -> o.toString())
            .collect(Collectors.toList());

        Files.write(uspsResultDirPath.resolve("download-register.csv"), csvContent);


    public void updateAmazonS3Register(Set<Record> amazonRecords) throws IOException {

        AmazonS3Client client = Main.getS3Client();

        Comparator<Record> byCreatedTime = Comparator.comparingLong(Record::getCreatedTime);

        List<String> csvContent = amazonRecords.stream()
                .sorted(byCreatedTime)
                .map(o -> o.toString())
                .collect(Collectors.toList());

        File tempFile = File.createTempFile("prefix", "tmp");
        Files.write(tempFile.toPath(), csvContent);
        client.putObject(bucket, registerFile, tempFile);
        if (tempFile.exists()) {
            tempFile.delete();
        }

    }


    private Set<Record> requestAmazonS3ForBPODRecordsSummary() throws IOException {

        AmazonS3Client client = Main.getS3Client();

        Set<Record> amazonRecords;

        try (InputStream in = client.getObject(bucket, registerFile).getObjectContent();
             BufferedReader buffer = new BufferedReader(new InputStreamReader(in))) {
            amazonRecords = buffer
                    .lines()
                    .map(s -> {
                        String[] t = s.split(",");
                        return new Record(Long.parseLong(t[0]), Long.parseLong(t[1]), t[2],
                                Long.parseLong(t[3]), Integer.parseInt(t[4]));
                    }).collect(Collectors.toSet());
        }

        return amazonRecords;

    }


    public void reportProofOfDeliveryToReportStorage2() throws Exception {

        try (Stream<Path> stream = Files.list(Paths.get("/home/anpiakhota/idea-ide-projects-csv-report/storage/unpack/"))) {
            List<String> csvContent = stream
                    .map(path -> {
                        String filename = path.getFileName().toString();
                        return filename.substring(filename.indexOf(".") + 1, filename.lastIndexOf("."));
                    })
                    .collect(Collectors.toList());

            List<String> csvContent1;

            try (Stream<Path> stream1 = Files.list(Paths.get("/home/anpiakhota/idea-ide-projects-csv-report2/storage/unpack/"))) {
                csvContent1 = stream1
                        .map(path -> {
                            String filename1 = path.getFileName().toString();
                            return filename1.substring(filename1.indexOf(".") + 1, filename1.lastIndexOf("."));
                        })
                        .collect(Collectors.toList());
            }

//           List<String> result = Stream.concat(csvContent.stream(), csvContent1.stream())
//                   .distinct()
//                   .collect(Collectors.toList());

            csvContent.removeAll(csvContent1);

//           Set<String> ss = csvContent.stream().filter(i -> Collections.frequency(csvContent, i) > 1)
//                   .collect(Collectors.toSet());

            LocalDateTime now = LocalDateTime.now();

            Files.write(reportDirPath.resolve("BPOD list up to "
                            + DateTimeFormatter.ofPattern("d-MMM-yyyy").format(now) + ".csv"),
                    csvContent, StandardOpenOption.CREATE);

//           String infoContent = "Total number of pdf files: " + csvContent.size() + System.lineSeparator() + System.lineSeparator()
//                   + "Processed tar files: " + System.lineSeparator() + sb.toString();
//
//           Files.write(reportDirPath.resolve("BPOD list info at "
//                           + DateTimeFormatter.ofPattern("d-MMM-yyyy").format(now) + ".txt"),
//                   infoContent.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);

        }

    }

    public void configure() {

        USPSWebToolsConfigurer webToolsRegistrator = new USPSWebToolsConfigurer("047GLOBA3862", null);

        AmazonS3AccessConfigurer amazonS3AccessConfigurer = new AmazonS3AccessConfigurer(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        amazonS3AccessConfigurer.setBasePath(AWS_BASE_PATH);
        amazonS3AccessConfigurer.setBucketName(AWS_BUCKET_NAME);

        USPSConfigurer configurer = new USPSConfigurer(webToolsRegistrator, amazonS3AccessConfigurer);
        uspsDataProvider.configure(configurer);

    }

    public void track() throws Exception {

        try (Stream<String> stream = Files.lines(bulkSourceFilePath)) {

            stream.forEach((s) -> {

                if (!s.trim().isEmpty()) {

                    TrackingRequest trackingRequest = new TrackingRequest(Calendar.getInstance(), s.trim());

                    TrackingResponseImpl trackingResponse = null;
                    try {
                        trackingResponse = (TrackingResponseImpl) uspsDataProvider.track(trackingRequest);

                        if (!trackingResponse.getShipments().isEmpty()) {
                            TrackingShipment trackingShipment = trackingResponse.getShipments().getFirst();

                            String reportFilename = "success-" + s + "-" + trackingShipment.getStatus().name() + ".txt";

                            Files.write(bulkStorageDirPath.resolve(reportFilename), trackingResponse.toString().getBytes(),
                                    StandardOpenOption.CREATE);

                            Signature signature = trackingShipment.getSignatureData();

                            if (signature == null) return;

                            Path signatureFilePath = null;
                            switch (signature.getFormat()) {
                                case PDF:
                                    String signatureFilename = "success-" + s + "-" + trackingShipment.getStatus().name() + ".pdf";
                                    signatureFilePath = bulkStorageDirPath.resolve(signatureFilename);
                                    break;
                            }

                            byte[] decodedSignature = Base64.getDecoder().decode(signature.getSignatureData());
                            Files.write(signatureFilePath, decodedSignature, StandardOpenOption.CREATE);

                        }

                    } catch (TrackException e) {

                        try {
                            Path exceptionReportFilePath = Files.createFile(bulkStorageDirPath.resolve("failure-" + s + ".txt"));
                            StringWriter errors = new StringWriter();
                            e.printStackTrace(new PrintWriter(errors));
                            Files.write(exceptionReportFilePath, errors.toString().getBytes());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });

        }

    }

*/


}
