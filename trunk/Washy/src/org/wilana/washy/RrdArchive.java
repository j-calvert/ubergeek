package org.wilana.washy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

public class RrdArchive {
    
    private static RrdArchive instance;
    
    public static synchronized RrdArchive instance() {
        if(instance == null) {
            try {
                instance = new RrdArchive(new File("."), "washy.rrd");
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private final File workingDir;
    private final File rrdFile;
    
    private static final String[] devices = new String[] { "washer1", "washer2", "dryer1", "dryer2" };

    public RrdArchive(File workingDir, String filename) throws IOException {
        if(!workingDir.exists()) {
            workingDir.mkdir();
        }
        this.workingDir = workingDir;
        this.rrdFile = new File(workingDir, filename);
        File file = new File(filename);
        System.out.println("RRD File: " + file.getCanonicalPath());
        if (!file.exists()) {
            RrdDef rrdDef = new RrdDef(filename);
            for (String device : devices) {
                rrdDef.addDatasource(device, DsType.GAUGE, 120, Double.NaN, Double.NaN);
            }
            // Step = 5 seconds
            rrdDef.setStep(5l);
            // 6 hours of every 5 seconds sample
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 4320);
            // 7 days of every minute sample ave
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 12, 10080);
            // 7 days of every minute sample min
            rrdDef.addArchive(ConsolFun.MIN, 0.5, 12, 10080);
            // 7 days of every minute sample max
            rrdDef.addArchive(ConsolFun.MAX, 0.5, 12, 10080);
            new RrdDb(rrdDef).close();
        }
    }

    public void record(String data) throws IOException {
        RrdDb rrdDb = new RrdDb(rrdFile.getCanonicalPath());
        Sample s = rrdDb.createSample();
        s.setAndUpdate("NOW:" + data.replaceAll(",", ":"));
        rrdDb.close();
    }

    public String dump() throws IOException {
        long now = System.currentTimeMillis() / 1000;
        RrdDb rrdDb = new RrdDb(rrdFile.getCanonicalPath());
        FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.AVERAGE, now - 5000, now);
        FetchData fetchData = fetchRequest.fetchData();
        String ret = fetchData.dump();
        rrdDb.close();
        return ret;
    }

    public synchronized String graphDetail(String name) throws IOException {
        String imageFilename = new File(workingDir, name + "Detail.png").getCanonicalPath();
        File file = new File(imageFilename);
        long now = System.currentTimeMillis() / 1000;
        if (file.exists() && file.lastModified() / 1000 > now - 5) {
            return imageFilename; // Don't recreate files not older than 5 seconds
        }
        System.out.println("recreating " + name + " graph");
        RrdGraphDef graphDef = new RrdGraphDef();
        long then = now - 60 * 60 * 1; // Now minus 1 hour ago
        graphDef.setTimeSpan(then, now);
        graphDef.datasource(name, rrdFile.getCanonicalPath(), name, ConsolFun.AVERAGE);
        graphDef.line(name, new Color(0xFF, 0, 0), null, 2);
        graphDef.setTitle(name);
        graphDef.setFilename(imageFilename);
        RrdGraph graph = new RrdGraph(graphDef);
        BufferedImage bi = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        graph.render(bi.getGraphics());
        return imageFilename;
    }
}
