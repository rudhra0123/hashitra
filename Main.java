import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Step 1: Read the whole JSON file into a String
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader("input.json"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }
            }
            String json = sb.toString();

            // Step 2: Extract all points (x, y)
            List<double[]> points = new ArrayList<>();
            json = json.substring(1, json.length() - 1); // remove outer { }

            String[] entries = json.split("\\},");
            for (String entry : entries) {
                entry = entry.trim();
                if (entry.startsWith("\"keys\"")) continue;

                String[] keyValue = entry.split(":", 2);
                String keyStr = keyValue[0].replaceAll("[^0-9]", "");
                if (keyStr.isEmpty()) continue;

                int x = Integer.parseInt(keyStr);

                String baseStr = entry.replaceAll(".*\"base\"\\s*:\\s*\"(\\d+)\".*", "$1");
                int base = Integer.parseInt(baseStr);

                String valueStr = entry.replaceAll(".*\"value\"\\s*:\\s*\"([0-9a-zA-Z]+)\".*", "$1");
                long y = Long.parseLong(valueStr, base);

                points.add(new double[]{x, (double) y});
            }

            System.out.println("Decoded points: " + Arrays.deepToString(points.toArray()));

            // Step 3: Lagrange interpolation
            double[] coeffs = lagrangeInterpolation(points);
            long secretC = Math.round(coeffs[0]);

            System.out.println("Polynomial coefficients: " + Arrays.toString(coeffs));
            System.out.println("Secret (c): " + secretC);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lagrange interpolation to find polynomial coefficients
    public static double[] lagrangeInterpolation(List<double[]> points) {
        int n = points.size();
        double[] coeffs = new double[n];
        Arrays.fill(coeffs, 0);

        for (int i = 0; i < n; i++) {
            double[] term = {1};
            double denom = 1;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    term = multiplyPoly(term, new double[]{-points.get(j)[0], 1});
                    denom *= (points.get(i)[0] - points.get(j)[0]);
                }
            }
            double factor = points.get(i)[1] / denom;
            for (int k = 0; k < term.length; k++) {
                term[k] *= factor;
            }
            coeffs = addPoly(coeffs, term);
        }
        return coeffs;
    }

    public static double[] multiplyPoly(double[] a, double[] b) {
        double[] res = new double[a.length + b.length - 1];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                res[i + j] += a[i] * b[j];
            }
        }
        return res;
    }

    public static double[] addPoly(double[] a, double[] b) {
        int len = Math.max(a.length, b.length);
        double[] res = new double[len];
        for (int i = 0; i < len; i++) {
            double av = i < a.length ? a[i] : 0;
            double bv = i < b.length ? b[i] : 0;
            res[i] = av + bv;
        }
        return res;
    }
}
