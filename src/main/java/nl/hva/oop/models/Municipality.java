package nl.hva.oop.models;

/**
 * Represents a municipality with a name, a code and a province.
 * @author Remzi Cavdar - ict@remzi.info - <a href="https://remzi.info">remzi.info</a>
 */
public class Municipality {
    private final String name;
    private final String code;
    private final Province province;

    public Municipality(String name, String code, Province province) {
        this.name = name;
        this.code = code;
        this.province = province;
    }

    @Override
    public String toString() {
        return "Municipality { " +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", province=" + (province != null ? province.getName() : "null") +
                " }";
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Province getProvince() {
        return province;
    }
}