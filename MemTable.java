import java.io.*;

// Stores most recent key-value pairs in memory (Currently 4 GB)
// All data is stored in Memory (making for fast look ups, inserts and deletions)
// Whenever the MemTable reaches MAXIMUM_CAPACITY we flush it to disk in the form of a CSV file

// Change MAXIMUM_CAPACITY based on how much memory you are willing to offer
// Change BASE_PATH to wherever you want to store the Key_Value pairs on disk

public class MemTable {
    private static int MAXIMUM_CAPACITY; // Default Size 1GB

    private SkipList skipList;
    private long currentSize;
    private LSMTree tree;

    public MemTable(){
        MAXIMUM_CAPACITY = (int) (1 * Math.pow(1024, 3));
        skipList = new SkipList();
        currentSize = 0;
        tree = new LSMTree();
    }

    public MemTable(int GB) {
        this();
        MAXIMUM_CAPACITY = (int) (GB * Math.pow(1024, 3));
    }

    public boolean insert(int key, int val){
        int entrySize = (SkipListNode.KEY_SIZE + SkipListNode.VALUE_SIZE);

        //if we reach maximum capacity, we flush the data to disk (LSM Tree)
        if(entrySize + currentSize >= MAXIMUM_CAPACITY)
            flush();

        if (skipList.update(key, val))
            return true;

        if (skipList.insert(key, val)){
            currentSize += entrySize;
            return true;
        }

        return false;
    }

    public boolean delete(int key){
        if (skipList.delete(key)) {
            currentSize -= (SkipListNode.KEY_SIZE + SkipListNode.VALUE_SIZE);
            return true;
        }

        return false;
    }

    public boolean update(int key, int value){
        if (!skipList.update(key, value)) {
            // If the key does not exist, insert it as a new key-value pair
            return insert(key, value);
        }
        return true;
    }

    public Integer search(int key){
        Integer value = skipList.search(key);

        // Explicitly handle potential null values
        if (value == null) {
            // If value is TOMBSTONE or not found in SkipList, check in LSMTree
            Integer treeValue = tree.search(key);
            if (treeValue != null && treeValue != SkipList.TOMBSTONE) {
                return treeValue; // Key found in LSM tree and not marked as deleted
            }
            return null; // Key not found or deleted
        }

        else if(value == SkipList.TOMBSTONE)
            return null;

        return value; // Return the found value from SkipList
    }

    private void flush() {
        // convert to SSTable and add it to the LSM Tree
        System.out.println("RAM Storage exceeded, flushing data to DISK: lvl 0");

        // something went wrong
        if (!tree.createSSTable(tree.BASE_PATH + "lvl0", skipList))
            return;

        // make skiptList empty, update the size of it, and update how many Files we have on disk
        this.skipList = new SkipList();
        this.currentSize = 0;
        this.tree.currentDiskCount++;
        tree.compactLevel(0, 1);
    }


    public void print(){
        skipList.print();
    }

}
