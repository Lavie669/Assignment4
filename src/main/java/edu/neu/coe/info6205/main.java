package edu.neu.coe.info6205;

import edu.neu.coe.info6205.union_find.UF_HWQUPC;
import edu.neu.coe.info6205.union_find.WQUPC;
import edu.neu.coe.info6205.union_find.WQUPC_ALT;
import edu.neu.coe.info6205.util.Benchmark;
import edu.neu.coe.info6205.util.Benchmark_Timer;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.Destination;
import tech.tablesaw.io.csv.CsvWriter;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {
    private static final List<Integer> X = new ArrayList<>();
    private static final List<Double> Y = new ArrayList<>();
    private static final List<Double> T = new ArrayList<>();
    private static final List<Integer> N = new ArrayList<>();
    private static final List<String> categories = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Please enter an integer number bigger than 0 or just enter 0 to do Benchmark:");
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (str.equals("0")){
            System.out.println("Start Benchmark:");
            doBenchmark();
        }
        else if(!isNum.matches() || str.isEmpty()){
            System.out.println("Illegal input!!!");
        }
        else {
            int n = Integer.parseInt(str);
            System.out.printf("Start UF_HWQUPC with %d sites:\n", n);
            System.out.println("Connections: " + count(n));
        }
    }

    public static int count(int n){
        UF_HWQUPC client = new UF_HWQUPC(n);
        int count = 0;
        Random random = new Random();
        //Loop until all sites are connected (number of components becomes 1)
        while (client.components() != 1){
            int p = random.nextInt(n);
            int q = random.nextInt(n);
            if (p!=q) {
                if (!client.isConnected(p, q)) {
                    client.union(p, q);
                    count++;
                }
            }
        }
        client.show();
        return count;
    }


    public static double countOG(int n){
        WQUPC client = new WQUPC(n);
        Integer[] xs = new Integer[n];
        Benchmark<Integer[]> timer = new Benchmark_Timer<>("WQUPC experiments", b->{
            client.doConnect(n);
        }, null);
        double time = timer.run(xs, 1000);
        return time;
    }

    public static double countALT(int n){
        WQUPC_ALT client = new WQUPC_ALT(n);
        Integer[] xs = new Integer[n];
        Benchmark<Integer[]> timer = new Benchmark_Timer<>("WQUPC_ALT experiments", b->{
            client.doConnect(n);
        }, null);
        double time = timer.run(xs, 1000);
        return time;
    }

    public static void doBenchmark() throws IOException {
        int e = 1;
        for (int i = 64; i <= 65536; i *= 2){
            double t1 = countOG(i);
            X.add((int) log2(i));
            Y.add(log2(t1));
            T.add(t1);
            categories.add("Original");
            N.add(i);
            double t2 = countALT(i);
            X.add((int) log2(i));
            Y.add(log2(t2));
            T.add(t2);
            categories.add("Alternative");
            N.add(i);
            System.out.printf("Experiment %d: %d objects — OG: %f s vs. ALT: %f s\n", e, i, t1, t2);
            e++;
            if (i==65536) break;
        }
        plotChart();
        System.out.println("Experiments done!!!");
    }

    public static double log2(double n){
        return Math.log(n)/Math.log(2);
    }

    public static void plotChart() throws IOException {
        Table table = createTable(X, Y, N, T, categories);
        CsvWriter writer = new CsvWriter();
        File file = new File("Results.csv");
        Destination destination = new Destination(file);
        writer.write(table, destination);
        Plot.show(LinePlot.create("Log-log plot", table, "lg(N)", "lg(T)", "categories"));
    }

    public static Table createTable(List<Integer> x, List<Double> y, List<Integer> n, List<Double> t, List<String> categories){
        Integer [] x_column = new Integer[x.size()];
        Double [] y_column = new Double[y.size()];
        Integer [] n_column = new Integer[n.size()];
        Double [] t_column = new Double[t.size()];
        String [] groups = new String[categories.size()];
        IntColumn lgN = IntColumn.create("lg(N)", x.toArray(x_column));
        DoubleColumn lgT = DoubleColumn.create("lg(T)", y.toArray(y_column));
        IntColumn Ns = IntColumn.create("N", n.toArray(n_column));
        DoubleColumn T = DoubleColumn.create("T", t.toArray(t_column));
        StringColumn group = StringColumn.create("categories", categories.toArray(groups));
        return Table.create(lgN, lgT, T, Ns, group);
    }
}
