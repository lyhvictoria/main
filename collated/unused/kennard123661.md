# kennard123661
###### \DeprecatedModelManager.java
``` java
    @Subscribe @Override
    public void setActiveList(JumpToTabRequestEvent event) {
        boolean isCompleted = event.targetIndex == INDEX_SECOND_TAB.getZeroBased();
        activeParcels = isCompleted ? completedParcels : uncompletedParcels;
        logger.info("Active list now set to "
                + (isCompleted ? "completed parcels list." : "uncompleted parcels list."));
    }
```
###### \DeprecatedModelManager.java
``` java
    @Override
    public synchronized void addAllParcels(List<ReadOnlyParcel> parcels, List<ReadOnlyParcel> uniqueParcels,
                                           List<ReadOnlyParcel> duplicateParcels) {

        assert parcels != null : "parcels should not be null";
        assert uniqueParcels != null : "uniqueParcels should not be null";
        assert duplicateParcels != null : "duplicateParcels should not be null";

        for (ReadOnlyParcel parcel : parcels) {
            try {
                // throws duplicate parcel exception if parcel is non-unique
                addressBook.addParcel(parcel);

                uniqueParcels.add(parcel);
            } catch (DuplicateParcelException ive) {
                duplicateParcels.add(parcel);
            }
        }

        maintainSorted();
        updateFilteredParcelList(PREDICATE_SHOW_ALL_PARCELS);
        indicateAddressBookChanged();
    }
```
###### \DeprecatedModelManager.java
``` java
    /**
     * Returns an unmodifiable view of the list of {@link ReadOnlyParcel} with {@link Status} that is COMPLETED,
     * backed by the internal list of {@code addressBook}
     */
    @Override
    public ObservableList<ReadOnlyParcel> getCompletedParcelList() {
        return FXCollections.unmodifiableObservableList(completedParcels);
    }

    /**
     * Returns an unmodifiable view of the list of {@link ReadOnlyParcel} with {@link Status} that is not COMPLETED,
     * backed by the internal list of {@code addressBook}
     */
    @Override
    public ObservableList<ReadOnlyParcel> getUncompletedParcelList() {
        return FXCollections.unmodifiableObservableList(uncompletedParcels);
    }

    /**
     * Returns an unmodifiable view of the list of {@link ReadOnlyParcel} in the {@code activeParcels}
     */
    @Override
    public ObservableList<ReadOnlyParcel> getActiveList() {
        return FXCollections.unmodifiableObservableList(activeParcels);
    }
```
