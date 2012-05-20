package org.wilana.washy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WashyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String pathInfo = req.getPathInfo();
            log(pathInfo);
            RrdArchive.instance().record(pathInfo.replaceFirst("\\/", ""));
        } catch (Exception e) {
            System.out.println("UH OH!!!  Arduino might stop uploading as a result of this!");
            e.printStackTrace();
        }
    }
}
