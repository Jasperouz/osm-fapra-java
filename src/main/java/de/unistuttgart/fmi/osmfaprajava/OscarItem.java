package de.unistuttgart.fmi.osmfaprajava;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OscarItem {
    private long id;
    private long osmid;
    private String type;
    private float[] bbox;
    private String[] k;
    private String[] v;

    public OscarItem() {
    }

    public long getId() {
        return id;
    }

    public long getOsmid() {
        return osmid;
    }

    public String getType() {
        return type;
    }

    public float[] getBbox() {
        return bbox;
    }

    public String[] getK() {
        return k;
    }

    public String[] getV() {
        return v;
    }
}
