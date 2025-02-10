package nl.hva.oop;

import nl.hva.oop.models.*;
import nl.hva.oop.utils.CSVImporter;
import java.util.*;
import static nl.hva.oop.utils.ConsoleColors.*;

/**
 * Main entry point for the console application.
 * It prints global stats, lists parties, and then
 * allows interactive user input to query municipalities, provinces, or "Nederland".
 * Example how you use the CSVImporter class to import data from a CSV file.
 *
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class Main {
    /**
     * Normalize region names by removing all whitespace
     * and converting to lowercase (e.g. "  Den Haag  " -> "denhaag").
     */
    private static String normalizeRegionName(String region) {
        return region.replaceAll("\\s+", "").toLowerCase();
    }

    public static void main(String[] args) {
        CSVImporter importer = new CSVImporter();

        // Import CSV
        String csvFilePath = "TK2023_uitslag.csv";
        long start = System.currentTimeMillis();
        importer.importCSV(csvFilePath);
        long end = System.currentTimeMillis();
        System.out.println("importCSV execution time (ms): " + (end - start));

        // Print top-level country stats
        System.out.println(BLUE_BOLD + "=== Tweede Kamer verkiezingen - landelijke statistieken ===" + RESET);
        for (Map.Entry<String, Long> entry : importer.getCountryStats().entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        // Print parties, sorted by seats desc, then name asc
        System.out.println(BLUE_BOLD + "\n=== Landelijke statistieken partijen ===" + RESET);
        Map<String, Party> partyMap = importer.getPartyMap();
        if (!partyMap.isEmpty()) {
            List<Party> sortedParties = new ArrayList<>(partyMap.values());
            sortedParties.sort(
                    Comparator
                            .comparingInt(Party::getSeats)
                            .reversed()
                            .thenComparing(Party::getName)
            );
            for (Party p : sortedParties) {
                System.out.println(YELLOW_BOLD + "- " + p.getName() + " => aantal zetels: " + p.getSeats() + RESET);
                for (Candidate c : p.getCandidates()) {
                    if (c.isChosen()) {
                        System.out.println("    #" + c.getCandidateNumber()
                                + " " + c.getFullName()
                                + " - [aantal stemmen = " + c.getTotalVotes() + "]");
                    }
                }
            }
        } else {
            System.out.println("No global parties found (using region-based approach?).");
        }

        // Region-based approach
        Map<String, Map<String, Party>> originalRegionPartyMap = importer.getRegionPartyMap();
        Map<String, Map<String, Party>> normalizedRegionPartyMap = new HashMap<>();

        // Build a normalized map: normalized key -> original region data
        for (String region : originalRegionPartyMap.keySet()) {
            String norm = normalizeRegionName(region);
            normalizedRegionPartyMap.put(norm, originalRegionPartyMap.get(region));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println(BLUE_BOLD + "\n=== Zoeken op ===" + RESET);
        System.out.println("Voer de naam van de gemeente of provincie in, of zoek op 'Nederland' om de statistieken te zien op gemeente-, provincie- of landelijk niveau.");
        System.out.println("Met het invoeren van 'exit' kan je de console app sluiten.");

        while (true) {
            // Prompt op een nieuwe regel (println)
            System.out.print(BLUE_BOLD + "Voer de naam in (of 'exit' om af te sluiten): " + RESET);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                System.out.println(YELLOW_BOLD + "Programma beëindigd." + RESET);
                break;
            }

            // Normalize input
            String normalizedInput = normalizeRegionName(input);

            // Check if we have region data
            if (!normalizedRegionPartyMap.containsKey(normalizedInput)) {
                System.err.println(RED_BOLD + "Geen data gevonden voor: " + RESET + input);
                continue;
            }

            Map<String, Party> partiesInRegion = normalizedRegionPartyMap.get(normalizedInput);
            if (partiesInRegion.isEmpty()) {
                System.err.println(RED_BOLD + "Geen partijen gevonden voor: " + RESET + input);
                continue;
            }

            // Sort parties by seats desc, then name asc
            List<Party> regionPartiesList = new ArrayList<>(partiesInRegion.values());
            regionPartiesList.sort(
                    Comparator
                            .comparingInt(Party::getSeats)
                            .reversed()
                            .thenComparing(Party::getName)
            );

            // 5) Print overview
            System.out.println(BLUE_BOLD + "\n\n==============================================================" + RESET);
            System.out.println(BLUE_BOLD + "====== Gezocht op: " + input + " ======" + RESET);

            for (Party party : regionPartiesList) {
                if (input.equalsIgnoreCase("Nederland")) {
                    System.out.println(YELLOW_BOLD + "Partij: " + party.getName()
                            + " - aantal zetels = " + party.getSeats()
                            + RESET);
                } else {
                    System.out.println(YELLOW_BOLD + "Partij: " + party.getName() + RESET);
                }

                List<Candidate> candidates = new ArrayList<>(party.getCandidates());
                candidates.sort(Comparator.comparingInt(Candidate::getCandidateNumber));

                for (Candidate c : candidates) {
                    System.out.println("   #" + c.getCandidateNumber()
                            + " " + c.getFullName()
                            + " - [aantal stemmen=" + c.getTotalVotes() + "]");
                }
            }
        }

        scanner.close();
    }
}