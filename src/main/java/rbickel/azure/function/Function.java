package rbickel.azure.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/HttpExample 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     * 
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        String path = System.getenv("RootPath");
        if (path == null) {
            path = "C:/Projects/jmeter/apache-jmeter-5.2.1";
        }

        JMeterUtils.loadJMeterProperties(path + "/bin/jmeter.properties");
        JMeterUtils.setJMeterHome(path);

        JMeterUtils.initLocale();

        try {
            // Initialize JMeter SaveService
            SaveService.loadProperties();

            // Load existing .jmx Test Plan
            File file = new File(path + "/bing.jmx");
            HashTree testPlanTree = SaveService.loadTree(file);

            testPlanTree = JMeter.convertSubTree(testPlanTree, true);
            StandardJMeterEngine engine = new StandardJMeterEngine();
            engine.configure(testPlanTree);
            engine.run();
        } catch (IOException e) {
            e.printStackTrace();
            context.getLogger().severe(e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return request.createResponseBuilder(HttpStatus.OK).build();
    }
}
