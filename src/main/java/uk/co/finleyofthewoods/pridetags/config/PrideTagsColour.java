package uk.co.finleyofthewoods.pridetags.config;

public class PrideTagsColour {
    private final String name;
    private final String colour;

    public PrideTagsColour(String name, String colour) {
        this.name = name;
        this.colour = colour;
    }

    public final String getName() {
        return this.name;
    }

    public String getColour() {
        return this.colour;
    }

    public boolean isEmpty() {
        return this.name == null;
    }

    public String toString() {
        return "PrideTagsTeam{name='" + this.name + "', colour='" + this.colour + "'}";
    }
}
