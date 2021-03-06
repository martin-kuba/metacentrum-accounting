package cz.cesnet.meta.pbs;

/**
 * Represents a resource in PBSPro.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsResource extends PbsInfoObject{

    public PbsResource(PBS pbs, String name) {
        super(pbs, name);
    }

    public String getType() {
        return attrs.get("type");
    }

    //hqnr
    public String getFlag() {
        return attrs.get("flag");
    }
}
