package org.wilana.washy;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WashyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String UPLOAD = "/upload";
    private static final String GETDATA = "/getdata";
    private static final int MAX_DATAPOINTS = 12 * 60 * 4;
    private static final int MAX_DP_VAL = 104;

    private static final Map<Date, List<Integer>> data = new LinkedHashMap<Date, List<Integer>>(
            MAX_DATAPOINTS + 1, 1.0f, false) {
        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(
                final Map.Entry<Date, List<Integer>> eldest) {
            return super.size() > MAX_DATAPOINTS;
        }
    };

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        System.out.println(new Date() + ": " + pathInfo);
        if (pathInfo.startsWith(UPLOAD)) {
            collectDatapoint(pathInfo);
        } else if (pathInfo.startsWith(GETDATA)) {
            resp.setContentType("application/json");
            resp.getWriter().write(formatData());
            resp.getWriter().flush();
        }
    }

    // "["
    // "[\"Alfred Hitchcock (1935)\", 8.4, 7.9, 8.4, 7.9, 8.4, 7.9, 8.4, 7.9],"
    // "[\"FOO\", 8.4, 7.9, 8.4, 7.9, 8.4, 7.9, 8.4, 7.9],"
    // "[\"BAR\", 8.4, 7.9, 8.4, 7.9, 8.4, 7.9, 8.4, 7.9]"
    // "]"
    DateFormat format = new SimpleDateFormat("MMM d, h:mm a");

    private String formatData() {
        List<Date> dates = new ArrayList<Date>(data.keySet());
        Collections.sort(dates);
        String ret = "[";
        int i = 0;
        for (Date d : dates) {
            if(i++ > 0) {
                ret += ",";
            }
            String key;
            key = format.format(d);
            ret += "[\"" + key + "\"";
            for(int dp : data.get(d)) {
                ret += "," + dp + "," + (MAX_DP_VAL - dp);
            }
            ret += "]";
        }
        ret += "]";
        return ret;
    }

    private void collectDatapoint(String pathInfo) {
        String[] parts = pathInfo.split(",");
        List<Integer> dp = new ArrayList<Integer>();
        for (int i = 1; i < 5; i++) {
            dp.add(Integer.parseInt(parts[i]));
        }
        data.put(new Date(), dp);
    }

}
