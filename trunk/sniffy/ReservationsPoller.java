import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class ReservationsPoller {

    private static final String RESERVATION_PAGE = "http://www.recreation.gov/camping/Clear_Lake_Cabin_Lookout_Or/r/campsiteDetails.do?arvdate=12/29/2009&contractCode=NRSO&parkId=75097&siteId=146537&lengthOfStay=2";
    private static final String BASE_URL = "http://www.recreation.gov/";
    private static final String BROWSER_START = "*chrome /usr/lib/firefox-3.0.11/firefox";
    private static final String OUT = "/home/calverje/clearLake.txt";
    private static PrintWriter pw;

    public static void main(String[] args) throws Exception {
        pw = new PrintWriter(new FileWriter(OUT));
        SeleniumServer server = new SeleniumServer();
        server.start();
        Selenium selenium = new DefaultSelenium("localhost", 4444, BROWSER_START, BASE_URL);
        selenium.start();
        while (true) {
            selenium.open(RESERVATION_PAGE);
            String[] statuses = new String[14];
            for (int i = 0; i < 14; i++) {
                statuses[i] = selenium.getText("avail" + (i + 1));
            }
            printStatuses(statuses);
            Thread.sleep(1000 * 60 * 60);
        }
    }

    private static void printStatuses(String[] statuses) {
        pw.print(new Date());
        for (String s : statuses) {
            pw.print(", " + s);
        }
        pw.println();
        pw.flush();

    }
}
