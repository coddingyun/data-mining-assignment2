import java.io.*;
import java.util.*;

public class A2_G2_t2 {
    private static List<Point> points = new ArrayList<>();
    private static double epsilon;
    private static int minPts = 4; // 2차원 데이터의 경우 4가 적절, (minPts = dim*2)

    public static void main(String[] args) throws IOException {
        String filename = args[0];
        readCSV(filename);

        if (args.length == 3) {
            if (args[1].contains(".")) {
                epsilon = Double.parseDouble(args[1]);
                minPts = Integer.parseInt(args[2]);
            } else {
                minPts = Integer.parseInt(args[1]);
                epsilon = Double.parseDouble(args[2]);
            }
        } else {
            if (args[1].contains(".")) { // 소수점일 경우 epsilon
                epsilon = Double.parseDouble(args[1]);
                System.out.println("Estimated MinPts: " + minPts);
            } else {
                minPts = Integer.parseInt(args[1]); // 아닐 경우 minPts
                epsilon = estimateEpsilon(); // epsilon 추정 실험, 아래의 두 함수중 하나만 주석 제거 후 실험
                System.out.println("Estimated eps: " + epsilon);
            }
        }

        Map<Integer, List<Point>> clusters = dbscan();

        printResults(clusters);
    }

    // k-distance Estimation으로 epsilon 추정
    // private static double estimateEpsilon() {
    //     int k = minPts;

    //     double[] distances = new double[points.size()];

    //     for (int i = 0; i < points.size(); i++) {
    //         Point point = points.get(i);
    //         double[] pointDistances = new double[points.size() - 1];
    //         int index = 0;
    //         for (int j = 0; j < points.size(); j++) {
    //             if (i != j) {
    //                 pointDistances[index++] = distance(point, points.get(j));
    //             }
    //         }
    //         Arrays.sort(pointDistances);
    //         distances[i] = pointDistances[k - 1];
    //     }

    //     Arrays.sort(distances);
    //     return distances[k - 1];
    // }

  // Elbow Method로 epsilon 추정
  private static double estimateEpsilon() {
        int k = minPts;

        double[] distances = new double[points.size() * (points.size() - 1) / 2];
        int index = 0;

        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                distances[index++] = distance(point, points.get(j));
            }
        }

        Arrays.sort(distances);

        // Elbow method
        double maxSlope = Double.MIN_VALUE;
        int valleyIndex = 0;
        for (int i = k; i < distances.length - k; i++) {
            double slope = (distances[i + k] - distances[i]) / k;
            if (slope < maxSlope) {
                maxSlope = slope;
                valleyIndex = i;
            }
        }

        double epsilon = distances[valleyIndex];

        return epsilon;
    }

    private static void readCSV(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                points.add(new Point(values[0], Double.parseDouble(values[1]), Double.parseDouble(values[2])));
            }
        }
    }

    private static Map<Integer, List<Point>> dbscan() {
        int clusterId = 0;
        Map<Integer, List<Point>> clusters = new HashMap<>();

        for (Point point : points) {
            if (point.visited) continue;

            point.visited = true;
            List<Point> neighbors = regionQuery(point);

            // minPts값보다 작을 경우 noise
            if (neighbors.size() < minPts) {
                point.noise = true;
                continue; 
            }

            clusterId++;
            List<Point> cluster = new ArrayList<>();
            clusters.put(clusterId, cluster);
            expandCluster(point, neighbors, cluster, clusterId);
        }

        return clusters;
    }

    // 군집된 neighbors를 대상으로 regionQuery 반복
    private static void expandCluster(Point point, List<Point> neighbors, List<Point> cluster, int clusterId) {
        cluster.add(point);
        point.clusterId = clusterId;

        int index = 0;
        while (index < neighbors.size()) {
            Point neighbor = neighbors.get(index);

            if (!neighbor.visited) {
                neighbor.visited = true;
                List<Point> newNeighbors = regionQuery(neighbor);

                // minPts보다 같거나 클 경우 expand
                if (newNeighbors.size() >= minPts) {
                    neighbors.addAll(newNeighbors);
                }
            }

            if (neighbor.clusterId == 0) {
                neighbor.clusterId = clusterId;
                cluster.add(neighbor);
            }

            index++;
        }
    }

    // 최소 epsilon 이내 최소 neighbors 수를 만족하는 군집 찾기
    private static List<Point> regionQuery(Point point) {
        List<Point> neighbors = new ArrayList<>();

        for (Point p : points) {
            if (distance(point, p) <= epsilon) {
                neighbors.add(p);
            }
        }

        return neighbors;
    }

    private static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    private static void printResults(Map<Integer, List<Point>> clusters) {
        int noiseCount = 0;
        for (Point point : points) {
            if (point.noise && point.clusterId == 0) {
                noiseCount++;
            }
        }
        System.out.println("Number of clusters : " + clusters.size());
        System.out.println("Number of noise : " + noiseCount);

        for (Map.Entry<Integer, List<Point>> entry : clusters.entrySet()) {
            System.out.print("Cluster #" + entry.getKey() + " => ");
            List<Point> sortedPoints = entry.getValue();
            // 오름차순 정렬
            Collections.sort(sortedPoints, new Comparator<Point>() {
                @Override
                public int compare(Point p1, Point p2) {
                    int num1 = Integer.parseInt(p1.id.substring(1));
                    int num2 = Integer.parseInt(p2.id.substring(1));
                    return Integer.compare(num1, num2);
                }
            });
            for (Point p : sortedPoints) {
                System.out.print(p.id + " ");
            }
            System.out.println();
        }
    }

    static class Point {
        String id;
        double x, y;
        boolean visited = false;
        boolean noise = false;
        int clusterId = 0;

        Point(String id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }
}
