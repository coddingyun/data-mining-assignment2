import java.io.*;
import java.util.*;

public class A2_G2_t2 {
    private static List<Point> points = new ArrayList<>();
    private static double epsilon = 0.5;
    private static int minPts = 4;

    public static void main(String[] args) throws IOException {
        String filename = args[0];
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
                System.out.println("Estimated eps: " + epsilon);
            }
        }

        readCSV(filename);
        Map<Integer, List<Point>> clusters = dbscan();

        printResults(clusters);
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

    private static void expandCluster(Point point, List<Point> neighbors, List<Point> cluster, int clusterId) {
        cluster.add(point);
        point.clusterId = clusterId;

        int index = 0;
        while (index < neighbors.size()) {
            Point neighbor = neighbors.get(index);

            if (!neighbor.visited) {
                neighbor.visited = true;
                List<Point> newNeighbors = regionQuery(neighbor);

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
      System.out.println("Number of clusters: " + clusters.size());
      System.out.println("Number of noise: " + noiseCount);

      for (Map.Entry<Integer, List<Point>> entry : clusters.entrySet()) {
          System.out.print("Cluster #" + entry.getKey() + " => ");
          List<Point> sortedPoints = entry.getValue();
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
