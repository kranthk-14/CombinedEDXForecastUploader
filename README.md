# Combined EDX Forecast Uploader

A Java application for uploading forecast data to EDX (Enterprise Data Exchange) system with configurable parameters for batch forecast exports.

## Overview

This application provides a robust solution for exporting forecast data from forecasting systems to EDX. It supports various configuration options including different time granularities (daily/weekly), probability values, and forecast types.

## Features

- ✅ **Flexible Time Granularity**: Support for both daily and weekly forecast exports
- ✅ **Configurable Parameters**: Extensive configuration options for forecast exports
- ✅ **Batch Processing**: Handle multiple forecasting groups in a single run
- ✅ **Probability Quantiles**: Support for custom probability values
- ✅ **Snapshot Support**: Export forecasts from specific snapshot dates
- ✅ **Comprehensive Logging**: Detailed logging for monitoring and debugging
- ✅ **Error Handling**: Robust error handling and exception management

## Project Structure

```
CombinedEDXForecastUploader/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── forecast/
│                   └── vending/
│                       └── app/
│                           └── CombinedEDXForecastUploader.java
├── pom.xml
├── README.md
├── config/
│   ├── application.properties
│   └── log4j.properties
└── examples/
    └── sample-config.properties
```

## Prerequisites

- Java 8 or higher
- Maven 3.6 or higher
- Access to internal forecast services
- Valid credentials and permissions

## Dependencies

### External Dependencies
- **Joda Time 2.10.14**: Date and time manipulation
- **Log4j 1.2.17**: Logging framework
- **JUnit 4.13.2**: Unit testing (test scope)

### Internal Dependencies
- **com.appcommon.application**: Application configuration utilities
- **com.forecast.application**: Forecast application utilities
- **bosc.commons.joda-time-utils**: Joda Time utilities
- **com.forecast.batchforecastexportservice**: Batch forecast export service client
- **com.forecast.vending.util**: Forecast vending utilities

## Configuration Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `fgidList` | String | Yes | - | Comma-separated list of Forecasting Group IDs |
| `probabilityValues` | String | Yes | - | Comma-separated probability values (e.g., "0.1,0.5,0.9") |
| `runDate` | Date | Yes | - | Date to run the forecast export |
| `datasetName` | String | Yes | - | Name of the dataset to export |
| `grainLength` | Enum | No | WEEK | Time granularity: DAY(1) or WEEK(7) |
| `seriesLength` | Integer | No | 53 | Number of time periods to export |
| `spanLength` | Integer | No | 1 | Length of each time span |
| `includeOverrides` | Boolean | No | true | Include forecast overrides |
| `includeSystemForecast` | Boolean | No | true | Include system-generated forecasts |
| `includeForecastMean` | Boolean | No | true | Include forecast mean values |
| `includeForecastVariance` | Boolean | No | true | Include forecast variance |
| `includePointForecast` | Boolean | No | true | Include point forecasts |
| `snapshotDate` | String | No | - | Specific snapshot date (ISO format) |
| `datasetDate` | String | No | - | Dataset date override |

## Building the Project

### Compile the project
```bash
mvn clean compile
```

### Run tests
```bash
mvn test
```

### Create executable JAR
```bash
mvn clean package
```

This creates two JAR files:
- `combined-edx-forecast-uploader-1.0.0.jar` (standard JAR)
- `combined-edx-forecast-uploader-1.0.0-jar-with-dependencies.jar` (executable JAR with all dependencies)

## Running the Application

### Using Maven Exec Plugin
```bash
mvn exec:java -Dexec.mainClass="com.forecast.vending.app.CombinedEDXForecastUploader" \
  -Dexec.args="--fgidList=12345,67890 --probabilityValues=0.1,0.5,0.9 --runDate=2024-01-01 --datasetName=my-dataset"
```

### Using Executable JAR
```bash
java -jar target/combined-edx-forecast-uploader-1.0.0-jar-with-dependencies.jar \
  --fgidList=12345,67890 \
  --probabilityValues=0.1,0.5,0.9 \
  --runDate=2024-01-01 \
  --datasetName=my-dataset
```

### Using Configuration File
```bash
java -jar target/combined-edx-forecast-uploader-1.0.0-jar-with-dependencies.jar \
  --config=config/application.properties
```

## Sample Configuration

Create a configuration file `config/application.properties`:

```properties
# Required Parameters
fgidList=12345,67890,54321
probabilityValues=0.1,0.25,0.5,0.75,0.9
runDate=2024-01-15
datasetName=weekly-forecast-export

# Optional Parameters
grainLength=WEEK
seriesLength=52
spanLength=1
includeOverrides=true
includeSystemForecast=true
includeForecastMean=true
includeForecastVariance=true
includePointForecast=true

# Snapshot Configuration (optional)
# snapshotDate=2024-01-15T00:00:00Z
# datasetDate=2024-01-15
```

## Output Information

The application logs the following information for each processed forecasting group:

- **Output Location**: S3 URI where the exported data is stored
- **Computation ID**: Unique identifier for the export computation
- **Forecast Vending ID**: Identifier for the forecast vending process
- **Snapshot Date**: Date of the forecast snapshot used

Example output:
```
INFO  Output location for EDX is: s3://forecast-exports/batch-123456/
INFO  The computation id is: comp-789012
INFO  The forecastVendingId is: fv-345678
INFO  The snapshot date is: 2024-01-15T00:00:00Z
```

## Error Handling

The application includes comprehensive error handling:

- **Configuration Errors**: Invalid parameters or missing required fields
- **Service Errors**: Issues communicating with forecast services
- **Data Errors**: Problems with forecast data or processing
- **Runtime Errors**: Unexpected exceptions during execution

All errors are logged with detailed stack traces for debugging.

## Development

### Code Structure

- **CombinedEDXForecastUploader**: Main application class
- **GrainType**: Enum for time granularity options
- **exportForecast()**: Core export logic
- **getDateRanges()**: Date range generation utility
- **getQuantilesList()**: Probability value parsing
- **getIntegerList()**: FGID list parsing

### Adding New Features

1. **New Configuration Parameters**: Add constants and update the config helper calls
2. **New Time Granularities**: Extend the GrainType enum
3. **Custom Date Logic**: Modify the getDateRanges() method
4. **Additional Output Formats**: Extend the request configuration

### Testing

Run unit tests with:
```bash
mvn test
```

For integration testing with actual Amazon services, ensure proper credentials and permissions are configured.

## Security Considerations

- **Credentials**: Ensure proper credentials are configured
- **Permissions**: Verify access to required forecast services and storage buckets
- **Data Sensitivity**: Handle forecast data according to data classification policies
- **Logging**: Avoid logging sensitive information in production

## Troubleshooting

### Common Issues

1. **Authentication Errors**: Verify credentials and permissions
2. **Service Unavailable**: Check service health and network connectivity
3. **Invalid Configuration**: Validate all required parameters are provided
4. **Date Format Errors**: Ensure dates are in ISO format (YYYY-MM-DD)

### Debug Mode

Enable debug logging by adding to JVM arguments:
```bash
-Dlog4j.configuration=file:config/log4j.properties -Dlog4j.debug=true
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is proprietary and subject to internal licensing terms.

## Support

For support and questions:
- Internal teams: Contact the Forecast Engineering team
- Documentation: Refer to internal forecast service documentation
- Issues: Create tickets in the appropriate internal tracking system
