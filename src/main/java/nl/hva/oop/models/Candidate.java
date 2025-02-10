package nl.hva.oop.models;

/**
 * Represents a candidate for a political party.
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class Candidate {
    private final Party party;
    private final int candidateNumber;
    private final String initials;
    private final String firstName;
    private final String prefix;
    private final String lastName;
    private final String residence;
    private final String sex;
    private boolean chosen;
    private int totalVotes;

    public Candidate(Party party, int candidateNumber, String initials, String firstName, String prefix,
                     String lastName, String residence, String sex) {
        this.party = party;
        this.candidateNumber = candidateNumber;
        this.initials = initials;
        this.firstName = firstName;
        this.prefix = prefix;
        this.lastName = lastName;
        this.residence = residence;
        this.sex = sex;
        this.chosen = false;
        this.totalVotes = 0;
    }

    /**
     * For example, "D. (Dilan) Yeşilgöz", with prefix if present.
     */
    public String getFullName() {
        String full = initials + " (" + firstName + ")";
        if (prefix != null && !prefix.isEmpty()) {
            full += " " + prefix;
        }
        full += " " + lastName;
        return full.trim();
    }

    // Getters and setters
    public Party getParty() {
        return party;
    }

    public int getCandidateNumber() {
        return candidateNumber;
    }

    public String getInitials() {
        return initials;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLastName() {
        return lastName;
    }

    public String getResidence() {
        return residence;
    }

    public String getSex() {
        return sex;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }
}