package g1t1.models.sessions;

/**
 * Module & Section. Used to uniquely identify a class.
 */
public class ModuleSection {
    /**
     * Module of the class. i.e CS102
     */
    private String module;

    /**
     * Section of the class. i.e G1
     */
    private String section;

    public ModuleSection(String module, String section) {
        this.module = module;
        this.section = section;
    }

    /**
     * Module of the class. i.e CS102
     */
    public String getModule() {
        return module;
    }

    /**
     * Section of the class. i.e G1
     */
    public String getSection() {
        return section;
    }

    @Override
    public String toString() {
        return "ModuleSection [module=" + module + ", section=" + section + "]";
    }
}
