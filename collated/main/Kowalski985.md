# Kowalski985
###### \java\seedu\address\ui\autocompleter\AutocompleteCommand.java
``` java
/**
 * Represents the current command that the autocompleter recognises in the {@code CommandBox}
 */
public enum AutocompleteCommand {
    ADD,
    CLEAR,
    DELETE,
    EDIT,
    EXIT,
    FIND,
    HELP,
    HISTORY,
    IMPORT,
    LIST,
    NONE,
    REDO,
    SELECT,
    TAB,
    UNDO;

    public static final String[] ALL_COMMANDS = {"add", "clear", "delete", "edit", "exit", "find", "help",
        "history", "import", "list", "redo", "select", "tab", "undo"};

    public static final Prefix[] ALL_PREFIXES = {PREFIX_TRACKING_NUMBER, PREFIX_NAME, PREFIX_ADDRESS,
        PREFIX_DELIVERY_DATE, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_STATUS, PREFIX_TAG};

    private static final String[] commandsWithIndexes = {"delete", "edit", "select"};

    private static final String[] commandsWithPrefixes = {"add", "edit"};

    public static AutocompleteCommand getInstance(String commandName) {
        requireNonNull(commandName);
        switch (commandName) {
        case AddCommand.COMMAND_WORD:
            return ADD;

        case ClearCommand.COMMAND_WORD:
            return CLEAR;

        case DeleteCommand.COMMAND_WORD:
            return DELETE;

        case EditCommand.COMMAND_WORD:
            return EDIT;

        case ExitCommand.COMMAND_WORD:
            return EXIT;

        case FindCommand.COMMAND_WORD:
            return FIND;

        case HelpCommand.COMMAND_WORD:
            return HELP;

        case HistoryCommand.COMMAND_WORD:
            return HISTORY;

        case ImportCommand.COMMAND_WORD:
            return IMPORT;

        case ListCommand.COMMAND_WORD:
            return LIST;

        case RedoCommand.COMMAND_WORD:
            return REDO;

        case SelectCommand.COMMAND_WORD:
            return SELECT;

        case TabCommand.COMMAND_WORD:
            return TAB;

        case UndoCommand.COMMAND_WORD:
            return UNDO;

        default:
            return NONE;
        }
    }

    /**
     * Returns true if {@code command} takes in an {@code Index} as an argument
     */
    public static boolean hasIndexParameter (String command) throws IllegalArgumentException {
        try {
            return Arrays.asList(commandsWithIndexes).contains(command);
        } catch (NullPointerException e) {
            return false;
        } catch (IllegalArgumentException ie) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns true if {@code command} takes in a {@code Prefix} as an argument
     */
    public static boolean hasPrefixParameter (String command) throws IllegalArgumentException {
        try {
            return Arrays.asList(commandsWithPrefixes).contains(command);
        } catch (NullPointerException e) {
            return false;
        } catch (IllegalArgumentException ie) {
            throw new IllegalArgumentException();
        }
    }


}
```
###### \java\seedu\address\ui\autocompleter\Autocompleter.java
``` java
/**
 * Autocomplete utility used in the command box
 */
public class Autocompleter {

    private static final String PROMPT_USER_TO_USE_HELP_MESSAGE = "To see what commands are available, type 'help' "
            + "into the command box";
    private static final String MULTIPLE_RESULT_MESSAGE = "Multiple matches found";
    private static final String EMPTY_STRING = "";
    private static final String SPACE = " ";
    private static final String PREFIX_INDICATOR = "/";
    private final Logger logger = LogsCenter.getLogger(this.getClass());

    private int resultIndex;
    private int countingIndex;
    private String textInCommandBox;
    private AutocompleteState state;
    private AutocompleteCommand currentCommand;
    private ArrayList<String> possibleAutocompleteResults;
    private CommandBoxParser parser;
    private Logic logic;


    public Autocompleter(Logic logic) {
        registerAsAnEventHandler(this);
        this.logic = logic;
        parser = new CommandBoxParser();
        resultIndex = 0;
        countingIndex = 1;
        textInCommandBox = EMPTY_STRING;
        state = AutocompleteState.EMPTY;
        currentCommand = AutocompleteCommand.NONE;
        possibleAutocompleteResults = new ArrayList<>();
    }

    /**
     * Returns a string that will replace the current text in the {@code CommandBox}
     * depending on the current state of the autocompleter. A {@code NewResultAvailableEvent}
     * will is also raised to update the text inside the {@code ResultDisplay}
     *
     * @return {@code String} that will replace text in teh {@code CommandBox}
     */
    public String autocomplete() {
        switch (state) {
        case COMMAND:
            clearResultsWindow();
            if (isImportOrFindCommand()) {
                return textInCommandBox;
            }
            return (possibleAutocompleteResults.isEmpty()) ? textInCommandBox : possibleAutocompleteResults.get(0);

        case EMPTY:
            raise(new NewResultAvailableEvent(PROMPT_USER_TO_USE_HELP_MESSAGE, false));
            return EMPTY_STRING;

        case COMMAND_NEXT_PREFIX:
            clearResultsWindow();
            if (possibleAutocompleteResults.isEmpty()) {
                return textInCommandBox;
            }
            return textInCommandBox.trim() + SPACE + possibleAutocompleteResults.get(0);

        case COMMAND_CYCLE_PREFIX:
            clearResultsWindow();
            return textInCommandBox.substring(0, textInCommandBox.length() - 2)
                    + possibleAutocompleteResults.get(cycleIndex());

        case COMMAND_COMPLETE_PREFIX:
            clearResultsWindow();
            return textInCommandBox + PREFIX_INDICATOR;

        case INDEX:
            clearResultsWindow();
            String[] temp = textInCommandBox.split(" ");
            return temp[0] + " " + cycleCountingIndex();

        case MULTIPLE_COMMAND:
            displayMultipleResults(possibleAutocompleteResults);
            return possibleAutocompleteResults.get(cycleIndex());

        case NO_RESULT: //fall through to default

        default:
            clearResultsWindow();
            return textInCommandBox;
        }
    }

    /**
     * Returns true if {@code currentCommand} is IMPORT or FIND
     */
    private boolean isImportOrFindCommand() {
        return currentCommand.equals(AutocompleteCommand.IMPORT)
                || currentCommand.equals(AutocompleteCommand.FIND);
    }

    /**
     * Returns the current value of resultIndex and then increment it by 1 with wrap-around
     *
     * @return current value of resultIndex
     */
    private int cycleIndex() {
        int currentIndex = resultIndex;
        resultIndex = (resultIndex + 1) % possibleAutocompleteResults.size();
        return currentIndex;

    }

    /**
     * Returns the current value of countingIndex, updates the value of maxIndex and then increments
     * countingIndex by 1 with wrap-around
     *
     * @return current value of countingIndex
     */
    private int cycleCountingIndex() {
        int currentIndex = countingIndex;
        int maxIndex = logic.getActiveList().size();
        countingIndex = (countingIndex + 1) % (maxIndex + 1);
        if (countingIndex == 0) {
            countingIndex = 1;
        }
        return currentIndex;
    }

    /**
     * Main entry point to the autocompleter package, updates textInCommandBox and calls {@code updateState}
     * to update the state of the autocompleter
     *
     * @param commandBoxText the current text inside the {@code CommandBox}
     */
    public void updateAutocompleter(String commandBoxText) {
        requireNonNull(commandBoxText);
        textInCommandBox = commandBoxText;
        updateState(commandBoxText);
        logger.info("Current state of the autocompleter is now " + state.toString());
        logger.info("Current command recognized by the autocompleter is now " + currentCommand.toString());
    }

    /**
     * Updates the {@code state} and {@code currentCommand} according their current values
     * as well as the current text inside the {@code commandBoxText}
     *
     * @param commandBoxText the current text inside the {@code CommandBox}
     */
    private void updateState(String commandBoxText) {
        if (commandBoxText.equals(EMPTY_STRING)) {
            state = AutocompleteState.EMPTY;
            return;
        }

        String[] currentTextArray = parser.parseCommandAndPrefixes(commandBoxText);
        String commandWord = currentTextArray[CommandBoxParser.COMMAND_INDEX];
        String arguments = currentTextArray[CommandBoxParser.ARGUMENT_INDEX];
        currentCommand = AutocompleteCommand.getInstance(commandWord);

        // First word does not match any commands
        if (currentCommand.equals(AutocompleteCommand.NONE)) {
            autocompleteCommandWord(commandBoxText);
            return;
        }

        // Autocompleter is currently cycling through possible commands
        if (isCyclingThroughCommands(commandWord)) {
            return;
        }

        // Current command word has an index parameter in its arguments
        if (AutocompleteCommand.hasIndexParameter(commandWord)
                && (!AutocompleteCommand.hasPrefixParameter(commandWord)
                || needIndex(arguments))) {
            resetCountingAndMaxIndexIfNeeded();
            state = AutocompleteState.INDEX;
            return;
        }

        // Current command word has a prefix parameter in its arguments
        if (AutocompleteCommand.hasPrefixParameter(commandWord)) {
            updateStateForCommandsWithPrefixes(commandBoxText, arguments);
            return;
        } else {
            state = AutocompleteState.COMMAND;
        }

    }

    /**
     * Updates the {@code state} of the autocompleter for {@code Command} that requires prefixes
     *
     * @param commandBoxText the current text inside the {@code CommandBox}
     * @param arguments the {@code String} of arguments identified by the parser
     */
    private void updateStateForCommandsWithPrefixes(String commandBoxText, String arguments) {
        ArrayList<String> missingPrefixes = parser.getMissingPrefixes(arguments);
        if (lastCharIsStartOfPrefix(commandBoxText)) {
            state = AutocompleteState.COMMAND_COMPLETE_PREFIX;
            return;
        }

        if (lastTwoCharactersArePrefix(commandBoxText)) {
            setIndexToOneIfNeeded();
            if (missingPrefixes.size() > possibleAutocompleteResults.size()) {
                possibleAutocompleteResults = missingPrefixes;
            }
            state = AutocompleteState.COMMAND_CYCLE_PREFIX;
            return;
        }

        possibleAutocompleteResults = missingPrefixes;
        state = AutocompleteState.COMMAND_NEXT_PREFIX;

    }

    /**
     * Returns true if the value of resultIndex to 0 if the the autocompleter was not cycling through possible options
     * in its previous state
     *
     * @param commandWord the current text inside the {@code CommandBox}
     */
    private boolean isCyclingThroughCommands(String commandWord) {
        return state.equals(AutocompleteState.MULTIPLE_COMMAND) && textInCommandBox.length() == commandWord.length();
    }

    /**
     * Resets the value of resultIndex to 0 if the the autocompleter was not cycling through possible options
     * in its previous state
     */
    private void resetIndexIfNeeded() {
        if (!state.equals(AutocompleteState.MULTIPLE_COMMAND)) {
            resultIndex = 0;
        }
    }

    /**
     * Resets the value of resultIndex to 1 if the autocompleter was not cycling through prefixes
     * int its previous state
     */
    private void setIndexToOneIfNeeded() {
        if (!state.equals(AutocompleteState.COMMAND_CYCLE_PREFIX)) {
            resultIndex = 1;
        }
    }

    /**
     * Resets the value of counting to 1 if the autocompleter was not cycling through indexes
     * int its previous state
     */
    private void resetCountingAndMaxIndexIfNeeded() {
        if (!state.equals(AutocompleteState.INDEX)) {
            countingIndex = 1;
        }
    }

    /**
     * Returns true if the {@code arguments} still requires the index argument to be filled in
     *
     * @param arguments the current {@code String} of the arguments in the {@code CommandBox}
     */
    private boolean needIndex(String arguments) {
        return  (state.equals(AutocompleteState.INDEX) && lastCharIsDigit(textInCommandBox))
                || !(containsIndex(arguments));
    }

    /**
     * Returns true if the index field in the {@code arguments} has been filled in
     *
     * @param arguments the current {@code String} of the arguments in the {@code CommandBox}
     */
    private boolean containsIndex(String arguments) {
        String parameters = arguments;
        Prefix[] prefixes = AutocompleteCommand.ALL_PREFIXES;
        if (lastTwoCharactersArePrefix(arguments)) {
            parameters = arguments + SPACE;
        } else if (lastCharIsStartOfPrefix(arguments)) {
            parameters = arguments + PREFIX_INDICATOR + SPACE;
        }
        ArgumentMultimap argMap = ArgumentTokenizer.tokenize(parameters, prefixes);
        String index = argMap.getPreamble();
        return (!index.equals(EMPTY_STRING) && isNumeric(index));
    }

    /**
     * Returns true if the {@code index} is numeric
     */
    private boolean isNumeric (String index) {
        return index.matches("[0-9]+");
    }

    /**
     * Returns true if the last character of the {@code text} is a digit
     */
    private boolean lastCharIsDigit(String text) {
        if (text.length() < 1) {
            return false;
        }
        return Character.isDigit(text.charAt(text.length() - 1));
    }

    /**
     * Updates the {@code state} of the autocompleter and the list of possible commands
     * in {@code possibleAutocompleteResults}
     *
     * @param commandBoxText the current {@code String} inside the {@code CommandBox}
     */
    private void autocompleteCommandWord(String commandBoxText) {
        ArrayList<String> possibleResults = getClosestCommands(commandBoxText);
        possibleAutocompleteResults = possibleResults;
        switch (possibleResults.size()) {
        case 0:
            state = AutocompleteState.NO_RESULT;
            break;

        case 1:
            state = AutocompleteState.COMMAND;
            break;

        default:
            resetIndexIfNeeded();
            state = AutocompleteState.MULTIPLE_COMMAND;
            break;
        }
    }

    /**
     * Returns true if the last 2 characters of {@code commandBoxText} is a space followed
     * by the first letter of a {@code Prefix}
     */
    private boolean lastCharIsStartOfPrefix(String commandBoxText) {
        if (commandBoxText.length() < 1) {
            return false;
        }
        String lastTwoCharacter = commandBoxText.substring(commandBoxText.length() - 2);
        return Arrays.stream(AutocompleteCommand.ALL_PREFIXES)
                .map(s -> SPACE + s.toString().substring(0, 1))
                .anyMatch(s -> s.equals(lastTwoCharacter));
    }

    /**
     * Checks if the last two characters of the {@code commandBoxText} are prefixes
     */
    private boolean lastTwoCharactersArePrefix(String commandBoxText) {
        if (commandBoxText.length() < 2) {
            return false;
        }
        String lastTwoCharacters = commandBoxText.substring(commandBoxText.length() - 2);
        return Arrays.stream(AutocompleteCommand.ALL_PREFIXES)
                .anyMatch(s -> lastTwoCharacters.equals(s.toString()));
    }

    /**
     * Returns an {@code ArrayList} of possible commands that match {@code commandBoxText}
     */
    private ArrayList<String> getClosestCommands (String commandBoxText) {
        ArrayList<String> possibleResults = new ArrayList<>();
        Arrays.stream(AutocompleteCommand.ALL_COMMANDS)
                .filter(s -> isPossibleMatch(commandBoxText.toLowerCase(), s))
                .forEach(s -> possibleResults.add(s));
        return possibleResults;
    }

    /**
     * Checks if {@code commandBoxText} is a substring of {@code commandWord}
     */
    private boolean isPossibleMatch(String commandBoxText, String commandWord) {
        return (commandBoxText.length() <= commandWord.length()
                && commandBoxText.equals(commandWord.substring(0, commandBoxText.length())));
    }

    /**
     * Creates message and raises a {@code NewResultAvailableEvent} to tell user that there are multiple results
     */
    private void displayMultipleResults(ArrayList<String> results) {
        String resultToDisplay = MULTIPLE_RESULT_MESSAGE + ":\n";
        for (String result : results) {
            resultToDisplay += result + "\t";
        }
        raise(new NewResultAvailableEvent(resultToDisplay.trim(), false));
    }

    private void clearResultsWindow() {
        raise(new NewResultAvailableEvent(EMPTY_STRING, false));
    }

    /**
     * Registers the object as an event handler at the {@link EventsCenter}
     *
     * @param handler usually {@code this}
     */
    private void registerAsAnEventHandler(Object handler) {
        EventsCenter.getInstance().registerHandler(handler);
    }

    /**
     * Raises the event via {@link EventsCenter#post(BaseEvent)}
     */
    private void raise(BaseEvent event) {
        EventsCenter.getInstance().post(event);
    }
}
```
###### \java\seedu\address\ui\autocompleter\AutocompleteState.java
``` java
/**
 * Represents the states of the autcompleter
 */
public enum AutocompleteState {
    COMMAND,
    COMMAND_NEXT_PREFIX,
    COMMAND_CYCLE_PREFIX,
    COMMAND_COMPLETE_PREFIX,
    EMPTY,
    MULTIPLE_COMMAND,
    NO_RESULT,
    INDEX
}
```
###### \java\seedu\address\ui\autocompleter\CommandBoxParser.java
``` java
/**
 * Parses text that the user has entered in the command box
 */
public class CommandBoxParser {

    protected static final int COMMAND_INDEX = 0;
    protected static final int ARGUMENT_INDEX = 1;

    private static final  String EMPTY_STRING = "";

    /**
     * Used for initial separation of command word and args
     */
    private static final Pattern BASIC_COMMAND_FORMAT = Pattern.compile("(?<commandWord>\\S+)(?<arguments>.*)");

    private static final Prefix[] ALL_PREFIXES = {PREFIX_TRACKING_NUMBER, PREFIX_NAME, PREFIX_ADDRESS,
        PREFIX_DELIVERY_DATE, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_STATUS, PREFIX_TAG};

    /**
     * Parses {@code commandBoxText} to see if it contains any instances of a {@code Command} and {@code Prefix}
     *
     * @return {@code String} array containing the {@code Command} at index 0 and the remaining arguments at index 1
     */
    public String[] parseCommandAndPrefixes(String commandBoxText) {
        requireNonNull(commandBoxText);
        String[] parseResults = {EMPTY_STRING, EMPTY_STRING };
        final Matcher matcher = BASIC_COMMAND_FORMAT.matcher(commandBoxText.trim());
        if (!matcher.matches()) {
            return parseResults;
        }
        final String commandWord = matcher.group("commandWord");
        final String arguments = matcher.group("arguments");
        if (isValidCommand(commandWord)) {
            parseResults[COMMAND_INDEX] = commandWord;
        }
        parseResults[ARGUMENT_INDEX] = arguments;
        return parseResults;
    }

    /**
     * Returns the ArrayList of prefixes that are missing from the {@code argument}
     */
    public ArrayList<String> getMissingPrefixes(String argument) {
        requireNonNull(argument);
        Prefix[] prefixes = ALL_PREFIXES;
        ArrayList<String> missingPrefixes = new ArrayList<>();
        ArgumentMultimap argMap = ArgumentTokenizer.tokenize(argument, prefixes);
        Arrays.stream(ALL_PREFIXES)
                .filter(p -> isMissing(argMap, p))
                .forEach(p -> missingPrefixes.add(p.toString()));
        return missingPrefixes;
    }

    /**
     * Returns true if {@code prefix} is not present in the {@code argMap}
     * or if it is present but is still missing arguments
     */
    private boolean isMissing(ArgumentMultimap argMap, Prefix prefix) {
        return prefix.equals(PREFIX_TAG)
                || !argMap.getValue(prefix).isPresent()
                || argMap.getValue(prefix).get().equals(EMPTY_STRING);
    }

    private boolean isValidCommand(String commandWord) {
        return Arrays.asList(AutocompleteCommand.ALL_COMMANDS).contains(commandWord);
    }
}
```
