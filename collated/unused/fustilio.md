# fustilio
###### \DeleteTagCommand.java
``` java
package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.exceptions.TagInternalErrorException;
import seedu.address.model.tag.exceptions.TagNotFoundException;

/**
 * Deletes a tag from all parcels from the address book.
 *
 * Deprecated in Ark v1.5
 * This function was deprecated in v1.5 as the team decided that it did not enhance
 * the overall functionality of Ark and did not fit in well with the other features.
 */
public class DeleteTagCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "deleteTag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the tag from all parcels in the address book.\n"
            + "Parameters: Tag name\n"
            + "Example: " + COMMAND_WORD + " friends";

    public static final String MESSAGE_DELETE_TAG_SUCCESS = "Deleted Tag: %1$s";
    public static final String MESSAGE_INVALID_DELETE_TAG_NOT_FOUND = "Tag not found: %1$s";

    private final Tag targetTag;

    public DeleteTagCommand(Tag targetTag) {
        this.targetTag = targetTag;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {

        Tag tagToDelete = targetTag;

        try {
            model.deleteTag(tagToDelete);
        } catch (TagInternalErrorException tiee) {
            throw new CommandException(MESSAGE_USAGE);
        } catch (TagNotFoundException tnfe) {
            throw new CommandException(String.format(MESSAGE_INVALID_DELETE_TAG_NOT_FOUND, tagToDelete));
        }

        return new CommandResult(String.format(MESSAGE_DELETE_TAG_SUCCESS, tagToDelete));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeleteTagCommand // instanceof handles nulls
                && this.targetTag.equals(((DeleteTagCommand) other).targetTag)); // state check
    }
}
```
###### \DeleteTagCommandParser.java
``` java
package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.DeleteTagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new DeleteTagCommand object
 *
 * Deprecated in Ark v1.5
 * See {@link DeleteTagCommand}
 */
public class DeleteTagCommandParser implements Parser<DeleteTagCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns an DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public DeleteTagCommand parse(String args) throws ParseException {
        try {
            Tag tag = ParserUtil.parseTag(args);
            return new DeleteTagCommand(tag);
        } catch (IllegalValueException ive) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteTagCommand.MESSAGE_USAGE));
        }
    }

}
```
###### \DeleteTagCommandParserTest.java
``` java
package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FROZEN;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.DeleteTagCommand;
import seedu.address.model.tag.Tag;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteTagCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteTagCommand, and therefore we test only one of them.
 * The path variation for those two cases occur inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 *
 * Deprecated in Ark v1.5
 * See {@link DeleteTagCommand}
 */
public class DeleteTagCommandParserTest {

    private static final String INVALID_TAG = "#friend";
    private static final String VALID_TAG_1 = VALID_TAG_FROZEN;
    private DeleteTagCommandParser parser = new DeleteTagCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteTagCommand() throws IllegalValueException {
        assertParseSuccess(parser, VALID_TAG_FROZEN, new DeleteTagCommand(Tag.getInstance(VALID_TAG_1)));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, INVALID_TAG, String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                                                              DeleteTagCommand.MESSAGE_USAGE));
    }
}
```
###### \DeleteTagCommandSystemTest.java
``` java
package systemtests;

import static seedu.address.commons.core.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FLAMMABLE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FROZEN;
import static seedu.address.logic.commands.DeleteTagCommand.MESSAGE_DELETE_TAG_SUCCESS;
import static seedu.address.logic.commands.DeleteTagCommand.MESSAGE_INVALID_DELETE_TAG_NOT_FOUND;
import static seedu.address.testutil.TestUtil.getParcel;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PARCEL;

import java.util.Iterator;

import org.junit.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.DeleteTagCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.model.Model;
import seedu.address.model.parcel.ReadOnlyParcel;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.exceptions.TagInternalErrorException;
import seedu.address.model.tag.exceptions.TagNotFoundException;

/**
 * Deprecated in Ark v1.5
 * See {@link DeleteTagCommand}
 */
public class DeleteTagCommandSystemTest extends AddressBookSystemTest {

    @Test
    public void deleteTag() throws IllegalValueException {
        /* ---------------- Performing deleteTag operation while an unfiltered list is being shown ---------------- */

        /* Case: delete the first parcel in the list, command with leading spaces and trailing spaces -> deleted */
        Model expectedModel = getModel();

        ReadOnlyParcel targetParcel = getParcel(expectedModel, INDEX_FIRST_PARCEL);

        Iterator<Tag> targetTags = targetParcel.getTags().iterator();
        Tag targetTag = null;

        if (targetTags.hasNext()) {
            targetTag = targetTags.next();
        }

        String command = "     " + DeleteTagCommand.COMMAND_WORD + "      " + targetTag.toString() + "       ";

        Tag deletedTag = removeTag(expectedModel, targetTag);
        String expectedResultMessage = String.format(MESSAGE_DELETE_TAG_SUCCESS, deletedTag);
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        Model modelBeforeDeletingLast = getModel();
        targetTag = Tag.getInstance(VALID_TAG_FLAMMABLE);

        assertCommandSuccess(targetTag);

        /* Case: undo deleting the previous tag in the list -> deleted tag restored */
        command = UndoCommand.COMMAND_WORD;
        expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, expectedModel, expectedResultMessage);

        /* Case: redo deleting the last parcel in the list -> last tag deleted again */
        command = RedoCommand.COMMAND_WORD;
        removeTag(modelBeforeDeletingLast, targetTag);
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, modelBeforeDeletingLast, expectedResultMessage);

        /* ------------------------------- Performing invalid deleteTag operation ----------------------------------- */

        /* Case: invalid arguments (tag not founds) -> rejected */
        expectedResultMessage = String.format(MESSAGE_INVALID_DELETE_TAG_NOT_FOUND,
                Tag.getInstance(Tag.FROZEN.toString()));
        assertCommandFailure(DeleteTagCommand.COMMAND_WORD + " " + VALID_TAG_FROZEN.toString(),
                expectedResultMessage);

        /* Case: mixed case command word -> rejected */
        assertCommandFailure("DelETEtAG friends", MESSAGE_UNKNOWN_COMMAND);
    }

    /**
     * Removes the {@code ReadOnlyParcel} at the specified {@code index} in {@code model}'s address book.
     * @return the removed parcel
     */
    private Tag removeTag(Model model, Tag targetTag) {
        try {
            model.deleteTag(targetTag);
        } catch (TagNotFoundException | TagInternalErrorException e) {
            throw new AssertionError("targetTag is retrieved from model.");
        }
        return targetTag;
    }

    /**
     * Deletes the tag at {@code toDelete} by creating a default {@code DeleteTagCommand} using {@code toDelete} and
     * performs the same verification as {@code assertCommandSuccess(String, Model, String)}.
     * @see DeleteTagCommandSystemTest#assertCommandSuccess(String, Model, String)
     */
    private void assertCommandSuccess(Tag toDelete) {
        Model expectedModel = getModel();
        Tag deletedTag = removeTag(expectedModel, toDelete);
        String expectedResultMessage = String.format(MESSAGE_DELETE_TAG_SUCCESS, deletedTag);

        assertCommandSuccess(
                DeleteTagCommand.COMMAND_WORD + " " + toDelete.toString(), expectedModel, expectedResultMessage);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Model, String)} except that the browser url
     * and selected card are expected to update accordingly depending on the card at {@code expectedSelectedCardIndex}.
     * @see DeleteTagCommandSystemTest#assertCommandSuccess(String, Model, String)
     * @see AddressBookSystemTest#assertSelectedCardChanged(Index)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);

        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and in addition,<br>
     * 1. Asserts that the command box displays {@code command}.<br>
     * 2. Asserts that result display box displays {@code expectedResultMessage}.<br>
     * 3. Asserts that the model related components equal to the current model.<br>
     * 4. Asserts that the browser url, selected card and status bar remain unchanged.<br>
     * 5. Asserts that the command box has the error style.<br>
     * Verifications 1 to 3 are performed by
     * {@code AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see AddressBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
```
###### \DeleteTagCommandSystemTest.java
``` java

```
###### \DeleteTagCommandTest.java
``` java
package seedu.address.logic.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FLAMMABLE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRAGILE;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalParcels.getTypicalAddressBook;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.UndoRedoStack;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.parcel.ReadOnlyParcel;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.exceptions.TagNotFoundException;


/**
 * Contains integration tests (interaction with the Model) and unit tests for ListCommand.
 *
 * Deprecated in Ark v1.5
 * See {@link DeleteTagCommand}
 */
public class DeleteTagCommandTest {

    private Model model;
    private Model expectedModel;
    private DeleteTagCommand deleteTagCommand;
    private Tag tagToDelete;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        ObservableList<ReadOnlyParcel> parcelsToManipulate = model.getFilteredParcelList();

        Iterator it = parcelsToManipulate.iterator();
        Boolean noCandidate = true;

        while (it.hasNext() && noCandidate) {
            ReadOnlyParcel parcelToManipulate = (ReadOnlyParcel) it.next();
            if (!parcelToManipulate.getTags().isEmpty()) {
                tagToDelete = (Tag) parcelToManipulate.getTags().toArray()[0];
                noCandidate = false;
            }
        }
    }

    @Test
    public void execute_deleteTag_success() throws Exception {

        deleteTagCommand = prepareCommand(tagToDelete);

        String expectedMessage = String.format(DeleteTagCommand.MESSAGE_DELETE_TAG_SUCCESS, tagToDelete);

        try {
            expectedModel.deleteTag(tagToDelete);
        } catch (TagNotFoundException e) {
            e.printStackTrace();
        }

        assertCommandSuccess(deleteTagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_deleteTag_tagNotFoundFailure() throws Exception {

        try {
            tagToDelete = Tag.getInstance(Tag.FRAGILE.toString());
        } catch (IllegalValueException e) {
            e.printStackTrace();
        }

        deleteTagCommand = prepareCommand(tagToDelete);

        String exceptionMessage = String.format(DeleteTagCommand.MESSAGE_INVALID_DELETE_TAG_NOT_FOUND, tagToDelete);

        assertCommandFailure(deleteTagCommand, model, exceptionMessage);
    }

    @Test
    public void execute_deleteTag_tagNotValid() throws Exception {

        String exceptionMessage = "";

        try {
            tagToDelete = Tag.getInstance("!@#$%^&*()");
        } catch (IllegalValueException e) {
            exceptionMessage = e.getMessage();
        }

        String expectedMessage = Tag.MESSAGE_TAG_CONSTRAINTS;

        assertEquals(expectedMessage, exceptionMessage);
    }

    private DeleteTagCommand prepareCommand(Tag target) {
        DeleteTagCommand deleteTagCommand = new DeleteTagCommand(target);
        deleteTagCommand.setData(model, new CommandHistory(), new UndoRedoStack());
        return deleteTagCommand;
    }

    @Test
    public void equals() {
        Tag flammable = null;
        Tag fragile = null;
        try {
            flammable = Tag.getInstance(VALID_TAG_FLAMMABLE.toLowerCase());
            fragile = Tag.getInstance(VALID_TAG_FRAGILE);
        } catch (IllegalValueException e) {
            e.printStackTrace();
        }

        DeleteTagCommand deleteUrgentTagCommand = new DeleteTagCommand(flammable);
        DeleteTagCommand deleteFragileTagCommand = new DeleteTagCommand(fragile);

        // same object -> returns true
        assertTrue(deleteUrgentTagCommand.equals(deleteUrgentTagCommand));

        // same values -> returns true
        DeleteTagCommand deleteUrgentTagCommandCopy = new DeleteTagCommand(flammable);
        assertTrue(deleteUrgentTagCommand.equals(deleteUrgentTagCommandCopy));

        // different types -> returns false
        assertFalse(deleteUrgentTagCommand.equals(1));

        // null -> returns false
        assertFalse(deleteUrgentTagCommand.equals(null));

        // different parcel -> returns false
        assertFalse(deleteUrgentTagCommand.equals(deleteFragileTagCommand));
    }
}
```
###### \DeprecatedModelManager.java
``` java
    /** Deletes the tag from every parcel in the address book */
    public void deleteTag(Tag target) throws TagNotFoundException, TagInternalErrorException {

        int tagsFound = 0;
        Iterator it = addressBook.getParcelList().iterator();
        while (it.hasNext()) {
            Parcel oldParcel = (Parcel) it.next();
            Parcel newParcel = new Parcel(oldParcel);
            Set<Tag> newTags = new HashSet<>(newParcel.getTags());
            if (newTags.contains(target)) {
                newTags.remove(target);
                tagsFound++;
            }

            newParcel.setTags(newTags);

            try {
                addressBook.updateParcel(oldParcel, newParcel);
            } catch (DuplicateParcelException | ParcelNotFoundException dpe) {
                throw new TagInternalErrorException();
            }
        }

        if (tagsFound == 0) {
            throw new TagNotFoundException();
        }

        indicateAddressBookChanged();
    }
```
###### \DeprecatedModelManager.java
``` java
    @Override
    public void maintainSorted() {
        addressBook.sort();
    }

    @Override
    public void forceSelect(Index target) {
        EventsCenter.getInstance().post(new JumpToListRequestEvent(target));
    }

    @Override
    public void forceSelectParcel(ReadOnlyParcel target) {
        forceSelect(Index.fromZeroBased(findIndex(target)));
    }

    @Override
    public void setTabIndex(Index index) {
        this.tabIndex = index;
    }

    @Override
    public Index getTabIndex() {
        return this.tabIndex;
    }

    @Override
    public void addParcelCommand(ReadOnlyParcel toAdd) throws DuplicateParcelException {
        this.addParcel(toAdd);
        this.maintainSorted();
        this.handleTabChange(toAdd);
        this.forceSelectParcel(toAdd);
        indicateAddressBookChanged();
    }

    @Override
    public void editParcelCommand(ReadOnlyParcel parcelToEdit, ReadOnlyParcel editedParcel)
            throws DuplicateParcelException, ParcelNotFoundException {
        this.updateParcel(parcelToEdit, editedParcel);
        this.maintainSorted();
        this.handleTabChange(editedParcel);
        this.forceSelectParcel(editedParcel);
        indicateAddressBookChanged();
    }

    @Override
    public boolean getActiveIsAllBool() {
        return tabIndex.equals(TAB_ALL_PARCELS);
    }

    /**
     * Method to internally change the active list to the correct tab according to the changed parcel.
     * @param targetParcel
     */
    private void handleTabChange(ReadOnlyParcel targetParcel) {
        try {
            if (targetParcel.getStatus().equals(Status.getInstance("COMPLETED"))) {
                if (this.getTabIndex().equals(TAB_ALL_PARCELS)) {
                    uiJumpToTabCompleted();
                }
            } else {
                if (this.getTabIndex().equals(TAB_COMPLETED_PARCELS)) {
                    uiJumpToTabAll();
                }
            }
        } catch (IllegalValueException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void uiJumpToTabAll() {
        EventsCenter.getInstance().post(new JumpToTabRequestEvent(TAB_ALL_PARCELS));
    }

    @Override
    public void uiJumpToTabCompleted() {
        EventsCenter.getInstance().post(new JumpToTabRequestEvent(TAB_COMPLETED_PARCELS));
    }

    /**
     * Method to retrieve the index of a given parcel in the active list.
     */
    private int findIndex(ReadOnlyParcel target) {
        return getActiveList().indexOf(target);
    }
```
