package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_TAB;

import org.junit.Test;

import seedu.address.logic.commands.TabCommand;

/**
 * Test scope: similar to {@code SelectCommandParserTest}.
 * @see SelectCommandParserTest
 */
//@@author vicisapotato
public class TabCommandParserTest {

    private TabCommandParser parser = new TabCommandParser();

    @Test
    public void parse_validArgs_returnsTabCommand() {
        assertParseSuccess(parser, "1", new TabCommand(INDEX_FIRST_TAB));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a", String.format(MESSAGE_INVALID_COMMAND_FORMAT, TabCommand.MESSAGE_USAGE));
    }
}
//@@author vicisapotato
