# Kowalski985
###### \java\seedu\address\ui\AutocompleteCommandTest.java
``` java
public class AutocompleteCommandTest {
    @Test
    public void getInstance() throws Exception {
        assertEquals(AutocompleteCommand.ADD, AutocompleteCommand.getInstance("add"));
        assertEquals(AutocompleteCommand.CLEAR, AutocompleteCommand.getInstance("clear"));
        assertEquals(AutocompleteCommand.DELETE, AutocompleteCommand.getInstance("delete"));
        assertEquals(AutocompleteCommand.EDIT, AutocompleteCommand.getInstance("edit"));
        assertEquals(AutocompleteCommand.EXIT, AutocompleteCommand.getInstance("exit"));
        assertEquals(AutocompleteCommand.FIND, AutocompleteCommand.getInstance("find"));
        assertEquals(AutocompleteCommand.HELP, AutocompleteCommand.getInstance("help"));
        assertEquals(AutocompleteCommand.HISTORY, AutocompleteCommand.getInstance("history"));
        assertEquals(AutocompleteCommand.IMPORT, AutocompleteCommand.getInstance("import"));
        assertEquals(AutocompleteCommand.LIST, AutocompleteCommand.getInstance("list"));
        assertEquals(AutocompleteCommand.NONE, AutocompleteCommand.getInstance("none"));
        assertEquals(AutocompleteCommand.REDO, AutocompleteCommand.getInstance("redo"));
        assertEquals(AutocompleteCommand.SELECT, AutocompleteCommand.getInstance("select"));
        assertEquals(AutocompleteCommand.TAB, AutocompleteCommand.getInstance("tab"));
        assertEquals(AutocompleteCommand.UNDO, AutocompleteCommand.getInstance("undo"));
    }

    @Test
    public void hasIndexParameter() throws Exception {
        assertTrue(AutocompleteCommand.hasIndexParameter("edit"));
        assertTrue(AutocompleteCommand.hasIndexParameter("delete"));
        assertFalse(AutocompleteCommand.hasIndexParameter("add"));
        assertFalse(AutocompleteCommand.hasIndexParameter("all123"));
        assertFalse(AutocompleteCommand.hasIndexParameter(null));
    }

    @Test
    public void hasPrefixParameter() throws Exception {
        assertTrue(AutocompleteCommand.hasPrefixParameter("edit"));
        assertTrue(AutocompleteCommand.hasPrefixParameter("add"));
        assertFalse(AutocompleteCommand.hasPrefixParameter("delete"));
        assertFalse(AutocompleteCommand.hasPrefixParameter("all123"));
        assertFalse(AutocompleteCommand.hasPrefixParameter(null));
    }
}
```
###### \java\seedu\address\ui\AutocompleterTest.java
``` java
public class AutocompleterTest extends GuiUnitTest {

    private static final String EMPTY_STRING = "";
    private static final String ADD_COMMAND_WORD = "add";
    private static final String EDIT_COMMAND_WORD = "edit";
    private static final String EXIT_COMMAND_WORD = "exit";
    private static final String MULTIPLE_RESULTS_MESSAGE = "Multiple matches found:" + "\n" + "edit" + "\t" + "exit";
    private static final String PROMPT_USER_TO_USE_HELP_MESSAGE = "To see what commands are available, type 'help' "
            + "into the command box";
    private static final String CURRENT_STATE_LOG_MESSAGE = "INFO - Current state of the autocompleter is now ";
    private static final String CURRENT_COMMAND_LOG_MESSAGE = "INFO - Current command recognized by"
            + " the autocompleter is now ";


    private ResultDisplayHandle resultDisplayHandle;
    private Autocompleter autocompleter;

    @Before
    public void setUp() throws Exception {
        ResultDisplay resultDisplay = new ResultDisplay();
        uiPartRule.setUiPart(resultDisplay);

        resultDisplayHandle = new ResultDisplayHandle(getChildNode(resultDisplay.getRoot(),
                ResultDisplayHandle.RESULT_DISPLAY_ID));
        Model model = new ModelManager();
        Logic logic = new LogicManager(model);

        model.addParcel(AMY);
        model.addParcel(IDA);
        autocompleter = new Autocompleter(logic);

    }

    @Test
    public void autocomplete_forNoIndexesOrPrefixes() throws Exception {
        autocompleter.updateAutocompleter("li");
        String autocompleteResult = autocompleter.autocomplete();
        assertEquals("list", autocompleteResult);
        guiRobot.pauseForEvent();
        assertEquals(EMPTY_STRING, resultDisplayHandle.getText());

    }

    @Test
    public void autocomplete_forCommand() throws Exception {
        // default result text
        guiRobot.pauseForEvent();
        assertEquals(EMPTY_STRING, resultDisplayHandle.getText());

        // autocomplete with empty string
        assertAutocompleteSuccess(EMPTY_STRING, EMPTY_STRING, PROMPT_USER_TO_USE_HELP_MESSAGE,
                "NONE", "EMPTY");

        // lowercase autocomplete with only one autocomplete option
        assertAutocompleteSuccess("a", ADD_COMMAND_WORD, EMPTY_STRING,
                "NONE", "COMMAND");

        // uppercase autocomplete with only one autocomplete option
        assertAutocompleteSuccess("A", ADD_COMMAND_WORD, EMPTY_STRING,
                "NONE", "COMMAND");

        // mix uppercase and lowercase autocomplete with only one autocomplete option
        assertAutocompleteSuccess("Ed", EDIT_COMMAND_WORD, EMPTY_STRING,
                "NONE", "COMMAND");

        // lowercase autocomplete with multiple autocomplete options
        assertAutocompleteSuccess("e", EDIT_COMMAND_WORD, MULTIPLE_RESULTS_MESSAGE,
                "NONE", "MULTIPLE_COMMAND");

        // uppercase autocomplete with multiple autocomplete options
        autocompleter.updateAutocompleter("");
        assertAutocompleteSuccess("E", EDIT_COMMAND_WORD, MULTIPLE_RESULTS_MESSAGE,
                "NONE", "MULTIPLE_COMMAND");

        // lowercase autocomplete with multiple options and cycling
        autocompleter.updateAutocompleter("");
        assertAutocompleteSuccess("E", EDIT_COMMAND_WORD, MULTIPLE_RESULTS_MESSAGE,
                "NONE", "MULTIPLE_COMMAND");
        assertAutocompleteSuccess(EDIT_COMMAND_WORD, EXIT_COMMAND_WORD, MULTIPLE_RESULTS_MESSAGE,
                "EDIT", "MULTIPLE_COMMAND");

        // autocomplete with no possible options
        assertAutocompleteSuccess("Z", "Z", EMPTY_STRING,
                "NONE", "NO_RESULT");
    }

    @Test
    public void autocomplete_forPrefixesOnly() throws Exception {

        // autocomplete prefix with first letter of prefix filled in
        assertAutocompleteSuccess("add #", "add #/", EMPTY_STRING,
                "ADD", "COMMAND_COMPLETE_PREFIX");

        // autocomplete first prefix after command word
        assertAutocompleteSuccess("add", "add #/", EMPTY_STRING,
                "ADD", "COMMAND_NEXT_PREFIX");

        // autocomplete second prefix
        assertAutocompleteSuccess("add #/RR123456789SG",
                "add #/RR123456789SG n/", EMPTY_STRING,
                "ADD", "COMMAND_NEXT_PREFIX");

        // autocomplete cycle first prefix
        assertAutocompleteSuccess("add #/", "add n/", EMPTY_STRING,
                "ADD", "COMMAND_CYCLE_PREFIX");

        // autocomplete cycle second prefix
        assertAutocompleteSuccess("add #/RR123456789SG",
                "add #/RR123456789SG n/", EMPTY_STRING,
                "ADD", "COMMAND_NEXT_PREFIX");
        assertAutocompleteSuccess("add #/RR123456789SG n/",
                "add #/RR123456789SG a/", EMPTY_STRING,
                "ADD", "COMMAND_CYCLE_PREFIX");
    }

    @Test
    public void autocomplete_forIndexesOnly() throws Exception {
        // autocomplete index after command
        assertAutocompleteSuccess("select", "select 1", EMPTY_STRING,
                "SELECT", "INDEX");

        // autocomplete cycle to next index after command
        assertAutocompleteSuccess("select 1", "select 2", EMPTY_STRING,
                "SELECT", "INDEX");

        // autocomplete cycle wrap around
        assertAutocompleteSuccess("select 2", "select 1", EMPTY_STRING,
                "SELECT", "INDEX");

        // autocomplete when letters are entered into the command box
        assertAutocompleteSuccess("select abc", "select 2", EMPTY_STRING,
                "SELECT", "INDEX");

        // autocomplete when mix of letters and numbers are entered into command box
        assertAutocompleteSuccess("select abc123", "select 1", EMPTY_STRING,
                "SELECT", "INDEX");
    }

    @Test
    public void autocomplete_forIndexesAndPrefixes() throws Exception {
        // autocomplete first index after command
        assertAutocompleteSuccess(EDIT_COMMAND_WORD, "edit 1", EMPTY_STRING,
                "EDIT", "INDEX");

        // autocomplete number out of range of index
        assertAutocompleteSuccess("edit 999", "edit 2", EMPTY_STRING,
                "EDIT", "INDEX");

        // autocomplete with letters instead and no index
        assertAutocompleteSuccess("edit abc", "edit 1", EMPTY_STRING,
                "EDIT", "INDEX");

        // autocomplete with prefix without
        assertAutocompleteSuccess("edit #/", "edit 2", EMPTY_STRING,
                "EDIT", "INDEX");

        // autocomplete with prefix and parameters without index
        assertAutocompleteSuccess("edit #/RR123456789SG", "edit 1", EMPTY_STRING,
                "EDIT", "INDEX");

        // cycle to next index
        assertAutocompleteSuccess("edit 1", "edit 2", EMPTY_STRING,
                "EDIT", "INDEX");

        // fill in prefix
        assertAutocompleteSuccess("edit 2 ", "edit 2 #/", EMPTY_STRING,
                "EDIT", "COMMAND_NEXT_PREFIX");

        // cycle to next prefix
        assertAutocompleteSuccess("edit 2 #/", "edit 2 n/", EMPTY_STRING,
                "EDIT", "COMMAND_CYCLE_PREFIX");

        // move to next prefix
        assertAutocompleteSuccess("edit 2 #/RR123456789SG", "edit 2 #/RR123456789SG n/",
                EMPTY_STRING, "EDIT", "COMMAND_NEXT_PREFIX");
    }

    @Test
    public void autocomplete_forFindAndImport() throws Exception {
        // autocomplete should do nothing if arguments are already filled in for find
        assertAutocompleteSuccess("find abcd", "find abcd", EMPTY_STRING,
                "FIND", "COMMAND");

        // autocomplete should do nothing if arguments are already filled in for import
        assertAutocompleteSuccess("import fileName", "import fileName", EMPTY_STRING,
                "IMPORT", "COMMAND");

    }

    @Test
    public void autocomplete_forMultipleTag() throws Exception {
        assertAutocompleteSuccess("add #/RR123456789SG n/John a/123 Computing S123456 d/today"
                        + " e/john@example.com p/12345678 s/PENDING t/FROZEN", "add #/RR123456789SG"
                        + " n/John a/123 Computing S123456 d/today e/john@example.com p/12345678 s/PENDING t/FROZEN t/",
                EMPTY_STRING, "ADD", "COMMAND_NEXT_PREFIX");
    }

    /**
     * Simulates an autocomplete with {@code commandBoxText} and verifies that the resulting string matches
     * {@code expectedResult} also verifies that the logged details match {@code expectedCommand}
     * and {@code expectedState}
     */
    private void assertAutocompleteSuccess(String commandBoxText, String expectedResult, String expectedMessage,
                   String expectedCommand, String expectedState) throws DataConversionException, IOException {
        TestLogger testLogger = new TestLogger(autocompleter.getClass(), Level.INFO);
        autocompleter.updateAutocompleter(commandBoxText);
        String autocompleteResult = autocompleter.autocomplete();
        assertEquals(expectedResult, autocompleteResult);
        guiRobot.pauseForEvent();
        assertEquals(expectedMessage, resultDisplayHandle.getText());
        String capturedLog = testLogger.getTestCapturedLog();
        String expectedLogMessage = CURRENT_STATE_LOG_MESSAGE + expectedState + "\n"
                + CURRENT_COMMAND_LOG_MESSAGE + expectedCommand + "\n";
        assertEquals(capturedLog, expectedLogMessage);
    }
}
```
###### \java\seedu\address\ui\CommandBoxTest.java
``` java
    @Test
    public void tabAutoCompleteTest_withNoMatchingCommands() {
        // text field is empty
        assertInputHistory(KeyCode.TAB, "");

        // one letter in text field
        guiRobot.push(KeyCode.Y);
        assertInputHistory(KeyCode.TAB, "y");

        // two letters in text field
        guiRobot.push(KeyCode.T);
        assertInputHistory(KeyCode.TAB, "yt");

        // current text in text field is longer than some commands
        guiRobot.push(KeyCode.Y);
        guiRobot.push(KeyCode.SPACE);
        guiRobot.push(KeyCode.Y);
        assertInputHistory(KeyCode.TAB, "yty y");
    }

    @Test
    public void tabAutoCompleteTest_withOneMatchingCommand() {
        // text in text filed is in lowercase
        guiRobot.push(KeyCode.L);
        assertInputHistory(KeyCode.TAB, COMMAND_THAT_SUCCEEDS);
        guiRobot.push(KeyCode.ENTER);

        // text in text filed is in uppercase
        guiRobot.push(new KeyCodeCombination(KeyCode.L, KeyCombination.SHIFT_DOWN));
        assertInputHistory(KeyCode.TAB, COMMAND_THAT_SUCCEEDS);
        guiRobot.push(KeyCode.ENTER);

        // text in text filed is in mix of uppercase and lowercase
        guiRobot.push(new KeyCodeCombination(KeyCode.E, KeyCombination.SHIFT_DOWN));
        guiRobot.push(KeyCode.D);
        assertInputHistory(KeyCode.TAB, "edit");
    }

    @Test
    public void tabAutoCompleteTest_withMultipleMatchingCommands() {
        guiRobot.push(KeyCode.E);
        assertInputHistory(KeyCode.TAB, "edit");
    }
```
