import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class LSMTree {
    public int currentDiskCount;
    public static final String BASE_PATH = "C:\\Users\\13054\\Desktop\\Final Project (Data Structures)\\disk";
    public static final int COMPACTION = 4;
    public static final int TOMBSTONE = Integer.MIN_VALUE;

    public LSMTree(){
        this.currentDiskCount = 0;
        ensureDirectory(BASE_PATH + "lvl0");
        ensureDirectory(BASE_PATH + "lvl1");
        ensureDirectory(BASE_PATH + "lvl2");
        ensureDirectory(BASE_PATH + "lvl3");
    }

    // creates the directories for where we will store our data on disk
    private void ensureDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.out.println("Failed to create directory: " + path);
            }
        }
    }

    // creates an individual SSTable at lvl 0
    public boolean createSSTable(String path, SkipList skipList){
        File csvFile = new File(path, "table_" + System.currentTimeMillis() + ".csv");

        try (BufferedWriter out = new BufferedWriter(new FileWriter(csvFile))) {
            SkipListNode node = skipList.header.forward[0];
            while (node != null) {
                out.write(node.key + ":" + node.value);
                node = node.forward[0];
                if (node != null)
                    out.write(", ");
            }
            out.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write SSTable: " + e.getMessage());
            return false;
        }

        return true;
    }

    // very important function, take a look at it (Took me a long time to Implement)
    public void compactLevel(int currentLevel, int nextLevel) {
        if (currentLevel >= 2)
            return;

        String currentLevelPath = BASE_PATH + "lvl" + currentLevel;
        String nextLevelPath = BASE_PATH + "lvl" + nextLevel;

        File currentLevelDir = new File(currentLevelPath);
        File[] files = currentLevelDir.listFiles();

        if (files != null && files.length >= COMPACTION) {
            System.out.println("Compacting lvl " + currentLevel + " to lvl " + nextLevel);
            SortedMap<Integer, Integer> allEntries = new TreeMap<>();

            // Merge all files at the current level into a single sorted map
            for (File file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        String[] entries = scanner.nextLine().split(",");
                        for (String entry : entries) {
                            String[] kv = entry.trim().split(":");
                            int key = Integer.parseInt(kv[0]);
                            int value = Integer.parseInt(kv[1]);
                            allEntries.put(key, value); // In case of duplicate keys, the value will be overwritten
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("Failed to read SSTable: " + e.getMessage());
                }
            }

            // Write the merged data to a new SSTable at the next level
            File newSSTable = new File(nextLevelPath, "table_" + System.currentTimeMillis() + ".csv");
            try (BufferedWriter out = new BufferedWriter(new FileWriter(newSSTable))) {
                for (Map.Entry<Integer, Integer> entry : allEntries.entrySet()) {
                    out.write(entry.getKey() + ":" + entry.getValue() + ",");
                }
                out.newLine();
            } catch (IOException e) {
                System.err.println("Failed to write merged SSTable to lvl" + nextLevel + ": " + e.getMessage());
            }

            // Delete old files after successful merge
            for (File file : files) {
                if (!file.delete()) {
                    System.err.println("Failed to delete " + file.getAbsolutePath());
                }
            }

            File nextLevelDir = new File(nextLevelPath);
            files = nextLevelDir.listFiles();

            if (files != null && files.length >= COMPACTION)
                compactLevel(nextLevel, nextLevel + 1);
        }
    }

    public boolean delete(int key){

        return false;
    }
}
