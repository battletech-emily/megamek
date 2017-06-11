/**
 * * MegaMek - Copyright (C) 2017 - The MegaMek Team
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 */
package megamek.common;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Handles the progression of technology through prototype, production, extinction and reintroduction
 * phases. Calculates current rules level for IS or Clan.
 * 
 * @author Neoancient
 *
 */
public class TechAdvancement implements ITechnology {	
	
    //Dates that are approximate can be pushed this many years earlier (or later for extinctions).
    public static final int APPROXIMATE_MARGIN = 5;
    
	public static final int PROTOTYPE    = 0;
	public static final int PRODUCTION   = 1;
	public static final int COMMON       = 2;
	public static final int EXTINCT      = 3;
	public static final int REINTRODUCED = 4;

    private int techBase = TECH_BASE_ALL;
    private int[] isAdvancement = new int[5];
    private int[] clanAdvancement = new int[5];
    private boolean[] isApproximate = new boolean[5];
    private boolean[] clanApproximate = new boolean[5];
    private int[] prototypeFactions = {};
    private int[] productionFactions = {};
    private int[] extinctionFactions = {};
    private int[] reintroductionFactions = {};
    private boolean isIntroLevel = false; //Whether RULES_STANDARD should be considered T_INTRO_BOXSET.
    private boolean unofficial = false;
    private int techRating = RATING_C;
    private int[] availability = new int[ERA_DA + 1];
    
    public TechAdvancement() {
        Arrays.fill(isAdvancement, DATE_NONE);
        Arrays.fill(clanAdvancement, DATE_NONE);
    }
    
    public TechAdvancement(int techBase) {
        this();
        this.techBase = techBase;
    }
    
    /**
     * Copy constructor
     */
    public TechAdvancement(TechAdvancement ta) {
        this(ta.techBase);
        System.arraycopy(ta.isAdvancement, 0, this.isAdvancement, 0, ta.isAdvancement.length);
        System.arraycopy(ta.clanAdvancement, 0, this.clanAdvancement, 0, ta.clanAdvancement.length);
        System.arraycopy(ta.isApproximate, 0, this.isApproximate, 0, ta.isApproximate.length);
        System.arraycopy(ta.clanApproximate, 0, this.clanApproximate, 0, ta.clanApproximate.length);
        if (prototypeFactions.length > 0) {
            this.prototypeFactions = new int[prototypeFactions.length];
            System.arraycopy(ta.productionFactions, 0, this.prototypeFactions, 0, ta.prototypeFactions.length);
        }
        if (prototypeFactions.length > 0) {
            this.prototypeFactions = new int[prototypeFactions.length];
            System.arraycopy(ta.productionFactions, 0, this.prototypeFactions, 0, ta.prototypeFactions.length);
        }
        if (extinctionFactions.length > 0) {
            this.extinctionFactions = new int[extinctionFactions.length];
            System.arraycopy(ta.extinctionFactions, 0, this.extinctionFactions, 0, ta.extinctionFactions.length);
        }
        if (reintroductionFactions.length > 0) {
            this.reintroductionFactions = new int[reintroductionFactions.length];
            System.arraycopy(ta.reintroductionFactions, 0, this.reintroductionFactions, 0, ta.reintroductionFactions.length);
        }
        this.isIntroLevel = ta.isIntroLevel;
        this.unofficial = ta.unofficial;
        this.techRating = ta.techRating;
        System.arraycopy(ta.availability, 0, this.availability, 0, ta.availability.length);
    }
    
    public TechAdvancement setTechBase(int base) {
        techBase = base;
        return this;
    }
    
    public int getTechBase() {
        return techBase;
    }
    
    /**
     * Provide years for prototype, production, common, extinction, and reintroduction for IS factions.
     * 
     * @param prog Up to five tech progression years. Missing levels should be marked by DATE_NONE.
     * @return a reference to this object
     */
    public TechAdvancement setISAdvancement(int... prog) {
        Arrays.fill(isAdvancement, DATE_NONE);
        System.arraycopy(prog, 0, isAdvancement, 0, Math.min(isAdvancement.length, prog.length));
        return this;
    }
    
    /**
     * Indicate whether the years for prototype, production, common, extinction, and reintroduction
     * for IS factions should be considered approximate.
     * 
     * @param prog Up to five tech progression years.
     * @return a reference to this object
     */
    public TechAdvancement setISApproximate(boolean... approx) {
        Arrays.fill(isApproximate, false);
        System.arraycopy(approx, 0, isApproximate, 0, Math.min(isApproximate.length, approx.length));
        return this;
    }
    
    /**
     * Provide years for prototype, production, common, extinction, and reintroduction for Clan factions.
     * 
     * @param prog Up to five tech progression years. Missing levels should be marked by DATE_NONE.
     * @return a reference to this object
     */
    public TechAdvancement setClanAdvancement(int... prog) {
        Arrays.fill(clanAdvancement, DATE_NONE);
        System.arraycopy(prog, 0, clanAdvancement, 0, Math.min(clanAdvancement.length, prog.length));
        return this;
    }
    
    /**
     * Indicate whether the years for prototype, production, common, extinction, and reintroduction
     * for Clan factions should be considered approximate.
     * 
     * @param prog Up to five tech progression years.
     * @return a reference to this object
     */
    public TechAdvancement setClanApproximate(boolean... approx) {
        Arrays.fill(clanApproximate, false);
        System.arraycopy(approx, 0, clanApproximate, 0, Math.min(clanApproximate.length, approx.length));
        return this;
    }
    
    /**
     * A convenience method that will set identical values for IS and Clan factions.
     * @param prog
     * @return
     */
    public TechAdvancement setAdvancement(int... prog) {
        setISAdvancement(prog);
        setClanAdvancement(prog);
        return this;
    }
    
    /**
     * A convenience method that will set identical values for IS and Clan factions.
     * @param prog
     * @return
     */
    public TechAdvancement setApproximate(boolean... approx) {
        setISApproximate(approx);
        setClanApproximate(approx);
        return this;
    }

    /**
     * Sets which factions developed a prototype.
     * 
     * @param factions A list of F_* faction constants
     * @return
     */
    public TechAdvancement setPrototypeFactions(int... factions) {
        prototypeFactions = factions;
        return this;
    }
    
    /**
     * 
     * @return A list of F_* constants that indicate which factions started prototype development.
     */
    public int[] getPrototypeFactions() {
        return prototypeFactions;
    }

    /**
     * Sets which factions started production before the technology was commonly available.
     * 
     * @param factions A list of F_* faction constants
     * @return A reference to this object.
     */
    public TechAdvancement setProductionFactions(int... factions) {
        productionFactions = factions;
        return this;
    }

    /**
     * 
     * @return A list of F_* constants that indicate which factions started production
     * before the technology was commonly available.
     */
    public int[] getProductionFactions() {
        return productionFactions;
    }

    /**
     * Sets the factions for which the technology became extinct.
     * 
     * @param factions A list of F_* faction constants
     * @return A reference to this object.
     */
    public TechAdvancement setExtinctionFactions(int... factions) {
        extinctionFactions = factions;
        return this;
    }

    /**
     * 
     * @return A list of F_* constants that indicate the factions for which the technology
     * became extinct.
     */
    public int[] getExtinctionFactions() {
        return extinctionFactions;
    }

    /**
     * Sets the factions which reintroduced technology that had been extinct.
     * 
     * @param factions A list of F_* faction constants
     * @return A reference to this object.
     */
    public TechAdvancement setReintroductionFactions(int... factions) {
        reintroductionFactions = factions;
        return this;
    }

    /**
     * 
     * @return A list of F_* constants that indicate the factions that reintroduced extinct technology.
     * became extinct.
     */
    public int[] getReintroductionFactions() {
        return reintroductionFactions;
    }

    /**
     * The prototype date for either Clan or IS factions. If the date is flagged as approximate,
     * the date returned will be earlier by the value of APPROXIMATE_MARGIN.
     */
    public int getPrototypeDate(boolean clan) {
        return getDate(PROTOTYPE, clan);
    }
    
    /**
     * The production date for either Clan or IS factions. If the date is flagged as approximate,
     * the date returned will be earlier by the value of APPROXIMATE_MARGIN.
     */
    public int getProductionDate(boolean clan) {
        return getDate(PRODUCTION, clan);
    }

    /**
     * The common date for either Clan or IS factions. If the date is flagged as approximate,
     * the date returned will be earlier by the value of APPROXIMATE_MARGIN.
     */
    public int getCommonDate(boolean clan) {
        return getDate(COMMON, clan);
    }

    /**
     * The extinction date for either Clan or IS factions. If the date is flagged as approximate,
     * the date returned will be later by the value of APPROXIMATE_MARGIN.
     */
    public int getExtinctionDate(boolean clan) {
        return getDate(EXTINCT, clan);
    }

    /**
     * The reintroduction date for either Clan or IS factions. If the date is flagged as approximate,
     * the date returned will be earlier by the value of APPROXIMATE_MARGIN.
     */
    public int getReintroductionDate(boolean clan) {
        return getDate(REINTRODUCED, clan);
    }
    
    /**
     * The year the technology first became available for Clan or IS factions, regardless
     * of production level, or APPROXIMATE_MARGIN years earlier if
     * marked as approximate.
     */
    public int getIntroductionDate(boolean clan) {
        if (getPrototypeDate(clan) > 0) {
            return getPrototypeDate(clan);
        }
        if (getProductionDate(clan) > 0) {
            return getProductionDate(clan);
        }
        return getCommonDate(clan);
    }
    
    /**
     * Convenience method for calculating approximations.
     */
    private int getDate(int index, boolean clan) {
        if (clan) {
            if (clanApproximate[index] && clanAdvancement[index] > 0) {
                return clanAdvancement[index] + ((index == EXTINCT)? 5 : -5);
            } else {
                return clanAdvancement[index];
            }
        } else {
            if (isApproximate[index] && isAdvancement[index] > 0) {
                return isAdvancement[index] + ((index == EXTINCT)? 5 : -5);
            } else {
                return isAdvancement[index];
            }
        }
    }
    
    /*
     * Methods which return universe-wide dates
     */
    
    public int getPrototypeDate() {
        return earliestDate(getDate(PROTOTYPE, false), getDate(PROTOTYPE, true));
    }
    
    public int getProductionDate() {
        return earliestDate(getDate(PRODUCTION, false), getDate(PRODUCTION, true));
    }
    
    public int getCommonDate() {
        return earliestDate(getDate(COMMON, false), getDate(COMMON, true));
    }

    /**
     * If the tech base is IS or Clan, returns the extinction date that matches the tech base. Otherwise
     * returns the later of the IS and Clan dates, or DATE_NONE if the tech has not gone extinct for both.
     * 
     * @return Universe-wide extinction date.
     */
    public int getExtinctionDate() {
        if (getTechBase() != TECH_BASE_ALL) {
            return getDate(EXTINCT, getTechBase() == TECH_BASE_CLAN);
        }
        if (isAdvancement[EXTINCT] == DATE_NONE
                || clanAdvancement[EXTINCT] == DATE_NONE) {
            return DATE_NONE;
        }
        return Math.max(getDate(EXTINCT, false), getDate(EXTINCT, true));
    }
    
    public int getReintroductionDate() {
        if (getTechBase() != TECH_BASE_ALL) {
            return getDate(REINTRODUCED, getTechBase() == TECH_BASE_CLAN);
        }
        return earliestDate(getDate(REINTRODUCED, false), getDate(REINTRODUCED, true));
    }
    
    public int getIntroductionDate() {
        if (getPrototypeDate() > 0) {
            return getPrototypeDate();
        }
        if (getProductionDate() > 0) {
            return getProductionDate();
        }
        return getCommonDate();
    }
    
    /**
     * Formats the date at an index for display in a table, showing DATE_NONE as "-" and prepending
     * "~" to approximate dates.
     * 
     * @param index PROTOTYPE, PRODUCTION, COMMON, EXTINCT, or REINTRODUCED
     * @param clan  Use the Clan progression
     * @param factions  A list of factions to include in parentheses after the date.
     * @return
     */
    private String formatDate(int index, boolean clan, int[] factions) {
        int date = clan? clanAdvancement[index] : isAdvancement[index];
        if (date == DATE_NONE) {
            return "-";
        }
        StringBuilder sb = new StringBuilder();
        if (clan? clanApproximate[index] : isApproximate[index]) {
            sb.append("~");
        }
        if (date == DATE_PS) {
            sb.append("PS");
        } else if (date == DATE_ES) {
            sb.append("ES");
        } else {
            sb.append(date);
        }
        if (factions != null && factions.length > 0) {
            StringJoiner sj = new StringJoiner(",");
            for (int f : factions) {
                if ((clan && f >= F_CLAN) || (!clan && f < F_CLAN)) {
                    sj.add(IO_FACTION_CODES[f]);
                }
            }
            if (sj.length() > 0) {
                sb.append("(").append(sj.toString()).append(")");
            }
        }
        return sb.toString();
    }
    
    /**
     * Formats prototype date indicating approximate when appropriate, and prototype faction if any
     * for either IS or Clan use tech base.
     */
    public String getPrototypeDateName(boolean clan) {
        return formatDate(PROTOTYPE, clan, prototypeFactions);
    }
    
    /**
     * Formats earliest of Clan or IS prototype date indicating approximate when appropriate,
     * and prototype faction if any for mixed tech.
     */
    public String getPrototypeDateName() {
        boolean useClanDate = isAdvancement[PROTOTYPE] == DATE_NONE
                || (clanAdvancement[PROTOTYPE] != DATE_NONE && clanAdvancement[PROTOTYPE] < isAdvancement[PROTOTYPE]);
        return formatDate(PROTOTYPE, useClanDate, prototypeFactions);
    }
    
    /**
     * Formats production date indicating approximate when appropriate, and production faction if any
     * for either IS or Clan use tech base.
     */
    public String getProductionDateName(boolean clan) {
        return formatDate(PRODUCTION, clan, productionFactions);
    }
    
    /**
     * Formats earliest of Clan or IS production date indicating approximate when appropriate,
     * and production faction if any for mixed tech.
     */
    public String getProductionDateName() {
        boolean useClanDate = isAdvancement[PRODUCTION] == DATE_NONE
                || (clanAdvancement[PRODUCTION] != DATE_NONE && clanAdvancement[PRODUCTION] < isAdvancement[PRODUCTION]);
        return formatDate(PRODUCTION, useClanDate, productionFactions);
    }
    
    /**
     * Formats common date indicating approximate when appropriate.
     */
    public String getCommonDateName(boolean clan) {
        return formatDate(COMMON, clan, null);
    }
    
    /**
     * Formats earliest of Clan or IS common date indicating approximate when appropriate for mixed tech.
     */
    public String getCommonDateName() {
        boolean useClanDate = isAdvancement[COMMON] == DATE_NONE
                || (clanAdvancement[COMMON] != DATE_NONE && clanAdvancement[COMMON] < isAdvancement[COMMON]);
        return formatDate(COMMON, useClanDate, null);
    }
    
    /**
     * Formats extinction date indicating approximate when appropriate, and extinction faction if any
     * for either IS or Clan use tech base.
     */
    public String getExtinctionDateName(boolean clan) {
        return formatDate(EXTINCT, clan, extinctionFactions);
    }
    
    /**
     * Formats latest of Clan or IS extinction date indicating approximate when appropriate,
     * and extinction faction if any for mixed tech.
     */
    public String getExtinctionDateName() {
        if (techBase == TECH_BASE_ALL) {
            if (isAdvancement[EXTINCT] == DATE_NONE) {
                return getExtinctionDateName(false);
            } else if (clanAdvancement[EXTINCT] == DATE_NONE) {
                return getExtinctionDateName(true);
            } else {
                return formatDate(EXTINCT, clanAdvancement[EXTINCT] > isAdvancement[EXTINCT], extinctionFactions);                
            }
        } else {
            return getExtinctionDateName(techBase == TECH_BASE_CLAN);
        }
    }
    
    /**
     * Formats reintroduction date indicating approximate when appropriate, and reintroduction faction if any
     * for either IS or Clan use tech base.
     */
    public String getReintroductionDateName(boolean clan) {
        return formatDate(REINTRODUCED, clan, reintroductionFactions);
    }
    
    /**
     * Formats earliest of Clan or IS reintroduction date indicating approximate when appropriate,
     * and reintroduction faction if any for mixed tech.
     */
    public String getReintroductionDateName() {
        boolean useClanDate = isAdvancement[REINTRODUCED] == DATE_NONE
                || (clanAdvancement[REINTRODUCED] != DATE_NONE && clanAdvancement[REINTRODUCED] < isAdvancement[REINTRODUCED]);
        return formatDate(REINTRODUCED, useClanDate, reintroductionFactions);
    }
    
    /**
     * Finds the earliest of two dates, ignoring DATE_NA unless both values are set to DATE_NA
     */
    public static int earliestDate(int d1, int d2) {
        if (d1 < 0) {
            return d2;
        }
        if (d2 < 0) {
            return d1;
        }
        return Math.min(d1, d2);
    }
    
    public boolean isIntroLevel() {
        return isIntroLevel;
    }

    public TechAdvancement setIntroLevel(boolean intro) {
        isIntroLevel = intro;
        return this;
    }
    
    public boolean isUnofficial() {
        return unofficial;
    }
    
    public TechAdvancement setUnofficial(boolean unofficial) {
        this.unofficial = unofficial;
        return this;
    }
    
    public TechAdvancement setTechRating(int rating) {
        techRating = rating;
        return this;
    }
    
    public int getTechRating() {
        return techRating;
    }
    
    public TechAdvancement setAvailability(int... av) {
        System.arraycopy(av, 0, availability, 0, Math.min(av.length, availability.length));
        return this;
    }
    
    public TechAdvancement setAvailability(int era, int av) {
        if (era > 0 && era < availability.length) {
            availability[era] = av;
        }
        return this;
    }

    public int getBaseAvailability(int era) {
        if (era < 0 || era > availability.length) {
            return RATING_X;
        }
        return availability[era];
    }

    @Override
    public boolean isClan() {
        return techBase == TECH_BASE_CLAN;
    }

    @Override
    public boolean isMixedTech() {
        return techBase == TECH_BASE_ALL;
    }
}
