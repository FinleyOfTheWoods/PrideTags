package uk.co.finleyofthewoods.pridetags.config;

public class PrideTagsTeam {
    private final String name;
    private final String colour;
    private final String prefix;
    private final String suffix;

    public PrideTagsTeam(String name, String colour, String prefix, String suffix) {
        this.name = name;
        this.colour = colour;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public final String getName() {
        return this.name;
    }

    public String getColour() {
        return this.colour;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean hasPrefix() {
        return !this.prefix.isEmpty();
    }

    public String getSuffix() {
        return this.suffix;
    }

    public boolean hasSuffix() {
        return !this.suffix.isEmpty();
    }

    public boolean isEmpty() {
        return this.name == null;
    }

    public String toString() {
        return "PrideTagsTeam{name='" + this.name + "', colour='" + this.colour + "', prefix='" + this.prefix + "', suffix='" + this.suffix + "'}";
    }
}
