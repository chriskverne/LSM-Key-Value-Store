public class SkipListNode {
    int key;
    int value;
    SkipListNode[] forward; // array to hold references to node of different levels

    // Change the KEY_SIZE based on how many key-value pairs you want to store
    // Change the VALUE_SIZE based on how large data you want to store
    public static final int KEY_SIZE = 8; // 8B
    public static final int VALUE_SIZE = 1024; // 1KB

    public SkipListNode(int key, int value, int level) {
        this.key = key; // 32 bits
        this.value = value; // Treated as 1KB

        // array of each next SkipListNode this node holds
        this.forward = new SkipListNode[level + 1]; // 0 based index
    }
}
