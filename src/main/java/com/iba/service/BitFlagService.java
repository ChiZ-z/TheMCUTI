package com.iba.service;

import com.iba.model.project.TermLang;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
public class BitFlagService {

    public enum StatusFlag {
        DEFAULT_WAS_CHANGED,
        FUZZY,
        AUTOTRANSLATED;

        private final int flag;

        StatusFlag() {
            this.flag = 1 << this.ordinal();
        }

        public int getValue() {
            return this.flag;
        }
    }

    /**
     * Get enum value from int flag value.
     *
     * @param statusValue - int value of the flag
     * @return enum value of flag on term lang
     */
    EnumSet<StatusFlag> getStatusFlags(int statusValue) {
        EnumSet<StatusFlag> statusFlags = EnumSet.noneOf(StatusFlag.class);
        EnumSet<StatusFlag> flags = EnumSet.allOf(StatusFlag.class);
        flags.forEach(flag -> {
                    int value = flag.getValue();
                    if ((value & statusValue) == value) {
                        statusFlags.add(flag);
                    }
                }
        );
        return statusFlags;
    }

    /**
     * If TermLang don't have flag,
     * add this flag in TermLang
     *
     * @param termLang - TermLang
     * @param flag     - flag to add
     */
    public void addFlag(TermLang termLang, BitFlagService.StatusFlag flag) {
        if (!isContainsFlag(termLang.getStatus(), flag)) {
            termLang.setStatus(termLang.getStatus() + flag.getValue());
        }
    }

    /**
     * If TermLang have flag,
     * drop this flag in TermLang
     *
     * @param termLang - TermLang
     * @param flag     - flag to delete
     */
    public void dropFlag(TermLang termLang, BitFlagService.StatusFlag flag) {
        if (isContainsFlag(termLang.getStatus(), flag)) {
            termLang.setStatus(termLang.getStatus() - flag.getValue());
        }
    }

    /**
     * If TermLang have any flags,
     * reset them.
     *
     * @param termLang - TermLang
     */
    public void dropAllFlags(TermLang termLang) {
        if (isContainsFlag(termLang.getStatus(), StatusFlag.DEFAULT_WAS_CHANGED)) {
            termLang.setStatus(termLang.getStatus() - StatusFlag.DEFAULT_WAS_CHANGED.getValue());
        }
        if (isContainsFlag(termLang.getStatus(), StatusFlag.FUZZY)) {
            termLang.setStatus(termLang.getStatus() - StatusFlag.FUZZY.getValue());
        }
        if (isContainsFlag(termLang.getStatus(), StatusFlag.AUTOTRANSLATED)) {
            termLang.setStatus(termLang.getStatus() - StatusFlag.AUTOTRANSLATED.getValue());
        }
    }

    /**
     * If TermLang have flag,
     * drop this flag in TermLang,
     * drop flag from exist TermLang
     *
     * @param termLang - TermLang
     * @param flag     - flag to delete
     * @param exist    - exist
     */
    public void dropFlagDropFromTermLang(TermLang termLang, BitFlagService.StatusFlag flag, TermLang exist) {
        if (isContainsFlag(termLang.getStatus(), flag)) {
            termLang.setStatus(termLang.getStatus() - flag.getValue());
            exist.getFlags().remove(flag.name());
        }
    }

    /**
     * If TermLang don't have flag,
     * add this flag in TermLang,
     * add flag from exist TermLang
     *
     * @param termLang - TermLang
     * @param flag     - flag to add
     * @param exist    - exist
     */
    public void addFlagAddToTermLang(TermLang termLang, BitFlagService.StatusFlag flag, TermLang exist) {
        if (!isContainsFlag(termLang.getStatus(), flag)) {
            termLang.setStatus(termLang.getStatus() + flag.getValue());
            exist.getFlags().add(flag.name());
        }
    }

    /**
     * Check if termLang has flag.
     *
     * @param status - status of termLang
     * @param flag   - flag for check
     * @return true if flag exist, else false
     */
    public boolean isContainsFlag(int status, StatusFlag flag) {
        EnumSet<BitFlagService.StatusFlag> enumSet = getStatusFlags(status);
        if (enumSet.isEmpty()) {
            return false;
        }
        return enumSet.contains(flag);
    }



    /*public void updateFlag(String newVal,TermLang termLang){
        if (newVal == null) {
            logger.error("New value is null");
            if (isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.FUZZY)) {
                dropFlag(termLang, BitFlagService.StatusFlag.FUZZY);
            }
            newVal = "";
        }
        if (isContainsFlag(termLang.getStatus(), BitFlagService.StatusFlag.AUTOTRANSLATED)) {
            dropFlag(termLang, BitFlagService.StatusFlag.AUTOTRANSLATED);
        }
    }*/

}
