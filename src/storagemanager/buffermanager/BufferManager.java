package storagemanager.buffermanager;

import storagemanager.StorageManager;
import storagemanager.buffermanager.diskUtils.DataManager;
import storagemanager.StorageManagerException;
import storagemanager.buffermanager.page.PageTypes;
import storagemanager.buffermanager.page.RecordPage;
import storagemanager.buffermanager.pageManager.PageBuffer;

import java.io.IOException;
import java.util.*;


public class BufferManager {
    /**
     * manages two maps,
     * manage catalog
     * and two maps one for
     * table id -> set of pages
     * table id -> catalog of table
     *
     **/
    private PageBuffer pageBuffer;
    private final Map<Integer, Table> tableMap;
    private final int pageSize;

    public BufferManager(int maxPages, int pageSize){
        this.pageSize = pageSize;
        tableMap = new HashMap<>();
        pageBuffer = new PageBuffer(this, maxPages);
    }

    /**
     * check table map, add to table map if doesn't exist
     * need to manually search all the pages in the directory as they may not be loaded into memory
     * find correct page, insert on page
     * needs to handle splitting.
     * calls get record to check if exists
     * if id doesnt then ->
     * inserts a record through insertion sort.
     */
    public void insertRecord(int tableId, Object[] record) throws StorageManagerException, IOException {
        Table table = getTable(tableId);

        // in this case just create page and insert in empty page, it's our first entry
        pageBuffer.insertRecord(table, record);
    }

    /**
     * Check table map, find the correct page\
     * need to manually search all the pages in the directory as they may not be loaded into memory
     * 1. check if element exists
     * 2. give us element if exists
     * 3. tells us the page it is in.
     * How to find the correct page:
     *    binary-search on the pages, and go through the records of each page
     *
     *    returns the position of the record in the table
     */
    public Object[] getRecord(int table, Object[] key) throws StorageManagerException {

        Table target_table = tableMap.get(table);
        //call getPages in dataManager
        //call searchPages in pageBuffer to get the record page
        RecordPage  record = pageBuffer.searchPages(target_table, DataManager.getPages(target_table.getId()), key);
        return record.getRecord(target_table, key);
    }


    public void updateRecord(int tableId, Object[] record) throws StorageManagerException{
        Table table = getTable(tableId);

        pageBuffer.updateRecord(table, record);
    }

    public void removeRecord(int tableId, Object[] keyValue) throws StorageManagerException {
        Table table = getTable(tableId);
        pageBuffer.removeRecord(table, keyValue);
    }

    public void clearTable(int table) throws StorageManagerException {
        for(int pageID = 0; pageID <= getTable(table).getHighestPage(); pageID++) {
            DataManager.deletePage(table, pageID, PageTypes.RECORD_PAGE);
        }
        getTable(table).resetPages();
    }

    /**
     * Updates a table after it has been modified in the table map
     */
    public void updateTable(Table table){
        tableMap.put(table.getId(),table);
    }

    /**
     * tihs function loads a table into memory
     *
     * @throws StorageManagerException table does not exist
     */
     private Table loadTable(int id) throws StorageManagerException{
        Table table = null;
        try {
            table = DataManager.getTable(id);
        } catch (IOException e) {
            throw new StorageManagerException(String.format(StorageManager.TABLE_DNE_FORMAT, id));
        }
        tableMap.put(id,table);
        return table;
    }

    public Table getTable(int id) throws StorageManagerException {
        if(tableMap.get(id) == null) {
            return loadTable(id);
        }
        return tableMap.get(id);
    }

    public Map<Integer, Table> getTableMap() {
        return tableMap;
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * Functions to execute when the program is shut down
     */
    public void shutDown() throws StorageManagerException {
        pageBuffer.purge();
        // need to write out our tables as well
        for(Table table: tableMap.values()){
            DataManager.saveTable(table,table.getId());
        }
    }

}
