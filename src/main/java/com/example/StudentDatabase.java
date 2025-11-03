package com.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class StudentDatabase{

     private List<Student> students;
    private Map<String, Student> emailMap;
    private final String variantName;

    public StudentDatabase(String variant) {
        this.variantName = variant;

        
        if (variant.contains("LinkedList")) {
            this.students = new LinkedList<>();
        } else {
            this.students = new ArrayList<>();
        }

        if (variant.contains("TreeMap")) {
            this.emailMap = new TreeMap<>();
        } else {
            this.emailMap = new HashMap<>();
        }
    }
    public void loadFromCSV(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] s = line.split(",");
                if (s.length < 9) continue;
                Student st = new Student();
                st.name = s[0];
                st.surname = s[1];
                st.email = s[2];
                st.birthYear = Integer.parseInt(s[3]);
                st.birthMonth = Integer.parseInt(s[4]);
                st.birthDay = Integer.parseInt(s[5]);
                st.group = s[6];
                st.rating = Float.parseFloat(s[7]);
                st.phoneNumber = s[8];
                students.add(st);
                emailMap.put(st.email, st);
            }
        }
    }

  
    public List<Student> getTop100() {
        return students.stream()
                .sorted((a, b) -> Float.compare(b.rating, a.rating))
                .limit(100)
                .collect(Collectors.toList());
    }

    public void setRatingByEmail(String email, float newRating) {
        Student st = emailMap.get(email);
        if (st != null) {
            st.rating = newRating;
        }
    }

    
    public String getGroupWithHighestAvg() {
        return students.stream()
                .collect(Collectors.groupingBy(s -> s.group,
                        Collectors.averagingDouble(s -> s.rating)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    
    public void sortByBirthDate() {
        students.sort(Comparator
                .comparingInt((Student s) -> s.birthMonth)
                .thenComparingInt(s -> s.birthDay));
    }

    public String getVariantName() {
        return variantName;
    }
    public void heapSortByBirthDate() {
        int n = students.size();

    
        Student[] arr = students.toArray(new Student[0]);

        
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i);
        }

        for (int i = n - 1; i > 0; i--) {
            Student temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

                heapify(arr, i, 0);
            }


            students.clear();
            students.addAll(Arrays.asList(arr));
        }

    private void heapify(Student[] arr, int heapSize, int rootIndex) {
        int largest = rootIndex;
        int left = 2 * rootIndex + 1;
        int right = 2 * rootIndex + 2;

        int rootKey = arr[largest].birthMonth * 100 + arr[largest].birthDay;

        if (left < heapSize) {
            int leftKey = arr[left].birthMonth * 100 + arr[left].birthDay;
            if (leftKey > rootKey) largest = left;
        }

        if (right < heapSize) {
            int rightKey = arr[right].birthMonth * 100 + arr[right].birthDay;
            int currLargestKey = arr[largest].birthMonth * 100 + arr[largest].birthDay;
            if (rightKey > currLargestKey) largest = right;
        }

        if (largest != rootIndex) {
            Student temp = arr[rootIndex];
            arr[rootIndex] = arr[largest];
            arr[largest] = temp;

            heapify(arr, heapSize, largest);
        }
    }
        
    public void saveToCSV(String fileName) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (Student s : students) {
                pw.printf("%s,%s,%s,%d,%d,%d,%s,%.2f,%s%n",
                        s.name, s.surname, s.email,
                        s.birthYear, s.birthMonth, s.birthDay,
                        s.group, s.rating, s.phoneNumber);
            }
        }
    }

    
    public List<Student> getAll() {
        return students;
    }
}
