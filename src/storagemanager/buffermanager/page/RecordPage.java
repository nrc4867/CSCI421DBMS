package storagemanager.buffermanager.page;

import storagemanager.buffermanager.BufferManager;
import storagemanager.buffermanager.datatypes.Datatype;
import storagemanager.buffermanager.Table;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.buffermanager.pageManager.PageBuffer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class RecordPage extends Page<Object[]> {

    /**
     * A page class, this stores our records and id ( does not need to be volatile talked to professor )
     * Oh also it makes sense for a page to be a Java file, as it should have a literal place on the computer
     * it represents upon construction.
     */

    private static final long serialVersionUID = 2L;

    private Object[][] records;

    public int getEntriesCount() {
        return entries;
    }

    RecordPage(Table table){
        super(table, PageTypes.RECORD_PAGE);
        // initially just a empty array with no entries?
        System.out.println("created new page with maxRecords: " + table.getMaxRecords() + " and record size: " + table.getRecordSize());
        this.records = new Object[table.getMaxRecords()][];
    }

    @Override
    public boolean insertRecord(Object[] record) throws StorageManagerException, IOException {
        // we split if we are full.
        if(!hasSpace()){
            System.out.println("Splitting!");
            splitPage();

            bufferManager.insertRecord(getTableID(),record);
            return true;
        }


        // iterative binary
        int l = 0,r=entries, m=0;
        while (l <= r) {
            m = l + (r - l) / 2;

            // retrieve record at m
            // Check if record is present at mid
            if(records[m] == null) {
                System.out.println("Inserting in null spot, record exceeds every other records value");
                break;
            }

//            System.out.println("comparing " + record[0] + " to " + records[m][0] + " at index " + m);

            int res = compareRecord(table,record,m);

            // in this case the record already exists in the page
            if(res == 0)
                throw new StorageManagerException(String.format(StorageManagerException.INSERT_RECORD_EXISTS_FORMAT, recordToString(record)));

            // If record greater, ignore left half
            if (res == 1)
                l = m + 1;
                // If record is smaller, ignore right half
            else
                r = m - 1;
        }
        // we need to compare our value to the value we are inserting on
        //m = l;
        // if we are greater than the record we are inserting over, and it is not null we are inbetewen two values
        // and we are greater than the record we are inserting over, so we want to insert +1 more than where we are inserting
        if(records[m] != null) {
            int res = compareRecord(table,record,m);
            if(res == 1)
                m += 1;
        }

        System.out.println("We should insert at " + m);
        // if we reach here, then element was
        // not present

        // this gives us all the entries above m
        int aboveIndex = entries-(m+1);
        System.out.println("Above: " + aboveIndex);
        for(int i=aboveIndex; i>=0; i--){
            int currentIndex = m+i;
            int newIndex = m+i+1;
//            System.out.println("Moving record " + currentIndex + " to index " + newIndex);
            records[newIndex] = records[currentIndex];
        }

        records[m] = record;
        entries++;
        System.out.println("Entries: " + entries);
        // just for nice testing output
        int remaining = records.length-entries;
        System.out.println("Inserted " + record[0] + " into page " + pageID + " there are " + remaining + " records left");
        return true;
    }

    @Override
    public boolean deleteRecord(Object[] record) throws StorageManagerException {
        return true;
    }

    @Override
    public boolean recordExists(Object[] record) {
        return false;
    }


    /**
     * Checks if a page has space to write to
     * @return
     */
    public boolean hasSpace(Table table){
        return entries < table.getMaxRecords();
    }

    @Override
    /**
     *
     */
    public Page     splitPage() {
        System.out.println("Page buffer: " + pageBuffer);
        RecordPage other = (RecordPage) Page.createPage(table, PageTypes.RECORD_PAGE, bufferManager, pageBuffer);

        // split at n/2
        int splitPoint = Math.floorDiv(entries, 2);
        int j=0;

        int startOffset = splitPoint;

        // if we don't have a equal split we increment the start offset up one
        if(entries-splitPoint != splitPoint){
            startOffset+=1;
        }

        for(int i=startOffset; i<entries; i++){
            other.setRecord(this.records[i].clone(), j);
            this.records[i] = null;
            j++;
        }
        System.out.println("Our entries before splitting: " + entries);
        other.entries = splitPoint;
        this.entries = j;
        System.out.println("Entries after: " + entries);
        System.out.println("Other entries: " + other.getEntriesCount());

        return null;
    }

    @Override
    public boolean hasSpace() {
        return table.getMaxRecords() > entries;
    }

    @Override
    public void save() {
        DataManager.savePage(this,table.getId());
    }

    /**
     * find's a record within a page
     */
    public int findRecord(Table table, Object[] record){
        // iterative binary search
        int l = 0, r = entries - 1;
        while (l <= r) {
            int m = l + (r - l) / 2;

            // retrieve record at m
            // Check if record is present at mid
            int res = compareRecord(table,record,m);
            if(res == 0)
                return m;

            // If record greater, ignore left half
            if (res == 1)
                l = m + 1;
                // If record is smaller, ignore right half
            else
                r = m - 1;
        }

        // if we reach here, then element was
        // not present
        return -1;
    }
    /**
     * Get's the bounds of a page
     */
    public int[] bounds(Table table, Object[] record){
        return new int[]{compareRecord(table, record, 0), compareRecord(table, record, entries-1)};
    }

    private String recordToString(Object[] record) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Datatype> datatypes = table.getDatatypes();
        builder.append("{");
        for (int i = 0; i < datatypes.size(); i++, builder.append(", ")) {
            Datatype datatype = datatypes.get(i);
            builder.append(datatype.resolveToString(record[i]));
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * This method compares a record to another record returns
     * 1 : the record is greater than the other record
     * -1: the record is less than the other record
     * 0: the record is equal to the other record
     */
    private int compareRecord(Table table, Object[] record, int index){

        for(int i=0; i < table.getKeyIndices().length; i++){

            int keyIndex = table.getKeyIndices()[i];
            Object obj = record[keyIndex];

            int ret = 0;

            ret = table.compareDataTypes(keyIndex, obj, records[index][keyIndex]);

            // this is a big if statement which basically just compares values depending on their underlying
            // data type as a object, calling Java's built in compare
//            if(table.getDatatypes().get(keyIndex).getType().equals(ValidDataTypes.BOOLEAN)){
//                Boolean firstVal = (Boolean)obj;
//                Boolean secondVal = (Boolean)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(keyIndex).getType().equals(ValidDataTypes.DOUBLE)){
//                Double firstVal = (Double)obj;
//                Double secondVal = (Double)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(keyIndex).getType().equals(ValidDataTypes.INTEGER)){
//                Integer firstVal = (Integer)obj;
//                Integer secondVal = (Integer)records[index][keyIndex];
//                ret = firstVal.compareTo(secondVal);
//            }
//            else if(table.getDatatypes().get(keyIndex).getType().equals(ValidDataTypes.VARCHAR) ||
//                    table.getDatatypes().get(keyIndex).getType().equals(ValidDataTypes.CHAR)){
//                // casting to a string to compare, makes comparisons easier.
//                String firstVal =  String.valueOf((char[])obj);
//                String secondVal = String.valueOf((char[])records[index][keyIndex]);
//
//                ret = firstVal.compareTo(secondVal);
//            }

           if(ret != 0) {
               // restricting this function to return -1 or 1
               if(ret >= 1)
                   return 1;
               else
                   return -1;
           }
        }

        return 0;

    }

    /**
     * Sets a pages records to a passed in value at given index
     * @param records
     */
    public void setRecord(Object[] records, int index){
        this.records[index] = records;
    }
}