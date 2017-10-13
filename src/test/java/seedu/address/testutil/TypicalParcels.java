package seedu.address.testutil;

import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import seedu.address.model.AddressBook;
import seedu.address.model.parcel.ReadOnlyParcel;
import seedu.address.model.parcel.exceptions.DuplicateParcelException;

/**
 * A utility class containing a list of {@code Parcel} objects to be used in tests.
 */
public class TypicalParcels {

    public static final ReadOnlyParcel ALICE = new ParcelBuilder().withName("Alice Pauline")
            .withAddress("6, Jurong West Ave 1, #08-111 S649520").withEmail("alice@example.com")
            .withPhone("85355255")
            .withTags("friends").build();
    public static final ReadOnlyParcel BENSON = new ParcelBuilder().withName("Benson Meier")
            .withAddress("336, Clementi Ave 2, #02-25 s120336")
            .withEmail("johnd@example.com").withPhone("98765432")
            .withTags("owesMoney", "friends").build();
    public static final ReadOnlyParcel CARL = new ParcelBuilder().withName("Carl Kurz").withPhone("95352563")
            .withEmail("heinz@example.com").withAddress("18 Marina Blvd, S018980").build();
    public static final ReadOnlyParcel DANIEL = new ParcelBuilder().withName("Daniel Meier").withPhone("87652533")
            .withEmail("cornelia@example.com").withAddress("59 Namly Garden S267387").build();
    public static final ReadOnlyParcel ELLE = new ParcelBuilder().withName("Elle Meyer").withPhone("9482224")
            .withEmail("werner@example.com").withAddress("2 Finlayson Green, S049247").build();
    public static final ReadOnlyParcel FIONA = new ParcelBuilder().withName("Fiona Kunz").withPhone("9482427")
            .withEmail("lydia@example.com").withAddress("48 Upper Dickson Rd S207502").build();
    public static final ReadOnlyParcel GEORGE = new ParcelBuilder().withName("George Best").withPhone("9482442")
            .withEmail("anna@example.com").withAddress("Block 532 HDB Upper Cross Street s050532").build();

    // Manually added
    public static final ReadOnlyParcel HOON = new ParcelBuilder().withName("Hoon Meier").withPhone("8482424")
            .withEmail("stefan@example.com").withAddress("522 Hougang Ave 6 s530522").build();
    public static final ReadOnlyParcel IDA = new ParcelBuilder().withName("Ida Mueller").withPhone("8482131")
            .withEmail("hans@example.com").withAddress("3 River Valley Rd, S179024").build();

    // Manually added - Parcel's details found in {@code CommandTestUtil}
    public static final ReadOnlyParcel AMY = new ParcelBuilder().withName(VALID_NAME_AMY).withPhone(VALID_PHONE_AMY)
            .withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY).withTags(VALID_TAG_FRIEND).build();
    public static final ReadOnlyParcel BOB = new ParcelBuilder().withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
            .withEmail(VALID_EMAIL_BOB).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND)
            .build();

    public static final String KEYWORD_MATCHING_MEIER = "Meier"; // A keyword that matches MEIER

    private TypicalParcels() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical parcels.
     */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        for (ReadOnlyParcel parcel : getTypicalParcels()) {
            try {
                ab.addParcel(parcel);
            } catch (DuplicateParcelException e) {
                assert false : "not possible";
            }
        }
        return ab;
    }

    public static List<ReadOnlyParcel> getTypicalParcels() {
        return new ArrayList<>(Arrays.asList(ALICE, BENSON, CARL, DANIEL, ELLE, FIONA, GEORGE));
    }
}