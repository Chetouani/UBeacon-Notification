package be.uchrony.ubeacon.metier;

import org.altbeacon.beacon.Beacon;

/**
 * Crée par Abdel le 28/02/2015.
 */
public class Ibeacon extends Beacon implements Comparable {

    public Ibeacon(Beacon otherBeacon) {
        super(otherBeacon);
    }

    @Override
    public int compareTo(Object o) {

        Ibeacon b1 = (Ibeacon) o;
        if (b1.getDistance() == this.getDistance())
            return 0;
        else if (b1.getDistance() > this.getDistance())
            return -1;
        else
            return 1;
    }
}