package buffermanager.Page;

import buffermanager.BufferManager;
import buffermanager.PageBuffer;
import buffermanager.Table;
import storagemanager.StorageManagerException;

import java.io.Serializable;

public abstract class Page<E> implements Serializable, Comparable<Page> {

    transient int pageID;
    transient Table table;
    transient BufferManager bufferManager;
    transient PageBuffer pageBuffer;

    int entries = 0;

    public Page(int pageID, Table table, BufferManager bufferManager) {
        this.pageID = pageID;
        this.table = table;
        this.bufferManager = bufferManager;
    }

    public void setBufferManager(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public void setPageID(int pageID) {
        this.pageID = pageID;
    }

    public void setPageBuffer(PageBuffer pageBuffer) {
        this.pageBuffer = pageBuffer;
    }

    public int getPageID() {
        return pageID;
    }

    public int getEntriesCount() {
        return this.entries;
    }

    public abstract boolean insertRecord(E record) throws StorageManagerException;
    public abstract boolean deleteRecord(E record) throws StorageManagerException;
    public abstract boolean recordExists(E record);
    public abstract Page<E> splitPage();
    public abstract boolean hasSpace();

    @Override
    public int compareTo(Page page) {
        return pageID - page.pageID;
    }

}
