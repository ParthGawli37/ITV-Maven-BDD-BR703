package hooks;

import base.BaseClass;
import io.cucumber.java.Before;
import io.cucumber.java.After;

public class Hooks {

    @Before
    public void setup() {
        System.out.println("Launching browser");
        BaseClass.initDriver();   // 🔥 THIS LINE WAS MISSING
    }

    @After
    public void teardown() {
        System.out.println("Closing browser");
        BaseClass.quitDriver();   // 🔥 also important
    }
}