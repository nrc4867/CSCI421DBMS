package buffermanager.Page;

import buffermanager.BufferManager;
import buffermanager.Table;
import storagemanager.StorageManagerException;

public class IndexPage extends Page<Object[]> {
    public IndexPage(int pageID, Table table, BufferManager bufferManager) {
        super(pageID, table, bufferManager);
    }

    @Override
    public void insertRecord(Object[] record) throws StorageManagerException {

    }

    @Override
    public void deleteRecord(Object[] record) throws StorageManagerException {

    }

    @Override
    public boolean recordExists(Object[] record) {
        return false;
    }


    @Override
    public Page<Object[]> splitPage() {
        return null;
    }

    @Override
    public boolean hasSpace() {
        return false;
    }
}
