package nl.hva.oop.models;

/**
 * Represents a country with a name and a code.
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class Country {
    private final String name;
    private final String code;

    public Country(String name, String code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return "Country { " +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                " }";
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}