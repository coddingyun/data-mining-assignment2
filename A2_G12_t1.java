import java.io.*;
import java.util.*;

public class A2_G12_t1 {
    static class Point {
        String id;
        double x;
        double y;
        int cluster;

        Point(String id, double x, double y) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.cluster = -1;
        }
    }

    public static void main(String[] args) throws IOException {
        String inputFile = args[0];
        int k = args.length > 1 ? Integer.parseInt(args[1]) : 0;

        List<Point> points = readPoints(inputFile);

        if (k == 0) {
            k = estimateK(points);
            System.out.println("estimated k: " + k);
        }

        List<List<Point>> clusters = kMeans(points, k);

        printing(clusters);
    }

    private static List<Point> readPoints(String inputFile) throws IOException {
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                String id = tokens[0];
                double x = Double.parseDouble(tokens[1]);
                double y = Double.parseDouble(tokens[2]);
                points.add(new Point(id, x, y));
            }
        }
        return points;
    }
    // Silhouette method
    private static int estimateK(List<Point> points) {
        int maxK = 20;
        double bestSilhouette = Double.NEGATIVE_INFINITY;
        int bestK = 2;

        for (int k = 2; k <= maxK; k++) {
            List<List<Point>> clusters = kMeans(points, k);
            double silhouette = silhouette(clusters);
            if (silhouette > bestSilhouette) {
                bestSilhouette = silhouette;
                bestK = k;
            }
        }

        return bestK;
    }

    private static double silhouette(List<List<Point>> clusters) {
        double totalScore = 0.0;
        int totalPoints = 0;

        for (List<Point> cluster : clusters) {
            for (Point point : cluster) {
                double score = perPoint(point, clusters);
                totalScore += score;
                totalPoints++;
            }
        }

        return totalScore / totalPoints;
    }

    private static double perPoint(Point point, List<List<Point>> clusters) {
        List<Point> ownCluster = clusters.get(point.cluster);
        double avg = avgDist(point, ownCluster);

        double cur = Double.MAX_VALUE;
        for (int i = 0; i < clusters.size(); i++) {
            if (i != point.cluster && clusters.get(i).size() > 0) {
                double distance = avgDist(point, clusters.get(i));
                if (distance < cur) {
                    cur = distance;
                }
            }
        }

        return (cur - avg) / Math.max(avg, cur);
    }

    private static double avgDist(Point point, List<Point> cluster) {
        double totdist = 0.0;
        for (Point other : cluster) {
            if (!point.equals(other)) {
                totdist += Math.sqrt(Math.pow(point.x - other.x, 2) + Math.pow(point.y - other.y, 2));
            }
        }
        return totdist / (cluster.size() - 1);
    }

    private static List<List<Point>> kMeans(List<Point> points, int k) {
        List<Point> centroids = init(points, k);
        boolean converged = false;
        int maxiter = 1000;
        int iter = 0;

        while (!converged && iter < maxiter) {
            pointToCluster(points, centroids);
            List<Point> newCentroids = newCentroid(points, k);
            converged = centroids.equals(newCentroids);
            centroids = newCentroids;
            iter++;
        }
        return getCluster(points, k);
    }

    private static List<Point> init(List<Point> points, int k) {
        List<Point> centroids = new ArrayList<>();
        Random random = new Random();
        centroids.add(points.get(random.nextInt(points.size())));

        while (centroids.size() < k) {
            double[] distance = new double[points.size()];
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                double minDist = Double.MAX_VALUE;
                for (Point centroid : centroids) {
                    double dist = Math.pow(p.x - centroid.x, 2) + Math.pow(p.y - centroid.y, 2);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                distance[i] = minDist;
            }

            /* Calculate distance-proportional probability */
            double totalDist = Arrays.stream(distance).sum();
            double rand = random.nextDouble() * totalDist;
            double cumulativeDist = 0;
            for (int i = 0; i < points.size(); i++) {
                cumulativeDist += distance[i];
                if (cumulativeDist >= rand) {
                    centroids.add(points.get(i));
                    break;
                }
            }
        }

        return centroids;
    }

    private static void pointToCluster(List<Point> points, List<Point> centroids) {
        for (Point p : points) {
            double minDist = Double.MAX_VALUE;
            int cluster = -1;
            for (int i = 0; i < centroids.size(); i++) {
                Point centroid = centroids.get(i);
                double dist = Math.sqrt(Math.pow(p.x - centroid.x, 2) + Math.pow(p.y - centroid.y, 2));
                if (dist < minDist) {
                    minDist = dist;
                    cluster = i;
                }
            }
            p.cluster = cluster;
        }
    }

    private static List<Point> newCentroid(List<Point> points, int k) {
        Point[] newCentroids = new Point[k];
        int[] counts = new int[k];
        for (int i = 0; i < k; i++) {
            newCentroids[i] = new Point("", 0, 0);
        }

        for (Point p : points) {
            newCentroids[p.cluster].x += p.x;
            newCentroids[p.cluster].y += p.y;
            counts[p.cluster]++;
        }

        for (int i = 0; i < k; i++) {
            if (counts[i] > 0) {
                newCentroids[i].x /= counts[i];
                newCentroids[i].y /= counts[i];
            } else {
                newCentroids[i] = points.get(new Random().nextInt(points.size()));
            }
        }
        return Arrays.asList(newCentroids);
    }

    private static List<List<Point>> getCluster(List<Point> points, int k) {
        List<List<Point>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new ArrayList<>());
        }
        for (Point p : points) {
            clusters.get(p.cluster).add(p);
        }
        return clusters;
    }

    private static void printing(List<List<Point>> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("Cluster #" + (i + 1) + " => ");
            for (Point p : clusters.get(i)) {
                System.out.print(p.id + " ");
            }
            System.out.println();
        }
    }
}