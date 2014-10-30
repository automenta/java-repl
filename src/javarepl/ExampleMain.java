package javarepl;

import com.googlecode.totallylazy.Mapper;
import com.googlecode.totallylazy.Option;
import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Sequences;
import javarepl.client.EvaluationResult;
import javarepl.client.JavaREPLClient;
import javarepl.completion.CompletionCandidate;
import javarepl.completion.CompletionResult;
import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.CompletionHandler;
import jline.console.history.MemoryHistory;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.*;

import static com.googlecode.totallylazy.Callables.compose;
import static com.googlecode.totallylazy.Option.none;
import static com.googlecode.totallylazy.Option.some;
import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.Strings.replaceAll;
import static com.googlecode.totallylazy.Strings.startsWith;
import static com.googlecode.totallylazy.numbers.Numbers.intValue;
import static com.googlecode.totallylazy.numbers.Numbers.valueOf;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static javarepl.Utils.applicationVersion;
import static javarepl.Utils.randomServerPort;
import static javarepl.completion.CompletionCandidate.functions.candidateForms;
import static javarepl.completion.CompletionCandidate.functions.candidateValue;
import static javarepl.completion.CompletionResult.methods.fromJson;
import static javarepl.completion.CompletionResult.methods.toJson;
import static javax.tools.ToolProvider.getSystemJavaCompiler;

public class ExampleMain {

    public static void main(String... args) throws Exception {
        Main.process = none();
        Main.console = new ResultPrinter(printColors(args));

        ConsoleConfig config = consoleConfig()                
                //.historyFile(new File(getProperty("user.home"), ".javarepl-embedded.history"))
                .commands(
                        ListValues.class,
                        ShowHistory.class,
                        EvaluateFromHistory.class,
                        SearchHistory.class)                
                .results(
                        result("date", new Date()),
                        result("num", 42));

        int port = 8001;
        new RestConsole(new SimpleConsole(config), port);
        
        JavaREPLClient client = new JavaREPLClient("localhost", 8001);
                
        //JavaREPLClient client = clientFor(hostname(args), port(args));
        ExpressionReader expressionReader = Main.expressionReaderFor("java", client);

        Option<String> expression = none();
        Option<EvaluationResult> result = none();
        while (expression.isEmpty() || !result.isEmpty()) {
            expression = expressionReader.readExpression();

            if (!expression.isEmpty()) {
                result = client.execute(expression.get());
                if (!result.isEmpty())
                    Main.console.printEvaluationResult(result.get());
            }
        }
    }
}
    
