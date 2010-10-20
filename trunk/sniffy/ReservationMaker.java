import java.util.Date;

import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

public class ReservationMaker {

    private final String ARRIVE_DATE = "01/01/2010";
    private final String RESERVATION_PAGE = "http://www.recreation.gov/camping/Clear_Lake_Cabin_Lookout_Or/r/campsiteDetails.do?arvdate="
            + ARRIVE_DATE + "&contractCode=NRSO&parkId=75097&siteId=146537&lengthOfStay=2";
    private final String RG_BASE_URL = "http://www.recreation.gov/";
    private final String SW_BASE_URL = "http://swaggle.mobi/";
    private final String BROWSER_START = "*chrome /usr/lib/firefox-3.5.9/firefox";
    private final Selenium rgSelenium;
    private final Selenium swSelenium;
    private final SeleniumServer server;

    public static void main(String[] args) throws Exception {
        ReservationMaker rm = new ReservationMaker();
        rm.makeReservation();

    }

    public ReservationMaker() throws Exception {
        server = new SeleniumServer();
        server.start();
        rgSelenium = new DefaultSelenium("localhost", 4444, BROWSER_START, RG_BASE_URL);
        swSelenium = new DefaultSelenium("localhost", 4444, BROWSER_START, SW_BASE_URL);

    }

    private void makeReservation() {
        rgSelenium.start();
        rgSelenium.open(RESERVATION_PAGE);
        swSelenium.start();
        swSelenium.open("http://swaggle.mobi/groups/1578");
        while (true) {
            try {
                System.out.println(new Date() + ": checking");
                if(rgSelenium.isElementPresent("btnbookdates")) {
                	rgSelenium.click("btnbookdates");
                	rgSelenium.waitForPageToLoad("30000");
                } else {
                	rgSelenium.open(RESERVATION_PAGE);
                }
                if (isSuccess()) {
                    break;
                }
                sleepSome();
            } catch (Exception e) {
                e.printStackTrace();
                sleepSome();
            }
        }
        reportSuccess();
    }

    private boolean isSuccess(){
    	return rgSelenium.isTextPresent("Member Sign In");
    }
    
    private void sleepSome() {
        try {
            Thread.sleep(1000 * (45 + (int) (Math.random() * 30)));
        } catch (InterruptedException e) {
        }
    }

    private void reportSuccess() {
        System.out.println("=================================================================");
        System.out.println("GOT IT!  Leave this procees running and complete the reservation!");
        sendSMS("http://tinyurl.com/n7m7y5");
//        new Alarm().start();
        try {
            Thread.sleep(1000 * 60 * 15);
        } catch (InterruptedException e) {
        }
    }

    private void sendSMS(String string) {
        swSelenium.open("http://swaggle.mobi/groups/1578");
        swSelenium.waitForPageToLoad("30000");
        swSelenium.type("message", string);
        swSelenium.click("//input[@name='commit' and @value='Send']");
        swSelenium.waitForPageToLoad("30000");
    }
}
