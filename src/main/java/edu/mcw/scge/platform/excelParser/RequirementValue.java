package edu.mcw.scge.platform.excelParser;

/**
 * Normalizes the various spellings used in the spreadsheet (e.g. "Y", "Yes",
 * "yes") to the canonical value that should be stored in the database.
 */
public enum RequirementValue {
    YES("Yes", "y", "yes"),
    NO("No", "n", "no"),
    MAYBE("Maybe", "m", "maybe");

    private final String dbValue;
    private final String[] aliases;

    RequirementValue(String dbValue, String... aliases) {
        this.dbValue = dbValue;
        this.aliases = aliases;
    }

    public String getDbValue() {
        return dbValue;
    }

    /**
     * Returns the canonical database value for the given raw cell text, or the
     * trimmed input unchanged when it does not match a known variation.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        for (RequirementValue rv : values()) {
            if (value.equalsIgnoreCase(rv.dbValue)) {
                return rv.dbValue;
            }
            for (String alias : rv.aliases) {
                if (value.equalsIgnoreCase(alias)) {
                    return rv.dbValue;
                }
            }
        }
        return value;
    }
}
