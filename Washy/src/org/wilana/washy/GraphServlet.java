package org.wilana.washy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphServlet extends HttpServlet {
    private static final long serialVersionUID = 1;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log(req.getPathInfo());
        String name = req.getPathInfo().replaceFirst("\\/", "");
        String graphFile = RrdArchive.instance().graphDetail(name);
        InputStream input = new FileInputStream(graphFile);
        resp.setContentType("image/png");
        ServletOutputStream output = resp.getOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }
}
