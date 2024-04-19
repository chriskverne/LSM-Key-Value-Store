import java.io.*;
import java.util.*;

public class LSMTree {
    public int currentDiskCount;
    public static final String BASE_PATH = "C:\\Users\\13054\\Desktop\\Final Project (Data Structures)\\disk";
    public static final int COMPACTION = 4;
    public static final int TOMBSTONE = Integer.MIN_VALUE;
    public static final int MAX_LEVEL = 3;

    public LSMTree(){
        this.currentDiskCount = 0;
        for (int i = 0; i <= MAX_LEVEL; i++)
            ensureDirectory(BASE_PATH + "lvl" + i);
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
                if (node.value == TOMBSTONE) // if we encounter a deleted key-value pair
                    out.write(node.key + ":" + "TOMBSTONE");
                else
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
        String currentLevelPath = BASE_PATH + "lvl" + currentLevel;
        String nextLevelPath = BASE_PATH + "lvl" + nextLevel;

        File currentLevelDir = new File(currentLevelPath);
        File[] files = currentLevelDir.listFiles();
        if (files == null || files.length < COMPACTION) return;

        // Sort files by modification time or name in reverse order if timestamps are used in names
        Arrays.sort(files, Collections.reverseOrder(Comparator.comparingLong(File::lastModified)));

        System.out.println("Compacting lvl " + currentLevel + " to lvl " + nextLevel);
        SortedMap<Integer, Integer> allEntries = new TreeMap<>();

        for (File file : files) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String[] entries = scanner.nextLine().split(",");
                    for (String entry : entries) {
                        String[] kv = entry.trim().split(":");
                        int key = Integer.parseInt(kv[0]);
                        int value = Integer.parseInt(kv[1]);
                        // Only put if not already present since we're processing from most recent to oldest
                        allEntries.putIfAbsent(key, value);
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

    public Integer search(int key){
        for (int level =0; level <= MAX_LEVEL; level++){
            File levelDir = new File(BASE_PATH + "lvl" + level);
            File[] sstableFiles = levelDir.listFiles();

            if (sstableFiles == null) continue;

            // Sort files by last modified (newest first)
            Arrays.sort(sstableFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            // Search each file for key-value
            for (File file : sstableFiles) {
                Integer result = searchFileForKey(file, key);
                if (result != null)
                    return result;  // Found key-value
            }
        }

        return null; // Key not found on disk
    }

    // Searches an individual SSTable for a key
    public Integer searchFileForKey(File file, int key){
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] entries = line.split(",");
                for (String entry : entries) {
                    String[] kv = entry.trim().split(":");
                    int foundKey = Integer.parseInt(kv[0].trim());
                    String valueStr = kv[1].trim();

                    if (foundKey == key) {
                        if (!"TOMBSTONE".equals(valueStr)) {
                            return Integer.parseInt(valueStr); // Return value only if it's not a tombstone
                        } else {
                            return null; // Return null immediately if a tombstone is found for this key
                        }
                    }
                }
            }
        } catch (IOException e){
            System.err.println("read error:  " + e.getMessage());
        }
        return null; // Key not found in this file after full scan
    }

}
