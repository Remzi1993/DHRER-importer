package nl.hva.oop.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a political party (e.g. "VVD", "D66", etc.),
 * stored per region, NOT globally.
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class Party {
    private final int listNumber; // e.g. "1" for VVD
    private final String name;    // e.g. "VVD"
    private int seats;
    private final List<Candidate> candidates;

    public Party(int listNumber, String name) {
        this.listNumber = listNumber;
        this.name = name;
        this.seats = 0;
        this.candidates = new ArrayList<>();
    }

    // Getters and setters
    public int getListNumber() {
        return listNumber;
    }

    public String getName() {
        return name;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void addCandidate(Candidate c) {
        candidates.add(c);
    }
}