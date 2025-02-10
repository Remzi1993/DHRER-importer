package nl.hva.oop.models;

/**
 * Represents a province with a name, a code and a country.
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class Province {
    private final String name;
    private final String code;
    private final Country country;

    public Province(String name, String code, Country country) {
        this.name = name;
        this.code = code;
        this.country = country;
    }

    @Override
    public String toString() {
        return "Province { " +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", country=" + (country != null ? country.getName() : "null") +
                " }";
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Country getCountry() {
        return country;
    }
}