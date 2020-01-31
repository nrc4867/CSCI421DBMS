package testing;

import storagemanager.buffermanager.BufferManager;
import storagemanager.StorageManager;
import storagemanager.StorageManagerException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class BufferManagerTests {
    /**
     * I had some time so I thought i'd create a buffer manager testing file.
     * This is just to get some ground work on constructing pages for the database ect. Basically just to see how it
     * work...
     */

    public static void main(String... args) throws StorageManagerException {
        BufferManager bufferManager = new BufferManager("/",100,4096);
        // also testing some storage manager stuff
        try{
            // the paramaters don't matter right now...
            StorageManager storageManager = new StorageManager("/",100,4096,false);
            testCreateTable(storageManager);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            testWritePage(bufferManager);
        } catch (IOException | StorageManagerException e) {
            e.printStackTrace();
        }

        // time for the money
        try {
            testSplitting(bufferManager);
        } catch (IOException e) {
            e.printStackTrace();
        }

        testShutDown(bufferManager);

    }

    /**
     * Basic method to create a table in a folder.
     */
    public static void testCreateTable(StorageManager storageManager) throws StorageManagerException {
        storageManager.addTable(0, new String[]{"Integer", "varChar(3)", "boolean", "char(3)"}, new Integer[]{0, 2, 3});
    }

    /**
     * Testing writing a record to a page.
     */
    public static void testWritePage(BufferManager bufferManager) throws StorageManagerException, IOException {
        bufferManager.insertRecord(0,new Object[]{0, new char[]{'t', 'e', 's', 't'}, true, "123".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{0, "test".toCharArray(), true, "124".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{2, "test".toCharArray(), false, "123".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{3, "test".toCharArray(), false, "123".toCharArray()});
        bufferManager.insertRecord(0,new Object[]{0, "a".toCharArray(), false, "123".toCharArray()});
    }

    /**
     * "Ah shit, here we go again" - 'C.J' Johnson, From the Grand Theft Auto Series, 'Grand Theft Auto: San Andreas'
     */
    public static void testSplitting(BufferManager bufferManager) throws IOException {
        // casually writing 500 entries. rip console output

        for(int i=0; i<500; i++){
            System.out.println("Inserting " + i);
            Object[] data = new Object[]{i, "a".toCharArray(), false, "123".toCharArray()};
            try {
                bufferManager.insertRecord(0,data);
            } catch (StorageManagerException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Simple test to see how the program handles shutting down
     * @param bufferManager
     */
    public static void testShutDown(BufferManager bufferManager){
        bufferManager.shutDown();
    }
}
