import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import org.testng.annotations.*;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.lambdatest.tunnel.Tunnel;
import org.testng.annotations.Test;


public class SampleBM {


    public static RemoteWebDriver driver;
    public static BrowserMobProxy proxy;
    public static Tunnel t;
    public String username = "vishnurv2";
    public String accesskey = "2dEK8rin7XC3cChRqccFoMlcC9FwPNloQdqV9c4y7vFUAnAB18";
    public String gridURL = "@hub.lambdatest.com/wd/hub";

    public static String  portn;

    public static Proxy seleniumProxy;

    @BeforeSuite
    public void SampleBMSuite() throws Exception {

        if (proxy == null) {

            proxy = new BrowserMobProxyServer();
            proxy.setTrustAllServers(true);
            proxy.start();

            // start the proxy

            portn = String.valueOf(proxy.getPort());

            // get the Selenium proxy object

            seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            String hostIp = Inet4Address.getLocalHost().getHostAddress();
            seleniumProxy.setHttpProxy(hostIp + ":" + proxy.getPort());
            seleniumProxy.setSslProxy(hostIp + ":" + proxy.getPort());


            System.out.println(portn + " <-- BrowserMob Proxy port passed in tunnel");

            t = new Tunnel();
            HashMap<String, String> options = new HashMap<String, String>();
            options.put("user", username);
            options.put("key", accesskey);
            options.put("proxyHost", hostIp);
            options.put("proxyPort", portn);
            options.put("ingress-only", "--ingress-only");          //mandatory while using BM proxy
            options.put("tunnelName", portn);


            //start tunnel
            t.start(options);
        }
    }

    @org.testng.annotations.Parameters(value = {"browser", "version", "platform"})
    @BeforeTest
    public void setup(String browser, String version, String platform) throws Exception {

        System.out.println(seleniumProxy);

        ChromeOptions option = new ChromeOptions();

        option.addArguments("--ignore-certificate-errors");
        option.setProxy(seleniumProxy);
        option.setAcceptInsecureCerts(true);
        option.addArguments("--disable-backgrounding-occluded-windows");


        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, option);
        capabilities.setCapability("browserName", browser);
        capabilities.setCapability("version", version);
        capabilities.setCapability("platform", platform);
        capabilities.setCapability("build","BMProxy Sample");
        capabilities.setCapability("tunnel",true);
        capabilities.setCapability("tunnelName",portn);

        driver = new RemoteWebDriver(new URL("https://"+username+":"+accesskey+gridURL),capabilities);
    }

    @Test
    public void test() throws Exception {


        driver.manage().window().maximize();

        proxy.newHar("HomePage");

        driver.get("https://www.google.com/");
        driver.findElement(By.name("q")).sendKeys("LambdaTest");
        driver.findElement(By.name("q")).sendKeys(Keys.ENTER);
        Thread.sleep(5000);

        driver.get("https://lambdatest.com");

        // get the HAR data
        Thread.sleep(5000);
        Har har = proxy.getHar();

        try {
            har.writeTo(new File(proxy.getPort()+"homepage.har"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }


    }

    @AfterTest
    public void tearDown() throws Exception {

        if (driver != null) {

            try {
                driver.quit();              //quitting the driver instance
//                proxy.stop();               //stop proxy
//                t.stop();                   //stop tunnel
            }catch(Exception e){
                System.out.println(e);
            }

        }
    }



}
