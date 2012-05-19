package org.wilana.washy;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WashyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String UPLOAD = "/upload";

    private RrdArchive archive;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            archive = new RrdArchive("./washy.rrd");
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        log(new Date() + ": " + pathInfo);
        if (pathInfo.startsWith(UPLOAD)) {
            archive.record(pathInfo.replaceFirst("\\" + UPLOAD + ",", ""));
        } else {
            resp.getWriter().write(archive.dump());
            resp.getWriter().flush();
        }
    }
}
