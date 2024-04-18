public class main {
    public static void main(String[] args) {
        // This is our database
        MemTable table = new MemTable();

        // Fills up the Database with Data
        // Notice how we start by filling up data to RAM and when it reaches its capacity we flush it to different disk levels
        for(int j = 0; j < 4; j++) {
            for (int i = 0; i < 10_000_000; i++)
                table.insert(i, i);
        }


    }
}
