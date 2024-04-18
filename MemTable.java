import java.io.*;

// Stores most recent key-value pairs in memory (Currently 4 GB)
// All data is stored in Memory (making for fast look ups, inserts and deletions)
// Whenever the MemTable reaches MAXIMUM_CAPACITY we flush it to disk in the form of a CSV file

// Change MAXIMUM_CAPACITY based on how much memory you are willing to offer
// Change BASE_PATH to wherever you want to store the Key_Value pairs on disk

public class MemTable {
    private static final int MAXIMUM_CAPACITY = (int) (1 * Math.pow(1024, 3)); // 1GB

    private SkipList skipList;
    private long currentSize;
    private LSMTree tree;

    public MemTable() {
        skipList = new SkipList();
        currentSize = 0;
        tree = new LSMTree();
    }

    public boolean insert(int key, int val){
        int entrySize = (SkipListNode.KEY_SIZE + SkipListNode.VALUE_SIZE);

        //if we reach maximum capacity, we flush the data to disk (LSM Tree)
        if(entrySize + currentSize >= MAXIMUM_CAPACITY)
            flush();

        if (skipList.insert(key, val)){
            currentSize += entrySize;
            return true;
        }

        return false;
    }

    public boolean delete(int key){
        if(skipList.delete(key)) {
            currentSize -= (SkipListNode.KEY_SIZE + SkipListNode.VALUE_SIZE);
            // tree.insert(key, tree.TOMBSTONE);
        }

        return false;
    }

    public boolean update(int key, int value){
        return skipList.update(key, value);
    }

    public boolean search(int key){
        return skipList.search(key);
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
