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
    public boolean search(int key) {
        SkipListNode current = header;
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
        }

        current = current.forward[0];
        return (current != null) && (current.key == key);
    }

    public boolean delete(int key) {
        SkipListNode[] update = new SkipListNode[MAX_LEVEL + 1];
        SkipListNode current = header;

        // Traverse from the highest level to the lowest level to find the node to be deleted
        for (int i = level; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].key < key) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        // Move to the next node, which should be the node to delete
        current = current.forward[0];

        // Check if the node to be deleted is actually present and matches the key
        if (current != null && current.key == key) {
            // Update forward pointers for each level
            for (int i = 0; i <= level; i++) {
                // Only update pointers if the next node is the one to be deleted
                if (update[i].forward[i] != current) continue;
                update[i].forward[i] = current.forward[i];
            }

            // After removal, check if the highest level is now empty and adjust the list level
            while (level > 0 && header.forward[level] == null) {
                level--;
            }
            return true;
        }
        return false;
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
