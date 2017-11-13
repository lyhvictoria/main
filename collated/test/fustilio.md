# fustilio
###### \java\seedu\address\bot\ArkBotTest.java
``` java
package seedu.address.bot;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static seedu.address.bot.ArkBot.BOT_MESSAGE_CANCEL_COMMAND;
import static seedu.address.bot.ArkBot.BOT_MESSAGE_COMPLETE_COMMAND;
import static seedu.address.bot.ArkBot.BOT_MESSAGE_FAILURE;
import static seedu.address.bot.ArkBot.BOT_MESSAGE_START;
import static seedu.address.bot.ArkBot.BOT_MESSAGE_SUCCESS;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.TypicalParcels.BENSON;
import static seedu.address.testutil.TypicalParcels.DANIEL;
import static seedu.address.testutil.TypicalParcels.HOON;

import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.objects.EndUser;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.api.objects.Update;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import seedu.address.TestApp;
import seedu.address.bot.parcel.ParcelParser;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.parcel.ReadOnlyParcel;
import seedu.address.model.parcel.exceptions.DuplicateParcelException;
import seedu.address.model.parcel.exceptions.ParcelNotFoundException;
import seedu.address.testutil.ParcelBuilder;
import systemtests.ModelHelper;
import systemtests.SystemTestSetupHelper;

public class ArkBotTest {
    public static final long CHAT_ID = 1337L;
    public static final int USER_ID = 1337;

    private static final String BOT_DEMO_JOHN = "#/RR000000000SG n/John Doe p/98765432 e/johnd@example.com "
            + "a/John street, block 123, #01-01 S123121 d/01-01-2001 s/DELIVERING";
    private static final String SAMPLE_ADD_COMMAND = BOT_DEMO_JOHN;
    private static final Logger logger = LogsCenter.getLogger(ArkBotTest.class);

    private ArkBot bot;
    private DBContext db;
    private MessageSender sender;
    private EndUser endUser;
    private Model model;
    private ParcelParser parcelParser;
    private int numberOfFailures = 0;

    @BeforeClass
    public static void setupBeforeClass() {
        SystemTestSetupHelper.initializeStage();
    }

    @Before
    public void setUp() {
        // Offline instance will get deleted at JVM shutdown
        db = MapDBContext.offlineInstance("test");
        SystemTestSetupHelper setupHelper = new SystemTestSetupHelper();
        TestApp testApp = setupHelper.setupApplication();
        model = testApp.getModel();
        bot = testApp.getBot();
        sender = mock(MessageSender.class);
        endUser = EndUser.endUser(USER_ID, "Abbas", "Abou Daya", "addo37");
        bot.setSender(sender);
        parcelParser = new ParcelParser();
    }

    @Test
    public void arkBotAllTests() throws InterruptedException, ParseException, DuplicateParcelException,
            ParcelNotFoundException {
        Update mockedUpdate = mock(Update.class);
        MessageContext context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);

        bot.startCommand().action().accept(context);

        // We verify that the sender was called only ONCE and sent start up message
        Mockito.verify(sender, times(1)).send(BOT_MESSAGE_START, CHAT_ID);

        /*================================== UNDO COMMAND FAILURE TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);

        bot.undoCommand().action().accept(context);
        String message = BOT_MESSAGE_FAILURE;
        waitForRunLater();

        // We verify that the sender sent failed message numberOfFailures times.
        Mockito.verify(sender, times(++numberOfFailures)).send(message, CHAT_ID);

        /*================================== REDO COMMAND FAILURE TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);

        bot.redoCommand().action().accept(context);
        message = BOT_MESSAGE_FAILURE;
        waitForRunLater();

        // We verify that the sender sent failed message numberOfFailures times.
        Mockito.verify(sender, times(++numberOfFailures)).send(message, CHAT_ID);

        /*================================== ADD COMMAND SUCCESS TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, SAMPLE_ADD_COMMAND);

        bot.addCommand().action().accept(context);
        message = String.format(BOT_MESSAGE_SUCCESS, AddCommand.COMMAND_WORD);
        model.addParcelCommand(parcelParser.parse(BOT_DEMO_JOHN));
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent add command success.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== ADD COMMAND FAILURE TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, SAMPLE_ADD_COMMAND);

        bot.addCommand().action().accept(context);
        message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent add command failure.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== LIST COMMAND SUCCESS TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);

        bot.listCommand().action().accept(context);
        ObservableList<ReadOnlyParcel> parcels = model.getUncompletedParcelList();
        message = bot.parseDisplayParcels(bot.formatParcelsForBot(parcels));
        logger.info("Listing parcels: \n" + message);
        waitForRunLater();

        // We verify that the sender was called only ONCE and listed parcels
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== DELETE COMMAND SUCCESS TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, "1");

        bot.deleteCommand().action().accept(context);
        message = bot.parseDisplayParcels(bot.formatParcelsForBot(parcels));
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent delete command success.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== UNDO COMMAND SUCCESS TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);

        bot.undoCommand().action().accept(context);
        message = String.format(BOT_MESSAGE_SUCCESS, UndoCommand.COMMAND_WORD);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent undo command success.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== REDO COMMAND SUCCESS TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);

        bot.redoCommand().action().accept(context);
        message = String.format(BOT_MESSAGE_SUCCESS, RedoCommand.COMMAND_WORD);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent redo command success.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== DELETE COMMAND FAILURE TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, (model.getUncompletedParcelList().size()
                + 1) + "");

        bot.deleteCommand().action().accept(context);
        waitForRunLater();

        // We verify that the sender sent failed message numberOfFailures times.
        Mockito.verify(sender, times(++numberOfFailures)).send(BOT_MESSAGE_FAILURE, CHAT_ID);

        /*================================== FIND COMMAND SUCCESS TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, "Meier");
        ModelHelper.setFilteredList(model, BENSON, DANIEL, HOON);
        parcels = model.getUncompletedParcelList();
        message = bot.parseDisplayParcels(bot.formatParcelsForBot(parcels));
        bot.findCommand().action().accept(context);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent find command success.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*================================== FIND COMMAND FAILURE TEST ====================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);
        message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        bot.findCommand().action().accept(context);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent find command failure.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*======================== COMPLETE COMMAND SUCCESS TEST (VALID INPUT) ===========================*/

        mockedUpdate = mock(Update.class);
        parcels = model.getUncompletedParcelList();
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, parcels.size() + "");
        message = bot.parseDisplayParcels(bot.formatParcelsForBot(parcels));
        bot.completeCommand().action().accept(context);
        ReadOnlyParcel oldParcel = parcels.get(parcels.size() - 1);
        ReadOnlyParcel editedParcel = new ParcelBuilder(oldParcel).withStatus("Completed").build();
        model.updateParcel(oldParcel, editedParcel);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent message command success.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*============================== COMPLETE COMMAND FAILURE TEST (INVALID INPUT) ==============================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID, "@#$");
        message = BOT_MESSAGE_FAILURE;
        bot.completeCommand().action().accept(context);
        waitForRunLater();

        // We verify that the sender was called only ONCE and sent message command failure.
        Mockito.verify(sender, times(++numberOfFailures)).send(message, CHAT_ID);

        /*=============================== COMPLETE COMMAND SUCCESS TEST (NO INPUT) ==================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);
        message = BOT_MESSAGE_COMPLETE_COMMAND;
        bot.completeCommand().action().accept(context);
        waitForRunLater();

        assertEquals(bot.getWaitingForImageFlag(), true);
        // We verify that the sender was called only ONCE and sent complete command success and QR code prompt.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

        /*=============================== CANCEL COMMAND SUCCESS TEST (NO INPUT) ==================================*/

        mockedUpdate = mock(Update.class);
        context = MessageContext.newContext(mockedUpdate, endUser, CHAT_ID);
        message = BOT_MESSAGE_CANCEL_COMMAND;
        bot.cancelCommand().action().accept(context);
        waitForRunLater();

        assertEquals(bot.getWaitingForImageFlag(), false);
        // We verify that the sender was called only ONCE and sent complete command success and QR code prompt.
        Mockito.verify(sender, times(1)).send(message, CHAT_ID);

    }

    /**
     * Using semaphores to wait for task on current thread to cease before carrying on.
     */
    public static void waitForRunLater() throws InterruptedException {
        Semaphore semaphore = new Semaphore(0);
        Platform.runLater(() -> semaphore.release());
        semaphore.acquire();

    }

    @After
    public void tearDown() {
        db.clear();
    }
}
```
###### \java\seedu\address\bot\parcel\ParcelParserTest.java
``` java
package seedu.address.bot.parcel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.DELIVERY_DATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DELIVERY_DATE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_DELIVERY_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_STATUS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TRACKING_NUMBER_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_COMPLETED;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_DELIVERING;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FLAMMABLE;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FROZEN;
import static seedu.address.logic.commands.CommandTestUtil.TRACKING_NUMBER_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.TRACKING_NUMBER_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DELIVERY_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_DELIVERY_DATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STATUS_COMPLETED;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FLAMMABLE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FROZEN;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TRACKING_NUMBER_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TRACKING_NUMBER_BOB;

import org.junit.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.parcel.Address;
import seedu.address.model.parcel.DeliveryDate;
import seedu.address.model.parcel.Email;
import seedu.address.model.parcel.Name;
import seedu.address.model.parcel.Parcel;
import seedu.address.model.parcel.Phone;
import seedu.address.model.parcel.ReadOnlyParcel;
import seedu.address.model.parcel.Status;
import seedu.address.model.parcel.TrackingNumber;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.ParcelBuilder;

public class ParcelParserTest {
    private ParcelParser parser = new ParcelParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Parcel expectedParcel = new ParcelBuilder().withTrackingNumber(VALID_TRACKING_NUMBER_BOB)
                .withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withDeliveryDate(VALID_DELIVERY_DATE_BOB)
                .withStatus(VALID_STATUS_COMPLETED).withTags(VALID_TAG_FLAMMABLE).build();

        // multiple tracking number - last tracking number accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_AMY
                + TRACKING_NUMBER_DESC_BOB + NAME_DESC_AMY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
                expectedParcel);

        // multiple names - last name accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_AMY
                + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
                expectedParcel);

        // multiple phones - last phone accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_AMY + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + STATUS_DESC_COMPLETED
                + DELIVERY_DATE_DESC_BOB + TAG_DESC_FLAMMABLE,
                expectedParcel);

        // multiple emails - last email accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_AMY + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
                expectedParcel);

        // multiple addresses - last address accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_AMY + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
                expectedParcel);

        // multiple delivery dates - last delivery date accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
               + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_AMY
               + DELIVERY_DATE_DESC_BOB + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
               expectedParcel);

        // multiple status - last status accepted
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
               + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
               + STATUS_DESC_DELIVERING + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
               expectedParcel);

        // multiple tags - all accepted
        Parcel expectedParcelMultipleTags = new ParcelBuilder().withTrackingNumber(VALID_TRACKING_NUMBER_BOB)
                .withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withDeliveryDate(VALID_DELIVERY_DATE_BOB)
                .withTags(VALID_TAG_FLAMMABLE, VALID_TAG_FROZEN).build();
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB + TAG_DESC_FROZEN
                + TAG_DESC_FLAMMABLE,
                expectedParcelMultipleTags);
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero tags and no status
        Parcel expectedParcel = new ParcelBuilder().withTrackingNumber(VALID_TRACKING_NUMBER_AMY)
                .withName(VALID_NAME_AMY).withPhone(VALID_PHONE_AMY).withEmail(VALID_EMAIL_AMY)
                .withAddress(VALID_ADDRESS_AMY).withDeliveryDate(VALID_DELIVERY_DATE_AMY).withStatus("PENDING")
                .withTags().build();
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_AMY + NAME_DESC_AMY
                + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + DELIVERY_DATE_DESC_AMY,
                expectedParcel);

        // no phone number
        Parcel expectedParcelDefaultPhone = new ParcelBuilder().withTrackingNumber(VALID_TRACKING_NUMBER_BOB)
                .withName(VALID_NAME_BOB).withPhone(Phone.PHONE_DEFAULT_VALUE).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withDeliveryDate(VALID_DELIVERY_DATE_BOB)
                .withStatus(VALID_STATUS_COMPLETED).withTags(VALID_TAG_FLAMMABLE).build();
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
                expectedParcelDefaultPhone);

        // no email
        Parcel expectedParcelDefaultEmail = new ParcelBuilder().withTrackingNumber(VALID_TRACKING_NUMBER_BOB)
                .withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB).withEmail(Email.EMAIL_DEFAULT_VALUE)
                .withAddress(VALID_ADDRESS_BOB).withDeliveryDate(VALID_DELIVERY_DATE_BOB)
                .withStatus(VALID_STATUS_COMPLETED).withTags(VALID_TAG_FLAMMABLE).build();
        assertParseSuccess(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FLAMMABLE,
                expectedParcelDefaultEmail);


    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = ParcelParser.PARCEL_PARSER_ERROR;

        // missing tracking number prefix
        assertParseFailure(parser, " " + VALID_TRACKING_NUMBER_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB, expectedMessage);

        // missing name prefix
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + VALID_NAME_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB, expectedMessage);

        // missing address prefix
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB + DELIVERY_DATE_DESC_BOB, expectedMessage);

        // missing delivery date prefix
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + VALID_DELIVERY_DATE_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + VALID_NAME_BOB
                + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_ADDRESS_BOB + VALID_DELIVERY_DATE_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid tracking number
        assertParseFailure(parser, " " + INVALID_TRACKING_NUMBER_DESC + INVALID_NAME_DESC
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB + STATUS_DESC_COMPLETED
                + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE, TrackingNumber.MESSAGE_TRACKING_NUMBER_CONSTRAINTS);

        // invalid name
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + INVALID_NAME_DESC
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB + STATUS_DESC_COMPLETED
                + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE, Name.MESSAGE_NAME_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE, Phone.MESSAGE_PHONE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE, Email.MESSAGE_EMAIL_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC + DELIVERY_DATE_DESC_BOB
                + STATUS_DESC_COMPLETED + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE, Address.MESSAGE_ADDRESS_CONSTRAINTS);

        // invalid delivery date
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + INVALID_DELIVERY_DATE_DESC
                + STATUS_DESC_COMPLETED + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE,
                DeliveryDate.MESSAGE_DELIVERY_DATE_CONSTRAINTS);

        // invalid status
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB + INVALID_STATUS_DESC
                + TAG_DESC_FROZEN + TAG_DESC_FLAMMABLE, Status.MESSAGE_STATUS_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + NAME_DESC_BOB
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + DELIVERY_DATE_DESC_BOB
                + INVALID_TAG_DESC + VALID_TAG_FLAMMABLE, Tag.MESSAGE_TAG_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, " " + TRACKING_NUMBER_DESC_BOB + INVALID_NAME_DESC
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC + INVALID_DELIVERY_DATE_DESC,
                Name.MESSAGE_NAME_CONSTRAINTS);
    }

    /**
     * Asserts that the parsing of {@code userInput} by {@code parser} is successful and the command created
     * equals to {@code expectedCommand}.
     */
    public static void assertParseSuccess(ParcelParser parser, String userInput, ReadOnlyParcel expectedParcel) {
        try {
            ReadOnlyParcel parsedParcel = parser.parse(userInput);
            assertEquals(parsedParcel, expectedParcel);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid userInput.", pe);
        }
    }

    /**
     * Asserts that the parsing of {@code userInput} by {@code parser} is unsuccessful and the error message
     * equals to {@code expectedMessage}.
     */
    public static void assertParseFailure(ParcelParser parser, String userInput, String expectedMessage) {
        try {
            parser.parse(userInput);
            fail("The expected ParseException was not thrown.");
        } catch (ParseException pe) {
            assertEquals(expectedMessage, pe.getMessage());
        }
    }
}
```
###### \java\seedu\address\bot\qrcode\QRcodeAnalyserTest.java
``` java
package seedu.address.bot.qrcode;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.bot.qrcode.exceptions.QRreadException;


/**
 * QRcodeAnalyser takes in a QR code image and unwraps the information encoded within it.
 */
public class QRcodeAnalyserTest {

    private static final String UNREADABLE_FILE_PATH = "./src/test/data/qrcode/BETSY_CROWE_UNREADABLE.jpg";
    private static final String READABLE_FILE_PATH = "./src/test/data/qrcode/BETSY_CROWE_READABLE.jpg";
    private static final String BETSY_DESIRED_DATA = "#/RR000000000SG n/Betsy Crowe t/frozen d/02-02-2002 "
            + "e/betsycrowe@example.com a/22 Crowe road S123123 p/1234567 t/fragile";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private QRcodeAnalyser qrCodeAnalyser;

    @Test
    public void acceptsReadableAndClear() throws QRreadException {
        File readable = new File(READABLE_FILE_PATH);
        qrCodeAnalyser = new QRcodeAnalyser(readable);
        assertEquals(BETSY_DESIRED_DATA, qrCodeAnalyser.getDecodedText());
    }

    @Test
    public void rejectsUnreadable() throws QRreadException {
        thrown.expect(QRreadException.class);
        File unreadable = new File(UNREADABLE_FILE_PATH);
        qrCodeAnalyser = new QRcodeAnalyser(unreadable);
    }

}
```
###### \java\seedu\address\logic\commands\AddCommandTest.java
``` java
    /**
     * A Model stub that always accept the parcel being added.
     */
    private class ModelStubAcceptingParcelAdded extends ModelStub {
        final ArrayList<Parcel> parcelsAdded = new ArrayList<>();

        /*
        @Override
        public boolean hasSelected() {
            return false;
        }
        */
        @Override
        public void addParcelCommand(ReadOnlyParcel parcel) throws DuplicateParcelException {
            addParcel(parcel);
        }

        @Override
        public void maintainSorted() {
            Collections.sort(parcelsAdded);
        }

        @Override
        public void forceSelectParcel(ReadOnlyParcel parcel) {
            logger.info("Simulate force selection of parcel.");
        }

        @Override
        public void addParcel(ReadOnlyParcel parcel) throws DuplicateParcelException {
            parcelsAdded.add(new Parcel(parcel));
        }

        @Override
        public boolean getActiveIsAllBool() {
            return true;
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }
```
###### \java\seedu\address\logic\commands\EditCommandTest.java
``` java
    /**
     * Returns an {@code EditCommand} with parameters {@code index} and {@code descriptor}
     */
    private EditCommand prepareCommand(Index index, EditCommand.EditParcelDescriptor descriptor) {
        EditCommand editCommand = new EditCommand(index, descriptor);
        editCommand.setData(model, new CommandHistory(), new UndoRedoStack());
        return editCommand;
    }
```
###### \java\seedu\address\model\parcel\DeliveryDateTest.java
``` java
    @Test
    public void isValidDate() {
        // invalid dates
        assertFalse(DeliveryDate.isValidDate("")); // empty string
        assertFalse(DeliveryDate.isValidDate(" ")); // spaces only
        assertFalse(DeliveryDate.isValidDate("91")); // less than 3 numbers
        assertFalse(DeliveryDate.isValidDate("9321313213213123212131")); // only numbers, can't understand
        assertFalse(DeliveryDate.isValidDate("a")); // short string
        assertFalse(DeliveryDate.isValidDate("date")); // non-numeric
        assertFalse(DeliveryDate.isValidDate("#(_!@!@(")); // special charactors
        assertFalse(DeliveryDate.isValidDate("\u200E\uD83D\uDE03\uD83D\uDC81")); // emojis
        assertFalse(DeliveryDate.isValidDate("I love cs2103T")); // non date sentence
        assertFalse(DeliveryDate.isValidDate("32-05-1995")); // too many days in a month
        assertFalse(DeliveryDate.isValidDate("05-13-1995")); // too many months in a year
        assertFalse(DeliveryDate.isValidDate("32/05/1995")); // too many days in a month
        assertFalse(DeliveryDate.isValidDate("05/13/1995")); // too many months in a year
        assertFalse(DeliveryDate.isValidDate("32.05.1995")); // too many days in a month
        assertFalse(DeliveryDate.isValidDate("05.13.1995")); // too many months in a year
        assertFalse(DeliveryDate.isValidDate("29.02.2001")); // Not a leap year
        assertFalse(DeliveryDate.isValidDate("0.02.2001")); // single digit but wrong day
        assertFalse(DeliveryDate.isValidDate("29.0.2001")); // single digit but wrong month

        // valid dates
        assertTrue(DeliveryDate.isValidDate("20-12-1990")); // exactly 3 numbers
        assertTrue(DeliveryDate.isValidDate("01-01-2001"));
        assertTrue(DeliveryDate.isValidDate("01/01/2001"));
        assertTrue(DeliveryDate.isValidDate("01.01.2001"));
        assertTrue(DeliveryDate.isValidDate("31.1.2001")); // single digit month
        assertTrue(DeliveryDate.isValidDate("1.01.2001")); // single digit day
        assertTrue(DeliveryDate.isValidDate("1.1.2001")); // single digit day and month
        assertTrue(DeliveryDate.isValidDate("29.02.2004")); // is leap year

        // invalid dates but returns true because parser "understands" it
        assertTrue(DeliveryDate.isValidDate("9011p041")); // alphabets within digits
        assertTrue(DeliveryDate.isValidDate("9312 1534")); // spaces within digits
    }

    @Test
    public void isPrettyTimeAccurate() throws IllegalValueException {
        assertEquals(new DeliveryDate("01-01-2001"), new DeliveryDate("First day of 2001"));
        assertEquals(new DeliveryDate("02-08-2017"), new DeliveryDate("Second day of August 2017"));
        assertEquals(new DeliveryDate("4-7-2017"), new DeliveryDate("independence day 2017"));
        assertEquals(new DeliveryDate("14-2-2017"), new DeliveryDate("Valentines day 2017"));
        assertEquals(new DeliveryDate("24-12-2017"), new DeliveryDate("Christmas eve 2017"));
    }
```
###### \java\systemtests\AddCommandSystemTest.java
``` java
        /* Case: add Hoon's parcel (completed) and Ida's parcel (pending) and check if tab is switched back and forth*/
        model = getModel();
        assertTrue(model.getTabIndex().equals(TAB_ALL_PARCELS));
        model.addParcelCommand(HOON);
        assertTrue(model.getTabIndex().equals(TAB_COMPLETED_PARCELS));
        model.addParcelCommand(IDA);
        assertTrue(model.getTabIndex().equals(TAB_ALL_PARCELS));
        model.deleteParcel(HOON);
        model.deleteParcel(IDA);
```
###### \java\systemtests\EditCommandSystemTest.java
``` java
        /* ----------------------------- Performing edit operation with tab switches -------------------------------- */

        /* Case: Edit status of first parcel to completed and check if tab is switched back and forth*/
        model = getModel();
        assertTrue(model.getTabIndex().equals(TAB_ALL_PARCELS));
        parcelToEdit = model.getActiveList().get(index.getZeroBased());
        editedParcel = new ParcelBuilder(parcelToEdit).withStatus(VALID_STATUS_COMPLETED).build();
        model.editParcelCommand(parcelToEdit, editedParcel);
        assertTrue(model.getTabIndex().equals(TAB_COMPLETED_PARCELS));
        parcelToEdit = editedParcel;
        editedParcel = new ParcelBuilder(parcelToEdit).withStatus(VALID_STATUS_OVERDUE).build();
        model.editParcelCommand(parcelToEdit, editedParcel);
        assertTrue(model.getTabIndex().equals(TAB_ALL_PARCELS));
```
###### \java\systemtests\MaintainSortedMechanismSystemTest.java
``` java
package systemtests;

import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_STATUS_DELIVERING;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FLAMMABLE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TRACKING_NUMBER_AMY;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalParcels.getTypicalAddressBook;

import java.util.List;

import org.junit.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.logic.UndoRedoStack;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditParcelDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.parcel.Parcel;
import seedu.address.model.parcel.ReadOnlyParcel;
import seedu.address.testutil.EditParcelDescriptorBuilder;
import seedu.address.testutil.ParcelBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class MaintainSortedMechanismSystemTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_maintainSorted_success() throws Exception {
        Index indexLastParcel = Index.fromOneBased(model.getActiveList().size());
        ReadOnlyParcel lastParcel = model.getActiveList().get(indexLastParcel.getZeroBased());

        ParcelBuilder parcelInList = new ParcelBuilder(lastParcel);
        Parcel editedParcel = parcelInList.withName(VALID_NAME_AMY).withPhone(VALID_PHONE_AMY)
                .withTags(VALID_TAG_FLAMMABLE).withTrackingNumber(VALID_TRACKING_NUMBER_AMY)
                .withStatus(VALID_STATUS_DELIVERING).withEmail(VALID_EMAIL_AMY)
                .withAddress(VALID_ADDRESS_AMY).build();

        EditParcelDescriptor descriptor = new EditParcelDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_AMY).withTags(VALID_TAG_FLAMMABLE).withTrackingNumber(VALID_TRACKING_NUMBER_AMY)
                .withStatus(VALID_STATUS_DELIVERING).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .build();
        EditCommand editCommand = prepareCommand(indexLastParcel, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PARCEL_SUCCESS, editedParcel);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateParcel(lastParcel, editedParcel);
        expectedModel.maintainSorted();
        expectedModel.forceSelectParcel(editedParcel);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertTrue(checkSortedLinear(model));
    }

    /**
     * Method to retrieve list of parcels from input model and checks if the list is in sorted order.
     */
    private boolean checkSortedLinear(Model inputModel) {
        ObservableList<ReadOnlyParcel> listToCheck = inputModel.getActiveList();
        return checkSorted(listToCheck);
    }

    /**
     * Iterates recursively through the input list to check whether each element is in sorted order.
     */
    private boolean checkSorted(List listToCheck) {
        if (listToCheck.size() == 0 || listToCheck.size() == 1) {
            return true;
        } else {
            return compareParcels((Parcel) listToCheck.get(0), (Parcel) listToCheck.get(1))
                    && checkSorted(listToCheck.subList(1, listToCheck.size() - 1));
        }
    }

    /**
     * Compares two parcels, returns true if first Parcel should come before second Parcel
     * @param parcelOne
     * @param parcelTwo
     * @return true when ParcelOne compared to ParcelTwo returns less than 0;
     */
    private boolean compareParcels(ReadOnlyParcel parcelOne, ReadOnlyParcel parcelTwo) {
        int result = parcelOne.compareTo(parcelTwo);
        return result <= 0;
    }

    /**
     * Returns an {@code EditCommand} with parameters {@code index} and {@code descriptor}
     */
    private EditCommand prepareCommand(Index index, EditParcelDescriptor descriptor) {
        EditCommand editCommand = new EditCommand(index, descriptor);
        editCommand.setData(model, new CommandHistory(), new UndoRedoStack());
        return editCommand;
    }
}
```
