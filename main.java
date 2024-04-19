import java.util.Random;
import java.util.stream.Stream;

public class main {
    public static Random random = new Random();

    public static void main(String[] args) {
        int memorySize = 1;

        // This is our database
        MemTable table = new MemTable(memorySize);

        // How many Key_value pairs you want to insert
        int amount = 10_000_000;

        // test 1: inserting O(log n) memory, Flushing takes O(n) and accesses HDD (Slow flushing)
        // The third argument is the maxIndex (largest index we can insert)
        // The smaller this value is, the more updates we will have
        test1(table, amount, amount / 2);

        // test2: searching O(log n) memory, O(n) worst case when reading HDD (Slow when reading from disk)
        test2(table, amount, 10);

        // test3 : delete O(1) always
        test3(table, amount, 10);
    }

    public static int get_random_val(int limit){
        return random.nextInt(limit);
    }

    public static void test1(MemTable table, int amount, int maxIndex){
        System.out.println("Starting Insertion");
        System.out.println("-----------------------------------------");
        for (int i = 0; i < amount; i++){
            int randInd = get_random_val(maxIndex);
            int randVal = get_random_val(amount);
            table.insert(randInd, randVal);
        }

        System.out.println("-----------------------------------------");
    }

    public static void test2(MemTable table, int tableSize, int amount){
        System.out.println();
        System.out.println("Starting search");
        System.out.println("-----------------------------------------");

        for (int i = 0; i < amount; i++){
            int randInd = get_random_val(tableSize);
            Integer val = table.search(randInd);

            if (val == null)
                System.out.println("Couldn't find index: " + randInd);
            else
                System.out.println("Index = " + randInd + " val = " + val);

        }

        System.out.println("-----------------------------------------");
    }

    public static void test3(MemTable table, int tableSize, int amount){
        System.out.println();
        System.out.println("Starting delete");
        System.out.println("-----------------------------------------");

        for (int i = 0; i < amount; i++){
            int randInd = get_random_val(tableSize);
            boolean deleted = table.delete(randInd);
            if (deleted)
                System.out.println("Index: " + randInd + " deleted successfully");
            else
                System.out.println("Couldn't find index: " + randInd + " to delete");
        }
        System.out.println("-----------------------------------------");
    }
}
