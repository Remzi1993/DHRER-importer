package nl.hva.oop.utils;

import nl.hva.oop.models.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Imports election data from a CSV file, storing it in memory.
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class CSVImporter {
    private final List<Country> country = new ArrayList<>();
    private final List<Province> provinces = new ArrayList<>();
    private final List<Municipality> municipalities = new ArrayList<>();
    private final Map<String, Country> countryByCode = new HashMap<>();
    private final Map<String, Province> provinceByCode = new HashMap<>();

    // Store country stats in a map:
    private final Map<String, Long> countryStats = new LinkedHashMap<>();

    // Keep a "global" Party map (Key = partyName, Value = Party)
    private final Map<String, Party> globalPartyMap = new HashMap<>();

    // Also store region -> (partyName -> Party), so each region has its own Party objects.
    private final Map<String, Map<String, Party>> regionPartyMap = new HashMap<>();

    private static final String SEPARATOR = ";";

    /**
     * Imports the CSV file, parsing it line by line.
     * @param csvFilePath Path to the CSV file
     */
    public void importCSV(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // Skip the header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Split into columns
                String[] cols = line.split(SEPARATOR, -1);
                if (cols.length < 16) {
                    continue;
                }

                String regio = cols[0].trim();
                String regioCode = cols[1].trim();
                String ouderRegioCode = cols[2].trim();
                String grootOuderRegioCode = cols[3].trim();

                // Potential fields for stats in col[14], col[15]
                String veldType = cols[14].trim();
                String veldWaarde = cols[15].trim();

                // If it's "Nederland;L528" => country lines
                if (regio.equalsIgnoreCase("Nederland") && regioCode.equalsIgnoreCase("L528")) {
                    /*
                     * Only store *actual* country statistics in countryStats map.
                     * That is, skip lines whose veldType is "KandidaatAantalStemmen",
                     * "KandidaatGekozen", "LijstAantalZetels", "LijstAantalStemmen", etc.
                     */
                    if (!veldType.isEmpty() && !veldWaarde.isEmpty()) {
                        if (isCountryStat(veldType)) {
                            try {
                                long value = Long.parseLong(veldWaarde);
                                String humanReadableKey = FieldNameFormatter.toHumanReadable(veldType);
                                countryStats.put(humanReadableKey, value);
                            } catch (NumberFormatException ignored) {
                                // col[15] might not be numeric
                            }
                        }
                    }
                    findOrCreateCountry(regio, regioCode);

                    // Also parse any party/candidate data at the country level
                    // (e.g. "KandidaatAantalStemmen", "LijstAantalZetels", etc.)
                    handlePartyLine(cols, "Nederland");
                    continue;
                }

                // Province detection if e.g. "Groningen;P20;L528;..."
                if (regioCode.startsWith("P") && ouderRegioCode.equalsIgnoreCase("L528")) {
                    findOrCreateProvince(regio, regioCode);
                    handlePartyLine(cols, regio);
                    continue;
                }

                // Municipality detection if e.g. "Amsterdam;K9;P27;L528;..."
                if (regioCode.startsWith("K") || regioCode.startsWith("G")) {
                    Province parentProv = provinceByCode.get(ouderRegioCode);
                    if (parentProv != null) {
                        findOrCreateMunicipality(regio, regioCode, parentProv);
                    } else {
                        Province parent2 = provinceByCode.get(grootOuderRegioCode);
                        if (parent2 != null) {
                            findOrCreateMunicipality(regio, regioCode, parent2);
                        }
                    }
                    handlePartyLine(cols, regio);
                    continue;
                }

                // Otherwise, still parse any party line for that "regio"
                handlePartyLine(cols, regio);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return true if the veldType is actually one of the country statistics
     * we want to store (e.g. "AantalBlancoStemmen", "AantalGeldigeStemmen",
     * "AantalOngeldigeStemmen", "Kiesgerechtigden", "Opkomst", etc.).
     * Return false if it's something like "KandidaatGekozen", "LijstAantalZetels", etc.
     */
    private boolean isCountryStat(String veldType) {
        Set<String> validStats = new HashSet<>(Arrays.asList(
                "AantalBlancoStemmen",
                "AantalGeldigeStemmen",
                "AantalOngeldigeStemmen",
                "Kiesgerechtigden",
                "Opkomst"
        ));
        return validStats.contains(veldType);
    }

    /**
     * Creates/updates a Party or Candidate if the veldType is
     * "LijstAantalZetels", "KandidaatGekozen", or "KandidaatAantalStemmen".
     * Also updates both region-based AND global party maps.
     */
    private void handlePartyLine(String[] cols, String regionName) {
        // col[5] => "LijstNummer"
        String listNumberStr = cols[5].trim();
        int listNumber = parseIntSafe(listNumberStr, -1);

        // col[6] => "LijstNaam" => partyName
        String partyName = cols[6].trim();
        if (partyName.isEmpty()) {
            return;
        }

        // col[14] => veldType
        // col[15] => veldWaarde
        String veldType = cols[14].trim();
        String veldWaarde = cols[15].trim();

        // Region-based map
        Map<String, Party> partiesInRegion =
                regionPartyMap.computeIfAbsent(regionName, _ -> new HashMap<>());

        Party partyInRegion = partiesInRegion.computeIfAbsent(
                partyName, name -> new Party(listNumber, name));

        // Global map
        Party globalParty = globalPartyMap.computeIfAbsent(
                partyName, name -> new Party(listNumber, name));

        switch (veldType) {
            case "LijstAantalZetels": {
                if (!veldWaarde.isEmpty()) {
                    int seats = parseIntSafe(veldWaarde, 0);
                    partyInRegion.setSeats(seats);
                    globalParty.setSeats(seats);
                }
                break;
            }
            case "KandidaatGekozen": {
                Candidate candidateRegion = parseCandidate(cols, partyInRegion);
                if (candidateRegion != null) {
                    candidateRegion.setChosen(true);
                }
                Candidate candidateGlobal = parseCandidate(cols, globalParty);
                if (candidateGlobal != null) {
                    candidateGlobal.setChosen(true);
                }
                break;
            }
            case "KandidaatAantalStemmen": {
                if (!veldWaarde.isEmpty()) {
                    int votes = parseIntSafe(veldWaarde, 0);

                    Candidate candidateRegion = parseCandidate(cols, partyInRegion);
                    if (candidateRegion != null) {
                        // Only update if new votes > existing
                        if (votes > candidateRegion.getTotalVotes()) {
                            candidateRegion.setTotalVotes(votes);
                        }
                    }

                    Candidate candidateGlobal = parseCandidate(cols, globalParty);
                    if (candidateGlobal != null) {
                        // Same logic: only store if higher
                        if (votes > candidateGlobal.getTotalVotes()) {
                            candidateGlobal.setTotalVotes(votes);
                        }
                    }
                }
                break;
            }
            // ignore others
            default:
                break;
        }
    }

    /**
     * Parses a candidate from the CSV line, adding it to the party if it doesn't exist.
     *
     * @param cols CSV columns
     * @param party Party object
     * @return Candidate object if created, null if not
     */
    private Candidate parseCandidate(String[] cols, Party party) {
        int candidateNumber = parseIntSafe(cols[7].trim(), -1);
        if (candidateNumber < 0) {
            return null;
        }

        String initials = cols[8].trim();
        String firstName = cols[9].trim();
        String prefix = cols[10].trim();
        String lastName = cols[11].trim();
        String residence = cols[12].trim();
        String sex = cols[13].trim();

        // Check if a candidate already exists
        for (Candidate c : party.getCandidates()) {
            if (c.getCandidateNumber() == candidateNumber) {
                return c;
            }
        }

        // Otherwise create
        Candidate newC = new Candidate(party, candidateNumber, initials, firstName, prefix, lastName, residence, sex);
        party.addCandidate(newC);
        return newC;
    }

    /**
     * Safely parses an integer from a string, returning a default value if it fails.
     * @param str String to parse
     * @param defaultVal Default value
     * @return Parsed integer or default value
     */
    private int parseIntSafe(String str, int defaultVal) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * Creates a new Country object if it doesn't exist, or returns the existing one.
     * @param name Country name
     * @param code Country code
     * @return Country object
     */
    private Country findOrCreateCountry(String name, String code) {
        Country existing = countryByCode.get(code);
        if (existing != null) {
            return existing;
        }
        Country c = new Country(name, code);
        country.add(c);
        countryByCode.put(code, c);
        return c;
    }

    /**
     * Creates a new Province object if it doesn't exist, or returns the existing one.
     * @param name Province name
     * @param code Province code
     * @return Province object
     */
    private Province findOrCreateProvince(String name, String code) {
        Province existing = provinceByCode.get(code);
        if (existing != null) {
            return existing;
        }
        Country country = findOrCreateCountry("Nederland", "L528");
        Province p = new Province(name, code, country);
        provinces.add(p);
        provinceByCode.put(code, p);
        return p;
    }

    /**
     * Creates a new Municipality object if it doesn't exist, or returns the existing one.
     * @param name Municipality name
     * @param code Municipality code
     * @param province Parent province
     * @return Municipality object
     */
    private Municipality findOrCreateMunicipality(String name, String code, Province province) {
        Municipality m = new Municipality(name, code, province);
        municipalities.add(m);
        return m;
    }

    // Getters
    public Map<String, Long> getCountryStats() {
        return countryStats;
    }

    /**
     * So you can pick a region (municipality, province, 'Nederland'), etc.
     * fetch the parties, then see seats/candidates/votes.
     */
    public Map<String, Map<String, Party>> getRegionPartyMap() {
        return regionPartyMap;
    }

    /**
     * Returns the global party map, preserving older functionality.
     */
    public Map<String, Party> getPartyMap() {
        return globalPartyMap;
    }
}