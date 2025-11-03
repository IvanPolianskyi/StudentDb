package com.example;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final String[] VARIANTS = {
            "ArrayList+HashMap",
            "LinkedList+HashMap",
            "ArrayList+TreeMap"
    };

    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final int TEST_DURATION_MS = 10_000;
    private static final int[] WEIGHTS = {100, 10, 50};
    public static void main(String[] args) throws IOException {
        

        try (FileWriter csv = new FileWriter("results.csv")) {
            csv.write("Variant,Size,Ops_Top100,Ops_SetRating,Ops_BestGroup,Memory_MB,Sort_Default_ms,Sort_Heap_ms\n");

            for (String variant : VARIANTS) {
                for (int size : SIZES) {
                    String fileName = "students_" + size + ".csv";
                    ensureDatasetOfSize(fileName, size);

                   

                    StudentDatabase db = new StudentDatabase(variant);
                    db.loadFromCSV(fileName);
                    Map<String, Object> res = runTest(db);

                    csv.write(String.format(
                            "%s,%d,%d,%d,%d,%.2f,%.3f,%.3f\n",
                            variant,
                            size,
                            res.get("ops1"),
                            res.get("ops2"),
                            res.get("ops3"),
                            res.get("memoryMB"),
                            res.get("sortDefaultMs"),
                            res.get("sortHeapMs")
                    ));
                }
            }
        }

    }

    private static Map<String, Object> runTest(StudentDatabase db) {
        int ops1 = 0, ops2 = 0, ops3 = 0;
        int total = Arrays.stream(WEIGHTS).sum();
        Random random = new Random();

        List<String> emails = db.getAll().stream().map(s -> s.email).toList();

        long endTime = System.currentTimeMillis() + TEST_DURATION_MS;

        while (System.currentTimeMillis() < endTime) {
            int rnd = random.nextInt(total);
            if (rnd < WEIGHTS[0]) {
                db.getTop100();
                ops1++;
            } else if (rnd < WEIGHTS[0] + WEIGHTS[1]) {
                if (!emails.isEmpty()) {
                    String email = emails.get(random.nextInt(emails.size()));
                    float newRating = random.nextFloat() * 100;
                    db.setRatingByEmail(email, newRating);
                }
                ops2++;
            } else {
                db.getGroupWithHighestAvg();
                ops3++;
            }
        }

        double memoryMB = getUsedMemoryMB();

        long t1 = System.nanoTime();
        db.sortByBirthDate();
        long t2 = System.nanoTime();
        db.heapSortByBirthDate();
        long t3 = System.nanoTime();

        return Map.of(
                "ops1", ops1,
                "ops2", ops2,
                "ops3", ops3,
                "memoryMB", memoryMB,
                "sortDefaultMs", (t2 - t1) / 1e6,
                "sortHeapMs", (t3 - t2) / 1e6
        );
    }

    private static double getUsedMemoryMB() {
        MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return mem.getUsed() / (1024.0 * 1024.0);
    }

    private static void ensureDatasetOfSize(String fileName, int size) throws IOException {
        if (Files.exists(Paths.get(fileName))) return;

        List<String> allLines = Files.readAllLines(Paths.get("students.csv"));
        List<String> subset = allLines.subList(0, Math.min(size + 1, allLines.size()));

        Files.write(Paths.get(fileName), subset);
    }
}
