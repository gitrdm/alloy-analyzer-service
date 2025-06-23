package AlloyAnalyzerService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

class DummyCommand {
    String label;
    String str;
    DummyCommand(String label, String str) {
        this.label = label;
        this.str = str;
    }
    @Override
    public String toString() { return str; }
    public String getLabel() { return label; }
}

public class AlloyAnalyzerServiceTest {
    @Test
    void testServiceInitialization() {
        AlloyAnalyzerService service = new AlloyAnalyzerService();
        assertNotNull(service);
    }

    @Test
    void testFindMatchingLabelCommand() {
        List<DummyCommand> commands = new ArrayList<>();
        commands.add(new DummyCommand("ShowTrace", "run ShowTrace for 5 Service, 4 Step, 3 Request, 3 Result"));
        commands.add(new DummyCommand("IsNeverStuck", "check IsNeverStuck for 5 Service, 4 Step, 3 Request, 3 Result"));
        assertNotNull(AlloyAnalyzerService.findMatchingLabelCommand(commands, "ShowTrace", DummyCommand::getLabel));
        assertNotNull(AlloyAnalyzerService.findMatchingLabelCommand(commands, "run ShowTrace", DummyCommand::getLabel));
        assertNotNull(AlloyAnalyzerService.findMatchingLabelCommand(commands, "check IsNeverStuck", DummyCommand::getLabel));
        assertNull(AlloyAnalyzerService.findMatchingLabelCommand(commands, "NonExistent", DummyCommand::getLabel));
    }

    // Add more unit tests for core logic as needed
}
