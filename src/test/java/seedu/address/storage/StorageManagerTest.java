package seedu.address.storage;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.util.Optional;

import javax.xml.crypto.Data;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.commons.events.storage.DataSavingExceptionEvent;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.ui.testutil.EventsCollectorRule;

public class StorageManagerTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Rule
    public final EventsCollectorRule eventsCollectorRule = new EventsCollectorRule();

    private StorageManager storageManager;

    @Before
    public void setUp() {
        XmlAddressBookStorage addressBookStorage = new XmlAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        storageManager = new StorageManager(addressBookStorage, userPrefsStorage);
    }

    private String getTempFilePath(String fileName) {
        return testFolder.getRoot().getPath() + fileName;
    }

    @Test
    public void testPrefFilePath() {
        assertEquals(getTempFilePath("prefs"), storageManager.getUserPrefsFilePath());
    }

    @Test
	public void read_notXmlFormat_exceptionThrown() throws DataConversionException, IOException {

		// thrown.expect(Exception.class);
		storageManager = new StorageManager(new XmlAddressBookStorage("NotXmlFormatAddressBook.xml"), new JsonUserPrefsStorage("random.json"));
		Optional<ReadOnlyAddressBook> backupAddressBookOptional = storageManager
				.readAddressBook(storageManager.getBackupStorageFilePath());
		assertFalse(backupAddressBookOptional.isPresent());
        /* IMPORTANT: Any code below an exception-throwing line (like the one above) will be ignored.
         * That means you should not have more than one exception test in one method
         */
	}

    @Test
    public void backupAddressBook() throws Exception {
        // set up
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);

        // create new backup by loading another Storage Manager
        XmlAddressBookStorage addressBookStorage = new XmlAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        StorageManager backupStorageManager = new StorageManager(addressBookStorage, userPrefsStorage);

        // checks that the backup properly backups the new file.
        Optional<ReadOnlyAddressBook> backupAddressBookOptional = backupStorageManager
                .readAddressBook(backupStorageManager.getBackupStorageFilePath());
        AddressBook backupAddressBook = new AddressBook(backupAddressBookOptional.get());
        assertEquals(backupAddressBook, original);

        // checks that the file does not backup on every save
        AddressBook editedBook = new AddressBook();
        backupStorageManager.saveAddressBook(editedBook);
        Optional<ReadOnlyAddressBook> mainAddressBookOptional = backupStorageManager
                .readAddressBook(backupStorageManager.getAddressBookFilePath());

        AddressBook mainAddressBook = new AddressBook(mainAddressBookOptional.get());
        assertFalse(mainAddressBook.equals(backupAddressBook));

        // checks that the backup only saves on the initialization of another storage manager.
        StorageManager anotherStorageManager = new StorageManager(addressBookStorage, userPrefsStorage);
        backupAddressBookOptional = anotherStorageManager
                .readAddressBook(backupStorageManager.getBackupStorageFilePath());
        backupAddressBook = new AddressBook(backupAddressBookOptional.get());
        assertEquals(editedBook, backupAddressBook);
    }

    @Test
    public void backUpURLTest() {
        String expectedURL = storageManager.getAddressBookFilePath() + "-backup.xml";
        String actualURL = storageManager.getBackupStorageFilePath();
        assertEquals(expectedURL, actualURL);
    }

    @Test
    public void backUpCommnadTest() throws IOException, DataConversionException {
        AddressBook original = getTypicalAddressBook();
        storageManager.backupAddressBook(original);
        Optional<ReadOnlyAddressBook> backupAddressBookOptional = storageManager
                .readAddressBook(storageManager.getBackupStorageFilePath());
        AddressBook backupAddressBook = new AddressBook(backupAddressBookOptional.get());
        assertEquals(backupAddressBook, original);
    }

    @Test
    public void prefsReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonUserPrefsStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonUserPrefsStorageTest} class.
         */
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(300, 600, 4, 6);
        storageManager.saveUserPrefs(original);
        UserPrefs retrieved = storageManager.readUserPrefs().get();
        assertEquals(original, retrieved);
    }

    @Test
    public void addressBookReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link XmlAddressBookStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link XmlAddressBookStorageTest} class.
         */
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);
        ReadOnlyAddressBook retrieved = storageManager.readAddressBook().get();
        assertEquals(original, new AddressBook(retrieved));
    }

    @Test
    public void getAddressBookFilePath() {
        assertNotNull(storageManager.getAddressBookFilePath());
    }

    @Test
    public void handleAddressBookChangedEvent_exceptionThrown_eventRaised() {
        // Create a StorageManager while injecting a stub that  throws an exception when the save method is called
        Storage storage = new StorageManager(new XmlAddressBookStorageExceptionThrowingStub("dummy"),
                                             new JsonUserPrefsStorage("dummy"));
        storage.handleAddressBookChangedEvent(new AddressBookChangedEvent(new AddressBook()));
        assertTrue(eventsCollectorRule.eventsCollector.getMostRecent() instanceof DataSavingExceptionEvent);
    }


    /**
     * A Stub class to throw an exception when the save method is called
     */
    class XmlAddressBookStorageExceptionThrowingStub extends XmlAddressBookStorage {

        public XmlAddressBookStorageExceptionThrowingStub(String filePath) {
            super(filePath);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) throws IOException {
            throw new IOException("dummy exception");
        }
    }


}
