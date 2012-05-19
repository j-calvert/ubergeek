package org.wilana.washy;

import java.io.File;
import java.io.IOException;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;

public class RrdArchive {

    private final String filename;

    public RrdArchive(String filename) throws IOException {
        this.filename = filename;
        File file = new File(filename);
        System.out.println("RRD File: " + file.getCanonicalPath()); 
        if (!file.exists()) {
            RrdDef rrdDef = new RrdDef(filename);
            rrdDef.addDatasource("washer1", DsType.GAUGE, 120, Double.NaN,
                    Double.NaN);
            rrdDef.addDatasource("washer2", DsType.GAUGE, 120, Double.NaN,
                    Double.NaN);
            rrdDef.addDatasource("dryer1", DsType.GAUGE, 120, Double.NaN,
                    Double.NaN);
            rrdDef.addDatasource("dryer2", DsType.GAUGE, 120, Double.NaN,
                    Double.NaN);
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, 24);
            rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 6, 10);
            new RrdDb(rrdDef).close();
        }
    }
    
    public void record(String data) throws IOException {
        RrdDb rrdDb = new RrdDb(filename);
        Sample s = rrdDb.createSample();
        s.setAndUpdate("NOW:" + data.replaceAll(",", ":"));
        rrdDb.close();
    }
    
    public String dump() throws IOException {
        long now = System.currentTimeMillis() / 1000;
        RrdDb rrdDb = new RrdDb(filename);
        FetchRequest fetchRequest = rrdDb.createFetchRequest(ConsolFun.AVERAGE, now - 1000, now);
        FetchData fetchData = fetchRequest.fetchData();
        String ret = fetchData.dump();
        rrdDb.close();
        return ret;
    }

}
