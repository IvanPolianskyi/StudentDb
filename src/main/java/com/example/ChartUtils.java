package com.example;

import org.knowm.xchart.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.knowm.xchart.style.Styler;
public class ChartUtils {

    public static void main(String[] args) throws IOException {
        List<ResultRecord> records = loadResults("results.csv");

        Files.createDirectories(Paths.get("charts"));

        plotOps(records, "Ops_Top100", "Top 100", "ops_top100.png");
        plotOps(records, "Ops_SetRating", "Set Rating", "ops_setrating.png");
        plotOps(records, "Ops_BestGroup", "Best Group", "ops_bestgroup.png");

        plotMemory(records);
        plotSorting(records);

    }

    private static List<ResultRecord> loadResults(String fileName) throws IOException {
        List<ResultRecord> records = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(fileName));

        for (int i = 1; i < lines.size(); i++) {
            String[] s = lines.get(i).split(",");
            records.add(new ResultRecord(
                    s[0], Integer.parseInt(s[1]),
                    Integer.parseInt(s[2]), Integer.parseInt(s[3]),
                    Integer.parseInt(s[4]), Double.parseDouble(s[5]),
                    Double.parseDouble(s[6]), Double.parseDouble(s[7])
            ));
        }
        return records;
    }

    private static void plotOps(List<ResultRecord> records, String field, String title, String fileName) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(850).height(600)
                .title("Операція: " + title)
                .xAxisTitle("Розмір бази")
                .yAxisTitle("Кількість операцій")
                .build();

        for (String variant : getVariants(records)) {
            List<ResultRecord> r = filter(records, variant);
            chart.addSeries(variant, getSizes(r), log10(getField(r, field)));
        }

        chart.getStyler().setYAxisLogarithmic(true);
        style(chart);
        BitmapEncoder.saveBitmap(chart, "charts/" + fileName, BitmapEncoder.BitmapFormat.PNG);
    }

    private static void plotMemory(List<ResultRecord> records) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(850).height(600)
                .title("Використання памʼяті")
                .xAxisTitle("Розмір бази")
                .yAxisTitle("Памʼять")
                .build();

        for (String variant : getVariants(records)) {
            List<ResultRecord> r = filter(records, variant);
            chart.addSeries(variant, getSizes(r), getMemory(r));
        }

        style(chart);
        BitmapEncoder.saveBitmap(chart, "charts/memory.png", BitmapEncoder.BitmapFormat.PNG);
    }

    private static void plotSorting(List<ResultRecord> records) throws IOException {
        XYChart chart = new XYChartBuilder()
                .width(850).height(600)
                .title("Час сортування (мс)")
                .xAxisTitle("Розмір бази (рядків)")
                .yAxisTitle("Час (мс)")
                .build();

        for (String variant : getVariants(records)) {
            List<ResultRecord> r = filter(records, variant);
            chart.addSeries(variant + " - Default", getSizes(r), getSortDefault(r));
            chart.addSeries(variant + " - HeapSort", getSizes(r), getSortHeap(r));
        }

        style(chart);
        BitmapEncoder.saveBitmap(chart, "charts/sort_time.png", BitmapEncoder.BitmapFormat.PNG);
    }

    // ====== Styling ======
    private static void style(XYChart chart) {
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        chart.getStyler().setMarkerSize(6);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setChartFontColor(java.awt.Color.BLACK);
        chart.getStyler().setAxisTickLabelsColor(java.awt.Color.BLACK);
        chart.getStyler().setChartBackgroundColor(java.awt.Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(java.awt.Color.WHITE);
    }

    // ===== Helpers =====
    private static List<String> getVariants(List<ResultRecord> r) {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (ResultRecord x : r) s.add(x.variant);
        return new ArrayList<>(s);
    }

    private static List<ResultRecord> filter(List<ResultRecord> r, String v) {
        List<ResultRecord> a = new ArrayList<>();
        for (ResultRecord x : r) if (x.variant.equals(v)) a.add(x);
        return a;
    }

    private static double[] getSizes(List<ResultRecord> r) {
        return r.stream().mapToDouble(x -> x.size).toArray();
    }

    private static double[] getField(List<ResultRecord> r, String field) {
        return r.stream().mapToDouble(x -> switch (field) {
            case "Ops_Top100" -> x.ops1;
            case "Ops_SetRating" -> x.ops2;
            case "Ops_BestGroup" -> x.ops3;
            default -> 0;
        }).toArray();
    }

    private static double[] getMemory(List<ResultRecord> r) {
        return r.stream().mapToDouble(x -> x.memoryMB).toArray();
    }

    private static double[] getSortDefault(List<ResultRecord> r) {
        return r.stream().mapToDouble(x -> x.sortDefaultMs).toArray();
    }

    private static double[] getSortHeap(List<ResultRecord> r) {
        return r.stream().mapToDouble(x -> x.sortHeapMs).toArray();
    }

    private static double[] log10(double[] arr) {
        return Arrays.stream(arr).map(Math::log10).toArray();
    }

    private record ResultRecord(String variant, int size, int ops1, int ops2, int ops3,
                                double memoryMB, double sortDefaultMs, double sortHeapMs) {}
}
