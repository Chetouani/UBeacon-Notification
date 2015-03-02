package be.uchrony.ubeacon.metier;

/**
 * Crée par Abdel le 28/02/2015.
 */

public class UBeacon {

    private String ibeacon_id;
    private String ibeacon_title;
    private String ibeacon_uuid;
    private String ibeacon_major;
    private String ibeacon_minor;

    public UBeacon(String ibeacon_id, String ibeacon_title, String ibeacon_uuid
                    , String ibeacon_major, String ibeacon_minor) {
        this.ibeacon_id = ibeacon_id;
        this.ibeacon_title = ibeacon_title;
        this.ibeacon_uuid = ibeacon_uuid;
        this.ibeacon_major = ibeacon_major;
        this.ibeacon_minor = ibeacon_minor;
    }

    public String getUbeaconID() {
        return ibeacon_id;
    }

    public String getUbeaconTitre() {
        return ibeacon_title;
    }

    public String getUbeaconUuid() {
        return ibeacon_uuid;
    }

    public String getUbeaconMajor() {
        return ibeacon_major;
    }

    public String getUbeaconMinor() {
        return ibeacon_minor;
    }

    @Override
    public String toString() {
        return "[ " +
                "ID = " + ibeacon_id +
                ", Title = " + ibeacon_title +
                ", Uuid = " + ibeacon_uuid  +
                ", Major = " + ibeacon_major  +
                ", Minor = " + ibeacon_minor  +
                ']';
    }

}
