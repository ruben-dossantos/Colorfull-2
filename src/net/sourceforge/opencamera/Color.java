package net.sourceforge.opencamera;

public class Color {

    private String name;
    private String hex;

    public Color(String hex, String name){
        this.setName(name);
        this.setHex(hex);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}
