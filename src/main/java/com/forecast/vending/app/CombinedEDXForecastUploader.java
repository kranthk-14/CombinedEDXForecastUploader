package com.forecast.vending.app;

import static com.forecast.vending.util.ServicesHelper.getBatchClient;
import com.appcommon.application.ConfigProvider;
import com.appcommon.application.Initializer;
import com.forecast.application.ConfigHelper;
import bosc.commons.joda.time.JodaTimeUtils;

import java.util.ArrayList;
import java.util.List;

import com.forecast.batchforecastexportservice.ForecastReference;
import com.forecast.batchforecastexportservice.ForecastSnapshotDate;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.forecast.batchforecastexportservice.ExportForecastRequest;
import com.forecast.batchforecastexportservice.ExportForecastResult;
import com.forecast.batchforecastexportservice.ISO8601DateRange;

public class CombinedEDXForecastUploader {
    final private static Logger logger = Logger.getLogger(CombinedEDXForecastUploader.class);

    private static final String FgidList = "fgidList";
    private static final String ProbabilityValues = "probabilityValues";
    private static final String IncludeOverrides = "includeOverrides";
    private static final String IncludeSystemForecast = "includeSystemForecast";
    private static final String IncludeForecastMean = "includeForecastMean";
    private static final String IncludeForecastVariance = "includeForecastVariance";
    private static final String IncludePointForecast = "includePointForecast";
    private static final String DatasetName = "datasetName";
    private static final String DatasetDate = "datasetDate";
    private static final String RunDate = "runDate";
    private static final String SeriesLength = "seriesLength";
    private static final String GrainLength = "grainLength";
    private static final String SpanLength = "spanLength";
    private static final String SnapshotDate = "snapshotDate";    
    private static final int DEFAULT_SPAN_LENGTH = 1;

    private enum GrainType {
        DAY(1),
        WEEK(7);

        private int grainLength;
        GrainType(int grainLength) {
            this.grainLength = grainLength;
        }

        private int getGrainLength() {
            return grainLength;
        }
    }

    private static void exportForecast(final ConfigProvider configProvider) throws Exception {
        final ConfigHelper config = new ConfigHelper(configProvider);

        final LocalDate runDate = config.getDate(RunDate);
        final List<Integer> fgList = getIntegerList(config);

        final String probabilityValues = config.getString(ProbabilityValues);
        final Boolean includeOverrides = config.getBoolean(IncludeOverrides, true);
        final Boolean includeSystemForecast = config.getBoolean(IncludeSystemForecast, true);
        final Boolean includeForecastMean = config.getBoolean(IncludeForecastMean, true);
        final Boolean includeForecastVariance = config.getBoolean(IncludeForecastVariance, true);
        final Boolean includePointForecast = config.getBoolean(IncludePointForecast, true);
        final String datasetName = config.getString(DatasetName);

        final GrainType grainType = config.getEnum(GrainType.class, GrainLength, GrainType.WEEK);
        final Integer seriesLength = config.getInt(SeriesLength, 53);
        final Integer spanLength = config.getInt(SpanLength, DEFAULT_SPAN_LENGTH);
        final String snapshotDate = config.getString(SnapshotDate, "");

        List<ISO8601DateRange> dateRanges;
        if (grainType.equals(GrainType.DAY)) {
            dateRanges = getDateRanges(runDate, seriesLength, grainType.getGrainLength(), grainType.getGrainLength());
        }
   
        else {
            dateRanges = getDateRanges(JodaTimeUtils.sundayFloor(runDate), seriesLength,  grainType.getGrainLength(), grainType.getGrainLength()* spanLength);
        }

        List<Double> quantiles = getQuantilesList(probabilityValues);
        for (Integer forecastingGroupId : fgList) {

            final ExportForecastRequest requestInput = new ExportForecastRequest();
            requestInput.setForecastingGroupId(forecastingGroupId);
            requestInput.setForecastDateRanges(dateRanges);
            requestInput.setProbabilityValues(quantiles);
            requestInput.setIncludeOverrides(includeOverrides);
            requestInput.setIncludeSystemForecast(includeSystemForecast);
            requestInput.setIncludeForecastMean(includeForecastMean);
            requestInput.setIncludeForecastVariance(includeForecastVariance);
            requestInput.setIncludePointForecast(includePointForecast);
            requestInput.setDatasetName(datasetName);


            if (config.contains(SnapshotDate)) {
            	requestInput.setForecastReference(ForecastSnapshotDate.builder().withSnapshotDate(snapshotDate).build());
            }

            if (config.contains(DatasetDate)) {
                requestInput.setDatasetDate(config.getString(DatasetDate));
            }

            ExportForecastResult result = getBatchClient().newExportForecastCall().call(requestInput);

            logger.info("Output location for EDX is: " + result.getOutput().getUri());
            logger.info("The computation id is: " + result.getComputationId());
            logger.info("The forecastVendingId is: " + result.getForecastVendingId().getVendingId());
            logger.info("The snapshot date is: " + result.getForecastSnapshotDate().getSnapshotDate());

        }
    }

    /**
     * Generate Date Ranges that start from startDate, and take in seriesLength, grainLength and  spanLength
     * @param startDate
     * @param seriesLength
     * @param grainLength
     * @param spanLength
     * @return 
     */
    private static List<ISO8601DateRange> getDateRanges(LocalDate startDate, Integer seriesLength, Integer grainLength, Integer spanLength) {
        List<ISO8601DateRange> dateRangesList = new ArrayList<>();
        LocalDate endDate;
        for(int i = 0; i < seriesLength; i++) {
            endDate = startDate.plusDays(spanLength);

            ISO8601DateRange dateRange = new ISO8601DateRange();
            dateRange.setStartDate(startDate.toString());
            dateRange.setEndDate(endDate.toString());

            dateRangesList.add(dateRange);
           	startDate = startDate.plusDays(grainLength);
        }
        return dateRangesList;
    }

    /**
     * @param startDate
     * @param seriesLength
     * @param grainLength
     * @return
     */


    private static List<Double> getQuantilesList(String probabilityValues) {
        String[] quantiles = probabilityValues.split(",");
        List<Double> quantilesList = new ArrayList<Double>();
        for(String value: quantiles) {
            quantilesList.add(Double.parseDouble(value));
        }
        return quantilesList;
    }

    /**
     * Parse List of FGIDs from CLI Argument
     * @param config
     * @return
     */
    private static List<Integer> getIntegerList(ConfigHelper config) {
        String[] fgidArray = config.getString(FgidList).split(",");
        List<Integer> fgidList = new ArrayList<>();
        for (String fg : fgidArray) {
            fgidList.add(Integer.parseInt(fg));
        }
        return fgidList;
    }

    public static void main(final String[] args) throws Exception {
        try {
            final ConfigProvider configProvider =
                    Initializer.initializeMain("CombinedEDXForecastUploader", "ForecastTransformer", args);

            exportForecast(configProvider);
        } catch (final Throwable e) {
            logger.error("CombinedEDXForecastUploader::main exception", e);

            throw new RuntimeException(e);
        }
    }
}
