package rabickel.azure.scheduledtest;

import java.io.File;
import java.io.IOException;
import java.time.*;
import com.microsoft.azure.functions.annotation.*;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Timer trigger.
 */
public class ScheduledTest {
    /**
     * This function will be invoked periodically according to the specified
     * schedule.
     * 
     * @throws IOException
     */
    @FunctionName("ScheduledTest")
    public void run(@TimerTrigger(name = "timerInfo", schedule = "0 */5 * * * *") String timerInfo,
            final ExecutionContext context) throws IOException {
        
        context.getLogger().info("Java Timer trigger function executed at: " + LocalDateTime.now());

        String path = System.getenv("RootPath");
        if (path == null) {
            path = "C:/Projects/jmeter/apache-jmeter-5.2.1";
        }

        context.getLogger().info("Load: " + path + "/bin/jmeter.properties");
        JMeterUtils.loadJMeterProperties(path + "/bin/jmeter.properties");
        context.getLogger().info("Set JMeter home: " + path);
        JMeterUtils.setJMeterHome(path);

        JMeterUtils.initLocale();

        try {
            // Initialize JMeter SaveService
            SaveService.loadProperties();

            String test = path + "/bing2.jmx";
            // Load existing .jmx Test Plan
            context.getLogger().info("Load file: " + test);
            File file = new File(test);
            HashTree testPlanTree = SaveService.loadTree(file);

            testPlanTree = JMeter.convertSubTree(testPlanTree, true);
            StandardJMeterEngine engine = new StandardJMeterEngine();
            context.getLogger().info("Configure test plan");
            engine.configure(testPlanTree);
            context.getLogger().info("Run : " + test);
            engine.run();
            context.getLogger().info("Runned");
        } catch (Exception e) {
            e.printStackTrace();
            context.getLogger().severe(e.getMessage());
        }
        context.getLogger().info("Complete");
    }
}
