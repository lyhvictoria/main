# fustilio
###### \java\seedu\address\bot\ArkBot.java
``` java
package seedu.address.bot;

import static org.telegram.abilitybots.api.objects.Flag.PHOTO;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.MessageSender;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.google.common.annotations.VisibleForTesting;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import seedu.address.bot.parcel.DisplayParcel;
import seedu.address.bot.parcel.ParcelParser;
import seedu.address.bot.qrcode.QRcodeAnalyser;
import seedu.address.bot.qrcode.exceptions.QRreadException;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.parcel.ReadOnlyParcel;

/**
 * Arkbot contains all of the commands available for use.
 */
public class ArkBot extends AbilityBot {

    public static final String BOT_MESSAGE_FAILURE = "Oh dear, something went wrong! Please try again!";
    public static final String BOT_MESSAGE_SUCCESS = "%s command has been successfully executed!";
    public static final String BOT_MESSAGE_START = "Welcome to ArkBot, your friendly companion to ArkBot on Desktop.\n"
                                                 + "Over here, you can interface with your Desktop application with "
                                                 + "the following functions /add, /list, /delete, /undo, /redo, "
                                                 + "/complete, /cancel and /help.";
    public static final String BOT_MESSAGE_COMPLETE_COMMAND = "Please upload QR code to complete delivery.\n"
                                                            + "Type \"/cancel\" to stop uploading process.";
    public static final String BOT_MESSAGE_CANCEL_COMMAND = "QR Code upload successfully cancelled!";
    public static final String BOT_MESSAGE_HELP = "The commands available to ArkBot v1.5 are as follows: \n"
                                                + "/all Parcel Details - Adds a parcel.\n"
                                                + "/list - Lists uncompleted parcel deliveries.\n"
                                                + "/delete Parcel Index - Deletes a parcel.\n"
                                                + "/undo - Undo a command.\n"
                                                + "/redo - Redo a command.\n"
                                                + "/complete Parcel Index - Marks a parcel as completed.\n"
                                                + "/complete - Activates `listen` mode.\n"
                                                + "/cancel - Cancels `listen` mode.\n"
                                                + "/help - Brings up this dialogue again.\n\n"
                                                + "In `listen` mode, ArkBot will wait for a QR code of a parcel "
                                                + "to be marked as completed. Otherwise, ArkBot "
                                                + "will return the details of the parcel embedded in the QR code.\n\n"
                                                + "Refer to our [User Guide](https://github.com/CS2103AUG2017-T16-B1"
                                                + "/main/blob/master/docs/UserGuide.adoc) for more information.";
    private static final String BOT_SET_COMPLETED = "s/Completed";
    private static final String DEFAULT_BOT_TOKEN = "339790464:AAGUN2BmhnU0I2B2ULenDdIudWyv1d4OTqY";
    private static final String DEFAULT_BOT_USERNAME = "ArkBot";
    private static final Privacy PRIVACY_SETTING = Privacy.PUBLIC;

    private static final Logger logger = LogsCenter.getLogger(ArkBot.class);

    private Logic logic;
    private Model model;
    private boolean waitingForImage;

    public ArkBot(Logic logic, Model model, String botToken, String botUsername) {
        super(botToken, botUsername);
        this.logic = logic;
        this.model = model;
        this.waitingForImage = false;
        logger.info("ArkBot successfully booted up.");
    }

    /**
     * Replicates the effects of AddCommand on ArkBot.
     */
    public Ability startCommand() {
        return Ability
                .builder()
                .name("start")
                .info("welcomes the user to ArkBot")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> sender.send(BOT_MESSAGE_START, ctx.chatId())))
                .build();
    }

    /**
     * Replicates the effects of AddCommand on ArkBot.
     */
    public Ability addCommand() {
        return Ability
                .builder()
                .name(AddCommand.COMMAND_WORD)
                .info("adds parcel to list")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> {
                    try {
                        logic.execute(AddCommand.COMMAND_WORD + " "
                                + combineArguments(ctx.arguments()));
                        sender.send(String.format(BOT_MESSAGE_SUCCESS, AddCommand.COMMAND_WORD), ctx.chatId());
                    } catch (CommandException | ParseException e) {
                        sender.send(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE),
                                ctx.chatId());
                    }
                }))
                .build();
    }

    /**
     * Replicates the effects of ListCommand on ArkBot.
     */
    public Ability listCommand() {
        return Ability
                .builder()
                .name(ListCommand.COMMAND_WORD)
                .info("lists all parcels")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> {
                    try {
                        logic.execute(ListCommand.COMMAND_WORD + " "
                                + combineArguments(ctx.arguments()));
                        ObservableList<ReadOnlyParcel> parcels = model.getUncompletedParcelList();
                        sender.send(parseDisplayParcels(formatParcelsForBot(parcels)),
                                ctx.chatId());
                    } catch (CommandException | ParseException e) {
                        sender.send("Sorry, I don't understand.", ctx.chatId());
                    }
                }))
                .build();
    }

    /**
     * Replicates the effects of DeleteCommand on ArkBot.
     */
    public Ability deleteCommand() {
        return Ability
                .builder()
                .name(DeleteCommand.COMMAND_WORD)
                .info("deletes parcel at selected index")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action((MessageContext ctx) -> Platform.runLater(() -> {
                    try {
                        logic.execute(DeleteCommand.COMMAND_WORD + " "
                                + combineArguments(ctx.arguments()));
                        ObservableList<ReadOnlyParcel> parcels = model.getUncompletedParcelList();
                        sender.send(parseDisplayParcels(formatParcelsForBot(parcels)), ctx.chatId());
                    } catch (CommandException | ParseException e) {
                        sender.send(BOT_MESSAGE_FAILURE, ctx.chatId());
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }))
                .build();
    }

    /**
     * Replicates the effects of RedoCommand on ArkBot.
     */
    public Ability redoCommand() {
        return Ability
                .builder()
                .name(RedoCommand.COMMAND_WORD)
                .info("deletes parcel at selected index")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action((MessageContext ctx) -> Platform.runLater(() -> {
                    try {
                        logic.execute(RedoCommand.COMMAND_WORD);
                        sender.send(String.format(BOT_MESSAGE_SUCCESS, RedoCommand.COMMAND_WORD), ctx.chatId());
                    } catch (CommandException | ParseException e) {
                        sender.send(BOT_MESSAGE_FAILURE, ctx.chatId());
                    }
                }))
                .build();
    }

    /**
     * Replicates the effects of UndoCommand on ArkBot.
     */
    public Ability undoCommand() {
        return Ability
                .builder()
                .name(UndoCommand.COMMAND_WORD)
                .info("deletes parcel at selected index")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action((MessageContext ctx) -> Platform.runLater(() -> {
                    try {
                        logic.execute(UndoCommand.COMMAND_WORD);
                        sender.send(String.format(BOT_MESSAGE_SUCCESS, UndoCommand.COMMAND_WORD), ctx.chatId());
                    } catch (CommandException | ParseException e) {
                        sender.send(BOT_MESSAGE_FAILURE, ctx.chatId());
                    }
                }))
                .build();
    }

    /**
     * Replicates the effects of FindCommand on ArkBot.
     */
    public Ability findCommand() {
        return Ability
                .builder()
                .name(FindCommand.COMMAND_WORD)
                .info("adds parcel to list")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> {
                    try {
                        logic.execute(FindCommand.COMMAND_WORD + " "
                                + combineArguments(ctx.arguments()));
                        ObservableList<ReadOnlyParcel> parcels = model.getUncompletedParcelList();
                        sender.send(parseDisplayParcels(formatParcelsForBot(parcels)), ctx.chatId());
                    } catch (CommandException | ParseException e) {
                        sender.send(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE),
                                ctx.chatId());
                    }
                }))
                .build();
    }

    /**
     * Command to complete tasks with QR code or number
     */
    public Ability completeCommand() {
        return Ability
                .builder()
                .name("complete")
                .info("completes a parcel in list")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> {
                    String input = combineArguments(ctx.arguments());
                    try {
                        if (input.trim().equals("")) {
                            this.waitingForImage = true;
                            sender.send(BOT_MESSAGE_COMPLETE_COMMAND, ctx.chatId());
                        } else if (containsAllNumbers(input.trim())) {
                            logic.execute(EditCommand.COMMAND_WORD + " "
                                    + input + BOT_SET_COMPLETED);
                            ObservableList<ReadOnlyParcel> parcels = model.getUncompletedParcelList();
                            sender.send(parseDisplayParcels(formatParcelsForBot(parcels)), ctx.chatId());
                        } else {
                            sender.send(BOT_MESSAGE_FAILURE, ctx.chatId());
                        }
                    } catch (CommandException | ParseException e) {
                        sender.send(BOT_MESSAGE_FAILURE, ctx.chatId());
                    }
                }))
                .build();
    }

    /**
     * Command to cancel waiting for a QR to mark as completed.
     */
    public Ability cancelCommand() {
        return Ability
                .builder()
                .name("cancel")
                .info("cancels QR code upload")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> {
                    this.waitingForImage = false;
                    sender.send(BOT_MESSAGE_CANCEL_COMMAND, ctx.chatId());
                }))
                .build();
    }

    /**
     * Command to advise user on usage of bot.
     */
    public Ability helpCommand() {
        return Ability
                .builder()
                .name("help")
                .info("adds parcel to list")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action(ctx -> Platform.runLater(() -> {
                    try {
                        sender.sendMessage(new SendMessage().setText(BOT_MESSAGE_HELP)
                                                                .setChatId(ctx.chatId())
                                                                .enableMarkdown(true));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }))
                .build();
    }

    /**
     * Takes in the array of arguments that is parsed into ArkBot and
     * returns a formatted string.
     */
    private String combineArguments(String[] arguments) {
        String result = "";

        for (int i = 0; i < arguments.length; i++) {
            result += arguments[i] + " ";
        }

        return result;
    }

    /**
     * Filters the list of parcels to only show Name, Address and Phone number
     * attributed to each parcel.
     */
    public ArrayList<DisplayParcel> formatParcelsForBot(ObservableList<ReadOnlyParcel> parcels) {
        ArrayList<DisplayParcel> toDisplay = new ArrayList<>();

        Iterator<ReadOnlyParcel> parcelIterator = parcels.iterator();

        while (parcelIterator.hasNext()) {
            ReadOnlyParcel currParcel = parcelIterator.next();
            DisplayParcel displayed = new DisplayParcel(currParcel.getName(), currParcel.getAddress(),
                    currParcel.getPhone());
            toDisplay.add(displayed);
        }

        return toDisplay;
    }

    /**
     * Formats a list of Parcels to be displayed on ArkBot
     */
    public String parseDisplayParcels(ArrayList<DisplayParcel> displayParcels) {
        if (displayParcels.size() == 0) {
            return "No parcels to be displayed.";
        } else {
            String result = "";
            for (int i = 0; i < displayParcels.size(); i++) {
                result += (i + 1) + ". " + displayParcels.get(i).toString() + "\n";
            }

            return result;
        }
    }

    @Override
    public int creatorId() {
        return 0;
    }

    @Override
    public boolean checkGlobalFlags(Update update) {
        return true;
    }

    /**
     * This ability has an extra "flag". It needs a photo to activate.
     */
    public Ability onPhotoCommand() {
        return Ability
                .builder()
                .name(DEFAULT)
                .flag(PHOTO)
                .info("receives Photos")
                .input(0)
                .locality(Locality.ALL)
                .privacy(PRIVACY_SETTING)
                .action((MessageContext ctx) -> Platform.runLater(() -> {
                    Update update = ctx.update();
                    if (update.hasMessage() && update.getMessage().hasPhoto()) {
                        java.io.File picture = getPictureFileFromUpdate(update);
                        try {
                            ReadOnlyParcel retrievedParcel = retrieveParcelFromPictureFile(picture);
                            logger.info("The retrieved parcel is: " + retrievedParcel);
                            if (retrievedParcel.equals(null)) {
                                sender.send("Sorry, I didn't seem to understand your image. Please try again.",
                                        ctx.chatId());
                            } else if (this.waitingForImage) {
                                int indexZeroBased = model.getUncompletedParcelList().indexOf(retrievedParcel);
                                if (indexZeroBased < 0) {
                                    sender.send("The parcel cannot be found! Please try again.",
                                            ctx.chatId());
                                } else {
                                    performCompleteParcel(sender, logic, indexZeroBased + 1,
                                            retrievedParcel, ctx.chatId());
                                }
                            } else {
                                sender.send("Here are the details of the parcel: \n"
                                                + retrievedParcel.toString(), ctx.chatId());
                            }
                        } catch (ParseException | CommandException | QRreadException e) {
                            sender.send(BOT_MESSAGE_FAILURE, ctx.chatId());
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                })).build();
    }

    /**
     * Abstracted method that performs edits a parcel to completed.
     */
    private void performCompleteParcel(MessageSender sender, Logic logic, int indexOfParcel,
                                       ReadOnlyParcel retrievedParcel, Long chatId)
                                       throws CommandException, ParseException, TelegramApiException {
        logic.execute(EditCommand.COMMAND_WORD + " "
                + indexOfParcel + BOT_SET_COMPLETED);
        sender.send("Here are the details of the parcel you just completed: \n"
                    + retrievedParcel.toString(), chatId);

    }

    /**
     * Method to extract ReadOnlyParcel from picture file using zxing qr code analyser.
     */
    private ReadOnlyParcel retrieveParcelFromPictureFile(java.io.File picture) throws ParseException, QRreadException {
        QRcodeAnalyser qrca = new QRcodeAnalyser(picture);
        ParcelParser pp = new ParcelParser();
        logger.info("The decoded text is: " + qrca.getDecodedText());
        return pp.parse(qrca.getDecodedText());
    }

    /**
     * Method to extract picture file from update.
     */
    private java.io.File getPictureFileFromUpdate(Update update) {
        return downloadPhotoByFilePath(getFilePath(getPhoto(update)));
    }
```
###### \java\seedu\address\bot\ArkBot.java
``` java
    /**
     * Returns true if a given string contains all numbers.
     */
    public static boolean containsAllNumbers(String test) {
        String regex = "\\d+";
        return test.matches(regex);
    }

    @VisibleForTesting
    void setSender(MessageSender sender) {
        this.sender = sender;
    }

    @VisibleForTesting
    boolean getWaitingForImageFlag() {
        return this.waitingForImage;
    }
}
```
###### \java\seedu\address\bot\parcel\DisplayParcel.java
``` java
package seedu.address.bot.parcel;

import seedu.address.model.parcel.Address;
import seedu.address.model.parcel.Name;
import seedu.address.model.parcel.Phone;

/**
 * Formats a parcel to be displayed on telegram.
 */
public class DisplayParcel {

    private Name name;
    private Address address;
    private Phone phone;

    public DisplayParcel(Name name, Address address, Phone phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Name: " + this.name.toString()
                + "\nAddress: " + this.address.toString()
                + "\nPhone: " + this.phone.toString();
    }
}
```
###### \java\seedu\address\bot\parcel\ParcelParser.java
``` java
package seedu.address.bot.parcel;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DELIVERY_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TRACKING_NUMBER;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.parser.ArgumentMultimap;
import seedu.address.logic.parser.ArgumentTokenizer;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.Prefix;
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

/**
 * Parser to parse details of a parcel.
 */
public class ParcelParser {

    public static final String PARCEL_PARSER_ERROR = "Invalid parcel format!";

    /**
     * Parse ia a method to parse a String containing the details of a parcel into a ReadOnlyParcel
     * @param args represent the details of the parcels with the prefixes.
     * @return a ReadOnlyParcel that has the details corresponding to the arguments
     * @throws ParseException if we are unable to understand the parcel information
     */
    public ReadOnlyParcel parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(" " + args, PREFIX_TRACKING_NUMBER, PREFIX_NAME, PREFIX_PHONE,
                        PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_DELIVERY_DATE, PREFIX_STATUS, PREFIX_TAG);
        if (!arePrefixesPresent(argMultimap, PREFIX_TRACKING_NUMBER, PREFIX_NAME, PREFIX_ADDRESS,
                PREFIX_DELIVERY_DATE)) {
            throw new ParseException(PARCEL_PARSER_ERROR);
        }

        try {
            TrackingNumber trackingNumber = ParserUtil.parseTrackingNumber(argMultimap
                    .getValue(PREFIX_TRACKING_NUMBER)).get();
            Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME)).get();
            Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS)).get();
            DeliveryDate deliveryDate = ParserUtil.parseDeliveryDate(argMultimap.getValue(PREFIX_DELIVERY_DATE)).get();
            Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
            Optional<Email> emailOptional = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL));
            Optional<Status> statusOptional = ParserUtil.parseStatus(argMultimap.getValue(PREFIX_STATUS));
            Optional<Phone> phoneOptional = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE));

            Email email;
            Status status;
            Phone phone;

            if (emailOptional.isPresent()) {
                email = emailOptional.get();
            } else {
                email = new Email();
            }

            if (statusOptional.isPresent()) {
                status = statusOptional.get();
            } else {
                status = Status.getInstance("Pending");
            }

            if (phoneOptional.isPresent()) {
                phone = phoneOptional.get();
            } else {
                phone = new Phone();
            }

            ReadOnlyParcel parcel = new Parcel(trackingNumber, name, phone, email, address, deliveryDate, status,
                    tagList);

            return parcel;
        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
```
###### \java\seedu\address\bot\qrcode\exceptions\QRreadException.java
``` java
package seedu.address.bot.qrcode.exceptions;

/**
 * Represents an error which occurs during QRcode analysis.
 */
public class QRreadException extends Exception {
}
```
###### \java\seedu\address\bot\qrcode\QRcodeAnalyser.java
``` java
package seedu.address.bot.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import seedu.address.bot.qrcode.exceptions.QRreadException;
import seedu.address.commons.core.LogsCenter;

/**
 * QRcodeAnalyser takes in a QR code image and unwraps the information encoded within it.
 */
public class QRcodeAnalyser {

    private static final Logger logger = LogsCenter.getLogger(QRcodeAnalyser.class);

    private String decodedText;

    public QRcodeAnalyser(File file) throws QRreadException {
        try {
            String decodedText = decodeQRcode(file);
            if (decodedText == null) {
                logger.info("No QR Code found in the image");
            } else {
                this.decodedText = decodedText;
                logger.info("Decoded text = " + decodedText);
            }
        } catch (QRreadException | IOException e) {
            logger.info("Could not decode QR Code: " + e.getMessage());
            throw new QRreadException();
        }
    }

    public String getDecodedText() {
        return this.decodedText;
    }

    /**
     * Method to decode the QR code using zxing api.
     */
    private static String decodeQRcode(File qrCodeimage) throws IOException, QRreadException {
        BufferedImage bufferedImage = ImageIO.read(qrCodeimage);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            logger.info("There is no QR code in the image");
            throw new QRreadException();
        }
    }
}
```
###### \java\seedu\address\logic\commands\UndoableCommand.java
``` java
    /**
     * Stores the current state of {@code model#addressBook}.
     */
    private void saveAddressBookSnapshot() {
        requireNonNull(model);
        this.previousAddressBook = new AddressBook(model.getAddressBook());
        this.previousActiveListIsAll = model.getActiveIsAllBool();
    }

    /**
     * Reverts the AddressBook to the state before this command
     * was executed and updates the filtered parcel list to
     * show all parcels.
     */
    protected final void undo() {
        requireAllNonNull(model, previousAddressBook);
        model.resetData(previousAddressBook);
        model.updateFilteredParcelList(PREDICATE_SHOW_ALL_PARCELS);
        if (previousActiveListIsAll) {
            model.uiJumpToTabAll();
        } else {
            model.uiJumpToTabCompleted();
        }
    }

    /**
     * Executes the command and updates the filtered parcel
     * list to show all parcels.
     */
    protected final void redo() {
        requireNonNull(model);
        try {
            executeUndoableCommand();
        } catch (CommandException ce) {
            throw new AssertionError("The command has been successfully executed previously; "
                    + "it should not fail now");
        }

        model.updateFilteredParcelList(PREDICATE_SHOW_ALL_PARCELS);
    }
```
###### \java\seedu\address\logic\parser\ParserUtil.java
``` java
    /**
     * Parses {@code Optional<String>} into an {@code Optional<DeliveryDate>} and returns it. Leading and trailing
     * whitespaces will be trimmed.
     */
    public static Optional<DeliveryDate> parseDeliveryDate(Optional<String> deliveryDate) throws IllegalValueException {
        requireNonNull(deliveryDate);
        return deliveryDate.isPresent() ? Optional.of(new DeliveryDate(deliveryDate.get())) : Optional.empty();
    }
    //@author

```
###### \java\seedu\address\MainApp.java
``` java
        // Instantiate bot here
        if (!botStarted) {

            ApiContextInitializer.init();

            TelegramBotsApi botsApi = new TelegramBotsApi();
            try {
                bot = new ArkBot(logic, model,
                        config.getBotToken(), config.getBotUsername());
                logger.info("Bot Authentication Token: " + config.getBotToken());
                botSession = botsApi.registerBot(bot);
                botStarted = true;
            } catch (TelegramApiException e) {
                logger.warning("Invalid Telegram Bot authentication token. Please check to ensure that "
                        + "you have keyed in the token correctly and restart the application.");
            }
        }
```
###### \java\seedu\address\MainApp.java
``` java
    /**
     * Method to return instance of logic manager for testing.
     */
    @VisibleForTesting
    public Logic getLogic() {
        return this.logic;
    }


    /**
     * Method to return instance of bot for testing.
     */
    @VisibleForTesting
    public ArkBot getBot() {
        return this.bot;
    }
```
###### \java\seedu\address\model\AddressBook.java
``` java
    /**
     * Function that sorts the lists of parcels
     */
    public void sort() {
        try {
            this.setParcels(parcels.getSortedList());
        } catch (DuplicateParcelException e) {
            e.printStackTrace();
        }
    }
```
###### \java\seedu\address\model\Model.java
``` java
    /**
     * Method to sort the lists of addresses by delivery date with the earliest date in front
     */
    void maintainSorted();

    /**
     * Method to force the model to select a card without using the select command.
     */
    void forceSelect(Index target);


    /**
     * Method to force the model to select a card without using the select command.
     */
    void forceSelectParcel(ReadOnlyParcel target);

    /**
     * Method to set tabIndex attribute in Model.
     */
    void setTabIndex(Index index);

    /**
     * Method to get tabIndex attribute in Model.
     */
    Index getTabIndex();

    /**
     * Method to encapsulate all the sub methods to be executed when AddCommand is executed.
     * @param parcel the parcel to add
     * @throws DuplicateParcelException if parcel is already inside the list of parcels, reject the input
     */
    void addParcelCommand(ReadOnlyParcel parcel) throws DuplicateParcelException;

    /**
     * Method to encapsulate all the sub methods to be executed when EditCommand is executed.
     * @param parcelToEdit the parcel to edit
     * @param editedParcel the edited parcel to replace the parcel to edit.
     * @throws DuplicateParcelException if editedParcel already exists unless the parcelToEdit is the same entity.
     * @throws ParcelNotFoundException if parcelToEdit cannot be found in the list
     */
    void editParcelCommand(ReadOnlyParcel parcelToEdit, ReadOnlyParcel editedParcel)
            throws DuplicateParcelException, ParcelNotFoundException;

    /**
     * Method to retrieve flag that represents whether the current tab selected is all parcels.
     */
    boolean getActiveIsAllBool();

    /**
     * Method to forcefully raise the event to switch tabs to all parcels.
     */
    void uiJumpToTabAll();

    /**
     * Method to forcefully rasie the event to switch tabs to completed parcels.
     */
    void uiJumpToTabCompleted();
```
###### \java\seedu\address\model\ModelListener.java
``` java
package seedu.address.model;

import com.google.common.eventbus.Subscribe;

import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.events.ui.JumpToTabRequestEvent;

/**
 * SelectionListener listens for events that select a parcel card.
 */
public class ModelListener {

    private Model model = null;

    /**
     * Initializes a SelectionLister with the given model.
     */
    public ModelListener(Model model) {
        this.model = model;
        registerAsAnEventHandler(this);
    }

    /**
     * Registers the object as an event handler at the {@link EventsCenter}
     * @param handler usually {@code this}
     */
    protected void registerAsAnEventHandler(Object handler) {
        EventsCenter.getInstance().registerHandler(handler);
    }

    /**
     * Triggers when there is a JumpToTabRequestEvent and sets the tabIndex in the model
     * to keep track of which tab the model is "on".
     */
    @Subscribe
    private void handleJumpToTabEvent(JumpToTabRequestEvent event) {
        model.setTabIndex(Index.fromZeroBased(event.targetIndex));
    }
}
```
###### \java\seedu\address\model\ModelManager.java
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
###### \java\seedu\address\model\parcel\DeliveryDate.java
``` java
package seedu.address.model.parcel;

import static java.util.Objects.requireNonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Represents a Parcel's delivery date in the address book.
 * Guarantees: mutable; is valid as declared in {@link #isValidDate(String)}
 */
public class DeliveryDate {


    public static final String MESSAGE_DELIVERY_DATE_CONSTRAINTS =
            "Delivery dates should be in the format dd-mm-yyyy or references to date such as "
            + "\"today\" or \"next week.\"";
    public static final List<String> VALID_STRING_FORMATS = Arrays.asList(
            "dd-MM-yyyy", "d-MM-yyyy", "d-M-yyyy", "dd-M-yyyy",
            "dd/MM/yyyy", "d/MM/yyyy", "d/M/yyyy", "dd/M/yyyy",
            "dd.MM.yyyy", "d.MM.yyyy", "d.M.yyyy", "dd/M.yyyy");
    public static final String DATE_FORMAT_VALIDATION_REGEX = "^(\\d{1,2}[./-]\\d{1,2}[./-]\\d{4})$";
    public final String value;
    private Date date;
    private final Logger logger = LogsCenter.getLogger(this.getClass());

    /**
     * Validates given delivery date.
     *
     * @throws IllegalValueException if given delivery date string is invalid.
     */
    public DeliveryDate(String deliveryDate) throws IllegalValueException {
        requireNonNull(deliveryDate);
        String trimmedDate = deliveryDate.trim();
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        df.setLenient(false);

        // Check if input is in a format we can understand
        if (!isValidDateFormat(trimmedDate)) {
            // Check if input is in a format that PrettyTime(NLP) can understand
            if (isValidPrettyTimeDate(trimmedDate)
                    && hasMinimumLength(trimmedDate)
                    && !containsAllNumbers(trimmedDate)) {
                // NLP appears to understand the intention, so we accept the input
                List<Date> dates = new PrettyTimeParser().parse(trimmedDate);
                this.date = dates.get(0);
            } else {
                throw new IllegalValueException(MESSAGE_DELIVERY_DATE_CONSTRAINTS);
            }
        } else { // We understand the intention, so we accept the input
            try {
                this.date = formatDate(trimmedDate);
            } catch (ParseException e) { // date is in correct format, but not a valid date.
                throw new IllegalValueException(MESSAGE_DELIVERY_DATE_CONSTRAINTS);
            }
        }

        // Format date correctly
        this.value = df.format(this.date);
    }

    /**
     * Formats the input date according to the list VALID_STRING_FORMATS and returns it.
     */
    private Date formatDate(String inputDate) throws ParseException {

        for (String formatString : VALID_STRING_FORMATS) {
            DateFormat df = new SimpleDateFormat(formatString);
            df.setLenient(false);
            try {
                return df.parse(inputDate);
            } catch (ParseException e) {
                logger.info("Failed to fit input delivery date in current format, trying next format...");
            }
        }

        logger.warning("Exhausted all formats, not a valid input.");

        throw new ParseException(inputDate, 0);

    }

    /**
     * Returns true if a given string is a valid date for delivery.
     */
    public static boolean isValidDate(String test) {
        DeliveryDate result;
        try {
            result = new DeliveryDate(test);
        } catch (IllegalValueException e) {
            return false;
        }
        return !result.equals(null);
    }

    public static boolean isValidDateFormat(String test) {
        return test.matches(DATE_FORMAT_VALIDATION_REGEX);
    }

    /**
     * Returns true if a given string is a valid date for delivery.
     */
    public static boolean isValidPrettyTimeDate(String test) {
        List<Date> dates = new PrettyTimeParser().parse(test);

        return dates.size() > 0;
    }

    /**
     * Returns true if a given string is of a minimum length, more than 2 chars
     */
    public static boolean hasMinimumLength(String test) {
        return test.length() > 2;
    }

    /**
     * Returns true if a given string contains all numbers.
     */
    public static boolean containsAllNumbers(String test) {
        String regex = "\\d+";
        return test.matches(regex);
    }

    private Date getDate() {
        return this.date;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeliveryDate // instanceof handles nulls
                && this.value.equals(((DeliveryDate) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public int compareTo(DeliveryDate deliveryDate) {
        return this.date.compareTo(deliveryDate.getDate());
    }
}
```
###### \java\seedu\address\model\parcel\Parcel.java
``` java
    /**
     * We choose to order parcels first by delivery date, next by tracking number if the delivery dates
     * are the same, and lastly by name if the tracking numbers are the same as well.
     */
    @Override
    public int compareTo(Object o) {
        Parcel other = (Parcel) o;
        if (other == this) { // short circuit if same object
            return 0;
        } else if (this.getDeliveryDate().compareTo(other.getDeliveryDate()) == 0) { // delivery dates are equal
            if (this.getName().compareTo(other.getName()) == 0) { // names are equal
                return this.getTrackingNumber().compareTo(other.getTrackingNumber()); // compare tracking numbers
            } else {
                return this.getName().compareTo(other.getName()); // compare names
            }
        } else {
            return this.getDeliveryDate().compareTo(other.getDeliveryDate()); // compare delivery dates
        }
    }
```
###### \java\seedu\address\model\tag\exceptions\TagInternalErrorException.java
``` java
package seedu.address.model.tag.exceptions;

/**
 * Signals that there is an error within a Tag operation.
 */
public class TagInternalErrorException extends Exception {}
```
###### \java\seedu\address\model\tag\exceptions\TagNotFoundException.java
``` java
package seedu.address.model.tag.exceptions;

/**
 * Signals that the operation is unable to find the tag of a specified parcel.
 */
public class TagNotFoundException extends Exception {}
```
