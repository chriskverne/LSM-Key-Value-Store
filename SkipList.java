import java.util.Random;
// implemented based on information from this video https://www.youtube.com/watch?v=NDGpsfwAaqo
// skip lists are very efficient for searching, inserting and deletion which is the primary purpose of the MemTable
// they combine the best of both worlds of Arrays (searches, O(log n)) and LinkedList (update, O(1))
// as data is stored in RAM we want to make it as fast as possible, making this a perfect data structure for this purpose

// each Node will have at least 1 level
// the nodes are connected to the same nodes in the same level, here's a representation

// Level 2: [Header] ----------------------> [4]
// Level 1: [Header] --------> [2] --------> [4] -> [5]
// Level 0: [Header] -> [1] -> [2] -> [3] -> [4] -> [5] -> [6]

// we search by starting at the top level and going downwards whenever we reach a value that's greater than desired

public class SkipList {
    private static final int MAX_LEVEL = 16; // maximum level for this skip list
    private static final double P = 0.5; // probability factor
    public static final int TOMBSTONE = Integer.MIN_VALUE; // marks a key-value pair deleted
    public SkipListNode header;
    private int level;
    private Random random;

    public SkipList(){
        header = new SkipListNode(Integer.MIN_VALUE, 0, MAX_LEVEL); // initializes the top node in the SkipList
        level = 0;
        random = new Random();
    }

    // Generates a random lvl for each Node, we need to randomize this so delete operations don't destroy the data structure
    private int randomLevel() {
        int lvl = 0;
        while (lvl < MAX_LEVEL - 1 && random.nextDouble() < P) {
            lvl++;
        }
        return lvl;
    }

    public boolean insert(int key, int value) {
        // keeps track on the path we take
        SkipListNode[] update = new SkipListNode[MAX_LEVEL + 1];
        SkipListNode current = header;

        // start from top level, traverse downwards
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
            // update path
            update[i] = current;
        }

        // move to where we potentially want to insert
        current = current.forward[0];

        // if the key already exists, update the value
        if (current != null && current.key == key) {
            current.value = value; // Update the existing key with the new value (or TOMBSTONE)
            return true;
        }

        if (current == null || current.key != key) {
            int newLevel = randomLevel();

            // if the new level of the node we try to insert is greater than our current level
            // point header to the higher levels and update the current level
            if (newLevel > level) {
                for (int i = level + 1; i <= newLevel; i++) {
                    update[i] = header;
                }
                level = newLevel;
            }

            // insert new node by moving pointers
            SkipListNode newNode = new SkipListNode(key, value, newLevel);
            for (int i = 0; i <= newLevel; i++) {
                newNode.forward[i] = update[i].forward[i];
                update[i].forward[i] = newNode;
            }
            return true;
        }
        return false;
    }

    // searches by starting at the top level and moving downwards
    public Integer search(int key) {
        SkipListNode current = header;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
        }

        current = current.forward[0];
        if(current != null && current.key == key){
            if (current.value == TOMBSTONE) // we found a deleted key-value pair
                return TOMBSTONE;
            return current.value; // valid key-value pair found
        }

        return null; // key not found in memory
    }

    public boolean delete(int key) {
        return insert(key, TOMBSTONE);
    }

    public boolean update(int key, int newValue) {
        SkipListNode current = header;

        // Start from the highest level and move downwards
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
        }

        // Check the next node at the base level
        current = current.forward[0];

        // If we find the key, update value
        if (current != null && current.key == key) {
            current.value = newValue; // Update the value
            return true;
        }

        return false;
    }

    public void print() {
        System.out.println("Complete Skip List Structure:");
        for (int i = level; i >= 0; i--) {
            System.out.print("Level " + i + ": [Header]");
            SkipListNode node = header.forward[i];
            while (node != null) {
                System.out.print(" -> " + "(key: " + node.key + ", value: " + node.value + ")");
                node = node.forward[i];
            }
            System.out.println(" -> null");
        }
    }
}
