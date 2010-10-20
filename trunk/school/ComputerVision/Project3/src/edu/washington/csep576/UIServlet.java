package edu.washington.csep576;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.DatabaseException;

public class UIServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String query = null;
	
	private Sort sort = Sort.COLOR;
	
	private Regions.RegionsComparator scorer = new Regions.RegionsComparator();
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		parseFields(request.getQueryString());
		try {
			if(query != null){
				ImageSearch.setScores(query, sort);
			}
		} catch (DatabaseException e) {
			throw new ServletException(e);
		}
		StringBuffer buf = new StringBuffer();
		header(buf);
		Collections.sort(ImageSearch.regions, scorer);
		printSortOptions(buf);
		printResults(buf, ImageSearch.regions);
		footer(buf);
		response.getOutputStream().print(buf + "");
	}
	

	private void parseFields(String queryString){
		query = null;
		sort = Sort.COLOR;
		if(queryString == null){ return; }
		String[] fields = queryString.split("&");
		for(String field : fields){
			String[] parts = field.split("=");
			if(parts.length == 2){
				if(parts[0].equalsIgnoreCase("q")){
					query = parts[1];
				}
				if(parts[0].equalsIgnoreCase("s")){
					sort = Sort.fromString(parts[1]);
				}
			}
		}
	}

	private static void header(StringBuffer buf) {
		buf
				.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		buf
				.append("<html><head><title>Image Search -- Jeremy Calvert -- CSEP576</title>");
		buf.append("</head><body>");
		buf.append("\n");
	}
	
	private void printSortOptions(StringBuffer buf){
		buf.append("<br>Sort method:<br>");
		buf.append("<table><tr>");
		for(Sort s : Sort.values()){
			buf.append("<th>");
			if(s == sort){
				buf.append(s.toString());
			} else {
				buf.append("<a href=\"/ui?s=" + s.toString());
				if(query != null){
					buf.append("&q=" + query);
				}
				buf.append("\">" + s.toString() + "</a>");
			}
			buf.append("</th>");
		}
		buf.append("</tr></table>");
	}

	/**
	 * At this point we expect the regions representing the images to have been
	 * assigned their score relative to the search image.
	 */
	private void printResults(StringBuffer buf, List<Regions> regionses){
		int width = 5;
		int i = 0;
		buf.append("<br>Click an image to query by that image<br>");
		buf.append("<table><tr>");
		for(Regions regions : regionses){
			if(i++ % width == 0){
				buf.append("</tr>");
				buf.append("\n");
				buf.append("<tr>");
			}
			buf.append("<td><a href=\"/ui?q=" + regions.name + "&s=" + sort.toString() + "\"><img src=\"" + Util.thumbFromKey(regions.name) + "\"></a>");
			if(regions.score > 0){
				buf.append("<br>" + Util.formatScore(regions.score));
			}
			buf.append("</td>");
		}
		buf.append("</tr></table>");
	}

	private static void footer(StringBuffer buf) {
		buf.append("</body></html>");
	}

}
